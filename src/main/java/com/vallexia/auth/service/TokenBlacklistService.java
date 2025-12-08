package com.vallexia.auth.service;

import com.vallexia.auth.exception.CryptographicException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing JWT token blacklisting using Redis.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-30
 */
@Slf4j
@Service
public class TokenBlacklistService {
    
    private static final String BLACKLIST_PREFIX = "blacklist:";
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * Add a token to the blacklist.
     * 
     * @param token JWT token to blacklist
     * @param expirationTime expiration time in milliseconds
     * @return true if token was successfully blacklisted, false otherwise
     */
    public boolean blacklistToken(String token, long expirationTime) {
        try {
            String hashedToken = hashToken(token);
            String key = BLACKLIST_PREFIX + hashedToken;
            long ttlSeconds = (expirationTime - System.currentTimeMillis()) / 1000;
            
            if (ttlSeconds > 0) {
                redisTemplate.opsForValue().set(key, "blacklisted", ttlSeconds, TimeUnit.SECONDS);
                log.info("Token blacklisted successfully. TTL: {} seconds", ttlSeconds);
                return true;
            } else {
                log.warn("Token already expired, not adding to blacklist");
                return false;
            }
        } catch (Exception e) {
            log.error("CRITICAL: Failed to blacklist token - {}. "
                + "Token blacklisting is a security-critical operation. Error: {}", 
                e.getClass().getSimpleName(), e.getMessage());
            // Return false to indicate failure, but don't throw exception
            // Callers can decide whether to throw based on their context
            return false;
        }
    }
    
    /**
     * Check if a token is blacklisted.
     * 
     * SECURITY NOTE: This method fails closed (returns true) when Redis is unavailable
     * to prevent blacklisted tokens from being accepted during outages.
     * 
     * @param token JWT token to check
     * @return true if token is blacklisted or if Redis is unavailable, false otherwise
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            String hashedToken = hashToken(token);
            String key = BLACKLIST_PREFIX + hashedToken;
            Boolean isBlacklisted = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(isBlacklisted);
        } catch (Exception e) {
            log.error("SECURITY ALERT: Error checking token blacklist - Redis unavailable. "
                    + "Failing closed (rejecting token) for security. Error: {}", e.getMessage());
            // Fail closed - reject tokens when we can't verify blacklist status
            // This prevents blacklisted tokens from working during Redis outages
            return true;
        }
    }
    
    /**
     * Hash token using SHA-256 to prevent token exposure in Redis keys.
     * If Redis is compromised, only hashes are visible, not actual tokens.
     * 
     * @param token JWT token to hash
     * @return hex-encoded SHA-256 hash of the token
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is standard, this should never happen
            throw new CryptographicException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * Convert byte array to hex string.
     * 
     * @param bytes byte array to convert
     * @return hex-encoded string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
