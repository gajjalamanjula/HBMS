package com.hms.hms.modules.monthlyrent.service.serviceimpl;

import com.hms.hms.common.enums.BookingStatus;
import com.hms.hms.common.enums.MonthlyRentStatus;
import com.hms.hms.common.enums.Role;
import com.hms.hms.common.exception.CustomException;
import com.hms.hms.modules.booking.entity.Booking;
import com.hms.hms.modules.booking.repository.BookingRepository;
import com.hms.hms.modules.monthlyrent.dto.MonthlyRentPaymentRequest;
import com.hms.hms.modules.monthlyrent.entity.MonthlyRentInvoice;
import com.hms.hms.modules.monthlyrent.repository.MonthlyRentInvoiceRepository;
import com.hms.hms.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonthlyRentServiceImplTest {

    @Mock
    private MonthlyRentInvoiceRepository monthlyRentInvoiceRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private MonthlyRentServiceImpl monthlyRentService;

    private UUID bookingId;
    private UUID invoiceId;
    private UUID tenantId;
    private UUID branchId;

    private Booking booking;
    private MonthlyRentInvoice invoice;

    @BeforeEach
    void setUp() {

        bookingId = UUID.randomUUID();
        invoiceId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        User user = User.builder()
                .id(tenantId)
                .name("Tenant User")
                .email("tenant@test.com")
                .role(Role.TENANT)
                .build();

        booking = Booking.builder()
                .id(bookingId)
                .user(user)
                .branchId(branchId)
                .status(BookingStatus.APPROVED)
                .totalRent(5000.0)
                .build();

        invoice = MonthlyRentInvoice.builder()
                .id(invoiceId)
                .bookingId(bookingId)
                .tenantId(tenantId)
                .branchId(branchId)
                .rentAmount(5000.0)
                .paidAmount(0.0)
                .dueAmount(5000.0)
                .status(MonthlyRentStatus.PENDING)
                .build();
    }

    @Test
    void generateMonthlyRent_success() {

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(monthlyRentInvoiceRepository
                .findByBookingIdAndRentMonthAndRentYear(
                        any(),
                        anyInt(),
                        anyInt()
                ))
                .thenReturn(Optional.empty());

        when(monthlyRentInvoiceRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response =
                monthlyRentService.generateMonthlyRent(bookingId);

        assertNotNull(response);

        assertEquals(
                MonthlyRentStatus.PENDING,
                response.getStatus()
        );

        assertEquals(
                5000.0,
                response.getRentAmount()
        );

        verify(monthlyRentInvoiceRepository)
                .save(any(MonthlyRentInvoice.class));
    }

    @Test
    void generateMonthlyRent_bookingNotFound() {

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        CustomException exception =
                assertThrows(
                        CustomException.class,
                        () -> monthlyRentService
                                .generateMonthlyRent(bookingId)
                );

        assertEquals(
                "Booking not found",
                exception.getMessage()
        );
    }

    @Test
    void payMonthlyRent_fullPayment_shouldMarkPaid() {

        MonthlyRentPaymentRequest request =
                new MonthlyRentPaymentRequest();

        request.setAmount(5000.0);

        when(monthlyRentInvoiceRepository.findById(invoiceId))
                .thenReturn(Optional.of(invoice));

        when(monthlyRentInvoiceRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response =
                monthlyRentService.payMonthlyRent(
                        invoiceId,
                        request
                );

        assertEquals(
                MonthlyRentStatus.PAID,
                response.getStatus()
        );

        assertEquals(
                0.0,
                response.getDueAmount()
        );

        assertEquals(
                5000.0,
                response.getPaidAmount()
        );
    }

    @Test
    void payMonthlyRent_overPayment_shouldThrowException() {

        MonthlyRentPaymentRequest request =
                new MonthlyRentPaymentRequest();

        request.setAmount(6000.0);

        when(monthlyRentInvoiceRepository.findById(invoiceId))
                .thenReturn(Optional.of(invoice));

        CustomException exception =
                assertThrows(
                        CustomException.class,
                        () -> monthlyRentService
                                .payMonthlyRent(
                                        invoiceId,
                                        request
                                )
                );

        assertEquals(
                "Payment amount exceeds due amount",
                exception.getMessage()
        );
    }
}