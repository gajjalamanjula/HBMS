package com.hms.hms.modules.payment.repository;

import com.hms.hms.modules.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByTenantId(UUID tenantId);

    List<Payment> findByBranchId(UUID branchId);
}