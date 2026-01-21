package com.vallexia.store.controller;

import com.vallexia.store.dto.admin.AdminCreateStoreOfferExclusionRuleRequestDto;
import com.vallexia.store.dto.admin.AdminStoreOfferExclusionRuleDto;
import com.vallexia.store.dto.admin.AdminUpdateStoreOfferExclusionRuleRequestDto;
import com.vallexia.store.service.AdminStoreOfferExclusionRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin endpoints for managing store offer exclusion rules.
 */
@RestController
@RequestMapping("/api/v1/admin/store-offer-filters")
@Tag(name = "Admin Store Offer Filters", description = "Admin operations for managing offer exclusion rules")
@RequiredArgsConstructor
public class AdminStoreOfferExclusionRuleController {

    private final AdminStoreOfferExclusionRuleService service;

    @Operation(summary = "List all exclusion rules (Admin only)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminStoreOfferExclusionRuleDto>> listRules() {
        return ResponseEntity.ok(service.listRules());
    }

    @Operation(summary = "Create exclusion rule (Admin only)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminStoreOfferExclusionRuleDto> create(
        @Valid @RequestBody AdminCreateStoreOfferExclusionRuleRequestDto req) {
        return ResponseEntity.ok(service.create(req));
    }

    @Operation(summary = "Update exclusion rule (Admin only)")
    @PutMapping("/{ruleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminStoreOfferExclusionRuleDto> update(
        @PathVariable Long ruleId,
        @Valid @RequestBody AdminUpdateStoreOfferExclusionRuleRequestDto req) {
        return ResponseEntity.ok(service.update(ruleId, req));
    }

    @Operation(summary = "Enable exclusion rule (Admin only)")
    @PatchMapping("/{ruleId}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminStoreOfferExclusionRuleDto> enable(@PathVariable Long ruleId) {
        return ResponseEntity.ok(service.enable(ruleId));
    }

    @Operation(summary = "Disable exclusion rule (Admin only)")
    @PatchMapping("/{ruleId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminStoreOfferExclusionRuleDto> disable(@PathVariable Long ruleId) {
        return ResponseEntity.ok(service.disable(ruleId));
    }

    @Operation(summary = "Delete exclusion rule (Admin only)")
    @DeleteMapping("/{ruleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long ruleId) {
        service.delete(ruleId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("ruleId", ruleId);
        return ResponseEntity.ok(response);
    }
}
