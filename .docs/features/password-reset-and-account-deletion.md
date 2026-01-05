# Password Reset and Account Deletion - Implementation Guide

## Overview

This document outlines the implementation plan for two critical user account management features:
1. **Password Reset** - Allows users to reset forgotten passwords via email
2. **Account Deletion** - Allows users to permanently delete their accounts (GDPR compliance)

## Current State

### Existing Features
- User registration with password
- User login with password verification
- Password change functionality: **NOT IMPLEMENTED**
- Account deletion functionality: **NOT IMPLEMENTED**

### Existing Audit Event Types
- `PASSWORD_CHANGE` - Defined but not used (only in tests)
- `ACCOUNT_DELETED` - **NOT DEFINED** - needs to be added to `EventType` enum

## Feature 1: Password Reset

### Overview
Allow users to reset their password when forgotten. This should include:
- Password reset request via email
- Secure token-based reset link
- Token expiration and single-use validation
- Audit logging for security

### Implementation Plan

#### 1.1 Database Schema

**New Table: `password_reset_tokens`**
```sql
CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_token ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_user ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_expires ON password_reset_tokens(expires_at);
```

#### 1.2 Entity

**File**: `src/main/java/com/vallexia/auth/entity/PasswordResetToken.java`

```java
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    private Boolean used = false;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isValid() {
        return !used && !isExpired();
    }
}
```

#### 1.3 Repository

**File**: `src/main/java/com/vallexia/auth/repository/PasswordResetTokenRepository.java`

```java
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    List<PasswordResetToken> findByUserIdAndUsedFalse(Long userId);
    void deleteByExpiresAtBefore(LocalDateTime now);
}
```

#### 1.4 DTOs

**File**: `src/main/java/com/vallexia/auth/dto/PasswordResetRequestDto.java`
```java
public class PasswordResetRequestDto {
    @NotBlank
    @Email
    private String email;
}
```

**File**: `src/main/java/com/vallexia/auth/dto/PasswordResetDto.java`
```java
public class PasswordResetDto {
    @NotBlank
    private String token;
    
    @NotBlank
    @Size(min = 8, max = 100)
    private String newPassword;
    
    @NotBlank
    private String confirmPassword;
}
```

#### 1.5 Service Methods

**File**: `src/main/java/com/vallexia/auth/service/PasswordResetService.java`

**Key Methods:**
1. `requestPasswordReset(String email, HttpServletRequest request)`
   - Find user by email
   - Generate secure token (UUID or cryptographically secure random)
   - Create `PasswordResetToken` with 1-hour expiration
   - Invalidate previous unused tokens for user
   - Send email with reset link
   - **Audit log**: `EventType.PASSWORD_CHANGE` with description "Password reset requested"

2. `resetPassword(PasswordResetDto dto, HttpServletRequest request)`
   - Validate token exists and is valid
   - Validate password and confirmation match
   - Validate password strength
   - Update user password
   - Mark token as used
   - Invalidate all user sessions (optional - blacklist all tokens)
   - **Audit log**: `EventType.PASSWORD_CHANGE` with description "Password reset completed"

#### 1.6 Email Service Integration

**File**: `src/main/java/com/vallexia/auth/service/EmailService.java`

```java
@Service
public class EmailService {
    public void sendPasswordResetEmail(User user, String resetToken, String resetUrl) {
        // Implementation using email service (e.g., SendGrid, AWS SES, etc.)
        // Include:
        // - Reset link: {baseUrl}/reset-password?token={token}
        // - Expiration time (1 hour)
        // - Security warning about not sharing link
    }
}
```

#### 1.7 Controller Endpoints

**File**: `src/main/java/com/vallexia/auth/controller/AuthController.java`

```java
@PostMapping("/password-reset/request")
public ResponseEntity<Void> requestPasswordReset(
    @Valid @RequestBody PasswordResetRequestDto dto,
    HttpServletRequest request) {
    passwordResetService.requestPasswordReset(dto.getEmail(), request);
    return ResponseEntity.ok().build();
}

@PostMapping("/password-reset")
public ResponseEntity<Void> resetPassword(
    @Valid @RequestBody PasswordResetDto dto,
    HttpServletRequest request) {
    passwordResetService.resetPassword(dto, request);
    return ResponseEntity.ok().build();
}
```

#### 1.8 Security Considerations

