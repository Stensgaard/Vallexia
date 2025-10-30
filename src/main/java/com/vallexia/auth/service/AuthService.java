package com.vallexia.auth.service;

import com.vallexia.auth.dto.*;
import com.vallexia.auth.mapper.AuthMapper;
import com.vallexia.config.security.AccountSecurityProperties;
import com.vallexia.user.entity.*;
import com.vallexia.user.repository.*;
import com.vallexia.user.service.DietaryPreferencesService;
import com.vallexia.user.service.NutritionalGoalsService;
import com.vallexia.audit.entity.EventType;
import com.vallexia.audit.service.AuditService;
import com.vallexia.exception.ValidationException;
import com.vallexia.auth.exception.UserAlreadyExistsException;
import com.vallexia.auth.exception.AuthenticationException;
import com.vallexia.auth.exception.AccountLockedException;
import com.vallexia.auth.exception.AccountDisabledException;
import com.vallexia.security.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Date;

/**
 * Service for managing authentication operations including registration, login, token refresh, and logout.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuditService auditService;
    private final DietaryPreferencesService dietaryPreferencesService;
    private final NutritionalGoalsService nutritionalGoalsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final AccountSecurityProperties accountSecurityProperties;
    private final AuthMapper authMapper;
    
    /**
     * Constructor with dependency injection.
     * 
     * @param userRepository user repository
     * @param passwordEncoder password encoder
     * @param jwtUtils JWT utility
     * @param auditService audit service
     * @param dietaryPreferencesService dietary preferences service
     * @param nutritionalGoalsService nutritional goals service
     * @param tokenBlacklistService token blacklist service
     * @param accountSecurityProperties account security configuration
     * @param authMapper mapper for auth DTOs and entities
     */
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils,
            AuditService auditService,
            DietaryPreferencesService dietaryPreferencesService,
            NutritionalGoalsService nutritionalGoalsService,
            TokenBlacklistService tokenBlacklistService,
            AccountSecurityProperties accountSecurityProperties,
            AuthMapper authMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.auditService = auditService;
        this.dietaryPreferencesService = dietaryPreferencesService;
        this.nutritionalGoalsService = nutritionalGoalsService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.accountSecurityProperties = accountSecurityProperties;
        this.authMapper = authMapper;
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
        
        // Create new user using mapper
        User user = authMapper.toUser(registerRequestDto);
        user.setPasswordHash(passwordEncoder.encode(registerRequestDto.getPassword()));
        user.addRole(Role.USER);
        
        // Save user
        User savedUser = userRepository.save(user);
        
        // Create default dietary preferences and nutritional goals
        dietaryPreferencesService.createDefaultPreferences(savedUser);
        nutritionalGoalsService.createDefaultGoals(savedUser);
        
        // Prepare user roles for JWT claims
        List<String> roles = extractUserRoles(savedUser);
        
        // Generate JWT tokens with user ID and roles to reduce database lookups
        String accessToken = jwtUtils.generateAccessToken(savedUser.getUsername(), savedUser.getId(), roles);
        String refreshToken = jwtUtils.generateRefreshToken(savedUser.getUsername(), savedUser.getId(), roles);
        Date expDate = jwtUtils.getExpirationDateFromToken(accessToken);
        LocalDateTime expiresAt = (expDate != null)
            ? expDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
            : LocalDateTime.now();
        
        log.info("User registered successfully with ID: {}", savedUser.getId());
        
        // Log registration event
        auditService.logAuthenticationEvent(
            EventType.REGISTRATION,
            "User registered successfully",
            savedUser.getId(),
            savedUser.getUsername(),
            request, // Pass the actual request for audit context
            true
        );
        
        return authMapper.toJwtResponse(savedUser, accessToken, refreshToken, expiresAt);
    }
    
    /**
     * Authenticate user and return JWT tokens.
     * 
     * @param loginRequestDto login request data
     * @param request HTTP request for audit logging
     * @return JWT response with tokens and user info
     * @throws AuthenticationException if authentication fails
     */
    @Transactional
    public JwtResponseDto authenticateUser(LoginRequestDto loginRequestDto, HttpServletRequest request) {
        log.debug("Authenticating user");
        
        // Find user by username or email
        Optional<User> userOpt = userRepository.findByUsernameAndEnabledTrue(loginRequestDto.getUsernameOrEmail());
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmailAndEnabledTrue(loginRequestDto.getUsernameOrEmail());
        }
        
        if (userOpt.isEmpty()) {
            throw new AuthenticationException("Invalid username/email or password");
        }
        
        User user = userOpt.get();
        
        // Check if account is locked
        if (user.isAccountLocked()) {
            log.warn("Account locked until {}", user.getAccountLockedUntil());
            throw new AccountLockedException("Account is temporarily locked due to multiple failed login attempts");
        }
        
        // Verify password
        // Note: Account disabled check is handled by repository queries (findByUsernameAndEnabledTrue/findByEmailAndEnabledTrue)
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPasswordHash())) {
            // Increment failed login attempts
            user.incrementFailedLoginAttempts();
            
            // Lock account after configured number of failed attempts
            int maxAttempts = accountSecurityProperties.getMaxFailedAttempts();
            int lockoutMinutes = accountSecurityProperties.getDurationMinutes();
            
            if (user.getFailedLoginAttempts() >= maxAttempts) {
                user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(lockoutMinutes));
                log.warn("Account locked for {} minutes due to {} failed attempts", 
                    lockoutMinutes, user.getFailedLoginAttempts());
            }
            
            userRepository.save(user);
            throw new AuthenticationException("Invalid username/email or password");
        }
        
        // Reset failed login attempts on successful login
        if (user.getFailedLoginAttempts() > 0) {
            user.resetFailedLoginAttempts();
            userRepository.save(user);
        }
        
        // Prepare user roles for JWT claims
        List<String> roles = extractUserRoles(user);
        
        // Generate JWT tokens with user ID and roles to reduce database lookups
        String accessToken = jwtUtils.generateAccessToken(user.getUsername(), user.getId(), roles);
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername(), user.getId(), roles);
        Date expDate = jwtUtils.getExpirationDateFromToken(accessToken);
        LocalDateTime expiresAt = (expDate != null)
            ? expDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
            : LocalDateTime.now();
        
        log.debug("User authenticated successfully");
        
        // Log successful login
        auditService.logAuthenticationEvent(
            EventType.LOGIN_SUCCESS,
            "User logged in successfully",
            user.getId(),
            user.getUsername(),
            request, // Pass the actual request for audit context
            true
        );
        
        return authMapper.toJwtResponse(user, accessToken, refreshToken, expiresAt);
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
        // Validate token format
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new AuthenticationException("Refresh token is required");
        }
        
        if (!jwtUtils.validateJwtToken(refreshToken)) {
            throw new AuthenticationException("Invalid refresh token");
        }
        
        // Check if refresh token is already blacklisted
        if (tokenBlacklistService.isTokenBlacklisted(refreshToken)) {
            log.warn("Attempted to use blacklisted refresh token");
            throw new AuthenticationException("Refresh token has been revoked");
        }
        
        String username = jwtUtils.getUsernameFromJwtToken(refreshToken);
        User user = userRepository.findByUsernameAndEnabledTrue(username)
                .orElseThrow(() -> new AuthenticationException("User not found"));
        
        // Check if account is locked
        if (user.isAccountLocked()) {
            log.warn("Attempted token refresh for locked account: {}", username);
            throw new AccountLockedException("Account is temporarily locked");
        }
        
        // Check if account is disabled
        if (!user.getEnabled()) {
            log.warn("Attempted token refresh for disabled account: {}", username);
            throw new AccountDisabledException("Account is disabled");
        }
        
        // Prepare user roles for JWT claims
        List<String> roles = extractUserRoles(user);
        
        // Generate new tokens with user ID and roles
        String newAccessToken = jwtUtils.generateAccessToken(user.getUsername(), user.getId(), roles);
        String newRefreshToken = jwtUtils.generateRefreshToken(user.getUsername(), user.getId(), roles);
        Date expDate = jwtUtils.getExpirationDateFromToken(newAccessToken);
        LocalDateTime expiresAt = (expDate != null)
            ? expDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
            : LocalDateTime.now();
        
        // Blacklist the old refresh token to prevent reuse (token rotation)
        long oldTokenExpiration = jwtUtils.getExpirationDateFromToken(refreshToken).getTime();
        boolean blacklisted = tokenBlacklistService.blacklistToken(refreshToken, oldTokenExpiration);
        if (!blacklisted) {
            log.error("CRITICAL: Failed to blacklist old refresh token during token rotation");
            throw new AuthenticationException("Token refresh failed due to security error. Please log in again.");
        }
        
        log.debug("Old refresh token blacklisted successfully");
        return authMapper.toJwtResponse(user, newAccessToken, newRefreshToken, expiresAt);
    }
    
    /**
     * Logout user and blacklist access token.
     * 
     * @param request HTTP request to extract tokens
     */
    public void logoutUser(HttpServletRequest request) {
        log.debug("Processing logout request");
        
        try {
            // Extract access token from request
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String accessToken = authHeader.substring(7);
                
                // Validate token format before processing
                if (accessToken == null || accessToken.trim().isEmpty()) {
                    log.warn("Empty access token in logout request");
                    return;
                }
                
                // Validate token before extracting expiration
                if (!jwtUtils.validateJwtToken(accessToken)) {
                    log.warn("Invalid access token format in logout request");
                    return;
                }
                
                // Get token expiration time
                long expirationTime = jwtUtils.getExpirationDateFromToken(accessToken).getTime();
                
                // Blacklist the access token
                boolean blacklisted = tokenBlacklistService.blacklistToken(accessToken, expirationTime);
                if (blacklisted) {
                    log.debug("Access token blacklisted successfully");
                } else {
                    log.warn("Failed to blacklist access token during logout. "
                        + "Token may remain valid until expiration");
                }
            } else {
                log.warn("No valid Authorization header found in logout request");
            }
        } catch (io.jsonwebtoken.JwtException e) {
            log.error("JWT error during logout: {}", e.getMessage());
            // Don't throw exception - logout should always succeed from user perspective
        } catch (Exception e) {
            log.error("Unexpected error during logout: {}", e.getMessage());
            // Don't throw exception - logout should always succeed from user perspective
        }
    }
    
    /**
     * Extract user roles as a list of authority strings.
     * Helper method to eliminate code duplication.
     * 
     * @param user the user entity
     * @return list of role authority strings
     */
    private List<String> extractUserRoles(User user) {
        return user.getRoles().stream()
                .map(role -> role.getAuthority())
                .collect(Collectors.toList());
    }
}
