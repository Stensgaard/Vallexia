# User Settings Implementation Plan

## Overview

Add a fourth "Settings" tab to the Profile Management page that allows users to customize application display preferences including localization, date/time formats, measurement units, and other UI preferences.

## Settings to Include

### Core Settings

1. **Language** - Interface language (default: English, prepare for future i18n)
2. **Country/Region** - User's country (affects defaults, regional recipes, currency)
3. **Date Format** - MM/DD/YYYY, DD/MM/YYYY, YYYY-MM-DD, DD.MM.YYYY
4. **Time Format** - 12-hour (AM/PM) vs 24-hour
5. **Timezone** - IANA timezone identifier (e.g., "America/New_York", "Europe/London")
6. **First Day of Week** - Monday vs Sunday (critical for meal planning calendar)
7. **Measurement System** - Metric vs Imperial (affects recipe ingredient display)

### Additional Settings (Nice to Have)

8. **Number Format** - Decimal separator (comma vs period), thousands separator
9. **Currency** - For future shopping list features (USD, EUR, GBP, etc.)

## Backend Implementation

### 1. Database Migration

- **File**: `src/main/resources/db/migration/V7__create_user_settings_table.sql`
- Create `user_settings` table with:
- `id` (BIGSERIAL PRIMARY KEY)
- `user_id` (BIGINT, UNIQUE, FK to users)
- `language` (VARCHAR(10), default 'en')
- `country` (VARCHAR(2), ISO 3166-1 alpha-2 country code)
- `date_format` (VARCHAR(20), default 'MM/DD/YYYY')
- `time_format` (VARCHAR(10), default '12h' or '24h')
- `timezone` (VARCHAR(50), default 'UTC')
- `first_day_of_week` (INTEGER, 0=Sunday, 1=Monday, default 1)
- `measurement_system` (VARCHAR(10), 'METRIC' or 'IMPERIAL', default 'METRIC')
- `number_decimal_separator` (VARCHAR(1), default '.')
- `number_thousands_separator` (VARCHAR(1), default ',')
- `currency` (VARCHAR(3), ISO 4217 currency code, nullable)
- `created_at`, `updated_at` timestamps

### 2. Entity

- **File**: `src/main/java/com/vallexia/user/entity/UserSettings.java`
- JPA entity with OneToOne relationship to User
- Enums for: DateFormat, TimeFormat, MeasurementSystem, FirstDayOfWeek
- Validation annotations

### 3. DTOs

- **File**: `src/main/java/com/vallexia/user/dto/UserSettingsDto.java`
- Request/response DTO matching entity fields
- Validation annotations

### 4. Repository

- **File**: `src/main/java/com/vallexia/user/repository/UserSettingsRepository.java`
- Methods: `findByUserId(Long userId)`, `existsByUserId(Long userId)`

### 5. Service

- **File**: `src/main/java/com/vallexia/user/service/UserSettingsService.java`
- Methods:
- `getUserSettings(Long userId)` - Get or create default settings
- `updateUserSettings(Long userId, UserSettingsDto dto)` - Update settings
- `getDefaultSettings()` - Return default settings object

### 6. Mapper

- **File**: `src/main/java/com/vallexia/user/mapper/UserSettingsMapper.java`
- Map between Entity and DTO

### 7. Controller

- **File**: `src/main/java/com/vallexia/user/controller/UserSettingsController.java`
- Endpoints:
- `GET /api/v1/users/settings` - Get current user settings
- `PUT /api/v1/users/settings` - Update current user settings

### 8. Update User Entity

- Add `@OneToOne` relationship to UserSettings in `User.java`

## Frontend Implementation

### 1. Update ProfileView.vue

- Add "Settings" tab to tabs array (line 340-344)
- Add Settings tab content section (after Nutritional Goals tab, around line 282)
- Create `settingsForm` reactive object with all settings fields
- Create `settingsErrors` reactive object for validation
- Add `isSettingsLoading` ref
- Implement `updateSettings()` method following pattern of other update methods
- Load settings in `onMounted()` hook

### 2. Settings Form UI Components

- Country selector (dropdown with country list)
- Language selector (dropdown, prepare for future i18n)
- Date format selector (radio buttons or dropdown)
- Time format selector (radio buttons: 12h/24h)
- Timezone selector (dropdown with common timezones)
- First day of week selector (radio buttons: Sunday/Monday)
- Measurement system selector (radio buttons: Metric/Imperial)
- Number format inputs (decimal/thousands separators)
- Currency selector (optional, dropdown)

### 3. Update userService.js

- **File**: `src/main/java/com/vallexia/web/src/services/userService.js`
- Add methods:
- `getSettings()` - GET /api/v1/users/settings
- `updateSettings(settingsData)` - PUT /api/v1/users/settings

### 4. Create Formatting Utilities

- **File**: `src/main/java/com/vallexia/web/src/utils/formatUtils.js`
- Functions:
- `formatDate(date, format, locale)` - Format dates based on user settings
- `formatTime(date, format)` - Format time based on user settings
- `formatNumber(number, decimalSep, thousandsSep)` - Format numbers
- `getLocaleFromSettings(settings)` - Get locale string from settings
- Store settings in Pinia store or localStorage for quick access

### 5. Update Components Using Hardcoded Formats

- **File**: `src/main/java/com/vallexia/web/src/components/dashboard/WeeklyMealPlanOverview.vue`
- Replace hardcoded `'en-US'` (line 108) with user settings
- **File**: `src/main/java/com/vallexia/web/src/components/dashboard/TodaysMeals.vue`
- Replace hardcoded `'en-US'` (line 69) with user settings
- **File**: `src/main/java/com/vallexia/web/src/views/ProfileView.vue`
- Update `formatDate()` (line 445-448) to use user settings

### 6. Create Settings Store (Optional)

- **File**: `src/main/java/com/vallexia/web/src/stores/settings.js`
- Pinia store to cache user settings globally
- Load settings on app initialization
- Provide computed properties for formatting functions

## Constants and Enums

### Frontend Constants

- **File**: `src/main/java/com/vallexia/web/src/utils/constants.js`
- Add:
- `DATE_FORMATS` - Available date format options
- `TIME_FORMATS` - 12h/24h options
- `MEASUREMENT_SYSTEMS` - Metric/Imperial
- `FIRST_DAY_OF_WEEK` - Sunday/Monday options
- `SUPPORTED_LANGUAGES` - Language codes
- `COMMON_TIMEZONES` - List of common timezones
- `COUNTRIES` - Country list with codes

## Default Values

- Language: 'en'
- Country: Detect from browser or default to 'US'
- Date Format: 'MM/DD/YYYY' (US default)
- Time Format: '12h' (US default)
- Timezone: Detect from browser or default to 'UTC'
- First Day of Week: 1 (Monday)
- Measurement System: 'METRIC'
- Number separators: '.' (decimal), ',' (thousands)

## Testing Considerations

- Test default settings creation on user registration
- Test settings update API
- Test date/time formatting with different settings
- Test measurement system conversion display
- Test calendar first day of week change

## Future Enhancements

- Internationalization (i18n) implementation using selected language
- Theme/dark mode preference
- Notification preferences
- Email digest preferences