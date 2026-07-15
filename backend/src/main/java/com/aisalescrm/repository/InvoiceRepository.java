package com.aisalescrm.repository;

import com.aisalescrm.entity.Invoice;
import com.aisalescrm.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumber(String invoiceNumber);

    Optional<Invoice> findByOrderId(Long orderId);

    // ── Filtered listing ─────────────────────────────────────────────────────

    @Query("""
        SELECT i FROM Invoice i
        LEFT JOIN FETCH i.customer c
        WHERE (:status     IS NULL OR i.status  = :status)
          AND (:customerId IS NULL OR c.id      = :customerId)
          AND (
            :search = ''
            OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(c.firstName)     LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(c.lastName)      LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(c.company)       LIKE LOWER(CONCAT('%', :search, '%'))
          )
        """)
    Page<Invoice> findAllWithFilters(
            @Param("status")     InvoiceStatus status,
            @Param("customerId") Long customerId,
            @Param("search")     String search,
            Pageable pageable
    );

    // ── By Customer ──────────────────────────────────────────────────────────

    List<Invoice> findByCustomerIdOrderByIssueDateDesc(Long customerId);

    // ── Overdue detection ────────────────────────────────────────────────────

    @Query("""
        SELECT i FROM Invoice i
        WHERE i.status NOT IN ('PAID','CANCELLED')
          AND i.dueDate < :today
        """)
    List<Invoice> findOverdueInvoices(@Param("today") LocalDate today);

    // ── Revenue stats ─────────────────────────────────────────────────────────

    @Query("SELECT SUM(i.total) FROM Invoice i WHERE i.status = 'PAID'")
    BigDecimal sumPaidRevenue();

    @Query("SELECT SUM(i.total) FROM Invoice i WHERE i.status NOT IN ('PAID','CANCELLED')")
    BigDecimal sumOutstandingRevenue();

    @Query("SELECT i.status, COUNT(i), SUM(i.total) FROM Invoice i GROUP BY i.status")
    List<Object[]> getStatsByStatus();
}