package com.medstockpro.service;

import com.medstockpro.dto.SaleItemRequest;
import com.medstockpro.entity.*;
import com.medstockpro.exception.InsufficientStockException;
import com.medstockpro.repository.MedicineRepository;
import com.medstockpro.repository.StockBatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FIFOStockServiceTest {

    @Mock
    private StockBatchRepository batchRepository;

    @Mock
    private MedicineRepository medicineRepository;

    @InjectMocks
    private FIFOStockService fifoStockService;

    private Medicine medicine;
    private StockBatch batch1;
    private StockBatch batch2;

    @BeforeEach
    void setup() {
        medicine = Medicine.builder()
                .id(1L)
                .name("Paracetamol 500mg")
                .gstSlab(BigDecimal.valueOf(12))
                .unit("strip")
                .build();

        batch1 = StockBatch.builder()
                .id(1L)
                .medicine(medicine)
                .batchNumber("PCM-001")
                .quantityRemaining(5)
                .sellingPrice(BigDecimal.valueOf(10))
                .expiryDate(LocalDate.now().plusMonths(2))
                .build();

        batch2 = StockBatch.builder()
                .id(2L)
                .medicine(medicine)
                .batchNumber("PCM-002")
                .quantityRemaining(10)
                .sellingPrice(BigDecimal.valueOf(10))
                .expiryDate(LocalDate.now().plusMonths(6))
                .build();
    }

    @Test
    @DisplayName("Should deduct from oldest batch first (FIFO)")
    void shouldDeductFromOldestBatchFirst() {
        when(medicineRepository.findById(1L))
                .thenReturn(Optional.of(medicine));
        when(batchRepository
                .findByMedicineAndDisposedFalseAndQuantityRemainingGreaterThanOrderByExpiryDateAsc(
                        medicine, 0))
                .thenReturn(List.of(batch1, batch2));

        SaleItemRequest request = new SaleItemRequest(1L, 7);

        List<SaleItem> items = fifoStockService.deductStockFIFO(request);

        assertThat(items).hasSize(2);
        assertThat(batch1.getQuantityRemaining()).isEqualTo(0);
        assertThat(batch2.getQuantityRemaining()).isEqualTo(8);
    }

    @Test
    @DisplayName("Should deduct fully from single batch when sufficient")
    void shouldDeductFromSingleBatch() {
        when(medicineRepository.findById(1L))
                .thenReturn(Optional.of(medicine));
        when(batchRepository
                .findByMedicineAndDisposedFalseAndQuantityRemainingGreaterThanOrderByExpiryDateAsc(
                        medicine, 0))
                .thenReturn(List.of(batch1, batch2));

        SaleItemRequest request = new SaleItemRequest(1L, 3);

        List<SaleItem> items = fifoStockService.deductStockFIFO(request);

        assertThat(items).hasSize(1);
        assertThat(batch1.getQuantityRemaining()).isEqualTo(2);
        assertThat(batch2.getQuantityRemaining()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when stock too low")
    void shouldThrowWhenInsufficientStock() {
        when(medicineRepository.findById(1L))
                .thenReturn(Optional.of(medicine));
        when(batchRepository
                .findByMedicineAndDisposedFalseAndQuantityRemainingGreaterThanOrderByExpiryDateAsc(
                        medicine, 0))
                .thenReturn(List.of(batch1));

        SaleItemRequest request = new SaleItemRequest(1L, 20);

        assertThatThrownBy(() -> fifoStockService.deductStockFIFO(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("Should calculate GST correctly for deducted items")
    void shouldCalculateGSTCorrectly() {
        when(medicineRepository.findById(1L))
                .thenReturn(Optional.of(medicine));
        when(batchRepository
                .findByMedicineAndDisposedFalseAndQuantityRemainingGreaterThanOrderByExpiryDateAsc(
                        medicine, 0))
                .thenReturn(List.of(batch1));

        SaleItemRequest request = new SaleItemRequest(1L, 2);

        List<SaleItem> items = fifoStockService.deductStockFIFO(request);

        SaleItem item = items.get(0);
        assertThat(item.getGstAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(2.40));
        assertThat(item.getLineTotal())
                .isEqualByComparingTo(BigDecimal.valueOf(22.40));
    }
}