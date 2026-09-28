package com.hms.hms.modules.payment.service;

import com.hms.hms.modules.payment.dto.CashPaymentRequest;
import com.hms.hms.modules.payment.dto.PaymentResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    String recordCashPayment(CashPaymentRequest request);

    List<PaymentResponseDTO> getTenantPayments(UUID tenantId);

    String getInvoice(UUID paymentId);

    List<PaymentResponseDTO> getBranchPayments(UUID branchId);

    String initiateOnlinePayment(double amount);

    void handleWebhook(String payload, String signature);

    Object getSummary(UUID branchId);
}