package com.hms.hms.modules.admin.service;

import com.hms.hms.modules.branch.entity.Branch;
import com.hms.hms.modules.user.dto.CreateUserRequest;
import com.hms.hms.modules.user.dto.UserResponse;
import com.hms.hms.modules.user.entity.User;
import org.springframework.data.domain.Page;

import com.hms.hms.common.enums.Role;
import java.util.*;

public interface AdminService {

    Map<String, Object> getDashboard();

    public UserResponse createOwner(CreateUserRequest request, String adminEmail);

    List<UserResponse> getOwners();

    String updateOwner(UUID id, CreateUserRequest request);

    Page<UserResponse> getAllUsers(
            int page,
            int size,
            Role role,
            Boolean active,
            String search,
            String sortBy,
            String direction
    );

    String changeUserStatus(UUID id, boolean active);

    List<Branch> getAllBranches();

    Map<String, Object> getHealth();

}