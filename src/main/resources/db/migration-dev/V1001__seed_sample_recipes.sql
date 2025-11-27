-- Migration: Seed sample recipes for development
-- Version: V1001 (Dev migration)
-- Description: Seeds database with diverse sample recipes for testing and development.
--              Recipes are created by the admin user and marked as public.
--              Includes ingredients, nutritional info, and tags for completeness.
--
-- NOTE: This migration depends on V1000__create_admin_user.sql having run first.
--       Recipes will be created by the admin user and set as public for easy access.
--       Run ON CONFLICT checks are included to allow re-running this migration safely.

-- Get admin user ID (created in V1000)
DO $$
DECLARE
    admin_user_id BIGINT;
    v_recipe_id BIGINT;
    nutrition_id BIGINT;
BEGIN
    -- Get admin user ID
    SELECT id INTO admin_user_id FROM users WHERE username = 'admin';
    
    IF admin_user_id IS NULL THEN
        RAISE EXCEPTION 'Admin user not found. Ensure V1000__create_admin_user.sql has run first.';
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
            category, cuisine_type, is_public, base_locale
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
            15, 15, 30, 4, 'EASY', 'DINNER', 'ITALIAN', true, 'en'
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
        
        -- Dietary restrictions
        INSERT INTO recipe_dietary_restrictions (recipe_id, restriction) VALUES 
            (v_recipe_id, 'VEGETARIAN')
        ON CONFLICT DO NOTHING;
        
        -- Allergens
        INSERT INTO recipe_allergens (recipe_id, allergy) VALUES 
            (v_recipe_id, 'MILK'), (v_recipe_id, 'WHEAT')
        ON CONFLICT DO NOTHING;
    END IF;

    -- Add Danish translation for Classic Margherita Pizza
    IF v_recipe_id IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM recipe_translations rt WHERE rt.recipe_id = v_recipe_id AND rt.locale = 'da'
    ) THEN
        INSERT INTO recipe_translations (recipe_id, locale, name, description, instructions) VALUES (
            v_recipe_id,
            'da',
            'Klassisk Margherita Pizza',
            'En traditionel italiensk pizza med frisk basilikum, mozzarella og tomatsauce. Simple ingredienser skaber en uforglemmelig smag.',
            '1. Forvarm ovnen til 475°F (245°C). ' ||
            '2. Rul pizzadej ud på et meldrysset bord til ønsket tykkelse. ' ||
            '3. Fordel tomatsauce jævnt, og efterlad en kant til skorpen. ' ||
            '4. Arranger mozzarellaskiver jævnt over saucen. ' ||
            '5. Dryp med lidt olivenolie. ' ||
            '6. Bag i 12-15 minutter indtil skorpen er gylden og osten er boblende. ' ||
            '7. Tag ud af ovnen og topp med friske basilikumblade. ' ||
            '8. Lad køle i 2-3 minutter før du skærer og serverer.'
        );
    END IF;

    -- ========================================================================
    -- Recipe 2: Grilled Salmon with Vegetables
    -- ========================================================================
    SELECT id INTO v_recipe_id FROM recipes WHERE name = 'Grilled Salmon with Vegetables' AND user_id = admin_user_id LIMIT 1;
    
    IF v_recipe_id IS NULL THEN
        INSERT INTO recipes (
            user_id, name, description, instructions, prep_time_minutes, 
            cook_time_minutes, total_time_minutes, servings, difficulty_level, 
            category, cuisine_type, is_public, base_locale
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
            10, 15, 25, 2, 'MEDIUM', 'DINNER', 'MEDITERRANEAN', true, 'en'
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
        
        -- Dietary restrictions
        INSERT INTO recipe_dietary_restrictions (recipe_id, restriction) VALUES 
            (v_recipe_id, 'HIGH_PROTEIN'), (v_recipe_id, 'LOW_CARB')
        ON CONFLICT DO NOTHING;
        
        -- Allergens
        INSERT INTO recipe_allergens (recipe_id, allergy) VALUES 
            (v_recipe_id, 'FISH')
        ON CONFLICT DO NOTHING;
    END IF;

    -- Add Danish translation for Grilled Salmon with Vegetables
    IF v_recipe_id IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM recipe_translations rt WHERE rt.recipe_id = v_recipe_id AND rt.locale = 'da'
    ) THEN
        INSERT INTO recipe_translations (recipe_id, locale, name, description, instructions) VALUES (
            v_recipe_id,
            'da',
            'Grillet Laks med Grøntsager',
            'Sund og smagfuld laks grillet til perfektion med sæsongrøntsager. En middelhavsinspireret ret, der er både nærende og lækker.',
            '1. Forvarm grillen til medium-høj varme (400°F / 200°C). ' ||
            '2. Krydrer laksfileter med salt, peber og en skvæt citronsaft. ' ||
            '3. Vend asparges, courgette og cherrytomater med olivenolie, salt og peber. ' ||
            '4. Læg laksen på grillen, skindside ned, og steg i 4-5 minutter. ' ||
            '5. Vend laksen og steg i yderligere 4-5 minutter indtil den er flager. ' ||
            '6. Grill grøntsagerne i 8-10 minutter, vend dem lejlighedsvis. ' ||
            '7. Tag af grillen og server straks med citronbåde.'
        );
    END IF;

    -- ========================================================================
    -- Recipe 3: Overnight Oats with Berries
    -- ========================================================================
    SELECT id INTO v_recipe_id FROM recipes WHERE name = 'Overnight Oats with Berries' AND user_id = admin_user_id LIMIT 1;
    
    IF v_recipe_id IS NULL THEN
        INSERT INTO recipes (
            user_id, name, description, instructions, prep_time_minutes, 
            cook_time_minutes, total_time_minutes, servings, difficulty_level, 
            category, cuisine_type, is_public, base_locale
        ) VALUES (
            admin_user_id,
            'Overnight Oats with Berries',
            'Make-ahead breakfast that''s ready in the morning. No cooking required! Perfect for busy mornings.',
            '1. In a mason jar or container, combine rolled oats, milk, and Greek yogurt. ' ||
            '2. Add honey and vanilla extract, then stir well to combine. ' ||
            '3. Seal the container and refrigerate overnight (at least 6 hours). ' ||
            '4. In the morning, top with fresh berries and sliced almonds. ' ||
            '5. Stir and enjoy cold, or let sit at room temperature for 10 minutes if preferred.',
            5, 0, 480, 1, 'EASY', 'BREAKFAST', 'AMERICAN', true, 'en'
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
        
        -- Dietary restrictions
        INSERT INTO recipe_dietary_restrictions (recipe_id, restriction) VALUES 
            (v_recipe_id, 'VEGETARIAN')
        ON CONFLICT DO NOTHING;
        
        -- Allergens
        INSERT INTO recipe_allergens (recipe_id, allergy) VALUES 
            (v_recipe_id, 'MILK'), (v_recipe_id, 'TREE_NUTS')
        ON CONFLICT DO NOTHING;
    END IF;

    -- Add Danish translation for Overnight Oats with Berries
    IF v_recipe_id IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM recipe_translations rt WHERE rt.recipe_id = v_recipe_id AND rt.locale = 'da'
    ) THEN
        INSERT INTO recipe_translations (recipe_id, locale, name, description, instructions) VALUES (
            v_recipe_id,
            'da',
            'Natgrød med Bær',
            'Forberedt morgenmad, der er klar om morgenen. Ingen tilberedning påkrævet! Perfekt til travle morgener.',
            '1. I en glasbeholder eller beholder, bland sammenrullede havregryn, mælk og græsk yoghurt. ' ||
            '2. Tilsæt honning og vaniljeekstrakt, og rør godt sammen. ' ||
            '3. Luk beholderen og sæt i køleskabet natten over (mindst 6 timer). ' ||
            '4. Om morgenen toppes med friske bær og skårede mandler. ' ||
            '5. Rør og nyd kold, eller lad stå ved stuetemperatur i 10 minutter hvis foretrukket.'
        );
    END IF;

    -- ========================================================================
    -- Recipe 4: Chicken Pad Thai
    -- ========================================================================
    SELECT id INTO v_recipe_id FROM recipes WHERE name = 'Chicken Pad Thai' AND user_id = admin_user_id LIMIT 1;
    
    IF v_recipe_id IS NULL THEN
        INSERT INTO recipes (
            user_id, name, description, instructions, prep_time_minutes, 
            cook_time_minutes, total_time_minutes, servings, difficulty_level, 
            category, cuisine_type, is_public, base_locale
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
            20, 15, 35, 4, 'HARD', 'DINNER', 'THAI', true, 'en'
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
        
        -- Dietary restrictions
        INSERT INTO recipe_dietary_restrictions (recipe_id, restriction) VALUES 
            (v_recipe_id, 'HIGH_PROTEIN')
        ON CONFLICT DO NOTHING;
        
        -- Allergens
        INSERT INTO recipe_allergens (recipe_id, allergy) VALUES 
            (v_recipe_id, 'EGGS'), (v_recipe_id, 'PEANUTS'), (v_recipe_id, 'SOY'), (v_recipe_id, 'FISH')
        ON CONFLICT DO NOTHING;
    END IF;

    -- Add Danish translation for Chicken Pad Thai
    IF v_recipe_id IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM recipe_translations rt WHERE rt.recipe_id = v_recipe_id AND rt.locale = 'da'
    ) THEN
        INSERT INTO recipe_translations (recipe_id, locale, name, description, instructions) VALUES (
            v_recipe_id,
            'da',
            'Kylling Pad Thai',
            'Klassisk thailandsk wokstegte nudler med kylling, jordnødder og tamarindsauce. En restaurantfavorit, du kan lave derhjemme.',
            '1. Blød risnudler i lunkent vand i 30 minutter indtil de er bøjelige men ikke bløde. ' ||
            '2. Opvarm olie i en stor wok eller stegepande over høj varme. ' ||
            '3. Tilsæt kylling og steg indtil den ikke længere er lyserød, ca. 5 minutter. ' ||
            '4. Skub kyllingen til den ene side, tilsæt æg og rør sammen. ' ||
            '5. Tilsæt drænede nudler og pad thai-sauce. ' ||
            '6. Vend alt sammen og steg i 2-3 minutter. ' ||
            '7. Tilsæt bønnespirer og steg i 1 minut mere. ' ||
            '8. Server toppet med knuste jordnødder, limebåde og frisk koriander.'
        );
    END IF;

    -- ========================================================================
    -- Recipe 5: Chocolate Chip Cookies
    -- ========================================================================
    SELECT id INTO v_recipe_id FROM recipes WHERE name = 'Classic Chocolate Chip Cookies' AND user_id = admin_user_id LIMIT 1;
    
    IF v_recipe_id IS NULL THEN
        INSERT INTO recipes (
            user_id, name, description, instructions, prep_time_minutes, 
            cook_time_minutes, total_time_minutes, servings, difficulty_level, 
            category, cuisine_type, is_public, base_locale
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
            15, 11, 26, 24, 'EASY', 'SNACK', 'AMERICAN', true, 'en'
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
        
        -- Dietary restrictions
        INSERT INTO recipe_dietary_restrictions (recipe_id, restriction) VALUES 
            (v_recipe_id, 'VEGETARIAN')
        ON CONFLICT DO NOTHING;
        
        -- Allergens
        INSERT INTO recipe_allergens (recipe_id, allergy) VALUES 
            (v_recipe_id, 'EGGS'), (v_recipe_id, 'MILK'), (v_recipe_id, 'WHEAT')
        ON CONFLICT DO NOTHING;
    END IF;

    -- Add Danish translation for Classic Chocolate Chip Cookies
    IF v_recipe_id IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM recipe_translations rt WHERE rt.recipe_id = v_recipe_id AND rt.locale = 'da'
    ) THEN
        INSERT INTO recipe_translations (recipe_id, locale, name, description, instructions) VALUES (
            v_recipe_id,
            'da',
            'Klassiske Chokoladechips Cookies',
            'Den ultimative komfortdessert - bløde, seje chokoladechips cookies, som alle elsker.',
            '1. Forvarm ovnen til 375°F (190°C). Læg bageplader med bagepapir. ' ||
            '2. I en stor skål, pisk blød smør, brunt sukker og hvidt sukker sammen indtil let og luftigt. ' ||
            '3. Pisk æg i en ad gangen, og tilsæt derefter vaniljeekstrakt. ' ||
            '4. I en separat skål, pisk mel, bagepulver og salt sammen. ' ||
            '5. Bland gradvist tørre ingredienser i våde ingredienser indtil lige kombineret. ' ||
            '6. Fold chokoladechips i. ' ||
            '7. Drop afrundede spiseskefulde dej på forberedte bageplader, med 5 cm mellemrum. ' ||
            '8. Bag i 9-11 minutter indtil kanterne er gyldne men centrene stadig er bløde. ' ||
            '9. Køl på bagepladen i 5 minutter før overføring til kagerist.'
        );
    END IF;

    -- ========================================================================
    -- Recipe 6: Caesar Salad
    -- ========================================================================
    SELECT id INTO v_recipe_id FROM recipes WHERE name = 'Classic Caesar Salad' AND user_id = admin_user_id LIMIT 1;
    
    IF v_recipe_id IS NULL THEN
        INSERT INTO recipes (
            user_id, name, description, instructions, prep_time_minutes, 
            cook_time_minutes, total_time_minutes, servings, difficulty_level, 
            category, cuisine_type, is_public, base_locale
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
            15, 10, 25, 4, 'EASY', 'LUNCH', 'ITALIAN', true, 'en'
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
        
        -- Dietary restrictions
        INSERT INTO recipe_dietary_restrictions (recipe_id, restriction) VALUES 
            (v_recipe_id, 'VEGETARIAN')
        ON CONFLICT DO NOTHING;
        
        -- Allergens
        INSERT INTO recipe_allergens (recipe_id, allergy) VALUES 
            (v_recipe_id, 'EGGS'), (v_recipe_id, 'MILK'), (v_recipe_id, 'FISH'), (v_recipe_id, 'WHEAT')
        ON CONFLICT DO NOTHING;
    END IF;

    -- Add Danish translation for Classic Caesar Salad
    IF v_recipe_id IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM recipe_translations rt WHERE rt.recipe_id = v_recipe_id AND rt.locale = 'da'
    ) THEN
        INSERT INTO recipe_translations (recipe_id, locale, name, description, instructions) VALUES (
            v_recipe_id,
            'da',
            'Klassisk Caesar Salat',
            'Sprød romaine salat med hjemmelavet Caesar-dressing, parmesanost og croutoner.',
            '1. Vask og hak romaine salat i bidstørrelse stykker. ' ||
            '2. Lav croutoner: skær brød i terninger, vend med olivenolie, hvidløg og salt, og bag derefter ved 350°F (175°C) i 10 minutter. ' ||
            '3. Til dressing: pisk mayonnaise, parmesan, citronsaft, hvidløg, ansjospaste og Worcestershire-sauce sammen. ' ||
            '4. Vend salat med dressing i en stor skål. ' ||
            '5. Toppes med croutoner og ekstra parmesanost. ' ||
            '6. Server straks.'
        );
    END IF;

    -- ========================================================================
    -- Add Danish translations for all ingredients used in recipes
    -- ========================================================================
    INSERT INTO ingredient_translations (ingredient_id, locale, name)
    SELECT i.id, 'da',
           CASE i.name
               WHEN 'Pizza Dough' THEN 'Pizzadej'
               WHEN 'Tomato Sauce' THEN 'Tomatsauce'
               WHEN 'Mozzarella Cheese' THEN 'Mozzarella'
               WHEN 'Fresh Basil' THEN 'Frisk Basilikum'
               WHEN 'Olive Oil' THEN 'Olivenolie'
               WHEN 'Salmon Fillet' THEN 'Laksfilet'
               WHEN 'Asparagus' THEN 'Asparges'
               WHEN 'Zucchini' THEN 'Courgette'
               WHEN 'Cherry Tomatoes' THEN 'Cherrytomater'
               WHEN 'Lemon' THEN 'Citron'
               WHEN 'Salt' THEN 'Salt'
               WHEN 'Black Pepper' THEN 'Sort Peber'
               WHEN 'Rolled Oats' THEN 'Havregryn'
               WHEN 'Milk' THEN 'Mælk'
               WHEN 'Greek Yogurt' THEN 'Græsk Yoghurt'
               WHEN 'Honey' THEN 'Honning'
               WHEN 'Vanilla Extract' THEN 'Vaniljeekstrakt'
               WHEN 'Berries' THEN 'Bær'
               WHEN 'Almonds' THEN 'Mandler'
               WHEN 'Rice Noodles' THEN 'Risnudler'
               WHEN 'Chicken Breast' THEN 'Kyllingebryst'
               WHEN 'Eggs' THEN 'Æg'
               WHEN 'Bean Sprouts' THEN 'Bønnespirer'
               WHEN 'Peanuts' THEN 'Jordnødder'
               WHEN 'Lime' THEN 'Lime'
               WHEN 'Fish Sauce' THEN 'Fiskesauce'
               WHEN 'Tamarind Paste' THEN 'Tamarindpuré'
               WHEN 'Brown Sugar' THEN 'Brun Sukker'
               WHEN 'Soy Sauce' THEN 'Sojasauce'
               WHEN 'Butter' THEN 'Smør'
               WHEN 'White Sugar' THEN 'Hvidt Sukker'
               WHEN 'All-Purpose Flour' THEN 'Hvedemel'
               WHEN 'Baking Soda' THEN 'Bagepulver'
               WHEN 'Chocolate Chips' THEN 'Chokoladechips'
               WHEN 'Romaine Lettuce' THEN 'Romaine Salat'
               WHEN 'Bread' THEN 'Brød'
               WHEN 'Garlic' THEN 'Hvidløg'
               WHEN 'Mayonnaise' THEN 'Mayonnaise'
               WHEN 'Parmesan Cheese' THEN 'Parmesan'
               WHEN 'Anchovy Paste' THEN 'Ansjospaste'
               WHEN 'Worcestershire Sauce' THEN 'Worcestershire-sauce'
           END
    FROM ingredients i
    WHERE i.name IN (
        'Pizza Dough', 'Tomato Sauce', 'Mozzarella Cheese', 'Fresh Basil', 'Olive Oil',
        'Salmon Fillet', 'Asparagus', 'Zucchini', 'Cherry Tomatoes', 'Lemon', 'Salt', 'Black Pepper',
        'Rolled Oats', 'Milk', 'Greek Yogurt', 'Honey', 'Vanilla Extract', 'Berries', 'Almonds',
        'Rice Noodles', 'Chicken Breast', 'Eggs', 'Bean Sprouts', 'Peanuts', 'Lime', 'Fish Sauce', 'Tamarind Paste', 'Brown Sugar', 'Soy Sauce',
        'Butter', 'White Sugar', 'All-Purpose Flour', 'Baking Soda', 'Chocolate Chips',
        'Romaine Lettuce', 'Bread', 'Garlic', 'Mayonnaise', 'Parmesan Cheese', 'Anchovy Paste', 'Worcestershire Sauce'
    )
      AND NOT EXISTS (SELECT 1 FROM ingredient_translations it WHERE it.ingredient_id = i.id AND it.locale = 'da')
    ON CONFLICT (ingredient_id, locale) DO NOTHING;

END $$;
