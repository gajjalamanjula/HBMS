package com.hms.hms.modules.monthlyrent.scheduler;

import com.hms.hms.common.enums.MonthlyRentStatus;
import com.hms.hms.modules.monthlyrent.entity.MonthlyRentInvoice;
import com.hms.hms.modules.monthlyrent.repository.MonthlyRentInvoiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MonthlyRentScheduler {

    private final MonthlyRentInvoiceRepository monthlyRentInvoiceRepository;

    @Scheduled(cron = "0 0 9 * * *")
    public void markOverdueMonthlyRents() {

        List<MonthlyRentInvoice> overdueInvoices =
                monthlyRentInvoiceRepository
                        .findByStatusAndDueDateBefore(
                                MonthlyRentStatus.PENDING,
                                LocalDate.now()
                        );

        overdueInvoices.forEach(invoice ->
                invoice.setStatus(MonthlyRentStatus.OVERDUE)
        );

        monthlyRentInvoiceRepository.saveAll(overdueInvoices);
    }

    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void sendMonthlyRentReminder() {

        LocalDate reminderDate = LocalDate.now().plusDays(3);

        List<MonthlyRentInvoice> invoices =
                monthlyRentInvoiceRepository
                        .findByStatusAndDueDate(
                                MonthlyRentStatus.PENDING,
                                reminderDate
                        );

        // Notification/email integration can be added here later.
        // For now, this confirms invoices due in 3 days are detected.
    }
}