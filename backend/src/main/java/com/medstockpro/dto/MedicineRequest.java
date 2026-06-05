package com.medstockpro.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MedicineRequest {

    @NotBlank(message = "Medicine name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 200)
    private String genericName;

    @Size(max = 100)
    private String category;

    @Size(max = 20)
    private String hsnCode;

    @NotNull(message = "GST slab is required")
    @DecimalMin("0.0")
    @DecimalMax("18.0")
    private BigDecimal gstSlab;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotNull(message = "Reorder level is required")
    @Min(value = 0, message = "Reorder level cannot be negative")
    private Integer reorderLevel;
}