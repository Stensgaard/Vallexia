package com.vallexia.user.controller;

import com.vallexia.user.dto.UserProfileDto;
import com.vallexia.user.service.UserService;
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

// FIXME: make it a requirement to send username with then you update the profile, 
// and use this username to check instead of the email
// TODO make sure they only edit their own profile
// TODO make api tests once delete as been implemented


// TODO HEAD {{baseUrl}}/api/{{apiVersion}}/users/profile returns 200 OK why is that the case?
// should it not return 405 Method Not Allowed?

// FIXME: unsupported http calls should return 405 Method Not Allowed

/**
 * REST controller for user profile management endpoints.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users/profile")
@Tag(
    name = "User Profile Management", 
    description = "Operations related to user profile management")
public class UserController {
    
    private final UserService userService;
    private final AuthenticationHelper authenticationHelper;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param userService the user service
     * @param authenticationHelper the authentication helper
     */
    public UserController(UserService userService, AuthenticationHelper authenticationHelper) {
        this.userService = userService;
        this.authenticationHelper = authenticationHelper;
    }
    
    /**
     * Get current user profile.
     * 
     * @param authentication current authentication
     * @return UserProfileDto
     */
    @Operation(
        summary = "Get current user profile", 
        description = "Retrieves the authenticated user's profile information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileDto> getCurrentUserProfile(Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        log.info("Getting profile for user ID: {}", userId);
        
        UserProfileDto profile = userService.getUserProfile(userId);
        
        return ResponseEntity.ok(profile);
    }
    
    /**
     * Update current user profile.
     * 
     * @param userProfileDto updated profile data
     * @param authentication current authentication
     * @return updated UserProfileDto
     */
    @Operation(
        summary = "Update current user profile", 
        description = "Updates the authenticated user's profile information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileDto> updateCurrentUserProfile(
            @Valid @RequestBody UserProfileDto userProfileDto,
            Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        log.info("Updating profile for user ID: {}", userId);
        
        UserProfileDto updatedProfile = userService.updateUserProfile(userId, userProfileDto);
        
        return ResponseEntity.ok(updatedProfile);
    }
}
