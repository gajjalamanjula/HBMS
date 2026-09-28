package com.hms.hms.modules.complaint.dto;

import com.hms.hms.common.enums.ComplaintStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    private ComplaintStatus status;

    @Size(max = 500, message = "Resolution note too long")
    private String resolutionNote;
}