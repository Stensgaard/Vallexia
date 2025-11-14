package com.vallexia.user.entity;

import com.vallexia.user.entity.enums.Allergy;
import com.vallexia.user.entity.enums.CuisineType;
import com.vallexia.user.entity.enums.DietaryRestriction;
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
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
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
    private Set<DietaryRestriction> restrictions = new HashSet<>();
    
    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "allergies", joinColumns = @JoinColumn(name = "preferences_id"))
    @Column(name = "allergy")
    private Set<Allergy> allergies = new HashSet<>();
    
    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "cuisine_preferences", joinColumns = @JoinColumn(name = "preferences_id"))
    @Column(name = "cuisine")
    private Set<CuisineType> preferredCuisines = new HashSet<>();
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Helper methods
    public void addRestriction(DietaryRestriction restriction) {
        this.restrictions.add(restriction);
    }
    
    public void removeRestriction(DietaryRestriction restriction) {
        this.restrictions.remove(restriction);
    }
    
    public void addAllergy(Allergy allergy) {
        this.allergies.add(allergy);
    }
    
    public void removeAllergy(Allergy allergy) {
        this.allergies.remove(allergy);
    }
    
    public void addPreferredCuisine(CuisineType cuisine) {
        this.preferredCuisines.add(cuisine);
    }
    
    public void removePreferredCuisine(CuisineType cuisine) {
        this.preferredCuisines.remove(cuisine);
    }
}
