package com.medstockpro.service;

import com.medstockpro.dto.SaleItemRequest;
import com.medstockpro.entity.Medicine;
import com.medstockpro.entity.SaleItem;
import com.medstockpro.entity.StockBatch;
import com.medstockpro.exception.InsufficientStockException;
import com.medstockpro.repository.MedicineRepository;
import com.medstockpro.repository.StockBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FIFOStockService {

    private final StockBatchRepository batchRepository;
    private final MedicineRepository   medicineRepository;

    /**
     * FIFO Stock Deduction Logic:
     *
     * 1. Fetch all active batches for the medicine sorted by expiry ASC
     * 2. Deduct from the oldest batch first (soonest to expire)
     * 3. If batch exhausted, move to next batch
     * 4. Each batch consumed creates a separate SaleItem (audit trail)
     * 5. If total available stock < requested quantity → throw exception
     *
     * Why FIFO? Pharma regulations require oldest stock to be sold first
     * to minimize expiry wastage. This is also how 1mg and Pharmeasy
     * handle batch management internally.
     */
    @Transactional
    public List<SaleItem> deductStockFIFO(SaleItemRequest request) {

        Medicine medicine = medicineRepository
                .findById(request.getMedicineId())
                .orElseThrow(() -> new RuntimeException(
                        "Medicine not found: " + request.getMedicineId()));

        // Get all active batches sorted oldest expiry first
        List<StockBatch> batches = batchRepository
                .findByMedicineAndDisposedFalseAndQuantityRemainingGreaterThanOrderByExpiryDateAsc(
                        medicine, 0);

        // Check total available stock
        int totalAvailable = batches.stream()
                .mapToInt(StockBatch::getQuantityRemaining)
                .sum();

        if (totalAvailable < request.getQuantity()) {
            throw new InsufficientStockException(
                    "Insufficient stock for '" + medicine.getName() +
                    "'. Requested: " + request.getQuantity() +
                    ", Available: " + totalAvailable);
        }

        List<SaleItem> saleItems = new ArrayList<>();
        int remaining = request.getQuantity();

        for (StockBatch batch : batches) {
            if (remaining <= 0) break;

            // How much to deduct from this batch
            int deduct = Math.min(batch.getQuantityRemaining(), remaining);

            // Update batch quantity
            batch.setQuantityRemaining(
                    batch.getQuantityRemaining() - deduct);
            batchRepository.save(batch);

            // Calculate GST for this line item
            BigDecimal gstRate = medicine.getGstSlab()
                    .divide(BigDecimal.valueOf(100));
            BigDecimal baseAmount = batch.getSellingPrice()
                    .multiply(BigDecimal.valueOf(deduct));
            BigDecimal gstAmount = baseAmount
                    .multiply(gstRate)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = baseAmount.add(gstAmount);

            // Create sale item for this batch consumption
            SaleItem item = SaleItem.builder()
                    .stockBatch(batch)
                    .medicine(medicine)
                    .quantitySold(deduct)
                    .unitPrice(batch.getSellingPrice())
                    .gstSlab(medicine.getGstSlab())
                    .gstAmount(gstAmount)
                    .lineTotal(lineTotal)
                    .build();

            saleItems.add(item);
            remaining -= deduct;

            log.debug("Deducted {} units from batch {} (expiry: {})",
                    deduct, batch.getBatchNumber(), batch.getExpiryDate());
        }

        return saleItems;
    }
}