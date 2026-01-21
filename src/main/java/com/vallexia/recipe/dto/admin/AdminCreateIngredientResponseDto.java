package com.vallexia.recipe.dto.admin;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin response DTO for created ingredient.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateIngredientResponseDto {

  private Long ingredientId;
  private String canonicalName;
  private Map<String, String> translations;
}

