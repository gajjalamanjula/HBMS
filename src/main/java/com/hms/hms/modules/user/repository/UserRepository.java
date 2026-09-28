package com.hms.hms.modules.user.repository;

import com.hms.hms.common.enums.Role;
import com.hms.hms.modules.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.*;

public interface UserRepository extends JpaRepository<User,UUID> {

    Optional<User> findByEmail(String email);

    List<User> findByBranch_Id(UUID branchId);;
    Optional<User> findByRefreshToken(String refreshToken);

    long countByBranch_IdAndRole(UUID branchId, Role role);

    long countByRole(Role role);

List<User> findByRole(Role role);

    long countByBranch_IdInAndRole(List<UUID> branchIds, Role role);

    List<User> findByBranch_IdInAndRole(List<UUID> branchIds, Role role);

    boolean existsByEmail(String email);

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByActive(boolean active, Pageable pageable);

    Page<User> findByRoleAndActive(Role role, boolean active, Pageable pageable);

    @Query("""
SELECT u FROM User u
WHERE (:role IS NULL OR u.role = :role)
  AND (:active IS NULL OR u.active = :active)
  AND (
        COALESCE(:search, '') = '' OR
        LOWER(u.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
        LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
      )
""")
    Page<User> searchUsers(
            @Param("role") Role role,
            @Param("active") Boolean active,
            @Param("search") String search,
            Pageable pageable
    );

    List<User> findByBranch_IdAndRole(UUID branchId, Role role);


}