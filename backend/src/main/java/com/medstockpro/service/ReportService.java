package com.medstockpro.service;

import com.medstockpro.entity.StockBatch;
import com.medstockpro.repository.SaleBillRepository;
import com.medstockpro.repository.StockBatchRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final StockBatchRepository batchRepository;
    private final SaleBillRepository   saleBillRepository;

    // Excel report: medicines expiring within N days
    public byte[] generateExpiryReport(int days) {
        LocalDate today    = LocalDate.now();
        LocalDate deadline = today.plusDays(days);

        List<StockBatch> batches =
                batchRepository.findExpiringBefore(today, deadline);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Expiry Report");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(
                    IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header row
            Row header = sheet.createRow(0);
            String[] cols = {
                "Medicine", "Generic Name", "Batch No",
                "Expiry Date", "Qty Remaining", "Selling Price",
                "Supplier", "Days to Expiry"
            };
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (StockBatch batch : batches) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(
                        batch.getMedicine().getName());
                row.createCell(1).setCellValue(
                        batch.getMedicine().getGenericName() != null
                        ? batch.getMedicine().getGenericName() : "");
                row.createCell(2).setCellValue(batch.getBatchNumber());
                row.createCell(3).setCellValue(
                        batch.getExpiryDate().toString());
                row.createCell(4).setCellValue(
                        batch.getQuantityRemaining());
                row.createCell(5).setCellValue(
                        batch.getSellingPrice().doubleValue());
                row.createCell(6).setCellValue(
                        batch.getSupplier() != null
                        ? batch.getSupplier().getName() : "N/A");
                long daysLeft = today.until(
                        batch.getExpiryDate(),
                        java.time.temporal.ChronoUnit.DAYS);
                row.createCell(7).setCellValue(daysLeft);
            }

            // Auto-size columns
            for (int i = 0; i < cols.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to generate expiry report", e);
        }
    }

    // Sales summary between date range
    public BigDecimal getSalesSummary(LocalDateTime from,
                                       LocalDateTime to) {
        return saleBillRepository.sumTotalBetween(from, to);
    }

    // Total stock valuation
    public Double getStockValuation() {
        return batchRepository.getTotalStockValuation();
    }
}