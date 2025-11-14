package com.vallexia.recipe.entity;

import lombok.Getter;

/**
 * Recipe difficulty levels representing skill required.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Getter
public enum DifficultyLevel {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard"),
    EXPERT("Expert");
    
    private final String displayName;
    
    DifficultyLevel(String displayName) {
        this.displayName = displayName;
    }
}
