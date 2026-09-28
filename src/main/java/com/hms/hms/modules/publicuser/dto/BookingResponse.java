package com.hms.hms.modules.publicuser.dto;

import com.hms.hms.common.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BookingResponse {

    private String message;

    private UUID bookingId;


    private BookingStatus status;
}
