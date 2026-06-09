package com.medstockpro.service;

import com.medstockpro.entity.AlertLog;
import com.medstockpro.entity.Medicine;
import com.medstockpro.entity.StockBatch;
import com.medstockpro.repository.AlertLogRepository;
import com.medstockpro.repository.MedicineRepository;
import com.medstockpro.repository.StockBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertSchedulerService {

    private final StockBatchRepository batchRepository;
    private final MedicineRepository   medicineRepository;
    private final AlertLogRepository   alertLogRepository;
    private final JavaMailSender       mailSender;

    @Value("${app.alert.expiry-warning-days}")
    private int warningDays;

    @Value("${app.alert.expiry-critical-days}")
    private int criticalDays;

    @Value("${app.admin-email}")
    private String adminEmail;

    @Value("${app.name}")
    private String appName;

    // Runs every day at 8:00 AM
    @Scheduled(cron = "0 0 8 * * *")
    public void runDailyAlerts() {
        log.info("Running daily MedStock Pro alerts...");
        checkExpiryAlerts();
        checkLowStockAlerts();
        log.info("Daily alerts completed.");
    }

    private void checkExpiryAlerts() {
        LocalDate today    = LocalDate.now();
        LocalDate deadline = today.plusDays(warningDays);

        List<StockBatch> expiring =
                batchRepository.findExpiringBefore(today, deadline);

        for (StockBatch batch : expiring) {

            AlertLog.AlertType type =
                    batch.getExpiryDate()
                         .isBefore(today.plusDays(criticalDays))
                    ? AlertLog.AlertType.EXPIRY_CRITICAL
                    : AlertLog.AlertType.EXPIRY_WARNING;

            // Skip if unresolved alert already exists
            Boolean exists = alertLogRepository.existsUnresolvedAlert(
                    batch.getMedicine().getId(), type);
            if (Boolean.TRUE.equals(exists)) continue;

            String message = String.format(
                    "Batch %s of '%s' expires on %s. " +
                    "Remaining quantity: %d %s.",
                    batch.getBatchNumber(),
                    batch.getMedicine().getName(),
                    batch.getExpiryDate(),
                    batch.getQuantityRemaining(),
                    batch.getMedicine().getUnit());

            saveAndEmail(batch.getMedicine(), batch, type, message);
        }
    }

    private void checkLowStockAlerts() {
        List<Medicine> lowStock =
                medicineRepository.findLowStockMedicines();

        for (Medicine medicine : lowStock) {

            Boolean exists = alertLogRepository.existsUnresolvedAlert(
                    medicine.getId(), AlertLog.AlertType.LOW_STOCK);
            if (Boolean.TRUE.equals(exists)) continue;

            int total = batchRepository
                    .getTotalStockByMedicineId(medicine.getId());

            String message = String.format(
                    "'%s' is running low. Current stock: %d %s. " +
                    "Reorder level: %d.",
                    medicine.getName(),
                    total,
                    medicine.getUnit(),
                    medicine.getReorderLevel());

            saveAndEmail(medicine, null,
                    AlertLog.AlertType.LOW_STOCK, message);
        }
    }

    private void saveAndEmail(Medicine medicine,
                               StockBatch batch,
                               AlertLog.AlertType type,
                               String message) {
        // Save alert to DB
        AlertLog alert = AlertLog.builder()
                .medicine(medicine)
                .stockBatch(batch)
                .alertType(type)
                .message(message)
                .build();
        alertLogRepository.save(alert);

        // Send email notification
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(adminEmail);
            mail.setSubject("[" + appName + "] " +
                    type.name().replace("_", " ") +
                    " — " + medicine.getName());
            mail.setText(message +
                    "\n\nThis is an automated alert from " + appName + ".");
            mailSender.send(mail);
            log.info("Alert email sent for: {}", medicine.getName());
        } catch (Exception e) {
            log.error("Failed to send alert email: {}", e.getMessage());
        }
    }

    public void resolveAlert(Long alertId) {
        AlertLog alert = alertLogRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException(
                        "Alert not found: " + alertId));
        alert.setResolved(true);
        alert.setResolvedAt(java.time.LocalDateTime.now());
        alertLogRepository.save(alert);
    }

    public Long getUnresolvedCount() {
        return alertLogRepository.countByResolvedFalse();
    }
}