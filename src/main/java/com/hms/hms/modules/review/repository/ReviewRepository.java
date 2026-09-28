package com.hms.hms.modules.review.repository;

import com.hms.hms.modules.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface ReviewRepository
        extends JpaRepository<Review, UUID> {

    List<Review> findByBranchIdOrderByCreatedAtDesc(
            UUID branchId
    );

    Optional<Review> findByBranchIdAndTenantId(
            UUID branchId,
            UUID tenantId
    );

    Long countByBranchId(UUID branchId);

    @Query("""
            SELECT AVG(r.foodRating)
            FROM Review r
            WHERE r.branchId = :branchId
            """)
    Double getAverageFoodRating(UUID branchId);

    @Query("""
            SELECT AVG(r.cleaningRating)
            FROM Review r
            WHERE r.branchId = :branchId
            """)
    Double getAverageCleaningRating(UUID branchId);

    @Query("""
            SELECT AVG(r.securityRating)
            FROM Review r
            WHERE r.branchId = :branchId
            """)
    Double getAverageSecurityRating(UUID branchId);

    @Query("""
            SELECT AVG(r.wifiRating)
            FROM Review r
            WHERE r.branchId = :branchId
            """)
    Double getAverageWifiRating(UUID branchId);

    @Query("""
            SELECT AVG(r.powerRating)
            FROM Review r
            WHERE r.branchId = :branchId
            """)
    Double getAveragePowerRating(UUID branchId);

    @Query("""
            SELECT AVG(r.overallRating)
            FROM Review r
            WHERE r.branchId = :branchId
            """)
    Double getAverageOverallRating(UUID branchId);
}