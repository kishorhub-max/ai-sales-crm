package com.aisalescrm.repository;

import com.aisalescrm.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    Optional<Product> findBySku(String sku);

    // ── Filtered Listing ─────────────────────────────────────────────────────

    @Query("""
        SELECT p FROM Product p
        WHERE (:active   IS NULL OR p.active   = :active)
          AND (:category IS NULL OR p.category = :category)
          
        """)
    Page<Product> findAllWithFilters(
            @Param("active")   Boolean active,
            @Param("category") String category,

            Pageable pageable
    );

    // ── Category list ────────────────────────────────────────────────────────

    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL ORDER BY p.category")
    List<String> findAllCategories();

    // ── Stats ────────────────────────────────────────────────────────────────

    long countByActiveTrue();

    @Query("SELECT p.category, COUNT(p) FROM Product p WHERE p.active = true GROUP BY p.category")
    List<Object[]> countGroupByCategory();

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stockQuantity <= :threshold")
    List<Product> findLowStockProducts(@Param("threshold") int threshold);
}