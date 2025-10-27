package com.vallexia.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for user profile information.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
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
    
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;
    
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;
    
    @Size(max = 500, message = "Profile picture URL must not exceed 500 characters")
    private String profilePictureUrl;
    
    private Boolean enabled;
    
    @Min(value = 1, message = "Household size must be at least 1")
    @Max(value = 20, message = "Household size cannot exceed 20")
    private Integer householdSize;
    
    @Min(value = 1, message = "Meals per day must be at least 1")
    @Max(value = 10, message = "Meals per day cannot exceed 10")
    private Integer mealsPerDay;
    
    private String subscriptionStatus;
    
    private LocalDateTime subscriptionExpiresAt;
    
    // Helper methods
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return username;
    }
}
