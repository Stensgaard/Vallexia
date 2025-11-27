package com.vallexia.auth.service;

import com.vallexia.security.util.JwtUtils;
import com.vallexia.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for JWT token generation and parsing operations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-30
 */
@Slf4j
@Service
public class JwtTokenService {
    
    private final JwtUtils jwtUtils;
    
    /**
     * Constructor with dependency injection.
     * 
     * @param jwtUtils JWT utility for token operations
     */
    public JwtTokenService(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }
    
    /**
     * Record class for JWT token data.
     */
    public record JwtTokenData(String accessToken, String refreshToken, LocalDateTime expiresAt) {}
    
    /**
     * Generate JWT tokens for user with roles.
     * 
     * @param user the user to generate tokens for
     * @return JWT token data (access token, refresh token, expiration time)
     */
    public JwtTokenData generateTokens(User user) {
        List<String> roles = extractUserRoles(user);
        
        String accessToken = jwtUtils.generateAccessToken(user.getUsername(), user.getId(), roles);
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername(), user.getId(), roles);
        Date expDate = jwtUtils.getExpirationDateFromToken(accessToken);
        LocalDateTime expiresAt = (expDate != null)
            ? expDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
            : LocalDateTime.now();
        
        return new JwtTokenData(accessToken, refreshToken, expiresAt);
    }
    
    /**
     * Parse JWT token from Authorization header.
     * Consistent with AuthTokenFilter.parseJwt() pattern.
     * 
     * @param request HTTP request
     * @return JWT token string, or null if not found or invalid format
     */
    public String parseJwtFromRequest(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7);
            // Return null if token is empty after Bearer prefix
            return StringUtils.hasText(token) ? token : null;
        }
        
        return null;
    }
    
    /**
     * Validate JWT token structure and signature.
     * 
     * @param token JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean isValidToken(String token) {
        return token != null && !token.trim().isEmpty() && jwtUtils.validateJwtToken(token);
    }
    
    /**
     * Check if token is expired.
     * 
     * @param token JWT token to check
     * @return true if token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        return jwtUtils.isTokenExpired(token);
    }
    
    /**
     * Get token expiration time in milliseconds.
     * 
     * @param token JWT token
     * @return expiration time in milliseconds
     */
    public long getTokenExpirationTime(String token) {
        Date expDate = jwtUtils.getExpirationDateFromToken(token);
        return expDate != null ? expDate.getTime() : 0;
    }
    
    /**
     * Extract username from JWT token.
     * 
     * @param token JWT token
     * @return username from token
     */
    public String getUsernameFromToken(String token) {
        return jwtUtils.getUsernameFromJwtToken(token);
    }
    
    /**
     * Extract user roles as a list of authority strings.
     * 
     * @param user the user entity
     * @return list of role authority strings
     */
    private List<String> extractUserRoles(User user) {
        return user.getRoles().stream()
                .map(role -> role.getAuthority())
                .collect(Collectors.toList());
    }
}
