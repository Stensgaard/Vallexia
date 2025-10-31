package com.vallexia.auth.dto;

import com.vallexia.user.entity.MealType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Data Transfer Object for user registration request.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9_-]{3,20}$",
        message = "Username must be 3-20 characters and contain only letters, numbers, underscores, and dashes"
    )
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 50, message = "Email must not exceed 50 characters")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 40, message = "Password must be between 8 and 40 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain uppercase, lowercase, number, and special character"
    )
    private String password;
    
    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
    
    @Min(value = 1, message = "Household size must be at least 1")
    @Max(value = 20, message = "Household size cannot exceed 20")
    private Integer householdSize = 1;
    
    @NotEmpty(message = "At least one meal type must be selected")
    private Set<MealType> mealTypes = new HashSet<>(Set.of(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER));
}
