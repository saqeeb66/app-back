package com.arcotcabs.arcotcabs_backend.service;



import com.arcotcabs.arcotcabs_backend.model.Expense;
import com.arcotcabs.arcotcabs_backend.model.Invoice;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    public byte[] generateInvoicePdf(Invoice invoice) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 12);

            document.add(new Paragraph("Arcot Cabs - Invoice", titleFont));
            document.add(new Paragraph("Invoice ID: " + invoice.getInvoiceId()));
            document.add(new Paragraph("Trip ID: " + invoice.getTripId()));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Expenses:", titleFont));
            document.add(new Paragraph(" "));

            for (Expense e : invoice.getExpenses()) {
                document.add(
                        new Paragraph(
                                e.getType() + " - ₹" + e.getAmount() +
                                        " (" + e.getDescription() + ")",
                                normalFont
                        )
                );
            }

            document.add(new Paragraph(" "));
            document.add(
                    new Paragraph(
                            "TOTAL AMOUNT: ₹" + invoice.getTotalAmount(),
                            titleFont
                    )
            );

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }
}
