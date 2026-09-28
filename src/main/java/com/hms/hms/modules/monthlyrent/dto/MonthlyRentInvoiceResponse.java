package com.hms.hms.modules.monthlyrent.dto;

import com.hms.hms.common.enums.MonthlyRentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class MonthlyRentInvoiceResponse {

    private UUID id;

    private UUID bookingId;
    private UUID tenantId;
    private UUID branchId;

    private Integer rentMonth;
    private Integer rentYear;

    private Double rentAmount;
    private Double paidAmount;
    private Double dueAmount;

    private MonthlyRentStatus status;

    private LocalDate dueDate;
}