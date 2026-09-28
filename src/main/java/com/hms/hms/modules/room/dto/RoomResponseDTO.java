package com.hms.hms.modules.room.dto;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomResponseDTO {

    private UUID id;
    private String roomNumber;
    private String roomType;
    private int totalBeds;
    private String name;
    private String imageUrl;
}