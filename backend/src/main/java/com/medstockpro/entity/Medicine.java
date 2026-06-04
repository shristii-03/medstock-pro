package com.medstockpro.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200)
    private String genericName;

    @Column(length = 100)
    private String category;

    @Column(length = 20)
    private String hsnCode;

    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal gstSlab = BigDecimal.valueOf(12.00);

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String unit = "strip";

    @Column(nullable = false)
    @Builder.Default
    private Integer reorderLevel = 10;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}