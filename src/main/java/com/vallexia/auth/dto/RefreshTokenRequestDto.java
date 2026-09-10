package com.vallexia.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for refresh token request.
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequestDto {
    
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
