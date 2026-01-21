package com.vallexia.store.service;

import com.vallexia.store.dto.admin.AdminCreateStoreOfferExclusionRuleRequestDto;
import com.vallexia.store.dto.admin.AdminStoreOfferExclusionRuleDto;
import com.vallexia.store.dto.admin.AdminUpdateStoreOfferExclusionRuleRequestDto;
import com.vallexia.store.entity.StoreOfferExclusionRule;
import com.vallexia.store.repository.StoreOfferExclusionRuleRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing store offer exclusion rules (admin operations).
 */
@Service
@RequiredArgsConstructor
public class AdminStoreOfferExclusionRuleService {

    private final StoreOfferExclusionRuleRepository ruleRepository;

    @Transactional(readOnly = true)
    public List<AdminStoreOfferExclusionRuleDto> listRules() {
        return ruleRepository.findAllOrdered().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public AdminStoreOfferExclusionRuleDto create(AdminCreateStoreOfferExclusionRuleRequestDto req) {
        if (req == null) {
            throw new IllegalArgumentException("Request is required");
        }

        validateScopeAndStoreName(req.getScope(), req.getStoreName());
        validateMatchType(req.getMatchType());

        StoreOfferExclusionRule rule = new StoreOfferExclusionRule();
        rule.setName(req.getName().trim());
        rule.setEnabled(req.getEnabled() != null ? req.getEnabled() : true);
        rule.setScope(req.getScope().trim().toUpperCase());
        rule.setStoreName(req.getStoreName() != null && !req.getStoreName().isBlank() 
            ? req.getStoreName().trim().toUpperCase() : null);
        rule.setMatchType(req.getMatchType().trim().toUpperCase());
        rule.setPatterns(req.getPatterns().stream()
            .filter(p -> p != null && !p.isBlank())
            .map(String::trim)
            .collect(Collectors.toList())
            .toArray(new String[0]));
        rule.setPriority(100); // Default priority, can be adjusted via update if needed

        rule = ruleRepository.save(rule);
        return toDto(rule);
    }

    @Transactional
    public AdminStoreOfferExclusionRuleDto update(Long ruleId, AdminUpdateStoreOfferExclusionRuleRequestDto req) {
        if (ruleId == null) {
            throw new IllegalArgumentException("ruleId is required");
        }
        if (req == null) {
            throw new IllegalArgumentException("Request is required");
        }

        StoreOfferExclusionRule rule = ruleRepository.findById(ruleId)
            .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));

        if (req.getName() != null) {
            String v = req.getName().trim();
            if (v.isBlank()) {
                throw new IllegalArgumentException("name cannot be blank");
            }
            rule.setName(v);
        }
        if (req.getEnabled() != null) {
            rule.setEnabled(req.getEnabled());
        }
        if (req.getScope() != null) {
            String scope = req.getScope().trim().toUpperCase();
            validateScopeAndStoreName(scope, req.getStoreName());
            rule.setScope(scope);
        }
        if (req.getStoreName() != null) {
            String storeName = req.getStoreName().trim().toUpperCase();
            if (rule.getScope() != null && "STORE".equals(rule.getScope()) && storeName.isBlank()) {
                throw new IllegalArgumentException("storeName is required when scope is STORE");
            }
            if (rule.getScope() != null && "GLOBAL".equals(rule.getScope()) && !storeName.isBlank()) {
                throw new IllegalArgumentException("storeName must be null when scope is GLOBAL");
            }
            rule.setStoreName(storeName.isBlank() ? null : storeName);
        }
        if (req.getMatchType() != null) {
            validateMatchType(req.getMatchType());
            rule.setMatchType(req.getMatchType().trim().toUpperCase());
        }
        if (req.getPatterns() != null) {
            if (req.getPatterns().isEmpty()) {
                throw new IllegalArgumentException("patterns must contain at least one pattern");
            }
            rule.setPatterns(req.getPatterns().stream()
                .filter(p -> p != null && !p.isBlank())
                .map(String::trim)
                .collect(Collectors.toList())
                .toArray(new String[0]));
        }

        rule = ruleRepository.save(rule);
        return toDto(rule);
    }

    @Transactional
    public AdminStoreOfferExclusionRuleDto enable(Long ruleId) {
        if (ruleId == null) {
            throw new IllegalArgumentException("ruleId is required");
        }
        StoreOfferExclusionRule rule = ruleRepository.findById(ruleId)
            .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));
        rule.setEnabled(true);
        rule = ruleRepository.save(rule);
        return toDto(rule);
    }

    @Transactional
    public AdminStoreOfferExclusionRuleDto disable(Long ruleId) {
        if (ruleId == null) {
            throw new IllegalArgumentException("ruleId is required");
        }
        StoreOfferExclusionRule rule = ruleRepository.findById(ruleId)
            .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));
        rule.setEnabled(false);
        rule = ruleRepository.save(rule);
        return toDto(rule);
    }

    @Transactional
    public void delete(Long ruleId) {
        if (ruleId == null) {
            throw new IllegalArgumentException("ruleId is required");
        }
        if (!ruleRepository.existsById(ruleId)) {
            throw new IllegalArgumentException("Rule not found: " + ruleId);
        }
        ruleRepository.deleteById(ruleId);
    }

    private AdminStoreOfferExclusionRuleDto toDto(StoreOfferExclusionRule rule) {
        List<String> patterns = rule.getPatterns() == null 
            ? List.of() 
            : List.of(rule.getPatterns());

        return new AdminStoreOfferExclusionRuleDto(
            rule.getId(),
            rule.getName(),
            rule.isEnabled(),
            rule.getScope(),
            rule.getStoreName(),
            rule.getMatchType(),
            patterns,
            rule.getPriority(),
            rule.getCreatedAt(),
            rule.getUpdatedAt()
        );
    }

    private void validateScopeAndStoreName(String scope, String storeName) {
        if (scope == null) {
            return;
        }
        scope = scope.trim().toUpperCase();
        if (!"GLOBAL".equals(scope) && !"STORE".equals(scope)) {
            throw new IllegalArgumentException("scope must be GLOBAL or STORE");
        }
        if ("STORE".equals(scope) && (storeName == null || storeName.trim().isBlank())) {
            throw new IllegalArgumentException("storeName is required when scope is STORE");
        }
        if ("GLOBAL".equals(scope) && storeName != null && !storeName.trim().isBlank()) {
            throw new IllegalArgumentException("storeName must be null when scope is GLOBAL");
        }
    }

    private void validateMatchType(String matchType) {
        if (matchType == null) {
            return;
        }
        matchType = matchType.trim().toUpperCase();
        if (!"WORD".equals(matchType) && !"CONTAINS".equals(matchType) && !"REGEX".equals(matchType)) {
            throw new IllegalArgumentException("matchType must be WORD, CONTAINS, or REGEX");
        }
    }
}
