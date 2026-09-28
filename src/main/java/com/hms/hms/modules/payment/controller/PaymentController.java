package com.hms.hms.modules.payment.controller;


import com.hms.hms.common.response.ApiResponse;
import com.hms.hms.modules.payment.dto.CashPaymentRequest;
import com.hms.hms.modules.payment.dto.OnlinePaymentRequest;
import com.hms.hms.modules.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/cash")
    public ApiResponse<?> cash(@Valid @RequestBody CashPaymentRequest request) {

        return ApiResponse.builder()
                .success(true)
                .message(paymentService.recordCashPayment(request))
                .data(null)
                .build();
    }

    @GetMapping("/tenant/{id}")
    public ApiResponse<?> tenant(@PathVariable UUID id) {
        return ApiResponse.builder()
                .success(true)
                .message("Payments fetched")
                .data(paymentService.getTenantPayments(id))
                .build();
    }

    @GetMapping("/invoice/{id}")
    public ApiResponse<?> invoice(@PathVariable UUID id) {
        return ApiResponse.builder()
                .success(true)
                .message("Invoice URL")
                .data(paymentService.getInvoice(id))
                .build();
    }

    @GetMapping("/due/{branchId}")
    public ApiResponse<?> due(@PathVariable UUID branchId) {
        return ApiResponse.builder()
                .success(true)
                .message("Branch payments")
                .data(paymentService.getBranchPayments(branchId))
                .build();
    }

    @PostMapping("/online/initiate")
    public ApiResponse<?> initiate( @Valid @RequestBody OnlinePaymentRequest request) {
        return ApiResponse.builder()
                .success(true)
                .message("Order created")
                .data(paymentService.initiateOnlinePayment(request.getAmount()))
                .build();
    }

    @PostMapping("/online/webhook")
    public void webhook(@RequestBody String payload,
                        @RequestHeader("X-Razorpay-Signature") String signature) {
        paymentService.handleWebhook(payload, signature);
    }

    @GetMapping("/summary/{branchId}")
    public ApiResponse<?> summary(@PathVariable UUID branchId) {
        return ApiResponse.builder()
                .success(true)
                .message("Summary")
                .data(paymentService.getSummary(branchId))
                .build();
    }
}
