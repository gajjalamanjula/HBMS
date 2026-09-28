package com.hms.hms.modules.user.service.serviceimpl;

import com.hms.hms.common.enums.Role;
import com.hms.hms.common.exception.CustomException;
import com.hms.hms.modules.branch.entity.Branch;
import com.hms.hms.modules.branch.repository.BranchRepository;
import com.hms.hms.modules.notification.event.NotificationEvent;
import com.hms.hms.modules.user.dto.*;
import com.hms.hms.modules.user.entity.User;
import com.hms.hms.modules.user.repository.UserRepository;
import com.hms.hms.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    @Override
    public UserResponse createUser(CreateUserRequest request, String creatorEmail) {

        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        //  Duplicate check
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException("User already exists", HttpStatus.CONFLICT);
        }

        Role creatorRole = creator.getRole();
        Role newUserRole = request.getRole();

        // CENTRAL VALIDATION (NEW)
        validateUserCreation(creatorRole, newUserRole);

        Branch branch = resolveBranch(creator, request, creatorRole, newUserRole);

        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

        //  CREATE USER
        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .phone(request.getPhone())
                .aadhaarNumber(request.getAadhaarNumber())
                .password(passwordEncoder.encode(tempPassword))
                .role(newUserRole)
                .branch(branch)
                .photoUrl(request.getPhotoUrl())
                .active(true)
                .mustChangePassword(true)
                .build();

        userRepository.save(user);
        eventPublisher.publishEvent(
                new NotificationEvent(
                        user.getId(),
                        "Account Created",
                        "Your account has been created successfully"
                )
        );

        System.out.println("Generated Password: " + tempPassword);

        return mapToResponse(user);
    }

    //  ROLE VALIDATION (CLEAN + REUSABLE)
    private void validateUserCreation(Role creatorRole, Role newUserRole) {

        switch (creatorRole) {

            case SUPER_ADMIN:
                if (newUserRole != Role.OWNER) {
                    throw new CustomException("SUPER_ADMIN can only create OWNER", HttpStatus.FORBIDDEN);
                }
                break;

            case OWNER:
                if (!(newUserRole == Role.MANAGER ||
                        newUserRole == Role.STAFF ||
                        newUserRole == Role.TENANT)) {

                    throw new CustomException("OWNER can only create MANAGER, STAFF, TENANT", HttpStatus.FORBIDDEN);
                }
                break;

            case MANAGER:
                if (!(newUserRole == Role.STAFF || newUserRole == Role.TENANT)) {
                    throw new CustomException("MANAGER can only create STAFF or TENANT", HttpStatus.FORBIDDEN);
                }
                break;

            default:
                throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
        }
    }

    //  BRANCH ASSIGNMENT LOGIC
    private Branch resolveBranch(User creator, CreateUserRequest request,
                                 Role creatorRole, Role newUserRole) {

        Branch branch = null;

        //  MANAGER ---- always same branch
        if (creatorRole == Role.MANAGER) {

            if (creator.getBranch() == null) {
                throw new CustomException("Manager not assigned to any branch", HttpStatus.BAD_REQUEST);
            }

            return creator.getBranch();
        }

        //  OWNER
        if (creatorRole == Role.OWNER) {

            // MANAGER → must assign branch
            if (newUserRole == Role.MANAGER) {

                if (request.getBranchId() == null) {
                    throw new CustomException("Owner must assign branch to manager", HttpStatus.BAD_REQUEST);
                }

                return getBranchById(request.getBranchId());
            }

            // STAFF → optional branch
            if (newUserRole == Role.STAFF && request.getBranchId() != null) {
                return getBranchById(request.getBranchId());
            }

            // TENANT → auto assign later (or no manual assignment)
            if (newUserRole == Role.TENANT && request.getBranchId() != null) {
                throw new CustomException("Do not assign branch to tenant manually", HttpStatus.BAD_REQUEST);
            }
        }

        //  SUPER_ADMIN → no branch
        return null;
    }

    private Branch getBranchById(UUID branchId) {
    return branchRepository.findById(branchId)
            .orElseThrow(() -> new CustomException("Branch not found", HttpStatus.NOT_FOUND));
}

    @Override
    public List<UserResponse> getUsersByBranch(UUID branchId){

        return userRepository.findByBranch_Id(branchId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public long countStaff(UUID branchId) {
        return userRepository.countByBranch_IdAndRole(branchId, Role.STAFF);
    }

    @Override
    public long countTenants(UUID branchId) {
        return userRepository.countByBranch_IdAndRole(branchId, Role.TENANT);
    }

    //  COMMON MAPPER
    private UserResponse mapToResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                .build();
    }
}