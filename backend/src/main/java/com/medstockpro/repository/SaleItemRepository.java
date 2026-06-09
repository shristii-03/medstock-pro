package com.medstockpro.repository;

import com.medstockpro.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    List<SaleItem> findBySaleBillId(Long saleBillId);

    // Top selling medicines
    @Query("SELECT si.medicine.name, SUM(si.quantitySold) as totalSold " +
           "FROM SaleItem si " +
           "WHERE si.saleBill.createdAt BETWEEN :from AND :to " +
           "GROUP BY si.medicine.name " +
           "ORDER BY totalSold DESC")
    List<Object[]> findTopSellingMedicines(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}