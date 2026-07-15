package com.aisalescrm.entity;

import com.aisalescrm.entity.BaseEntity;
import com.aisalescrm.entity.Lead;
import com.aisalescrm.entity.Opportunity;
import com.aisalescrm.entity.Order;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "customers",
        indexes = {
                @Index(name = "idx_customer_email",   columnList = "email", unique = true),
                @Index(name = "idx_customer_company", columnList = "company")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Profile ──────────────────────────────────────────────────────────────
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;
    private String mobile;

    // ── Company details ──────────────────────────────────────────────────────
    private String company;

    @Column(name = "job_title")
    private String jobTitle;

    private String industry;
    private String website;

    // ── Address ──────────────────────────────────────────────────────────────
    private String street;
    private String city;
    private String state;
    private String country;

    @Column(name = "zip_code")
    private String zipCode;

    // ── Notes & metadata ─────────────────────────────────────────────────────
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "total_purchase_value")
    @Builder.Default
    private Double totalPurchaseValue = 0.0;

    @Column(name = "purchase_count")
    @Builder.Default
    private Integer purchaseCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    // ── Relationships ────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private Lead convertedFromLead;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Opportunity> opportunities = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    // ── Helpers ──────────────────────────────────────────────────────────────
    public String getFullName() {
        return firstName + " " + lastName;
    }
}