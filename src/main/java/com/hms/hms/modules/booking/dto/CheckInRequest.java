package com.hms.hms.modules.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CheckInRequest {

    @NotNull(message = "Booking id is required")
    private UUID bookingId;
}