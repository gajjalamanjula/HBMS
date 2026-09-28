package com.hms.hms.modules.publicuser.service.serviceimpl;

import com.hms.hms.common.enums.BedStatus;
import com.hms.hms.common.enums.BookingStatus;
import com.hms.hms.common.enums.Role;
import com.hms.hms.common.exception.CustomException;
import com.hms.hms.modules.booking.entity.Booking;
import com.hms.hms.modules.booking.repository.BookingRepository;
import com.hms.hms.modules.branch.entity.Branch;
import com.hms.hms.modules.branch.repository.BranchRepository;
import com.hms.hms.modules.notification.event.NotificationEvent;
import com.hms.hms.modules.publicuser.dto.*;
import com.hms.hms.modules.publicuser.service.PublicService;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.hms.hms.common.enums.HostelType;
import com.hms.hms.modules.branch.dto.BranchResponse;
@Service
@RequiredArgsConstructor
public class PublicServiceImpl implements PublicService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BranchRepository branchRepository;
    private final RoomRepository roomRepository;
    private final BedRepository bedRepository;
    private final BookingRepository bookingRepository;
    private final ApplicationEventPublisher eventPublisher;
    // PUBLIC USER REGISTER
    @Override
    public String register(PublicRegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {

            throw new CustomException(
                    "Email already exists",
                    HttpStatus.CONFLICT
            );
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(
                        passwordEncoder.encode(request.getPassword())
                )
                .role(Role.PUBLIC_USER)
                .active(true)
                .verified(true)
                .mustChangePassword(false)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        eventPublisher.publishEvent(
                new NotificationEvent(
                        user.getId(),
                        "Registration Successful",
                        "Your account has been registered successfully"
                )
        );
        return "Public user registered successfully";
    }

    // GET ALL BRANCHES

    @Override
    public List <BranchResponse> getBranches() {

        return branchRepository.findAll()
                .stream()
                .map(branch -> BranchResponse.builder()

                        .id(branch.getId())

                        .location(branch.getLocation())

                        .imageUrl(branch.getImageUrl())

                        .monthlyRent(branch.getMonthlyRent())

                        .hostelType(branch.getHostelType())

                        // PUBLIC HOSTEL DETAILS

                        .hostelName(branch.getHostelName())

                        .city(branch.getCity())

                        .area(branch.getArea())

                        .colony(branch.getColony())

                        .roadNo(branch.getRoadNo())

                        .landmark(branch.getLandmark())

                        .fullAddress(branch.getFullAddress())

                        .description(branch.getDescription())

                        .managerName(branch.getManagerName())

                        .managerPhone(branch.getManagerPhone())

                        .whatsappNumber(branch.getWhatsappNumber())

                        .totalFloors(branch.getTotalFloors())

                        .amenities(branch.getAmenities())

                        .mapLocationUrl(branch.getMapLocationUrl())

                        .build())
                .toList();
    }
    @Override
    public List<BranchResponse> getMenHostels(String location) {
        return getHostelsByType(HostelType.MALE, location);
    }

    @Override
    public List<BranchResponse> getWomenHostels(String location) {
        return getHostelsByType(HostelType.FEMALE, location);
    }

    @Override
    public List<BranchResponse> getCoLivingHostels(String location) {
        return getHostelsByType(HostelType.CO_LIVING, location);
    }


    @Override
    public List<RoomAvailabilityResponse> getAvailableRooms(
            UUID branchId
    ) {

        List<Room> rooms =
                roomRepository.findByBranchId(branchId);

        if (rooms.isEmpty()) {

            throw new CustomException(
                    "No rooms found",
                    HttpStatus.NOT_FOUND
            );
        }

        return rooms.stream()
                .map(room -> {

                    List<Bed> beds =
                            bedRepository.findByRoomId(room.getId());

                    long availableBeds =
                            beds.stream()
                                    .filter(bed ->
                                            bed.getStatus() == BedStatus.AVAILABLE
                                    )
                                    .count();

                    return RoomAvailabilityResponse.builder()
                            .roomId(room.getId())

                            .roomNumber(room.getRoomNumber())

                            .roomType(
                                    room.getRoomType()
                            )

                            .totalBeds(room.getTotalBeds())

                            .availableBeds((int) availableBeds)

                            .fullyOccupied(
                                    availableBeds == 0
                            )

                            .build();
                })
                .toList();
    }
    @Override
    public List<BedResponse> getBedsByRoom(UUID roomId) {

        List<Bed> beds =
                bedRepository.findByStatusAndRoomIdIn(
                        BedStatus.AVAILABLE,
                        List.of(roomId)
                );

        if (beds.isEmpty()) {

            throw new CustomException(
                    "No beds available in this room",
                    HttpStatus.NOT_FOUND
            );
        }

        return beds.stream()
                .map(bed -> BedResponse.builder()

                        .id(bed.getId())

                        .bedNumber(bed.getBedNumber())

                        .status(bed.getStatus())

                        .build())
                .toList();
    }
    // GET AVAILABLE BEDS
    @Override
    public List<BedResponse> getBeds(UUID branchId) {

        List<Room> rooms =
                roomRepository.findByBranchId(branchId);

        if (rooms.isEmpty()) {

            throw new CustomException(
                    "No rooms found for this branch",
                    HttpStatus.NOT_FOUND
            );
        }

        List<UUID> roomIds = rooms.stream()
                .map(Room::getId)
                .toList();

        List<Bed> beds =
                bedRepository.findByStatusAndRoomIdIn(
                        BedStatus.AVAILABLE,
                        roomIds
                );

        if (beds.isEmpty()) {

            throw new CustomException(
                    "No beds available",
                    HttpStatus.NOT_FOUND
            );
        }

        return beds.stream()
                .map(bed -> BedResponse.builder()
                        .id(bed.getId())
                        .bedNumber(bed.getBedNumber())
                        .status(bed.getStatus())
                        .build())
                .toList();
    }
    @Override
    public String payFullAmount(UUID bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new CustomException(
                                "Booking not found",
                                HttpStatus.NOT_FOUND
                        ));

        if (booking.getStatus() != BookingStatus.APPROVED) {

            throw new CustomException(
                    "Booking not approved yet",
                    HttpStatus.BAD_REQUEST
            );
        }

        booking.setFullPaymentPaid(true);

        booking.setStatus(BookingStatus.PAID);

        bookingRepository.save(booking);
        eventPublisher.publishEvent(
                new NotificationEvent(
                        booking.getUser().getId(),
                        "Payment Successful",
                        "Your full payment completed successfully"
                )
        );
        return "Full payment successful";
    }
    // BOOKING REQUEST
    @Transactional
    @Override
    public BookingResponse book(BookingRequest request){

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new CustomException(
                                "User not found",
                                HttpStatus.NOT_FOUND
                        ));

        // ONLY PUBLIC USER CAN BOOK
        if (user.getRole() != Role.PUBLIC_USER) {

            throw new CustomException(
                    "Only public users can request booking",
                    HttpStatus.BAD_REQUEST
            );
        }

        // GET BED
        Bed bed = bedRepository.findById(request.getBedId())
                .orElseThrow(() ->
                        new CustomException(
                                "Bed not found",
                                HttpStatus.NOT_FOUND
                        ));

        // CHECK BED STATUS
        if (bed.getStatus() != BedStatus.AVAILABLE) {

            throw new CustomException(
                    "Bed not available",
                    HttpStatus.BAD_REQUEST
            );
        }

