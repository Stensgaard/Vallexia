package com.vallexia.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User principal implementation for Spring Security.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
public class UserPrincipal implements UserDetails {
    
    private Long id;
    private String username;
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean enabled;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    
    public UserPrincipal(Long id, String username, String email, String password, 
                        Collection<? extends GrantedAuthority> authorities,
                        boolean enabled, boolean accountNonExpired, 
                        boolean accountNonLocked, boolean credentialsNonExpired) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
    }
    
    /**
     * Create UserPrincipal from JWT token claims.
     * Used for stateless JWT authentication without database lookup.
     * 
     * <p>Account status flags (enabled, accountNonExpired, accountNonLocked, credentialsNonExpired)
     * are set to true by design, as token validation and blacklist checking handle account status.
     * Tokens should be invalidated (blacklisted) when accounts are disabled/locked.
     * 
     * <p>This method converts role strings to Spring Security GrantedAuthority objects
     * and creates a UserPrincipal suitable for stateless authentication.
     * 
     * @param userId the user ID from token claims
     * @param username the username from token claims
     * @param roles the list of role strings from token claims
     * @return UserPrincipal instance
     * @throws IllegalArgumentException if userId is null, username is null/empty, or roles is null/empty
     */
    public static UserPrincipal createFromJwtClaims(Long userId, String username, List<String> roles) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("Roles cannot be null or empty");
        }
        
        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        
        return new UserPrincipal(
                userId,
                username,
                null,  // email not in token
                "",    // password not needed for JWT auth
                authorities,
                true,  // enabled - assume enabled if token is valid
                true,  // accountNonExpired
                true,  // accountNonLocked
                true   // credentialsNonExpired
        );
    }
    
    public Long getId() {
        return id;
    }
    
    public String getEmail() {
        return email;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @JsonIgnore
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
