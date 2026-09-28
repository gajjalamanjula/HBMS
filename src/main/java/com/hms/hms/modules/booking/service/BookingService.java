package com.hms.hms.modules.booking.service;

import com.hms.hms.modules.booking.dto.PublicBookingResponse;
import com.hms.hms.modules.booking.entity.Booking;

import java.util.List;
import java.util.UUID;

public interface BookingService {

    String approveBooking(UUID bookingId);
    String rejectBooking(UUID bookingId);
    List<Booking> getBranchBookings();
    List<PublicBookingResponse> getManagerBookings(String email);
    String checkoutTenant(UUID bookingId);

}