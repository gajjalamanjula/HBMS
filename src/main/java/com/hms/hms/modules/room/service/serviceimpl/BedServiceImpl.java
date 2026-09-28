package com.hms.hms.modules.room.service.serviceimpl;

import com.hms.hms.common.enums.BedStatus;
import com.hms.hms.common.exception.CustomException;
import com.hms.hms.modules.notification.event.NotificationEvent;
import com.hms.hms.modules.room.dto.*;
import com.hms.hms.modules.room.entity.*;
import com.hms.hms.modules.room.repository.*;
import com.hms.hms.modules.room.service.BedService;
import com.hms.hms.modules.user.entity.User;
import com.hms.hms.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BedServiceImpl implements BedService {

    private final BedRepository bedRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    @Override
    public List<Bed> getAvailableBedsByUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        if (user.getBranch() == null) {
            throw new CustomException("User not assigned to any branch", HttpStatus.BAD_REQUEST);
        }

        UUID branchId = user.getBranch().getId();

        List<UUID> roomIds = roomRepository.findByBranchId(branchId)
                .stream().map(Room::getId).toList();

        return bedRepository.findByStatusAndRoomIdIn(BedStatus.AVAILABLE, roomIds);
    }
    @Override
    public void assignBed(
            AssignBedRequest request,
            String email
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new CustomException(
                                "User not found",
                                HttpStatus.NOT_FOUND
                        )
                );

        if (user.getBranch() == null) {

            throw new CustomException(
                    "User not assigned to branch",
                    HttpStatus.BAD_REQUEST
            );
        }

        UUID userBranchId = user.getBranch().getId();

        User tenant = userRepository.findById(request.getTenantId())
                .orElseThrow(() ->
                        new CustomException(
                                "Tenant not found",
                                HttpStatus.NOT_FOUND
                        )
                );

        if (tenant.getBranch() == null) {

            throw new CustomException(
                    "Tenant not assigned to branch",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (!tenant.getBranch().getId().equals(userBranchId)) {

            throw new CustomException(
                    "Tenant belongs to another branch",
                    HttpStatus.FORBIDDEN
            );
        }
        Bed bed = bedRepository.findById(request.getBedId())
                .orElseThrow(() ->
                        new CustomException(
                                "Bed not found",
                                HttpStatus.NOT_FOUND
                        )
                );

        Room room = roomRepository.findById(bed.getRoomId())
                .orElseThrow(() ->
                        new CustomException(
                                "Room not found",
                                HttpStatus.NOT_FOUND
                        )
                );

        if (!room.getBranchId().equals(userBranchId)) {

            throw new CustomException(
                    "Cannot assign bed from another branch",
                    HttpStatus.FORBIDDEN
            );
        }

        if (bed.getStatus() != BedStatus.AVAILABLE) {

            throw new CustomException(
                    "Bed not available",
                    HttpStatus.BAD_REQUEST
            );
        }

        bed.setStatus(BedStatus.OCCUPIED);

        bed.setTenantId(request.getTenantId());

        bedRepository.save(bed);

        eventPublisher.publishEvent(
                new NotificationEvent(
                        tenant.getId(),
                        "Bed Assigned",
                        "A bed has been assigned to you successfully"
                )
        );
    }

    @Override
    public void updateBedStatus(UUID bedId, String status) {

        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() -> new CustomException("Bed not found",HttpStatus.NOT_FOUND));

        bed.setStatus(BedStatus.valueOf(status));
        bedRepository.save(bed);
        if (bed.getTenantId() != null) {

            eventPublisher.publishEvent(
                    new NotificationEvent(
                            bed.getTenantId(),
                            "Bed Status Updated",
                            "Your bed status updated to " + status
                    )
            );
        }
    }

    @Override
    public Map<String, Object> getBedStatsByUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        if (user.getBranch() == null) {
            throw new CustomException("User not assigned to any branch", HttpStatus.BAD_REQUEST);
        }

        UUID branchId = user.getBranch().getId();

        List<UUID> roomIds = roomRepository.findByBranchId(branchId)
                .stream().map(Room::getId).toList();

        Map<String, Object> stats = new HashMap<>();

        stats.put("total", bedRepository.countByRoomIdIn(roomIds));
        stats.put("occupied", bedRepository.countByStatusAndRoomIdIn(BedStatus.OCCUPIED, roomIds));
        stats.put("available", bedRepository.countByStatusAndRoomIdIn(BedStatus.AVAILABLE, roomIds));
        stats.put("maintenance", bedRepository.countByStatusAndRoomIdIn(BedStatus.MAINTENANCE, roomIds));
        stats.put("reserved", bedRepository.countByStatusAndRoomIdIn(BedStatus.RESERVED, roomIds));

        return stats;
    }
}