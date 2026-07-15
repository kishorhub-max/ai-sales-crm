package com.aisalescrm.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    // ── KPI Cards ─────────────────────────────────────────────────────────────
    private BigDecimal totalRevenue;
    private BigDecimal outstandingRevenue;
    private long       activeCustomers;
    private long       openOpportunities;
    private BigDecimal pipelineValue;
    private long       totalLeads;
    private long       newLeadsThisMonth;
    private long       overdueInvoices;

    // ── Conversion Rate (leads → customers) ──────────────────────────────────
    private Double conversionRate;

    // ── Charts data ───────────────────────────────────────────────────────────

    // [{month:"2024-01", revenue:50000, orders:12}, ...]
    private List<Map<String, Object>> monthlyRevenue;

    // [{stage:"NEW", count:5, value:120000}, ...]
    private List<Map<String, Object>> opportunityPipeline;

    // [{status:"NEW", count:8}, ...]
    private Map<String, Long> leadsByStatus;

    // [{status:"PENDING", count:3, revenue:15000}, ...]
    private Map<String, Object> ordersByStatus;

    // Top 5 customers by value
    private List<CustomerResponse> topCustomers;

    // Recent activity
    private List<LeadResponse>     recentLeads;
    private List<OrderResponse>    recentOrders;
}