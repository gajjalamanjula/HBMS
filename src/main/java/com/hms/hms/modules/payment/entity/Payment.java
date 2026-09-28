package com.hms.hms.modules.payment.entity;

import com.hms.hms.common.enums.PaymentMode;
import com.hms.hms.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID tenantId;
    private UUID branchId;

    private double amount;

    @Enumerated(EnumType.STRING)
    private PaymentMode mode; // CASH / ONLINE

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // SUCCESS / FAILED

    private String razorpayOrderId;
    private String razorpayPaymentId;

    private String invoiceUrl;

    private LocalDateTime createdAt;
}