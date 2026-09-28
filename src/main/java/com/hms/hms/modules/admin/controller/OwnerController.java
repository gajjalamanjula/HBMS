package com.hms.hms.modules.admin.controller;

import com.hms.hms.common.enums.Role;
import com.hms.hms.modules.admin.service.OwnerService;

import com.hms.hms.modules.auth.service.AuthService;
import com.hms.hms.modules.complaint.dto.ComplaintResponseDTO;
import com.hms.hms.modules.user.dto.CreateUserRequest;
import com.hms.hms.modules.user.dto.ManagerResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@PreAuthorize("hasRole('OWNER')")
@RestController
@RequestMapping("/api/v1/auth/owner")
@RequiredArgsConstructor
public class OwnerController {



    private final OwnerService ownerService;
    private final AuthService authService;
    @PostMapping(value = "/create-manager", consumes = "multipart/form-data")
    public String createManager(@ModelAttribute CreateUserRequest request) {
        return ownerService.createManager(request);
    }

    @PostMapping(value = "/create-staff", consumes = "multipart/form-data")
    public String createStaff(@ModelAttribute CreateUserRequest request) {
        return ownerService.createStaff(request);
    }

    @PostMapping(value = "/create-tenant", consumes = "multipart/form-data")
    public String createTenant(@ModelAttribute CreateUserRequest request) {
        return ownerService.createUser(request);
    }
    @PostMapping("/verify-otp")
    public String verifyOtp(
            @RequestParam String email,
            @RequestParam String otp
    ) {
        return authService.verifyOtp(email, otp);
    }

    // DASHBOARD
    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
        return ownerService.getDashboard(email);
    }

    //  BRANCHES
    @GetMapping("/branches")
    public List<Map<String, Object>> branches(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
        return ownerService.getBranches(email);
    }

    // BRANCH STATS
    @GetMapping("/branches/{id}/stats")
    public Map<String, Object> branchStats(@PathVariable UUID id) {
        return ownerService.getBranchStats(id);
    }

    //  REVENUE
    @GetMapping("/revenue")
    public List<Map<String, Object>> revenue(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
        return ownerService.getRevenue(email);
    }

    //  BED SUMMARY
    @GetMapping("/beds/summary")
    public Map<String, Object> bedSummary(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
        return ownerService.getBedSummary(email);
    }

    //  COMPLAINTS
    @GetMapping("/complaints")
    public List<ComplaintResponseDTO> complaints(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
        return ownerService.getComplaints(email);
    }

    //  ESCALATE
    @PutMapping("/complaints/{id}/escalate")
    public String escalate(@PathVariable UUID id, HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
        return ownerService.escalateComplaint(id, email);
    }

    //  MANAGERS
    @GetMapping("/managers")
    public List<ManagerResponseDTO> managers(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
        return ownerService.getManagers(email);
    }

    //STAFF COUNT
    @GetMapping("/staff/count")
    public Map<String, Object> staffCount(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
        return ownerService.getStaffCount(email);
    }

    //TENANT COUNT
    @GetMapping("/tenants/count")
    public Map<String, Object> tenantCount(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
        return ownerService.getTenantCount(email);
    }
}
