package com.hms.hms.modules.admin.controller;

import com.hms.hms.common.response.ApiResponse;
import com.hms.hms.modules.admin.service.ManagerService;
import com.hms.hms.modules.auth.service.AuthService;
import com.hms.hms.modules.room.dto.CreateRoomRequest;
import com.hms.hms.modules.user.dto.CreateUserRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/auth/manager")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerController {

    private final ManagerService managerService;
    private final AuthService authService;

    //  CREATE STAFF
    @PostMapping(value = "/create-staff", consumes = "multipart/form-data")
    public ApiResponse<?> createStaff(
            @Valid @ModelAttribute CreateUserRequest request,
            HttpServletRequest httpRequest
    ) {

        String email =
                (String) httpRequest.getAttribute("userEmail");

        String response =
                managerService.createStaff(request, email);
        return ApiResponse.builder()
                .success(true)
                .message(response)
                .data(null)
                .build();
    }

    // CREATE TENANT
    @PostMapping(value = "/create-tenant", consumes = "multipart/form-data")
    public ApiResponse<?> createTenant(
            @Valid @ModelAttribute CreateUserRequest request,
            HttpServletRequest httpRequest
    ) {

        String email =
                (String) httpRequest.getAttribute("userEmail");

        String response =
                managerService.createUser(request, email);

        return ApiResponse.builder()
                .success(true)
                .message(response)
                .data(null)
                .build();
    }

    // VERIFY OTP
    @PostMapping("/verify-otp")
    public ApiResponse<?> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp
    ) {
        return ApiResponse.builder()
                .success(true)
                .message("OTP verified successfully")
                .data(authService.verifyOtp(email, otp))
                .build();
    }

    // DASHBOARD
    @GetMapping("/dashboard")
    public ApiResponse<?> dashboard() {
        return ApiResponse.builder()
                .success(true)
                .message("Dashboard fetched successfully")
                .data(managerService.getDashboard())
                .build();
    }

    //  STAFF LIST
    @GetMapping("/staff")
    public ApiResponse<?> getStaff() {
        return ApiResponse.builder()
                .success(true)
                .message("Staff fetched successfully")
                .data(managerService.getStaff())
                .build();
    }

    // UPDATE STAFF
    @PutMapping("/staff/{id}")
    public ApiResponse<?> updateStaff(@PathVariable UUID id,
                                      @RequestBody CreateUserRequest request) {

        managerService.updateStaff(id, request);

        return ApiResponse.builder()
                .success(true)
                .message("Staff updated successfully")
                .data(null)
                .build();
    }

    //  DELETE STAFF
    @DeleteMapping("/staff/{id}")
    public ApiResponse<?> deleteStaff(@PathVariable UUID id) {

        managerService.deleteStaff(id);

        return ApiResponse.builder()
                .success(true)
                .message("Staff deleted successfully")
                .data(null)
                .build();
    }

    // TENANTS LIST
    @GetMapping("/tenants")
    public ApiResponse<?> getTenants() {
        return ApiResponse.builder()
                .success(true)
                .message("Tenants fetched successfully")
                .data(managerService.getTenants())
                .build();
    }

    //  TENANT CHECKOUT
    @PostMapping("/tenants/{id}/checkout")
    public ApiResponse<?> checkout(@PathVariable UUID id) {

        managerService.checkoutTenant(id);

        return ApiResponse.builder()
                .success(true)
                .message("Tenant checked out successfully")
                .data(null)
                .build();
    }

    //  COUNTS
    @GetMapping("/counts")
    public ApiResponse<?> counts() {
        return ApiResponse.builder()
                .success(true)
                .message("Counts fetched successfully")
                .data(managerService.getCounts())
                .build();
    }

    // COMPLAINTS
    @GetMapping("/complaints")
    public ApiResponse<?> complaints() {
        return ApiResponse.builder()
                .success(true)
                .message("Complaints fetched successfully")
                .data(managerService.getComplaints())
                .build();
    }

    //  ASSIGN COMPLAINT
    @PutMapping("/complaints/{id}/assign")
    public ApiResponse<?> assignComplaint(@PathVariable UUID id,
                                          @RequestParam UUID staffId) {

        managerService.assignComplaint(id, staffId);

        return ApiResponse.builder()
                .success(true)
                .message("Complaint assigned successfully")
                .data(null)
                .build();
    }

    //  ROOMS
    @GetMapping("/rooms")
    public ApiResponse<?> rooms() {
        return ApiResponse.builder()
                .success(true)
                .message("Rooms fetched successfully")
                .data(managerService.getRooms())
                .build();
    }

    //  CREATE ROOM
    @PostMapping("/rooms")
    public ApiResponse<?> createRoom(@RequestBody CreateRoomRequest request) {

        managerService.createRoom(request);

        return ApiResponse.builder()
                .success(true)
                .message("Room created successfully")
                .data(null)
                .build();
    }

    //  UPDATE BED STATUS
    @PatchMapping("/beds/{id}/status")
    public ApiResponse<?> updateBed(@PathVariable UUID id,
                                    @RequestParam String status) {

        managerService.updateBedStatus(id, status);

        return ApiResponse.builder()
                .success(true)
                .message("Bed status updated successfully")
                .data(null)
                .build();
    }
}