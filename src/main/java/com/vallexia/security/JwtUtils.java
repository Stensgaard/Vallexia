package com.vallexia.security;

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

/**
 * JWT utility class for token generation and validation.
 * All tokens include userId and roles claims for stateless authentication.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
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
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Generate access token for user with claims (userId and roles).
     * All tokens must include userId and roles claims.
     * 
     * @param username the username
     * @param userId the user ID
     * @param roles the user roles
     * @return JWT access token
     */
    public String generateAccessToken(String username, Long userId, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roles", roles);
        return generateTokenWithClaims(username, claims, (int) jwtProperties.getAccessTokenExpiration());
    }
    
    /**
     * Generate refresh token for user with claims (userId and roles).
     * All tokens must include userId and roles claims.
     * 
     * @param username the username
     * @param userId the user ID
     * @param roles the user roles
     * @return JWT refresh token
     */
    public String generateRefreshToken(String username, Long userId, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roles", roles);
        return generateTokenWithClaims(username, claims, (int) jwtProperties.getRefreshTokenExpiration());
    }
    
    /**
     * Generate JWT token with custom claims.
     * 
     * @param username the username
     * @param claims custom claims to include
     * @param expirationMs expiration time in milliseconds
     * @return JWT token
     */
    private String generateTokenWithClaims(String username, Map<String, Object> claims, int expirationMs) {
        SecretKey key = getSecretKey();
        
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + expirationMs))
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
     * @return user ID or null if not present
     */
    public Long getUserIdFromJwtToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        
        try {
            SecretKey key = getSecretKey();
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            Object userId = claims.get("userId");
            if (userId instanceof Number) {
                return ((Number) userId).longValue();
            }
            return null;
        } catch (Exception e) {
            log.error("Error extracting user ID from token: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Get roles from JWT token.
     * 
     * @param token JWT token
     * @return list of roles or null if not present
     */
    public List<String> getRolesFromJwtToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        
        try {
            SecretKey key = getSecretKey();
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            Object roles = claims.get("roles");
            if (roles instanceof List) {
                List<?> rolesList = (List<?>) roles;
                return rolesList.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .collect(java.util.stream.Collectors.toList());
            }
            return null;
        } catch (Exception e) {
            log.error("Error extracting roles from token: {}", e.getMessage());
            return null;
        }
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
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("JWT validation failed: {}", e.getClass().getSimpleName());
            } else {
                log.warn("JWT validation failed");
            }
        }
        
        return false;
    }
    
    /**
     * Check if token is expired.
     * 
     * @param token JWT token
     * @return true if token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        if (token == null || token.trim().isEmpty()) {
            return true;
        }
        
        try {
            SecretKey key = getSecretKey();
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * Get token expiration date.
     * 
     * @param token JWT token
     * @return expiration date
     * @throws IllegalArgumentException if token is null or empty
     */
    public Date getExpirationDateFromToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        
        SecretKey key = getSecretKey();
        
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }
}
