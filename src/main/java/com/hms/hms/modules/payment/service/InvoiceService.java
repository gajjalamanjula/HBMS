package com.hms.hms.modules.payment.service;

import com.hms.hms.modules.payment.entity.Payment;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.*;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;

import com.itextpdf.layout.Document;
import java.io.File;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;

@Service
public class InvoiceService {

    public File generateInvoice(Payment payment) throws Exception {

        File file = File.createTempFile("invoice_" + payment.getId(), ".pdf");

        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);

        Document document = new Document(pdf);

        document.add(new Paragraph("HOSTEL INVOICE").setBold().setFontSize(18));
        document.add(new Paragraph("Payment ID: " + payment.getId()));
        document.add(new Paragraph("Amount: ₹" + payment.getAmount()));
        document.add(new Paragraph("Mode: " + payment.getMode()));
        document.add(new Paragraph("Date: " + payment.getCreatedAt()));

        document.close();

        return file;
    }
}