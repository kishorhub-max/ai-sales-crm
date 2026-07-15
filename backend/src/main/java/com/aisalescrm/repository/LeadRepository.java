package com.aisalescrm.repository;

import com.aisalescrm.entity.Lead;
import com.aisalescrm.enums.LeadSource;
import com.aisalescrm.enums.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    // ── Filtered Listing ─────────────────────────────────────────────────────

    @Query("""
    SELECT l FROM Lead l
    LEFT JOIN FETCH l.assignedTo u
    WHERE (:status IS NULL OR l.status = :status)
      AND (:source IS NULL OR l.source = :source)
      AND (:assignedToId IS NULL OR u.id = :assignedToId)
      AND (
        :search = ''
        OR LOWER(l.name) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(l.email) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(COALESCE(l.company, '')) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(COALESCE(l.phone, '')) LIKE LOWER(CONCAT('%', :search, '%'))
      )
    """)
    Page<Lead> findAllWithFilters(
            @Param("status") LeadStatus status,
            @Param("source") LeadSource source,
            @Param("assignedToId") Long assignedToId,
            @Param("search") String search,
            Pageable pageable
    );

    // ── Assignment ───────────────────────────────────────────────────────────

    List<Lead> findByAssignedToId(Long userId);

    long countByAssignedToId(Long userId);

    // ── Status Counts (for Dashboard) ────────────────────────────────────────

    long countByStatus(LeadStatus status);

    @Query("SELECT l.status, COUNT(l) FROM Lead l GROUP BY l.status")
    List<Object[]> countGroupByStatus();

    @Query("SELECT l.source, COUNT(l) FROM Lead l GROUP BY l.source")
    List<Object[]> countGroupBySource();

    // ── Recent Leads ─────────────────────────────────────────────────────────

    @Query("SELECT l FROM Lead l ORDER BY l.createdAt DESC")
    List<Lead> findRecentLeads(Pageable pageable);

    // ── Date Range ───────────────────────────────────────────────────────────

    @Query("SELECT l FROM Lead l WHERE l.createdAt BETWEEN :from AND :to")
    List<Lead> findByCreatedAtBetween(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to
    );

    // ── Existence check ──────────────────────────────────────────────────────

    boolean existsByEmail(String email);
}