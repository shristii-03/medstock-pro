package com.medstockpro.repository;

import com.medstockpro.entity.AlertLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertLogRepository extends JpaRepository<AlertLog, Long> {

    Page<AlertLog> findByResolvedFalseOrderByTriggeredAtDesc(
            Pageable pageable);

    Page<AlertLog> findAllByOrderByTriggeredAtDesc(Pageable pageable);

    List<AlertLog> findByMedicineIdAndResolvedFalse(Long medicineId);

    Long countByResolvedFalse();

    // Check if unresolved alert already exists for this medicine+type
    @Query("SELECT COUNT(a) > 0 FROM AlertLog a " +
           "WHERE a.medicine.id = :medicineId " +
           "AND a.alertType = :alertType " +
           "AND a.resolved = false")
    Boolean existsUnresolvedAlert(
            Long medicineId,
            AlertLog.AlertType alertType);
}