package com.vallexia.recipe.util;

import com.vallexia.user.entity.enums.Allergy;
import com.vallexia.user.entity.enums.DietaryRestriction;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for mapping dietary restrictions to incompatible allergens.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
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
     * For example, DAIRY_FREE is incompatible with MILK allergen.
     * 
     * Note: The MILK allergen represents all dairy products (milk, cheese, yogurt, butter, cream, etc.).
     * Recipes containing any dairy product should be tagged with MILK allergen.
     * 
     * @param restrictions list of dietary restrictions
     * @return list of incompatible allergens
     */
    public static List<Allergy> getIncompatibleAllergens(List<DietaryRestriction> restrictions) {
        List<Allergy> incompatibleAllergens = new ArrayList<>();
        for (DietaryRestriction restriction : restrictions) {
            switch (restriction) {
                case DAIRY_FREE:
                    // MILK allergen represents all dairy products (milk, cheese, yogurt, butter, cream, etc.)
                    incompatibleAllergens.add(Allergy.MILK);
                    break;
                case NUT_FREE:
                    incompatibleAllergens.add(Allergy.PEANUTS);
                    incompatibleAllergens.add(Allergy.TREE_NUTS);
                    break;
                case EGG_FREE:
                    incompatibleAllergens.add(Allergy.EGGS);
                    break;
                case SOY_FREE:
                    incompatibleAllergens.add(Allergy.SOY);
                    break;
                case GLUTEN_FREE:
                    incompatibleAllergens.add(Allergy.WHEAT);
                    break;
                // VEGAN is incompatible with all animal product allergens
                case VEGAN:
                    incompatibleAllergens.add(Allergy.MILK);
                    incompatibleAllergens.add(Allergy.EGGS);
                    incompatibleAllergens.add(Allergy.FISH);
                    incompatibleAllergens.add(Allergy.SHELLFISH);
                    break;
                // VEGETARIAN is incompatible with meat/fish allergens
                case VEGETARIAN:
                    incompatibleAllergens.add(Allergy.FISH);
                    incompatibleAllergens.add(Allergy.SHELLFISH);
                    break;
                default:
                    // Other restrictions don't have direct allergen incompatibilities
                    break;
            }
        }
        return incompatibleAllergens;
    }
}
