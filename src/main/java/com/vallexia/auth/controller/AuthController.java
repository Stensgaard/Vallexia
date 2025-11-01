package com.vallexia.auth.controller;

import com.vallexia.auth.dto.*;
import com.vallexia.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication and authorization endpoints")
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Constructor with dependency injection.
     * 
     * @param authService authentication service
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * Register a new user.
     * 
     * @param registerRequestDto registration request data
     * @param request HTTP request for audit logging
     * @return JWT response with tokens and user info
     */
    @Operation(
        summary = "Register a new user",
        description = "Create a new user account with email verification and return JWT tokens"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = JwtResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or user already exists"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    @PostMapping("/register")
    public ResponseEntity<JwtResponseDto> registerUser(
            @Parameter(description = "Registration data including username, email, and password")
            @Valid @RequestBody RegisterRequestDto registerRequestDto,
            HttpServletRequest request) {
        log.debug("Registration request received");
        
        JwtResponseDto response = authService.registerUser(registerRequestDto, request);
        
        log.info("User registered successfully with ID: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Authenticate user and return JWT tokens.
     * 
     * @param loginRequestDto login request data
     * @param request HTTP request for audit logging
     * @return JWT response with tokens and user info
     */
    @Operation(
        summary = "Authenticate user",
        description = "Login with username/email and password to receive JWT access and refresh tokens"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Authentication successful",
            content = @Content(schema = @Schema(implementation = JwtResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials or account locked"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> authenticateUser(
            @Parameter(description = "Login credentials (username/email and password)")
            @Valid @RequestBody LoginRequestDto loginRequestDto,
            HttpServletRequest request) {
        log.debug("Login request received");
        
        JwtResponseDto response = authService.authenticateUser(loginRequestDto, request);
        
        log.info("User authenticated successfully with ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Refresh JWT access token.
     * 
     * @param refreshTokenRequest refresh token request
     * @return new JWT response
     */
    @Operation(
        summary = "Refresh access token",
        description = "Use refresh token to obtain a new access token. Old refresh token will be invalidated (token rotation)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token refreshed successfully",
            content = @Content(schema = @Schema(implementation = JwtResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid or expired refresh token"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    @PostMapping("/refresh")
    public ResponseEntity<JwtResponseDto> refreshToken(
            @Parameter(description = "Valid refresh token")
            @Valid @RequestBody RefreshTokenRequestDto refreshTokenRequest) {
        log.debug("Token refresh request received");
        
        JwtResponseDto response = authService.refreshToken(refreshTokenRequest.getRefreshToken());
        
        log.info("Token refreshed successfully for user ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Logout user and blacklist tokens.
     * 
     * @param request HTTP request to extract tokens
     * @return no content (204)
     */
    @Operation(
        summary = "Logout user",
        description = "Invalidate the current access token by adding it to the blacklist. Requires authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Logout successful - no content"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - authentication required"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    @PostMapping("/logout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> logoutUser(
            @Parameter(hidden = true) HttpServletRequest request) {
        authService.logoutUser(request);
        log.info("User logged out successfully");
        return ResponseEntity.noContent().build();
    }
}
