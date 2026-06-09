package com.medstockpro.service;

import com.medstockpro.entity.PurchaseOrder;
import com.medstockpro.entity.Supplier;
import com.medstockpro.entity.User;
import com.medstockpro.exception.ResourceNotFoundException;
import com.medstockpro.repository.PurchaseOrderRepository;
import com.medstockpro.repository.SupplierRepository;
import com.medstockpro.repository.UserRepository;
import com.medstockpro.util.BillNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository poRepository;
    private final SupplierRepository      supplierRepository;
    private final UserRepository          userRepository;
    private final BillNumberGenerator     numberGenerator;

    public Page<PurchaseOrder> getAll(Pageable pageable) {
        return poRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public PurchaseOrder getById(Long id) {
        return poRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase order not found: " + id));
    }

    @Transactional
    public PurchaseOrder create(Long supplierId,
                                 String notes,
                                 String userEmail) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier not found: " + supplierId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + userEmail));

        PurchaseOrder po = PurchaseOrder.builder()
                .poNumber(numberGenerator.nextPO())
                .supplier(supplier)
                .notes(notes)
                .createdBy(user)
                .build();

        return poRepository.save(po);
    }

    @Transactional
    public PurchaseOrder markReceived(Long id) {
        PurchaseOrder po = getById(id);
        po.setStatus(PurchaseOrder.Status.RECEIVED);
        po.setReceivedAt(LocalDateTime.now());
        return poRepository.save(po);
    }

    @Transactional
    public PurchaseOrder cancel(Long id) {
        PurchaseOrder po = getById(id);
        if (po.getStatus() == PurchaseOrder.Status.RECEIVED) {
            throw new RuntimeException(
                    "Cannot cancel an already received order");
        }
        po.setStatus(PurchaseOrder.Status.CANCELLED);
        return poRepository.save(po);
    }
}