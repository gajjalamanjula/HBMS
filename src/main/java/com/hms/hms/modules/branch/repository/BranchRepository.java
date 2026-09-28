package com.hms.hms.modules.branch.repository;

import com.hms.hms.common.enums.HostelType;
import com.hms.hms.modules.branch.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {

    List<Branch> findByOwnerId(UUID ownerId);
    boolean existsByHostelNameAndOwnerId(
            String hostelName,
            UUID ownerId
    );
    List<Branch> findByHostelType(HostelType hostelType);

    List<Branch> findByHostelTypeAndAreaContainingIgnoreCase(
            HostelType hostelType,
            String area
    );
}