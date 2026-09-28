package com.hms.hms.modules.branch.service;

import com.hms.hms.modules.branch.dto.*;

import java.util.List;
import java.util.UUID;

public interface BranchService {

    BranchResponse createBranch(CreateBranchRequest request, String ownerEmail);

    List<BranchResponse> getBranches();

    BranchResponse updateBranch(UUID branchId, CreateBranchRequest request);
}