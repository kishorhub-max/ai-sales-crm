package com.aisalescrm.service.impl;

import com.aisalescrm.dto.request.LeadAssignRequest;
import com.aisalescrm.dto.request.LeadRequest;
import com.aisalescrm.dto.response.LeadResponse;
import com.aisalescrm.dto.response.PageResponse;
import com.aisalescrm.entity.Lead;
import com.aisalescrm.entity.User;
import com.aisalescrm.enums.LeadSource;
import com.aisalescrm.enums.LeadStatus;
import com.aisalescrm.exception.ResourceNotFoundException;
import com.aisalescrm.mapper.LeadMapper;
import com.aisalescrm.repository.LeadRepository;
import com.aisalescrm.repository.UserRepository;
import com.aisalescrm.service.LeadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeadServiceImpl implements LeadService {

    private final LeadRepository    leadRepository;
    private final UserRepository    userRepository;
    private final LeadMapper        leadMapper;

    // ── Create ───────────────────────────────────────────────────────────────

    @Override
    public LeadResponse createLead(LeadRequest request) {
        Lead lead = leadMapper.toEntity(request);

        if (request.getAssignedToId() != null) {
            User assignee = findUser(request.getAssignedToId());
            lead.setAssignedTo(assignee);
        }

        Lead saved = leadRepository.save(lead);
        log.info("Lead created: id={}, name={}", saved.getId(), saved.getName());
        return leadMapper.toResponse(saved);
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public LeadResponse getLeadById(Long id) {
        return leadMapper.toResponse(findLead(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LeadResponse> getAllLeads(LeadStatus status,
                                                  LeadSource source,
                                                  Long assignedToId,
                                                  String search,
                                                  Pageable pageable) {
        Page<Lead> page = leadRepository.findAllWithFilters(
                status, source, assignedToId,
                search == null ? "" : search.trim(),
                pageable
        );
        Page<LeadResponse> responsePage = page.map(leadMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    // ── Update ───────────────────────────────────────────────────────────────

    @Override
    public LeadResponse updateLead(Long id, LeadRequest request) {
        Lead lead = findLead(id);
        leadMapper.updateEntity(lead, request);

        // Handle assignment change
        if (request.getAssignedToId() != null) {
            lead.setAssignedTo(findUser(request.getAssignedToId()));
        } else {
            lead.setAssignedTo(null);
        }

        Lead updated = leadRepository.save(lead);
        log.info("Lead updated: id={}", updated.getId());
        return leadMapper.toResponse(updated);
    }

    // ── Assign ───────────────────────────────────────────────────────────────

    @Override
    public LeadResponse assignLead(Long id, LeadAssignRequest request) {
        Lead lead = findLead(id);
        User assignee = findUser(request.getAssignedToId());
        lead.setAssignedTo(assignee);

        Lead saved = leadRepository.save(lead);
        log.info("Lead {} assigned to user {}", id, assignee.getEmail());
        return leadMapper.toResponse(saved);
    }

    // ── Status Update ────────────────────────────────────────────────────────

    @Override
    public LeadResponse updateStatus(Long id, LeadStatus status) {
        Lead lead = findLead(id);
        lead.setStatus(status);
        Lead saved = leadRepository.save(lead);
        log.info("Lead {} status changed to {}", id, status);
        return leadMapper.toResponse(saved);
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    @Override
    public void deleteLead(Long id) {
        Lead lead = findLead(id);
        leadRepository.delete(lead);
        log.info("Lead deleted: id={}", id);
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<LeadResponse> getLeadsByAssignedUser(Long userId) {
        return leadRepository.findByAssignedToId(userId)
                .stream()
                .map(leadMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getLeadStatusCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        leadRepository.countGroupByStatus()
                .forEach(row -> counts.put(row[0].toString(), (Long) row[1]));
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getLeadSourceCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        leadRepository.countGroupBySource()
                .forEach(row -> counts.put(row[0].toString(), (Long) row[1]));
        return counts;
    }

    // ── Private Helpers ──────────────────────────────────────────────────────

    private Lead findLead(Long id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", id));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}