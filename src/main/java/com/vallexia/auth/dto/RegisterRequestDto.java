package com.vallexia.auth.dto;

import com.vallexia.common.validator.ValidCountry;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// BUG: testUser@example.com1765437293 is passing as an valid email, should be fixed
// FIXME: make validation errors more clear and helpful
/*
"username": "Username must be between 3 and 20 characters; 
Username must be 3-20 characters and 
contain only letters, numbers, underscores, and dashes"

saying this when username too short, long or invalid characters

when there is no password it says:
"password": "Password must be between 8 and 40 characters; 
Password must contain uppercase, lowercase, number, and 
special character; Password is required"
saying this when password too short, long or invalid characters

make is say password is required when no password like it does with confirm password
*/

/**
 * Data Transfer Object for user registration request.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
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

    @NotBlank(message = "Country is required")
    @ValidCountry(message = "Invalid country code")
    private String country;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 40, message = "Password must be between 8 and 40 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]*$",
        message = "Password must contain uppercase, lowercase, number, and special character"
    )
    private String password;
    
    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
}
