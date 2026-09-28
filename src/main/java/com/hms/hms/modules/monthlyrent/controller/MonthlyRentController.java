package com.hms.hms.modules.monthlyrent.controller;

import com.hms.hms.modules.monthlyrent.dto.MonthlyRentInvoiceResponse;
import com.hms.hms.modules.monthlyrent.dto.MonthlyRentPaymentRequest;
import com.hms.hms.modules.monthlyrent.dto.MonthlyRentSummaryResponse;
import com.hms.hms.modules.monthlyrent.service.MonthlyRentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/monthly-rents")
@RequiredArgsConstructor
public class MonthlyRentController {

    private final MonthlyRentService monthlyRentService;

    @PostMapping("/generate/{bookingId}")
    public MonthlyRentInvoiceResponse generateMonthlyRent(
            @PathVariable UUID bookingId
    ) {
        return monthlyRentService.generateMonthlyRent(bookingId);
    }

    @PostMapping("/pay/{invoiceId}")
    public MonthlyRentInvoiceResponse payMonthlyRent(
            @PathVariable UUID invoiceId,
            @Valid @RequestBody MonthlyRentPaymentRequest request
    ) {
        return monthlyRentService.payMonthlyRent(
                invoiceId,
                request
        );
    }

    @GetMapping("/my")
    public List<MonthlyRentInvoiceResponse> getMyMonthlyRents() {
        return monthlyRentService.getMyMonthlyRents();
    }

    @GetMapping("/branch")
    public List<MonthlyRentInvoiceResponse> getBranchMonthlyRents() {
        return monthlyRentService.getBranchMonthlyRents();
    }

    @GetMapping("/branch/pending")
    public List<MonthlyRentInvoiceResponse> getPendingMonthlyRents() {
        return monthlyRentService.getPendingMonthlyRents();
    }

    @GetMapping("/branch/summary")
    public MonthlyRentSummaryResponse getMonthlyRentSummary() {
        return monthlyRentService.getMonthlyRentSummary();
    }
}
