package com.hms.hms.modules.monthlyrent.entity;

import com.hms.hms.common.enums.MonthlyRentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "monthly_rent_invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyRentInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID bookingId;
    private UUID tenantId;
    private UUID branchId;

    private Integer rentMonth;
    private Integer rentYear;

    private Double rentAmount;
    private Double paidAmount;
    private Double dueAmount;

    @Enumerated(EnumType.STRING)
    private MonthlyRentStatus status;

    private LocalDate dueDate;

    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.paidAmount == null) {
            this.paidAmount = 0.0;
        }

        if (this.dueAmount == null) {
            this.dueAmount = this.rentAmount;
        }
    }
}