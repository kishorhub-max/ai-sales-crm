package com.aisalescrm.service.impl;

import com.aisalescrm.dto.request.ConvertLeadRequest;
import com.aisalescrm.dto.request.CustomerRequest;
import com.aisalescrm.dto.response.CustomerResponse;
import com.aisalescrm.dto.response.CustomerStatsResponse;
import com.aisalescrm.dto.response.PageResponse;
import com.aisalescrm.entity.Customer;
import com.aisalescrm.entity.Lead;
import com.aisalescrm.entity.User;
import com.aisalescrm.enums.LeadStatus;
import com.aisalescrm.exception.BusinessException;
import com.aisalescrm.exception.DuplicateResourceException;
import com.aisalescrm.exception.ResourceNotFoundException;
import com.aisalescrm.mapper.CustomerMapper;
import com.aisalescrm.repository.CustomerRepository;
import com.aisalescrm.repository.LeadRepository;
import com.aisalescrm.repository.UserRepository;
import com.aisalescrm.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final LeadRepository     leadRepository;
    private final UserRepository     userRepository;
    private final CustomerMapper     customerMapper;

    // ── Create ───────────────────────────────────────────────────────────────

    @Override
    public CustomerResponse createCustomer(CustomerRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Customer already exists with email: " + request.getEmail());
        }

        Customer customer = customerMapper.toEntity(request);

        if (request.getAssignedToId() != null) {
            customer.setAssignedTo(findUser(request.getAssignedToId()));
        }

        Customer saved = customerRepository.save(customer);
        log.info("Customer created: id={}, email={}", saved.getId(), saved.getEmail());
        return customerMapper.toResponse(saved);
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        return customerMapper.toResponse(findCustomer(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> getAllCustomers(Boolean active,
                                                          Long assignedToId,
                                                          String search,
                                                          Pageable pageable) {
        String cleanSearch = search == null ? "" : search.trim();        Page<Customer> page = customerRepository.findAllWithFilters(
                active, assignedToId, cleanSearch, pageable);
        Page<CustomerResponse> mapped = page.map(customerMapper::toResponse);
        return PageResponse.of(mapped);
    }

    // ── Update ───────────────────────────────────────────────────────────────

    @Override
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer customer = findCustomer(id);

        // Guard duplicate email if email changed
        if (!customer.getEmail().equalsIgnoreCase(request.getEmail())
                && customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email already in use: " + request.getEmail());
        }

        customerMapper.updateEntity(customer, request);

        if (request.getAssignedToId() != null) {
            customer.setAssignedTo(findUser(request.getAssignedToId()));
        } else {
            customer.setAssignedTo(null);
        }

        Customer updated = customerRepository.save(customer);
        log.info("Customer updated: id={}", updated.getId());
        return customerMapper.toResponse(updated);
    }

    // ── Convert Lead → Customer ──────────────────────────────────────────────

    @Override
    public CustomerResponse convertLeadToCustomer(ConvertLeadRequest request) {
        Lead lead = leadRepository.findById(request.getLeadId())
                .orElseThrow(() -> new ResourceNotFoundException("Lead", request.getLeadId()));

        // Prevent double conversion
        if (lead.getStatus() == LeadStatus.CONVERTED) {
            throw new BusinessException("Lead is already converted to a customer");
        }

        customerRepository.findByConvertedFromLeadId(lead.getId()).ifPresent(c -> {
            throw new DuplicateResourceException(
                    "Lead already converted. Customer id: " + c.getId());
        });

        // Also guard email uniqueness
        if (customerRepository.existsByEmail(lead.getEmail())) {
            throw new DuplicateResourceException(
                    "A customer already exists with email: " + lead.getEmail());
        }

        // Build customer from lead — split name on first space
        String[] nameParts = splitName(lead.getName());

        Customer customer = Customer.builder()
                .firstName(nameParts[0])
                .lastName(nameParts[1])
                .email(lead.getEmail())
                .phone(lead.getPhone())
                .company(request.getCompany()  != null ? request.getCompany()  : lead.getCompany())
                .jobTitle(request.getJobTitle()!= null ? request.getJobTitle() : lead.getJobTitle())
                .industry(request.getIndustry()!= null ? request.getIndustry(): lead.getIndustry())
                .website(request.getWebsite()  != null ? request.getWebsite()  : lead.getWebsite())
                .notes(request.getNotes()      != null ? request.getNotes()    : lead.getNotes())
                .convertedFromLead(lead)
                .active(true)
                .totalPurchaseValue(0.0)
                .purchaseCount(0)
                .build();

        // Assign to requested user, else inherit from lead
        Long assignedId = request.getAssignedToId() != null
                ? request.getAssignedToId()
                : (lead.getAssignedTo() != null ? lead.getAssignedTo().getId() : null);
        if (assignedId != null) {
            customer.setAssignedTo(findUser(assignedId));
        }

        Customer saved = customerRepository.save(customer);

        // Mark lead as converted
        lead.setStatus(LeadStatus.CONVERTED);
        leadRepository.save(lead);

        log.info("Lead {} converted to Customer {}", lead.getId(), saved.getId());
        return customerMapper.toResponse(saved);
    }

    // ── Delete (soft — deactivate) ───────────────────────────────────────────

    @Override
    public void deleteCustomer(Long id) {
        Customer customer = findCustomer(id);
        customer.setActive(false);
        customerRepository.save(customer);
        log.info("Customer soft-deleted (deactivated): id={}", id);
    }

    // ── Top / Recent ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getTopCustomers(int limit) {
        return customerRepository
                .findTopCustomersByValue(PageRequest.of(0, limit))
                .stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getRecentCustomers(int limit) {
        return customerRepository
                .findRecentCustomers(PageRequest.of(0, limit))
                .stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ── Stats ────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public CustomerStatsResponse getCustomerStats() {
        Map<String, Long> byIndustry = new LinkedHashMap<>();
        customerRepository.countGroupByIndustry()
                .forEach(row -> byIndustry.put(row[0].toString(), (Long) row[1]));

        return CustomerStatsResponse.builder()
                .totalCustomers(customerRepository.count())
                .activeCustomers(customerRepository.countByActiveTrue())
                .customersWithPurchases(customerRepository.countCustomersWithPurchases())
                .totalRevenue(customerRepository.sumTotalPurchaseValue())
                .byIndustry(byIndustry)
                .build();
    }

    // ── Internal: called by OrderService after order is confirmed ────────────

    @Override
    public void updatePurchaseSummary(Long customerId, double orderTotal) {
        Customer customer = findCustomer(customerId);
        customer.setTotalPurchaseValue(
                (customer.getTotalPurchaseValue() == null ? 0 : customer.getTotalPurchaseValue())
                        + orderTotal);
        customer.setPurchaseCount(
                (customer.getPurchaseCount() == null ? 0 : customer.getPurchaseCount()) + 1);
        customerRepository.save(customer);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private String[] splitName(String fullName) {
        if (fullName == null || fullName.isBlank()) return new String[]{"Unknown", ""};
        int space = fullName.indexOf(' ');
        if (space < 0) return new String[]{fullName, ""};
        return new String[]{fullName.substring(0, space), fullName.substring(space + 1)};
    }
}