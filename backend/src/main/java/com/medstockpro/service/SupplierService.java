package com.medstockpro.service;

import com.medstockpro.dto.SupplierRequest;
import com.medstockpro.entity.Supplier;
import com.medstockpro.exception.ResourceNotFoundException;
import com.medstockpro.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public Page<Supplier> getAll(Pageable pageable, String search) {
        if (search != null && !search.isBlank()) {
            return supplierRepository
                    .findByActiveTrueAndNameContainingIgnoreCase(
                            search, pageable);
        }
        return supplierRepository.findByActiveTrue(pageable);
    }

    public Supplier getById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier not found with id: " + id));
    }

    @Transactional
    public Supplier create(SupplierRequest request) {
        Supplier supplier = Supplier.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .gstNumber(request.getGstNumber())
                .address(request.getAddress())
                .build();
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier update(Long id, SupplierRequest request) {
        Supplier supplier = getById(id);
        supplier.setName(request.getName());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setGstNumber(request.getGstNumber());
        supplier.setAddress(request.getAddress());
        return supplierRepository.save(supplier);
    }

    @Transactional
    public void softDelete(Long id) {
        Supplier supplier = getById(id);
        supplier.setActive(false);
        supplierRepository.save(supplier);
    }
}