1. **Token Generation**: Use cryptographically secure random (e.g., `SecureRandom`)
2. **Token Expiration**: 1 hour (configurable)
3. **Rate Limiting**: Limit password reset requests per email/IP
4. **Token Single-Use**: Mark as used after successful reset
5. **Email Validation**: Don't reveal if email exists (security through obscurity)
6. **Password Strength**: Enforce same rules as registration
7. **Session Invalidation**: Optionally invalidate all user sessions after reset

#### 1.9 Audit Logging

- **Request Password Reset**: `EventType.PASSWORD_CHANGE` with `success = true`
- **Complete Password Reset**: `EventType.PASSWORD_CHANGE` with `success = true`
- **Invalid Token Attempt**: `EventType.SECURITY_VIOLATION` with `success = false`

---

## Feature 2: Account Deletion

### Overview
Allow users to permanently delete their accounts. This is critical for GDPR compliance and user privacy rights.

### Implementation Plan

#### 2.1 Add New Event Type

**File**: `src/main/java/com/vallexia/audit/entity/enums/EventType.java`

```java
/**
 * Account deleted by user (GDPR compliance).
 */
ACCOUNT_DELETED,
```

#### 2.2 Database Considerations

**Option A: Soft Delete (Recommended)**
- Add `deleted_at TIMESTAMP` column to `users` table
- Mark as deleted instead of physical deletion
- Allows data recovery and audit trail maintenance
- Filter deleted users in queries

**Option B: Hard Delete**
- Physically delete user record
- Cascade deletes related data (recipes, favorites, etc.)
- **Risk**: Loses audit trail and recovery capability

**Recommendation**: Use **soft delete** for better audit compliance.

#### 2.3 Migration

**File**: `src/main/resources/db/migration/V{X}__add_user_deletion.sql`

```sql
-- Add soft delete column
ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP;

-- Add index for filtering active users
CREATE INDEX idx_users_deleted_at ON users(deleted_at) WHERE deleted_at IS NULL;

-- Update existing queries to filter deleted users
-- (Application-level filtering recommended)
```

#### 2.4 Entity Update

**File**: `src/main/java/com/vallexia/user/entity/User.java`

```java
@Column
private LocalDateTime deletedAt;

public boolean isDeleted() {
    return deletedAt != null;
}
```

#### 2.5 Repository Updates

**File**: `src/main/java/com/vallexia/user/repository/UserRepository.java`

```java
// Add methods to filter deleted users
Optional<User> findByIdAndDeletedAtIsNull(Long id);
Optional<User> findByUsernameAndDeletedAtIsNull(String username);
Optional<User> findByEmailAndDeletedAtIsNull(String email);
boolean existsByUsernameAndDeletedAtIsNull(String username);
boolean existsByEmailAndDeletedAtIsNull(String email);
```

#### 2.6 DTO

**File**: `src/main/java/com/vallexia/user/dto/DeleteAccountRequestDto.java`

```java
public class DeleteAccountRequestDto {
    @NotBlank
    private String password; // Require password confirmation
    
    @NotNull
    private Boolean confirmDeletion; // Explicit confirmation checkbox
}
```

#### 2.7 Service Method

**File**: `src/main/java/com/vallexia/user/service/UserService.java`

```java
@Transactional
public void deleteAccount(Long userId, DeleteAccountRequestDto dto, HttpServletRequest request) {
    log.info("Deleting account for user ID: {}", userId);
    
    User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found"));
    
    // Verify password
    if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
        throw new ValidationException("Invalid password");
    }
    
    // Verify confirmation
    if (!Boolean.TRUE.equals(dto.getConfirmDeletion())) {
        throw new ValidationException("Account deletion must be confirmed");
    }
    
    // Soft delete: Set deleted_at timestamp
    user.setDeletedAt(LocalDateTime.now());
    user.setEnabled(false); // Disable account immediately
    userRepository.save(user);
    
    // Invalidate all user sessions (blacklist all tokens)
    tokenBlacklistService.blacklistAllUserTokens(userId);
    
    // Audit log BEFORE deletion (so we have record)
    auditService.logAuthenticationEvent(
        EventType.ACCOUNT_DELETED,
        String.format("Account deleted by user ID: %d", userId),
        userId,
        user.getUsername(),
        request,
        true
    );
    
    log.info("Account deleted successfully for user ID: {}", userId);
}
```

#### 2.8 Controller Endpoint

**File**: `src/main/java/com/vallexia/user/controller/UserController.java`

```java
@DeleteMapping("/account")
@PreAuthorize("hasRole('USER')")
public ResponseEntity<Void> deleteAccount(
    @Valid @RequestBody DeleteAccountRequestDto dto,
    HttpServletRequest request,
    Authentication authentication) {
    
    Long userId = authenticationHelper.getCurrentUserId();
    userService.deleteAccount(userId, dto, request);
    return ResponseEntity.noContent().build();
}
```

