package com.hms.hms.modules.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PaymentResponseDTO {

    private UUID id;
    private double amount;
    private String status;
    private String mode;
    private String invoiceUrl;
}