package com.hms.hms.modules.publicuser.dto;



import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequest {

    @NotNull(message = "Branch id is required")
    private UUID branchId;

    @NotNull(message = "Bed id is required")
    private UUID bedId;
}
