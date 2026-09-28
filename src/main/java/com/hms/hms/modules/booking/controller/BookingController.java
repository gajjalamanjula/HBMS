package com.hms.hms.modules.booking.controller;

import com.hms.hms.modules.booking.dto.PublicBookingResponse;
import com.hms.hms.modules.booking.entity.Booking;
import com.hms.hms.modules.booking.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/manager/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class BookingController {

    private final BookingService bookingService;

    @PutMapping("/{bookingId}/approve")
    public String approveBooking(
            @PathVariable UUID bookingId
    ) {
        return bookingService.approveBooking(bookingId);
    }

    @PutMapping("/{bookingId}/reject")
    public String rejectBooking(
            @PathVariable UUID bookingId
    ) {
        return bookingService.rejectBooking(bookingId);
    }

    @GetMapping
    public List<PublicBookingResponse> getManagerBookings(
            HttpServletRequest request
    ) {

        String email =
                (String) request.getAttribute("userEmail");

        return bookingService.getManagerBookings(email);
    }

    @PatchMapping("/checkout/{bookingId}")
    public ResponseEntity<String> checkout(
            @PathVariable UUID bookingId
    ) {
        return ResponseEntity.ok(
                bookingService.checkoutTenant(
                        bookingId
                )
        );
    }
}