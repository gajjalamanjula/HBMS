package com.hms.hms.modules.complaint.service.serviceimpl;

import com.hms.hms.common.enums.ComplaintStatus;
import com.hms.hms.common.enums.Role;
import com.hms.hms.common.exception.CustomException;
import com.hms.hms.modules.branch.repository.BranchRepository;
import com.hms.hms.modules.complaint.dto.CreateComplaintRequest;
import com.hms.hms.modules.complaint.dto.UpdateStatusRequest;
import com.hms.hms.modules.complaint.entity.Complaint;
import com.hms.hms.modules.complaint.repository.ComplaintRepository;
import com.hms.hms.modules.complaint.service.ComplaintService;
import com.hms.hms.modules.notification.event.NotificationEvent;
import com.hms.hms.modules.user.entity.User;
import com.hms.hms.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.hms.hms.modules.branch.entity.Branch;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final BranchRepository branchRepository;
    @Override
    public Complaint raiseComplaint(CreateComplaintRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        if (!(user.getRole() == Role.MANAGER ||
                user.getRole() == Role.STAFF ||
                user.getRole() == Role.TENANT)) {
            throw new CustomException("Not allowed to raise complaint", HttpStatus.FORBIDDEN);
        }

        Complaint complaint = Complaint.builder()
                .category(request.getCategory())
                .priority(request.getPriority())
                .description(request.getDescription())
                .status(ComplaintStatus.OPEN)
                .raisedBy(user.getId())
                .branchId(user.getBranch().getId())
                .createdAt(LocalDateTime.now())
                .build();

        Complaint savedComplaint = complaintRepository.save(complaint);

        eventPublisher.publishEvent(
                new NotificationEvent(
                        user.getId(),
                        "Complaint Raised",
                        "Your complaint has been raised successfully"
                )
        );

        return savedComplaint;
    }

    @Override
    public List<Complaint> getComplaints(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        if (user.getRole() == Role.OWNER) {
            return complaintRepository.findAll();
        }

        if (user.getRole() == Role.MANAGER || user.getRole() == Role.STAFF) {
            return complaintRepository.findByBranchId(user.getBranch().getId());
        }

        return complaintRepository.findByRaisedBy(user.getId());
    }

    @Override
    public Complaint getComplaintById(UUID id, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new CustomException("Complaint not found", HttpStatus.NOT_FOUND));

        // 🔐 SECURITY CHECK
        if (user.getRole() == Role.TENANT &&
                !complaint.getRaisedBy().equals(user.getId())) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }

        if ((user.getRole() == Role.MANAGER || user.getRole() == Role.STAFF)
                && !complaint.getBranchId().equals(user.getBranch().getId())) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }

        return complaint;
    }

    @Override
    public Complaint assignComplaint(UUID id, UUID staffId, String email) {

        //  GET MANAGER
        User manager = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        if (manager.getRole() != Role.MANAGER) {
            throw new CustomException("Only MANAGER can assign", HttpStatus.FORBIDDEN);
        }

        //  GET COMPLAINT
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new CustomException("Complaint not found", HttpStatus.NOT_FOUND));

        //

        //  GET STAFF
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new CustomException("Staff not found", HttpStatus.NOT_FOUND));

        //  CHECK ROLE
        if (staff.getRole() != Role.STAFF) {
            throw new CustomException("Only STAFF can be assigned", HttpStatus.BAD_REQUEST);
        }



        // CHECK SAME BRANCH
        if (!staff.getBranch().getId().equals(manager.getBranch().getId())) {
            throw new CustomException("Staff must belong to same branch", HttpStatus.BAD_REQUEST);
        }

        //  ASSIGN
        complaint.setAssignedTo(staffId);

        //  OPTIONAL BUT IMPORTANT
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);

        complaint.setUpdatedAt(LocalDateTime.now());

        Complaint savedComplaint = complaintRepository.save(complaint);

        eventPublisher.publishEvent(
                new NotificationEvent(
                        staff.getId(),
                        "Complaint Assigned",
                        "A complaint has been assigned to you"
                )
        );

        return savedComplaint;
    }
    // UPDATE STATUS
    @Override
    public Complaint updateStatus(UUID id, UpdateStatusRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        if (!(user.getRole() == Role.STAFF || user.getRole() == Role.MANAGER)) {
            throw new CustomException("Not allowed", HttpStatus.FORBIDDEN);
        }

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new CustomException("Complaint not found", HttpStatus.NOT_FOUND));


        if (user.getRole() == Role.STAFF &&
                !complaint.getAssignedTo().equals(user.getId())) {
            throw new CustomException("Not assigned to you", HttpStatus.FORBIDDEN);
        }


        ComplaintStatus status = request.getStatus();

        complaint.setStatus(status);

        if (status == ComplaintStatus.RESOLVED) {
            complaint.setResolvedAt(LocalDateTime.now());
        }

        complaint.setResolutionNote(request.getResolutionNote());

        complaint.setUpdatedAt(
                LocalDateTime.now()
        );
        Complaint updatedComplaint = complaintRepository.save(complaint);

        eventPublisher.publishEvent(
                new NotificationEvent(
                        complaint.getRaisedBy(),
                        "Complaint Status Updated",
                        "Your complaint status updated to " + status
                )
        );

        return updatedComplaint;
    }

    // ESCALATE
    // ESCALATE
    @Override
    public Complaint escalateComplaint(UUID id, String email) {

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new CustomException(
                                "User not found",
                                HttpStatus.NOT_FOUND
                        ));

        if (owner.getRole() != Role.OWNER) {
            throw new CustomException(
                    "Only OWNER can escalate",
                    HttpStatus.FORBIDDEN
            );
        }

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() ->
                        new CustomException(
                                "Complaint not found",
                                HttpStatus.NOT_FOUND
                        ));

        Branch branch = branchRepository.findById(
                complaint.getBranchId()
        ).orElseThrow(() ->
                new CustomException(
                        "Branch not found",
                        HttpStatus.NOT_FOUND
                ));

        if (!branch.getOwnerId().equals(owner.getId())) {
            throw new CustomException(
                    "Not your branch complaint",
                    HttpStatus.FORBIDDEN
            );
        }

        complaint.setEscalated(true);
        complaint.setStatus(ComplaintStatus.ESCALATED);
        complaint.setUpdatedAt(LocalDateTime.now());
        Complaint escalatedComplaint =
                complaintRepository.save(complaint);

        eventPublisher.publishEvent(
                new NotificationEvent(
                        owner.getId(),
                        "Complaint Escalated",
                        "Complaint escalated successfully"
                )
        );

        return escalatedComplaint;
    }

    // CONFIRM CLOSE
    @Override
    public String confirmClose(UUID id, String email) {

        User tenant = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new CustomException("Complaint not found", HttpStatus.NOT_FOUND));

        if (!complaint.getRaisedBy().equals(tenant.getId())) {
            throw new CustomException("Not your complaint", HttpStatus.FORBIDDEN);
        }

        if (complaint.getStatus() != ComplaintStatus.RESOLVED) {
            throw new CustomException("Complaint not resolved yet", HttpStatus.BAD_REQUEST);
        }
        
        complaint.setStatus(ComplaintStatus.CLOSED);
        complaint.setUpdatedAt(LocalDateTime.now());
        complaintRepository.save(complaint);
        eventPublisher.publishEvent(
                new NotificationEvent(
                        tenant.getId(),
                        "Complaint Closed",
                        "Complaint closed successfully"
                )
        );
        return "Complaint closed successfully";
    }
}

