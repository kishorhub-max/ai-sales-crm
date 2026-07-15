package com.aisalescrm.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String name;

    @Size(max = 50, message = "SKU must not exceed 50 characters")
    private String sku;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @Size(max = 3000, message = "Description must not exceed 3000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price format invalid")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Cost price cannot be negative")
    private BigDecimal costPrice;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    private String unit;

    private boolean active = true;
}