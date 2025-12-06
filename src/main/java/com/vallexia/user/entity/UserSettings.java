package com.vallexia.user.entity;

import com.vallexia.common.enums.SupportedCountry;
import com.vallexia.common.enums.SupportedDateFormat;
import com.vallexia.common.enums.SupportedFirstDayOfWeek;
import com.vallexia.common.enums.SupportedLocale;
import com.vallexia.common.enums.SupportedMeasurementSystem;
import com.vallexia.common.enums.SupportedTimezone;
import com.vallexia.common.validator.ValidCountry;
import com.vallexia.common.validator.ValidCurrency;
import com.vallexia.common.validator.ValidDateFormat;
import com.vallexia.common.validator.ValidLocale;
import com.vallexia.common.validator.ValidTimezone;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * User settings entity storing user's display preferences including localization,
 * date formats, measurement units, and other UI preferences.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-15
 */
@Entity
@Table(name = "user_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @NotNull
    @ValidLocale
    @Size(max = 10)
    @Column(nullable = false, length = 10)
    private String language = SupportedLocale.EN.getCode();
    
    @ValidCountry
    @Size(max = 2)
    @Column(length = 2)
    private String country = SupportedCountry.US.getCountryCode();
    
    @NotNull
    @ValidDateFormat
    @Size(max = 20)
    @Column(name = "date_format", nullable = false, length = 20)
    private String dateFormat = SupportedDateFormat.MM_DD_YYYY.name();
    
    @NotNull
    @ValidTimezone
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String timezone = SupportedTimezone.UTC.getValue();
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "first_day_of_week", nullable = false)
    private SupportedFirstDayOfWeek firstDayOfWeek = SupportedFirstDayOfWeek.MONDAY;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_system", nullable = false, length = 10)
    private SupportedMeasurementSystem measurementSystem = SupportedMeasurementSystem.METRIC;
    
    @NotNull
    @Size(max = 1)
    @Column(name = "number_decimal_separator", nullable = false, length = 1)
    private String numberDecimalSeparator = ".";
    
    @NotNull
    @Size(max = 1)
    @Column(name = "number_thousands_separator", nullable = false, length = 1)
    private String numberThousandsSeparator = ",";
    
    @ValidCurrency
    @Size(max = 3)
    @Column(length = 3)
    private String currency;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
