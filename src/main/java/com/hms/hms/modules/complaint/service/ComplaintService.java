package com.hms.hms.modules.complaint.service;

import com.hms.hms.modules.complaint.dto.CreateComplaintRequest;
import com.hms.hms.modules.complaint.dto.UpdateStatusRequest;
import com.hms.hms.modules.complaint.entity.Complaint;

import java.util.List;
import java.util.UUID;

public interface ComplaintService {

    Complaint raiseComplaint(CreateComplaintRequest request, String email);

    List<Complaint> getComplaints(String email);

    Complaint getComplaintById(UUID id, String email);

    Complaint assignComplaint(UUID id, UUID staffId, String email);

    Complaint updateStatus(UUID id, UpdateStatusRequest request, String email);

    Complaint escalateComplaint(UUID id, String email);

    String confirmClose(UUID id, String email);
}