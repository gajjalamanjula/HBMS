package com.hms.hms.modules.monthlyrent.service.serviceimpl;

import com.hms.hms.common.enums.BookingStatus;
import com.hms.hms.common.enums.MonthlyRentStatus;
import com.hms.hms.common.exception.CustomException;
import com.hms.hms.config.CustomUserDetails;
import com.hms.hms.modules.booking.entity.Booking;
import com.hms.hms.modules.booking.repository.BookingRepository;
import com.hms.hms.modules.monthlyrent.dto.MonthlyRentInvoiceResponse;
import com.hms.hms.modules.monthlyrent.dto.MonthlyRentPaymentRequest;
import com.hms.hms.modules.monthlyrent.dto.MonthlyRentSummaryResponse;
import com.hms.hms.modules.monthlyrent.entity.MonthlyRentInvoice;
import com.hms.hms.modules.monthlyrent.repository.MonthlyRentInvoiceRepository;
import com.hms.hms.modules.monthlyrent.service.MonthlyRentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MonthlyRentServiceImpl implements MonthlyRentService {

    private final MonthlyRentInvoiceRepository monthlyRentInvoiceRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    @Override
    public MonthlyRentInvoiceResponse generateMonthlyRent(UUID bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new CustomException(
                                "Booking not found",
                                HttpStatus.NOT_FOUND
                        ));

        if (!(booking.getStatus() == BookingStatus.APPROVED ||
                booking.getStatus() == BookingStatus.PAID)) {

            throw new CustomException(
                    "Rent can be generated only for active tenant",
                    HttpStatus.BAD_REQUEST
            );
        }

        LocalDate today = LocalDate.now();

        monthlyRentInvoiceRepository
                .findByBookingIdAndRentMonthAndRentYear(
                        booking.getId(),
                        today.getMonthValue(),
                        today.getYear()
                )
                .ifPresent(invoice -> {
                    throw new CustomException(
                            "Monthly rent already generated for this month",
                            HttpStatus.CONFLICT
                    );
                });

        Double rentAmount = booking.getTotalRent();

        if (rentAmount == null || rentAmount <= 0) {
            throw new CustomException(
                    "Booking rent amount is missing",
                    HttpStatus.BAD_REQUEST
            );
        }

        MonthlyRentInvoice invoice = MonthlyRentInvoice.builder()
                .bookingId(booking.getId())
                .tenantId(booking.getUser().getId())
                .branchId(booking.getBranchId())
                .rentMonth(today.getMonthValue())
                .rentYear(today.getYear())
                .rentAmount(rentAmount)
                .paidAmount(0.0)
                .dueAmount(rentAmount)
                .status(MonthlyRentStatus.PENDING)
                .dueDate(today.plusDays(5))
                .build();

        return mapToResponse(
                monthlyRentInvoiceRepository.save(invoice)
        );
    }

    @Transactional
    @Override
    public MonthlyRentInvoiceResponse payMonthlyRent(
            UUID invoiceId,
            MonthlyRentPaymentRequest request
    ) {

        MonthlyRentInvoice invoice =
                monthlyRentInvoiceRepository.findById(invoiceId)
                        .orElseThrow(() ->
                                new CustomException(
                                        "Monthly rent invoice not found",
                                        HttpStatus.NOT_FOUND
                                ));

        if (invoice.getStatus() == MonthlyRentStatus.PAID) {
            throw new CustomException(
                    "Monthly rent already paid",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new CustomException(
                    "Invalid payment amount",
                    HttpStatus.BAD_REQUEST
            );
        }

        Double newPaidAmount =
                invoice.getPaidAmount() + request.getAmount();

        if (newPaidAmount > invoice.getRentAmount()) {
            throw new CustomException(
                    "Payment amount exceeds due amount",
                    HttpStatus.BAD_REQUEST
            );
        }

        Double dueAmount =
                invoice.getRentAmount() - newPaidAmount;

        invoice.setPaidAmount(newPaidAmount);
        invoice.setDueAmount(dueAmount);

        if (Math.abs(dueAmount) < 0.001) {
            invoice.setDueAmount(0.0);
            invoice.setStatus(MonthlyRentStatus.PAID);
            invoice.setPaidAt(LocalDateTime.now());
        } else {
            invoice.setStatus(MonthlyRentStatus.PARTIAL_PAID);
        }

        return mapToResponse(
                monthlyRentInvoiceRepository.save(invoice)
        );
    }

    @Override
    public List<MonthlyRentInvoiceResponse> getMyMonthlyRents() {

        CustomUserDetails user = getCurrentUser();

        return monthlyRentInvoiceRepository
                .findByTenantId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<MonthlyRentInvoiceResponse> getBranchMonthlyRents() {

        CustomUserDetails user = getCurrentUser();

        if (user.getBranchId() == null) {
            throw new CustomException(
                    "User is not assigned to branch",
                    HttpStatus.BAD_REQUEST
            );
        }

        return monthlyRentInvoiceRepository
                .findByBranchId(user.getBranchId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<MonthlyRentInvoiceResponse> getPendingMonthlyRents() {

        CustomUserDetails user = getCurrentUser();

        if (user.getBranchId() == null) {
            throw new CustomException(
                    "User is not assigned to branch",
                    HttpStatus.BAD_REQUEST
            );
        }

        return monthlyRentInvoiceRepository
                .findByBranchIdAndStatus(
                        user.getBranchId(),
                        MonthlyRentStatus.PENDING
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public MonthlyRentSummaryResponse getMonthlyRentSummary() {

        CustomUserDetails user = getCurrentUser();

        if (user.getBranchId() == null) {
            throw new CustomException(
                    "User is not assigned to branch",
                    HttpStatus.BAD_REQUEST
            );
        }

        List<MonthlyRentInvoice> invoices =
                monthlyRentInvoiceRepository.findByBranchId(
                        user.getBranchId()
                );

        double totalCollected = invoices.stream()
                .mapToDouble(invoice ->
                        invoice.getPaidAmount() != null
                                ? invoice.getPaidAmount()
                                : 0.0
                )
                .sum();

        double totalPending = invoices.stream()
                .mapToDouble(invoice ->
                        invoice.getDueAmount() != null
                                ? invoice.getDueAmount()
                                : 0.0
                )
                .sum();

        long paidInvoices = invoices.stream()
                .filter(invoice ->
                        invoice.getStatus() == MonthlyRentStatus.PAID
                )
                .count();

        long pendingInvoices = invoices.stream()
                .filter(invoice ->
                        invoice.getStatus() == MonthlyRentStatus.PENDING ||
                                invoice.getStatus() == MonthlyRentStatus.PARTIAL_PAID
                )
                .count();

        return MonthlyRentSummaryResponse.builder()
                .totalCollected(totalCollected)
                .totalPending(totalPending)
                .paidInvoices(paidInvoices)
                .pendingInvoices(pendingInvoices)
                .build();
    }

    private MonthlyRentInvoiceResponse mapToResponse(
            MonthlyRentInvoice invoice
    ) {

        return MonthlyRentInvoiceResponse.builder()
                .id(invoice.getId())
                .bookingId(invoice.getBookingId())
                .tenantId(invoice.getTenantId())
                .branchId(invoice.getBranchId())
                .rentMonth(invoice.getRentMonth())
                .rentYear(invoice.getRentYear())
                .rentAmount(invoice.getRentAmount())
                .paidAmount(invoice.getPaidAmount())
                .dueAmount(invoice.getDueAmount())
                .status(invoice.getStatus())
                .dueDate(invoice.getDueDate())
                .build();
    }

    private CustomUserDetails getCurrentUser() {

        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (!(principal instanceof CustomUserDetails user)) {
            throw new CustomException(
                    "Unauthorized",
                    HttpStatus.UNAUTHORIZED
            );
        }

        return user;
    }
}