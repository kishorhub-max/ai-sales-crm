package com.aisalescrm.service.impl;

import com.aisalescrm.dto.response.*;
import com.aisalescrm.enums.LeadStatus;
import com.aisalescrm.enums.OpportunityStage;
import com.aisalescrm.mapper.CustomerMapper;
import com.aisalescrm.mapper.LeadMapper;
import com.aisalescrm.mapper.OrderMapper;
import com.aisalescrm.repository.*;
import com.aisalescrm.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final LeadRepository        leadRepository;
    private final CustomerRepository    customerRepository;
    private final OpportunityRepository opportunityRepository;
    private final OrderRepository       orderRepository;
    private final InvoiceRepository     invoiceRepository;

    private final LeadMapper     leadMapper;
    private final CustomerMapper customerMapper;
    private final OrderMapper    orderMapper;

    @Override
    public DashboardStatsResponse getDashboardStats() {

        // ── KPIs ──────────────────────────────────────────────────────────────

        BigDecimal totalRevenue       = safe(invoiceRepository.sumPaidRevenue());
        BigDecimal outstandingRevenue = safe(invoiceRepository.sumOutstandingRevenue());
        long       activeCustomers    = customerRepository.countByActiveTrue();
        long       openOpportunities  = opportunityRepository.count()
                - opportunityRepository.countByStage(OpportunityStage.WON)
                - opportunityRepository.countByStage(OpportunityStage.LOST);
        BigDecimal pipelineValue      = safe(opportunityRepository.sumWeightedPipeline());
        long       totalLeads         = leadRepository.count();
        long       overdueInvoices    = invoiceRepository
                .findOverdueInvoices(LocalDate.now()).size();

        // Leads created this month
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long newLeadsThisMonth = leadRepository
                .findByCreatedAtBetween(startOfMonth, LocalDateTime.now()).size();

        // Conversion rate = converted leads / total leads * 100
        double conversionRate = 0.0;
        if (totalLeads > 0) {
            long converted = leadRepository.countByStatus(LeadStatus.CONVERTED);
            conversionRate = BigDecimal.valueOf(converted)
                    .divide(BigDecimal.valueOf(totalLeads), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        // ── Monthly Revenue ────────────────────────────────────────────────────

        List<Map<String, Object>> monthlyRevenue = new ArrayList<>();
        for (Object[] row : orderRepository.getMonthlyRevenue()) {
            Map<String, Object> point = new HashMap<>();
            point.put("month",      row[0].toString());
            point.put("orderCount", row[1]);
            point.put("revenue",    row[2] != null ? row[2] : BigDecimal.ZERO);
            monthlyRevenue.add(point);
        }

        // ── Opportunity Pipeline ───────────────────────────────────────────────

        List<Map<String, Object>> pipeline = new ArrayList<>();
        for (Object[] row : opportunityRepository.getPipelineStats()) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("stage", row[0].toString());
            entry.put("count", row[1]);
            entry.put("value", row[2] != null ? row[2] : BigDecimal.ZERO);
            pipeline.add(entry);
        }

        // ── Leads by Status ────────────────────────────────────────────────────

        Map<String, Long> leadsByStatus = new LinkedHashMap<>();
        leadRepository.countGroupByStatus()
                .forEach(row -> leadsByStatus.put(row[0].toString(), (Long) row[1]));

        // ── Orders by Status ───────────────────────────────────────────────────

        Map<String, Object> ordersByStatus = new HashMap<>();
        for (Object[] row : orderRepository.getOrderStatsByStatus()) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("count",   row[1]);
            entry.put("revenue", row[2] != null ? row[2] : BigDecimal.ZERO);
            ordersByStatus.put(row[0].toString(), entry);
        }

        // ── Top Customers ──────────────────────────────────────────────────────

        List<CustomerResponse> topCustomers = customerRepository
                .findTopCustomersByValue(PageRequest.of(0, 5))
                .stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());

        // ── Recent Leads ───────────────────────────────────────────────────────

        List<LeadResponse> recentLeads = leadRepository
                .findRecentLeads(PageRequest.of(0, 5))
                .stream()
                .map(leadMapper::toResponse)
                .collect(Collectors.toList());

        // ── Recent Orders ──────────────────────────────────────────────────────

        List<OrderResponse> recentOrders = orderRepository
                .findAll(PageRequest.of(0, 5,
                        org.springframework.data.domain.Sort.by("createdAt").descending()))
                .getContent()
                .stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());

        // ── Assemble ───────────────────────────────────────────────────────────

        return DashboardStatsResponse.builder()
                .totalRevenue(totalRevenue)
                .outstandingRevenue(outstandingRevenue)
                .activeCustomers(activeCustomers)
                .openOpportunities(openOpportunities)
                .pipelineValue(pipelineValue)
                .totalLeads(totalLeads)
                .newLeadsThisMonth(newLeadsThisMonth)
                .overdueInvoices(overdueInvoices)
                .conversionRate(conversionRate)
                .monthlyRevenue(monthlyRevenue)
                .opportunityPipeline(pipeline)
                .leadsByStatus(leadsByStatus)
                .ordersByStatus(ordersByStatus)
                .topCustomers(topCustomers)
                .recentLeads(recentLeads)
                .recentOrders(recentOrders)
                .build();
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}