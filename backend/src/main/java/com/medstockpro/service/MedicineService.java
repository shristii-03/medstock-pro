package com.medstockpro.service;

import com.medstockpro.dto.MedicineRequest;
import com.medstockpro.entity.Medicine;
import com.medstockpro.exception.ResourceNotFoundException;
import com.medstockpro.repository.MedicineRepository;
import com.medstockpro.repository.StockBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository  medicineRepository;
    private final StockBatchRepository batchRepository;

    public Page<Medicine> getAll(Pageable pageable, String search,
                                  String category) {
        if (search != null && !search.isBlank()) {
            return medicineRepository
                    .findByActiveTrueAndNameContainingIgnoreCase(
                            search, pageable);
        }
        if (category != null && !category.isBlank()) {
            return medicineRepository
                    .findByActiveTrueAndCategoryIgnoreCase(
                            category, pageable);
        }
        return medicineRepository.findByActiveTrue(pageable);
    }

    public Medicine getById(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medicine not found with id: " + id));
    }

    @Transactional
    public Medicine create(MedicineRequest request) {
        Medicine medicine = Medicine.builder()
                .name(request.getName())
                .genericName(request.getGenericName())
                .category(request.getCategory())
                .hsnCode(request.getHsnCode())
                .gstSlab(request.getGstSlab())
                .unit(request.getUnit())
                .reorderLevel(request.getReorderLevel())
                .build();
        return medicineRepository.save(medicine);
    }

    @Transactional
    public Medicine update(Long id, MedicineRequest request) {
        Medicine medicine = getById(id);
        medicine.setName(request.getName());
        medicine.setGenericName(request.getGenericName());
        medicine.setCategory(request.getCategory());
        medicine.setHsnCode(request.getHsnCode());
        medicine.setGstSlab(request.getGstSlab());
        medicine.setUnit(request.getUnit());
        medicine.setReorderLevel(request.getReorderLevel());
        return medicineRepository.save(medicine);
    }

    @Transactional
    public void softDelete(Long id) {
        Medicine medicine = getById(id);
        medicine.setActive(false);
        medicineRepository.save(medicine);
    }

    public List<Medicine> getLowStock() {
        return medicineRepository.findLowStockMedicines();
    }

    public Integer getStockCount(Long medicineId) {
        return batchRepository.getTotalStockByMedicineId(medicineId);
    }

    public List<String> getAllCategories() {
        return medicineRepository.findAllCategories();
    }
}