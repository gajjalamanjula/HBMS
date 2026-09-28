package com.hms.hms.modules.complaint.dto;

import com.hms.hms.common.enums.ComplaintStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ComplaintResponseDTO {

    private UUID id;
    private String category;
    private String priority;
    private String description;

    private ComplaintStatus status;

    private UUID branchId;

    private UUID raisedBy;
    private UUID assignedTo;

    private String resolutionNote;
}