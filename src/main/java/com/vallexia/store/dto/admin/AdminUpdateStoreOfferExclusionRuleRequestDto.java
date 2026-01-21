package com.vallexia.store.dto.admin;

import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a store offer exclusion rule.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateStoreOfferExclusionRuleRequestDto {

    @Size(max = 255, message = "name must be at most 255 characters")
    private String name;

    private Boolean enabled;

    private String scope; // "GLOBAL" or "STORE"

    @Size(max = 100, message = "storeName must be at most 100 characters")
    private String storeName; // Required when scope = "STORE"

    private String matchType; // "WORD", "CONTAINS", or "REGEX"

    private List<String> patterns;

    private Integer priority;
}
