package com.arcotcabs.arcotcabs_backend.service;

import com.arcotcabs.arcotcabs_backend.model.Expense;
import com.arcotcabs.arcotcabs_backend.model.Invoice;
import com.arcotcabs.arcotcabs_backend.model.enums.InvoiceStatus;
import com.arcotcabs.arcotcabs_backend.repository.ExpenseRepository;
import com.arcotcabs.arcotcabs_backend.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepo;
    private final ExpenseRepository expenseRepo;
    private final PdfService pdfService;
    private final S3Service s3Service;

    public InvoiceService(
            InvoiceRepository invoiceRepo,
            ExpenseRepository expenseRepo,
            PdfService pdfService,
            S3Service s3Service
    ) {
        this.invoiceRepo = invoiceRepo;
        this.expenseRepo = expenseRepo;
        this.pdfService = pdfService;
        this.s3Service = s3Service;
    }

    public Invoice generateInvoice(String tripId) {

        List<Expense> expenses = expenseRepo.getByTrip(tripId);

        double total = expenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();

        Invoice invoice = new Invoice();
        invoice.setInvoiceId(UUID.randomUUID().toString());
        invoice.setTripId(tripId);
        invoice.setExpenses(expenses);
        invoice.setTotalAmount(total);
        invoice.setStatus(InvoiceStatus.FINALIZED);
        invoice.setGeneratedAt(System.currentTimeMillis());

        // 🔥 PDF
        byte[] pdfBytes = pdfService.generateInvoicePdf(invoice);

        // 🔥 S3
        String pdfUrl =
                s3Service.uploadInvoice(invoice.getInvoiceId(), pdfBytes);

        invoice.setInvoicePdfUrl(pdfUrl);

        invoiceRepo.save(invoice);

        return invoice;
    }
}
