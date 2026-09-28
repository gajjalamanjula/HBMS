package com.hms.hms.modules.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String action;        // CREATE_USER, UPDATE_OWNER, etc.
    private String entityName;    // USER, ROOM, BED
    private UUID entityId;
    private String performedBy;   // email
    private String role;          // ADMIN, OWNER

    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;
}