package com.aisalescrm.service.impl;

import com.aisalescrm.dto.request.OpportunityRequest;
import com.aisalescrm.dto.request.OpportunityStageUpdateRequest;
import com.aisalescrm.dto.response.OpportunityResponse;
import com.aisalescrm.dto.response.PageResponse;
import com.aisalescrm.dto.response.PipelineStatsResponse;
import com.aisalescrm.entity.Customer;
import com.aisalescrm.entity.Opportunity;
import com.aisalescrm.entity.Product;
import com.aisalescrm.entity.User;
import com.aisalescrm.enums.OpportunityStage;
import com.aisalescrm.exception.ResourceNotFoundException;
import com.aisalescrm.mapper.OpportunityMapper;
import com.aisalescrm.repository.CustomerRepository;
import com.aisalescrm.repository.OpportunityRepository;
import com.aisalescrm.repository.ProductRepository;
import com.aisalescrm.repository.UserRepository;
import com.aisalescrm.service.OpportunityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OpportunityServiceImpl implements OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final CustomerRepository    customerRepository;
    private final UserRepository        userRepository;
    private final ProductRepository     productRepository;
    private final OpportunityMapper     opportunityMapper;

    // ── Create ───────────────────────────────────────────────────────────────

    @Override
    public OpportunityResponse createOpportunity(OpportunityRequest request) {
        Opportunity opp = opportunityMapper.toEntity(request);
        resolveRelationships(opp, request);
        Opportunity saved = opportunityRepository.save(opp);
        log.info("Opportunity created: id={}, deal={}", saved.getId(), saved.getDealName());
        return opportunityMapper.toResponse(saved);
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public OpportunityResponse getOpportunityById(Long id) {
        return opportunityMapper.toResponse(findOpp(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OpportunityResponse> getAllOpportunities(OpportunityStage stage,
                                                                 Long customerId,
                                                                 Long assignedToId,
                                                                 String search,
                                                                 Pageable pageable) {

        Page<Opportunity> page = opportunityRepository.findAllWithFilters(
                stage, customerId, assignedToId, pageable);

        return PageResponse.of(page.map(opportunityMapper::toResponse));
    }

    // ── Update ───────────────────────────────────────────────────────────────

    @Override
    public OpportunityResponse updateOpportunity(Long id, OpportunityRequest request) {
        Opportunity opp = findOpp(id);
        opportunityMapper.updateEntity(opp, request);
        resolveRelationships(opp, request);
        Opportunity updated = opportunityRepository.save(opp);
        log.info("Opportunity updated: id={}", updated.getId());
        return opportunityMapper.toResponse(updated);
    }

    // ── Stage Change (PATCH) ─────────────────────────────────────────────────

    @Override
    public OpportunityResponse updateStage(Long id, OpportunityStageUpdateRequest request) {
        Opportunity opp = findOpp(id);
        opp.setStage(request.getStage());

        if (request.getProbability() != null) {
            opp.setProbability(request.getProbability());
        } else {
            // Auto-set probability on stage change
            opp.setProbability(defaultProbabilityFor(request.getStage()));
        }

        if (request.getLostReason() != null) {
            opp.setLostReason(request.getLostReason());
        }

        Opportunity saved = opportunityRepository.save(opp);
        log.info("Opportunity {} moved to stage {}", id, request.getStage());
        return opportunityMapper.toResponse(saved);
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    @Override
    public void deleteOpportunity(Long id) {
        Opportunity opp = findOpp(id);
        opportunityRepository.delete(opp);
        log.info("Opportunity deleted: id={}", id);
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<OpportunityResponse> getOpportunitiesByCustomer(Long customerId) {
        return opportunityRepository.findByCustomerId(customerId)
                .stream().map(opportunityMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpportunityResponse> getAtRiskOpportunities() {
        return opportunityRepository.findAtRiskOpportunities(LocalDate.now())
                .stream().map(opportunityMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpportunityResponse> getClosingSoon(int days) {
        LocalDate horizon = LocalDate.now().plusDays(days);
        return opportunityRepository.findClosingSoon(LocalDate.now(), horizon)
                .stream().map(opportunityMapper::toResponse).collect(Collectors.toList());
    }

    // ── Pipeline Stats ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PipelineStatsResponse getPipelineStats() {

        // Per-stage stats
        List<PipelineStatsResponse.StageStats> stageStats = new ArrayList<>();
        BigDecimal totalPipeline = BigDecimal.ZERO;

        for (Object[] row : opportunityRepository.getPipelineStats()) {
            String stage     = row[0].toString();
            long   count     = (Long) row[1];
            BigDecimal value = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
            stageStats.add(PipelineStatsResponse.StageStats.builder()
                    .stage(stage).count(count).value(value).build());
            totalPipeline = totalPipeline.add(value);
        }

        // Monthly revenue trend
        List<Map<String, Object>> monthlyRevenue = new ArrayList<>();
        for (Object[] row : opportunityRepository.getMonthlyWonRevenue()) {
            Map<String, Object> point = new HashMap<>();
            point.put("month",   row[0].toString());
            point.put("revenue", row[1] != null ? row[1] : BigDecimal.ZERO);
            monthlyRevenue.add(point);
        }

        BigDecimal wonRevenue      = opportunityRepository.sumWonRevenue();
        BigDecimal weightedValue   = opportunityRepository.sumWeightedPipeline();
        long       wonCount        = opportunityRepository.countByStage(OpportunityStage.WON);
        long       lostCount       = opportunityRepository.countByStage(OpportunityStage.LOST);
        long       totalCount      = opportunityRepository.count();
        long       openCount       = totalCount - wonCount - lostCount;

        return PipelineStatsResponse.builder()
                .stages(stageStats)
                .totalPipelineValue(totalPipeline)
                .weightedPipelineValue(weightedValue != null ? weightedValue : BigDecimal.ZERO)
                .wonRevenue(wonRevenue != null ? wonRevenue : BigDecimal.ZERO)
                .totalOpportunities(totalCount)
                .openOpportunities(openCount)
                .wonCount(wonCount)
                .lostCount(lostCount)
                .monthlyRevenue(monthlyRevenue)
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void resolveRelationships(Opportunity opp, OpportunityRequest req) {
        // Customer (required)
        Customer customer = customerRepository.findById(req.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", req.getCustomerId()));
        opp.setCustomer(customer);

        // Assigned user (optional)
        if (req.getAssignedToId() != null) {
            User user = userRepository.findById(req.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", req.getAssignedToId()));
            opp.setAssignedTo(user);
        } else {
            opp.setAssignedTo(null);
        }

        // Product (optional)
        if (req.getProductId() != null) {
            Product product = productRepository.findById(req.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", req.getProductId()));
            opp.setProduct(product);
        } else {
            opp.setProduct(null);
        }
    }

    private Opportunity findOpp(Long id) {
        return opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity", id));
    }

    private int defaultProbabilityFor(OpportunityStage stage) {
        return switch (stage) {
            case NEW         -> 10;
            case QUALIFIED   -> 25;
            case PROPOSAL    -> 50;
            case NEGOTIATION -> 75;
            case WON         -> 100;
            case LOST        -> 0;
        };
    }
}