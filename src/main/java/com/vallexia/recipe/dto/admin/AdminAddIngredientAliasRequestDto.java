package com.vallexia.recipe.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin request DTO to add an alias for an ingredient.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAddIngredientAliasRequestDto {

  @NotBlank
  @Size(max = 10)
  private String locale;

  @NotBlank
  @Size(max = 255)
  private String alias;

  private Integer priority;
}

