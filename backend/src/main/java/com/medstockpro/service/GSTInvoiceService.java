package com.medstockpro.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.medstockpro.entity.SaleBill;
import com.medstockpro.entity.SaleItem;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class GSTInvoiceService {

    @Value("${app.name}")
    private String appName;

    @Value("${app.gst-number}")
    private String gstNumber;

    @Value("${app.description}")
    private String appDescription;

    private static final DeviceRgb HEADER_COLOR =
            new DeviceRgb(41, 128, 185);
    private static final DeviceRgb TABLE_HEADER_COLOR =
            new DeviceRgb(236, 240, 241);
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");

    public byte[] generateInvoice(SaleBill bill) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter   writer = new PdfWriter(baos);
             PdfDocument pdf    = new PdfDocument(writer);
             Document    doc    = new Document(pdf)) {

            // ── Header ──────────────────────────────────────
            doc.add(new Paragraph(appName)
                    .setBold()
                    .setFontSize(22)
                    .setFontColor(HEADER_COLOR)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(new Paragraph(appDescription)
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(new Paragraph("GSTIN: " + gstNumber)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(new Paragraph(
                    "─────────────────────────────────────────")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY));

            // ── Bill Info ────────────────────────────────────
            doc.add(new Paragraph()
                    .add(new Text("GST TAX INVOICE")
                            .setBold().setFontSize(13))
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(new Paragraph(
                    "Bill No: " + bill.getBillNumber() +
                    "     Date: " +
                    bill.getCreatedAt().format(DATE_FMT))
                    .setFontSize(10));

            if (bill.getCustomerName() != null) {
                doc.add(new Paragraph(
                        "Customer: " + bill.getCustomerName() +
                        (bill.getCustomerPhone() != null
                                ? "     Ph: " + bill.getCustomerPhone()
                                : ""))
                        .setFontSize(10));
            }

            doc.add(new Paragraph(" "));

            // ── Items Table ──────────────────────────────────
            Table table = new Table(UnitValue.createPercentArray(
                    new float[]{3.5f, 1f, 1.5f, 1f, 1.5f, 1.5f}))
                    .setWidth(UnitValue.createPercentValue(100));

            // Table headers
            String[] headers = {
                "Medicine", "Qty", "Unit Price", "GST%",
                "GST Amt", "Total"
            };
            for (String h : headers) {
                table.addHeaderCell(
                        new Cell().add(new Paragraph(h)
                                .setBold().setFontSize(9))
                                .setBackgroundColor(TABLE_HEADER_COLOR));
            }

            // Table rows
            for (SaleItem item : bill.getItems()) {
                table.addCell(new Cell().add(
                        new Paragraph(item.getMedicine().getName())
                                .setFontSize(9)));
                table.addCell(new Cell().add(
                        new Paragraph(String.valueOf(
                                item.getQuantitySold()))
                                .setFontSize(9)
                                .setTextAlignment(TextAlignment.CENTER)));
                table.addCell(new Cell().add(
                        new Paragraph("₹" + item.getUnitPrice())
                                .setFontSize(9)
                                .setTextAlignment(TextAlignment.RIGHT)));
                table.addCell(new Cell().add(
                        new Paragraph(item.getGstSlab() + "%")
                                .setFontSize(9)
                                .setTextAlignment(TextAlignment.CENTER)));
                table.addCell(new Cell().add(
                        new Paragraph("₹" + item.getGstAmount())
                                .setFontSize(9)
                                .setTextAlignment(TextAlignment.RIGHT)));
                table.addCell(new Cell().add(
                        new Paragraph("₹" + item.getLineTotal())
                                .setFontSize(9)
                                .setTextAlignment(TextAlignment.RIGHT)));
            }

            doc.add(table);
            doc.add(new Paragraph(" "));

            // ── GST Summary ──────────────────────────────────
            BigDecimal cgst = bill.getGstAmount()
                    .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

            doc.add(new Paragraph(
                    "Subtotal:         ₹" + bill.getSubtotal())
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT));
            doc.add(new Paragraph("CGST:             ₹" + cgst)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT));
            doc.add(new Paragraph("SGST:             ₹" + cgst)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT));
            doc.add(new Paragraph(
                    "GRAND TOTAL:      ₹" + bill.getTotalAmount())
                    .setBold()
                    .setFontSize(13)
                    .setFontColor(HEADER_COLOR)
                    .setTextAlignment(TextAlignment.RIGHT));

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(
                    "Thank you for your purchase!")
                    .setItalic()
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY));

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to generate GST invoice PDF", e);
        }

        return baos.toByteArray();
    }
}