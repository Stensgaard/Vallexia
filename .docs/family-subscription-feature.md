# Family Subscription Feature - Technical Specification

## Overview

This document outlines the planned **Family Subscription** feature that will enable per-person nutrition goals and tracking for multi-person households. This is a future enhancement that will be gated behind the FAMILY subscription tier.

## Current State

### Current Implementation
- **Single nutritional goals per user account** (OneToOne relationship)
- Goals represent household-level nutrition targets
- Works well for:
  - Single users
  - Households with similar nutritional needs
  - Users who want aggregate household tracking

### Current Limitations
- Cannot track individual nutrition goals for different household members
- Cannot assign meal plans to specific people
- No per-person progress tracking
- Recommendations are based on aggregate household goals

## Future Family Subscription Feature

### Feature Description
The Family subscription will allow users to:
1. Create profiles for each household member
2. Set individual nutritional goals per person
3. Assign meal plans to specific members
4. Track individual progress for each person
5. View aggregate household nutrition summaries

### Subscription Tiers

| Tier | Features | Nutrition Goals |
|------|----------|-----------------|
| **FREE** | Basic meal planning | Single account-level goals |
| **PREMIUM** | Advanced features, unlimited recipes | Single account-level goals |
| **FAMILY** | All Premium features + Multi-person support | Per-person goals + household aggregate |

1. FREE (Default)
Basic meal planning
Single account-level nutrition goals
Default for all new users
2. PREMIUM
Advanced features
Unlimited recipes
Single account-level nutrition goals
3. FAMILY (Planned)
All Premium features
Multi-person support
Per-person nutrition goals
Household aggregate tracking
Not yet implemented

### Business Value
- **Monetization**: Premium feature for multi-person households
- **User Segmentation**: Power users pay for advanced functionality
- **Clear Upgrade Path**: Users with `householdSize > 1` see upgrade prompts
- **Market Differentiation**: Competitive feature for family meal planning apps

## Technical Implementation Plan

### Phase 1: Database Schema (Future Migration)

```sql
-- New table for household members
CREATE TABLE household_members (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    age INTEGER,
    gender VARCHAR(20),
    activity_level VARCHAR(50),
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_primary_member_per_user UNIQUE(user_id, is_primary) WHERE is_primary = TRUE
);

-- Modify nutritional_goals to support both user-level and member-level goals
ALTER TABLE nutritional_goals 
    ADD COLUMN household_member_id BIGINT REFERENCES household_members(id) ON DELETE CASCADE,
    ADD CONSTRAINT check_goal_ownership CHECK (
        (user_id IS NOT NULL AND household_member_id IS NULL) OR
        (user_id IS NULL AND household_member_id IS NOT NULL)
    ),
    DROP CONSTRAINT nutritional_goals_user_id_key; -- Remove unique constraint temporarily

-- Re-add unique constraint for user-level goals
ALTER TABLE nutritional_goals 
    ADD CONSTRAINT unique_user_goals UNIQUE(user_id) 
    WHERE household_member_id IS NULL;

-- Unique constraint for member-level goals
ALTER TABLE nutritional_goals 
    ADD CONSTRAINT unique_member_goals UNIQUE(household_member_id) 
    WHERE household_member_id IS NOT NULL;
```

### Phase 2: Backend Changes

#### New Entities
```java
@Entity
@Table(name = "household_members")
public class HouseholdMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    private String name;
    private Integer age;
    private String gender;
    private String activityLevel;
    private Boolean isPrimary = false;
    
    @OneToOne(mappedBy = "householdMember", cascade = CascadeType.ALL)
    private NutritionalGoals nutritionalGoals;
    
    // ... getters, setters, timestamps
}
```

#### Service Layer Updates
```java
// In NutritionalGoalsService
public NutritionalGoalsDto getNutritionalGoals(Long userId, Long memberId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(...));
    
    // Check subscription tier
    if (user.getSubscriptionStatus() == SubscriptionStatus.FAMILY && memberId != null) {
        // Return per-person goals
        return getMemberNutritionalGoals(userId, memberId);
    } else {
        // Return account-level goals (current behavior)
        return getAccountNutritionalGoals(userId);
    }
}

private boolean canCreateMultipleProfiles(Long userId) {
    User user = userRepository.findById(userId).orElseThrow();
    return user.getSubscriptionStatus() == SubscriptionStatus.FAMILY;
}
```

