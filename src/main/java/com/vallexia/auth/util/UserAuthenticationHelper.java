package com.vallexia.auth.util;

import com.vallexia.auth.exception.AccountDisabledException;
import com.vallexia.auth.exception.AccountLockedException;
import com.vallexia.user.entity.User;
import com.vallexia.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Helper component for user authentication operations including lookup and account validation.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-30
 */
@Slf4j
@Component
public class UserAuthenticationHelper {
    
    private final UserRepository userRepository;
    
    /**
     * Constructor with dependency injection.
     * 
     * @param userRepository user repository
     */
    public UserAuthenticationHelper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Find user by username or email.
     * Tries username first (immutable) then falls back to email.
     * 
     * @param usernameOrEmail username or email to search for
     * @return Optional containing the user if found
     */
    public Optional<User> findUserByUsernameOrEmail(String usernameOrEmail) {
        Optional<User> user = userRepository.findByUsername(usernameOrEmail);
        return user.isPresent() ? user : userRepository.findByEmail(usernameOrEmail);
    }
    
    /**
     * Validate account status (disabled, locked).
     * 
     * @param user the user to validate
     * @throws AccountDisabledException if account is disabled
     * @throws AccountLockedException if account is locked
     */
    public void validateAccountStatus(User user) {
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            log.warn("Attempted login for disabled account: {}", user.getUsername());
            throw new AccountDisabledException("Account is disabled");
        }
        
        if (user.isAccountLocked()) {
            log.warn("Account locked until {}", user.getAccountLockedUntil());
            throw new AccountLockedException("Account is temporarily locked due to multiple failed login attempts");
        }
    }
}
