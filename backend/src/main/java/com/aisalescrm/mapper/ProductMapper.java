

package com.aisalescrm.mapper;

import com.aisalescrm.dto.request.ProductRequest;
import com.aisalescrm.dto.response.ProductResponse;
import com.aisalescrm.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product p) {
        if (p == null) return null;

        BigDecimal margin        = null;
        BigDecimal marginPercent = null;

        if (p.getPrice() != null && p.getCostPrice() != null) {
            margin = p.getPrice().subtract(p.getCostPrice()).setScale(2, RoundingMode.HALF_UP);
            if (p.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                marginPercent = margin
                        .divide(p.getPrice(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
            }
        }

        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .sku(p.getSku())
                .category(p.getCategory())
                .description(p.getDescription())
                .price(p.getPrice())
                .costPrice(p.getCostPrice())
                .stockQuantity(p.getStockQuantity())
                .unit(p.getUnit())
                .active(p.isActive())
                .margin(margin)
                .marginPercent(marginPercent)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public Product toEntity(ProductRequest req) {
        return Product.builder()
                .name(req.getName())
                .sku(req.getSku())
                .category(req.getCategory())
                .description(req.getDescription())
                .price(req.getPrice())
                .costPrice(req.getCostPrice())
                .stockQuantity(req.getStockQuantity() != null ? req.getStockQuantity() : 0)
                .unit(req.getUnit())
                .active(req.isActive())
                .build();
    }

    public void updateEntity(Product p, ProductRequest req) {
        p.setName(req.getName());
        p.setSku(req.getSku());
        p.setCategory(req.getCategory());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setCostPrice(req.getCostPrice());
        if (req.getStockQuantity() != null) p.setStockQuantity(req.getStockQuantity());
        p.setUnit(req.getUnit());
        p.setActive(req.isActive());
    }
}