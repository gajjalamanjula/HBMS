package com.hms.hms.modules.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TenantResponseDTO {

    private UUID id;
    private String name;
    private String email;
    private String phone;

    private UUID branchId;
    private String branchName;

    private UUID bedId;
    private String roomNumber;

    private String bedNumber;

    private String hostelName;

    private String branchLocation;
}