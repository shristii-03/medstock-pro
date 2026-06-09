package com.medstockpro.repository;

import com.medstockpro.entity.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    Page<Medicine> findByActiveTrue(Pageable pageable);

    Page<Medicine> findByActiveTrueAndNameContainingIgnoreCase(
            String name, Pageable pageable);

    Page<Medicine> findByActiveTrueAndCategoryIgnoreCase(
            String category, Pageable pageable);

    // Medicines where total stock across all batches <= reorder level
    @Query("""
        SELECT m FROM Medicine m
        WHERE m.active = true
        AND (
            SELECT COALESCE(SUM(sb.quantityRemaining), 0)
            FROM StockBatch sb
            WHERE sb.medicine = m
            AND sb.disposed = false
        ) <= m.reorderLevel
    """)
    List<Medicine> findLowStockMedicines();

    @Query("SELECT DISTINCT m.category FROM Medicine m " +
           "WHERE m.active = true AND m.category IS NOT NULL " +
           "ORDER BY m.category")
    List<String> findAllCategories();
}
