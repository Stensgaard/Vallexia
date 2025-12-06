package com.vallexia.config.unit.security;

import com.vallexia.config.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SecurityConfig.
 * Tests password encoder bean creation and BCrypt functionality.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {
  
  @Test
  @DisplayName("Should create PasswordEncoder bean")
  void shouldCreatePasswordEncoderBean() {
    // Given
    SecurityConfig config = new SecurityConfig();
    
    // When
    PasswordEncoder encoder = config.passwordEncoder();
    
    // Then
    assertThat(encoder).isNotNull();
    assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
  }
  
  @Test
  @DisplayName("Should create BCryptPasswordEncoder with strength 12")
  void shouldCreateBCryptPasswordEncoderWithStrength12() {
    // Given
    SecurityConfig config = new SecurityConfig();
    
    // When
    PasswordEncoder encoder = config.passwordEncoder();
    
    // Then
    assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
    // BCrypt strength 12 means rounds = 2^12 = 4096
    // We can verify by encoding and checking the hash format
    String encoded = encoder.encode("testPassword123");
    assertThat(encoded).startsWith("$2a$12$"); // BCrypt format with strength 12
  }
  
  @Test
  @DisplayName("Should encode password successfully")
  void shouldEncodePasswordSuccessfully() {
    // Given
    SecurityConfig config = new SecurityConfig();
    PasswordEncoder encoder = config.passwordEncoder();
    String rawPassword = "testPassword123";
    
    // When
    String encodedPassword = encoder.encode(rawPassword);
    
    // Then
    assertThat(encodedPassword).isNotNull();
    assertThat(encodedPassword).isNotEqualTo(rawPassword);
    assertThat(encodedPassword.length()).isGreaterThan(50); // BCrypt hashes are long
  }
  
  @Test
  @DisplayName("Should verify encoded password matches original")
  void shouldVerifyEncodedPasswordMatchesOriginal() {
    // Given
    SecurityConfig config = new SecurityConfig();
    PasswordEncoder encoder = config.passwordEncoder();
    String rawPassword = "testPassword123";
    String encodedPassword = encoder.encode(rawPassword);
    
    // When
    boolean matches = encoder.matches(rawPassword, encodedPassword);
    
    // Then
    assertThat(matches).isTrue();
  }
  
  @Test
  @DisplayName("Should fail verification for incorrect password")
  void shouldFailVerificationForIncorrectPassword() {
    // Given
    SecurityConfig config = new SecurityConfig();
    PasswordEncoder encoder = config.passwordEncoder();
    String correctPassword = "testPassword123";
    String incorrectPassword = "wrongPassword456";
    String encodedPassword = encoder.encode(correctPassword);
    
    // When
    boolean matches = encoder.matches(incorrectPassword, encodedPassword);
    
    // Then
    assertThat(matches).isFalse();
  }
  
  @Test
  @DisplayName("Should produce different hashes for same password")
  void shouldProduceDifferentHashesForSamePassword() {
    // Given
    SecurityConfig config = new SecurityConfig();
    PasswordEncoder encoder = config.passwordEncoder();
    String password = "testPassword123";
    
    // When
    String encoded1 = encoder.encode(password);
    String encoded2 = encoder.encode(password);
    
    // Then
    assertThat(encoded1).isNotEqualTo(encoded2); // BCrypt uses random salt
    // But both should verify correctly
    assertThat(encoder.matches(password, encoded1)).isTrue();
    assertThat(encoder.matches(password, encoded2)).isTrue();
  }
}
