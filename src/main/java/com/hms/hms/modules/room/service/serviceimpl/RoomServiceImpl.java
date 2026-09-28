package com.hms.hms.modules.room.service.serviceimpl;

import com.hms.hms.common.enums.BedStatus;
import com.hms.hms.common.enums.FileType;
import com.hms.hms.common.enums.Role;
import com.hms.hms.common.exception.CustomException;
import com.hms.hms.common.service.FileService;
import com.hms.hms.modules.notification.event.NotificationEvent;
import com.hms.hms.modules.room.dto.*;
import com.hms.hms.modules.room.entity.*;
import com.hms.hms.modules.room.repository.*;
import com.hms.hms.modules.room.service.RoomService;
import com.hms.hms.modules.user.entity.User;
import com.hms.hms.modules.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final BedRepository bedRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final ApplicationEventPublisher eventPublisher;
    // CREATE ROOM
    @Override
    @Transactional
    public void createRoom(CreateRoomRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        if (!(user.getRole() == Role.MANAGER || user.getRole() == Role.OWNER)) {
            throw new CustomException("Only OWNER or MANAGER allowed", HttpStatus.FORBIDDEN);
        }

        if (user.getBranch() == null) {
            throw new CustomException("User not assigned to any branch", HttpStatus.BAD_REQUEST);
        }

        UUID branchId = user.getBranch().getId();

        String roomNumber = request.getRoomNumber().trim();

        if (roomRepository.existsByRoomNumberAndBranchId(roomNumber, branchId)) {
            throw new CustomException("Room already exists", HttpStatus.BAD_REQUEST);
        }

        //  IMAGE HANDLING (ONLY THIS PART MATTERS)
        String imagePath = null;

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            imagePath = fileService.uploadFile(request.getImage(), FileType.ROOM_PHOTO);
        }

        //  SAVE ROOM
        Room room = Room.builder()
                .roomNumber(roomNumber)
                .floor(request.getFloor())
                .roomType(request.getRoomType())
                .totalBeds(request.getTotalBeds())
                .imageUrl(imagePath) //  store here
                .branchId(branchId)
                .build();

        Room savedRoom = roomRepository.save(room);

        // CREATE BEDS
        for (int i = 0; i < request.getTotalBeds(); i++) {
            bedRepository.save(
                    Bed.builder()
                            .roomId(savedRoom.getId())
                            .bedNumber(
                                    savedRoom.getRoomNumber()
                                            + "-B" + (i + 1)
                            )
                            .status(BedStatus.AVAILABLE)
                            .build()
            );
        }
        eventPublisher.publishEvent(
                new NotificationEvent(
                        user.getId(),
                        "Room Created",
                        "Room " + savedRoom.getRoomNumber() + " created successfully"
                )
        );
    }

    public List<RoomResponseDTO> getRoomsByUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        "User not found",
                        HttpStatus.NOT_FOUND
                ));

        if (user.getBranch() == null) {
            throw new CustomException(
                    "User not assigned to any branch",
                    HttpStatus.BAD_REQUEST
            );
        }

        UUID branchId = user.getBranch().getId(); //

        return roomRepository.findByBranchId(branchId)
                .stream()
                .map(room -> RoomResponseDTO.builder()
                        .id(room.getId())
                        .roomNumber(room.getRoomNumber())
                        .roomType(room.getRoomType())
                        .totalBeds(room.getTotalBeds())
                        .name("Room " + room.getRoomNumber())
                        .imageUrl(room.getImageUrl())
                        .build()
                ).toList();
    }
}