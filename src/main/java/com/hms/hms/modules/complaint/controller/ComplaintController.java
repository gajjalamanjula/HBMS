package com.hms.hms.modules.complaint.controller;

import com.hms.hms.modules.complaint.dto.CreateComplaintRequest;
import com.hms.hms.modules.complaint.dto.UpdateStatusRequest;
import com.hms.hms.modules.complaint.entity.Complaint;
import com.hms.hms.modules.complaint.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    public ResponseEntity<?> raise(
            @Valid @RequestBody CreateComplaintRequest request,
            Principal principal) {
        return ResponseEntity.ok(
                complaintService.raiseComplaint(request, principal.getName())
        );
    }

    @GetMapping
    public List<Complaint> get(Principal principal) {
        return complaintService.getComplaints(principal.getName());
    }

    @GetMapping("/{id}")
    public Complaint getById(@PathVariable UUID id, Principal principal) {
        return complaintService.getComplaintById(id, principal.getName());
    }

    @PutMapping("/{id}/assign")
    public Complaint assign(@PathVariable UUID id,
                            @RequestParam UUID staffId,
                            Principal principal) {
        return complaintService.assignComplaint(id, staffId, principal.getName());
    }

    @PutMapping("/{id}/status")
    public Complaint updateStatus( @PathVariable UUID id,
                                 @Valid @RequestBody UpdateStatusRequest request,
                                  Principal principal) {
        return complaintService.updateStatus(id, request, principal.getName());
    }

    @PutMapping("/{id}/escalate")
    public Complaint escalate(@PathVariable UUID id, Principal principal) {
        return complaintService.escalateComplaint(id, principal.getName());
    }

    @PostMapping("/{id}/confirm-close")
    public String confirmClose(@PathVariable UUID id, Principal principal) {
        return complaintService.confirmClose(id, principal.getName());
    }
}