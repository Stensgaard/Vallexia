package com.vallexia.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vallexia.exception.ErrorCode;
import com.vallexia.exception.ErrorResponseDto;
import com.vallexia.exception.ErrorResponseMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT authentication entry point for handling unauthorized access.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {
    
    private final ErrorResponseMapper errorResponseMapper;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param errorResponseMapper the error response mapper
     * @param objectMapper the JSON object mapper
     */
    public AuthEntryPointJwt(ErrorResponseMapper errorResponseMapper, ObjectMapper objectMapper) {
        this.errorResponseMapper = errorResponseMapper;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        log.error("Unauthorized error: {}", authException.getMessage());
        
        String requestId = errorResponseMapper.generateRequestId();
        String path = request.getRequestURI();
        
        // Build error response using the standardized framework
        ErrorResponseDto error = errorResponseMapper.toAuthenticationErrorResponse(
                ErrorCode.AUTHENTICATION_ERROR,
                path,
                requestId
        );
        
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
