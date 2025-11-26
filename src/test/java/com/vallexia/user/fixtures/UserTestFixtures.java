package com.vallexia.user.fixtures;

import com.vallexia.common.enums.SupportedCountry;
import com.vallexia.common.enums.SupportedDateFormat;
import com.vallexia.common.enums.SupportedFirstDayOfWeek;
import com.vallexia.common.enums.SupportedLocale;
import com.vallexia.common.enums.SupportedMeasurementSystem;
import com.vallexia.common.enums.SupportedTimezone;
import com.vallexia.user.dto.DietaryPreferencesDto;
import com.vallexia.user.dto.NutritionalGoalsDto;
import com.vallexia.user.dto.UserProfileDto;
import com.vallexia.user.dto.UserSettingsDto;
import com.vallexia.user.entity.DietaryPreferences;
import com.vallexia.user.entity.NutritionalGoals;
import com.vallexia.user.entity.User;
import com.vallexia.user.entity.UserSettings;
import com.vallexia.common.enums.SupportedAllergy;
import com.vallexia.common.enums.SupportedCuisineType;
import com.vallexia.common.enums.SupportedDietaryRestriction;
import com.vallexia.common.enums.SupportedMealCategory;
import com.vallexia.user.entity.enums.GoalType;
import com.vallexia.user.entity.enums.Role;
import com.vallexia.user.entity.enums.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Test fixtures for user testing.
 * Provides reusable test data and builder methods.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
public class UserTestFixtures {
  
