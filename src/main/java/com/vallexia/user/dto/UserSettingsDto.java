package com.vallexia.user.dto;

import com.vallexia.common.validator.ValidDateFormat;
import com.vallexia.common.validator.ValidLocale;
import com.vallexia.common.validator.ValidMeasurementSystem;
import com.vallexia.user.entity.enums.FirstDayOfWeek;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user settings.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsDto {
    
    private Long id;
    
    private Long userId;
    
    @NotNull(message = "Language is required")
    @ValidLocale
    @Size(max = 10, message = "Language code must not exceed 10 characters")
    private String language;
    
    @Size(max = 2, message = "Country code must not exceed 2 characters")
    @Pattern(regexp = "^[A-Z]{2}$|^$", message = "Country code must be a valid ISO 3166-1 alpha-2 code")
    private String country;
    
    @NotNull(message = "Date format is required")
    @ValidDateFormat
    @Size(max = 20, message = "Date format must not exceed 20 characters")
    private String dateFormat;
    
    @NotNull(message = "Timezone is required")
    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone;
    
    @NotNull(message = "First day of week is required")
    private FirstDayOfWeek firstDayOfWeek;
    
    @NotNull(message = "Measurement system is required")
    @ValidMeasurementSystem
    @Size(max = 10, message = "Measurement system must not exceed 10 characters")
    private String measurementSystem;
}