// VALIDATE ROOM
        Room room = roomRepository.findById(
                bed.getRoomId()
        ).orElseThrow(() ->
                new CustomException(
                        "Room not found",
                        HttpStatus.NOT_FOUND
                ));

        if (!room.getBranchId().equals(request.getBranchId())) {
            throw new CustomException(
                    "Selected bed does not belong to this branch",
                    HttpStatus.BAD_REQUEST
            );
        }

// GET BRANCH FOR RENT AMOUNT
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() ->
                        new CustomException(
                                "Branch not found",
                                HttpStatus.NOT_FOUND
                        ));

// CREATE BOOKING REQUEST
        Booking booking = Booking.builder()
                .user(user)
                .branchId(request.getBranchId())
                .bedId(request.getBedId())

                .amount(branch.getMonthlyRent())
                .totalRent(branch.getMonthlyRent())
                .advanceAmount(branch.getMonthlyRent())

                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        bookingRepository.save(booking);
        bed.setStatus(BedStatus.RESERVED);

        bedRepository.save(bed);
        eventPublisher.publishEvent(
                new NotificationEvent(
                        user.getId(),
                        "Booking Requested",
                        "Your booking request submitted successfully"
                )
        );
        return BookingResponse.builder()
                .message("Booking request submitted successfully")
                .bookingId(booking.getId())
                .status(booking.getStatus())
                .build();
    }

    private List<BranchResponse> getHostelsByType(
            HostelType hostelType,
            String location
    ) {

        List<Branch> branches;

        if (location != null && !location.isBlank()) {

            branches = branchRepository
                    .findByHostelTypeAndAreaContainingIgnoreCase(
                            hostelType,
                            location
                    );

        } else {

            branches = branchRepository.findByHostelType(
                    hostelType
            );
        }

        return branches.stream()
                .map(branch -> BranchResponse.builder()
                        .id(branch.getId())
                        .location(branch.getLocation())
                        .imageUrl(branch.getImageUrl())
                        .monthlyRent(branch.getMonthlyRent())
                        .hostelType(branch.getHostelType())
                        .hostelName(branch.getHostelName())
                        .city(branch.getCity())
                        .area(branch.getArea())
                        .colony(branch.getColony())
                        .roadNo(branch.getRoadNo())
                        .landmark(branch.getLandmark())
                        .fullAddress(branch.getFullAddress())
                        .description(branch.getDescription())
                        .managerName(branch.getManagerName())
                        .managerPhone(branch.getManagerPhone())
                        .whatsappNumber(branch.getWhatsappNumber())
                        .totalFloors(branch.getTotalFloors())
                        .amenities(branch.getAmenities())
                        .mapLocationUrl(branch.getMapLocationUrl())
                        .build())
                .toList();
    }
}