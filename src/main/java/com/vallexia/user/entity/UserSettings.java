package com.vallexia.user.entity;

import com.vallexia.common.validator.ValidLocale;
import com.vallexia.user.entity.enums.FirstDayOfWeek;
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
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
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
    private String language = "en";
    
    @Size(max = 2)
    @Column(length = 2)
    private String country;
    
    @NotNull
    @Size(max = 20)
    @Column(name = "date_format", nullable = false, length = 20)
    private String dateFormat = "MM/DD/YYYY";
    
    @NotNull
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String timezone = "UTC";
    
    @NotNull
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "first_day_of_week", nullable = false)
    private FirstDayOfWeek firstDayOfWeek = FirstDayOfWeek.MONDAY;
    
    @NotNull
    @Size(max = 10)
    @Column(name = "measurement_system", nullable = false, length = 10)
    private String measurementSystem = "METRIC";
    
    @NotNull
    @Size(max = 1)
    @Column(name = "number_decimal_separator", nullable = false, length = 1)
    private String numberDecimalSeparator = ".";
    
    @NotNull
    @Size(max = 1)
    @Column(name = "number_thousands_separator", nullable = false, length = 1)
    private String numberThousandsSeparator = ",";
    
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
