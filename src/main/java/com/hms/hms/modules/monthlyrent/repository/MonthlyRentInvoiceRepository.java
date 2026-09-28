package com.hms.hms.modules.monthlyrent.repository;

import com.hms.hms.common.enums.MonthlyRentStatus;
import com.hms.hms.modules.monthlyrent.entity.MonthlyRentInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MonthlyRentInvoiceRepository
        extends JpaRepository<MonthlyRentInvoice, UUID> {

    Optional<MonthlyRentInvoice> findByBookingIdAndRentMonthAndRentYear(
            UUID bookingId,
            Integer rentMonth,
            Integer rentYear
    );

    List<MonthlyRentInvoice> findByTenantId(UUID tenantId);

    List<MonthlyRentInvoice> findByBranchId(UUID branchId);

    List<MonthlyRentInvoice> findByBranchIdAndStatus(
            UUID branchId,
            MonthlyRentStatus status
    );

    @Query("""
        SELECT m.rentMonth, m.rentYear, SUM(m.paidAmount)
        FROM MonthlyRentInvoice m
        WHERE m.branchId IN :branchIds
        GROUP BY m.rentMonth, m.rentYear
        ORDER BY m.rentYear, m.rentMonth
        """)
    List<Object[]> getRevenueByMonthAndYear(
            @Param("branchIds") List<UUID> branchIds
    );

    List<MonthlyRentInvoice> findByStatusAndDueDate(
            MonthlyRentStatus status,
            LocalDate dueDate
    );

    List<MonthlyRentInvoice> findByStatusAndDueDateBefore(
            MonthlyRentStatus status,
            LocalDate dueDate
    );
}