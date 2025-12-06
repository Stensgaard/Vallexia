package com.vallexia.user.entity;

import com.vallexia.common.enums.SupportedAllergy;
import com.vallexia.common.enums.SupportedCuisineType;
import com.vallexia.common.enums.SupportedDietaryRestriction;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Dietary preferences entity storing user's dietary restrictions and preferences.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
@Entity
@Table(name = "dietary_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DietaryPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "dietary_restrictions", joinColumns = @JoinColumn(name = "preferences_id"))
    @Column(name = "restriction")
    private Set<SupportedDietaryRestriction> restrictions = new HashSet<>();
    
    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "allergies", joinColumns = @JoinColumn(name = "preferences_id"))
    @Column(name = "allergy")
    private Set<SupportedAllergy> allergies = new HashSet<>();
    
    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "cuisine_preferences", joinColumns = @JoinColumn(name = "preferences_id"))
    @Column(name = "cuisine")
    private Set<SupportedCuisineType> preferredCuisines = new HashSet<>();
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Helper methods
    public void addRestriction(SupportedDietaryRestriction restriction) {
        this.restrictions.add(restriction);
    }
    
    public void removeRestriction(SupportedDietaryRestriction restriction) {
        this.restrictions.remove(restriction);
    }
    
    public void addAllergy(SupportedAllergy allergy) {
        this.allergies.add(allergy);
    }
    
    public void removeAllergy(SupportedAllergy allergy) {
        this.allergies.remove(allergy);
    }
    
    public void addPreferredCuisine(SupportedCuisineType cuisine) {
        this.preferredCuisines.add(cuisine);
    }
    
    public void removePreferredCuisine(SupportedCuisineType cuisine) {
        this.preferredCuisines.remove(cuisine);
    }
}
