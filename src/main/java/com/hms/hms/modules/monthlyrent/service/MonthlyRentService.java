package com.hms.hms.modules.monthlyrent.service;

import com.hms.hms.modules.monthlyrent.dto.MonthlyRentInvoiceResponse;
import com.hms.hms.modules.monthlyrent.dto.MonthlyRentPaymentRequest;
import com.hms.hms.modules.monthlyrent.dto.MonthlyRentSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface MonthlyRentService {

    MonthlyRentInvoiceResponse generateMonthlyRent(UUID bookingId);

    MonthlyRentInvoiceResponse payMonthlyRent(
            UUID invoiceId,
            MonthlyRentPaymentRequest request
    );

    List<MonthlyRentInvoiceResponse> getMyMonthlyRents();

    List<MonthlyRentInvoiceResponse> getBranchMonthlyRents();

    List<MonthlyRentInvoiceResponse> getPendingMonthlyRents();

    MonthlyRentSummaryResponse getMonthlyRentSummary();
}