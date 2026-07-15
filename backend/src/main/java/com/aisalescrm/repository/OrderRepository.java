package com.aisalescrm.repository;

import com.aisalescrm.entity.Order;
import com.aisalescrm.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    // ── Filtered listing ─────────────────────────────────────────────────────

    @Query("""
    SELECT o FROM Order o
    LEFT JOIN o.customer c
    WHERE (:status IS NULL OR o.status = :status)
      AND (:customerId IS NULL OR c.id = :customerId)
    
    """)
    Page<Order> findAllWithFilters(
            @Param("status") OrderStatus status,
            @Param("customerId") Long customerId,
            Pageable pageable
    );

    // ── By Customer ──────────────────────────────────────────────────────────

    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    // ── Revenue Stats ────────────────────────────────────────────────────────

    @Query("SELECT SUM(o.total) FROM Order o WHERE o.status NOT IN ('CANCELLED','REFUNDED')")
    BigDecimal sumTotalRevenue();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") OrderStatus status);

    @Query("""
        SELECT o.status, COUNT(o), SUM(o.total)
        FROM Order o
        GROUP BY o.status
        """)
    List<Object[]> getOrderStatsByStatus();

    // ── Monthly order revenue ─────────────────────────────────────────────────

    @Query(value = """
        SELECT
            TO_CHAR(o.created_at, 'YYYY-MM') AS month,
            COUNT(o.id)                        AS order_count,
            SUM(o.total)                       AS revenue
        FROM orders o
        WHERE o.status NOT IN ('CANCELLED','REFUNDED')
          AND o.created_at >= NOW() - INTERVAL '12 months'
        GROUP BY TO_CHAR(o.created_at, 'YYYY-MM')
        ORDER BY month
        """, nativeQuery = true)
    List<Object[]> getMonthlyRevenue();

    // ── Date range ────────────────────────────────────────────────────────────

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :from AND :to")
    List<Order> findByDateRange(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to
    );
}