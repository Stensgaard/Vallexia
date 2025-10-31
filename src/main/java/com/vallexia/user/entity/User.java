package com.vallexia.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity representing application users with authentication and profile information.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "users", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "username"),
           @UniqueConstraint(columnNames = "email")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(min = 3, max = 20)
    @Column(unique = true, nullable = false)
    private String username;
    
    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;
    
    @JsonIgnore
    @NotBlank
    @Size(min = 8)
    @Column(nullable = false)
    private String passwordHash;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private DietaryPreferences dietaryPreferences;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private NutritionalGoals nutritionalGoals;
    
    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Column(nullable = false)
    private Boolean accountNonExpired = true;
    
    @Column(nullable = false)
    private Boolean accountNonLocked = true;
    
    @Column(nullable = false)
    private Boolean credentialsNonExpired = true;
    
    @Column(nullable = false)
    private Integer failedLoginAttempts = 0;
    
    @Column
    private LocalDateTime accountLockedUntil;
    
    @Column(nullable = false)
    private Integer householdSize = 1;
    
    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_meal_types", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "meal_type")
    private Set<MealType> mealTypes = new HashSet<>();
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.FREE;
    
    @Column
    private LocalDateTime subscriptionExpiresAt;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public void addRole(Role role) {
        this.roles.add(role);
    }
    
    public void removeRole(Role role) {
        this.roles.remove(role);
    }
    
    public boolean hasRole(Role role) {
        return this.roles.contains(role);
    }
    
    public boolean isAccountLocked() {
        return accountLockedUntil != null && accountLockedUntil.isAfter(LocalDateTime.now());
    }
    
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }
    
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
    }
    
    public void setAccountLockedUntil(LocalDateTime accountLockedUntil) {
        this.accountLockedUntil = accountLockedUntil;
    }
}
