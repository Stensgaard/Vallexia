package com.vallexia.user.service;

import com.vallexia.audit.entity.enums.EventType;
import com.vallexia.audit.service.AuditService;
import com.vallexia.user.dto.UserProfileDto;
import com.vallexia.user.entity.User;
import com.vallexia.user.mapper.UserMapper;
import com.vallexia.user.repository.UserRepository;
import com.vallexia.user.exception.UserNotFoundException;
import com.vallexia.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing user profile operations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
@Slf4j
@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuditService auditService;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param userRepository the user repository
     * @param userMapper the user mapper
     * @param auditService the audit service
     */
    public UserService(UserRepository userRepository, UserMapper userMapper, AuditService auditService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.auditService = auditService;
    }
    
    
    /**
     * Get user profile by ID.
     * 
     * @param userId user ID
     * @return UserProfileDto
     * @throws UserNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        return userMapper.toUserProfileDto(user);
    }
    
    /**
     * Update user profile.
     * 
     * @param userId user ID
     * @param userProfileDto updated profile data
     * @return updated UserProfileDto
     * @throws UserNotFoundException if user not found
     * @throws ValidationException if validation fails
     */
    public UserProfileDto updateUserProfile(Long userId, UserProfileDto userProfileDto) {
        log.info("Updating profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                    String.format("User not found with id: %d. This may be due to account deletion or invalid user ID.", userId)
                ));
        
        // Check if email is being changed and if it's already in use
        if (!user.getEmail().equals(userProfileDto.getEmail()) && 
            userRepository.existsByEmail(userProfileDto.getEmail())) {
            throw new ValidationException("Email is already in use");
        }
        
        // Update user fields (username is immutable and not updated)
        user.setEmail(userProfileDto.getEmail());
        user.setHouseholdSize(userProfileDto.getHouseholdSize());
        user.setMealTypes(userProfileDto.getMealTypes());
        
        User updatedUser = userRepository.save(user);
        
        // Audit log
        auditService.logEvent(
            EventType.PROFILE_UPDATE,
            userId,
            String.format("User profile updated for user ID: %d", userId)
        );
        
        log.info("Profile updated successfully for user ID: {}", userId);
        
        return userMapper.toUserProfileDto(updatedUser);
    }
}
