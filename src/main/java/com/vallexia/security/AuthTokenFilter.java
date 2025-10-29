package com.vallexia.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vallexia.auth.service.TokenBlacklistService;
import com.vallexia.exception.ErrorCode;
import com.vallexia.exception.ErrorResponseDto;
import com.vallexia.exception.ErrorResponseMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT authentication filter for processing JWT tokens in requests.
 * Authenticates users directly from token claims (userId and roles) without database lookups.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
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
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                // Check if token is blacklisted
                if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                    log.warn("Blacklisted token attempted to be used");
                    sendErrorResponse(response, request, ErrorCode.INVALID_TOKEN);
                    return;
                }
                
                // Extract user information from token claims
                String username = jwtUtils.getUsernameFromJwtToken(jwt);
                Long userId = jwtUtils.getUserIdFromJwtToken(jwt);
                List<String> roles = jwtUtils.getRolesFromJwtToken(jwt);
                
                // Validate that required claims are present
                if (userId == null || roles == null || roles.isEmpty()) {
                    log.error("Invalid token for user {}: missing required claims (userId or roles)", username);
                    sendErrorResponse(response, request, ErrorCode.INVALID_TOKEN);
                    return;
                }
                
                // Create authorities from token claims
                List<GrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                
                // Create UserDetails directly from token claims (no database lookup)
                UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                        username, "", authorities);
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("User {} authenticated via token claims", username);
            }
        } catch (JwtException e) {
            log.error("JWT validation failed: {}", e.getMessage());
            // Don't set authentication, let downstream handle unauthorized access
        } catch (IllegalArgumentException e) {
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
