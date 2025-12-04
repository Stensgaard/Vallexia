package com.vallexia.recipe.unit.service;

import com.vallexia.audit.entity.enums.EventType;
import com.vallexia.audit.service.AuditService;
import com.vallexia.recipe.dto.CreateRecipeDto;
import com.vallexia.recipe.dto.RecipeDto;
import com.vallexia.recipe.dto.UpdateRecipeDto;
import com.vallexia.recipe.entity.*;
import com.vallexia.recipe.exception.RecipeNotFoundException;
import com.vallexia.recipe.fixtures.RecipeTestFixtures;
import com.vallexia.recipe.mapper.RecipeMapper;
import com.vallexia.recipe.repository.*;
import com.vallexia.recipe.service.FavoriteRecipeService;
import com.vallexia.recipe.service.RecipeNutritionService;
import com.vallexia.recipe.service.RecipeService;
import com.vallexia.security.AuthenticationHelper;
import com.vallexia.user.entity.User;
import com.vallexia.user.exception.UserNotFoundException;
import com.vallexia.user.fixtures.UserTestFixtures;
import com.vallexia.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import org.mockito.ArgumentMatchers;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RecipeService.
 * Tests business logic with mocked dependencies.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
@DisplayName("RecipeService Unit Tests")
class RecipeServiceTest {
  
  @Mock
  private RecipeRepository recipeRepository;
  
  @Mock
  private IngredientRepository ingredientRepository;
  
  @Mock
  private RecipeIngredientRepository recipeIngredientRepository;
  
  @Mock
  private NutritionalInfoRepository nutritionalInfoRepository;
  
  @Mock
  private UserRepository userRepository;
  
  @Mock
  private RecipeMapper recipeMapper;
  
  @Mock
  private FavoriteRecipeService favoriteRecipeService;
  
  @Mock
  private RecipeNutritionService recipeNutritionService;
  
  @Mock
  private AuditService auditService;
  
  @Mock
  private AuthenticationHelper authenticationHelper;
  
  @Mock
  private com.vallexia.user.service.UserSettingsService userSettingsService;
  
  @Mock
  private com.vallexia.recipe.service.RecipeEnrichmentService recipeEnrichmentService;
  
  @Mock
  private com.vallexia.recipe.repository.RecipeTranslationRepository recipeTranslationRepository;
  
  @InjectMocks
  private RecipeService recipeService;
  
  // ==================== Helper Methods ====================
  
  /**
   * Sets up user locale mock with default English language.
   */
  private void setupUserLocaleMock() {
    when(userSettingsService.getUserLocale(any(Long.class)))
        .thenReturn("en");
    when(userSettingsService.getUserLocale(null))
        .thenReturn("en");
  }
  
  /**
   * Sets up recipe enrichment service mock to return enriched DTO.
   */
  private void setupRecipeEnrichmentServiceMock() {
    when(recipeEnrichmentService.enrichWithTranslations(any(RecipeDto.class), any(Recipe.class), any(String.class)))
        .thenAnswer(invocation -> {
          RecipeDto dto = invocation.getArgument(0);
          return dto; // Return the DTO as-is for testing
        });
  }
  
  /**
   * Sets up nutritional info mapper mock to convert DTO to entity.
   */
  private void setupNutritionalInfoMapperMock() {
    when(recipeMapper.toNutritionalInfo(any(com.vallexia.recipe.dto.NutritionalInfoDto.class)))
        .thenAnswer(invocation -> {
          NutritionalInfo ni = new NutritionalInfo();
          com.vallexia.recipe.dto.NutritionalInfoDto nutritionalDto = invocation.getArgument(0);
          if (nutritionalDto != null) {
            ni.setCalories(nutritionalDto.getCalories());
            ni.setProtein(nutritionalDto.getProtein());
            ni.setCarbs(nutritionalDto.getCarbs());
            ni.setFats(nutritionalDto.getFats());
          }
          return ni;
        });
  }
  
