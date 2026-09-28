package com.hms.hms.modules.foodmenu.repository;

import com.hms.hms.modules.branch.entity.Branch;
import com.hms.hms.modules.foodmenu.entity.FoodMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FoodMenuRepository extends JpaRepository<FoodMenu, UUID> {

    Optional<FoodMenu> findTopByBranchIdOrderByWeekStartDateDesc(UUID branchId);

    List<FoodMenu> findTop8ByBranchIdOrderByWeekStartDateDesc(UUID branchId);

    boolean existsByBranchIdAndWeekStartDate(UUID branchId, LocalDate weekStartDate);

    @Modifying
    @Query("UPDATE FoodMenu f SET f.isActive = false WHERE f.branchId = :branchId AND f.isActive = true")
    void deactivateOldMenus(UUID branchId);



}