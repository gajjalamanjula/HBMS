package com.hms.hms.modules.complaint.entity;

import com.hms.hms.common.enums.ComplaintStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "complaints")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Complaint {

    @Id
    @GeneratedValue

    private UUID id;

    private UUID raisedBy;
    private UUID assignedTo;
    private UUID branchId;

    private String category;
    private String priority;

    @Column(length = 1000)
    private String description;
    @Enumerated(EnumType.STRING)
    private ComplaintStatus status; // OPEN, IN_PROGRESS, RESOLVED, CLOSED

    private Boolean escalated;

    private String resolutionNote;


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
}