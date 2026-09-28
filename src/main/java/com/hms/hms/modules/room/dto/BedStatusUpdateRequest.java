package com.hms.hms.modules.room.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BedStatusUpdateRequest {

    @NotBlank(message = "Status is required")
    private String status;
}