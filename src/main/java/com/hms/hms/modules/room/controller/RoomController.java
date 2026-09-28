package com.hms.hms.modules.room.controller;

import com.hms.hms.modules.room.dto.CreateRoomRequest;
import com.hms.hms.modules.room.dto.RoomResponseDTO;
import com.hms.hms.modules.room.service.RoomService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public List<RoomResponseDTO> getRooms(HttpServletRequest request) {

        String email = (String) request.getAttribute("userEmail");

        return roomService.getRoomsByUser(email);
    }
    // CREATE ROOM
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ResponseEntity<?> createRoom(
           @Valid @ModelAttribute CreateRoomRequest request,
            HttpServletRequest httpRequest) {

        String email = (String) httpRequest.getAttribute("userEmail");

        roomService.createRoom(request, email); // USED HERE

        return ResponseEntity.ok("Room created successfully");
    }
}


