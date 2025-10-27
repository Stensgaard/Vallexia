package com.vallexia.user.unit.controller;

import com.vallexia.security.AuthenticationHelper;
import com.vallexia.user.controller.UserController;
import com.vallexia.user.dto.UserProfileDto;
import com.vallexia.user.fixtures.UserTestFixtures;
import com.vallexia.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserController.
 * Tests REST endpoints with mocked dependencies.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserController Unit Tests")
class UserControllerTest {
  
  @Mock
  private UserService userService;
  
  @Mock
  private AuthenticationHelper authenticationHelper;
  
  @InjectMocks
  private UserController userController;
  
  private Authentication mockAuthentication;
  
  @BeforeEach
  void setUp() {
    mockAuthentication = createMockAuthentication();
  }
  
  private Authentication createMockAuthentication() {
    return new Authentication() {
      @Override
      public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
      }
      
      @Override
      public Object getCredentials() {
        return null;
      }
      
      @Override
      public Object getDetails() {
        return null;
      }
      
      @Override
      public Object getPrincipal() {
        return "testuser";
      }
      
      @Override
      public boolean isAuthenticated() {
        return true;
      }
      
      @Override
      public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        
      }
      
      @Override
      public String getName() {
        return "testuser";
      }
    };
  }
  
  // ==================== getCurrentUserProfile() Tests ====================
  
  @SuppressWarnings("null")
@Test
  @DisplayName("Should retrieve profile for authenticated user successfully")
  void shouldRetrieveProfileForAuthenticatedUserSuccessfully() {
    // Given
    Long userId = 1L;
    UserProfileDto expectedDto = UserTestFixtures.createUserProfileDto();
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(userService.getUserProfile(userId))
        .thenReturn(expectedDto);
    
    // When
    ResponseEntity<UserProfileDto> response = userController.getCurrentUserProfile(mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    UserProfileDto body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getId()).isEqualTo(userId);
    assertThat(body.getUsername()).isEqualTo("testuser");
    
    verify(authenticationHelper).getCurrentUserId(mockAuthentication);
    verify(userService).getUserProfile(userId);
  }
  
  @SuppressWarnings("null")
@Test
  @DisplayName("Should return 200 OK with correct profile data")
  void shouldReturnOkWithCorrectProfileData() {
    // Given
    Long userId = 1L;
    UserProfileDto expectedDto = UserTestFixtures.createUserProfileDto();
    expectedDto.setId(userId);
    expectedDto.setEmail("test@example.com");
    expectedDto.setFirstName("Test");
    expectedDto.setLastName("User");
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(userService.getUserProfile(userId))
        .thenReturn(expectedDto);
    
    // When
    ResponseEntity<UserProfileDto> response = userController.getCurrentUserProfile(mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    UserProfileDto body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getEmail()).isEqualTo("test@example.com");
    assertThat(body.getFirstName()).isEqualTo("Test");
    assertThat(body.getLastName()).isEqualTo("User");
  }
  
  @Test
  @DisplayName("Should handle authentication properly via AuthenticationHelper")
  void shouldHandleAuthenticationProperlyViaAuthenticationHelper() {
    // Given
    Long userId = 1L;
    UserProfileDto expectedDto = UserTestFixtures.createUserProfileDto();
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(userService.getUserProfile(userId))
        .thenReturn(expectedDto);
    
    // When
    userController.getCurrentUserProfile(mockAuthentication);
    
    // Then
    verify(authenticationHelper).getCurrentUserId(mockAuthentication);
    verify(userService).getUserProfile(userId);
  }
  
  // ==================== updateCurrentUserProfile() Tests ====================
  
  @SuppressWarnings("null")
@Test
  @DisplayName("Should update profile successfully with valid data")
  void shouldUpdateProfileSuccessfullyWithValidData() {
    // Given
    Long userId = 1L;
    UserProfileDto updateDto = UserTestFixtures.createUpdatedUserProfileDto();
    UserProfileDto expectedDto = UserTestFixtures.createUpdatedUserProfileDto();
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(userService.updateUserProfile(userId, updateDto))
        .thenReturn(expectedDto);
    
    // When
    ResponseEntity<UserProfileDto> response = userController.updateCurrentUserProfile(updateDto, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    UserProfileDto body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getEmail()).isEqualTo("updated@example.com");
    assertThat(body.getFirstName()).isEqualTo("Updated");
    
    verify(authenticationHelper).getCurrentUserId(mockAuthentication);
    verify(userService).updateUserProfile(userId, updateDto);
  }
  
  @Test
  @DisplayName("Should return 200 OK with updated profile")
  void shouldReturnOkWithUpdatedProfile() {
    // Given
    Long userId = 1L;
    UserProfileDto updateDto = UserTestFixtures.createUpdatedUserProfileDto();
    UserProfileDto expectedDto = UserTestFixtures.createUpdatedUserProfileDto();
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(userService.updateUserProfile(userId, updateDto))
        .thenReturn(expectedDto);
    
    // When
    ResponseEntity<UserProfileDto> response = userController.updateCurrentUserProfile(updateDto, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    UserProfileDto body = response.getBody();
    assertThat(body).isNotNull();
  }
  
  @Test
  @DisplayName("Should handle authentication properly when updating profile")
  void shouldHandleAuthenticationProperlyWhenUpdatingProfile() {
    // Given
    Long userId = 1L;
    UserProfileDto updateDto = UserTestFixtures.createUpdatedUserProfileDto();
    UserProfileDto expectedDto = UserTestFixtures.createUpdatedUserProfileDto();
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(userService.updateUserProfile(userId, updateDto))
        .thenReturn(expectedDto);
    
    // When
    userController.updateCurrentUserProfile(updateDto, mockAuthentication);
    
    // Then
    verify(authenticationHelper, times(1)).getCurrentUserId(mockAuthentication);
    verify(userService, times(1)).updateUserProfile(eq(userId), eq(updateDto));
  }
  
  @SuppressWarnings("null")
@Test
  @DisplayName("Should update profile with all fields")
  void shouldUpdateProfileWithAllFields() {
    // Given
    Long userId = 1L;
    UserProfileDto updateDto = UserTestFixtures.createUpdatedUserProfileDto();
    UserProfileDto expectedDto = UserTestFixtures.createUpdatedUserProfileDto();
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(userService.updateUserProfile(userId, updateDto))
        .thenReturn(expectedDto);
    
    // When
    ResponseEntity<UserProfileDto> response = userController.updateCurrentUserProfile(updateDto, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    UserProfileDto body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getEmail()).isEqualTo("updated@example.com");
    assertThat(body.getFirstName()).isEqualTo("Updated");
    assertThat(body.getLastName()).isEqualTo("Name");
    assertThat(body.getProfilePictureUrl()).isEqualTo("https://example.com/updated.jpg");
    assertThat(body.getHouseholdSize()).isEqualTo(4);
    assertThat(body.getMealsPerDay()).isEqualTo(5);
  }
  
  @Test
  @DisplayName("Should handle partial profile update")
  void shouldHandlePartialProfileUpdate() {
    // Given
    Long userId = 1L;
    UserProfileDto updateDto = UserTestFixtures.createUserProfileDto();
    updateDto.setEmail("newemail@example.com");
    updateDto.setFirstName(null); // Partial update
    
    UserProfileDto expectedDto = UserTestFixtures.createUserProfileDto();
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(userService.updateUserProfile(userId, updateDto))
        .thenReturn(expectedDto);
    
    // When
    ResponseEntity<UserProfileDto> response = userController.updateCurrentUserProfile(updateDto, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(userService).updateUserProfile(eq(userId), eq(updateDto));
  }
}

