package com.vallexia.user.controller;

import com.vallexia.user.dto.NutritionalGoalsDto;
import com.vallexia.user.service.NutritionalGoalsService;
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

/**
 * REST controller for nutritional goals management endpoints.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users/nutritional-goals")
@Tag(
    name = "Nutritional Goals Management", 
    description = "Operations related to nutritional goals management")
public class NutritionalGoalsController {
    
    private final NutritionalGoalsService nutritionalGoalsService;
    private final AuthenticationHelper authenticationHelper;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param nutritionalGoalsService the nutritional goals service
     * @param authenticationHelper the authentication helper
     */
    public NutritionalGoalsController(NutritionalGoalsService nutritionalGoalsService,
                                      AuthenticationHelper authenticationHelper) {
        this.nutritionalGoalsService = nutritionalGoalsService;
        this.authenticationHelper = authenticationHelper;
    }
    
    /**
     * Get current user's nutritional goals.
     * 
     * @param authentication current authentication
     * @return NutritionalGoalsDto
     */
    @Operation(
        summary = "Get current user's nutritional goals", 
        description = "Retrieves the authenticated user's nutritional goals")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Nutritional goals retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<NutritionalGoalsDto> getCurrentUserNutritionalGoals(Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        log.info("Getting nutritional goals for user ID: {}", userId);
        
        NutritionalGoalsDto goals = nutritionalGoalsService.getNutritionalGoals(userId);
        
        return ResponseEntity.ok(goals);
    }
    
    /**
     * Update current user's nutritional goals.
     * 
     * @param nutritionalGoalsDto updated goals data
     * @param authentication current authentication
     * @return updated NutritionalGoalsDto
     */
    @Operation(
        summary = "Update current user's nutritional goals", 
        description = "Updates the authenticated user's nutritional goals")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Nutritional goals updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<NutritionalGoalsDto> updateCurrentUserNutritionalGoals(
            @Valid @RequestBody NutritionalGoalsDto nutritionalGoalsDto,
            Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        log.info("Updating nutritional goals for user ID: {}", userId);
        
        NutritionalGoalsDto updatedGoals = nutritionalGoalsService.updateNutritionalGoals(userId, nutritionalGoalsDto);
        
        return ResponseEntity.ok(updatedGoals);
    }
    
    /**
     * Delete current user's nutritional goals.
     * 
     * @param authentication current authentication
     * @return no content
     */
    @Operation(
        summary = "Delete current user's nutritional goals", 
        description = "Deletes the authenticated user's nutritional goals (GDPR compliance)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Nutritional goals deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteCurrentUserNutritionalGoals(Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        log.info("Deleting nutritional goals for user ID: {}", userId);
        
        nutritionalGoalsService.deleteNutritionalGoals(userId);
        
        return ResponseEntity.noContent().build();
    }
}