### Phase 3: API Endpoints (Future)

```
# Current endpoints (work for all tiers)
GET    /api/v1/users/nutritional-goals
PUT    /api/v1/users/nutritional-goals

# New Family tier endpoints
GET    /api/v1/users/household-members
POST   /api/v1/users/household-members
GET    /api/v1/users/household-members/{memberId}
PUT    /api/v1/users/household-members/{memberId}
DELETE /api/v1/users/household-members/{memberId}
GET    /api/v1/users/household-members/{memberId}/nutritional-goals
PUT    /api/v1/users/household-members/{memberId}/nutritional-goals
GET    /api/v1/users/nutrition/household-summary
```

### Phase 4: Frontend Changes

#### New Components
- `HouseholdMemberList.vue` - List and manage household members
- `HouseholdMemberForm.vue` - Add/edit household member
- `PerPersonNutritionView.vue` - Individual nutrition tracking
- `HouseholdNutritionSummary.vue` - Aggregate household view

#### UI Updates
- Add member selector to meal plan assignment
- Per-person nutrition dashboards
- Household aggregate view toggle
- Subscription upgrade prompts (already implemented)

## Migration Strategy

### Backward Compatibility
- Current `nutritional_goals.user_id` relationship remains unchanged
- Existing users continue to work without changes
- New `household_member_id` column is optional (nullable)
- Service methods check subscription tier before using new features

### Data Migration
- No data migration needed for existing users
- Primary household member created automatically when user upgrades to FAMILY
- Existing goals can be copied to primary member or kept as account-level

### Feature Flag Approach
```java
public boolean canAccessFamilyFeatures(Long userId) {
    User user = userRepository.findById(userId).orElseThrow();
    return user.getSubscriptionStatus() == SubscriptionStatus.FAMILY;
}
```

## User Experience Flow

### Current Flow (All Tiers)
1. User sets household size in profile
2. User sets single nutritional goals
3. Meal plans use household-level goals
4. Nutrition tracking shows aggregate household data

### Future Family Tier Flow
1. User upgrades to FAMILY subscription
2. System prompts to create household member profiles
3. User creates profiles for each person (name, age, gender, activity level)
4. User sets individual goals for each member
5. When planning meals, user assigns recipes to specific members
6. Nutrition tracking shows:
   - Per-person dashboards
   - Household aggregate summary
   - Individual progress tracking

## Implementation Checklist

### Backend
- [ ] Create `HouseholdMember` entity
- [ ] Create `HouseholdMemberRepository`
- [ ] Create `HouseholdMemberService`
- [ ] Create `HouseholdMemberController`
- [ ] Update `NutritionalGoalsService` to support member-level goals
- [ ] Add subscription tier checks in service methods
- [ ] Create database migration
- [ ] Add validation for FAMILY tier access
- [ ] Update API documentation

### Frontend
- [ ] Create household member management components
- [ ] Update nutrition view to show per-person tracking
- [ ] Add member selector to meal planning
- [ ] Create household summary dashboard
- [ ] Update subscription upgrade flow
- [ ] Add member assignment to meal plans

### Testing
- [ ] Unit tests for household member CRUD
- [ ] Integration tests for per-person goals
- [ ] Subscription tier access tests
- [ ] Backward compatibility tests
- [ ] E2E tests for family subscription flow

## Current Implementation Status

✅ **Completed:**
- UI hint banner in NutritionView for multi-person households
- Subscription utility helpers
- Documentation comments in NutritionalGoalsService
- Auth store includes householdSize

⏳ **Planned:**
- Database schema changes
- Backend service implementation
- Frontend components
- API endpoints

## Notes

- This feature is **not yet implemented** - it's planned for future release
- Current implementation supports the foundation (household size, subscription status)
- Users with `householdSize > 1` see upgrade prompts to FAMILY tier
- No breaking changes to current functionality
- Feature can be implemented incrementally without disrupting existing users

## Related Files

- `src/main/java/com/vallexia/user/entity/NutritionalGoals.java` - Current goals entity
- `src/main/java/com/vallexia/user/service/NutritionalGoalsService.java` - Goals service
- `src/main/java/com/vallexia/user/entity/enums/SubscriptionStatus.java` - Subscription enum
- `src/main/java/com/vallexia/web/src/views/NutritionView.vue` - Nutrition tracking view
- `src/main/java/com/vallexia/web/src/utils/subscriptionUtils.js` - Subscription utilities
