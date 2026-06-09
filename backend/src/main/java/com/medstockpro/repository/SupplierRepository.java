package com.medstockpro.repository;

import com.medstockpro.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Page<Supplier> findByActiveTrue(Pageable pageable);

    Page<Supplier> findByActiveTrueAndNameContainingIgnoreCase(
            String name, Pageable pageable);

    Boolean existsByGstNumber(String gstNumber);
}