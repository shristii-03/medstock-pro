package com.medstockpro.service;

import com.medstockpro.dto.StockBatchRequest;
import com.medstockpro.entity.Medicine;
import com.medstockpro.entity.StockBatch;
import com.medstockpro.entity.Supplier;
import com.medstockpro.exception.ResourceNotFoundException;
import com.medstockpro.repository.MedicineRepository;
import com.medstockpro.repository.StockBatchRepository;
import com.medstockpro.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockBatchService {

    private final StockBatchRepository batchRepository;
    private final MedicineRepository   medicineRepository;
    private final SupplierRepository   supplierRepository;

    @Transactional
    public StockBatch addBatch(StockBatchRequest request) {

        Medicine medicine = medicineRepository
                .findById(request.getMedicineId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medicine not found: " + request.getMedicineId()));

        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository
                    .findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Supplier not found: " + request.getSupplierId()));
        }

        StockBatch batch = StockBatch.builder()
                .medicine(medicine)
                .supplier(supplier)
                .batchNumber(request.getBatchNumber())
                .quantityRemaining(request.getQuantity())
                .purchasePrice(request.getPurchasePrice())
                .sellingPrice(request.getSellingPrice())
                .manufacturingDate(request.getManufacturingDate())
                .expiryDate(request.getExpiryDate())
                .build();

        return batchRepository.save(batch);
    }

    public List<StockBatch> getBatchesByMedicine(Long medicineId) {
        return batchRepository
                .findByMedicineIdAndDisposedFalseOrderByExpiryDateAsc(
                        medicineId);
    }

    public List<StockBatch> getExpiringBatches(int days) {
        LocalDate today    = LocalDate.now();
        LocalDate deadline = today.plusDays(days);
        return batchRepository.findExpiringBefore(today, deadline);
    }

    public List<StockBatch> getExpiredBatches() {
        return batchRepository.findAlreadyExpired(LocalDate.now());
    }

    @Transactional
    public StockBatch disposeBatch(Long batchId) {
        StockBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Batch not found: " + batchId));
        batch.setDisposed(true);
        batch.setQuantityRemaining(0);
        return batchRepository.save(batch);
    }

    public Double getStockValuation() {
        return batchRepository.getTotalStockValuation();
    }
}