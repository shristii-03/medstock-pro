package com.medstockpro.controller;

import com.medstockpro.dto.ApiResponse;
import com.medstockpro.dto.MedicineRequest;
import com.medstockpro.entity.Medicine;
import com.medstockpro.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
@Tag(name = "Medicines",
     description = "Medicine catalog management")
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping
    @Operation(summary = "Get all medicines with search and pagination")
    public ResponseEntity<Page<Medicine>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    String search,
            @RequestParam(required = false)    String category) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("name").ascending());
        return ResponseEntity.ok(
                medicineService.getAll(pageable, search, category));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get medicine by ID")
    public ResponseEntity<Medicine> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(medicineService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Add new medicine")
    public ResponseEntity<ApiResponse<Medicine>> create(
            @Valid @RequestBody MedicineRequest request) {
        Medicine medicine = medicineService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        medicine, "Medicine added successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Update medicine details")
    public ResponseEntity<ApiResponse<Medicine>> update(
            @PathVariable Long id,
            @Valid @RequestBody MedicineRequest request) {
        Medicine medicine = medicineService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(
                medicine, "Medicine updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete a medicine")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id) {
        medicineService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.success(
                null, "Medicine deleted successfully"));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get medicines below reorder level")
    public ResponseEntity<List<Medicine>> getLowStock() {
        return ResponseEntity.ok(medicineService.getLowStock());
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all medicine categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(medicineService.getAllCategories());
    }

    @GetMapping("/{id}/stock-count")
    @Operation(summary = "Get total stock count for a medicine")
    public ResponseEntity<ApiResponse<Integer>> getStockCount(
            @PathVariable Long id) {
        Integer count = medicineService.getStockCount(id);
        return ResponseEntity.ok(ApiResponse.success(
                count, "Stock count retrieved"));
    }
}