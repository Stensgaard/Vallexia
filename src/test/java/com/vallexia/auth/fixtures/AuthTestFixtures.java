package com.vallexia.auth.fixtures;

import com.vallexia.auth.dto.JwtResponseDto;
import com.vallexia.auth.dto.LoginRequestDto;
import com.vallexia.auth.dto.RefreshTokenRequestDto;
import com.vallexia.auth.dto.RegisterRequestDto;
import com.vallexia.user.entity.Role;
import com.vallexia.user.entity.User;
import com.vallexia.user.fixtures.UserTestFixtures;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Test fixtures for authentication testing.
 * Provides reusable test data and builder methods.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
public class AuthTestFixtures {
  
  // Test Constants
  public static final String TEST_PASSWORD = "TestPass123!";
  public static final String TEST_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.access.token";
  public static final String TEST_REFRESH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.refresh.token";
  public static final String TEST_INVALID_TOKEN = "invalid.token.here";
  public static final String TEST_EMPTY_TOKEN = "";
  public static final String TEST_BLACKLISTED_TOKEN = "blacklisted.token.here";
  public static final LocalDateTime TEST_TOKEN_EXPIRES_AT = LocalDateTime.now().plusHours(1);
  public static final LocalDateTime TEST_TOKEN_EXPIRED_AT = LocalDateTime.now().minusHours(1);
  
  /**
   * Creates a valid RegisterRequestDto.
   */
  public static RegisterRequestDto createRegisterRequestDto() {
    RegisterRequestDto dto = new RegisterRequestDto();
    dto.setUsername(UserTestFixtures.TEST_USERNAME);
    dto.setEmail(UserTestFixtures.TEST_EMAIL);
    dto.setPassword(TEST_PASSWORD);
    dto.setConfirmPassword(TEST_PASSWORD);
    return dto;
  }
  
  /**
   * Creates a RegisterRequestDto with password mismatch.
   */
  public static RegisterRequestDto createRegisterRequestDtoWithPasswordMismatch() {
    RegisterRequestDto dto = createRegisterRequestDto();
    dto.setConfirmPassword("DifferentPass123!");
    return dto;
  }
  
  /**
   * Creates a RegisterRequestDto with existing username.
   */
  public static RegisterRequestDto createRegisterRequestDtoWithExistingUsername() {
    RegisterRequestDto dto = createRegisterRequestDto();
    dto.setUsername("existinguser");
    return dto;
  }
  
  /**
   * Creates a RegisterRequestDto with existing email.
   */
  public static RegisterRequestDto createRegisterRequestDtoWithExistingEmail() {
    RegisterRequestDto dto = createRegisterRequestDto();
    dto.setEmail("existing@example.com");
    return dto;
  }
  
  /**
   * Creates a valid LoginRequestDto with username.
   */
  public static LoginRequestDto createLoginRequestDto() {
    LoginRequestDto dto = new LoginRequestDto();
    dto.setUsernameOrEmail(UserTestFixtures.TEST_USERNAME);
    dto.setPassword(TEST_PASSWORD);
    return dto;
  }
  
  /**
   * Creates a LoginRequestDto with email.
   */
  public static LoginRequestDto createLoginRequestDtoWithEmail() {
    LoginRequestDto dto = new LoginRequestDto();
    dto.setUsernameOrEmail(UserTestFixtures.TEST_EMAIL);
    dto.setPassword(TEST_PASSWORD);
    return dto;
  }
  
  /**
   * Creates a LoginRequestDto with invalid credentials.
   */
  public static LoginRequestDto createLoginRequestDtoWithInvalidPassword() {
    LoginRequestDto dto = createLoginRequestDto();
    dto.setPassword("WrongPass123!");
    return dto;
  }
  
  /**
   * Creates a valid RefreshTokenRequestDto.
   */
  public static RefreshTokenRequestDto createRefreshTokenRequestDto() {
    RefreshTokenRequestDto dto = new RefreshTokenRequestDto();
    dto.setRefreshToken(TEST_REFRESH_TOKEN);
    return dto;
  }
  
  /**
   * Creates a JwtResponseDto with test data.
   */
  public static JwtResponseDto createJwtResponseDto() {
    return JwtResponseDto.builder()
        .accessToken(TEST_ACCESS_TOKEN)
        .refreshToken(TEST_REFRESH_TOKEN)
        .tokenType("Bearer")
        .id(UserTestFixtures.TEST_USER_ID)
        .username(UserTestFixtures.TEST_USERNAME)
        .email(UserTestFixtures.TEST_EMAIL)
        .expiresAt(TEST_TOKEN_EXPIRES_AT)
        .build();
  }
  
  /**
   * Creates a user with locked account.
   */
  public static User createLockedUser() {
    User user = UserTestFixtures.createUser();
    user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(15));
    user.setFailedLoginAttempts(5);
    return user;
  }
  
  /**
   * Creates a user with specified failed login attempts.
   */
  public static User createUserWithFailedAttempts(int attempts) {
    User user = UserTestFixtures.createUser();
    user.setFailedLoginAttempts(attempts);
    return user;
  }
  
  /**
   * Creates a user with account that will be locked after next failed attempt.
   */
  public static User createUserNearLockout(int maxAttempts) {
    User user = UserTestFixtures.createUser();
    user.setFailedLoginAttempts(maxAttempts - 1);
    return user;
  }
  
  /**
   * Creates a user with multiple roles.
   */
  public static User createUserWithRoles(Role... roles) {
    User user = UserTestFixtures.createUser();
    Set<Role> roleSet = new HashSet<>();
    for (Role role : roles) {
      roleSet.add(role);
    }
    user.setRoles(roleSet);
    return user;
  }
}
