package com.medstockpro.controller;

import com.medstockpro.dto.ApiResponse;
import com.medstockpro.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports",
     description = "Excel and summary reports")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/expiry-report")
    @Operation(summary = "Download Excel report of expiring medicines")
    public ResponseEntity<byte[]> expiryReport(
            @RequestParam(defaultValue = "90") int days) {

        byte[] bytes = reportService.generateExpiryReport(days);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=expiry-report.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-" +
                        "officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/sales-summary")
    @Operation(summary = "Get sales total between date range")
    public ResponseEntity<ApiResponse<BigDecimal>> salesSummary(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to) {

        BigDecimal total = reportService.getSalesSummary(from, to);
        return ResponseEntity.ok(ApiResponse.success(
                total, "Sales summary retrieved"));
    }

    @GetMapping("/stock-valuation")
    @Operation(summary = "Get total current stock valuation")
    public ResponseEntity<ApiResponse<Double>> stockValuation() {
        Double val = reportService.getStockValuation();
        return ResponseEntity.ok(ApiResponse.success(
                val, "Stock valuation: ₹" + val));
    }
}