package com.vallexia.store.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a store offer exclusion rule.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateStoreOfferExclusionRuleRequestDto {

    @NotBlank(message = "name is required")
    @Size(max = 255, message = "name must be at most 255 characters")
    private String name;

    private Boolean enabled = true;

    @NotBlank(message = "scope is required")
    private String scope; // "GLOBAL" or "STORE"

    @Size(max = 100, message = "storeName must be at most 100 characters")
    private String storeName; // Required when scope = "STORE"

    @NotBlank(message = "matchType is required")
    private String matchType; // "WORD", "CONTAINS", or "REGEX"

    @NotNull(message = "patterns is required")
    @Size(min = 1, message = "patterns must contain at least one pattern")
    private List<@NotBlank(message = "pattern cannot be blank") String> patterns;
}
