package com.medstockpro.controller;

import com.medstockpro.dto.ApiResponse;
import com.medstockpro.dto.CreateSaleRequest;
import com.medstockpro.entity.SaleBill;
import com.medstockpro.service.GSTInvoiceService;
import com.medstockpro.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@Tag(name = "Sales & Billing",
     description = "Sales billing and GST invoice management")
public class SaleController {

    private final SaleService       saleService;
    private final GSTInvoiceService invoiceService;

    @PostMapping
    @Operation(summary = "Create new sale bill with FIFO stock deduction")
    public ResponseEntity<ApiResponse<SaleBill>> createSale(
            @Valid @RequestBody CreateSaleRequest request,
            Authentication auth) {

        SaleBill bill = saleService.createSale(
                request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        bill, "Sale created successfully. " +
                             "Bill No: " + bill.getBillNumber()));
    }

    @GetMapping
    @Operation(summary = "Get all sale bills paginated")
    public ResponseEntity<Page<SaleBill>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(saleService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sale bill by ID")
    public ResponseEntity<SaleBill> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(saleService.getById(id));
    }

    @GetMapping("/{id}/invoice")
    @Operation(summary = "Download GST invoice PDF for a bill")
    public ResponseEntity<byte[]> downloadInvoice(
            @PathVariable Long id) {

        SaleBill bill  = saleService.getById(id);
        byte[]   bytes = invoiceService.generateInvoice(bill);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=invoice-" +
                        bill.getBillNumber() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @GetMapping("/today/summary")
    @Operation(summary = "Get today's total sales amount and count")
    public ResponseEntity<ApiResponse<Object>> getTodaySummary() {
        BigDecimal total = saleService.getTodayTotal();
        Long       count = saleService.getTodayCount();
        return ResponseEntity.ok(ApiResponse.success(
                java.util.Map.of(
                        "totalAmount", total,
                        "billCount",   count),
                "Today's summary retrieved"));
    }
}