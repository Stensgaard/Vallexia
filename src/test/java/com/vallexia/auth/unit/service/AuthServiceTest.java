package com.vallexia.auth.unit.service;

import com.vallexia.audit.entity.EventType;
import com.vallexia.audit.service.AuditService;
import com.vallexia.auth.dto.*;
import com.vallexia.auth.exception.AccountLockedException;
import com.vallexia.auth.exception.AccountDisabledException;
import com.vallexia.auth.exception.AuthenticationException;
import com.vallexia.auth.exception.UserAlreadyExistsException;
import com.vallexia.auth.fixtures.AuthTestFixtures;
import com.vallexia.auth.mapper.AuthMapper;
import com.vallexia.auth.service.AuthService;
import com.vallexia.auth.service.TokenBlacklistService;
import com.vallexia.config.security.AccountSecurityProperties;
import com.vallexia.exception.ValidationException;
import com.vallexia.security.JwtUtils;
import com.vallexia.user.entity.Role;
import com.vallexia.user.entity.User;
import com.vallexia.user.fixtures.UserTestFixtures;
import com.vallexia.user.repository.UserRepository;
import com.vallexia.user.service.DietaryPreferencesService;
import com.vallexia.user.service.NutritionalGoalsService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * Tests business logic with mocked dependencies.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {
  
  @Mock
  private UserRepository userRepository;
  
  @Mock
  private PasswordEncoder passwordEncoder;
  
  @Mock
  private JwtUtils jwtUtils;
  
  @Mock
  private AuditService auditService;
  
  @Mock
  private DietaryPreferencesService dietaryPreferencesService;
  
  @Mock
  private NutritionalGoalsService nutritionalGoalsService;
  
  @Mock
  private TokenBlacklistService tokenBlacklistService;
  
  @Mock
  private AccountSecurityProperties accountSecurityProperties;
  
  @Mock
  private AuthMapper authMapper;
  
  @Mock
  private HttpServletRequest request;
  
  @InjectMocks
  private AuthService authService;
  
  private User testUser;
  
  @BeforeEach
  void setUp() {
    testUser = UserTestFixtures.createUser();
    when(accountSecurityProperties.getMaxFailedAttempts()).thenReturn(5);
    when(accountSecurityProperties.getDurationMinutes()).thenReturn(15);
  }
  
  // ==================== registerUser() Tests ====================
  
  @Test
  @DisplayName("Should successfully register new user with valid data")
  void shouldSuccessfullyRegisterNewUserWithValidData() {
    // Given
    RegisterRequestDto registerDto = AuthTestFixtures.createRegisterRequestDto();
    User newUser = UserTestFixtures.createUser();
    JwtResponseDto expectedResponse = AuthTestFixtures.createJwtResponseDto();
    
    when(userRepository.existsByUsername(registerDto.getUsername())).thenReturn(false);
    when(userRepository.existsByEmail(registerDto.getEmail())).thenReturn(false);
    when(passwordEncoder.encode(registerDto.getPassword())).thenReturn(UserTestFixtures.TEST_PASSWORD_HASH);
    when(authMapper.toUser(registerDto)).thenReturn(newUser);
    when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
    when(jwtUtils.generateAccessToken(anyString(), anyLong(), anyList())).thenReturn(AuthTestFixtures.TEST_ACCESS_TOKEN);
    when(jwtUtils.generateRefreshToken(anyString(), anyLong(), anyList())).thenReturn(AuthTestFixtures.TEST_REFRESH_TOKEN);
    when(jwtUtils.getExpirationDateFromToken(anyString())).thenReturn(Date.from(AuthTestFixtures.TEST_TOKEN_EXPIRES_AT.atZone(java.time.ZoneId.systemDefault()).toInstant()));
    when(authMapper.toJwtResponse(any(), anyString(), anyString(), any())).thenReturn(expectedResponse);
    
    // When
    JwtResponseDto result = authService.registerUser(registerDto, request);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getAccessToken()).isEqualTo(expectedResponse.getAccessToken());
    assertThat(result.getRefreshToken()).isEqualTo(expectedResponse.getRefreshToken());
    
    verify(userRepository).existsByUsername(registerDto.getUsername());
    verify(userRepository).existsByEmail(registerDto.getEmail());
    verify(passwordEncoder).encode(registerDto.getPassword());
    verify(authMapper).toUser(registerDto);
    verify(userRepository).save(any(User.class));
    verify(dietaryPreferencesService).createDefaultPreferences(any(User.class));
    verify(nutritionalGoalsService).createDefaultGoals(any(User.class));
    verify(jwtUtils).generateAccessToken(anyString(), anyLong(), anyList());
    verify(jwtUtils).generateRefreshToken(anyString(), anyLong(), anyList());
    verify(auditService).logAuthenticationEvent(eq(EventType.REGISTRATION), anyString(), anyLong(), anyString(), any(), eq(true));
  }
  
  @Test
  @DisplayName("Should throw ValidationException when password confirmation doesn't match")
  void shouldThrowValidationExceptionWhenPasswordConfirmationDoesNotMatch() {
    // Given
    RegisterRequestDto registerDto = AuthTestFixtures.createRegisterRequestDtoWithPasswordMismatch();
    
    // When & Then
    assertThatThrownBy(() -> authService.registerUser(registerDto, request))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Password and confirmation password do not match");
    
    verify(userRepository, never()).existsByUsername(anyString());
    verify(userRepository, never()).existsByEmail(anyString());
    verify(userRepository, never()).save(any());
  }
  
  @Test
  @DisplayName("Should throw UserAlreadyExistsException when username exists")
  void shouldThrowUserAlreadyExistsExceptionWhenUsernameExists() {
    // Given
    RegisterRequestDto registerDto = AuthTestFixtures.createRegisterRequestDto();
    when(userRepository.existsByUsername(registerDto.getUsername())).thenReturn(true);
    
    // When & Then
    assertThatThrownBy(() -> authService.registerUser(registerDto, request))
        .isInstanceOf(UserAlreadyExistsException.class)
        .hasMessageContaining("Username is already taken");
    
    verify(userRepository).existsByUsername(registerDto.getUsername());
    verify(userRepository, never()).existsByEmail(anyString());
    verify(userRepository, never()).save(any());
  }
  
  @Test
  @DisplayName("Should throw UserAlreadyExistsException when email exists")
  void shouldThrowUserAlreadyExistsExceptionWhenEmailExists() {
    // Given
    RegisterRequestDto registerDto = AuthTestFixtures.createRegisterRequestDto();
    when(userRepository.existsByUsername(registerDto.getUsername())).thenReturn(false);
    when(userRepository.existsByEmail(registerDto.getEmail())).thenReturn(true);
    
    // When & Then
    assertThatThrownBy(() -> authService.registerUser(registerDto, request))
        .isInstanceOf(UserAlreadyExistsException.class)
        .hasMessageContaining("Email is already in use");
    
    verify(userRepository).existsByUsername(registerDto.getUsername());
    verify(userRepository).existsByEmail(registerDto.getEmail());
    verify(userRepository, never()).save(any());
  }
  
  @Test
  @DisplayName("Should encode password before saving")
  void shouldEncodePasswordBeforeSaving() {
    // Given
    RegisterRequestDto registerDto = AuthTestFixtures.createRegisterRequestDto();
    User newUser = UserTestFixtures.createUser();
    String encodedPassword = "encoded_password_hash";
    
    when(userRepository.existsByUsername(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(passwordEncoder.encode(registerDto.getPassword())).thenReturn(encodedPassword);
    when(authMapper.toUser(registerDto)).thenReturn(newUser);
    when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
    when(jwtUtils.generateAccessToken(anyString(), anyLong(), anyList())).thenReturn(AuthTestFixtures.TEST_ACCESS_TOKEN);
    when(jwtUtils.generateRefreshToken(anyString(), anyLong(), anyList())).thenReturn(AuthTestFixtures.TEST_REFRESH_TOKEN);
    Date expirationDate = Date.from(AuthTestFixtures.TEST_TOKEN_EXPIRES_AT.atZone(java.time.ZoneId.systemDefault()).toInstant());
    when(jwtUtils.getExpirationDateFromToken(anyString())).thenReturn(expirationDate);
    when(authMapper.toJwtResponse(any(), anyString(), anyString(), any())).thenReturn(AuthTestFixtures.createJwtResponseDto());
    
    // When
    authService.registerUser(registerDto, request);
    
    // Then
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo(encodedPassword);
    verify(passwordEncoder).encode(registerDto.getPassword());
  }
  
  @Test
  @DisplayName("Should assign USER role to new user")
  void shouldAssignUserRoleToNewUser() {
    // Given
    RegisterRequestDto registerDto = AuthTestFixtures.createRegisterRequestDto();
    User newUser = new User();
    
    when(userRepository.existsByUsername(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("encoded");
    when(authMapper.toUser(registerDto)).thenReturn(newUser);
    when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
    
    // Mock JWT token generation - exactly matching the working test pattern
    when(jwtUtils.generateAccessToken(anyString(), anyLong(), anyList())).thenReturn(AuthTestFixtures.TEST_ACCESS_TOKEN);
    when(jwtUtils.generateRefreshToken(anyString(), anyLong(), anyList())).thenReturn(AuthTestFixtures.TEST_REFRESH_TOKEN);
    // Create expiration date inline exactly like the working test (line 117)
    when(jwtUtils.getExpirationDateFromToken(anyString())).thenReturn(Date.from(AuthTestFixtures.TEST_TOKEN_EXPIRES_AT.atZone(java.time.ZoneId.systemDefault()).toInstant()));
    
    when(authMapper.toJwtResponse(any(), anyString(), anyString(), any())).thenReturn(AuthTestFixtures.createJwtResponseDto());
    
    // When
    authService.registerUser(registerDto, request);
    
    // Then
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    User savedUser = userCaptor.getValue();
    assertThat(savedUser.getRoles()).contains(Role.USER);
  }
  
  // ==================== authenticateUser() Tests ====================
  
  @Test
  @DisplayName("Should successfully authenticate with valid username and password")
  void shouldSuccessfullyAuthenticateWithValidUsernameAndPassword() {
    // Given
    LoginRequestDto loginDto = AuthTestFixtures.createLoginRequestDto();
    JwtResponseDto expectedResponse = AuthTestFixtures.createJwtResponseDto();
    
    when(userRepository.findByUsernameAndEnabledTrue(loginDto.getUsernameOrEmail())).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(loginDto.getPassword(), testUser.getPasswordHash())).thenReturn(true);
    when(jwtUtils.generateAccessToken(anyString(), anyLong(), anyList())).thenReturn(AuthTestFixtures.TEST_ACCESS_TOKEN);
    when(jwtUtils.generateRefreshToken(anyString(), anyLong(), anyList())).thenReturn(AuthTestFixtures.TEST_REFRESH_TOKEN);
    when(jwtUtils.getExpirationDateFromToken(anyString())).thenReturn(Date.from(AuthTestFixtures.TEST_TOKEN_EXPIRES_AT.atZone(java.time.ZoneId.systemDefault()).toInstant()));
    when(authMapper.toJwtResponse(any(), anyString(), anyString(), any())).thenReturn(expectedResponse);
    
    // When
    JwtResponseDto result = authService.authenticateUser(loginDto, request);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getAccessToken()).isEqualTo(expectedResponse.getAccessToken());
    assertThat(result.getRefreshToken()).isEqualTo(expectedResponse.getRefreshToken());
    
    verify(userRepository).findByUsernameAndEnabledTrue(loginDto.getUsernameOrEmail());
    verify(passwordEncoder).matches(loginDto.getPassword(), testUser.getPasswordHash());
    verify(auditService).logAuthenticationEvent(eq(EventType.LOGIN_SUCCESS), anyString(), anyLong(), anyString(), any(), eq(true));
  }
  
  @Test
  @DisplayName("Should successfully authenticate with valid email and password")
  void shouldSuccessfullyAuthenticateWithValidEmailAndPassword() {
    // Given
    LoginRequestDto loginDto = AuthTestFixtures.createLoginRequestDtoWithEmail();
    JwtResponseDto expectedResponse = AuthTestFixtures.createJwtResponseDto();
    
    when(userRepository.findByUsernameAndEnabledTrue(loginDto.getUsernameOrEmail())).thenReturn(Optional.empty());
    when(userRepository.findByEmailAndEnabledTrue(loginDto.getUsernameOrEmail())).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(loginDto.getPassword(), testUser.getPasswordHash())).thenReturn(true);
    when(jwtUtils.generateAccessToken(anyString(), anyLong(), anyList())).thenReturn(AuthTestFixtures.TEST_ACCESS_TOKEN);
    when(jwtUtils.generateRefreshToken(anyString(), anyLong(), anyList())).thenReturn(AuthTestFixtures.TEST_REFRESH_TOKEN);
    when(jwtUtils.getExpirationDateFromToken(anyString())).thenReturn(Date.from(AuthTestFixtures.TEST_TOKEN_EXPIRES_AT.atZone(java.time.ZoneId.systemDefault()).toInstant()));
    when(authMapper.toJwtResponse(any(), anyString(), anyString(), any())).thenReturn(expectedResponse);
    
    // When
    JwtResponseDto result = authService.authenticateUser(loginDto, request);
    
    // Then
    assertThat(result).isNotNull();
    verify(userRepository).findByUsernameAndEnabledTrue(loginDto.getUsernameOrEmail());
    verify(userRepository).findByEmailAndEnabledTrue(loginDto.getUsernameOrEmail());
  }
  
  @Test
  @DisplayName("Should throw AuthenticationException for invalid username or email")
  void shouldThrowAuthenticationExceptionForInvalidUsernameOrEmail() {
    // Given
    LoginRequestDto loginDto = AuthTestFixtures.createLoginRequestDto();
    
    when(userRepository.findByUsernameAndEnabledTrue(loginDto.getUsernameOrEmail())).thenReturn(Optional.empty());
    when(userRepository.findByEmailAndEnabledTrue(loginDto.getUsernameOrEmail())).thenReturn(Optional.empty());
    
    // When & Then
    assertThatThrownBy(() -> authService.authenticateUser(loginDto, request))
        .isInstanceOf(AuthenticationException.class)
        .hasMessageContaining("Invalid username/email or password");
    
    verify(userRepository).findByUsernameAndEnabledTrue(loginDto.getUsernameOrEmail());
    verify(userRepository).findByEmailAndEnabledTrue(loginDto.getUsernameOrEmail());
    verify(passwordEncoder, never()).matches(anyString(), anyString());
  }
  
  @Test
  @DisplayName("Should throw AuthenticationException for invalid password")
  void shouldThrowAuthenticationExceptionForInvalidPassword() {
    // Given
    LoginRequestDto loginDto = AuthTestFixtures.createLoginRequestDto();
    
    when(userRepository.findByUsernameAndEnabledTrue(loginDto.getUsernameOrEmail())).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(loginDto.getPassword(), testUser.getPasswordHash())).thenReturn(false);
    when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
    
    // When & Then
    assertThatThrownBy(() -> authService.authenticateUser(loginDto, request))
        .isInstanceOf(AuthenticationException.class)
        .hasMessageContaining("Invalid username/email or password");
    
    verify(passwordEncoder).matches(loginDto.getPassword(), testUser.getPasswordHash());
    verify(userRepository).save(any(User.class));
  }
  
  @Test
  @DisplayName("Should throw AccountLockedException when account is locked")
  void shouldThrowAccountLockedExceptionWhenAccountIsLocked() {
    // Given
    LoginRequestDto loginDto = AuthTestFixtures.createLoginRequestDto();
    User lockedUser = AuthTestFixtures.createLockedUser();
    
    when(userRepository.findByUsernameAndEnabledTrue(loginDto.getUsernameOrEmail())).thenReturn(Optional.of(lockedUser));
    
    // When & Then
    assertThatThrownBy(() -> authService.authenticateUser(loginDto, request))
        .isInstanceOf(AccountLockedException.class)
        .hasMessageContaining("Account is temporarily locked");
    
    verify(passwordEncoder, never()).matches(anyString(), anyString());
  }
  
  @Test
  @DisplayName("Should increment failed login attempts on wrong password")
  void shouldIncrementFailedLoginAttemptsOnWrongPassword() {
    // Given
    LoginRequestDto loginDto = AuthTestFixtures.createLoginRequestDto();
    User user = AuthTestFixtures.createUserWithFailedAttempts(2);
    
    when(userRepository.findByUsernameAndEnabledTrue(loginDto.getUsernameOrEmail())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())).thenReturn(false);
    when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
    
    // When
    try {
      authService.authenticateUser(loginDto, request);
    } catch (AuthenticationException e) {
      // Expected
    }
    
    // Then
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    assertThat(userCaptor.getValue().getFailedLoginAttempts()).isEqualTo(3);
  }
  
  @Test
  @DisplayName("Should lock account after max failed attempts")
  void shouldLockAccountAfterMaxFailedAttempts() {
    // Given
    LoginRequestDto loginDto = AuthTestFixtures.createLoginRequestDto();
    User user = AuthTestFixtures.createUserNearLockout(5);
    
    when(userRepository.findByUsernameAndEnabledTrue(loginDto.getUsernameOrEmail())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())).thenReturn(false);
    when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
    
    // When
    try {
      authService.authenticateUser(loginDto, request);
    } catch (AuthenticationException e) {
      // Expected
    }
    
    // Then
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    User savedUser = userCaptor.getValue();
    assertThat(savedUser.getFailedLoginAttempts()).isEqualTo(5);
    assertThat(savedUser.getAccountLockedUntil()).isNotNull();
    assertThat(savedUser.getAccountLockedUntil()).isAfter(LocalDateTime.now());
  }
  
  @Test
  @DisplayName("Should reset failed login attempts on successful login")
  void shouldResetFailedLoginAttemptsOnSuccessfulLogin() {
    // Given
    LoginRequestDto loginDto = AuthTestFixtures.createLoginRequestDto();
    User user = AuthTestFixtures.createUserWithFailedAttempts(3);
    JwtResponseDto expectedResponse = AuthTestFixtures.createJwtResponseDto();
    
    when(userRepository.findByUsernameAndEnabledTrue(loginDto.getUsernameOrEmail())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())).thenReturn(true);
    when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
    when(jwtUtils.generateAccessToken(anyString(), anyLong(), anyList())).thenReturn(AuthTestFixtures.TEST_ACCESS_TOKEN);
    when(jwtUtils.generateRefreshToken(anyString(), anyLong(), anyList())).thenReturn(AuthTestFixtures.TEST_REFRESH_TOKEN);
    when(jwtUtils.getExpirationDateFromToken(anyString())).thenReturn(Date.from(AuthTestFixtures.TEST_TOKEN_EXPIRES_AT.atZone(java.time.ZoneId.systemDefault()).toInstant()));
    when(authMapper.toJwtResponse(any(), anyString(), anyString(), any())).thenReturn(expectedResponse);
    
    // When
    authService.authenticateUser(loginDto, request);
    
    // Then
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    User savedUser = userCaptor.getValue();
    assertThat(savedUser.getFailedLoginAttempts()).isEqualTo(0);
    assertThat(savedUser.getAccountLockedUntil()).isNull();
  }
  
  // ==================== refreshToken() Tests ====================
  
  @Test
  @DisplayName("Should successfully refresh token with valid refresh token")
  void shouldSuccessfullyRefreshTokenWithValidRefreshToken() {
    // Given
    String refreshToken = AuthTestFixtures.TEST_REFRESH_TOKEN;
    User user = UserTestFixtures.createUser();
    JwtResponseDto expectedResponse = AuthTestFixtures.createJwtResponseDto();
    
    when(jwtUtils.validateJwtToken(refreshToken)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(refreshToken)).thenReturn(false);
    when(jwtUtils.getUsernameFromJwtToken(refreshToken)).thenReturn(user.getUsername());
    when(userRepository.findByUsernameAndEnabledTrue(user.getUsername())).thenReturn(Optional.of(user));
    when(jwtUtils.generateAccessToken(anyString(), anyLong(), anyList())).thenReturn(AuthTestFixtures.TEST_ACCESS_TOKEN);
    when(jwtUtils.generateRefreshToken(anyString(), anyLong(), anyList())).thenReturn("new_refresh_token");
    when(jwtUtils.getExpirationDateFromToken(anyString())).thenReturn(Date.from(AuthTestFixtures.TEST_TOKEN_EXPIRES_AT.atZone(java.time.ZoneId.systemDefault()).toInstant()));
    when(jwtUtils.getExpirationDateFromToken(refreshToken)).thenReturn(Date.from(AuthTestFixtures.TEST_TOKEN_EXPIRES_AT.atZone(java.time.ZoneId.systemDefault()).toInstant()));
    when(tokenBlacklistService.blacklistToken(eq(refreshToken), anyLong())).thenReturn(true);
    when(authMapper.toJwtResponse(any(), anyString(), anyString(), any())).thenReturn(expectedResponse);
    
    // When
    JwtResponseDto result = authService.refreshToken(refreshToken);
    
    // Then
    assertThat(result).isNotNull();
    verify(jwtUtils).validateJwtToken(refreshToken);
    verify(tokenBlacklistService).isTokenBlacklisted(refreshToken);
    verify(tokenBlacklistService).blacklistToken(eq(refreshToken), anyLong());
    verify(jwtUtils).generateAccessToken(anyString(), anyLong(), anyList());
    verify(jwtUtils).generateRefreshToken(anyString(), anyLong(), anyList());
  }
  
  @Test
  @DisplayName("Should throw AuthenticationException for invalid token format")
  void shouldThrowAuthenticationExceptionForInvalidTokenFormat() {
    // Given
    String invalidToken = AuthTestFixtures.TEST_INVALID_TOKEN;
    
    when(jwtUtils.validateJwtToken(invalidToken)).thenReturn(false);
    
    // When & Then
    assertThatThrownBy(() -> authService.refreshToken(invalidToken))
        .isInstanceOf(AuthenticationException.class)
        .hasMessageContaining("Invalid refresh token");
    
    verify(jwtUtils).validateJwtToken(invalidToken);
    verify(tokenBlacklistService, never()).isTokenBlacklisted(anyString());
  }
  
  @Test
  @DisplayName("Should throw AuthenticationException for null token")
  void shouldThrowAuthenticationExceptionForNullToken() {
    // When & Then
    assertThatThrownBy(() -> authService.refreshToken(null))
        .isInstanceOf(AuthenticationException.class)
        .hasMessageContaining("Refresh token is required");
    
    verify(jwtUtils, never()).validateJwtToken(anyString());
  }
  
  @Test
  @DisplayName("Should throw AuthenticationException for empty token")
  void shouldThrowAuthenticationExceptionForEmptyToken() {
    // When & Then
    assertThatThrownBy(() -> authService.refreshToken(""))
        .isInstanceOf(AuthenticationException.class)
        .hasMessageContaining("Refresh token is required");
  }
  
  @Test
  @DisplayName("Should throw AuthenticationException for blacklisted refresh token")
  void shouldThrowAuthenticationExceptionForBlacklistedRefreshToken() {
    // Given
    String blacklistedToken = AuthTestFixtures.TEST_BLACKLISTED_TOKEN;
    
    when(jwtUtils.validateJwtToken(blacklistedToken)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(blacklistedToken)).thenReturn(true);
    
    // When & Then
    assertThatThrownBy(() -> authService.refreshToken(blacklistedToken))
        .isInstanceOf(AuthenticationException.class)
        .hasMessageContaining("Refresh token has been revoked");
  }
  
  @Test
  @DisplayName("Should throw AuthenticationException when user not found")
  void shouldThrowAuthenticationExceptionWhenUserNotFound() {
    // Given
    String refreshToken = AuthTestFixtures.TEST_REFRESH_TOKEN;
    
    when(jwtUtils.validateJwtToken(refreshToken)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(refreshToken)).thenReturn(false);
    when(jwtUtils.getUsernameFromJwtToken(refreshToken)).thenReturn("nonexistent");
    when(userRepository.findByUsernameAndEnabledTrue("nonexistent")).thenReturn(Optional.empty());
    
    // When & Then
    assertThatThrownBy(() -> authService.refreshToken(refreshToken))
        .isInstanceOf(AuthenticationException.class)
        .hasMessageContaining("User not found");
  }
  
  @Test
  @DisplayName("Should throw AccountLockedException when account is locked")
  void shouldThrowAccountLockedExceptionWhenAccountIsLockedDuringRefresh() {
    // Given
    String refreshToken = AuthTestFixtures.TEST_REFRESH_TOKEN;
    User lockedUser = AuthTestFixtures.createLockedUser();
    
    when(jwtUtils.validateJwtToken(refreshToken)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(refreshToken)).thenReturn(false);
    when(jwtUtils.getUsernameFromJwtToken(refreshToken)).thenReturn(lockedUser.getUsername());
    when(userRepository.findByUsernameAndEnabledTrue(lockedUser.getUsername())).thenReturn(Optional.of(lockedUser));
    
    // When & Then
    assertThatThrownBy(() -> authService.refreshToken(refreshToken))
        .isInstanceOf(AccountLockedException.class)
        .hasMessageContaining("Account is temporarily locked");
  }
  
  @Test
  @DisplayName("Should throw AccountDisabledException when account is disabled")
  void shouldThrowAccountDisabledExceptionWhenAccountIsDisabledDuringRefresh() {
    // Given
    String refreshToken = AuthTestFixtures.TEST_REFRESH_TOKEN;
    User disabledUser = UserTestFixtures.createDisabledUser();
    
    when(jwtUtils.validateJwtToken(refreshToken)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(refreshToken)).thenReturn(false);
    when(jwtUtils.getUsernameFromJwtToken(refreshToken)).thenReturn(disabledUser.getUsername());
    when(userRepository.findByUsernameAndEnabledTrue(disabledUser.getUsername())).thenReturn(Optional.of(disabledUser));
    
    // When & Then
    assertThatThrownBy(() -> authService.refreshToken(refreshToken))
        .isInstanceOf(AccountDisabledException.class)
        .hasMessageContaining("Account is disabled");
  }
  
  @Test
  @DisplayName("Should throw AuthenticationException if blacklisting fails during token rotation")
  void shouldThrowAuthenticationExceptionIfBlacklistingFailsDuringTokenRotation() {
    // Given
    String refreshToken = AuthTestFixtures.TEST_REFRESH_TOKEN;
    User user = UserTestFixtures.createUser();
    
    when(jwtUtils.validateJwtToken(refreshToken)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(refreshToken)).thenReturn(false);
    when(jwtUtils.getUsernameFromJwtToken(refreshToken)).thenReturn(user.getUsername());
    when(userRepository.findByUsernameAndEnabledTrue(user.getUsername())).thenReturn(Optional.of(user));
    when(jwtUtils.generateAccessToken(anyString(), anyLong(), anyList())).thenReturn(AuthTestFixtures.TEST_ACCESS_TOKEN);
    when(jwtUtils.generateRefreshToken(anyString(), anyLong(), anyList())).thenReturn("new_refresh_token");
    when(jwtUtils.getExpirationDateFromToken(anyString())).thenReturn(Date.from(AuthTestFixtures.TEST_TOKEN_EXPIRES_AT.atZone(java.time.ZoneId.systemDefault()).toInstant()));
    when(jwtUtils.getExpirationDateFromToken(refreshToken)).thenReturn(Date.from(AuthTestFixtures.TEST_TOKEN_EXPIRES_AT.atZone(java.time.ZoneId.systemDefault()).toInstant()));
    when(tokenBlacklistService.blacklistToken(eq(refreshToken), anyLong())).thenReturn(false);
    
    // When & Then
    assertThatThrownBy(() -> authService.refreshToken(refreshToken))
        .isInstanceOf(AuthenticationException.class)
        .hasMessageContaining("Token refresh failed due to security error");
    
    verify(tokenBlacklistService).blacklistToken(eq(refreshToken), anyLong());
  }
  
  // ==================== logoutUser() Tests ====================
  
  @Test
  @DisplayName("Should successfully blacklist token from Authorization header")
  void shouldSuccessfullyBlacklistTokenFromAuthorizationHeader() {
    // Given
    String accessToken = AuthTestFixtures.TEST_ACCESS_TOKEN;
    String authHeader = "Bearer " + accessToken;
    
    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtUtils.validateJwtToken(accessToken)).thenReturn(true);
    Date expirationDate = Date.from(AuthTestFixtures.TEST_TOKEN_EXPIRES_AT.atZone(java.time.ZoneId.systemDefault()).toInstant());
    when(jwtUtils.getExpirationDateFromToken(accessToken)).thenReturn(expirationDate);
    when(tokenBlacklistService.blacklistToken(eq(accessToken), anyLong())).thenReturn(true);
    
    // When
    authService.logoutUser(request);
    
    // Then
    verify(request).getHeader("Authorization");
    verify(jwtUtils).validateJwtToken(accessToken);
    verify(jwtUtils).getExpirationDateFromToken(accessToken);
    verify(tokenBlacklistService).blacklistToken(eq(accessToken), anyLong());
  }
  
  @Test
  @DisplayName("Should handle missing Authorization header gracefully")
  void shouldHandleMissingAuthorizationHeaderGracefully() {
    // Given
    when(request.getHeader("Authorization")).thenReturn(null);
    
    // When
    authService.logoutUser(request);
    
    // Then - should not throw exception
    verify(request).getHeader("Authorization");
    verify(jwtUtils, never()).validateJwtToken(anyString());
    verify(tokenBlacklistService, never()).blacklistToken(anyString(), anyLong());
  }
  
  @Test
  @DisplayName("Should handle invalid Bearer token format gracefully")
  void shouldHandleInvalidBearerTokenFormatGracefully() {
    // Given
    when(request.getHeader("Authorization")).thenReturn("InvalidFormat token");
    
    // When
    authService.logoutUser(request);
    
    // Then - should not throw exception
    verify(request).getHeader("Authorization");
    verify(jwtUtils, never()).validateJwtToken(anyString());
    verify(tokenBlacklistService, never()).blacklistToken(anyString(), anyLong());
  }
  
  @Test
  @DisplayName("Should handle JWT exceptions gracefully")
  void shouldHandleJwtExceptionsGracefully() {
    // Given
    String accessToken = AuthTestFixtures.TEST_ACCESS_TOKEN;
    String authHeader = "Bearer " + accessToken;
    
    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtUtils.validateJwtToken(accessToken)).thenReturn(false);
    
    // When
    authService.logoutUser(request);
    
    // Then - should not throw exception
    verify(request).getHeader("Authorization");
    verify(jwtUtils).validateJwtToken(accessToken);
    verify(tokenBlacklistService, never()).blacklistToken(anyString(), anyLong());
  }
  
  @Test
  @DisplayName("Should never throw exception during logout")
  void shouldNeverThrowExceptionDuringLogout() {
    // Given
    String accessToken = AuthTestFixtures.TEST_ACCESS_TOKEN;
    String authHeader = "Bearer " + accessToken;
    
    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtUtils.validateJwtToken(accessToken)).thenThrow(new RuntimeException("JWT error"));
    
    // When & Then - should not throw
    authService.logoutUser(request);
    
    verify(request).getHeader("Authorization");
  }
}

