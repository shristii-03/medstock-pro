package com.medstockpro.controller;

import com.medstockpro.dto.ApiResponse;
import com.medstockpro.entity.PurchaseOrder;
import com.medstockpro.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@Tag(name = "Purchase Orders",
     description = "Supplier purchase order management")
public class PurchaseOrderController {

    private final PurchaseOrderService poService;

    @GetMapping
    @Operation(summary = "Get all purchase orders")
    public ResponseEntity<Page<PurchaseOrder>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(poService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase order by ID")
    public ResponseEntity<PurchaseOrder> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(poService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Create new purchase order")
    public ResponseEntity<ApiResponse<PurchaseOrder>> create(
            @RequestParam Long   supplierId,
            @RequestParam(required = false) String notes,
            Authentication auth) {

        PurchaseOrder po = poService.create(
                supplierId, notes, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        po, "Purchase order created: " +
                            po.getPoNumber()));
    }

    @PutMapping("/{id}/receive")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Mark purchase order as received")
    public ResponseEntity<ApiResponse<PurchaseOrder>> markReceived(
            @PathVariable Long id) {
        PurchaseOrder po = poService.markReceived(id);
        return ResponseEntity.ok(ApiResponse.success(
                po, "Purchase order marked as received"));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Cancel a purchase order")
    public ResponseEntity<ApiResponse<PurchaseOrder>> cancel(
            @PathVariable Long id) {
        PurchaseOrder po = poService.cancel(id);
        return ResponseEntity.ok(ApiResponse.success(
                po, "Purchase order cancelled"));
    }
}