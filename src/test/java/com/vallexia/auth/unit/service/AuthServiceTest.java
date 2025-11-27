package com.vallexia.auth.unit.service;

import com.vallexia.audit.entity.enums.EventType;
import com.vallexia.audit.service.AuditService;
import com.vallexia.auth.dto.*;
import com.vallexia.auth.exception.AccountLockedException;
import com.vallexia.auth.exception.AccountDisabledException;
import com.vallexia.auth.exception.AuthenticationException;
import com.vallexia.auth.exception.UserAlreadyExistsException;
import com.vallexia.auth.fixtures.AuthTestFixtures;
import com.vallexia.auth.mapper.AuthMapper;
import com.vallexia.auth.service.AuthService;
import com.vallexia.auth.service.JwtTokenService;
import com.vallexia.auth.service.TokenBlacklistService;
import com.vallexia.auth.util.AccountSecurityHelper;
import com.vallexia.auth.util.UserAuthenticationHelper;
import com.vallexia.exception.ValidationException;
import com.vallexia.user.entity.enums.Role;
import com.vallexia.user.entity.User;
import com.vallexia.user.fixtures.UserTestFixtures;
import com.vallexia.user.repository.UserRepository;
import com.vallexia.user.service.DietaryPreferencesService;
import com.vallexia.user.service.NutritionalGoalsService;
import com.vallexia.user.service.UserSettingsService;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * Tests business logic with mocked dependencies.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-30
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
  private AuditService auditService;
  
  @Mock
  private DietaryPreferencesService dietaryPreferencesService;
  
  @Mock
  private NutritionalGoalsService nutritionalGoalsService;
  
  @Mock
  private UserSettingsService userSettingsService;
  
  @Mock
  private TokenBlacklistService tokenBlacklistService;
  
  @Mock
  private AuthMapper authMapper;
  
  @Mock
  private JwtTokenService jwtTokenService;
  
  @Mock
  private UserAuthenticationHelper userAuthenticationHelper;
  
  @Mock
  private AccountSecurityHelper accountSecurityHelper;
  
  @Mock
  private HttpServletRequest request;
  
  @InjectMocks
  private AuthService authService;
  
  private User testUser;
  
  @BeforeEach
  void setUp() {
    testUser = UserTestFixtures.createUser();
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
    JwtTokenService.JwtTokenData tokenData = new JwtTokenService.JwtTokenData(
        AuthTestFixtures.TEST_ACCESS_TOKEN,
        AuthTestFixtures.TEST_REFRESH_TOKEN,
        AuthTestFixtures.TEST_TOKEN_EXPIRES_AT
    );
    when(jwtTokenService.generateTokens(any(User.class))).thenReturn(tokenData);
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
    verify(jwtTokenService).generateTokens(any(User.class));
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
    JwtTokenService.JwtTokenData tokenData = new JwtTokenService.JwtTokenData(
        AuthTestFixtures.TEST_ACCESS_TOKEN,
        AuthTestFixtures.TEST_REFRESH_TOKEN,
        AuthTestFixtures.TEST_TOKEN_EXPIRES_AT
    );
    when(jwtTokenService.generateTokens(any(User.class))).thenReturn(tokenData);
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
    
    JwtTokenService.JwtTokenData tokenData = new JwtTokenService.JwtTokenData(
        AuthTestFixtures.TEST_ACCESS_TOKEN,
        AuthTestFixtures.TEST_REFRESH_TOKEN,
        AuthTestFixtures.TEST_TOKEN_EXPIRES_AT
    );
    when(jwtTokenService.generateTokens(any(User.class))).thenReturn(tokenData);
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
    testUser.setEnabled(true);
    
    when(userAuthenticationHelper.findUserByUsernameOrEmail(loginDto.getUsernameOrEmail())).thenReturn(Optional.of(testUser));
    doNothing().when(userAuthenticationHelper).validateAccountStatus(testUser);
    when(passwordEncoder.matches(loginDto.getPassword(), testUser.getPasswordHash())).thenReturn(true);
    doNothing().when(accountSecurityHelper).resetFailedLoginAttempts(testUser);
    JwtTokenService.JwtTokenData tokenData = new JwtTokenService.JwtTokenData(
        AuthTestFixtures.TEST_ACCESS_TOKEN,
        AuthTestFixtures.TEST_REFRESH_TOKEN,
        AuthTestFixtures.TEST_TOKEN_EXPIRES_AT
    );
    when(jwtTokenService.generateTokens(any(User.class))).thenReturn(tokenData);
    when(authMapper.toJwtResponse(any(), anyString(), anyString(), any())).thenReturn(expectedResponse);
    
    // When
    JwtResponseDto result = authService.authenticateUser(loginDto, request);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getAccessToken()).isEqualTo(expectedResponse.getAccessToken());
    assertThat(result.getRefreshToken()).isEqualTo(expectedResponse.getRefreshToken());
    
    verify(userAuthenticationHelper).findUserByUsernameOrEmail(loginDto.getUsernameOrEmail());
    verify(userAuthenticationHelper).validateAccountStatus(testUser);
    verify(passwordEncoder).matches(loginDto.getPassword(), testUser.getPasswordHash());
    verify(accountSecurityHelper).resetFailedLoginAttempts(testUser);
    verify(jwtTokenService).generateTokens(any(User.class));
    verify(auditService).logAuthenticationEvent(eq(EventType.LOGIN_SUCCESS), anyString(), anyLong(), anyString(), any(), eq(true));
  }
  
  @Test
  @DisplayName("Should successfully authenticate with valid email and password")
  void shouldSuccessfullyAuthenticateWithValidEmailAndPassword() {
    // Given
    LoginRequestDto loginDto = AuthTestFixtures.createLoginRequestDtoWithEmail();
    JwtResponseDto expectedResponse = AuthTestFixtures.createJwtResponseDto();
    testUser.setEnabled(true);
    
    when(userAuthenticationHelper.findUserByUsernameOrEmail(loginDto.getUsernameOrEmail())).thenReturn(Optional.of(testUser));
    doNothing().when(userAuthenticationHelper).validateAccountStatus(testUser);
    when(passwordEncoder.matches(loginDto.getPassword(), testUser.getPasswordHash())).thenReturn(true);
    doNothing().when(accountSecurityHelper).resetFailedLoginAttempts(testUser);
    JwtTokenService.JwtTokenData tokenData = new JwtTokenService.JwtTokenData(
        AuthTestFixtures.TEST_ACCESS_TOKEN,
        AuthTestFixtures.TEST_REFRESH_TOKEN,
        AuthTestFixtures.TEST_TOKEN_EXPIRES_AT
    );
    when(jwtTokenService.generateTokens(any(User.class))).thenReturn(tokenData);
    when(authMapper.toJwtResponse(any(), anyString(), anyString(), any())).thenReturn(expectedResponse);
    
    // When
    JwtResponseDto result = authService.authenticateUser(loginDto, request);
    
    // Then
    assertThat(result).isNotNull();
    verify(userAuthenticationHelper).findUserByUsernameOrEmail(loginDto.getUsernameOrEmail());
    verify(userAuthenticationHelper).validateAccountStatus(testUser);
    verify(accountSecurityHelper).resetFailedLoginAttempts(testUser);
  }
  
  @Test
  @DisplayName("Should throw AuthenticationException for invalid username or email")
  void shouldThrowAuthenticationExceptionForInvalidUsernameOrEmail() {
    // Given
    LoginRequestDto loginDto = AuthTestFixtures.createLoginRequestDto();
    
    when(userAuthenticationHelper.findUserByUsernameOrEmail(loginDto.getUsernameOrEmail())).thenReturn(Optional.empty());
    
    // When & Then
    assertThatThrownBy(() -> authService.authenticateUser(loginDto, request))
        .isInstanceOf(AuthenticationException.class)
        .hasMessageContaining("Invalid username/email or password");
    
    verify(userAuthenticationHelper).findUserByUsernameOrEmail(loginDto.getUsernameOrEmail());
    verify(passwordEncoder, never()).matches(anyString(), anyString());
  }
  
  @Test
  @DisplayName("Should throw AuthenticationException for invalid password")
  void shouldThrowAuthenticationExceptionForInvalidPassword() {
    // Given
    LoginRequestDto loginDto = AuthTestFixtures.createLoginRequestDto();
    testUser.setEnabled(true);
    
    when(userAuthenticationHelper.findUserByUsernameOrEmail(loginDto.getUsernameOrEmail())).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(loginDto.getPassword(), testUser.getPasswordHash())).thenReturn(false);
    doNothing().when(accountSecurityHelper).handleFailedLoginAttempt(any(User.class));
    
    // When & Then
    assertThatThrownBy(() -> authService.authenticateUser(loginDto, request))
        .isInstanceOf(AuthenticationException.class)
        .hasMessageContaining("Invalid username/email or password");
    
    verify(passwordEncoder).matches(loginDto.getPassword(), testUser.getPasswordHash());
    verify(accountSecurityHelper).handleFailedLoginAttempt(testUser);
  }
  
  @Test
  @DisplayName("Should throw AccountLockedException when account is locked")
  void shouldThrowAccountLockedExceptionWhenAccountIsLocked() {
    // Given
    LoginRequestDto loginDto = AuthTestFixtures.createLoginRequestDto();
    User lockedUser = AuthTestFixtures.createLockedUser();
    lockedUser.setEnabled(true);
    
    when(userAuthenticationHelper.findUserByUsernameOrEmail(loginDto.getUsernameOrEmail())).thenReturn(Optional.of(lockedUser));
    doThrow(new AccountLockedException("Account is temporarily locked")).when(userAuthenticationHelper).validateAccountStatus(lockedUser);
    
    // When & Then
    assertThatThrownBy(() -> authService.authenticateUser(loginDto, request))
        .isInstanceOf(AccountLockedException.class)
        .hasMessageContaining("Account is temporarily locked");
    
    verify(passwordEncoder, never()).matches(anyString(), anyString());
  }
  
  @Test
  @DisplayName("Should throw AccountDisabledException when account is disabled")
  void shouldThrowAccountDisabledExceptionWhenAccountIsDisabled() {
    // Given
    LoginRequestDto loginDto = AuthTestFixtures.createLoginRequestDto();
    User disabledUser = UserTestFixtures.createDisabledUser();
    
    when(userAuthenticationHelper.findUserByUsernameOrEmail(loginDto.getUsernameOrEmail())).thenReturn(Optional.of(disabledUser));
    doThrow(new AccountDisabledException("Account is disabled")).when(userAuthenticationHelper).validateAccountStatus(disabledUser);
    
    // When & Then
    assertThatThrownBy(() -> authService.authenticateUser(loginDto, request))
        .isInstanceOf(AccountDisabledException.class)
        .hasMessageContaining("Account is disabled");
    
    verify(passwordEncoder, never()).matches(anyString(), anyString());
  }
  
  @Test
  @DisplayName("Should increment failed login attempts on wrong password")
  void shouldIncrementFailedLoginAttemptsOnWrongPassword() {
    // Given
    LoginRequestDto loginDto = AuthTestFixtures.createLoginRequestDto();
    User user = AuthTestFixtures.createUserWithFailedAttempts(2);
    user.setEnabled(true);
    
    when(userAuthenticationHelper.findUserByUsernameOrEmail(loginDto.getUsernameOrEmail())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())).thenReturn(false);
    doAnswer(invocation -> {
      User u = invocation.getArgument(0);
      u.incrementFailedLoginAttempts();
      return null;
    }).when(accountSecurityHelper).handleFailedLoginAttempt(any(User.class));
    
    // When
    try {
      authService.authenticateUser(loginDto, request);
    } catch (AuthenticationException e) {
      // Expected
    }
    
    // Then
    verify(accountSecurityHelper).handleFailedLoginAttempt(user);
  }
  
  @Test
  @DisplayName("Should lock account after max failed attempts")
  void shouldLockAccountAfterMaxFailedAttempts() {
    // Given
    LoginRequestDto loginDto = AuthTestFixtures.createLoginRequestDto();
    User user = AuthTestFixtures.createUserNearLockout(5);
    user.setEnabled(true);
    
    when(userAuthenticationHelper.findUserByUsernameOrEmail(loginDto.getUsernameOrEmail())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())).thenReturn(false);
    doAnswer(invocation -> {
      User u = invocation.getArgument(0);
      u.incrementFailedLoginAttempts();
      if (u.getFailedLoginAttempts() >= 5) {
        u.setAccountLockedUntil(LocalDateTime.now().plusMinutes(15));
      }
      return null;
    }).when(accountSecurityHelper).handleFailedLoginAttempt(any(User.class));
    
    // When
    try {
      authService.authenticateUser(loginDto, request);
    } catch (AuthenticationException e) {
      // Expected
    }
    
    // Then
    verify(accountSecurityHelper).handleFailedLoginAttempt(user);
    assertThat(user.getFailedLoginAttempts()).isEqualTo(5);
    assertThat(user.getAccountLockedUntil()).isNotNull();
  }
  
  @Test
  @DisplayName("Should reset failed login attempts on successful login")
  void shouldResetFailedLoginAttemptsOnSuccessfulLogin() {
    // Given
    LoginRequestDto loginDto = AuthTestFixtures.createLoginRequestDto();
    User user = AuthTestFixtures.createUserWithFailedAttempts(3);
    user.setEnabled(true);
    JwtResponseDto expectedResponse = AuthTestFixtures.createJwtResponseDto();
    
    when(userAuthenticationHelper.findUserByUsernameOrEmail(loginDto.getUsernameOrEmail())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())).thenReturn(true);
    doAnswer(invocation -> {
      User u = invocation.getArgument(0);
      u.resetFailedLoginAttempts();
      return null;
    }).when(accountSecurityHelper).resetFailedLoginAttempts(any(User.class));
    JwtTokenService.JwtTokenData tokenData = new JwtTokenService.JwtTokenData(
        AuthTestFixtures.TEST_ACCESS_TOKEN,
        AuthTestFixtures.TEST_REFRESH_TOKEN,
        AuthTestFixtures.TEST_TOKEN_EXPIRES_AT
    );
    when(jwtTokenService.generateTokens(any(User.class))).thenReturn(tokenData);
    when(authMapper.toJwtResponse(any(), anyString(), anyString(), any())).thenReturn(expectedResponse);
    
    // When
    authService.authenticateUser(loginDto, request);
    
    // Then
    verify(accountSecurityHelper).resetFailedLoginAttempts(user);
    assertThat(user.getFailedLoginAttempts()).isEqualTo(0);
    assertThat(user.getAccountLockedUntil()).isNull();
  }
  
  // ==================== refreshToken() Tests ====================
  
  @Test
  @DisplayName("Should successfully refresh token with valid refresh token")
  void shouldSuccessfullyRefreshTokenWithValidRefreshToken() {
    // Given
    String refreshToken = AuthTestFixtures.TEST_REFRESH_TOKEN;
    User user = UserTestFixtures.createUser();
    JwtResponseDto expectedResponse = AuthTestFixtures.createJwtResponseDto();
    
    when(jwtTokenService.isValidToken(refreshToken)).thenReturn(true);
    when(jwtTokenService.isTokenExpired(refreshToken)).thenReturn(false);
    when(tokenBlacklistService.isTokenBlacklisted(refreshToken)).thenReturn(false);
    when(jwtTokenService.getUsernameFromToken(refreshToken)).thenReturn(user.getUsername());
    when(userRepository.findByUsernameAndEnabledTrue(user.getUsername())).thenReturn(Optional.of(user));
    doNothing().when(userAuthenticationHelper).validateAccountStatus(user);
    JwtTokenService.JwtTokenData tokenData = new JwtTokenService.JwtTokenData(
        AuthTestFixtures.TEST_ACCESS_TOKEN,
        "new_refresh_token",
        AuthTestFixtures.TEST_TOKEN_EXPIRES_AT
    );
    when(jwtTokenService.generateTokens(any(User.class))).thenReturn(tokenData);
    when(jwtTokenService.getTokenExpirationTime(refreshToken)).thenReturn(AuthTestFixtures.TEST_TOKEN_EXPIRES_AT.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
    when(tokenBlacklistService.blacklistToken(eq(refreshToken), anyLong())).thenReturn(true);
    when(authMapper.toJwtResponse(any(), anyString(), anyString(), any())).thenReturn(expectedResponse);
    
    // When
    JwtResponseDto result = authService.refreshToken(refreshToken);
    
    // Then
    assertThat(result).isNotNull();
    verify(jwtTokenService).isValidToken(refreshToken);
    verify(jwtTokenService).isTokenExpired(refreshToken);
    verify(tokenBlacklistService).isTokenBlacklisted(refreshToken);
    verify(jwtTokenService).getUsernameFromToken(refreshToken);
    verify(userAuthenticationHelper).validateAccountStatus(user);
    verify(jwtTokenService).generateTokens(any(User.class));
    verify(jwtTokenService).getTokenExpirationTime(refreshToken);
    verify(tokenBlacklistService).blacklistToken(eq(refreshToken), anyLong());
  }
  
  @Test
  @DisplayName("Should throw AuthenticationException for invalid token format")
  void shouldThrowAuthenticationExceptionForInvalidTokenFormat() {
    // Given
    String invalidToken = AuthTestFixtures.TEST_INVALID_TOKEN;
    
    when(jwtTokenService.isValidToken(invalidToken)).thenReturn(false);
    
    // When & Then
    assertThatThrownBy(() -> authService.refreshToken(invalidToken))
        .isInstanceOf(AuthenticationException.class)
        .hasMessageContaining("Invalid refresh token");
    
    verify(jwtTokenService).isValidToken(invalidToken);
    verify(tokenBlacklistService, never()).isTokenBlacklisted(anyString());
  }
  
  @Test
  @DisplayName("Should throw AuthenticationException for null token")
  void shouldThrowAuthenticationExceptionForNullToken() {
    // Given
    when(jwtTokenService.isValidToken(null)).thenReturn(false);
    
    // When & Then
    assertThatThrownBy(() -> authService.refreshToken(null))
        .isInstanceOf(AuthenticationException.class)
        .hasMessageContaining("Invalid refresh token");
    
    verify(jwtTokenService).isValidToken(null);
  }
  
  @Test
  @DisplayName("Should throw AuthenticationException for empty token")
  void shouldThrowAuthenticationExceptionForEmptyToken() {
    // Given
    when(jwtTokenService.isValidToken("")).thenReturn(false);
    
    // When & Then
    assertThatThrownBy(() -> authService.refreshToken(""))
        .isInstanceOf(AuthenticationException.class)
        .hasMessageContaining("Invalid refresh token");
  }
  
  @Test
  @DisplayName("Should throw AuthenticationException for expired token")
  void shouldThrowAuthenticationExceptionForExpiredToken() {
    // Given
    String expiredToken = AuthTestFixtures.TEST_REFRESH_TOKEN;
    when(jwtTokenService.isValidToken(expiredToken)).thenReturn(true);
    when(jwtTokenService.isTokenExpired(expiredToken)).thenReturn(true);
    
    // When & Then
    assertThatThrownBy(() -> authService.refreshToken(expiredToken))
        .isInstanceOf(AuthenticationException.class)
        .hasMessageContaining("Refresh token has expired");
    
    verify(jwtTokenService).isValidToken(expiredToken);
    verify(jwtTokenService).isTokenExpired(expiredToken);
    verify(tokenBlacklistService, never()).isTokenBlacklisted(anyString());
  }
  
  @Test
  @DisplayName("Should throw AuthenticationException for blacklisted refresh token")
  void shouldThrowAuthenticationExceptionForBlacklistedRefreshToken() {
    // Given
    String blacklistedToken = AuthTestFixtures.TEST_BLACKLISTED_TOKEN;
    
    when(jwtTokenService.isValidToken(blacklistedToken)).thenReturn(true);
    when(jwtTokenService.isTokenExpired(blacklistedToken)).thenReturn(false);
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
    
    when(jwtTokenService.isValidToken(refreshToken)).thenReturn(true);
    when(jwtTokenService.isTokenExpired(refreshToken)).thenReturn(false);
    when(tokenBlacklistService.isTokenBlacklisted(refreshToken)).thenReturn(false);
    when(jwtTokenService.getUsernameFromToken(refreshToken)).thenReturn("nonexistent");
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
    
    when(jwtTokenService.isValidToken(refreshToken)).thenReturn(true);
    when(jwtTokenService.isTokenExpired(refreshToken)).thenReturn(false);
    when(tokenBlacklistService.isTokenBlacklisted(refreshToken)).thenReturn(false);
    when(jwtTokenService.getUsernameFromToken(refreshToken)).thenReturn(lockedUser.getUsername());
    when(userRepository.findByUsernameAndEnabledTrue(lockedUser.getUsername())).thenReturn(Optional.of(lockedUser));
    doThrow(new AccountLockedException("Account is temporarily locked")).when(userAuthenticationHelper).validateAccountStatus(lockedUser);
    
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
    
    when(jwtTokenService.isValidToken(refreshToken)).thenReturn(true);
    when(jwtTokenService.isTokenExpired(refreshToken)).thenReturn(false);
    when(tokenBlacklistService.isTokenBlacklisted(refreshToken)).thenReturn(false);
    when(jwtTokenService.getUsernameFromToken(refreshToken)).thenReturn(disabledUser.getUsername());
    when(userRepository.findByUsernameAndEnabledTrue(disabledUser.getUsername())).thenReturn(Optional.of(disabledUser));
    doThrow(new AccountDisabledException("Account is disabled")).when(userAuthenticationHelper).validateAccountStatus(disabledUser);
    
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
    
    when(jwtTokenService.isValidToken(refreshToken)).thenReturn(true);
    when(jwtTokenService.isTokenExpired(refreshToken)).thenReturn(false);
    when(tokenBlacklistService.isTokenBlacklisted(refreshToken)).thenReturn(false);
    when(jwtTokenService.getUsernameFromToken(refreshToken)).thenReturn(user.getUsername());
    when(userRepository.findByUsernameAndEnabledTrue(user.getUsername())).thenReturn(Optional.of(user));
    JwtTokenService.JwtTokenData tokenData = new JwtTokenService.JwtTokenData(
        AuthTestFixtures.TEST_ACCESS_TOKEN,
        "new_refresh_token",
        AuthTestFixtures.TEST_TOKEN_EXPIRES_AT
    );
    when(jwtTokenService.generateTokens(any(User.class))).thenReturn(tokenData);
    when(jwtTokenService.getTokenExpirationTime(refreshToken)).thenReturn(AuthTestFixtures.TEST_TOKEN_EXPIRES_AT.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
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
    
    when(jwtTokenService.parseJwtFromRequest(request)).thenReturn(accessToken);
    when(jwtTokenService.isValidToken(accessToken)).thenReturn(true);
    when(jwtTokenService.getTokenExpirationTime(accessToken)).thenReturn(AuthTestFixtures.TEST_TOKEN_EXPIRES_AT.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
    when(tokenBlacklistService.blacklistToken(eq(accessToken), anyLong())).thenReturn(true);
    
    // When
    authService.logoutUser(request);
    
    // Then
    verify(jwtTokenService).parseJwtFromRequest(request);
    verify(jwtTokenService).isValidToken(accessToken);
    verify(jwtTokenService).getTokenExpirationTime(accessToken);
    verify(tokenBlacklistService).blacklistToken(eq(accessToken), anyLong());
  }
  
  @Test
  @DisplayName("Should handle missing Authorization header gracefully")
  void shouldHandleMissingAuthorizationHeaderGracefully() {
    // Given
    when(jwtTokenService.parseJwtFromRequest(request)).thenReturn(null);
    
    // When
    authService.logoutUser(request);
    
    // Then - should not throw exception
    verify(jwtTokenService).parseJwtFromRequest(request);
    verify(jwtTokenService, never()).isValidToken(anyString());
    verify(tokenBlacklistService, never()).blacklistToken(anyString(), anyLong());
  }
  
  @Test
  @DisplayName("Should handle invalid Bearer token format gracefully")
  void shouldHandleInvalidBearerTokenFormatGracefully() {
    // Given
    when(jwtTokenService.parseJwtFromRequest(request)).thenReturn(null);
    
    // When
    authService.logoutUser(request);
    
    // Then - should not throw exception
    verify(jwtTokenService).parseJwtFromRequest(request);
    verify(jwtTokenService, never()).isValidToken(anyString());
    verify(tokenBlacklistService, never()).blacklistToken(anyString(), anyLong());
  }
  
  @Test
  @DisplayName("Should handle JWT exceptions gracefully")
  void shouldHandleJwtExceptionsGracefully() {
    // Given
    String accessToken = AuthTestFixtures.TEST_ACCESS_TOKEN;
    
    when(jwtTokenService.parseJwtFromRequest(request)).thenReturn(accessToken);
    when(jwtTokenService.isValidToken(accessToken)).thenReturn(false);
    
    // When
    authService.logoutUser(request);
    
    // Then - should not throw exception
    verify(jwtTokenService).parseJwtFromRequest(request);
    verify(jwtTokenService).isValidToken(accessToken);
    verify(tokenBlacklistService, never()).blacklistToken(anyString(), anyLong());
  }
  
  @Test
  @DisplayName("Should never throw exception during logout")
  void shouldNeverThrowExceptionDuringLogout() {
    // Given
    String accessToken = AuthTestFixtures.TEST_ACCESS_TOKEN;
    
    when(jwtTokenService.parseJwtFromRequest(request)).thenReturn(accessToken);
    when(jwtTokenService.isValidToken(accessToken)).thenThrow(new RuntimeException("JWT error"));
    
    // When & Then - should not throw
    authService.logoutUser(request);
    
    verify(jwtTokenService).parseJwtFromRequest(request);
  }
}
