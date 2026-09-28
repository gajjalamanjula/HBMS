package com.hms.hms.modules.admin.service.serviceimpl;

import com.hms.hms.common.enums.*;
import com.hms.hms.common.exception.CustomException;
import com.hms.hms.modules.complaint.dto.ComplaintResponseDTO;
import com.hms.hms.modules.room.dto.RoomResponseDTO;
import com.hms.hms.modules.user.dto.StaffResponseDTO;
import com.hms.hms.common.enums.BookingStatus;
import com.hms.hms.common.enums.FileType;
import com.hms.hms.common.enums.Role;
import com.hms.hms.common.exception.CustomException;
import com.hms.hms.common.service.FileService;
import com.hms.hms.modules.admin.service.ManagerService;
import com.hms.hms.modules.auth.service.AuthService;
import com.hms.hms.modules.booking.entity.Booking;
import com.hms.hms.modules.booking.repository.BookingRepository;
import com.hms.hms.modules.complaint.repository.ComplaintRepository;
import com.hms.hms.modules.room.dto.CreateRoomRequest;
import com.hms.hms.modules.room.entity.*;
import com.hms.hms.modules.room.repository.*;
import com.hms.hms.modules.user.dto.CreateUserRequest;
import com.hms.hms.modules.user.dto.StaffResponseDTO;
import com.hms.hms.modules.user.dto.UserResponse;
import com.hms.hms.modules.user.entity.User;
import com.hms.hms.modules.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.toList;
import org.springframework.context.ApplicationEventPublisher;
import com.hms.hms.modules.notification.event.NotificationEvent;
@Service
@RequiredArgsConstructor
public class ManagerServiceImpl implements ManagerService {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final BedRepository bedRepository;
    private final ComplaintRepository complaintRepository;
    private final FileService fileService;
    private final ApplicationEventPublisher eventPublisher;
    //  HELPER
    private User getCurrentManager() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Manager not found", HttpStatus.NOT_FOUND));
    }

    //  CREATE STAFF
    @Override
    public String createStaff(
            CreateUserRequest request,
            String email
    ) {
        User manager = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        "Manager not found",
                        HttpStatus.NOT_FOUND
                ));

        if (manager.getBranch() == null) {
            throw new CustomException("Manager not assigned to any branch", HttpStatus.BAD_REQUEST);
        }
        String aadhaarPath = fileService.uploadFile(
                request.getAadhaarPhoto(),
                FileType.KYC_IMAGE
        );


        String response = authService.createUserByRole(
                request.getEmail(),
                Role.STAFF,
                request.getName(),
                request.getPhone(),
                request.getAadhaarNumber(),
                aadhaarPath,
                manager.getBranch().getId()
        );

        eventPublisher.publishEvent(
                new NotificationEvent(
                        manager.getId(),
                        "Staff Account Created",
                        "New staff account created successfully"
                )
        );

        return response;
    }

    //  CREATE TENANT

    @Override
    public String createUser(
            CreateUserRequest request,
            String email
    ) {

        User manager = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        "Manager not found",
                        HttpStatus.NOT_FOUND
                ));

        if (manager.getBranch() == null) {
            throw new CustomException("Manager not assigned to any branch", HttpStatus.BAD_REQUEST);
        }
        String aadhaarPath = fileService.uploadFile(
                request.getAadhaarPhoto(),
                FileType.KYC_IMAGE
        );

        String response = authService.createUserByRole(
                request.getEmail(),
                Role.TENANT,
                request.getName(),
                request.getPhone(),
                request.getAadhaarNumber(),
                aadhaarPath,
                manager.getBranch().getId()
        );

        eventPublisher.publishEvent(
                new NotificationEvent(
                        manager.getId(),
                        "Tenant Account Created",
                        "New tenant account created successfully"
                )
        );

        return response;
    }

    //  DASHBOARD
    @Override
    public Map<String, Object> getDashboard() {

        User manager = getCurrentManager();
        UUID branchId = manager.getBranch().getId();

        List<Room> rooms = roomRepository.findByBranchId(branchId);
        List<UUID> roomIds = rooms.stream().map(Room::getId).toList();

        Map<String, Object> map = new HashMap<>();

        map.put("staffCount", userRepository.countByBranch_IdAndRole(branchId, Role.STAFF));
        map.put("tenantCount", userRepository.countByBranch_IdAndRole(branchId, Role.TENANT));
        map.put("totalBeds", bedRepository.countByRoomIdIn(roomIds));
        map.put("occupiedBeds", bedRepository.countByStatusAndRoomIdIn(BedStatus.OCCUPIED, roomIds));
        map.put("complaints", complaintRepository.countByBranchId(branchId));

        return map;
    }

    //  STAFF LIST
    @Override
    public List<StaffResponseDTO> getStaff() {

        User manager = getCurrentManager();

        return userRepository.findByBranch_IdAndRole(
                        manager.getBranch().getId(),
                        Role.STAFF
                ).stream()
                .map(u -> StaffResponseDTO.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .phone(u.getPhone())
                        .role(u.getRole().name())
                        .active(u.getActive())
                        .build())
                .toList();
    }

    //  UPDATE STAFF
    @Override
    public String updateStaff(UUID id, CreateUserRequest request) {

        User staff = userRepository.findById(id)
                .orElseThrow(() -> new CustomException("Staff not found", HttpStatus.NOT_FOUND));

        if (request.getName() != null) {
            staff.setName(request.getName());
        }
        staff.setPhone(request.getPhone());

        userRepository.save(staff);

        eventPublisher.publishEvent(
                new NotificationEvent(
                        staff.getId(),
                        "Profile Updated",
                        "Your staff profile has been updated"
                )
        );
        return "Staff updated";
    }

    //  DELETE STAFF
    @Override
    public String deleteStaff(UUID id) {

        User staff = userRepository.findById(id)
                .orElseThrow(() -> new CustomException("Staff not found", HttpStatus.NOT_FOUND));

        staff.setActive(false);
        userRepository.save(staff);

        eventPublisher.publishEvent(
                new NotificationEvent(
                        staff.getId(),
                        "Account Deactivated",
                        "Your staff account has been deactivated"
                )
        );
        return "Staff deactivated";
    }

    // TENANTS
    @Override
    public List<UserResponse> getTenants() {

        User manager = getCurrentManager();

        return userRepository.findByBranch_IdAndRole(
                        manager.getBranch().getId(),
                        Role.TENANT
                ).stream()
                .map(u -> UserResponse.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .email(u.getEmail())
                        .phone(u.getPhone())
                        .role(u.getRole())
                        .build())
                .toList();
    }

    // CHECKOUT TENANT
    @Transactional
    @Override
    public String checkoutTenant(UUID tenantId) {

        User manager = getCurrentManager();

        User tenant = userRepository.findById(tenantId)
                .orElseThrow(() ->
                        new CustomException(
                                "Tenant not found",
                                HttpStatus.NOT_FOUND
                        ));

        if (tenant.getBranch() == null ||
                !tenant.getBranch().getId().equals(manager.getBranch().getId())) {

            throw new CustomException(
                    "You cannot checkout tenant of another branch",
                    HttpStatus.FORBIDDEN
            );
        }

        Booking booking = bookingRepository
                .findByUser_IdAndStatus(
                        tenant.getId(),
                        BookingStatus.APPROVED
                )
                .orElseThrow(() ->
                        new CustomException(
                                "Active booking not found for tenant",
                                HttpStatus.NOT_FOUND
                        ));

        Bed bed = bedRepository.findById(booking.getBedId())
                .orElseThrow(() ->
                        new CustomException(
                                "Bed not found",
                                HttpStatus.NOT_FOUND
                        ));

        booking.setStatus(BookingStatus.CHECKED_OUT);
        booking.setCheckoutDate(LocalDateTime.now());

        bed.setStatus(BedStatus.AVAILABLE);
        bed.setTenantId(null);

        tenant.setActive(false);

        bookingRepository.save(booking);
        bedRepository.save(bed);
        userRepository.save(tenant);

        eventPublisher.publishEvent(
                new NotificationEvent(
                        tenant.getId(),
                        "Checkout Completed",
                        "Your hostel checkout has been completed"
                )
        );

        return "Tenant checked out successfully";
    }
    //  COUNTS
    @Override
    public Map<String, Object> getCounts() {

        User manager = getCurrentManager();
        UUID branchId = manager.getBranch().getId();

        return Map.of(
                "staff", userRepository.countByBranch_IdAndRole(branchId, Role.STAFF),
                "tenants", userRepository.countByBranch_IdAndRole(branchId, Role.TENANT)
        );
    }

    //  COMPLAINTS
    @Override
    public List<ComplaintResponseDTO> getComplaints() {

        User manager = getCurrentManager();

        return complaintRepository.findByBranchId(
                        manager.getBranch().getId()
                ).stream()
                .map(c -> ComplaintResponseDTO.builder()
                        .id(c.getId())
                        .description(c.getDescription())
                        .status(c.getStatus())
                        .build())
                .toList();
    }

    //  ASSIGN COMPLAINT
    @Override
    public String assignComplaint(UUID complaintId, UUID staffId) {

        var complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new CustomException("Complaint not found", HttpStatus.NOT_FOUND));

        complaint.setAssignedTo(staffId);
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);

        complaintRepository.save(complaint);
        eventPublisher.publishEvent(
                new NotificationEvent(
                        staffId,
                        "Complaint Assigned",
                        "A complaint has been assigned to you"
                )
        );
        return "Complaint assigned";
    }

    //  ROOMS
    @Override
    public List<RoomResponseDTO> getRooms() {

        User manager = getCurrentManager();

        return roomRepository.findByBranchId(manager.getBranch().getId())
                .stream()
                .map(r -> RoomResponseDTO.builder()
                        .id(r.getId())
                        .roomNumber(r.getRoomNumber())
                        .roomType(r.getRoomType())
                        .totalBeds(r.getTotalBeds())
                        .build())
                .toList();
    }
    //  CREATE ROOM
    @Override
    public String createRoom(CreateRoomRequest request) {

        User manager = getCurrentManager();

        Room room = Room.builder()
                .roomNumber(request.getRoomNumber())
                .floor(request.getFloor())
                .roomType(request.getRoomType())
                .totalBeds(request.getTotalBeds())
                .branchId(manager.getBranch().getId())
                .build();

        roomRepository.save(room);
        eventPublisher.publishEvent(
                new NotificationEvent(
                        manager.getId(),
                        "Room Created",
                        "New room created successfully"
                )
        );
        for (int i = 0; i < request.getTotalBeds(); i++) {
            bedRepository.save(
                    Bed.builder()
                            .roomId(room.getId())
                            .status(BedStatus.AVAILABLE)
                            .build()
            );
        }

        return "Room created";
    }

    //  UPDATE BED STATUS
    @Override
    public String updateBedStatus(UUID bedId, String status) {

        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() -> new CustomException("Bed not found", HttpStatus.NOT_FOUND));

        bed.setStatus(BedStatus.valueOf(status.toUpperCase()));
        bedRepository.save(bed);
        eventPublisher.publishEvent(
                new NotificationEvent(
                        getCurrentManager().getId(),
                        "Bed Status Updated",
                        "Bed status updated to " + status
                )
        );
        return "Bed updated";
    }
}