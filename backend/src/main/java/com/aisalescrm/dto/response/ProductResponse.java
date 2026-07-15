package com.aisalescrm.dto.response;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long       id;
    private String     name;
    private String     sku;
    private String     category;
    private String     description;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer    stockQuantity;
    private String     unit;
    private boolean    active;

    // Computed
    private BigDecimal margin;         // price - costPrice
    private BigDecimal marginPercent;  // ((price - cost) / price) * 100

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}