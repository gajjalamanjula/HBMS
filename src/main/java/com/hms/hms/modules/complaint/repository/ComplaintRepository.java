package com.hms.hms.modules.complaint.repository;

import com.hms.hms.modules.complaint.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {

    List<Complaint> findByBranchId(UUID branchId);

    List<Complaint> findByRaisedBy(UUID userId);

    List<Complaint> findByBranchIdIn(List<UUID> branchIds);

    Long countByBranchId(UUID branchId);

}