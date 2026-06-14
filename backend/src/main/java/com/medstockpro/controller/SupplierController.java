package com.medstockpro.controller;

import com.medstockpro.dto.ApiResponse;
import com.medstockpro.dto.SupplierRequest;
import com.medstockpro.entity.Supplier;
import com.medstockpro.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@Tag(name = "Suppliers",
     description = "Supplier management")
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @Operation(summary = "Get all suppliers")
    public ResponseEntity<Page<Supplier>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    String search) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("name").ascending());
        return ResponseEntity.ok(
                supplierService.getAll(pageable, search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by ID")
    public ResponseEntity<Supplier> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Add new supplier")
    public ResponseEntity<ApiResponse<Supplier>> create(
            @Valid @RequestBody SupplierRequest request) {
        Supplier supplier = supplierService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        supplier, "Supplier added successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Update supplier")
    public ResponseEntity<ApiResponse<Supplier>> update(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequest request) {
        Supplier supplier = supplierService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(
                supplier, "Supplier updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete supplier")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id) {
        supplierService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.success(
                null, "Supplier deleted successfully"));
    }
}