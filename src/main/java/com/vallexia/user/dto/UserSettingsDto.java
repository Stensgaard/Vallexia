package com.vallexia.user.dto;

import com.vallexia.common.validator.ValidCountry;
import com.vallexia.common.validator.ValidCurrency;
import com.vallexia.common.validator.ValidDateFormat;
import com.vallexia.common.validator.ValidFirstDayOfWeek;
import com.vallexia.common.validator.ValidLocale;
import com.vallexia.common.validator.ValidMeasurementSystem;
import com.vallexia.common.validator.ValidTimezone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user settings.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-15
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
    
    @NotBlank(message = "Country is required")
    @ValidCountry
    private String country;
    
    @NotBlank(message = "Date format code is required")
    @ValidDateFormat
    @Size(max = 20, message = "Date format code must not exceed 20 characters")
    private String dateFormat;
    
    @NotNull(message = "Timezone is required")
    @ValidTimezone
    private String timezone;
    
    @NotNull(message = "First day of week is required")
    @ValidFirstDayOfWeek
    @Size(max = 10, message = "First day of week code must not exceed 10 characters")
    private String firstDayOfWeek;
    
    @NotNull(message = "Measurement system is required")
    @ValidMeasurementSystem
    @Size(max = 10, message = "Measurement system must not exceed 10 characters")
    private String measurementSystem;
    
    @ValidCurrency
    @Size(max = 3, message = "Currency code must not exceed 3 characters")
    private String currency;
}