  /**
   * Sets up nutritional info repository mock to return saved entity.
   */
  private void setupNutritionalInfoRepositoryMock() {
    when(nutritionalInfoRepository.save(any(NutritionalInfo.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }
  
  /**
   * Sets up favorite recipe service mock.
   * 
   * @param isFavorite whether the recipe should be marked as favorite
   * @param userId user ID to check favorite status for
   */
  private void setupFavoriteRecipeMock(boolean isFavorite, Long userId) {
    when(favoriteRecipeService.isFavorite(any(Long.class), eq(userId)))
        .thenReturn(isFavorite);
  }
  
  /**
   * Sets up favorite recipe service mock with any user ID.
   * 
   * @param isFavorite whether the recipe should be marked as favorite
   */
  private void setupFavoriteRecipeMock(boolean isFavorite) {
    when(favoriteRecipeService.isFavorite(any(), any()))
        .thenReturn(isFavorite);
  }
  
  // ==================== createRecipe() Tests ====================
  
  @Test
  @DisplayName("Should create recipe successfully")
  void shouldCreateRecipeSuccessfully() {
    // Given
    CreateRecipeDto dto = RecipeTestFixtures.createCreateRecipeDto();
    User creator = UserTestFixtures.createUser();
    Recipe recipe = RecipeTestFixtures.createRecipe();
    RecipeDto expectedDto = new RecipeDto();
    expectedDto.setId(RecipeTestFixtures.TEST_RECIPE_ID);
    expectedDto.setName(dto.getName());
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(creator));
    when(recipeMapper.toRecipe(dto))
        .thenReturn(recipe);
    when(recipeRepository.save(any(Recipe.class)))
        .thenReturn(recipe);
    when(ingredientRepository.findByNameIgnoreCase(any(String.class)))
        .thenReturn(Optional.empty());
    when(ingredientRepository.save(any(Ingredient.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    RecipeIngredient recipeIngredient = new RecipeIngredient();
    recipeIngredient.setId(1L);
    List<RecipeIngredient> mockIngredients = List.of(recipeIngredient);
    when(recipeMapper.toRecipeIngredient(any(com.vallexia.recipe.dto.IngredientDto.class)))
        .thenReturn(recipeIngredient);
    when(recipeIngredientRepository.saveAll(anyList()))
        .thenReturn(mockIngredients);
    setupNutritionalInfoMapperMock();
    setupNutritionalInfoRepositoryMock();
    setupUserLocaleMock();
    setupRecipeEnrichmentServiceMock();
    setupFavoriteRecipeMock(false, UserTestFixtures.TEST_USER_ID);
    when(recipeMapper.toRecipeDto(recipe, false))
        .thenReturn(expectedDto);
    
    // When
    RecipeDto result = recipeService.createRecipe(dto, UserTestFixtures.TEST_USER_ID);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(RecipeTestFixtures.TEST_RECIPE_ID);
    verify(recipeRepository, atLeastOnce()).save(any(Recipe.class));
    verify(auditService).logEvent(eq(EventType.RECIPE_CREATED), eq(UserTestFixtures.TEST_USER_ID), any(String.class));
  }
  
  @Test
  @DisplayName("Should throw UserNotFoundException when creator doesn't exist")
  void shouldThrowUserNotFoundExceptionWhenCreatorDoesNotExist() {
    // Given
    CreateRecipeDto dto = RecipeTestFixtures.createCreateRecipeDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.empty());
    
    // When & Then
    assertThatThrownBy(() -> recipeService.createRecipe(dto, UserTestFixtures.TEST_USER_ID))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found with id: " + UserTestFixtures.TEST_USER_ID);
    
    verify(recipeRepository, never()).save(any());
  }
  
  @Test
  @DisplayName("Should calculate total time when prep and cook times are provided")
  void shouldCalculateTotalTimeWhenPrepAndCookTimesAreProvided() {
    // Given
    CreateRecipeDto dto = RecipeTestFixtures.createCreateRecipeDto();
    User creator = UserTestFixtures.createUser();
    Recipe recipe = RecipeTestFixtures.createRecipe();
    RecipeDto expectedDto = new RecipeDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(creator));
    when(recipeMapper.toRecipe(dto))
        .thenReturn(recipe);
    when(recipeRepository.save(any(Recipe.class)))
        .thenReturn(recipe);
    when(ingredientRepository.findByNameIgnoreCase(any(String.class)))
        .thenReturn(Optional.empty());
    when(ingredientRepository.save(any(Ingredient.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    RecipeIngredient testRecipeIngredient = new RecipeIngredient();
    testRecipeIngredient.setId(1L);
    when(recipeMapper.toRecipeIngredient(any(com.vallexia.recipe.dto.IngredientDto.class)))
        .thenReturn(testRecipeIngredient);
    when(recipeIngredientRepository.saveAll(anyList()))
        .thenReturn(List.of(testRecipeIngredient));
    setupNutritionalInfoMapperMock();
    setupNutritionalInfoRepositoryMock();
    doNothing().when(auditService).logEvent(any(EventType.class), any(Long.class), any(String.class));
    setupUserLocaleMock();
    setupFavoriteRecipeMock(false);
    when(recipeMapper.toRecipeDto(any(), any()))
        .thenReturn(expectedDto);
    
    // When
    recipeService.createRecipe(dto, UserTestFixtures.TEST_USER_ID);
    
    // Then
    ArgumentCaptor<Recipe> recipeCaptor = ArgumentCaptor.forClass(Recipe.class);
    verify(recipeRepository, atLeastOnce()).save(recipeCaptor.capture());
    Recipe savedRecipe = recipeCaptor.getValue();
    assertThat(savedRecipe.getTotalTimeMinutes()).isEqualTo(45); // 15 + 30
  }
  
  @Test
  @DisplayName("Should process ingredients when provided")
  void shouldProcessIngredientsWhenProvided() {
    // Given
    CreateRecipeDto dto = RecipeTestFixtures.createCreateRecipeDto();
    User creator = UserTestFixtures.createUser();
    Recipe recipe = RecipeTestFixtures.createRecipe();
    RecipeDto expectedDto = new RecipeDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(creator));
    when(recipeMapper.toRecipe(dto))
        .thenReturn(recipe);
    when(recipeRepository.save(any(Recipe.class)))
        .thenReturn(recipe);
    when(ingredientRepository.findByNameIgnoreCase("Flour"))
        .thenReturn(Optional.empty());
    when(ingredientRepository.findByNameIgnoreCase("Eggs"))
        .thenReturn(Optional.empty());
    when(ingredientRepository.save(any(Ingredient.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    RecipeIngredient recipeIngredient1 = new RecipeIngredient();
    recipeIngredient1.setId(1L);
    RecipeIngredient recipeIngredient2 = new RecipeIngredient();
    recipeIngredient2.setId(2L);
    List<RecipeIngredient> savedIngredients = List.of(recipeIngredient1, recipeIngredient2);
    when(recipeMapper.toRecipeIngredient(any(com.vallexia.recipe.dto.IngredientDto.class)))
        .thenReturn(new RecipeIngredient());
    when(recipeIngredientRepository.saveAll(anyList()))
        .thenReturn(savedIngredients);
    setupNutritionalInfoMapperMock();
    setupNutritionalInfoRepositoryMock();
    setupUserLocaleMock();
    setupFavoriteRecipeMock(false);
    when(recipeMapper.toRecipeDto(any(), any()))
        .thenReturn(expectedDto);
    
    // When
    recipeService.createRecipe(dto, UserTestFixtures.TEST_USER_ID);
    
    // Then
    verify(ingredientRepository, atLeastOnce()).save(any(Ingredient.class));
    verify(recipeIngredientRepository).saveAll(anyList());
  }
  
  // ==================== getRecipeById() Tests ====================
  
  @Test
  @DisplayName("Should retrieve recipe by ID successfully")
  void shouldRetrieveRecipeByIdSuccessfully() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    RecipeDto expectedDto = new RecipeDto();
    expectedDto.setId(RecipeTestFixtures.TEST_RECIPE_ID);
    
    when(recipeRepository.findById(RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(Optional.of(recipe));
    setupUserLocaleMock();
    setupRecipeEnrichmentServiceMock();
    when(favoriteRecipeService.isFavorite(RecipeTestFixtures.TEST_RECIPE_ID, UserTestFixtures.TEST_USER_ID))
        .thenReturn(true);
    when(recipeMapper.toRecipeDto(recipe, true))
        .thenReturn(expectedDto);
    
    // When
    RecipeDto result = recipeService.getRecipeById(RecipeTestFixtures.TEST_RECIPE_ID, UserTestFixtures.TEST_USER_ID);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(RecipeTestFixtures.TEST_RECIPE_ID);
    verify(recipeRepository).findById(RecipeTestFixtures.TEST_RECIPE_ID);
    verify(recipeEnrichmentService).enrichWithTranslations(any(RecipeDto.class), eq(recipe), eq("en"));
  }
  
  @Test
  @DisplayName("Should throw RecipeNotFoundException when recipe doesn't exist")
  void shouldThrowRecipeNotFoundExceptionWhenRecipeDoesNotExist() {
    // Given
    when(recipeRepository.findById(RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(Optional.empty());
    
    // When & Then
    assertThatThrownBy(() -> recipeService.getRecipeById(RecipeTestFixtures.TEST_RECIPE_ID, UserTestFixtures.TEST_USER_ID))
        .isInstanceOf(RecipeNotFoundException.class)
        .hasMessageContaining("Recipe not found with id: " + RecipeTestFixtures.TEST_RECIPE_ID);
  }
  
  @Test
  @DisplayName("Should throw AccessDeniedException when accessing private recipe as non-owner")
  void shouldThrowAccessDeniedExceptionWhenAccessingPrivateRecipeAsNonOwner() {
    // Given
    Recipe recipe = RecipeTestFixtures.createPrivateRecipe();
    User differentUser = UserTestFixtures.createUser();
    differentUser.setId(999L);
    
    when(recipeRepository.findById(RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(Optional.of(recipe));
    
    // When & Then
    assertThatThrownBy(() -> recipeService.getRecipeById(RecipeTestFixtures.TEST_RECIPE_ID, 999L))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessageContaining("You do not have permission to access this recipe");
  }
  
  // ==================== updateRecipe() Tests ====================
  
  @Test
  @DisplayName("Should throw AccessDeniedException when non-admin user tries to update recipe")
  void shouldThrowAccessDeniedExceptionWhenNonAdminTriesToUpdateRecipe() {
    // Given
    Recipe existingRecipe = RecipeTestFixtures.createRecipe();
    UpdateRecipeDto dto = RecipeTestFixtures.createUpdateRecipeDto();
    
    when(recipeRepository.findById(RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(Optional.of(existingRecipe));
    when(authenticationHelper.hasRole("ROLE_ADMIN"))
        .thenReturn(false); // User is not admin
    
    // When & Then
    assertThatThrownBy(() -> recipeService.updateRecipe(RecipeTestFixtures.TEST_RECIPE_ID, dto, UserTestFixtures.TEST_USER_ID))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessageContaining("You do not have permission to update this recipe");
    
    verify(recipeRepository, never()).save(any(Recipe.class));
    verify(auditService, never()).logEvent(any(), any(), any());
  }
  
  @Test
  @DisplayName("Should update recipe successfully as admin")
  void shouldUpdateRecipeSuccessfullyAsAdmin() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    User differentUser = UserTestFixtures.createUser();
    differentUser.setId(999L);
    recipe.setCreator(differentUser);
    UpdateRecipeDto dto = RecipeTestFixtures.createUpdateRecipeDto();
    RecipeDto expectedDto = new RecipeDto();
    
    when(recipeRepository.findById(RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(Optional.of(recipe));
    when(authenticationHelper.hasRole("ROLE_ADMIN"))
        .thenReturn(true); // Admin role allows updating any recipe
    when(recipeRepository.save(any(Recipe.class)))
        .thenReturn(recipe);
    setupUserLocaleMock();
    setupRecipeEnrichmentServiceMock();
    when(favoriteRecipeService.isFavorite(any(), any()))
        .thenReturn(false);
    when(recipeMapper.toRecipeDto(any(), any()))
        .thenReturn(expectedDto);
    
    // When
    RecipeDto result = recipeService.updateRecipe(RecipeTestFixtures.TEST_RECIPE_ID, dto, UserTestFixtures.TEST_ADMIN_ID);
    
    // Then
    assertThat(result).isNotNull();
    verify(recipeRepository, atLeastOnce()).save(any(Recipe.class));
    verify(auditService).logEvent(eq(EventType.RECIPE_UPDATED), eq(UserTestFixtures.TEST_ADMIN_ID), any(String.class));
  }
  
  @Test
  @DisplayName("Should throw AccessDeniedException when non-admin tries to update recipe")
  void shouldThrowAccessDeniedExceptionWhenUpdatingRecipeAsNonOwnerAndNonAdmin() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    UpdateRecipeDto dto = RecipeTestFixtures.createUpdateRecipeDto();
    User differentUser = UserTestFixtures.createUser();
    differentUser.setId(999L);
    recipe.setCreator(differentUser);
    
    when(recipeRepository.findById(RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(Optional.of(recipe));
    when(authenticationHelper.hasRole("ROLE_ADMIN"))
        .thenReturn(false); // User is not admin
    
    // When & Then
    assertThatThrownBy(() -> recipeService.updateRecipe(RecipeTestFixtures.TEST_RECIPE_ID, dto, UserTestFixtures.TEST_USER_ID))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessageContaining("You do not have permission to update this recipe");
  }
  
  // ==================== deleteRecipe() Tests ====================
  
  @Test
  @DisplayName("Should throw AccessDeniedException when non-admin user tries to delete recipe")
  void shouldThrowAccessDeniedExceptionWhenNonAdminTriesToDeleteRecipe() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    
    when(recipeRepository.findById(RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(Optional.of(recipe));
    when(authenticationHelper.hasRole("ROLE_ADMIN"))
        .thenReturn(false); // User is not admin
    
    // When & Then
    assertThatThrownBy(() -> recipeService.deleteRecipe(RecipeTestFixtures.TEST_RECIPE_ID, UserTestFixtures.TEST_USER_ID))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessageContaining("You do not have permission to delete this recipe");
    
    verify(recipeRepository, never()).delete(any(Recipe.class));
    verify(auditService, never()).logEvent(any(), any(), any());
  }
  
  @Test
  @DisplayName("Should delete recipe successfully as admin")
  void shouldDeleteRecipeSuccessfullyAsAdmin() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    User differentUser = UserTestFixtures.createUser();
    differentUser.setId(999L);
    recipe.setCreator(differentUser);
    
    when(recipeRepository.findById(RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(Optional.of(recipe));
    when(authenticationHelper.hasRole("ROLE_ADMIN"))
        .thenReturn(true); // Admin role allows deleting any recipe
    doNothing().when(recipeRepository).delete(recipe);
    
    // When
    recipeService.deleteRecipe(RecipeTestFixtures.TEST_RECIPE_ID, UserTestFixtures.TEST_ADMIN_ID);
    
    // Then
    verify(recipeRepository).delete(recipe);
    verify(auditService).logEvent(eq(EventType.RECIPE_DELETED), eq(UserTestFixtures.TEST_ADMIN_ID), any(String.class));
  }
  
  @Test
  @DisplayName("Should throw AccessDeniedException when non-admin tries to delete recipe")
  void shouldThrowAccessDeniedExceptionWhenDeletingRecipeAsNonOwnerAndNonAdmin() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    User differentUser = UserTestFixtures.createUser();
    differentUser.setId(999L);
    recipe.setCreator(differentUser);
    
    when(recipeRepository.findById(RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(Optional.of(recipe));
    when(authenticationHelper.hasRole("ROLE_ADMIN"))
        .thenReturn(false); // User is not admin
    
    // When & Then
    assertThatThrownBy(() -> recipeService.deleteRecipe(RecipeTestFixtures.TEST_RECIPE_ID, UserTestFixtures.TEST_USER_ID))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessageContaining("You do not have permission to delete this recipe");
  }
  
  // ==================== getPublicRecipes() Tests ====================
  
  @Test
  @DisplayName("Should retrieve public recipes with pagination")
  void shouldRetrievePublicRecipesWithPagination() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    List<Recipe> recipes = List.of(recipe);
    Pageable pageable = PageRequest.of(0, 20);
    Page<Recipe> recipePage = new PageImpl<>(recipes, pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(ArgumentMatchers.<Specification<Recipe>>any(), any(Pageable.class)))
        .thenReturn(recipePage);
    setupUserLocaleMock();
    setupRecipeEnrichmentServiceMock();
    when(favoriteRecipeService.isFavorite(any(Long.class), eq(1L)))
        .thenReturn(false);
    when(recipeMapper.toRecipeDto(recipe, false))
        .thenReturn(recipeDto);
    
    // When
    Page<RecipeDto> result = recipeService.getPublicRecipes(pageable, 1L);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    verify(recipeRepository).findAll(ArgumentMatchers.<Specification<Recipe>>any(), any(Pageable.class));
    verify(favoriteRecipeService).isFavorite(any(Long.class), eq(1L));
    verify(recipeEnrichmentService).enrichWithTranslations(any(RecipeDto.class), eq(recipe), eq("en"));
  }
}
