package com.aisalescrm.entity;

import com.aisalescrm.enums.LeadSource;
import com.aisalescrm.enums.LeadStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "leads",
        indexes = {
                @Index(name = "idx_lead_email",    columnList = "email"),
                @Index(name = "idx_lead_status",   columnList = "status"),
                @Index(name = "idx_lead_assigned", columnList = "assigned_to_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String phone;
    private String company;

    @Column(name = "job_title")
    private String jobTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LeadSource source = LeadSource.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LeadStatus status = LeadStatus.NEW;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private String website;
    private String industry;

    @Column(name = "employee_count")
    private Integer employeeCount;

    @Column(name = "estimated_value")
    private Double estimatedValue;
}