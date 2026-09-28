package com.hms.hms.modules.auth.dto;

import com.hms.hms.common.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CurrentUserResponse {

    private UUID id;

    private String name;

    private String email;

    private String phone;

    private Role role;

    private UUID branchId;

    private Boolean active;

    private Boolean verified;

    private Boolean kycVerified;
}