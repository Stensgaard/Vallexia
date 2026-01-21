package com.vallexia.store.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for offers that have not yet been matched to an ingredient.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnmatchedStoreOfferDto {

  private Long offerId;
  private Long storeId;
  private String storeName;
  private String productName;
  /**
   * A UI-friendly product name (keeps the name, strips trailing size when size is extracted).
   */
  private String displayProductName;
  /**
   * Product name normalized for matching (marketing/units/quantities stripped).
   * Useful for debugging OCR noise and matching behavior.
   */
  private String normalizedProductName;
  private BigDecimal price;
  private BigDecimal bundlePrice;
  private BigDecimal unitPrice;
  private Integer minPurchaseQty;
  private String minPurchaseUnit;
  private String rawPriceText;
  private BigDecimal packageQtyMin;
  private BigDecimal packageQtyMax;
  private String packageUnit;
  private LocalDate validFrom;
  private LocalDate validTo;
  private boolean dismissed;
  private OffsetDateTime dismissedAt;
}

