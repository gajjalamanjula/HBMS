package com.hms.hms.modules.admin.controller;

import com.hms.hms.common.enums.FileType;
import com.hms.hms.common.enums.Role;
import com.hms.hms.modules.admin.service.AdminService;
import com.hms.hms.modules.audit.entity.AuditLog;
import com.hms.hms.modules.audit.service.AuditService;
import com.hms.hms.common.service.FileService;
import com.hms.hms.modules.auth.service.AuthService;
import com.hms.hms.modules.branch.entity.Branch;
import com.hms.hms.modules.user.dto.CreateUserRequest;
import com.hms.hms.modules.user.dto.UserResponse;
import com.hms.hms.modules.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/admin") 
@RequiredArgsConstructor
public class AdminController {
    private final AuthService authService;
    private final AdminService adminService;
    private final AuditService auditService;
    private final FileService fileService;

    // CREATE OWNER
    @PostMapping(value = "/create-owner", consumes = "multipart/form-data")
    public String createOwner(@Valid @ModelAttribute CreateUserRequest request) {

        String aadhaarPath = fileService.uploadFile(
                request.getAadhaarPhoto(),
                FileType.KYC_IMAGE
        );

        authService.createUserByRole(
                request.getEmail(),
                Role.OWNER,
                request.getName(),
                request.getPhone(),
                request.getAadhaarNumber(),
                aadhaarPath,
                request.getBranchId()
        );

        return "OTP sent successfully";
    }

    //  VERIFY OTP
    @PostMapping("/verify-otp")
    public String verifyOwnerOtp(
            @RequestParam String email,
            @RequestParam String otp
    ) {
        return authService.verifyOtp(email, otp);
    }



    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return adminService.getDashboard();
    }

    @GetMapping("/owners")
    public List<UserResponse> getOwners() {
        return adminService.getOwners();
    }

    @PutMapping("/owners/{id}")
    public String updateOwner(@PathVariable UUID id,
                             @RequestBody CreateUserRequest request) {
        return adminService.updateOwner(id, request);
    }

    @GetMapping("/users")
    public Page<UserResponse> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        return adminService.getAllUsers(page, size, role, active, search, sortBy, direction);
    }

    @PutMapping("/users/{id}/status")
    public String changeStatus(@PathVariable UUID id,
                              @RequestParam boolean active) {
        return adminService.changeUserStatus(id, active);
    }

    @GetMapping("/branches")
    public List<Branch> branches() {
        return adminService.getAllBranches();
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return adminService.getHealth();
    }

    @GetMapping("/audit-logs")
    public Page<AuditLog> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String action
    ) {
        return auditService.getLogs(page, size, action);
    }
}