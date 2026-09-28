package com.hms.hms.modules.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class StaffResponseDTO {

    private UUID id;
    private String name;
    private String email;
    private String phone;

    private UUID branchId;
    private String branchName;

    private String role;
    private boolean active;
}