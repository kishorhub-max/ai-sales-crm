package com.aisalescrm.repository;

import com.aisalescrm.entity.Opportunity;
import com.aisalescrm.enums.OpportunityStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    // ── Filtered listing ─────────────────────────────────────────────────────

    @Query("""
    SELECT o FROM Opportunity o
    LEFT JOIN o.customer c
    LEFT JOIN o.assignedTo u
    WHERE (:stage IS NULL OR o.stage = :stage)
      AND (:customerId IS NULL OR c.id = :customerId)
      AND (:assignedToId IS NULL OR u.id = :assignedToId)
    """)
    Page<Opportunity> findAllWithFilters(
            @Param("stage") OpportunityStage stage,
            @Param("customerId") Long customerId,
            @Param("assignedToId") Long assignedToId,
            Pageable pageable
    );

    // ── By Customer ──────────────────────────────────────────────────────────

    List<Opportunity> findByCustomerId(Long customerId);

    // ── By Assigned User ─────────────────────────────────────────────────────

    List<Opportunity> findByAssignedToId(Long userId);

    // ── Pipeline Stats ───────────────────────────────────────────────────────

    @Query("SELECT o.stage, COUNT(o), SUM(o.value) FROM Opportunity o GROUP BY o.stage")
    List<Object[]> getPipelineStats();

    @Query("SELECT SUM(o.value) FROM Opportunity o WHERE o.stage = 'WON'")
    BigDecimal sumWonRevenue();

    @Query("SELECT SUM(o.value * o.probability / 100.0) FROM Opportunity o WHERE o.stage NOT IN ('WON','LOST')")
    BigDecimal sumWeightedPipeline();

    long countByStage(OpportunityStage stage);

    // ── At-Risk (close date passed, not closed) ──────────────────────────────

    @Query("""
        SELECT o FROM Opportunity o
        WHERE o.stage NOT IN ('WON','LOST')
          AND o.expectedCloseDate < :today
        """)
    List<Opportunity> findAtRiskOpportunities(@Param("today") LocalDate today);

    // ── Closing Soon ─────────────────────────────────────────────────────────

    @Query("""
        SELECT o FROM Opportunity o
        WHERE o.stage NOT IN ('WON','LOST')
          AND o.expectedCloseDate BETWEEN :today AND :horizon
        ORDER BY o.expectedCloseDate ASC
        """)
    List<Opportunity> findClosingSoon(
            @Param("today")   LocalDate today,
            @Param("horizon") LocalDate horizon
    );

    // ── Monthly Won Revenue ──────────────────────────────────────────────────

    @Query(value = """
        SELECT
            TO_CHAR(o.updated_at, 'YYYY-MM') AS month,
            SUM(o.value) AS revenue
        FROM opportunities o
        WHERE o.stage = 'WON'
          AND o.updated_at >= NOW() - INTERVAL '12 months'
        GROUP BY TO_CHAR(o.updated_at, 'YYYY-MM')
        ORDER BY month
        """, nativeQuery = true)
    List<Object[]> getMonthlyWonRevenue();
}