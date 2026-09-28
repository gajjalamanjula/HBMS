package com.hms.hms.modules.monthlyrent.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonthlyRentSummaryResponse {

    private Double totalCollected;

    private Double totalPending;

    private Long paidInvoices;

    private Long pendingInvoices;
}