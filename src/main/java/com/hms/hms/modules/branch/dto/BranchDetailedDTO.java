package com.hms.hms.modules.branch.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BranchDetailedDTO {

    private UUID branchId;
    private String name;
    private String location;
    private long totalBeds;
    private long occupiedBeds;
    private long availableBeds;
    private long maintenanceBeds;
    private long totalTenants;
    private long totalStaff;

    private double occupancyPercentage;
}