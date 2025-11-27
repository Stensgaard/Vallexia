package com.vallexia.auth.util;

import com.vallexia.config.security.AccountSecurityProperties;
import com.vallexia.user.entity.User;
import com.vallexia.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Helper component for account security operations including failed login attempt handling.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-30
 */
@Slf4j
@Component
public class AccountSecurityHelper {
    
    private final UserRepository userRepository;
    private final AccountSecurityProperties accountSecurityProperties;
    
    /**
     * Constructor with dependency injection.
     * 
     * @param userRepository user repository
     * @param accountSecurityProperties account security configuration
     */
    public AccountSecurityHelper(UserRepository userRepository, 
                                 AccountSecurityProperties accountSecurityProperties) {
        this.userRepository = userRepository;
        this.accountSecurityProperties = accountSecurityProperties;
    }
    
    /**
     * Handle failed login attempt: increment counter and lock account if threshold reached.
     * 
     * @param user the user with failed login attempt
     */
    public void handleFailedLoginAttempt(User user) {
        user.incrementFailedLoginAttempts();
        
        int maxAttempts = accountSecurityProperties.getMaxFailedAttempts();
        int lockoutMinutes = accountSecurityProperties.getDurationMinutes();
        
        if (user.getFailedLoginAttempts() >= maxAttempts) {
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(lockoutMinutes));
            log.warn("Account locked for {} minutes due to {} failed attempts", 
                lockoutMinutes, user.getFailedLoginAttempts());
        }
        
        userRepository.save(user);
    }
    
    /**
     * Reset failed login attempts on successful authentication.
     * 
     * @param user the user to reset attempts for
     */
    public void resetFailedLoginAttempts(User user) {
        if (user.getFailedLoginAttempts() > 0) {
            user.resetFailedLoginAttempts();
            userRepository.save(user);
        }
    }
}
