package com.hms.hms.modules.user.entity;

import com.hms.hms.common.enums.Role;
import com.hms.hms.modules.branch.entity.Branch;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Name is required")
    private String name;

    private String aadhaarNumber;
    private String phone;

    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;

    //  Branch Mapping
    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;

    //  Who created this user
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    //  Auth / OTP
    private String otp;
    private LocalDateTime otpExpiry;
    private Integer otpAttempts = 0;


    //  Documents
    @Column(name = "aadhaar_url", columnDefinition = "TEXT")
    private String aadhaarUrl;
    @Column(columnDefinition = "TEXT")
    private String photoUrl;

    @Column(columnDefinition = "TEXT")
    private String refreshToken;

    //  Status Flags
    @Builder.Default
    private Boolean active = false;

    @Builder.Default
    private Boolean verified = false;

    @Builder.Default
    private Boolean kycVerified = false;

    @Builder.Default
    private Boolean mustChangePassword = false;

    //  Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}