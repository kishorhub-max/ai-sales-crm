package com.aisalescrm.repository;

import com.aisalescrm.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // ── Existence ────────────────────────────────────────────────────────────

    boolean existsByEmail(String email);

    Optional<Customer> findByEmail(String email);

    // ── Filtered Listing ─────────────────────────────────────────────────────

    @Query("""
    SELECT c FROM Customer c
    LEFT JOIN FETCH c.assignedTo u
    WHERE (:active IS NULL OR c.active = :active)
      AND (:assignedToId IS NULL OR u.id = :assignedToId)
      AND (
        :search = ''
        OR LOWER(COALESCE(c.firstName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(COALESCE(c.lastName, ''))  LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(COALESCE(c.email, ''))     LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(COALESCE(c.company, ''))   LIKE LOWER(CONCAT('%', :search, '%'))
      )
    """)
    Page<Customer> findAllWithFilters(
            @Param("active") Boolean active,
            @Param("assignedToId") Long assignedToId,
            @Param("search") String search,
            Pageable pageable
    );

    // ── Assignment ───────────────────────────────────────────────────────────

    List<Customer> findByAssignedToId(Long userId);

    long countByAssignedToId(Long userId);

    // ── Stats ────────────────────────────────────────────────────────────────

    long countByActiveTrue();

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.totalPurchaseValue > 0")
    long countCustomersWithPurchases();

    @Query("SELECT SUM(c.totalPurchaseValue) FROM Customer c WHERE c.active = true")
    Double sumTotalPurchaseValue();

    @Query("""
        SELECT c FROM Customer c
        WHERE c.active = true
        ORDER BY c.totalPurchaseValue DESC
        """)
    List<Customer> findTopCustomersByValue(Pageable pageable);

    @Query("""
        SELECT c FROM Customer c
        WHERE c.active = true
        ORDER BY c.createdAt DESC
        """)
    List<Customer> findRecentCustomers(Pageable pageable);
    List<Customer> findByActiveTrue();

    Page<Customer> findByActiveTrue(Pageable pageable);

    // ── Industry distribution ────────────────────────────────────────────────

    @Query("SELECT c.industry, COUNT(c) FROM Customer c WHERE c.industry IS NOT NULL GROUP BY c.industry")
    List<Object[]> countGroupByIndustry();

    // ── Converted from lead ──────────────────────────────────────────────────

    Optional<Customer> findByConvertedFromLeadId(Long leadId);
}