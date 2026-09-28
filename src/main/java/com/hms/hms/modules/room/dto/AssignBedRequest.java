package com.hms.hms.modules.room.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignBedRequest {

    @NotNull(message = "Bed id is required")
    private UUID bedId;

    @NotBlank(message = "Bed number is required")
    private String bedNumber;

    @NotNull(message = "Tenant id is required")
    private UUID tenantId;
}