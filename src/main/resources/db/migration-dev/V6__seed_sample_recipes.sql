-- Migration: Seed sample recipes for development
-- Version: V6 (Dev migration)
-- Description: Seeds database with diverse sample recipes for testing and development.
--              Recipes are created by the admin user and marked as public.
--              Includes ingredients, nutritional info, and tags for completeness.
--
-- NOTE: This migration depends on V5__create_admin_user.sql having run first.
--       Recipes will be created by the admin user and set as public for easy access.
--       Run ON CONFLICT checks are included to allow re-running this migration safely.

-- Get admin user ID (created in V5)
DO $$
DECLARE
    admin_user_id BIGINT;
    v_recipe_id BIGINT;
    ingredient_id BIGINT;
    nutrition_id BIGINT;
BEGIN
    -- Get admin user ID
    SELECT id INTO admin_user_id FROM users WHERE username = 'admin';
    
    IF admin_user_id IS NULL THEN
        RAISE EXCEPTION 'Admin user not found. Ensure V5__create_admin_user.sql has run first.';
    END IF;

    -- ========================================================================
    -- Recipe 1: Classic Margherita Pizza
    -- ========================================================================
    -- Check if recipe already exists
    SELECT id INTO v_recipe_id FROM recipes WHERE name = 'Classic Margherita Pizza' AND user_id = admin_user_id LIMIT 1;
    
    IF v_recipe_id IS NULL THEN
        INSERT INTO recipes (
            user_id, name, description, instructions, prep_time_minutes, 
            cook_time_minutes, total_time_minutes, servings, difficulty_level, 
            category, cuisine_type, is_public
        ) VALUES (
            admin_user_id,
            'Classic Margherita Pizza',
            'A traditional Italian pizza with fresh basil, mozzarella, and tomato sauce. Simple ingredients create an unforgettable flavor.',
            '1. Preheat oven to 475°F (245°C). ' ||
            '2. Roll out pizza dough on a floured surface to desired thickness. ' ||
            '3. Spread tomato sauce evenly, leaving a border for the crust. ' ||
            '4. Arrange mozzarella slices evenly over the sauce. ' ||
            '5. Drizzle with a little olive oil. ' ||
            '6. Bake for 12-15 minutes until crust is golden and cheese is bubbly. ' ||
            '7. Remove from oven and top with fresh basil leaves. ' ||
            '8. Let cool for 2-3 minutes before slicing and serving.',
            15, 15, 30, 4, 'EASY', 'DINNER', 'ITALIAN', true
        ) RETURNING id INTO v_recipe_id;
    END IF;

    -- Add ingredients and nutritional info (check if already exists to avoid duplicates)
    IF v_recipe_id IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM recipe_ingredients ri WHERE ri.recipe_id = v_recipe_id LIMIT 1
    ) THEN
        -- Insert ingredients (with conflict handling)
        INSERT INTO ingredients (name) VALUES 
            ('Pizza Dough'), ('Tomato Sauce'), ('Mozzarella Cheese'), ('Fresh Basil'), ('Olive Oil')
        ON CONFLICT (name) DO NOTHING;

        -- Link ingredients to recipe
        INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity, unit, display_order)
        SELECT v_recipe_id, i.id, 
               CASE i.name 
                   WHEN 'Pizza Dough' THEN 1.0
                   WHEN 'Tomato Sauce' THEN 0.5
                   WHEN 'Mozzarella Cheese' THEN 250.0
                   WHEN 'Fresh Basil' THEN 20.0
                   WHEN 'Olive Oil' THEN 15.0
               END,
               CASE i.name
                   WHEN 'Pizza Dough' THEN 'ball'
                   WHEN 'Tomato Sauce' THEN 'cup'
                   WHEN 'Mozzarella Cheese' THEN 'grams'
                   WHEN 'Fresh Basil' THEN 'leaves'
                   WHEN 'Olive Oil' THEN 'ml'
               END,
               ROW_NUMBER() OVER ()
        FROM ingredients i
        WHERE i.name IN ('Pizza Dough', 'Tomato Sauce', 'Mozzarella Cheese', 'Fresh Basil', 'Olive Oil');

        -- Nutritional info (per serving)
        INSERT INTO nutritional_info (recipe_id, calories, protein, carbs, fats, fiber, sodium, sugar, per_serving)
        SELECT v_recipe_id, 280.0, 12.0, 38.0, 10.0, 2.5, 680.0, 4.0, true
        WHERE NOT EXISTS (SELECT 1 FROM nutritional_info ni WHERE ni.recipe_id = v_recipe_id);

        -- Tags
        INSERT INTO recipe_tags (recipe_id, tag) VALUES 
            (v_recipe_id, 'vegetarian'), (v_recipe_id, 'italian'), (v_recipe_id, 'quick'), (v_recipe_id, 'family-friendly')
        ON CONFLICT DO NOTHING;
    END IF;

    -- ========================================================================
    -- Recipe 2: Grilled Salmon with Vegetables
    -- ========================================================================
    SELECT id INTO v_recipe_id FROM recipes WHERE name = 'Grilled Salmon with Vegetables' AND user_id = admin_user_id LIMIT 1;
    
    IF v_recipe_id IS NULL THEN
        INSERT INTO recipes (
            user_id, name, description, instructions, prep_time_minutes, 
            cook_time_minutes, total_time_minutes, servings, difficulty_level, 
            category, cuisine_type, is_public
        ) VALUES (
            admin_user_id,
            'Grilled Salmon with Vegetables',
            'Healthy and flavorful salmon grilled to perfection with seasonal vegetables. A Mediterranean-inspired dish that''s both nutritious and delicious.',
            '1. Preheat grill to medium-high heat (400°F). ' ||
            '2. Season salmon fillets with salt, pepper, and a squeeze of lemon juice. ' ||
            '3. Toss asparagus, zucchini, and cherry tomatoes with olive oil, salt, and pepper. ' ||
            '4. Place salmon on grill, skin-side down, and cook for 4-5 minutes. ' ||
            '5. Flip salmon and cook for another 4-5 minutes until flaky. ' ||
            '6. Grill vegetables for 8-10 minutes, turning occasionally. ' ||
            '7. Remove from grill and serve immediately with lemon wedges.',
            10, 15, 25, 2, 'MEDIUM', 'DINNER', 'MEDITERRANEAN', true
        ) RETURNING id INTO v_recipe_id;
    END IF;

    IF v_recipe_id IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM recipe_ingredients ri WHERE ri.recipe_id = v_recipe_id LIMIT 1
    ) THEN
        INSERT INTO ingredients (name) VALUES 
            ('Salmon Fillet'), ('Asparagus'), ('Zucchini'), ('Cherry Tomatoes'), ('Lemon'), ('Olive Oil'), ('Salt'), ('Black Pepper')
        ON CONFLICT (name) DO NOTHING;

        INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity, unit, display_order)
        SELECT v_recipe_id, i.id, 
               CASE i.name 
                   WHEN 'Salmon Fillet' THEN 500.0
                   WHEN 'Asparagus' THEN 200.0
                   WHEN 'Zucchini' THEN 2.0
                   WHEN 'Cherry Tomatoes' THEN 250.0
                   WHEN 'Lemon' THEN 1.0
                   WHEN 'Olive Oil' THEN 30.0
                   WHEN 'Salt' THEN 1.0
                   WHEN 'Black Pepper' THEN 0.5
               END,
               CASE i.name
                   WHEN 'Salmon Fillet' THEN 'grams'
                   WHEN 'Asparagus' THEN 'grams'
                   WHEN 'Zucchini' THEN 'pieces'
                   WHEN 'Cherry Tomatoes' THEN 'grams'
                   WHEN 'Lemon' THEN 'piece'
                   WHEN 'Olive Oil' THEN 'ml'
                   WHEN 'Salt' THEN 'teaspoon'
                   WHEN 'Black Pepper' THEN 'teaspoon'
               END,
               ROW_NUMBER() OVER ()
        FROM ingredients i
        WHERE i.name IN ('Salmon Fillet', 'Asparagus', 'Zucchini', 'Cherry Tomatoes', 'Lemon', 'Olive Oil', 'Salt', 'Black Pepper');

        INSERT INTO nutritional_info (recipe_id, calories, protein, carbs, fats, fiber, sodium, sugar, per_serving)
        SELECT v_recipe_id, 350.0, 35.0, 12.0, 18.0, 4.0, 420.0, 6.0, true
        WHERE NOT EXISTS (SELECT 1 FROM nutritional_info ni WHERE ni.recipe_id = v_recipe_id);

        INSERT INTO recipe_tags (recipe_id, tag) VALUES 
            (v_recipe_id, 'protein'), (v_recipe_id, 'healthy'), (v_recipe_id, 'low-carb'), (v_recipe_id, 'grilled')
        ON CONFLICT DO NOTHING;
    END IF;

    -- ========================================================================
    -- Recipe 3: Overnight Oats with Berries
    -- ========================================================================
    SELECT id INTO v_recipe_id FROM recipes WHERE name = 'Overnight Oats with Berries' AND user_id = admin_user_id LIMIT 1;
    
    IF v_recipe_id IS NULL THEN
        INSERT INTO recipes (
            user_id, name, description, instructions, prep_time_minutes, 
            cook_time_minutes, total_time_minutes, servings, difficulty_level, 
            category, cuisine_type, is_public
        ) VALUES (
            admin_user_id,
            'Overnight Oats with Berries',
            'Make-ahead breakfast that''s ready in the morning. No cooking required! Perfect for busy mornings.',
            '1. In a mason jar or container, combine rolled oats, milk, and Greek yogurt. ' ||
            '2. Add honey and vanilla extract, then stir well to combine. ' ||
            '3. Seal the container and refrigerate overnight (at least 6 hours). ' ||
            '4. In the morning, top with fresh berries and sliced almonds. ' ||
            '5. Stir and enjoy cold, or let sit at room temperature for 10 minutes if preferred.',
            5, 0, 480, 1, 'EASY', 'BREAKFAST', 'AMERICAN', true
        ) RETURNING id INTO v_recipe_id;
    END IF;

    IF v_recipe_id IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM recipe_ingredients ri WHERE ri.recipe_id = v_recipe_id LIMIT 1
    ) THEN
        INSERT INTO ingredients (name) VALUES 
            ('Rolled Oats'), ('Milk'), ('Greek Yogurt'), ('Honey'), ('Vanilla Extract'), ('Berries'), ('Almonds')
        ON CONFLICT (name) DO NOTHING;

        INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity, unit, display_order)
        SELECT v_recipe_id, i.id, 
               CASE i.name 
                   WHEN 'Rolled Oats' THEN 0.5
                   WHEN 'Milk' THEN 0.5
                   WHEN 'Greek Yogurt' THEN 0.25
                   WHEN 'Honey' THEN 1.0
                   WHEN 'Vanilla Extract' THEN 0.5
                   WHEN 'Berries' THEN 0.5
                   WHEN 'Almonds' THEN 15.0
               END,
               CASE i.name
                   WHEN 'Rolled Oats' THEN 'cup'
                   WHEN 'Milk' THEN 'cup'
                   WHEN 'Greek Yogurt' THEN 'cup'
                   WHEN 'Honey' THEN 'tablespoon'
                   WHEN 'Vanilla Extract' THEN 'teaspoon'
                   WHEN 'Berries' THEN 'cup'
                   WHEN 'Almonds' THEN 'grams'
               END,
               ROW_NUMBER() OVER ()
        FROM ingredients i
        WHERE i.name IN ('Rolled Oats', 'Milk', 'Greek Yogurt', 'Honey', 'Vanilla Extract', 'Berries', 'Almonds');

        INSERT INTO nutritional_info (recipe_id, calories, protein, carbs, fats, fiber, sodium, sugar, per_serving)
        SELECT v_recipe_id, 320.0, 15.0, 52.0, 8.0, 6.0, 85.0, 22.0, true
        WHERE NOT EXISTS (SELECT 1 FROM nutritional_info ni WHERE ni.recipe_id = v_recipe_id);

        INSERT INTO recipe_tags (recipe_id, tag) VALUES 
            (v_recipe_id, 'make-ahead'), (v_recipe_id, 'healthy'), (v_recipe_id, 'vegetarian'), (v_recipe_id, 'no-cook')
        ON CONFLICT DO NOTHING;
    END IF;

    -- ========================================================================
    -- Recipe 4: Chicken Pad Thai
    -- ========================================================================
    SELECT id INTO v_recipe_id FROM recipes WHERE name = 'Chicken Pad Thai' AND user_id = admin_user_id LIMIT 1;
    
    IF v_recipe_id IS NULL THEN
        INSERT INTO recipes (
            user_id, name, description, instructions, prep_time_minutes, 
            cook_time_minutes, total_time_minutes, servings, difficulty_level, 
            category, cuisine_type, is_public
        ) VALUES (
            admin_user_id,
            'Chicken Pad Thai',
            'Classic Thai stir-fried noodles with chicken, peanuts, and tamarind sauce. A restaurant favorite you can make at home.',
            '1. Soak rice noodles in warm water for 30 minutes until pliable but not soft. ' ||
            '2. Heat oil in a large wok or skillet over high heat. ' ||
            '3. Add chicken and cook until no longer pink, about 5 minutes. ' ||
            '4. Push chicken to one side, add eggs and scramble. ' ||
            '5. Add drained noodles and pad thai sauce. ' ||
            '6. Toss everything together and cook for 2-3 minutes. ' ||
            '7. Add bean sprouts and cook for 1 more minute. ' ||
            '8. Serve topped with crushed peanuts, lime wedges, and fresh cilantro.',
            20, 15, 35, 4, 'HARD', 'DINNER', 'THAI', true
        ) RETURNING id INTO v_recipe_id;
    END IF;

    IF v_recipe_id IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM recipe_ingredients ri WHERE ri.recipe_id = v_recipe_id LIMIT 1
    ) THEN
        INSERT INTO ingredients (name) VALUES 
            ('Rice Noodles'), ('Chicken Breast'), ('Eggs'), ('Bean Sprouts'), ('Peanuts'), ('Lime'), ('Fish Sauce'), ('Tamarind Paste'), ('Brown Sugar'), ('Soy Sauce')
        ON CONFLICT (name) DO NOTHING;

        INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity, unit, display_order)
        SELECT v_recipe_id, i.id, 
               CASE i.name 
                   WHEN 'Rice Noodles' THEN 200.0
                   WHEN 'Chicken Breast' THEN 400.0
                   WHEN 'Eggs' THEN 2.0
                   WHEN 'Bean Sprouts' THEN 150.0
                   WHEN 'Peanuts' THEN 50.0
                   WHEN 'Lime' THEN 2.0
                   WHEN 'Fish Sauce' THEN 3.0
                   WHEN 'Tamarind Paste' THEN 2.0
                   WHEN 'Brown Sugar' THEN 2.0
                   WHEN 'Soy Sauce' THEN 1.0
               END,
               CASE i.name
                   WHEN 'Rice Noodles' THEN 'grams'
                   WHEN 'Chicken Breast' THEN 'grams'
                   WHEN 'Eggs' THEN 'pieces'
                   WHEN 'Bean Sprouts' THEN 'grams'
                   WHEN 'Peanuts' THEN 'grams'
                   WHEN 'Lime' THEN 'pieces'
                   WHEN 'Fish Sauce' THEN 'tablespoons'
                   WHEN 'Tamarind Paste' THEN 'tablespoons'
                   WHEN 'Brown Sugar' THEN 'tablespoons'
                   WHEN 'Soy Sauce' THEN 'tablespoon'
               END,
               ROW_NUMBER() OVER ()
        FROM ingredients i
        WHERE i.name IN ('Rice Noodles', 'Chicken Breast', 'Eggs', 'Bean Sprouts', 'Peanuts', 'Lime', 'Fish Sauce', 'Tamarind Paste', 'Brown Sugar', 'Soy Sauce');

        INSERT INTO nutritional_info (recipe_id, calories, protein, carbs, fats, fiber, sodium, sugar, per_serving)
        SELECT v_recipe_id, 450.0, 28.0, 55.0, 12.0, 3.0, 1200.0, 8.0, true
        WHERE NOT EXISTS (SELECT 1 FROM nutritional_info ni WHERE ni.recipe_id = v_recipe_id);

        INSERT INTO recipe_tags (recipe_id, tag) VALUES 
            (v_recipe_id, 'asian'), (v_recipe_id, 'spicy'), (v_recipe_id, 'stir-fry'), (v_recipe_id, 'protein')
        ON CONFLICT DO NOTHING;
    END IF;

    -- ========================================================================
    -- Recipe 5: Chocolate Chip Cookies
    -- ========================================================================
    SELECT id INTO v_recipe_id FROM recipes WHERE name = 'Classic Chocolate Chip Cookies' AND user_id = admin_user_id LIMIT 1;
    
    IF v_recipe_id IS NULL THEN
        INSERT INTO recipes (
            user_id, name, description, instructions, prep_time_minutes, 
            cook_time_minutes, total_time_minutes, servings, difficulty_level, 
            category, cuisine_type, is_public
        ) VALUES (
            admin_user_id,
            'Classic Chocolate Chip Cookies',
            'The ultimate comfort dessert - soft, chewy chocolate chip cookies that everyone loves.',
            '1. Preheat oven to 375°F (190°C). Line baking sheets with parchment paper. ' ||
            '2. In a large bowl, cream together softened butter, brown sugar, and white sugar until light and fluffy. ' ||
            '3. Beat in eggs one at a time, then add vanilla extract. ' ||
            '4. In a separate bowl, whisk together flour, baking soda, and salt. ' ||
            '5. Gradually mix dry ingredients into wet ingredients until just combined. ' ||
            '6. Fold in chocolate chips. ' ||
            '7. Drop rounded tablespoons of dough onto prepared baking sheets, spacing 2 inches apart. ' ||
            '8. Bake for 9-11 minutes until edges are golden but centers are still soft. ' ||
            '9. Cool on baking sheet for 5 minutes before transferring to wire rack.',
            15, 11, 26, 24, 'EASY', 'DESSERT', 'AMERICAN', true
        ) RETURNING id INTO v_recipe_id;
    END IF;

    IF v_recipe_id IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM recipe_ingredients ri WHERE ri.recipe_id = v_recipe_id LIMIT 1
    ) THEN
        INSERT INTO ingredients (name) VALUES 
            ('Butter'), ('Brown Sugar'), ('White Sugar'), ('Eggs'), ('Vanilla Extract'), ('All-Purpose Flour'), ('Baking Soda'), ('Salt'), ('Chocolate Chips')
        ON CONFLICT (name) DO NOTHING;

        INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity, unit, display_order)
        SELECT v_recipe_id, i.id, 
               CASE i.name 
                   WHEN 'Butter' THEN 1.0
                   WHEN 'Brown Sugar' THEN 0.75
                   WHEN 'White Sugar' THEN 0.5
                   WHEN 'Eggs' THEN 2.0
                   WHEN 'Vanilla Extract' THEN 2.0
                   WHEN 'All-Purpose Flour' THEN 2.25
                   WHEN 'Baking Soda' THEN 1.0
                   WHEN 'Salt' THEN 1.0
                   WHEN 'Chocolate Chips' THEN 2.0
               END,
               CASE i.name
                   WHEN 'Butter' THEN 'cup'
                   WHEN 'Brown Sugar' THEN 'cup'
                   WHEN 'White Sugar' THEN 'cup'
                   WHEN 'Eggs' THEN 'pieces'
                   WHEN 'Vanilla Extract' THEN 'teaspoons'
                   WHEN 'All-Purpose Flour' THEN 'cups'
                   WHEN 'Baking Soda' THEN 'teaspoon'
                   WHEN 'Salt' THEN 'teaspoon'
                   WHEN 'Chocolate Chips' THEN 'cups'
               END,
               ROW_NUMBER() OVER ()
        FROM ingredients i
        WHERE i.name IN ('Butter', 'Brown Sugar', 'White Sugar', 'Eggs', 'Vanilla Extract', 'All-Purpose Flour', 'Baking Soda', 'Salt', 'Chocolate Chips');

        INSERT INTO nutritional_info (recipe_id, calories, protein, carbs, fats, fiber, sodium, sugar, per_serving)
        SELECT v_recipe_id, 150.0, 2.0, 20.0, 7.0, 0.8, 110.0, 12.0, true
        WHERE NOT EXISTS (SELECT 1 FROM nutritional_info ni WHERE ni.recipe_id = v_recipe_id);

        INSERT INTO recipe_tags (recipe_id, tag) VALUES 
            (v_recipe_id, 'dessert'), (v_recipe_id, 'baking'), (v_recipe_id, 'sweet'), (v_recipe_id, 'family-friendly')
        ON CONFLICT DO NOTHING;
    END IF;

    -- ========================================================================
    -- Recipe 6: Caesar Salad
    -- ========================================================================
    SELECT id INTO v_recipe_id FROM recipes WHERE name = 'Classic Caesar Salad' AND user_id = admin_user_id LIMIT 1;
    
    IF v_recipe_id IS NULL THEN
        INSERT INTO recipes (
            user_id, name, description, instructions, prep_time_minutes, 
            cook_time_minutes, total_time_minutes, servings, difficulty_level, 
            category, cuisine_type, is_public
        ) VALUES (
            admin_user_id,
            'Classic Caesar Salad',
            'Crisp romaine lettuce with homemade Caesar dressing, parmesan cheese, and croutons.',
            '1. Wash and chop romaine lettuce into bite-sized pieces. ' ||
            '2. Make croutons: cube bread, toss with olive oil, garlic, and salt, then bake at 350°F for 10 minutes. ' ||
            '3. For dressing: whisk together mayonnaise, parmesan, lemon juice, garlic, anchovy paste, and Worcestershire sauce. ' ||
            '4. Toss lettuce with dressing in a large bowl. ' ||
            '5. Top with croutons and additional parmesan cheese. ' ||
            '6. Serve immediately.',
            15, 10, 25, 4, 'EASY', 'LUNCH', 'ITALIAN', true
        ) RETURNING id INTO v_recipe_id;
    END IF;

    IF v_recipe_id IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM recipe_ingredients ri WHERE ri.recipe_id = v_recipe_id LIMIT 1
    ) THEN
        INSERT INTO ingredients (name) VALUES 
            ('Romaine Lettuce'), ('Bread'), ('Olive Oil'), ('Garlic'), ('Mayonnaise'), ('Parmesan Cheese'), ('Lemon'), ('Anchovy Paste'), ('Worcestershire Sauce'), ('Salt'), ('Black Pepper')
        ON CONFLICT (name) DO NOTHING;

        INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity, unit, display_order)
        SELECT v_recipe_id, i.id, 
               CASE i.name 
                   WHEN 'Romaine Lettuce' THEN 1.0
                   WHEN 'Bread' THEN 3.0
                   WHEN 'Olive Oil' THEN 45.0
                   WHEN 'Garlic' THEN 3.0
                   WHEN 'Mayonnaise' THEN 60.0
                   WHEN 'Parmesan Cheese' THEN 100.0
                   WHEN 'Lemon' THEN 1.0
                   WHEN 'Anchovy Paste' THEN 5.0
                   WHEN 'Worcestershire Sauce' THEN 5.0
                   WHEN 'Salt' THEN 0.5
                   WHEN 'Black Pepper' THEN 0.25
               END,
               CASE i.name
                   WHEN 'Romaine Lettuce' THEN 'head'
                   WHEN 'Bread' THEN 'slices'
                   WHEN 'Olive Oil' THEN 'ml'
                   WHEN 'Garlic' THEN 'cloves'
                   WHEN 'Mayonnaise' THEN 'grams'
                   WHEN 'Parmesan Cheese' THEN 'grams'
                   WHEN 'Lemon' THEN 'piece'
                   WHEN 'Anchovy Paste' THEN 'grams'
                   WHEN 'Worcestershire Sauce' THEN 'ml'
                   WHEN 'Salt' THEN 'teaspoon'
                   WHEN 'Black Pepper' THEN 'teaspoon'
               END,
               ROW_NUMBER() OVER ()
        FROM ingredients i
        WHERE i.name IN ('Romaine Lettuce', 'Bread', 'Olive Oil', 'Garlic', 'Mayonnaise', 'Parmesan Cheese', 'Lemon', 'Anchovy Paste', 'Worcestershire Sauce', 'Salt', 'Black Pepper');

        INSERT INTO nutritional_info (recipe_id, calories, protein, carbs, fats, fiber, sodium, sugar, per_serving)
        SELECT v_recipe_id, 220.0, 8.0, 15.0, 15.0, 2.0, 480.0, 3.0, true
        WHERE NOT EXISTS (SELECT 1 FROM nutritional_info ni WHERE ni.recipe_id = v_recipe_id);

        INSERT INTO recipe_tags (recipe_id, tag) VALUES 
            (v_recipe_id, 'salad'), (v_recipe_id, 'vegetarian'), (v_recipe_id, 'quick'), (v_recipe_id, 'healthy')
        ON CONFLICT DO NOTHING;
    END IF;

END $$;
