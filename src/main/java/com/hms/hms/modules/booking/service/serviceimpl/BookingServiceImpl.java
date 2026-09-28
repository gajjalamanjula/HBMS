package com.hms.hms.modules.booking.service.serviceimpl;

import com.hms.hms.common.enums.BedStatus;
import com.hms.hms.common.enums.BookingStatus;
import com.hms.hms.common.enums.Role;
import com.hms.hms.common.exception.CustomException;
import com.hms.hms.modules.booking.dto.PublicBookingResponse;
import com.hms.hms.modules.booking.entity.Booking;
import com.hms.hms.modules.booking.repository.BookingRepository;
import com.hms.hms.modules.booking.service.BookingService;
import com.hms.hms.modules.branch.entity.Branch;
import com.hms.hms.modules.branch.repository.BranchRepository;
import com.hms.hms.modules.notification.event.NotificationEvent;
import com.hms.hms.modules.room.entity.Bed;
import com.hms.hms.modules.room.entity.Room;
import com.hms.hms.modules.room.repository.BedRepository;
import com.hms.hms.modules.room.repository.RoomRepository;
import com.hms.hms.modules.user.entity.User;
import com.hms.hms.modules.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BedRepository bedRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoomRepository roomRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public String approveBooking(UUID bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new CustomException(
                                "Booking not found",
                                HttpStatus.NOT_FOUND
                        ));

        // GET LOGGED-IN MANAGER
        String managerEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User manager = userRepository.findByEmail(managerEmail)
                .orElseThrow(() ->
                        new CustomException(
                                "Manager not found",
                                HttpStatus.NOT_FOUND
                        ));

          // CHECK MANAGER BRANCH ACCESS
        if (!manager.getBranch().getId()
                .equals(booking.getBranchId())) {

            throw new CustomException(
                    "You cannot approve bookings of another branch",
                    HttpStatus.FORBIDDEN
            );
        }
        if (booking.getStatus() != BookingStatus.PENDING) {

            throw new CustomException(
                    "Booking already processed",
                    HttpStatus.BAD_REQUEST
            );
        }

        User user = booking.getUser();

        Branch branch = branchRepository.findById(
                booking.getBranchId()
        ).orElseThrow(() ->
                new CustomException(
                        "Branch not found",
                        HttpStatus.NOT_FOUND
                ));

        Bed bed = bedRepository.findById(
                booking.getBedId()
        ).orElseThrow(() ->
                new CustomException(
                        "Bed not found",
                        HttpStatus.NOT_FOUND
                ));

        if (bed.getStatus() != BedStatus.RESERVED) {

            throw new CustomException(
                    "Bed already occupied",
                    HttpStatus.BAD_REQUEST
            );
        }

        // APPROVED BOOKING MEANS TENANT OCCUPIES BED
        bed.setStatus(BedStatus.OCCUPIED);
        bed.setTenantId(user.getId());
        bedRepository.save(bed);

      // PUBLIC USER BECOMES TENANT
        user.setRole(Role.TENANT);
        user.setBranch(branch);

        userRepository.save(user);

       // UPDATE BOOKING STATUS
        booking.setStatus(BookingStatus.APPROVED);

        bookingRepository.save(booking);
        eventPublisher.publishEvent(
                new NotificationEvent(
                        user.getId(),
                        "Booking Approved",
                        "Your booking has been approved successfully"
                )
        );
        return "Booking approved successfully";
    }

    @Transactional
    @Override
    public String rejectBooking(UUID bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new CustomException(
                                "Booking not found",
                                HttpStatus.NOT_FOUND
                        ));

        if (booking.getStatus() != BookingStatus.PENDING) {

            throw new CustomException(
                    "Booking already processed",
                    HttpStatus.BAD_REQUEST
            );
        }

        // UPDATE STATUS
        booking.setStatus(BookingStatus.CANCELLED);
        // MAKE BED AVAILABLE AGAIN
        Bed bed = bedRepository.findById(
                booking.getBedId()
        ).orElseThrow(() ->
                new CustomException(
                        "Bed not found",
                        HttpStatus.NOT_FOUND
                ));

        bed.setStatus(BedStatus.AVAILABLE);
        bedRepository.save(bed);
        // REFUND STATUS
        booking.setRefundProcessed(true);

        // OPTIONAL REFUND ID
        booking.setRefundId(
                UUID.randomUUID().toString()
        );

        bookingRepository.save(booking);
        eventPublisher.publishEvent(
                new NotificationEvent(
                        booking.getUser().getId(),
                        "Booking Rejected",
                        "Your booking has been rejected and refund processed"
                )
        );
        return "Booking rejected and refund processed successfully";
    }

    @Override
    public List<Booking> getBranchBookings() {

        // LOGGED-IN MANAGER EMAIL
        String managerEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User manager = userRepository.findByEmail(managerEmail)
                .orElseThrow(() ->
                        new CustomException(
                                "Manager not found",
                                HttpStatus.NOT_FOUND
                        ));

        // RETURN ONLY THIS BRANCH BOOKINGS
        return bookingRepository.findByBranchId(
                manager.getBranch().getId()
        );
    }

    @Override
    public List<PublicBookingResponse> getManagerBookings(
            String email
    ) {

        User manager = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new CustomException(
                                "Manager not found",
                                HttpStatus.NOT_FOUND
                        ));

        if (manager.getBranch() == null) {

            throw new CustomException(
                    "Manager not assigned to branch",
                    HttpStatus.BAD_REQUEST
            );
        }

        UUID branchId = manager.getBranch().getId();

        List<Booking> bookings =
                bookingRepository.findByBranchId(branchId);

        if (bookings.isEmpty()) {
            return List.of();
        }

        List<UUID> bedIds = bookings.stream()
                .map(Booking::getBedId)
                .distinct()
                .toList();

        Map<UUID, Bed> bedMap = bedRepository.findByIdIn(bedIds)
                .stream()
                .collect(Collectors.toMap(
                        Bed::getId,
                        Function.identity()
                ));

        List<UUID> roomIds = bedMap.values()
                .stream()
                .map(Bed::getRoomId)
                .distinct()
                .toList();

        Map<UUID, Room> roomMap = roomRepository.findByIdIn(roomIds)
                .stream()
                .collect(Collectors.toMap(
                        Room::getId,
                        Function.identity()
                ));

        Branch branch = branchRepository.findById(branchId)
                .orElse(null);

        return bookings.stream()
                .map(booking -> {

                    Bed bed = bedMap.get(booking.getBedId());

                    Room room = null;

                    if (bed != null) {
                        room = roomMap.get(bed.getRoomId());
                    }

                    return PublicBookingResponse.builder()
                            .bookingId(booking.getId())

                            .publicUserName(
                                    booking.getUser().getName()
                            )

                            .publicUserPhone(
                                    booking.getUser().getPhone()
                            )

                            .publicUserEmail(
                                    booking.getUser().getEmail()
                            )

                            .hostelName(
                                    branch != null
                                            ? branch.getHostelName()
                                            : null
                            )

                            .roomNumber(
                                    room != null
                                            ? room.getRoomNumber()
                                            : null
                            )

                            .bedNumber(
                                    bed != null
                                            ? bed.getBedNumber()
                                            : null
                            )

                            .amountPaid(
                                    booking.getAmount()
                            )

                            .status(
                                    booking.getStatus()
                            )

                            .build();
                })
                .toList();
    }
    @Transactional
    @Override
    public String checkoutTenant(UUID bookingId) {

        Booking booking =
                bookingRepository.findByIdAndStatus(
                                bookingId,
                                BookingStatus.APPROVED
                        )
                        .orElseThrow(() ->
                                new CustomException(
                                        "Active booking not found",
                                        HttpStatus.NOT_FOUND
                                ));

        Bed bed = bedRepository.findById(
                booking.getBedId()
        ).orElseThrow(() ->
                new CustomException(
                        "Bed not found",
                        HttpStatus.NOT_FOUND
                ));

        booking.setStatus(
                BookingStatus.CHECKED_OUT
        );

        booking.setCheckoutDate(
                LocalDateTime.now()
        );

        bed.setStatus(
                BedStatus.AVAILABLE
        );

        bookingRepository.save(booking);
        bedRepository.save(bed);

        eventPublisher.publishEvent(
                new NotificationEvent(
                        booking.getUser().getId(),
                        "Checkout Successful",
                        "You have successfully checked out"
                )
        );

        return "Tenant checked out successfully";
    }

}