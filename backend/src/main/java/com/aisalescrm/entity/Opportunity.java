package com.aisalescrm.entity;
import com.aisalescrm.enums.OpportunityStage;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "opportunities",
        indexes = {
                @Index(name = "idx_opp_stage",    columnList = "stage"),
                @Index(name = "idx_opp_customer", columnList = "customer_id"),
                @Index(name = "idx_opp_assigned", columnList = "assigned_to_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Opportunity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deal_name", nullable = false)
    private String dealName;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal value;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OpportunityStage stage = OpportunityStage.NEW;

    /** 0–100 percent win probability */
    @Column(nullable = false)
    @Builder.Default
    private Integer probability = 10;

    @Column(name = "expected_close_date")
    private LocalDate expectedCloseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "lost_reason", columnDefinition = "TEXT")
    private String lostReason;
}