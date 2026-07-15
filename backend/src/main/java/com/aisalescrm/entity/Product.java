package com.aisalescrm.entity;

import com.aisalescrm.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_product_category", columnList = "category"),
                @Index(name = "idx_product_sku",      columnList = "sku", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String sku;

    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "stock_quantity")
    @Builder.Default
    private Integer stockQuantity = 0;

    private String unit;   // e.g. "each", "kg", "license"

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}