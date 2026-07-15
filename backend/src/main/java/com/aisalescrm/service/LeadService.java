package com.aisalescrm.service;

import com.aisalescrm.dto.request.LeadAssignRequest;
import com.aisalescrm.dto.request.LeadRequest;
import com.aisalescrm.dto.response.LeadResponse;
import com.aisalescrm.dto.response.PageResponse;
import com.aisalescrm.enums.LeadSource;
import com.aisalescrm.enums.LeadStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface LeadService {

    LeadResponse createLead(LeadRequest request);

    LeadResponse getLeadById(Long id);

    PageResponse<LeadResponse> getAllLeads(
            LeadStatus status,
            LeadSource source,
            Long assignedToId,
            String search,
            Pageable pageable
    );

    LeadResponse updateLead(Long id, LeadRequest request);

    LeadResponse assignLead(Long id, LeadAssignRequest request);

    LeadResponse updateStatus(Long id, LeadStatus status);

    void deleteLead(Long id);

    List<LeadResponse> getLeadsByAssignedUser(Long userId);

    Map<String, Long> getLeadStatusCounts();

    Map<String, Long> getLeadSourceCounts();
}