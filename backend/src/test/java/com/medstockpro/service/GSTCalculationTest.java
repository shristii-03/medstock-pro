package com.medstockpro.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

class GSTCalculationTest {

    @Test
    @DisplayName("Should calculate 12% GST correctly")
    void shouldCalculate12PercentGST() {
        BigDecimal baseAmount = BigDecimal.valueOf(100);
        BigDecimal gstSlab    = BigDecimal.valueOf(12);

        BigDecimal gstRate   = gstSlab.divide(BigDecimal.valueOf(100));
        BigDecimal gstAmount = baseAmount.multiply(gstRate)
                .setScale(2, RoundingMode.HALF_UP);

        assertThat(gstAmount).isEqualByComparingTo(BigDecimal.valueOf(12.00));
    }

    @Test
    @DisplayName("Should calculate 0% GST for exempted items")
    void shouldCalculateZeroGST() {
        BigDecimal baseAmount = BigDecimal.valueOf(500);
        BigDecimal gstSlab    = BigDecimal.valueOf(0);

        BigDecimal gstRate   = gstSlab.divide(BigDecimal.valueOf(100));
        BigDecimal gstAmount = baseAmount.multiply(gstRate)
                .setScale(2, RoundingMode.HALF_UP);

        assertThat(gstAmount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should split GST equally into CGST and SGST")
    void shouldSplitGSTIntoCGSTAndSGST() {
        BigDecimal totalGst = BigDecimal.valueOf(24.00);

        BigDecimal cgst = totalGst.divide(
                BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        BigDecimal sgst = totalGst.divide(
                BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

        assertThat(cgst).isEqualByComparingTo(BigDecimal.valueOf(12.00));
        assertThat(sgst).isEqualByComparingTo(BigDecimal.valueOf(12.00));
        assertThat(cgst.add(sgst)).isEqualByComparingTo(totalGst);
    }

    @Test
    @DisplayName("Should calculate 5% GST correctly")
    void shouldCalculate5PercentGST() {
        BigDecimal baseAmount = BigDecimal.valueOf(200);
        BigDecimal gstSlab    = BigDecimal.valueOf(5);

        BigDecimal gstRate   = gstSlab.divide(BigDecimal.valueOf(100));
        BigDecimal gstAmount = baseAmount.multiply(gstRate)
                .setScale(2, RoundingMode.HALF_UP);

        assertThat(gstAmount).isEqualByComparingTo(BigDecimal.valueOf(10.00));
    }

    @Test
    @DisplayName("Should calculate 18% GST correctly")
    void shouldCalculate18PercentGST() {
        BigDecimal baseAmount = BigDecimal.valueOf(150);
        BigDecimal gstSlab    = BigDecimal.valueOf(18);

        BigDecimal gstRate   = gstSlab.divide(BigDecimal.valueOf(100));
        BigDecimal gstAmount = baseAmount.multiply(gstRate)
                .setScale(2, RoundingMode.HALF_UP);

        assertThat(gstAmount).isEqualByComparingTo(BigDecimal.valueOf(27.00));
    }
}