package com.aisalescrm.service;

import com.aisalescrm.dto.request.OpportunityRequest;
import com.aisalescrm.dto.request.OpportunityStageUpdateRequest;
import com.aisalescrm.dto.response.OpportunityResponse;
import com.aisalescrm.dto.response.PageResponse;
import com.aisalescrm.dto.response.PipelineStatsResponse;
import com.aisalescrm.enums.OpportunityStage;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OpportunityService {

    OpportunityResponse createOpportunity(OpportunityRequest request);

    OpportunityResponse getOpportunityById(Long id);

    PageResponse<OpportunityResponse> getAllOpportunities(
            OpportunityStage stage,
            Long customerId,
            Long assignedToId,
            String search,
            Pageable pageable
    );

    OpportunityResponse updateOpportunity(Long id, OpportunityRequest request);

    OpportunityResponse updateStage(Long id, OpportunityStageUpdateRequest request);

    void deleteOpportunity(Long id);

    List<OpportunityResponse> getOpportunitiesByCustomer(Long customerId);

    List<OpportunityResponse> getAtRiskOpportunities();

    List<OpportunityResponse> getClosingSoon(int days);

    PipelineStatsResponse getPipelineStats();
}