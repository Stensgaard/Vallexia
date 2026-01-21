package com.vallexia.recipe.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin request DTO to create a canonical ingredient with translations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateIngredientRequestDto {

  @NotBlank
  @Size(max = 255)
  private String canonicalName;

  /**
   * Map of locale -> translated name (e.g., {\"da\":\"smør\",\"en\":\"butter\"}).
   */
  private Map<String, String> translations;
}

