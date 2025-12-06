package com.vallexia.recipe.util;

import com.vallexia.common.enums.SupportedAllergy;
import com.vallexia.common.enums.SupportedDietaryRestriction;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for mapping dietary restrictions to incompatible allergens.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
public final class AllergenCompatibilityUtil {
    
    /**
     * Private constructor to prevent instantiation.
     */
    private AllergenCompatibilityUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Get list of allergens that are incompatible with the given dietary restrictions.
     * 
     * @param restrictions list of dietary restrictions
     * @return list of incompatible allergens
     */
    public static List<SupportedAllergy> getIncompatibleAllergens(List<SupportedDietaryRestriction> restrictions) {
        if (restrictions == null || restrictions.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Use LinkedHashSet to avoid duplicates while preserving insertion order
        Set<SupportedAllergy> incompatibleAllergensSet = new LinkedHashSet<>();
        for (SupportedDietaryRestriction restriction : restrictions) {
            switch (restriction) {
                case DAIRY_FREE:
                    // MILK allergen represents all dairy products (milk, cheese, yogurt, butter, cream, etc.)
                    incompatibleAllergensSet.add(SupportedAllergy.MILK);
                    break;
                case NUT_FREE:
                    incompatibleAllergensSet.add(SupportedAllergy.PEANUTS);
                    incompatibleAllergensSet.add(SupportedAllergy.TREE_NUTS);
                    break;
                case EGG_FREE:
                    incompatibleAllergensSet.add(SupportedAllergy.EGGS);
                    break;
                case SOY_FREE:
                    incompatibleAllergensSet.add(SupportedAllergy.SOY);
                    break;
                case GLUTEN_FREE:
                    incompatibleAllergensSet.add(SupportedAllergy.WHEAT);
                    break;
                // VEGAN is incompatible with all animal product allergens
                case VEGAN:
                    incompatibleAllergensSet.add(SupportedAllergy.MILK);
                    incompatibleAllergensSet.add(SupportedAllergy.EGGS);
                    incompatibleAllergensSet.add(SupportedAllergy.FISH);
                    incompatibleAllergensSet.add(SupportedAllergy.SHELLFISH);
                    break;
                // VEGETARIAN is incompatible with meat/fish allergens
                case VEGETARIAN:
                    incompatibleAllergensSet.add(SupportedAllergy.FISH);
                    incompatibleAllergensSet.add(SupportedAllergy.SHELLFISH);
                    break;
                default:
                    // Other restrictions don't have direct allergen incompatibilities
                    break;
            }
        }
        return new ArrayList<>(incompatibleAllergensSet);
    }
}
