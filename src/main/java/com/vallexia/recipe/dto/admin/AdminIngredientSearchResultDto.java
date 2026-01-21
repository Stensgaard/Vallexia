package com.vallexia.recipe.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight DTO for admin ingredient search results.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminIngredientSearchResultDto {

  private Long ingredientId;
  private String canonicalName;
  private String localizedName;
}

