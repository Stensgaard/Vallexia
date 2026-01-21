package com.vallexia.store.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing an ingredient that is currently discounted via at least one flyer offer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountedIngredientDto {

  private Long ingredientId;
  private String ingredientName;

  // Best (lowest) price found among current offers for this ingredient
  private BigDecimal bestPrice;

  // Store where the best price was found (optional, but useful for UI)
  private Long storeId;
  private String storeName;

  private LocalDate validFrom;
  private LocalDate validTo;
}

