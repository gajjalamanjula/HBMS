package com.hms.hms.modules.room.service;

import com.hms.hms.modules.room.dto.CreateRoomRequest;
import com.hms.hms.modules.room.dto.RoomResponseDTO;

import java.util.List;

public interface RoomService {

    void createRoom(CreateRoomRequest request, String email);

    List<RoomResponseDTO> getRoomsByUser(String email);
}