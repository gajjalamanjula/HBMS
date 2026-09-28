package com.hms.hms.modules.booking.entity;

import com.hms.hms.common.enums.BookingStatus;
import com.hms.hms.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private User user;

    private UUID branchId;
    private UUID bedId;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private LocalDateTime createdAt;

    private Double totalRent;

    private Double advanceAmount;

    private Double remainingAmount;

    private Boolean refundProcessed = false;

    private String paymentId;

    private String refundId;

    private Boolean advancePaid = false;

    private Boolean fullPaymentPaid = false;

    private LocalDateTime checkoutDate;
}
