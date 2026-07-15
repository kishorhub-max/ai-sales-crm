package com.aisalescrm.service;

import com.aisalescrm.dto.request.ProductRequest;
import com.aisalescrm.dto.response.PageResponse;
import com.aisalescrm.dto.response.ProductResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse getProductById(Long id);

    PageResponse<ProductResponse> getAllProducts(
            Boolean active, String category, String search, Pageable pageable);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);

    List<String>         getAllCategories();

    List<ProductResponse> getLowStockProducts(int threshold);

    Map<String, Long>    getProductCountByCategory();
}