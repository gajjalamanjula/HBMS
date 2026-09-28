package com.hms.hms.modules.admin.service.serviceimpl;

import com.hms.hms.common.enums.BedStatus;
import com.hms.hms.common.enums.ComplaintStatus;
import com.hms.hms.common.enums.FileType;
import com.hms.hms.common.enums.Role;
import com.hms.hms.common.exception.CustomException;
import com.hms.hms.common.service.FileService;
import com.hms.hms.modules.admin.service.OwnerService;
import com.hms.hms.modules.auth.service.AuthService;
import com.hms.hms.modules.branch.entity.Branch;
import com.hms.hms.modules.branch.repository.BranchRepository;
import com.hms.hms.modules.complaint.dto.ComplaintResponseDTO;
import com.hms.hms.modules.complaint.entity.Complaint;
import com.hms.hms.modules.complaint.repository.ComplaintRepository;
import com.hms.hms.modules.monthlyrent.repository.MonthlyRentInvoiceRepository;
import com.hms.hms.modules.notification.event.NotificationEvent;
import com.hms.hms.modules.room.entity.Room;
import com.hms.hms.modules.room.repository.BedRepository;
import com.hms.hms.modules.room.repository.RoomRepository;
import com.hms.hms.modules.user.dto.CreateUserRequest;
import com.hms.hms.modules.user.dto.ManagerResponseDTO;
import com.hms.hms.modules.user.entity.User;
import com.hms.hms.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OwnerServiceImpl implements OwnerService {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final RoomRepository roomRepository;
    private final BedRepository bedRepository;
    private final ComplaintRepository complaintRepository;
    private final FileService fileService;
    private final ApplicationEventPublisher eventPublisher;
    private final MonthlyRentInvoiceRepository monthlyRentInvoiceRepository;
    @Override
    public String createManager(CreateUserRequest request) {

        if (request.getBranchId() == null) {
            throw new CustomException("Branch is required to create Manager", HttpStatus.BAD_REQUEST);
        }

        String aadhaarPath = fileService.uploadFile(
                request.getAadhaarPhoto(),
                FileType.KYC_IMAGE
        );

        return authService.createUserByRole(
                request.getEmail(),
                Role.MANAGER,
                request.getName(),
                request.getPhone(),
                request.getAadhaarNumber(),
                aadhaarPath,
                request.getBranchId()
        );
    }

    @Override
    public String createStaff(CreateUserRequest request) {

        if (request.getBranchId() == null) {
            throw new CustomException("Branch is required to create Staff", HttpStatus.BAD_REQUEST);
        }

        String aadhaarPath = fileService.uploadFile(
                request.getAadhaarPhoto(),
                FileType.KYC_IMAGE
        );

        return authService.createUserByRole(
                request.getEmail(),
                Role.STAFF,
                request.getName(),
                request.getPhone(),
                request.getAadhaarNumber(),
                aadhaarPath,
                request.getBranchId()
        );
    }

    @Override
    public String createUser(CreateUserRequest request) {

        if (request.getBranchId() == null) {
            throw new CustomException("Branch is required to create Tenant", HttpStatus.BAD_REQUEST);
        }

        String aadhaarPath = fileService.uploadFile(
                request.getAadhaarPhoto(),
                FileType.KYC_IMAGE
        );

        return authService.createUserByRole(
                request.getEmail(),
                Role.TENANT,
                request.getName(),
                request.getPhone(),
                request.getAadhaarNumber(),
                aadhaarPath,
                request.getBranchId()
        );
    }

    @Override
    public Map<String, Object> getDashboard(String email) {

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));

        List<Branch> branches = branchRepository.findByOwnerId(owner.getId());
        List<UUID> branchIds = branches.stream().map(Branch::getId).toList();

        Map<String, Object> data = new HashMap<>();

        data.put("totalBranches", branches.size());
        data.put("totalTenants", userRepository.countByBranch_IdInAndRole(branchIds, Role.TENANT));
        data.put("totalStaff", userRepository.countByBranch_IdInAndRole(branchIds, Role.STAFF));

        List<Room> rooms = roomRepository.findByBranchIdIn(branchIds);
        List<UUID> roomIds = rooms.stream().map(Room::getId).toList();

        data.put("totalBeds", bedRepository.countByRoomIdIn(roomIds));
        data.put("occupiedBeds", bedRepository.countByStatusAndRoomIdIn(BedStatus.OCCUPIED, roomIds));
        data.put("availableBeds", bedRepository.countByStatusAndRoomIdIn(BedStatus.AVAILABLE, roomIds));

        return data;
    }

    @Override
    public List<Map<String, Object>> getBranches(String email) {

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));

        List<Branch> branches = branchRepository.findByOwnerId(owner.getId());

        return branches.stream().map(branch -> {

            List<Room> rooms = roomRepository.findByBranchId(branch.getId());
            List<UUID> roomIds = rooms.stream().map(Room::getId).toList();

            long totalBeds = bedRepository.countByRoomIdIn(roomIds);
            long occupied = bedRepository.countByStatusAndRoomIdIn(BedStatus.OCCUPIED, roomIds);

            Map<String, Object> map = new HashMap<>();
            map.put("branchId", branch.getId());
            map.put("name", branch.getHostelName());
            map.put("location", branch.getLocation());
            map.put("totalBeds", totalBeds);
            map.put("occupiedBeds", occupied);

            return map;

        }).toList();
    }

    @Override
    public Map<String, Object> getBranchStats(UUID branchId) {

        List<Room> rooms = roomRepository.findByBranchId(branchId);
        List<UUID> roomIds = rooms.stream().map(Room::getId).toList();

        Map<String, Object> map = new HashMap<>();
        map.put("totalBeds", bedRepository.countByRoomIdIn(roomIds));
        map.put("occupied", bedRepository.countByStatusAndRoomIdIn(BedStatus.OCCUPIED, roomIds));
        map.put("available", bedRepository.countByStatusAndRoomIdIn(BedStatus.AVAILABLE, roomIds));

        return map;
    }
    @Override
    public List<Map<String, Object>> getRevenue(String email) {

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new CustomException(
                                "Owner not found",
                                HttpStatus.NOT_FOUND
                        ));

        List<Branch> branches =
                branchRepository.findByOwnerId(owner.getId());

        List<UUID> branchIds = branches.stream()
                .map(Branch::getId)
                .toList();

        if (branchIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Object[]> results =
                monthlyRentInvoiceRepository
                        .getRevenueByMonthAndYear(branchIds);

        return results.stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();

                    map.put("month", row[0]);
                    map.put("year", row[1]);
                    map.put("revenue", row[2]);

                    return map;
                })
                .toList();
    }

    @Override
    public Map<String, Object> getBedSummary(String email) {

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));

        List<Branch> branches = branchRepository.findByOwnerId(owner.getId());
        List<UUID> branchIds = branches.stream().map(Branch::getId).toList();

        List<Room> rooms = roomRepository.findByBranchIdIn(branchIds);
        List<UUID> roomIds = rooms.stream().map(Room::getId).toList();

        Map<String, Object> data = new HashMap<>();
        data.put("total", bedRepository.countByRoomIdIn(roomIds));
        data.put("occupied", bedRepository.countByStatusAndRoomIdIn(BedStatus.OCCUPIED, roomIds));
        data.put("available", bedRepository.countByStatusAndRoomIdIn(BedStatus.AVAILABLE, roomIds));
        data.put("maintenance", bedRepository.countByStatusAndRoomIdIn(BedStatus.MAINTENANCE, roomIds));

        return data;
    }

    @Override
    public List<ComplaintResponseDTO> getComplaints(String email) {

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));

        List<Branch> branches = branchRepository.findByOwnerId(owner.getId());
        List<UUID> branchIds = branches.stream().map(Branch::getId).toList();

        return complaintRepository.findByBranchIdIn(branchIds)
                .stream()
                .map(c -> ComplaintResponseDTO.builder()
                        .id(c.getId())
                        .description(c.getDescription())
                        .status(c.getStatus())
                        .build())
                .toList();
    }

    @Override
    public String escalateComplaint(UUID complaintId, String email) {

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new CustomException("Complaint not found", HttpStatus.NOT_FOUND));

        Branch branch = branchRepository.findById(complaint.getBranchId())
                .orElseThrow(() -> new CustomException("Branch not found", HttpStatus.NOT_FOUND));

        if (!branch.getOwnerId().equals(owner.getId())) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }

        complaint.setStatus(ComplaintStatus.ESCALATED);
        complaintRepository.save(complaint);

        eventPublisher.publishEvent(
                new NotificationEvent(
                        owner.getId(),
                        "Complaint Escalated",
                        "Complaint has been escalated successfully"
                )
        );

        return "Complaint escalated successfully";
    }

    @Override
    public List<ManagerResponseDTO> getManagers(String email) {

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));

        List<Branch> branches = branchRepository.findByOwnerId(owner.getId());

        if (branches.isEmpty()) {
            return new ArrayList<>();
        }

        List<UUID> branchIds = branches.stream()
                .map(Branch::getId)
                .toList();

        List<User> managers = userRepository.findByBranch_IdInAndRole(branchIds, Role.MANAGER);

        return managers.stream().map(manager -> {

            Branch branch = manager.getBranch();

            return ManagerResponseDTO.builder()
                    .id(manager.getId())
                    .name(manager.getName())
                    .email(manager.getEmail())
                    .phone(manager.getPhone())
                    .branchId(branch != null ? branch.getId() : null)
                    .branchName(branch != null ? branch.getHostelName() : null)
                    .createdAt(manager.getCreatedAt())
                    .build();

        }).toList();
    }

    @Override
    public Map<String, Object> getStaffCount(String email) {

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));

        List<UUID> branchIds = branchRepository.findByOwnerId(owner.getId())
                .stream()
                .map(Branch::getId)
                .toList();

        long total = userRepository.countByBranch_IdInAndRole(branchIds, Role.STAFF);

        Map<String, Object> map = new HashMap<>();
        map.put("totalStaff", total);

        return map;
    }

    @Override
    public Map<String, Object> getTenantCount(String email) {

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));

        List<Branch> branches = branchRepository.findByOwnerId(owner.getId());
        List<UUID> branchIds = branches.stream().map(Branch::getId).toList();

        long total = userRepository.countByBranch_IdInAndRole(branchIds, Role.TENANT);

        Map<String, Object> map = new HashMap<>();
        map.put("totalTenants", total);

        return map;
    }
}