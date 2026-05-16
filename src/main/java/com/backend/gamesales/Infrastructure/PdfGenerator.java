package com.backend.gamesales.Infrastructure;

import com.backend.gamesales.Model.Invoice;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.io.ByteArrayOutputStream;

@Component
@Slf4j
public class PdfGenerator {

    public byte[] generateInvoicePdf(Invoice invoice) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PdfWriter writer   = new PdfWriter(outputStream);
            PdfDocument pdf      = new PdfDocument(writer);
            Document    document = new Document(pdf);

            addHeader(document);
            addInvoiceInfo(document, invoice);
            addBuyerInfo(document, invoice);
            addItemsTable(document, invoice);
            addTotal(document, invoice);
            addFooter(document);

            document.close();

            log.info("PDF generated | invoiceNumber: {}", invoice.getInvoiceNumber());
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("PDF generation failed | invoiceNumber: {} | error: {}",
                    invoice.getInvoiceNumber(), e.getMessage());
            throw new RuntimeException("Could not generate invoice PDF", e);
        }
    }

    private void addHeader(Document document) {
        document.add(new Paragraph("GAMESALES")
                .setFontSize(24)
                .setBold()
                .setFontColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Digital Game Marketplace")
                .setFontSize(12)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph(" "));
    }

    private void addInvoiceInfo(Document document, Invoice invoice) {
        document.add(new Paragraph("INVOICE")
                .setFontSize(18)
                .setBold());

        document.add(new Paragraph("Invoice Number: " + invoice.getInvoiceNumber())
                .setFontSize(11));

        document.add(new Paragraph("Date: " + invoice.getIssuedAt().toLocalDate())
                .setFontSize(11));

        document.add(new Paragraph(" "));
    }

    private void addBuyerInfo(Document document, Invoice invoice) {
        document.add(new Paragraph("Bill To:")
                .setFontSize(13)
                .setBold());

        document.add(new Paragraph(
                invoice.getBuyerFullName() != null ? invoice.getBuyerFullName() : "N/A")
                .setFontSize(11));

        document.add(new Paragraph(invoice.getBuyerEmail())
                .setFontSize(11));

        if (invoice.getBuyerCountry() != null) {
            document.add(new Paragraph(invoice.getBuyerCountry())
                    .setFontSize(11));
        }

        document.add(new Paragraph(" "));
    }

    private void addItemsTable(Document document, Invoice invoice) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{60, 20, 20}))
                .useAllAvailableWidth();

        table.addHeaderCell(headerCell("Description"));
        table.addHeaderCell(headerCell("Qty"));
        table.addHeaderCell(headerCell("Price"));

        table.addCell(new Cell().add(new Paragraph(invoice.getGameTitle())));
        table.addCell(new Cell().add(new Paragraph("1")));
        table.addCell(new Cell().add(new Paragraph(
                invoice.getAmount() + " " + invoice.getCurrency())));

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addTotal(Document document, Invoice invoice) {
        document.add(new Paragraph(
                "Total: " + invoice.getAmount() + " " + invoice.getCurrency())
                .setFontSize(14)
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT));
    }

    private void addFooter(Document document) {
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Thank you for your purchase!")
                .setFontSize(11)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));
    }

    private Cell headerCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER);
    }
}
