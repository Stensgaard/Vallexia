package com.vallexia.user.fixtures;

import com.vallexia.user.dto.UserProfileDto;
import com.vallexia.user.entity.enums.MealType;
import com.vallexia.user.entity.enums.Role;
import com.vallexia.user.entity.enums.SubscriptionStatus;
import com.vallexia.user.entity.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Test fixtures for user testing.
 * Provides reusable test data and builder methods.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
public class UserTestFixtures {
  
  // Test Constants
  public static final Long TEST_USER_ID = 1L;
  public static final Long TEST_ADMIN_ID = 100L;
  public static final String TEST_USERNAME = "testuser";
  public static final String TEST_ADMIN_USERNAME = "admin";
  public static final String TEST_EMAIL = "test@example.com";
  public static final String TEST_ADMIN_EMAIL = "admin@example.com";
  public static final String TEST_PASSWORD_HASH = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
  public static final String TEST_FIRST_NAME = "Test";
  public static final String TEST_LAST_NAME = "User";
  public static final Integer TEST_HOUSEHOLD_SIZE = 2;
  public static final Set<MealType> TEST_MEAL_TYPES = Set.of(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER);
  
  /**
   * Creates a standard test user.
   */
  public static User createUser() {
    return createUser(TEST_USER_ID);
  }
  
  /**
   * Creates a user with specified ID.
   */
  public static User createUser(Long id) {
    User user = new User();
    user.setId(id);
    user.setUsername(TEST_USERNAME);
    user.setEmail(TEST_EMAIL);
    user.setPasswordHash(TEST_PASSWORD_HASH);
    user.setHouseholdSize(TEST_HOUSEHOLD_SIZE);
    user.setMealTypes(new HashSet<>(TEST_MEAL_TYPES));
    user.setEnabled(true);
    user.setAccountNonExpired(true);
    user.setAccountNonLocked(true);
    user.setCredentialsNonExpired(true);
    user.setFailedLoginAttempts(0);
    user.setSubscriptionStatus(SubscriptionStatus.FREE);
    user.setSubscriptionExpiresAt(null);
    
    // Set default roles
    Set<Role> roles = new HashSet<>();
    roles.add(Role.USER);
    user.setRoles(roles);
    
    // Set timestamps
    user.setCreatedAt(LocalDateTime.now().minusDays(30));
    user.setUpdatedAt(LocalDateTime.now().minusDays(1));
    
    return user;
  }
  
  /**
   * Creates a user with full profile data.
   */
  public static User createUserWithProfile() {
    User user = createUser();
    user.setHouseholdSize(3);
    user.setMealTypes(new HashSet<>(Set.of(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK)));
    return user;
  }
  
  /**
   * Creates an admin user.
   */
  public static User createAdminUser() {
    User user = new User();
    user.setId(TEST_ADMIN_ID);
    user.setUsername(TEST_ADMIN_USERNAME);
    user.setEmail(TEST_ADMIN_EMAIL);
    user.setPasswordHash(TEST_PASSWORD_HASH);
    user.setHouseholdSize(1);
    user.setMealTypes(new HashSet<>(Set.of(MealType.BREAKFAST, MealType.DINNER)));
    user.setEnabled(true);
    user.setAccountNonExpired(true);
    user.setAccountNonLocked(true);
    user.setCredentialsNonExpired(true);
    user.setFailedLoginAttempts(0);
    user.setSubscriptionStatus(SubscriptionStatus.PREMIUM);
    user.setSubscriptionExpiresAt(LocalDateTime.now().plusMonths(1));
    
    // Set admin role
    Set<Role> roles = new HashSet<>();
    roles.add(Role.USER);
    roles.add(Role.ADMIN);
    user.setRoles(roles);
    
    user.setCreatedAt(LocalDateTime.now().minusMonths(6));
    user.setUpdatedAt(LocalDateTime.now());
    
    return user;
  }
  
  /**
   * Creates a disabled user.
   */
  public static User createDisabledUser() {
    User user = createUser();
    user.setEnabled(false);
    user.setAccountNonExpired(false);
    return user;
  }
  
  /**
   * Creates a user with specified email.
   */
  public static User createUserWithEmail(String email) {
    User user = createUser();
    user.setEmail(email);
    return user;
  }
  
  /**
   * Creates a user with specified username.
   */
  public static User createUserWithUsername(String username) {
    User user = createUser();
    user.setUsername(username);
    return user;
  }
  
  /**
   * Creates a standard UserProfileDto.
   */
  public static UserProfileDto createUserProfileDto() {
    UserProfileDto dto = new UserProfileDto();
    dto.setId(TEST_USER_ID);
    dto.setUsername(TEST_USERNAME);
    dto.setEmail(TEST_EMAIL);
    dto.setEnabled(true);
    dto.setHouseholdSize(TEST_HOUSEHOLD_SIZE);
    dto.setMealTypes(new HashSet<>(TEST_MEAL_TYPES));
    dto.setSubscriptionStatus("FREE");
    dto.setSubscriptionExpiresAt(null);
    return dto;
  }
  
  /**
   * Creates a UserProfileDto with specified user ID.
   */
  public static UserProfileDto createUserProfileDto(Long userId) {
    UserProfileDto dto = createUserProfileDto();
    dto.setId(userId);
    return dto;
  }
  
  /**
   * Creates an updated UserProfileDto with new data.
   */
  public static UserProfileDto createUpdatedUserProfileDto() {
    UserProfileDto dto = new UserProfileDto();
    dto.setId(TEST_USER_ID);
    dto.setUsername(TEST_USERNAME); // Username is immutable
    dto.setEmail("updated@example.com");
    dto.setEnabled(true);
    dto.setHouseholdSize(4);
    dto.setMealTypes(Set.of(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK));
    dto.setSubscriptionStatus("PREMIUM");
    dto.setSubscriptionExpiresAt(LocalDateTime.now().plusMonths(1));
    return dto;
  }
  
  /**
   * Creates an invalid UserProfileDto for testing validation.
   */
  public static UserProfileDto createInvalidUserProfileDto() {
    UserProfileDto dto = new UserProfileDto();
    dto.setEmail("invalid-email"); // Invalid email format
    dto.setHouseholdSize(25); // Exceeds max of 20
    dto.setMealTypes(new HashSet<>()); // Empty set should fail @NotEmpty validation
    return dto;
  }
  
  /**
   * Creates a UserProfileDto with boundary values for testing.
   */
  public static UserProfileDto createBoundaryValueUserProfileDto() {
    UserProfileDto dto = new UserProfileDto();
    dto.setId(TEST_USER_ID);
    dto.setUsername(TEST_USERNAME);
    dto.setEmail("a@b.co"); // Minimum valid email
    dto.setEnabled(true);
    dto.setHouseholdSize(1); // Minimum value
    dto.setMealTypes(Set.of(MealType.BREAKFAST)); // Minimum - one meal type
    dto.setSubscriptionStatus("FREE");
    return dto;
  }
  
  /**
   * Creates a minimal UserProfileDto with only required fields.
   */
  public static UserProfileDto createMinimalUserProfileDto() {
    UserProfileDto dto = new UserProfileDto();
    dto.setEmail(TEST_EMAIL); // Only required field
    return dto;
  }
}
