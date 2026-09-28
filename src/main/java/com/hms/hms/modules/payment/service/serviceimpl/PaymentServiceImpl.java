package com.hms.hms.modules.payment.service.serviceimpl;

import com.hms.hms.common.enums.PaymentMode;
import com.hms.hms.common.enums.PaymentStatus;
import com.hms.hms.common.exception.CustomException;
import com.hms.hms.modules.notification.event.NotificationEvent;
import com.hms.hms.modules.payment.dto.CashPaymentRequest;
import com.hms.hms.modules.payment.dto.PaymentResponseDTO;
import com.hms.hms.modules.payment.entity.Payment;
import com.hms.hms.modules.payment.repository.PaymentRepository;
import com.hms.hms.modules.payment.service.InvoiceService;
import com.hms.hms.modules.payment.service.PaymentService;
import com.hms.hms.modules.payment.service.S3Service;
import com.hms.hms.modules.user.entity.User;
import com.hms.hms.modules.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final InvoiceService invoiceService;
    private final S3Service s3Service;
    private final ApplicationEventPublisher eventPublisher;
    @Transactional
    @Override
    public String recordCashPayment(CashPaymentRequest request) {

        User tenant = userRepository.findById(request.getTenantId())
                .orElseThrow(() -> new CustomException("Tenant not found", HttpStatus.NOT_FOUND));

        Payment payment = Payment.builder()
                .tenantId(tenant.getId())
                .branchId(tenant.getBranch().getId())
                .amount(request.getAmount())
                .mode(PaymentMode.CASH)
                .status(PaymentStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        try {
            // 1. GENERATE PDF
            File file = invoiceService.generateInvoice(payment);

            //  2. UPLOAD TO S3
            String key = "invoices/" + payment.getId() + ".pdf";
            s3Service.upload(file, key);

            //  3. SAVE KEY IN DB
            payment.setInvoiceUrl(key);
            paymentRepository.save(payment);
            eventPublisher.publishEvent(
                    new NotificationEvent(
                            tenant.getId(),
                            "Payment Successful",
                            "Your payment was completed successfully"
                    )
            );

        } catch (Exception e) {
            throw new RuntimeException("Invoice generation failed");
        }

        return "Cash payment recorded + invoice generated";
    }
    @Override
    public List <PaymentResponseDTO> getTenantPayments(UUID tenantId) {

        return paymentRepository.findByTenantId(tenantId)
                .stream()
                .map(p -> PaymentResponseDTO.builder()
                        .id(p.getId())
                        .amount(p.getAmount())
                        .status(p.getStatus().name())
                        .mode(p.getMode().name())
                        .invoiceUrl(p.getInvoiceUrl())
                        .build())
                .toList();
    }

    @Override
    public String getInvoice(UUID paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException("Payment not found", HttpStatus.NOT_FOUND));

        return s3Service.generatePresignedUrl(payment.getInvoiceUrl());
    }

    @Override
    public List<PaymentResponseDTO> getBranchPayments(UUID branchId) {

        return paymentRepository.findByBranchId(branchId)
                .stream()
                .map(p -> PaymentResponseDTO.builder()
                        .id(p.getId())
                        .amount(p.getAmount())
                        .status(p.getStatus().name())
                        .mode(p.getMode().name())
                        .invoiceUrl(p.getInvoiceUrl())
                        .build())
                .toList();
    }

    @Override
    public String initiateOnlinePayment(double amount) {

        // TODO Razorpay integration
        return "razorpay_order_id_dummy";
    }

    @Override
    public void

    handleWebhook(String payload, String signature) {
        // TODO verify HMAC + update payment
    }

    @Override
    public Object getSummary(UUID branchId) {

        List<Payment> payments = paymentRepository.findByBranchId(branchId);

        double total = payments.stream().mapToDouble(Payment::getAmount).sum();

        Map<String, Object> map = new HashMap<>();
        map.put("totalCollected", total);
        map.put("count", payments.size());

        return map;
    }
}