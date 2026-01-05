package com.vallexia.common.service;

import com.vallexia.common.dto.*;
import com.vallexia.common.enums.SupportedFirstDayOfWeek;
import com.vallexia.common.enums.SupportedCountUnit;
import com.vallexia.common.enums.SupportedCountry;
import com.vallexia.common.enums.SupportedCurrency;
import com.vallexia.common.enums.SupportedDateFormat;
import com.vallexia.common.enums.SupportedLocale;
import com.vallexia.common.enums.SupportedMealCategory;
import com.vallexia.common.enums.SupportedMeasurementSystem;
import com.vallexia.common.enums.SupportedTimezone;
import com.vallexia.common.enums.SupportedVolumeUnit;
import com.vallexia.common.enums.SupportedWeightUnit;
import com.vallexia.common.enums.SupportedAllergy;
import com.vallexia.common.enums.SupportedCuisineType;
import com.vallexia.common.enums.SupportedDietaryRestriction;
import com.vallexia.nutrition.enums.GoalType;
import com.vallexia.user.entity.enums.SubscriptionStatus;
import com.vallexia.common.mapper.DomainEnumMapper;
import com.vallexia.common.mapper.LocaleConfigBuilder;
import com.vallexia.common.mapper.LocaleConfigInput;
import com.vallexia.common.mapper.LocaleMapper;
import com.vallexia.common.mapper.UnitMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for locale-related configuration operations.
 * 
 * <p>This service provides methods to retrieve and build locale configuration DTOs
 * from supported enums. It handles the transformation of enums to DTOs using the
 * appropriate mappers ({@link LocaleMapper}, {@link UnitMapper}, {@link DomainEnumMapper}).
 * 
 * <p>To expose a new configuration resource:
 * <ol>
 *   <li>Add or update the relevant enum/DTO under {@code com.vallexia.common}</li>
 *   <li>Add a mapper method to the appropriate mapper ({@link LocaleMapper}, {@link UnitMapper}, or {@link DomainEnumMapper})</li>
 *   <li>Add a {@code getXYZ()} method in this service</li>
 *   <li>Wire the method into {@link #buildLocaleConfigSnapshot()} so /config stays in sync</li>
 * </ol>
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-08
 */
@Service
@Transactional(readOnly = true)
public class LocaleConfigService {

    /**
     * Builds a complete locale configuration snapshot containing all locale-related configuration.
     * 
     * @return the complete locale configuration DTO bundle
     */
    public LocaleConfigDto buildLocaleConfigSnapshot() {
        LocaleConfigInput input = LocaleConfigInput.builder()
                .locales(getSupportedLocales())
                .countries(getSupportedCountries())
                .currencies(getSupportedCurrencies())
                .timezones(getSupportedTimezones())
                .formattingRules(getFormattingRules())
                .dateFormats(getSupportedDateFormats())
                .measurementSystems(getSupportedMeasurementSystems())
                .weightUnits(getWeightUnits())
                .volumeUnits(getVolumeUnits())
                .countUnits(getCountUnits())
                .firstDayOfWeek(getFirstDayOfWeek())
                .mealCategories(getMealCategories())
                .dietaryRestrictions(getDietaryRestrictions())
                .allergies(getAllergies())
                .cuisineTypes(getCuisineTypes())
                .goalTypes(getGoalTypes())
                .subscriptionStatuses(getSubscriptionStatuses())
                .build();
        return LocaleConfigBuilder.buildConfig(input);
    }

    /**
     * Retrieves all supported locales sorted by code.
     * 
     * @return list of locale DTOs
     */
    public List<LocaleDto> getSupportedLocales() {
        return SupportedLocale.getAll().stream()
                .sorted(Comparator.comparing(SupportedLocale::getCode))
                .map(LocaleMapper::toLocaleDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all supported countries sorted by country code.
     * 
     * @return list of country DTOs
     */
    public List<CountryDto> getSupportedCountries() {
        return SupportedCountry.getAll().stream()
                .sorted(Comparator.comparing(SupportedCountry::getCountryCode))
                .map(LocaleMapper::toCountryDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all supported currencies sorted by code.
     * 
     * @return list of currency DTOs
     */
    public List<CurrencyDto> getSupportedCurrencies() {
        return SupportedCurrency.getAll().stream()
                .sorted(Comparator.comparing(SupportedCurrency::getCode))
                .map(LocaleMapper::toCurrencyDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all supported timezones.
     * 
     * @return list of timezone DTOs
     */
    public List<TimezoneDto> getSupportedTimezones() {
        return SupportedTimezone.getAll().stream()
                .map(LocaleMapper::toTimezoneDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all formatting rules derived from countries.
     * 
     * @return list of formatting rule DTOs
     */
    public List<FormattingRuleDto> getFormattingRules() {
        return SupportedCountry.getAll().stream()
                .map(LocaleMapper::toFormattingRuleDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all supported date formats.
     * 
     * @return list of date format DTOs
     */
    public List<DateFormatDto> getSupportedDateFormats() {
        return SupportedDateFormat.getAll().stream()
                .map(LocaleMapper::toDateFormatDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all supported measurement systems.
     * 
     * @return list of measurement system DTOs
     */
    public List<MeasurementSystemDto> getSupportedMeasurementSystems() {
        return SupportedMeasurementSystem.getAll().stream()
                .map(LocaleMapper::toMeasurementSystemDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all supported weight units.
     * 
     * @return list of weight unit DTOs
     */
    public List<UnitDto> getWeightUnits() {
        return SupportedWeightUnit.getAll().stream()
                .map(UnitMapper::toWeightUnitDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all supported volume units.
     * 
     * @return list of volume unit DTOs
     */
    public List<UnitDto> getVolumeUnits() {
        return SupportedVolumeUnit.getAll().stream()
                .map(UnitMapper::toVolumeUnitDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all supported count units.
     * 
     * @return list of count unit DTOs
     */
    public List<UnitDto> getCountUnits() {
        return SupportedCountUnit.getAll().stream()
                .map(UnitMapper::toCountUnitDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all supported first day of week options.
     * 
     * @return list of first day of week DTOs
     */
    public List<FirstDayOfWeekDto> getFirstDayOfWeek() {
        return SupportedFirstDayOfWeek.getAll().stream()
                .map(LocaleMapper::toFirstDayOfWeekDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all supported meal categories.
     * 
     * @return list of meal category DTOs
     */
    public List<MealCategoryDto> getMealCategories() {
        return SupportedMealCategory.getAll().stream()
                .map(LocaleMapper::toMealCategoryDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all supported dietary restrictions.
     * 
     * @return list of dietary restriction DTOs
     */
    public List<DietaryRestrictionDto> getDietaryRestrictions() {
        return SupportedDietaryRestriction.getAll().stream()
                .map(DomainEnumMapper::toDietaryRestrictionDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all supported allergies.
     * 
     * @return list of allergy DTOs
     */
    public List<AllergyDto> getAllergies() {
        return SupportedAllergy.getAll().stream()
                .map(DomainEnumMapper::toAllergyDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all supported cuisine types.
     * 
     * @return list of cuisine type DTOs
     */
    public List<CuisineTypeDto> getCuisineTypes() {
        return SupportedCuisineType.getAll().stream()
                .map(DomainEnumMapper::toCuisineTypeDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all supported goal types.
     * 
     * @return list of goal type DTOs
     */
    public List<GoalTypeDto> getGoalTypes() {
        return Arrays.stream(GoalType.values())
                .map(DomainEnumMapper::toGoalTypeDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all supported subscription statuses.
     * 
     * @return list of subscription status DTOs
     */
    public List<SubscriptionStatusDto> getSubscriptionStatuses() {
        return Arrays.stream(SubscriptionStatus.values())
                .map(DomainEnumMapper::toSubscriptionStatusDto)
                .collect(Collectors.toList());
    }
}
