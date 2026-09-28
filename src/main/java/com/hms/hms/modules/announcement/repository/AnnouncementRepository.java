package com.hms.hms.modules.announcement.repository;

import com.hms.hms.modules.announcement.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnnouncementRepository
        extends JpaRepository<Announcement, UUID> {

    List<Announcement> findByBranchIdAndActiveTrueOrderByCreatedAtDesc(
            UUID branchId
    );
}