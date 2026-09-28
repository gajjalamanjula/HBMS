package com.hms.hms.modules.admin.service;

import com.hms.hms.modules.complaint.dto.ComplaintResponseDTO;
import com.hms.hms.modules.user.dto.CreateUserRequest;
import com.hms.hms.modules.user.dto.ManagerResponseDTO;
import com.hms.hms.modules.user.dto.StaffResponseDTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface OwnerService {

    String createManager(CreateUserRequest request);

    String createStaff(CreateUserRequest request);

    String createUser(CreateUserRequest request);

    Map<String, Object> getDashboard(String email);

    List<Map<String, Object>> getBranches(String email);

    Map<String, Object> getBranchStats(UUID branchId);

    List<Map<String, Object>> getRevenue(String email);

    Map<String, Object> getBedSummary(String email);

    List<ComplaintResponseDTO> getComplaints(String email);

    String escalateComplaint(UUID id, String email);
    public List<ManagerResponseDTO> getManagers(String email);

    Map<String, Object> getStaffCount(String email);

    Map<String, Object> getTenantCount(String email);
}
