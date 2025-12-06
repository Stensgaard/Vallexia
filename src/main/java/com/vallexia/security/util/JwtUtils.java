package com.vallexia.security.util;

import com.vallexia.config.security.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JWT utility class for token generation and validation.
 * All tokens include userId and roles claims for stateless authentication.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@Slf4j
@Component
public class JwtUtils {
    
    private final JwtProperties jwtProperties;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param jwtProperties JWT configuration properties
     */
    public JwtUtils(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }
    
    /**
     * Get the secret key for JWT signing and verification.
     * 
     * @return SecretKey instance
     * @throws IllegalStateException if JWT secret is not configured (null)
     */
    private SecretKey getSecretKey() {
        String secret = jwtProperties.getSecret();
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret is not configured. Please set app.jwt.secret property.");
        }
        
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Validate inputs for token generation.
     * Ensures all required parameters are present and valid.
     * 
     * @param username the username to validate
     * @param userId the user ID to validate
     * @param roles the roles list to validate
     * @throws IllegalArgumentException if any input is invalid (null or empty)
     */
    private void validateTokenGenerationInputs(String username, Long userId, List<String> roles) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("Roles cannot be null or empty");
        }
    }
    
    /**
     * Create token claims map with userId and roles.
     * 
     * @param userId the user ID to include in claims
     * @param roles the roles list to include in claims
     * @return map containing userId and roles claims
     */
    private Map<String, Object> createTokenClaims(Long userId, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roles", roles);
        return claims;
    }
    
    /**
     * Parse JWT token and extract claims.
     * Common helper method for token parsing operations.
     * 
     * @param token JWT token to parse
     * @return Claims object if parsing succeeds, null otherwise
     */
    private Claims parseTokenClaims(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        
        try {
            SecretKey key = getSecretKey();
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token parsing failed: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error parsing token: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Generate access token for user with claims (userId and roles).
     * All tokens must include userId and roles claims.
     * 
     * @param username the username (must not be null or empty)
     * @param userId the user ID (must not be null)
     * @param roles the user roles (must not be null or empty)
     * @return JWT access token
     * @throws IllegalArgumentException if username is null/empty, userId is null, or roles is null/empty
     */
    public String generateAccessToken(String username, Long userId, List<String> roles) {
        validateTokenGenerationInputs(username, userId, roles);
        Map<String, Object> claims = createTokenClaims(userId, roles);
        return generateTokenWithClaims(username, claims, jwtProperties.getAccessTokenExpiration());
    }
    
    /**
     * Generate refresh token for user with claims (userId and roles).
     * All tokens must include userId and roles claims.
     * 
     * @param username the username (must not be null or empty)
     * @param userId the user ID (must not be null)
     * @param roles the user roles (must not be null or empty)
     * @return JWT refresh token
     * @throws IllegalArgumentException if username is null/empty, userId is null, or roles is null/empty
     */
    public String generateRefreshToken(String username, Long userId, List<String> roles) {
        validateTokenGenerationInputs(username, userId, roles);
        Map<String, Object> claims = createTokenClaims(userId, roles);
        return generateTokenWithClaims(username, claims, jwtProperties.getRefreshTokenExpiration());
    }
    
    /**
     * Generate JWT token with custom claims.
     * 
     * @param username the username
     * @param claims custom claims to include
     * @param expirationMs expiration time in milliseconds (long to prevent integer overflow)
     * @return JWT token
     */
    private String generateTokenWithClaims(String username, Map<String, Object> claims, long expirationMs) {
        SecretKey key = getSecretKey();
        Date now = new Date();
        
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }
    
    /**
     * Get username from JWT token.
     * 
     * @param token JWT token
     * @return username
     * @throws IllegalArgumentException if token is null or empty
     */
    public String getUsernameFromJwtToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        
        SecretKey key = getSecretKey();
        
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
    
    /**
     * Get user ID from JWT token.
     * 
     * @param token JWT token
     * @return user ID
     * @throws IllegalArgumentException if token is null/empty or token parsing failed
     * @throws IllegalStateException if userId claim is missing or invalid
     */
    public Long getUserIdFromJwtToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        
        Claims claims = parseTokenClaims(token);
        if (claims == null) {
            throw new IllegalArgumentException("Failed to parse token claims");
        }
        
        Object userId = claims.get("userId");
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        
        throw new IllegalStateException("User ID claim is missing or invalid in token");
    }
    
    /**
     * Get roles from JWT token.
     * 
     * @param token JWT token
     * @return list of roles
     * @throws IllegalArgumentException if token is null/empty or token parsing failed
     * @throws IllegalStateException if roles claim is missing or invalid
     */
    public List<String> getRolesFromJwtToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        
        Claims claims = parseTokenClaims(token);
        if (claims == null) {
            throw new IllegalArgumentException("Failed to parse token claims");
        }
        
        Object roles = claims.get("roles");
        if (roles instanceof List) {
            List<?> rolesList = (List<?>) roles;
            List<String> result = rolesList.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .collect(Collectors.toList());
            if (result.isEmpty()) {
                throw new IllegalStateException("Roles claim is empty in token");
            }
            
            return result;
        }
        
        throw new IllegalStateException("Roles claim is missing or invalid in token");
    }
    
    /**
     * Validate JWT token.
     * 
     * @param authToken JWT token
     * @return true if token is valid, false otherwise
     */
    public boolean validateJwtToken(String authToken) {
        if (authToken == null || authToken.trim().isEmpty()) {
            return false;
        }
        
        try {
            SecretKey key = getSecretKey();
            Jwts.parser().verifyWith(key).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("Unexpected error during JWT validation: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if token is expired.
     * 
     * @param token JWT token
     * @return true if token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        Claims claims = parseTokenClaims(token);
        if (claims == null) {
            return true;
        }
        
        Date expiration = claims.getExpiration();
        return expiration != null && expiration.before(new Date());
    }
    
    /**
     * Get token expiration date.
     * 
     * @param token JWT token
     * @return expiration date
     * @throws IllegalArgumentException if token is null/empty or token parsing failed
     * @throws IllegalStateException if expiration claim is missing
     */
    public Date getExpirationDateFromToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        
        Claims claims = parseTokenClaims(token);
        if (claims == null) {
            throw new IllegalArgumentException("Failed to parse token claims");
        }
        
        Date expiration = claims.getExpiration();
        if (expiration == null) {
            throw new IllegalStateException("Expiration claim is missing in token");
        }
        
        return expiration;
    }
}
