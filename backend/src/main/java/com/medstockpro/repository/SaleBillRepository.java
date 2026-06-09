package com.medstockpro.repository;

import com.medstockpro.entity.SaleBill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleBillRepository extends JpaRepository<SaleBill, Long> {

    Page<SaleBill> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<SaleBill> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime from, LocalDateTime to);

    Optional<SaleBill> findByBillNumber(String billNumber);

    // Total sales amount between dates
    @Query("SELECT COALESCE(SUM(sb.totalAmount), 0) " +
           "FROM SaleBill sb " +
           "WHERE sb.createdAt BETWEEN :from AND :to")
    BigDecimal sumTotalBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // Count bills between dates
    @Query("SELECT COUNT(sb) FROM SaleBill sb " +
           "WHERE sb.createdAt BETWEEN :from AND :to")
    Long countBillsBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // Latest bill number for auto-generation
    @Query("SELECT sb.billNumber FROM SaleBill sb " +
           "ORDER BY sb.createdAt DESC")
    List<String> findLatestBillNumbers(Pageable pageable);
}