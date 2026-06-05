package com.medstockpro.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class CreateSaleRequest {

    private String customerName;

    @Pattern(
        regexp  = "^[0-9]{10}$",
        message = "Phone must be 10 digits"
    )
    private String customerPhone;

    @NotEmpty(message = "Sale must have at least one item")
    @Valid
    private List<SaleItemRequest> items;
}
