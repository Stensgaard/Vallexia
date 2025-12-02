package com.vallexia.recipe.unit.util;

import com.vallexia.recipe.util.AllergenCompatibilityUtil;
import com.vallexia.common.enums.SupportedAllergy;
import com.vallexia.common.enums.SupportedDietaryRestriction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AllergenCompatibilityUtil.
 * Tests mapping of dietary restrictions to incompatible allergens, edge cases, and null handling.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-02
 */
@DisplayName("AllergenCompatibilityUtil Unit Tests")
class AllergenCompatibilityUtilTest {

    // ==================== Null and Empty Input Tests ====================

    @Test
    @DisplayName("getIncompatibleAllergens should return empty list when restrictions is null")
    void getIncompatibleAllergens_shouldReturnEmptyListWhenNull() {
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(null);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getIncompatibleAllergens should return empty list when restrictions is empty")
    void getIncompatibleAllergens_shouldReturnEmptyListWhenEmpty() {
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(Collections.emptyList());
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    // ==================== Single Restriction Tests ====================

    @Test
    @DisplayName("getIncompatibleAllergens should map DAIRY_FREE to MILK allergen")
    void getIncompatibleAllergens_shouldMapDairyFreeToMilk() {
        // Given
        List<SupportedDietaryRestriction> restrictions = List.of(SupportedDietaryRestriction.DAIRY_FREE);
        
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(restrictions);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(SupportedAllergy.MILK);
    }

    @Test
    @DisplayName("getIncompatibleAllergens should map NUT_FREE to PEANUTS and TREE_NUTS allergens")
    void getIncompatibleAllergens_shouldMapNutFreeToPeanutsAndTreeNuts() {
        // Given
        List<SupportedDietaryRestriction> restrictions = List.of(SupportedDietaryRestriction.NUT_FREE);
        
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(restrictions);
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(SupportedAllergy.PEANUTS, SupportedAllergy.TREE_NUTS);
    }

    @Test
    @DisplayName("getIncompatibleAllergens should map EGG_FREE to EGGS allergen")
    void getIncompatibleAllergens_shouldMapEggFreeToEggs() {
        // Given
        List<SupportedDietaryRestriction> restrictions = List.of(SupportedDietaryRestriction.EGG_FREE);
        
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(restrictions);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(SupportedAllergy.EGGS);
    }

    @Test
    @DisplayName("getIncompatibleAllergens should map SOY_FREE to SOY allergen")
    void getIncompatibleAllergens_shouldMapSoyFreeToSoy() {
        // Given
        List<SupportedDietaryRestriction> restrictions = List.of(SupportedDietaryRestriction.SOY_FREE);
        
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(restrictions);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(SupportedAllergy.SOY);
    }

    @Test
    @DisplayName("getIncompatibleAllergens should map GLUTEN_FREE to WHEAT allergen")
    void getIncompatibleAllergens_shouldMapGlutenFreeToWheat() {
        // Given
        List<SupportedDietaryRestriction> restrictions = List.of(SupportedDietaryRestriction.GLUTEN_FREE);
        
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(restrictions);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(SupportedAllergy.WHEAT);
    }

    @Test
    @DisplayName("getIncompatibleAllergens should map VEGAN to all animal product allergens")
    void getIncompatibleAllergens_shouldMapVeganToAllAnimalProductAllergens() {
        // Given
        List<SupportedDietaryRestriction> restrictions = List.of(SupportedDietaryRestriction.VEGAN);
        
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(restrictions);
        
        // Then
        assertThat(result).hasSize(4);
        assertThat(result).containsExactly(
            SupportedAllergy.MILK,
            SupportedAllergy.EGGS,
            SupportedAllergy.FISH,
            SupportedAllergy.SHELLFISH
        );
    }

    @Test
    @DisplayName("getIncompatibleAllergens should map VEGETARIAN to FISH and SHELLFISH allergens")
    void getIncompatibleAllergens_shouldMapVegetarianToFishAndShellfish() {
        // Given
        List<SupportedDietaryRestriction> restrictions = List.of(SupportedDietaryRestriction.VEGETARIAN);
        
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(restrictions);
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(SupportedAllergy.FISH, SupportedAllergy.SHELLFISH);
    }

    // ==================== Restrictions Without Allergen Mappings ====================

    @Test
    @DisplayName("getIncompatibleAllergens should return empty list for LOW_CARB restriction")
    void getIncompatibleAllergens_shouldReturnEmptyForLowCarb() {
        // Given
        List<SupportedDietaryRestriction> restrictions = List.of(SupportedDietaryRestriction.LOW_CARB);
        
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(restrictions);
        
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getIncompatibleAllergens should return empty list for KETO restriction")
    void getIncompatibleAllergens_shouldReturnEmptyForKeto() {
        // Given
        List<SupportedDietaryRestriction> restrictions = List.of(SupportedDietaryRestriction.KETO);
        
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(restrictions);
        
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getIncompatibleAllergens should return empty list for PALEO restriction")
    void getIncompatibleAllergens_shouldReturnEmptyForPaleo() {
        // Given
        List<SupportedDietaryRestriction> restrictions = List.of(SupportedDietaryRestriction.PALEO);
        
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(restrictions);
        
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getIncompatibleAllergens should return empty list for MEDITERRANEAN restriction")
    void getIncompatibleAllergens_shouldReturnEmptyForMediterranean() {
        // Given
        List<SupportedDietaryRestriction> restrictions = List.of(SupportedDietaryRestriction.MEDITERRANEAN);
        
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(restrictions);
        
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getIncompatibleAllergens should return empty list for LOW_SODIUM restriction")
    void getIncompatibleAllergens_shouldReturnEmptyForLowSodium() {
        // Given
        List<SupportedDietaryRestriction> restrictions = List.of(SupportedDietaryRestriction.LOW_SODIUM);
        
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(restrictions);
        
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getIncompatibleAllergens should return empty list for LOW_FAT restriction")
    void getIncompatibleAllergens_shouldReturnEmptyForLowFat() {
        // Given
        List<SupportedDietaryRestriction> restrictions = List.of(SupportedDietaryRestriction.LOW_FAT);
        
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(restrictions);
        
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getIncompatibleAllergens should return empty list for HIGH_PROTEIN restriction")
    void getIncompatibleAllergens_shouldReturnEmptyForHighProtein() {
        // Given
        List<SupportedDietaryRestriction> restrictions = List.of(SupportedDietaryRestriction.HIGH_PROTEIN);
        
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(restrictions);
        
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getIncompatibleAllergens should return empty list for HALAL restriction")
    void getIncompatibleAllergens_shouldReturnEmptyForHalal() {
        // Given
        List<SupportedDietaryRestriction> restrictions = List.of(SupportedDietaryRestriction.HALAL);
        
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(restrictions);
        
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getIncompatibleAllergens should return empty list for KOSHER restriction")
    void getIncompatibleAllergens_shouldReturnEmptyForKosher() {
        // Given
        List<SupportedDietaryRestriction> restrictions = List.of(SupportedDietaryRestriction.KOSHER);
        
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(restrictions);
        
        // Then
        assertThat(result).isEmpty();
    }

    // ==================== Multiple Restrictions Tests ====================

    @Test
    @DisplayName("getIncompatibleAllergens should combine allergens from multiple restrictions")
    void getIncompatibleAllergens_shouldCombineAllergensFromMultipleRestrictions() {
        // Given
        List<SupportedDietaryRestriction> restrictions = Arrays.asList(
            SupportedDietaryRestriction.DAIRY_FREE,
            SupportedDietaryRestriction.EGG_FREE,
            SupportedDietaryRestriction.SOY_FREE
        );
        
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(restrictions);
        
        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly(
            SupportedAllergy.MILK,
            SupportedAllergy.EGGS,
            SupportedAllergy.SOY
        );
    }

    @Test
    @DisplayName("getIncompatibleAllergens should deduplicate allergens when restrictions overlap")
    void getIncompatibleAllergens_shouldDeduplicateAllergens() {
        // Given - VEGAN includes MILK and EGGS, DAIRY_FREE also includes MILK
        List<SupportedDietaryRestriction> restrictions = Arrays.asList(
            SupportedDietaryRestriction.VEGAN,
            SupportedDietaryRestriction.DAIRY_FREE
        );
        
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(restrictions);
        
        // Then - MILK should appear only once
        assertThat(result).hasSize(4);
        assertThat(result).containsExactly(
            SupportedAllergy.MILK,
            SupportedAllergy.EGGS,
            SupportedAllergy.FISH,
            SupportedAllergy.SHELLFISH
        );
        // Verify MILK appears only once
        assertThat(result.stream().filter(a -> a == SupportedAllergy.MILK).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("getIncompatibleAllergens should handle mix of restrictions with and without allergen mappings")
    void getIncompatibleAllergens_shouldHandleMixOfRestrictions() {
        // Given
        List<SupportedDietaryRestriction> restrictions = Arrays.asList(
            SupportedDietaryRestriction.DAIRY_FREE,
            SupportedDietaryRestriction.LOW_CARB,
            SupportedDietaryRestriction.EGG_FREE,
            SupportedDietaryRestriction.KETO
        );
        
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(restrictions);
        
        // Then - Only restrictions with allergen mappings should contribute
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(SupportedAllergy.MILK, SupportedAllergy.EGGS);
    }

    @Test
    @DisplayName("getIncompatibleAllergens should preserve insertion order of allergens")
    void getIncompatibleAllergens_shouldPreserveInsertionOrder() {
        // Given
        List<SupportedDietaryRestriction> restrictions = Arrays.asList(
            SupportedDietaryRestriction.NUT_FREE,
            SupportedDietaryRestriction.DAIRY_FREE,
            SupportedDietaryRestriction.EGG_FREE
        );
        
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(restrictions);
        
        // Then - Order should be: PEANUTS, TREE_NUTS (from NUT_FREE), MILK (from DAIRY_FREE), EGGS (from EGG_FREE)
        assertThat(result).hasSize(4);
        assertThat(result).containsExactly(
            SupportedAllergy.PEANUTS,
            SupportedAllergy.TREE_NUTS,
            SupportedAllergy.MILK,
            SupportedAllergy.EGGS
        );
    }

    // ==================== Comprehensive Tests ====================

    @Test
    @DisplayName("getIncompatibleAllergens should handle all restrictions with allergen mappings")
    void getIncompatibleAllergens_shouldHandleAllRestrictionsWithAllergenMappings() {
        // Given - All restrictions that map to allergens
        List<SupportedDietaryRestriction> restrictions = Arrays.asList(
            SupportedDietaryRestriction.DAIRY_FREE,
            SupportedDietaryRestriction.NUT_FREE,
            SupportedDietaryRestriction.EGG_FREE,
            SupportedDietaryRestriction.SOY_FREE,
            SupportedDietaryRestriction.GLUTEN_FREE,
            SupportedDietaryRestriction.VEGAN,
            SupportedDietaryRestriction.VEGETARIAN
        );
        
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(restrictions);
        
        // Then - Should contain all unique allergens (with deduplication)
        // Total unique allergens: MILK, PEANUTS, TREE_NUTS, EGGS, SOY, WHEAT, FISH, SHELLFISH = 8
        assertThat(result).hasSize(8);
        assertThat(result).containsExactlyInAnyOrder(
            SupportedAllergy.MILK,
            SupportedAllergy.PEANUTS,
            SupportedAllergy.TREE_NUTS,
            SupportedAllergy.EGGS,
            SupportedAllergy.SOY,
            SupportedAllergy.WHEAT,
            SupportedAllergy.FISH,
            SupportedAllergy.SHELLFISH
        );
    }

    @Test
    @DisplayName("getIncompatibleAllergens should handle duplicate restrictions in input")
    void getIncompatibleAllergens_shouldHandleDuplicateRestrictions() {
        // Given - Same restriction appears multiple times
        List<SupportedDietaryRestriction> restrictions = Arrays.asList(
            SupportedDietaryRestriction.DAIRY_FREE,
            SupportedDietaryRestriction.DAIRY_FREE,
            SupportedDietaryRestriction.DAIRY_FREE
        );
        
        // When
        List<SupportedAllergy> result = AllergenCompatibilityUtil.getIncompatibleAllergens(restrictions);
        
        // Then - MILK should appear only once (deduplication)
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(SupportedAllergy.MILK);
    }
}

