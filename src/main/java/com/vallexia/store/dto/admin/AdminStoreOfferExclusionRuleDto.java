package com.vallexia.store.dto.admin;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin DTO for viewing/editing store offer exclusion rules.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminStoreOfferExclusionRuleDto {

    private Long id;
    private String name;
    private boolean enabled;
    private String scope; // "GLOBAL" or "STORE"
    private String storeName; // Only set when scope = "STORE"
    private String matchType; // "WORD", "CONTAINS", or "REGEX"
    private List<String> patterns;
    private int priority;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