#### 2.9 Data Cleanup Considerations

**Related Data to Handle:**
1. **Recipes**: Cascade delete or mark as deleted
2. **Favorite Recipes**: Cascade delete
3. **Nutritional Goals**: Cascade delete
4. **Dietary Preferences**: Cascade delete
5. **User Settings**: Cascade delete
6. **Audit Logs**: **KEEP** - Required for compliance, mark user as deleted
7. **Meal Plans**: Cascade delete (when implemented)
8. **Grocery Lists**: Cascade delete (when implemented)

**Database Cascade Configuration:**
```sql
-- Ensure CASCADE is set on foreign keys
ALTER TABLE recipes 
  DROP CONSTRAINT IF EXISTS fk_recipes_user,
  ADD CONSTRAINT fk_recipes_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
```

#### 2.10 Security Considerations

1. **Password Verification**: Require current password
2. **Explicit Confirmation**: Require checkbox confirmation
3. **Grace Period**: Optional 30-day grace period before permanent deletion
4. **Session Invalidation**: Immediately invalidate all user sessions
5. **Email Notification**: Send confirmation email to user
6. **Admin Override**: Allow admins to restore accounts if needed

#### 2.11 Audit Logging

- **Account Deletion**: `EventType.ACCOUNT_DELETED` with `success = true`
- **Failed Deletion Attempt**: `EventType.SECURITY_VIOLATION` with `success = false` (wrong password)

---

## Implementation Checklist

### Password Reset
- [ ] Create `password_reset_tokens` table migration
- [ ] Create `PasswordResetToken` entity
- [ ] Create `PasswordResetTokenRepository`
- [ ] Create DTOs (`PasswordResetRequestDto`, `PasswordResetDto`)
- [ ] Create `PasswordResetService` with request and reset methods
- [ ] Create `EmailService` for sending reset emails
- [ ] Add controller endpoints
- [ ] Add rate limiting for reset requests
- [ ] Implement audit logging with `EventType.PASSWORD_CHANGE`
- [ ] Add validation and error handling
- [ ] Write unit tests
- [ ] Write integration tests

### Account Deletion
- [ ] Add `ACCOUNT_DELETED` to `EventType` enum
- [ ] Create migration to add `deleted_at` column
- [ ] Update `User` entity with `deletedAt` field
- [ ] Update `UserRepository` to filter deleted users
- [ ] Create `DeleteAccountRequestDto`
- [ ] Implement `deleteAccount()` in `UserService`
- [ ] Add controller endpoint
- [ ] Update all user queries to filter deleted users
- [ ] Implement session invalidation
- [ ] Add audit logging with `EventType.ACCOUNT_DELETED`
- [ ] Add email notification
- [ ] Write unit tests
- [ ] Write integration tests

---

## Testing Requirements

### Password Reset Tests
1. **Request Reset**
   - Valid email exists
   - Invalid email (don't reveal if exists)
   - Rate limiting enforcement
   - Token generation and storage
   - Email sending verification

2. **Reset Password**
   - Valid token
   - Expired token
   - Used token
   - Invalid token
   - Password validation
   - Password confirmation mismatch
   - Successful reset and token invalidation

### Account Deletion Tests
1. **Delete Account**
   - Valid password and confirmation
   - Invalid password
   - Missing confirmation
   - Soft delete verification
   - Related data cascade
   - Session invalidation
   - Audit log creation

---

## Configuration

### Application Properties

```yaml
app:
  password-reset:
    token-expiration-minutes: 60
    rate-limit:
      requests: 3
      duration-minutes: 15
  account-deletion:
    grace-period-days: 30  # Optional
    require-password-confirmation: true
```

---

## Security Best Practices

1. **Password Reset**
   - Use cryptographically secure tokens
   - Short expiration (1 hour)
   - Single-use tokens
   - Rate limit requests
   - Don't reveal email existence
   - HTTPS only for reset links

2. **Account Deletion**
   - Require password confirmation
   - Explicit user confirmation
   - Immediate session invalidation
   - Soft delete for audit trail
   - Email notification
   - Admin restore capability

---

## Future Enhancements

1. **Password Reset**
   - SMS-based reset option
   - Security questions as backup
   - Password history (prevent reuse)

2. **Account Deletion**
   - Data export before deletion (GDPR)
   - Grace period with restore option
   - Bulk deletion for admins
   - Anonymization option instead of deletion

