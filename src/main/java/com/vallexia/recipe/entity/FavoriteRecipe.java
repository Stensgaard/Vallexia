package com.vallexia.recipe.entity;

import com.vallexia.user.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Join entity representing a user's favorite recipe.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
@Entity
@Table(name = "favorite_recipes", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"user_id", "spoonacular_id"})
       },
       indexes = {
           @Index(name = "idx_favorite_recipes_user_id", columnList = "user_id"),
           @Index(name = "idx_favorite_recipes_spoonacular_id", columnList = "spoonacular_id")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteRecipe {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "spoonacular_id", nullable = false)
    private Integer spoonacularId;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
