package com.vallexia.store.entity;

import com.vallexia.recipe.entity.Ingredient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * 1:1 mapping from a scraped store offer to a canonical ingredient.
 */
@Entity
@Table(
    name = "store_offer_ingredient_match",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"offer_id"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreOfferIngredientMatch {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "offer_id", nullable = false)
  private StoreOffer offer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ingredient_id", nullable = false)
  private Ingredient ingredient;

  @Column(name = "locale", nullable = false, length = 10)
  private String locale;

  @Column(name = "match_method", nullable = false, length = 30)
  private String matchMethod;

  @Column(name = "confidence", nullable = false, precision = 5, scale = 4)
  private java.math.BigDecimal confidence;

  @Column(name = "matched_text", nullable = false, length = 255)
  private String matchedText;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}

