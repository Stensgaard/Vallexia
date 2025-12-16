package com.vallexia.user.controller;

import com.vallexia.user.dto.DietaryPreferencesDto;
import com.vallexia.user.service.DietaryPreferencesService;
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

// TODO - does not look like the delete call is working, and im not sure what the point of it is
// should it just be removed? and merge it all with deleting the user and it delete it all

// TODO HEAD {{baseUrl}}/api/{{apiVersion}}/users/dietary-preferences returns 200 OK why is that the case?
// should it not return 405 Method Not Allowed?

// FIXME unsupported http calls should return 405 Method Not Allowed

/**
 * REST controller for dietary preferences management endpoints.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users/dietary-preferences")
@Tag(
    name = "Dietary Preferences Management", 
    description = "Operations related to dietary preferences management"
)
public class DietaryPreferencesController {
    
    private final DietaryPreferencesService dietaryPreferencesService;
    private final AuthenticationHelper authenticationHelper;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param dietaryPreferencesService the dietary preferences service
     * @param authenticationHelper the authentication helper
     */
    public DietaryPreferencesController(DietaryPreferencesService dietaryPreferencesService, 
                                        AuthenticationHelper authenticationHelper) {
        this.dietaryPreferencesService = dietaryPreferencesService;
        this.authenticationHelper = authenticationHelper;
    }
    
    /**
     * Get current user's dietary preferences.
     * 
     * @param authentication current authentication
     * @return DietaryPreferencesDto
     */
    @Operation(
        summary = "Get current user's dietary preferences", 
        description = "Retrieves the authenticated user's dietary preferences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dietary preferences retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<DietaryPreferencesDto> getCurrentUserDietaryPreferences(Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        log.info("Getting dietary preferences for user ID: {}", userId);
        
        DietaryPreferencesDto preferences = dietaryPreferencesService.getDietaryPreferences(userId);
        
        return ResponseEntity.ok(preferences);
    }
    
    /**
     * Update current user's dietary preferences.
     * 
     * @param dietaryPreferencesDto updated preferences data
     * @param authentication current authentication
     * @return updated DietaryPreferencesDto
     */
    @Operation(
        summary = "Update current user's dietary preferences", 
        description = "Updates the authenticated user's dietary preferences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dietary preferences updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<DietaryPreferencesDto> updateCurrentUserDietaryPreferences(
            @Valid @RequestBody DietaryPreferencesDto dietaryPreferencesDto,
            Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        log.info("Updating dietary preferences for user ID: {}", userId);
        
        DietaryPreferencesDto updatedPreferences = dietaryPreferencesService.updateDietaryPreferences(userId, dietaryPreferencesDto);
        
        return ResponseEntity.ok(updatedPreferences);
    }
    
    /**
     * Delete current user's dietary preferences.
     * 
     * @param authentication current authentication
     * @return no content
     */
    @Operation(
        summary = "Delete current user's dietary preferences", 
        description = "Deletes the authenticated user's dietary preferences (GDPR compliance)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Dietary preferences deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteCurrentUserDietaryPreferences(Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        log.info("Deleting dietary preferences for user ID: {}", userId);
        
        dietaryPreferencesService.deleteDietaryPreferences(userId);
        
        return ResponseEntity.noContent().build();
    }
}
