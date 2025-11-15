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
 * Entity representing a translation of recipe content for a specific locale.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "recipe_translations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"recipe_id", "locale"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeTranslation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;
    
    @NotBlank
    @ValidLocale
    @Column(nullable = false, length = 10)
    private String locale;
    
    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String name;
    
    @Size(max = 2000)
    @Column(length = 2000)
    private String description;
    
    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String instructions;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
