package com.aisalescrm.service.impl;

import com.aisalescrm.dto.request.ProductRequest;
import com.aisalescrm.dto.response.PageResponse;
import com.aisalescrm.dto.response.ProductResponse;
import com.aisalescrm.entity.Product;
import com.aisalescrm.exception.DuplicateResourceException;
import com.aisalescrm.exception.ResourceNotFoundException;
import com.aisalescrm.mapper.ProductMapper;
import com.aisalescrm.repository.ProductRepository;
import com.aisalescrm.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper     productMapper;

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        if (request.getSku() != null && !request.getSku().isBlank()
                && productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("SKU already exists: " + request.getSku());
        }
        Product saved = productRepository.save(productMapper.toEntity(request));
        log.info("Product created: id={}, name={}", saved.getId(), saved.getName());
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return productMapper.toResponse(findProduct(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAllProducts(Boolean active,
                                                        String category,
                                                        String search,
                                                        Pageable pageable) {
      
        String cleanCategory = (category != null && category.isBlank()) ? null : category;
        Page<Product> page = productRepository.findAllWithFilters(
                active, cleanCategory, pageable);
        return PageResponse.of(page.map(productMapper::toResponse));
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProduct(id);

        // SKU uniqueness if changed
        if (request.getSku() != null && !request.getSku().isBlank()
                && !request.getSku().equals(product.getSku())
                && productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("SKU already exists: " + request.getSku());
        }

        productMapper.updateEntity(product, request);
        Product updated = productRepository.save(product);
        log.info("Product updated: id={}", updated.getId());
        return productMapper.toResponse(updated);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = findProduct(id);
        product.setActive(false);
        productRepository.save(product);
        log.info("Product soft-deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts(int threshold) {
        return productRepository.findLowStockProducts(threshold)
                .stream().map(productMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getProductCountByCategory() {
        Map<String, Long> result = new LinkedHashMap<>();
        productRepository.countGroupByCategory()
                .forEach(row -> result.put(row[0].toString(), (Long) row[1]));
        return result;
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }
}