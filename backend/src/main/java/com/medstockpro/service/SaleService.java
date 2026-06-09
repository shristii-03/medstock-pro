package com.medstockpro.service;

import com.medstockpro.dto.CreateSaleRequest;
import com.medstockpro.entity.SaleBill;
import com.medstockpro.entity.SaleItem;
import com.medstockpro.entity.User;
import com.medstockpro.exception.ResourceNotFoundException;
import com.medstockpro.repository.SaleBillRepository;
import com.medstockpro.repository.UserRepository;
import com.medstockpro.util.BillNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleBillRepository  saleBillRepository;
    private final FIFOStockService    fifoStockService;
    private final UserRepository      userRepository;
    private final BillNumberGenerator billNumberGenerator;

    @Transactional
    public SaleBill createSale(CreateSaleRequest request,
                                String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + userEmail));

        List<SaleItem> allItems  = new ArrayList<>();
        BigDecimal subtotal      = BigDecimal.ZERO;
        BigDecimal totalGst      = BigDecimal.ZERO;

        // Process each item through FIFO deduction
        for (var itemRequest : request.getItems()) {
            List<SaleItem> items =
                    fifoStockService.deductStockFIFO(itemRequest);

            for (SaleItem item : items) {
                BigDecimal base = item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantitySold()));
                subtotal = subtotal.add(base);
                totalGst = totalGst.add(item.getGstAmount());
            }

            allItems.addAll(items);
        }

        // Build the bill
        SaleBill bill = SaleBill.builder()
                .billNumber(billNumberGenerator.next())
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .subtotal(subtotal)
                .gstAmount(totalGst)
                .totalAmount(subtotal.add(totalGst))
                .createdBy(user)
                .build();

        // Link items to bill
        allItems.forEach(item -> item.setSaleBill(bill));
        bill.setItems(allItems);

        SaleBill saved = saleBillRepository.save(bill);
        log.info("Created bill {} for total ₹{}",
                saved.getBillNumber(), saved.getTotalAmount());

        return saved;
    }

    public Page<SaleBill> getAll(Pageable pageable) {
        return saleBillRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public SaleBill getById(Long id) {
        return saleBillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bill not found with id: " + id));
    }

    public BigDecimal getTodayTotal() {
        LocalDateTime startOfDay = LocalDateTime.now()
                .withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay   = LocalDateTime.now()
                .withHour(23).withMinute(59).withSecond(59);
        return saleBillRepository.sumTotalBetween(startOfDay, endOfDay);
    }

    public Long getTodayCount() {
        LocalDateTime startOfDay = LocalDateTime.now()
                .withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay   = LocalDateTime.now()
                .withHour(23).withMinute(59).withSecond(59);
        return saleBillRepository.countBillsBetween(startOfDay, endOfDay);
    }
}