package com.vallexia.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vallexia.auth.service.TokenBlacklistService;
import com.vallexia.exception.ErrorCode;
import com.vallexia.exception.ErrorResponseDto;
import com.vallexia.exception.ErrorResponseMapper;
import com.vallexia.security.util.JwtUtils;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT authentication filter for processing JWT tokens in requests.
 * Authenticates users directly from token claims (userId and roles) without database lookups.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {
    
    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;
    private final ErrorResponseMapper errorResponseMapper;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor with dependency injection.
     * 
     * @param jwtUtils JWT utility for token operations
     * @param tokenBlacklistService service for checking token blacklist
     * @param errorResponseMapper mapper for standardized error responses
     * @param objectMapper JSON object mapper
     */
    public AuthTokenFilter(JwtUtils jwtUtils, TokenBlacklistService tokenBlacklistService,
                          ErrorResponseMapper errorResponseMapper, ObjectMapper objectMapper) {
        this.jwtUtils = jwtUtils;
        this.tokenBlacklistService = tokenBlacklistService;
        this.errorResponseMapper = errorResponseMapper;
        this.objectMapper = objectMapper;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                if (!processTokenAuthentication(jwt, request, response)) {
                    return;
                }
            }
        } catch (JwtException e) {
            log.error("JWT validation failed: {}", e.getMessage());
            // Don't set authentication, let downstream handle unauthorized access
        } catch (IllegalArgumentException e) {
            // This catch handles IllegalArgumentException from parseJwt or other token parsing issues
            // Username extraction failures are handled above with error response
            log.error("Invalid token parameter: {}", e.getMessage());
            // Don't set authentication, let downstream handle unauthorized access
        } catch (Exception e) {
            log.error("Unexpected error during authentication: {}", e.getMessage());
            // Re-throw unexpected errors to avoid silently failing
            throw new ServletException("Authentication processing failed", e);
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Process token authentication and set security context.
     * 
     * @param jwt JWT token
     * @param request HTTP request
     * @param response HTTP response
     * @return true if authentication succeeded, false if error response was sent
     * @throws IOException if error response writing fails
     */
    private boolean processTokenAuthentication(String jwt, HttpServletRequest request, 
                                              HttpServletResponse response) throws IOException {
        if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
            log.warn("Blacklisted token attempted to be used");
            sendErrorResponse(response, request, ErrorCode.INVALID_TOKEN);
            return false;
        }
        
        String username = extractUsername(jwt, request, response);
        if (username == null) {
            return false;
        }
        
        UserClaims userClaims = extractUserClaims(jwt, username, request, response);
        if (userClaims == null) {
            return false;
        }
        
        setAuthentication(userClaims, request);
        log.debug("User {} authenticated via token claims", username);
        return true;
    }
    
    /**
     * Extract username from JWT token.
     * 
     * @param jwt JWT token
     * @param request HTTP request
     * @param response HTTP response
     * @return username or null if extraction failed (error response sent)
     * @throws IOException if error response writing fails
     */
    private String extractUsername(String jwt, HttpServletRequest request, 
                                  HttpServletResponse response) throws IOException {
        String username;
        try {
            username = jwtUtils.getUsernameFromJwtToken(jwt);
        } catch (IllegalArgumentException e) {
            log.error("Invalid token: failed to extract username - {}", e.getMessage());
            sendErrorResponse(response, request, ErrorCode.INVALID_TOKEN);
            return null;
        }
        
        if (username == null || username.trim().isEmpty()) {
            log.error("Invalid token: missing or empty username claim");
            sendErrorResponse(response, request, ErrorCode.INVALID_TOKEN);
            return null;
        }
        
        return username;
    }
    
    /**
     * Extract user ID and roles from JWT token.
     * 
     * @param jwt JWT token
     * @param username username for logging
     * @param request HTTP request
     * @param response HTTP response
     * @return UserClaims or null if extraction failed (error response sent)
     * @throws IOException if error response writing fails
     */
    private UserClaims extractUserClaims(String jwt, String username, HttpServletRequest request,
                                        HttpServletResponse response) throws IOException {
        Long userId;
        List<String> roles;
        try {
            userId = jwtUtils.getUserIdFromJwtToken(jwt);
            roles = jwtUtils.getRolesFromJwtToken(jwt);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Invalid token for user {}: failed to extract claims - {}", 
                username != null ? username : "unknown", e.getMessage());
            sendErrorResponse(response, request, ErrorCode.INVALID_TOKEN);
            return null;
        }
        
        return new UserClaims(userId, username, roles);
    }
    
    /**
     * Set authentication in security context.
     * 
     * @param userClaims user claims from token
     * @param request HTTP request
     */
    private void setAuthentication(UserClaims userClaims, HttpServletRequest request) {
        UserPrincipal userPrincipal = UserPrincipal.createFromJwtClaims(
            userClaims.userId, userClaims.username, userClaims.roles);
        
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    /**
     * Helper class to hold user claims extracted from JWT token.
     */
    private static class UserClaims {
        final Long userId;
        final String username;
        final List<String> roles;
        
        UserClaims(Long userId, String username, List<String> roles) {
            this.userId = userId;
            this.username = username;
            this.roles = roles;
        }
    }
    
    /**
     * Parse JWT token from Authorization header.
     * 
     * @param request HTTP request
     * @return JWT token string
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        
        return null;
    }
    
    /**
     * Send standardized error response using ErrorResponseMapper.
     * 
     * @param response HTTP response
     * @param request HTTP request
     * @param errorCode error code to use
     * @throws IOException if response writing fails
     */
    private void sendErrorResponse(HttpServletResponse response, HttpServletRequest request, 
                                   ErrorCode errorCode) throws IOException {
        String requestId = errorResponseMapper.generateRequestId();
        String path = request.getRequestURI();
        
        ErrorResponseDto error = errorResponseMapper.toAuthenticationErrorResponse(
                errorCode,
                path,
                requestId
        );
        
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
