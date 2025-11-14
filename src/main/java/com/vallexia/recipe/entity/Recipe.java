package com.vallexia.recipe.entity;

import com.vallexia.user.entity.CuisineType;
import com.vallexia.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Recipe entity representing a recipe with ingredients, instructions, and nutritional information.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "recipes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User creator;
    
    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String name;
    
    @Size(max = 2000)
    @Column(length = 2000)
    private String description;
    
    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String instructions;
    
    @Min(value = 0)
    @Column(name = "prep_time_minutes")
    private Integer prepTimeMinutes;
    
    @Min(value = 0)
    @Column(name = "cook_time_minutes")
    private Integer cookTimeMinutes;
    
    @Min(value = 0)
    @Column(name = "total_time_minutes")
    private Integer totalTimeMinutes; // Calculated or manual
    
    @Min(value = 1)
    @Column(nullable = false)
    private Integer servings = 1; // Base servings count
    
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", length = 20)
    private DifficultyLevel difficultyLevel;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RecipeCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "cuisine_type", length = 50)
    private CuisineType cuisineType;
    
    @Size(max = 500)
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;
    
    @OneToOne(mappedBy = "recipe", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private NutritionalInfo nutritionalInfo;
    
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<RecipeIngredient> ingredients = new ArrayList<>();
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "recipe_tags", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "tag", length = 100)
    private Set<String> tags = new HashSet<>();
    
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<FavoriteRecipe> favoriteRecipes = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // TODO: move the helper methods to a separate class?
    /**
     * Helper method to calculate total time if prep and cook times are set.
     */
    public void calculateTotalTime() {
        if (prepTimeMinutes != null && cookTimeMinutes != null) {
            this.totalTimeMinutes = prepTimeMinutes + cookTimeMinutes;
        }
    }
    
    /**
     * Add an ingredient to the recipe.
     */
    public void addIngredient(RecipeIngredient recipeIngredient) {
        ingredients.add(recipeIngredient);
        recipeIngredient.setRecipe(this);
    }
    
    /**
     * Remove an ingredient from the recipe.
     */
    public void removeIngredient(RecipeIngredient recipeIngredient) {
        ingredients.remove(recipeIngredient);
        recipeIngredient.setRecipe(null);
    }
    
    /**
     * Add a tag to the recipe.
     */
    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty()) {
            this.tags.add(tag.toLowerCase().trim());
        }
    }
    
    /**
     * Remove a tag from the recipe.
     */
    public void removeTag(String tag) {
        if (tag != null) {
            this.tags.remove(tag.toLowerCase().trim());
        }
    }
}
