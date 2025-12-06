package com.vallexia.user.dto;

import com.vallexia.common.enums.SupportedMealCategory;
import com.vallexia.common.validator.ValidMealCategory;
import com.vallexia.common.validator.ValidSubscriptionStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Data Transfer Object for user profile information.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    
    private Long id;
    
    // Username is read-only (cannot be updated)
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 50, message = "Email must not exceed 50 characters")
    private String email;
    
    private Boolean enabled;
    
    @NotNull(message = "Household size is required")
    @Min(value = 1, message = "Household size must be at least 1")
    @Max(value = 20, message = "Household size cannot exceed 20")
    private Integer householdSize;
    
    @NotEmpty(message = "At least one meal type must be selected")
    @ValidMealCategory
    private Set<SupportedMealCategory> mealTypes = new HashSet<>();
    
    @ValidSubscriptionStatus
    private String subscriptionStatus;
    
    private LocalDateTime subscriptionExpiresAt;    
}
