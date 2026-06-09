package com.medstockpro.repository;

import com.medstockpro.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderRepository
        extends JpaRepository<PurchaseOrder, Long> {

    Page<PurchaseOrder> findBySupplierId(Long supplierId, Pageable pageable);

    Page<PurchaseOrder> findByStatus(
            PurchaseOrder.Status status, Pageable pageable);

    Page<PurchaseOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Latest PO number for auto-generation
    @Query("SELECT po.poNumber FROM PurchaseOrder po " +
           "ORDER BY po.createdAt DESC")
    List<String> findLatestPoNumbers(Pageable pageable);
}