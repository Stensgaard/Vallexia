package com.vallexia.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for JWT response containing tokens and user info.
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponseDto {
    
    private String accessToken;
    private String refreshToken;
    
    @Builder.Default
    private String tokenType = "Bearer";
    
    private Long id;
    private String username;
    private String email;
    private LocalDateTime expiresAt;
    private String subscriptionStatus;
}
