package com.hms.hms.modules.booking.service.serviceimpl;

import com.hms.hms.common.enums.BedStatus;
import com.hms.hms.common.enums.BookingStatus;
import com.hms.hms.common.enums.Role;
import com.hms.hms.common.exception.CustomException;
import com.hms.hms.modules.booking.entity.Booking;
import com.hms.hms.modules.booking.repository.BookingRepository;
import com.hms.hms.modules.branch.entity.Branch;
import com.hms.hms.modules.branch.repository.BranchRepository;
import com.hms.hms.modules.notification.event.NotificationEvent;
import com.hms.hms.modules.room.entity.Bed;
import com.hms.hms.modules.room.repository.BedRepository;
import com.hms.hms.modules.room.repository.RoomRepository;
import com.hms.hms.modules.user.entity.User;
import com.hms.hms.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BedRepository bedRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private UUID bookingId;
    private UUID branchId;
    private UUID bedId;
    private UUID userId;

    private Branch branch;
    private User manager;
    private User publicUser;
    private Bed bed;
    private Booking booking;

    @BeforeEach
    void setUp() {
        bookingId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        bedId = UUID.randomUUID();
        userId = UUID.randomUUID();

        branch = Branch.builder()
                .id(branchId)
                .hostelName("StayNest")
                .build();

        manager = User.builder()
                .id(UUID.randomUUID())
                .email("manager@test.com")
                .role(Role.MANAGER)
                .branch(branch)
                .build();

        publicUser = User.builder()
                .id(userId)
                .email("tenant@test.com")
                .role(Role.PUBLIC_USER)
                .build();

        bed = Bed.builder()
                .id(bedId)
                .status(BedStatus.RESERVED)
                .build();

        booking = Booking.builder()
                .id(bookingId)
                .user(publicUser)
                .branchId(branchId)
                .bedId(bedId)
                .status(BookingStatus.PENDING)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "manager@test.com",
                        null
                )
        );
    }

    @Test
    void approveBooking_success_shouldOccupyBedAndPromoteTenant() {

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(userRepository.findByEmail("manager@test.com"))
                .thenReturn(Optional.of(manager));

        when(branchRepository.findById(branchId))
                .thenReturn(Optional.of(branch));

        when(bedRepository.findById(bedId))
                .thenReturn(Optional.of(bed));

        String result = bookingService.approveBooking(bookingId);

        assertEquals("Booking approved successfully", result);
        assertEquals(BedStatus.OCCUPIED, bed.getStatus());
        assertEquals(userId, bed.getTenantId());
        assertEquals(Role.TENANT, publicUser.getRole());
        assertEquals(branch, publicUser.getBranch());
        assertEquals(BookingStatus.APPROVED, booking.getStatus());

        verify(bedRepository).save(bed);
        verify(userRepository).save(publicUser);
        verify(bookingRepository).save(booking);
        verify(eventPublisher).publishEvent(any(NotificationEvent.class));
    }

    @Test
    void approveBooking_bookingNotFound_shouldThrowException() {

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        CustomException exception = assertThrows(
                CustomException.class,
                () -> bookingService.approveBooking(bookingId)
        );

        assertEquals("Booking not found", exception.getMessage());
    }

    @Test
    void rejectBooking_success_shouldCancelBookingAndFreeBed() {

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(bedRepository.findById(bedId))
                .thenReturn(Optional.of(bed));

        String result = bookingService.rejectBooking(bookingId);

        assertEquals(
                "Booking rejected and refund processed successfully",
                result
        );

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(BedStatus.AVAILABLE, bed.getStatus());
        assertTrue(booking.getRefundProcessed());
        assertNotNull(booking.getRefundId());

        verify(bedRepository).save(bed);
        verify(bookingRepository).save(booking);
        verify(eventPublisher).publishEvent(any(NotificationEvent.class));
    }

    @Test
    void checkoutTenant_success_shouldCheckoutAndFreeBed() {

        booking.setStatus(BookingStatus.APPROVED);
        bed.setStatus(BedStatus.OCCUPIED);

        when(bookingRepository.findByIdAndStatus(
                bookingId,
                BookingStatus.APPROVED
        )).thenReturn(Optional.of(booking));

        when(bedRepository.findById(bedId))
                .thenReturn(Optional.of(bed));

        String result = bookingService.checkoutTenant(bookingId);

        assertEquals("Tenant checked out successfully", result);
        assertEquals(BookingStatus.CHECKED_OUT, booking.getStatus());
        assertEquals(BedStatus.AVAILABLE, bed.getStatus());
        assertNotNull(booking.getCheckoutDate());

        verify(bookingRepository).save(booking);
        verify(bedRepository).save(bed);
        verify(eventPublisher).publishEvent(any(NotificationEvent.class));
    }
}