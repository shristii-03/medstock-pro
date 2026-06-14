package com.medstockpro.util;

import com.medstockpro.repository.SaleBillRepository;
import com.medstockpro.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BillNumberGenerator {

    private final SaleBillRepository      saleBillRepository;
    private final PurchaseOrderRepository poRepository;

    /**
     * Generates bill numbers in format: MSP-2025-0001
     * MSP = MedStock Pro
     */
    public synchronized String next() {
        String year   = String.valueOf(LocalDate.now().getYear());
        String prefix = "MSP-" + year + "-";

        List<String> latest = saleBillRepository
                .findLatestBillNumbers(PageRequest.of(0, 1));

        if (latest.isEmpty()) {
            return prefix + "0001";
        }

        String last   = latest.get(0);
        int    number = 1;

        try {
            // Extract number from MSP-2025-0042 → 42
            String[] parts = last.split("-");
            number = Integer.parseInt(parts[parts.length - 1]) + 1;
        } catch (Exception e) {
            number = 1;
        }

        return prefix + String.format("%04d", number);
    }

    /**
     * Generates PO numbers in format: PO-2025-0001
     */
    public synchronized String nextPO() {
        String year   = String.valueOf(LocalDate.now().getYear());
        String prefix = "PO-" + year + "-";

        List<String> latest = poRepository
                .findLatestPoNumbers(PageRequest.of(0, 1));

        if (latest.isEmpty()) {
            return prefix + "0001";
        }

        String last   = latest.get(0);
        int    number = 1;

        try {
            String[] parts = last.split("-");
            number = Integer.parseInt(parts[parts.length - 1]) + 1;
        } catch (Exception e) {
            number = 1;
        }

        return prefix + String.format("%04d", number);
    }
}