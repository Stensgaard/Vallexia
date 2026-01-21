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
 * Alias/synonym for an ingredient name in a specific locale.
 *
 * <p>Used to match noisy flyer offer names to canonical ingredients.</p>
 */
@Entity
@Table(
    name = "ingredient_aliases",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"locale", "alias"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientAlias {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ingredient_id", nullable = false)
  private Ingredient ingredient;

  @Column(name = "locale", nullable = false, length = 10)
  private String locale;

  @Column(name = "alias", nullable = false, length = 255)
  private String alias;

  @Column(name = "priority", nullable = false)
  private int priority = 0;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}

