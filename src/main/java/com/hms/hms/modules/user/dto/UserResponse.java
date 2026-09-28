package com.hms.hms.modules.user.dto;

import com.hms.hms.common.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserResponse {

    private UUID id;
    private String email;
    private String name;
    private String phone;
    private Role role;

    private UUID branchId;
    private String branchName;

    private Boolean active;
    private Boolean verified;
    private Boolean kycVerified;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
