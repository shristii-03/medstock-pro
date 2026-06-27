package com.medstockpro.service;

import com.medstockpro.dto.CreateSaleRequest;
import com.medstockpro.dto.SaleItemRequest;
import com.medstockpro.entity.*;
import com.medstockpro.repository.SaleBillRepository;
import com.medstockpro.repository.UserRepository;
import com.medstockpro.util.BillNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private SaleBillRepository saleBillRepository;

    @Mock
    private FIFOStockService fifoStockService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BillNumberGenerator billNumberGenerator;

    @InjectMocks
    private SaleService saleService;

    private User user;
    private Medicine medicine;
    private StockBatch batch;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(1L)
                .name("Billing Staff")
                .email("billing@medstockpro.com")
                .role(User.Role.BILLING)
                .build();

        medicine = Medicine.builder()
                .id(1L)
                .name("Paracetamol 500mg")
                .gstSlab(BigDecimal.valueOf(12))
                .build();

        batch = StockBatch.builder()
                .id(1L)
                .medicine(medicine)
                .batchNumber("PCM-001")
                .quantityRemaining(10)
                .sellingPrice(BigDecimal.valueOf(10))
                .build();
    }

    @Test
    @DisplayName("Should create sale bill with correct totals")
    void shouldCreateSaleBillWithCorrectTotals() {
        when(userRepository.findByEmail("billing@medstockpro.com"))
                .thenReturn(Optional.of(user));
        when(billNumberGenerator.next()).thenReturn("MSP-2026-0001");

        SaleItem item = SaleItem.builder()
                .stockBatch(batch)
                .medicine(medicine)
                .quantitySold(2)
                .unitPrice(BigDecimal.valueOf(10))
                .gstSlab(BigDecimal.valueOf(12))
                .gstAmount(BigDecimal.valueOf(2.40))
                .lineTotal(BigDecimal.valueOf(22.40))
                .build();

        when(fifoStockService.deductStockFIFO(any(SaleItemRequest.class)))
                .thenReturn(List.of(item));

        when(saleBillRepository.save(any(SaleBill.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateSaleRequest request = new CreateSaleRequest();
        request.setCustomerName("Test Customer");
        request.setItems(List.of(new SaleItemRequest(1L, 2)));

        SaleBill result = saleService.createSale(
                request, "billing@medstockpro.com");

        assertThat(result.getBillNumber()).isEqualTo("MSP-2026-0001");
        assertThat(result.getSubtotal())
                .isEqualByComparingTo(BigDecimal.valueOf(20));
        assertThat(result.getGstAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(2.40));
        assertThat(result.getTotalAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(22.40));
    }
}