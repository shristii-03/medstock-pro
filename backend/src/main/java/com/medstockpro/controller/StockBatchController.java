package com.medstockpro.controller;

import com.medstockpro.dto.ApiResponse;
import com.medstockpro.dto.StockBatchRequest;
import com.medstockpro.entity.StockBatch;
import com.medstockpro.service.StockBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
@Tag(name = "Stock Batches",
     description = "Inventory batch management")
public class StockBatchController {

    private final StockBatchService batchService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Add new stock batch")
    public ResponseEntity<ApiResponse<StockBatch>> addBatch(
            @Valid @RequestBody StockBatchRequest request) {
        StockBatch batch = batchService.addBatch(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        batch, "Stock batch added successfully"));
    }

    @GetMapping("/medicine/{medicineId}")
    @Operation(summary = "Get all batches for a medicine")
    public ResponseEntity<List<StockBatch>> getByMedicine(
            @PathVariable Long medicineId) {
        return ResponseEntity.ok(
                batchService.getBatchesByMedicine(medicineId));
    }

    @GetMapping("/expiring")
    @Operation(summary = "Get batches expiring within N days")
    public ResponseEntity<List<StockBatch>> getExpiring(
            @RequestParam(defaultValue = "90") int days) {
        return ResponseEntity.ok(
                batchService.getExpiringBatches(days));
    }

    @GetMapping("/expired")
    @Operation(summary = "Get already expired batches still in stock")
    public ResponseEntity<List<StockBatch>> getExpired() {
        return ResponseEntity.ok(batchService.getExpiredBatches());
    }

    @PutMapping("/{id}/dispose")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Mark batch as disposed")
    public ResponseEntity<ApiResponse<StockBatch>> dispose(
            @PathVariable Long id) {
        StockBatch batch = batchService.disposeBatch(id);
        return ResponseEntity.ok(ApiResponse.success(
                batch, "Batch disposed successfully"));
    }

    @GetMapping("/valuation")
    @Operation(summary = "Get total stock valuation")
    public ResponseEntity<ApiResponse<Double>> getValuation() {
        Double val = batchService.getStockValuation();
        return ResponseEntity.ok(ApiResponse.success(
                val, "Stock valuation retrieved"));
    }
}
