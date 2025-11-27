package com.vallexia.recipe.entity;

import com.vallexia.common.validator.ValidLocale;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a translation of ingredient name for a specific locale.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-15
 */
@Entity
@Table(name = "ingredient_translations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ingredient_id", "locale"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientTranslation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;
    
    @NotBlank
    @ValidLocale
    @Column(nullable = false, length = 10)
    private String locale;
    
    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String name;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
