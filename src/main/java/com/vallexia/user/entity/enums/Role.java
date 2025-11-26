package com.vallexia.user.entity.enums;

import lombok.Getter;

/**
 * User roles for authorization and access control.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
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
