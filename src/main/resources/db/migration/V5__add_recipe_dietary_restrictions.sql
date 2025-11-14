-- ============================================================================
-- RECIPE DIETARY RESTRICTIONS TABLE
-- ============================================================================
-- Stores dietary restrictions associated with recipes (ElementCollection).
-- ============================================================================
CREATE TABLE recipe_dietary_restrictions (
    recipe_id BIGINT NOT NULL,
    restriction VARCHAR(50) NOT NULL,
    PRIMARY KEY (recipe_id, restriction),
    FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
    CONSTRAINT chk_recipe_dietary_restriction CHECK (
        restriction IN (
            'VEGETARIAN', 'VEGAN', 'GLUTEN_FREE', 'DAIRY_FREE', 'NUT_FREE',
            'SOY_FREE', 'EGG_FREE', 'LOW_CARB', 'KETO', 'PALEO',
            'MEDITERRANEAN', 'LOW_SODIUM', 'LOW_FAT', 'HIGH_PROTEIN',
            'HALAL', 'KOSHER'
        )
    )
);

COMMENT ON TABLE recipe_dietary_restrictions IS 'Dietary restrictions associated with recipes (ElementCollection mapping)';
COMMENT ON COLUMN recipe_dietary_restrictions.recipe_id IS 'Recipe this restriction belongs to';
COMMENT ON COLUMN recipe_dietary_restrictions.restriction IS 'Dietary restriction enum value';

-- ============================================================================
-- RECIPE ALLERGENS TABLE
-- ============================================================================
-- Stores allergens associated with recipes (ElementCollection).
-- ============================================================================
CREATE TABLE recipe_allergens (
    recipe_id BIGINT NOT NULL,
    allergy VARCHAR(50) NOT NULL,
    PRIMARY KEY (recipe_id, allergy),
    FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
    CONSTRAINT chk_recipe_allergy CHECK (
        allergy IN (
            'PEANUTS', 'TREE_NUTS', 'MILK', 'EGGS', 'FISH', 'SHELLFISH',
            'SOY', 'WHEAT', 'SESAME', 'MUSTARD', 'CELERY', 'LUPIN', 'SULFITES'
        )
    )
);

COMMENT ON TABLE recipe_allergens IS 'Allergens associated with recipes (ElementCollection mapping)';
COMMENT ON COLUMN recipe_allergens.recipe_id IS 'Recipe this allergen belongs to';
COMMENT ON COLUMN recipe_allergens.allergy IS 'Allergy enum value';

