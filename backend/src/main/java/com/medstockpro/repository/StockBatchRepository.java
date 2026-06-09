package com.medstockpro.repository;

import com.medstockpro.entity.Medicine;
import com.medstockpro.entity.StockBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StockBatchRepository extends JpaRepository<StockBatch, Long> {

    // FIFO: active batches sorted by expiry date ASC (oldest first)
    List<StockBatch> findByMedicineAndDisposedFalseAndQuantityRemainingGreaterThanOrderByExpiryDateAsc(
            Medicine medicine, int minQty);

    // All active batches for a medicine
    List<StockBatch> findByMedicineIdAndDisposedFalseOrderByExpiryDateAsc(
            Long medicineId);

    // Batches expiring between today and deadline
    @Query("SELECT sb FROM StockBatch sb " +
           "WHERE sb.disposed = false " +
           "AND sb.quantityRemaining > 0 " +
           "AND sb.expiryDate BETWEEN :today AND :deadline " +
           "ORDER BY sb.expiryDate ASC")
    List<StockBatch> findExpiringBefore(
            @Param("today") LocalDate today,
            @Param("deadline") LocalDate deadline);

    // Total stock for a medicine across all active batches
    @Query("SELECT COALESCE(SUM(sb.quantityRemaining), 0) " +
           "FROM StockBatch sb " +
           "WHERE sb.medicine.id = :medicineId " +
           "AND sb.disposed = false")
    Integer getTotalStockByMedicineId(@Param("medicineId") Long medicineId);

    // Stock valuation: sum of (quantity * selling price)
    @Query("SELECT COALESCE(SUM(sb.quantityRemaining * sb.sellingPrice), 0) " +
           "FROM StockBatch sb WHERE sb.disposed = false")
    Double getTotalStockValuation();

    // Already expired batches still in stock
    @Query("SELECT sb FROM StockBatch sb " +
           "WHERE sb.disposed = false " +
           "AND sb.quantityRemaining > 0 " +
           "AND sb.expiryDate < :today")
    List<StockBatch> findAlreadyExpired(@Param("today") LocalDate today);
}