package com.hms.hms.modules.user.service;

import com.hms.hms.modules.user.dto.*;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse createUser(CreateUserRequest request, String managerEmail);

    List<UserResponse> getUsersByBranch(UUID branchId);

    long countStaff(UUID branchId);

    long countTenants(UUID branchId);
}