package com.vallexia.user.entity;

import lombok.Getter;

/**
 * User roles for authorization and access control.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Getter
public enum Role {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN"),
    MODERATOR("ROLE_MODERATOR");
    
    private final String authority;
    
    Role(String authority) {
        this.authority = authority;
    }
}
