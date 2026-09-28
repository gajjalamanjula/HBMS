package com.hms.hms.modules.admin.service;

import com.hms.hms.modules.room.dto.CreateRoomRequest;
import com.hms.hms.modules.user.dto.CreateUserRequest;

import java.util.*;

public interface ManagerService {

    // EXISTING
    String createStaff(
            CreateUserRequest request,
            String email
    );
    String createUser(
            CreateUserRequest request,
            String email
    );

    // DASHBOARD
    Map<String, Object> getDashboard();

    // STAFF
    List<?> getStaff();
    String updateStaff(UUID id, CreateUserRequest request);
    String deleteStaff(UUID id);

    // TENANTS
    List<?> getTenants();
    String checkoutTenant(UUID tenantId);

    // COUNTS
    Map<String, Object> getCounts();

    // COMPLAINTS
    List<?> getComplaints();
    String assignComplaint(UUID complaintId, UUID staffId);

    // ROOMS
    List<?> getRooms();
    String createRoom(CreateRoomRequest request);

    // BEDS
    String updateBedStatus(UUID bedId, String status);
}