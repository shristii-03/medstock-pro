package com.medstockpro.controller;

import com.medstockpro.dto.ApiResponse;
import com.medstockpro.entity.AlertLog;
import com.medstockpro.repository.AlertLogRepository;
import com.medstockpro.service.AlertSchedulerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts",
     description = "Expiry and low stock alert management")
public class AlertController {

    private final AlertLogRepository    alertLogRepository;
    private final AlertSchedulerService alertService;

    @GetMapping
    @Operation(summary = "Get all alerts paginated")
    public ResponseEntity<Page<AlertLog>> getAll(
            @RequestParam(defaultValue = "0")       int     page,
            @RequestParam(defaultValue = "20")      int     size,
            @RequestParam(defaultValue = "false")   boolean unresolvedOnly) {

        Pageable pageable = PageRequest.of(page, size);
        if (unresolvedOnly) {
            return ResponseEntity.ok(
                    alertLogRepository
                            .findByResolvedFalseOrderByTriggeredAtDesc(
                                    pageable));
        }
        return ResponseEntity.ok(
                alertLogRepository
                        .findAllByOrderByTriggeredAtDesc(pageable));
    }

    @GetMapping("/count")
    @Operation(summary = "Get count of unresolved alerts")
    public ResponseEntity<ApiResponse<Long>> getUnresolvedCount() {
        Long count = alertService.getUnresolvedCount();
        return ResponseEntity.ok(ApiResponse.success(
                count, "Unresolved alert count retrieved"));
    }

    @PutMapping("/{id}/resolve")
    @Operation(summary = "Mark alert as resolved")
    public ResponseEntity<ApiResponse<Void>> resolve(
            @PathVariable Long id) {
        alertService.resolveAlert(id);
        return ResponseEntity.ok(ApiResponse.success(
                null, "Alert resolved successfully"));
    }
}