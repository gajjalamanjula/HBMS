package com.hms.hms.modules.booking.dto;

import com.hms.hms.common.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PublicBookingResponse {

    private UUID bookingId;

    private String publicUserName;

    private String publicUserPhone;

    private String publicUserEmail;

    private String hostelName;

    private String roomNumber;

    private String bedNumber;

    private Double amountPaid;

    private BookingStatus status;
}