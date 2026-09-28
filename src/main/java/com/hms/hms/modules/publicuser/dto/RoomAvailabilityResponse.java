package com.hms.hms.modules.publicuser.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RoomAvailabilityResponse {
    private UUID roomId;

    private String roomNumber;

    private String roomType;

    private Integer totalBeds;

    private Integer availableBeds;

    private Boolean fullyOccupied;
}