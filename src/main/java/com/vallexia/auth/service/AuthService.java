package com.vallexia.auth.service;

import com.vallexia.auth.dto.*;
import com.vallexia.auth.mapper.AuthMapper;
import com.vallexia.user.entity.*;
import com.vallexia.user.entity.enums.Role;
import com.vallexia.user.repository.*;
import com.vallexia.user.service.DietaryPreferencesService;
import com.vallexia.nutrition.service.NutritionalGoalsService;
import com.vallexia.user.service.UserSettingsService;
import com.vallexia.audit.entity.enums.EventType;
import com.vallexia.audit.service.AuditService;
import com.vallexia.exception.ErrorCode;
import com.vallexia.exception.ValidationException;
import com.vallexia.auth.exception.UserAlreadyExistsException;
import com.vallexia.auth.exception.AuthenticationException;
import com.vallexia.auth.util.AccountSecurityHelper;
import com.vallexia.auth.util.UserAuthenticationHelper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Service for managing authentication operations including registration, login, token refresh, and logout.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-30
 */
@Slf4j
@Service
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final DietaryPreferencesService dietaryPreferencesService;
    private final NutritionalGoalsService nutritionalGoalsService;
    private final UserSettingsService userSettingsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final AuthMapper authMapper;
    private final JwtTokenService jwtTokenService;
    private final UserAuthenticationHelper userAuthenticationHelper;
    private final AccountSecurityHelper accountSecurityHelper;
    
    /**
     * Constructor with dependency injection.
     * 
     * @param userRepository user repository
     * @param passwordEncoder password encoder
     * @param auditService audit service
     * @param dietaryPreferencesService dietary preferences service
     * @param nutritionalGoalsService nutritional goals service
     * @param userSettingsService user settings service
     * @param tokenBlacklistService token blacklist service
     * @param authMapper mapper for auth DTOs and entities
     * @param jwtTokenService JWT token service
     * @param userAuthenticationHelper user authentication helper
     * @param accountSecurityHelper account security helper
     */
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService,
            DietaryPreferencesService dietaryPreferencesService,
            NutritionalGoalsService nutritionalGoalsService,
            UserSettingsService userSettingsService,
            TokenBlacklistService tokenBlacklistService,
            AuthMapper authMapper,
            JwtTokenService jwtTokenService,
            UserAuthenticationHelper userAuthenticationHelper,
            AccountSecurityHelper accountSecurityHelper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.dietaryPreferencesService = dietaryPreferencesService;
        this.nutritionalGoalsService = nutritionalGoalsService;
        this.userSettingsService = userSettingsService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.authMapper = authMapper;
        this.jwtTokenService = jwtTokenService;
        this.userAuthenticationHelper = userAuthenticationHelper;
        this.accountSecurityHelper = accountSecurityHelper;
    }
    
    /**
     * Register a new user with the provided information.
     * 
     * @param registerRequestDto registration request data
     * @param request HTTP request for audit logging
     * @return JWT response with tokens and user info
     * @throws ValidationException if validation fails
     * @throws UserAlreadyExistsException if user already exists
     */
    @Transactional
    public JwtResponseDto registerUser(RegisterRequestDto registerRequestDto, HttpServletRequest request) {
        log.debug("Registering new user");
        
        // Validate registration data
        validateRegistrationData(registerRequestDto);
        
        // Create and save new user with default settings
        User savedUser = createNewUser(registerRequestDto);
        
        // Generate JWT tokens
        JwtTokenService.JwtTokenData tokenData = jwtTokenService.generateTokens(savedUser);
        
        log.info("User registered successfully with ID: {}", savedUser.getId());
        
        // Log registration event
        auditService.logAuthenticationEvent(
            EventType.REGISTRATION,
            "User registered successfully",
            savedUser.getId(),
            savedUser.getUsername(),
            request,
            true
        );
        
        return authMapper.toJwtResponse(savedUser, tokenData.accessToken(), 
            tokenData.refreshToken(), tokenData.expiresAt());
    }
    
    /**
     * Validate registration data (password confirmation, username/email uniqueness).
     * 
     * @param registerRequestDto registration request data
     * @throws ValidationException if password confirmation doesn't match
     * @throws UserAlreadyExistsException if username or email already exists
     */
    private void validateRegistrationData(RegisterRequestDto registerRequestDto) {
        // Validate password confirmation
        if (!registerRequestDto.getPassword().equals(registerRequestDto.getConfirmPassword())) {
            throw new ValidationException("Password and confirmation password do not match");
        }
        
        // Check if username already exists
        if (userRepository.existsByUsername(registerRequestDto.getUsername())) {
            throw new UserAlreadyExistsException("Username is already taken");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(registerRequestDto.getEmail())) {
            throw new UserAlreadyExistsException("Email is already in use");
        }
    }
    
    /**
     * Create new user with default settings and preferences.
     * 
     * @param registerRequestDto registration request data
     * @return saved user entity
     */
    private User createNewUser(RegisterRequestDto registerRequestDto) {
        // Create new user using mapper
        User user = authMapper.toUser(registerRequestDto);
        user.setPasswordHash(passwordEncoder.encode(registerRequestDto.getPassword()));
        user.addRole(Role.USER);
        
        // Save user
        User savedUser = userRepository.save(user);
        
        // Create default dietary preferences and nutritional goals
        dietaryPreferencesService.createDefaultPreferences(savedUser);
        nutritionalGoalsService.createDefaultGoals(savedUser);
        
        // Create default user settings with country-specific defaults
        userSettingsService.createDefaultSettings(savedUser, registerRequestDto.getCountry());
        
        return savedUser;
    }
    
    /**
     * Authenticate user and return JWT tokens.
     * 
     * @param loginRequestDto login request data
     * @param request HTTP request for audit logging
     * @return JWT response with tokens and user info
     * @throws AuthenticationException if authentication fails
     * @throws AccountLockedException if account is locked
     * @throws AccountDisabledException if account is disabled
     */
    @Transactional
    public JwtResponseDto authenticateUser(LoginRequestDto loginRequestDto, HttpServletRequest request) {
        log.debug("Authenticating user");
        
        // Find user by username or email (try username first as it's immutable)
        User user = userAuthenticationHelper.findUserByUsernameOrEmail(loginRequestDto.getUsernameOrEmail())
                .orElse(null);
        
        // If user doesn't exist, log failed attempt and throw exception
        if (user == null) {
            auditService.logAuthenticationEvent(
                EventType.LOGIN_FAILURE,
                "Failed login attempt - user not found",
                null,
                loginRequestDto.getUsernameOrEmail(),
                request,
                false
            );
            throw new AuthenticationException(ErrorCode.INVALID_CREDENTIALS, 
                "Invalid username/email or password");
        }
        
        // Validate account status (disabled, locked)
        userAuthenticationHelper.validateAccountStatus(user);
        
        // Verify password
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPasswordHash())) {
            accountSecurityHelper.handleFailedLoginAttempt(user);
            // Log failed login attempt
            auditService.logAuthenticationEvent(
                EventType.LOGIN_FAILURE,
                "Failed login attempt - invalid password",
                user.getId(),
                user.getUsername(),
                request,
                false
            );
            throw new AuthenticationException(ErrorCode.INVALID_CREDENTIALS, "Invalid username/email or password");
        }
        
        // Reset failed login attempts on successful login
        accountSecurityHelper.resetFailedLoginAttempts(user);
        
        // Generate JWT tokens
        JwtTokenService.JwtTokenData tokenData = jwtTokenService.generateTokens(user);
        
        log.debug("User authenticated successfully");
        
        // Log successful login
        auditService.logAuthenticationEvent(
            EventType.LOGIN_SUCCESS,
            "User logged in successfully",
            user.getId(),
            user.getUsername(),
            request,
            true
        );
        
        return authMapper.toJwtResponse(user, tokenData.accessToken(), 
            tokenData.refreshToken(), tokenData.expiresAt());
    }
    
    /**
     * Refresh JWT access token using refresh token.
     * Implements token rotation by blacklisting the old refresh token.
     * 
     * @param refreshToken refresh token
     * @return new JWT response
     * @throws AuthenticationException if refresh token is invalid
     * @throws AccountLockedException if account is locked
     * @throws AccountDisabledException if account is disabled
     */
    @Transactional(readOnly = true)
    public JwtResponseDto refreshToken(String refreshToken) {
        // Validate token structure and signature first (handles null/empty/invalid)
        if (!jwtTokenService.isValidToken(refreshToken)) {
            throw new AuthenticationException(ErrorCode.INVALID_TOKEN, "Invalid refresh token");
        }
        
        // Check if token is expired (only for valid tokens)
        if (jwtTokenService.isTokenExpired(refreshToken)) {
            throw new AuthenticationException(ErrorCode.TOKEN_EXPIRED, "Refresh token has expired");
        }
        
        // Check if refresh token is already blacklisted
        if (tokenBlacklistService.isTokenBlacklisted(refreshToken)) {
            log.warn("Attempted to use blacklisted refresh token");
            throw new AuthenticationException(ErrorCode.INVALID_TOKEN, "Refresh token has been revoked");
        }
        
        String username = jwtTokenService.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsernameAndEnabledTrue(username)
                .orElseThrow(() -> new AuthenticationException(ErrorCode.INVALID_TOKEN, "User not found"));
        
        // Validate account status (disabled, locked)
        userAuthenticationHelper.validateAccountStatus(user);
        
        // Generate new JWT tokens
        JwtTokenService.JwtTokenData tokenData = jwtTokenService.generateTokens(user);
        
        // Blacklist the old refresh token to prevent reuse (token rotation)
        long oldTokenExpiration = jwtTokenService.getTokenExpirationTime(refreshToken);
        boolean blacklisted = tokenBlacklistService.blacklistToken(refreshToken, oldTokenExpiration);
        if (!blacklisted) {
            log.error("CRITICAL: Failed to blacklist old refresh token during token rotation");
            throw new AuthenticationException("Token refresh failed due to security error. Please log in again.");
        }
        
        log.debug("Old refresh token blacklisted successfully");
        
        // Log token refresh event
        auditService.logEvent(
            EventType.TOKEN_REFRESH,
            user.getId(),
            String.format("Token refreshed for user %s", user.getUsername())
        );
        
        return authMapper.toJwtResponse(user, tokenData.accessToken(), 
            tokenData.refreshToken(), tokenData.expiresAt());
    }
    
    /**
     * Logout user and blacklist access token.
     * 
     * @param request HTTP request to extract tokens
     */
    public void logoutUser(HttpServletRequest request) {
        log.debug("Processing logout request");
        
        try {
            String accessToken = jwtTokenService.parseJwtFromRequest(request);
            if (!jwtTokenService.isValidToken(accessToken)) {
                log.warn("No valid access token found in logout request");
                return;
            }
            
            // Extract user info from token before blacklisting
            String username = jwtTokenService.getUsernameFromToken(accessToken);
            User user = userRepository.findByUsernameAndEnabledTrue(username).orElse(null);
            Long userId = user != null ? user.getId() : null;
            
            // Get token expiration time
            long expirationTime = jwtTokenService.getTokenExpirationTime(accessToken);
            
            // Blacklist the access token
            boolean blacklisted = tokenBlacklistService.blacklistToken(accessToken, expirationTime);
            if (blacklisted) {
                log.debug("Access token blacklisted successfully");
                // Log logout event
                auditService.logAuthenticationEvent(
                    EventType.LOGOUT,
                    "User logged out successfully",
                    userId,
                    username,
                    request,
                    true
                );
            } else {
                log.warn("Failed to blacklist access token during logout.");
            }
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            // Don't throw exception - logout should always succeed from user perspective
        }
    }
}
