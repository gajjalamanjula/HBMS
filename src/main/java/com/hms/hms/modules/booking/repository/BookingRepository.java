package com.hms.hms.modules.booking.repository;

import com.hms.hms.common.enums.BookingStatus;
import com.hms.hms.modules.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByBranchId(UUID branchId);

    Optional<Booking> findByIdAndStatus(
            UUID bookingId,
            BookingStatus status
    );

    Optional<Booking> findByUser_IdAndStatus(
            UUID userId,
            BookingStatus status
    );
}
