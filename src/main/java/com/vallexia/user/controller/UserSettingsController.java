package com.vallexia.user.controller;

import com.vallexia.user.dto.UserSettingsDto;
import com.vallexia.user.service.UserSettingsService;
import com.vallexia.security.AuthenticationHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// TODO why is curreny not required but the rest is?
// TODO make it so the currency is required and must be one of the 
// supported currencies, make it so it says which is supported for them all

// TODO HEAD {{baseUrl}}/api/{{apiVersion}}/users/settings returns 200 OK why is that the case?
// should it not return 405 Method Not Allowed?

/**
 * REST controller for user settings management endpoints.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-15
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users/settings")
@Tag(
    name = "User Settings Management", 
    description = "Operations related to user settings management")
public class UserSettingsController {
    
    private final UserSettingsService userSettingsService;
    private final AuthenticationHelper authenticationHelper;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param userSettingsService the user settings service
     * @param authenticationHelper the authentication helper
     */
    public UserSettingsController(UserSettingsService userSettingsService, 
                                 AuthenticationHelper authenticationHelper) {
        this.userSettingsService = userSettingsService;
        this.authenticationHelper = authenticationHelper;
    }
    
    /**
     * Get current user's settings.
     * 
     * @param authentication current authentication
     * @return UserSettingsDto
     */
    @Operation(
        summary = "Get current user's settings", 
        description = "Retrieves the authenticated user's display preferences and settings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User settings retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserSettingsDto> getCurrentUserSettings(Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        log.info("Getting user settings for user ID: {}", userId);
        
        UserSettingsDto settings = userSettingsService.getUserSettings(userId);
        
        return ResponseEntity.ok(settings);
    }
    
    /**
     * Update current user's settings.
     * 
     * @param userSettingsDto updated settings data
     * @param authentication current authentication
     * @return updated UserSettingsDto
     */
    @Operation(
        summary = "Update current user's settings", 
        description = "Updates the authenticated user's display preferences and settings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User settings updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserSettingsDto> updateCurrentUserSettings(
            @Valid @RequestBody UserSettingsDto userSettingsDto,
            Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        log.info("Updating user settings for user ID: {}", userId);
        
        UserSettingsDto updatedSettings = userSettingsService.updateUserSettings(userId, userSettingsDto);
        
        return ResponseEntity.ok(updatedSettings);
    }
}