  // Test Constants
  public static final Long TEST_USER_ID = 1L;
  public static final Long TEST_ADMIN_ID = 100L;
  public static final String TEST_USERNAME = "testuser";
  public static final String TEST_EMAIL = "test@example.com";
  public static final String TEST_PASSWORD_HASH = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
  public static final Integer TEST_HOUSEHOLD_SIZE = 2;
  public static final Set<SupportedMealCategory> TEST_MEAL_TYPES = Set.of(SupportedMealCategory.BREAKFAST, SupportedMealCategory.LUNCH, SupportedMealCategory.DINNER);
  
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
    user.setMealTypes(new HashSet<>(Set.of(SupportedMealCategory.BREAKFAST, SupportedMealCategory.LUNCH, SupportedMealCategory.DINNER, SupportedMealCategory.SNACK)));
    return user;
  }
  
  /**
   * Creates an admin user.
   */
  public static User createAdminUser() {
    User user = new User();
    user.setId(TEST_ADMIN_ID);
    user.setUsername("admin");
    user.setEmail("admin@example.com");
    user.setPasswordHash(TEST_PASSWORD_HASH);
    user.setHouseholdSize(1);
    user.setMealTypes(new HashSet<>(Set.of(SupportedMealCategory.BREAKFAST, SupportedMealCategory.DINNER)));
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
   * Creates an updated UserProfileDto with new data.
   */
  public static UserProfileDto createUpdatedUserProfileDto() {
    UserProfileDto dto = new UserProfileDto();
    dto.setId(TEST_USER_ID);
    dto.setUsername(TEST_USERNAME); // Username is immutable
    dto.setEmail("updated@example.com");
    dto.setEnabled(true);
    dto.setHouseholdSize(4);
    dto.setMealTypes(Set.of(SupportedMealCategory.BREAKFAST, SupportedMealCategory.LUNCH, SupportedMealCategory.DINNER, SupportedMealCategory.SNACK));
    dto.setSubscriptionStatus("PREMIUM");
    dto.setSubscriptionExpiresAt(LocalDateTime.now().plusMonths(1));
    return dto;
  }
  
  // ==================== DietaryPreferences Fixtures ====================
  
  /**
   * Creates a standard test DietaryPreferences entity.
   */
  public static DietaryPreferences createDietaryPreferences() {
    return createDietaryPreferences(createUser());
  }
  
  /**
   * Creates a DietaryPreferences entity with specified user.
   */
  public static DietaryPreferences createDietaryPreferences(User user) {
    DietaryPreferences preferences = new DietaryPreferences();
    preferences.setId(1L);
    preferences.setUser(user);
    preferences.setRestrictions(new HashSet<>(Set.of(
        SupportedDietaryRestriction.VEGETARIAN,
        SupportedDietaryRestriction.GLUTEN_FREE
    )));
    preferences.setAllergies(new HashSet<>(Set.of(
        SupportedAllergy.PEANUTS,
        SupportedAllergy.MILK
    )));
    preferences.setPreferredCuisines(new HashSet<>(Set.of(
        SupportedCuisineType.ITALIAN,
        SupportedCuisineType.MEDITERRANEAN
    )));
    preferences.setCreatedAt(LocalDateTime.now().minusDays(10));
    preferences.setUpdatedAt(LocalDateTime.now().minusDays(1));
    return preferences;
  }
  
  /**
   * Creates a standard DietaryPreferencesDto.
   */
  public static DietaryPreferencesDto createDietaryPreferencesDto() {
    DietaryPreferencesDto dto = new DietaryPreferencesDto();
    dto.setId(1L);
    dto.setUserId(TEST_USER_ID);
    dto.setRestrictions(new HashSet<>(Set.of(
        SupportedDietaryRestriction.VEGETARIAN,
        SupportedDietaryRestriction.GLUTEN_FREE
    )));
    dto.setAllergies(new HashSet<>(Set.of(
        SupportedAllergy.PEANUTS,
        SupportedAllergy.MILK
    )));
    dto.setPreferredCuisines(new HashSet<>(Set.of(
        SupportedCuisineType.ITALIAN,
        SupportedCuisineType.MEDITERRANEAN
    )));
    return dto;
  }
  
  /**
   * Creates an updated DietaryPreferencesDto with new data.
   */
  public static DietaryPreferencesDto createUpdatedDietaryPreferencesDto() {
    DietaryPreferencesDto dto = new DietaryPreferencesDto();
    dto.setId(1L);
    dto.setUserId(TEST_USER_ID);
    dto.setRestrictions(new HashSet<>(Set.of(
        SupportedDietaryRestriction.VEGAN,
        SupportedDietaryRestriction.KETO,
        SupportedDietaryRestriction.LOW_SODIUM
    )));
    dto.setAllergies(new HashSet<>(Set.of(
        SupportedAllergy.TREE_NUTS,
        SupportedAllergy.SOY,
        SupportedAllergy.WHEAT
    )));
    dto.setPreferredCuisines(new HashSet<>(Set.of(
        SupportedCuisineType.JAPANESE,
        SupportedCuisineType.THAI,
        SupportedCuisineType.INDIAN
    )));
    return dto;
  }
  
  // ==================== NutritionalGoals Fixtures ====================
  
  /**
   * Creates a standard test NutritionalGoals entity with default values.
   */
  public static NutritionalGoals createNutritionalGoals() {
    return createNutritionalGoals(createUser());
  }
  
  /**
   * Creates a NutritionalGoals entity with specified user.
   */
  public static NutritionalGoals createNutritionalGoals(User user) {
    NutritionalGoals goals = new NutritionalGoals();
    goals.setId(1L);
    goals.setUser(user);
    goals.setDailyCalories(BigDecimal.valueOf(2000));
    goals.setDailyProtein(BigDecimal.valueOf(150));
    goals.setDailyCarbs(BigDecimal.valueOf(250));
    goals.setDailyFats(BigDecimal.valueOf(67));
    goals.setDailyFiber(BigDecimal.valueOf(25));
    goals.setDailySodium(BigDecimal.valueOf(2300));
    goals.setDailySugar(BigDecimal.valueOf(50));
    goals.setProteinPercentage(BigDecimal.valueOf(30));
    goals.setCarbsPercentage(BigDecimal.valueOf(50));
    goals.setFatsPercentage(BigDecimal.valueOf(20));
    goals.setGoalType(GoalType.MAINTENANCE);
    goals.setCreatedAt(LocalDateTime.now().minusDays(10));
    goals.setUpdatedAt(LocalDateTime.now().minusDays(1));
    return goals;
  }
  
  /**
   * Creates a standard NutritionalGoalsDto.
   */
  public static NutritionalGoalsDto createNutritionalGoalsDto() {
    NutritionalGoalsDto dto = new NutritionalGoalsDto();
    dto.setId(1L);
    dto.setUserId(TEST_USER_ID);
    dto.setDailyCalories(BigDecimal.valueOf(2000));
    dto.setDailyProtein(BigDecimal.valueOf(150));
    dto.setDailyCarbs(BigDecimal.valueOf(250));
    dto.setDailyFats(BigDecimal.valueOf(67));
    dto.setDailyFiber(BigDecimal.valueOf(25));
    dto.setDailySodium(BigDecimal.valueOf(2300));
    dto.setDailySugar(BigDecimal.valueOf(50));
    dto.setProteinPercentage(BigDecimal.valueOf(30));
    dto.setCarbsPercentage(BigDecimal.valueOf(50));
    dto.setFatsPercentage(BigDecimal.valueOf(20));
    dto.setGoalType("MAINTENANCE");
    return dto;
  }
  
  /**
   * Creates an updated NutritionalGoalsDto with new data.
   */
  public static NutritionalGoalsDto createUpdatedNutritionalGoalsDto() {
    NutritionalGoalsDto dto = new NutritionalGoalsDto();
    dto.setId(1L);
    dto.setUserId(TEST_USER_ID);
    dto.setDailyCalories(BigDecimal.valueOf(2500));
    dto.setDailyProtein(BigDecimal.valueOf(200));
    dto.setDailyCarbs(BigDecimal.valueOf(300));
    dto.setDailyFats(BigDecimal.valueOf(83));
    dto.setDailyFiber(BigDecimal.valueOf(30));
    dto.setDailySodium(BigDecimal.valueOf(2000));
    dto.setDailySugar(BigDecimal.valueOf(40));
    dto.setProteinPercentage(BigDecimal.valueOf(32));
    dto.setCarbsPercentage(BigDecimal.valueOf(48));
    dto.setFatsPercentage(BigDecimal.valueOf(20));
    dto.setGoalType("MUSCLE_GAIN");
    return dto;
  }
  
  // ==================== UserSettings Fixtures ====================
  
  /**
   * Creates a standard test UserSettings entity.
   */
  public static UserSettings createUserSettings() {
    return createUserSettings(createUser());
  }
  
  /**
   * Creates a UserSettings entity with specified user.
   */
  public static UserSettings createUserSettings(User user) {
    UserSettings settings = new UserSettings();
    settings.setId(1L);
    settings.setUser(user);
    settings.setLanguage(SupportedLocale.EN.getCode());
    settings.setCountry(SupportedCountry.US.getCountryCode());
    settings.setDateFormat(SupportedDateFormat.MM_DD_YYYY.name());
    settings.setTimezone(SupportedTimezone.AMERICA_NEW_YORK.getValue());
    settings.setFirstDayOfWeek(SupportedFirstDayOfWeek.SUNDAY);
    settings.setMeasurementSystem(SupportedMeasurementSystem.IMPERIAL);
    settings.setNumberDecimalSeparator(".");
    settings.setNumberThousandsSeparator(",");
    settings.setCurrency(SupportedCountry.US.getCurrencyCode());
    settings.setCreatedAt(LocalDateTime.now().minusDays(10));
    settings.setUpdatedAt(LocalDateTime.now().minusDays(1));
    return settings;
  }
  
  /**
   * Creates a standard UserSettingsDto.
   */
  public static UserSettingsDto createUserSettingsDto() {
    UserSettingsDto dto = new UserSettingsDto();
    dto.setId(1L);
    dto.setUserId(TEST_USER_ID);
    dto.setLanguage(SupportedLocale.EN.getCode());
    dto.setCountry(SupportedCountry.US.getCountryCode());
    dto.setDateFormat(SupportedDateFormat.MM_DD_YYYY.name());
    dto.setTimezone(SupportedTimezone.AMERICA_NEW_YORK.getValue());
    dto.setFirstDayOfWeek(SupportedFirstDayOfWeek.SUNDAY.name());
    dto.setMeasurementSystem(SupportedMeasurementSystem.IMPERIAL.name());
    dto.setCurrency(SupportedCountry.US.getCurrencyCode());
    return dto;
  }
  
  /**
   * Creates an updated UserSettingsDto with new data.
   */
  public static UserSettingsDto createUpdatedUserSettingsDto() {
    UserSettingsDto dto = new UserSettingsDto();
    dto.setId(1L);
    dto.setUserId(TEST_USER_ID);
    dto.setLanguage(SupportedLocale.DA.getCode());
    dto.setCountry(SupportedCountry.DK.getCountryCode());
    dto.setDateFormat(SupportedDateFormat.DD_MM_YYYY_DOT.name());
    dto.setTimezone(SupportedTimezone.EUROPE_COPENHAGEN.getValue());
    dto.setFirstDayOfWeek(SupportedFirstDayOfWeek.MONDAY.name());
    dto.setMeasurementSystem(SupportedMeasurementSystem.METRIC.name());
    dto.setCurrency(SupportedCountry.DK.getCurrencyCode());
    return dto;
  }
}
