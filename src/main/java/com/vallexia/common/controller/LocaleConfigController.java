package com.vallexia.common.controller;

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
import com.vallexia.recipe.entity.enums.DifficultyLevel;
import com.vallexia.user.entity.enums.Allergy;
import com.vallexia.user.entity.enums.CuisineType;
import com.vallexia.user.entity.enums.DietaryRestriction;
import com.vallexia.user.entity.enums.GoalType;
import com.vallexia.user.entity.enums.SubscriptionStatus;
import com.vallexia.user.entity.enums.MealType;
import com.vallexia.common.mapper.LocaleConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for locale-related configuration.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/v1/locales")
@Tag(name = "Locale Configuration", description = "Localized configuration such as countries, currencies, timezones, and date formats")
public class LocaleConfigController {

    @GetMapping
    @Operation(summary = "List supported locales")
    public ResponseEntity<List<LocaleDto>> getSupportedLocales() {
        return ResponseEntity.ok(buildLocales());
    }

    @GetMapping("/countries")
    @Operation(summary = "List supported countries")
    public ResponseEntity<List<CountryDto>> getSupportedCountries() {
        return ResponseEntity.ok(buildCountries());
    }

    @GetMapping("/currencies")
    @Operation(summary = "List supported currencies")
    public ResponseEntity<List<CurrencyDto>> getSupportedCurrencies() {
        return ResponseEntity.ok(buildCurrencies());
    }

    @GetMapping("/timezones")
    @Operation(summary = "List supported timezones")
    public ResponseEntity<List<TimezoneDto>> getSupportedTimezones() {
        return ResponseEntity.ok(buildTimezones());
    }

    @GetMapping("/formatting-rules")
    @Operation(summary = "List formatting rules derived from countries")
    public ResponseEntity<List<FormattingRuleDto>> getFormattingRules() {
        return ResponseEntity.ok(buildFormattingRules());
    }

    @GetMapping("/date-formats")
    @Operation(summary = "List supported date formats with tokens")
    public ResponseEntity<List<DateFormatDto>> getDateFormats() {
        return ResponseEntity.ok(buildDateFormats());
    }

    @GetMapping("/measurement-systems")
    @Operation(summary = "List supported measurement systems")
    public ResponseEntity<List<MeasurementSystemDto>> getMeasurementSystems() {
        return ResponseEntity.ok(buildMeasurementSystems());
    }

    @GetMapping("/first-day-of-week")
    @Operation(summary = "List supported first-day-of-week options")
    public ResponseEntity<List<FirstDayOfWeekDto>> getFirstDayOfWeek() {
        return ResponseEntity.ok(buildFirstDayOfWeek());
    }

    @GetMapping("/meal-categories")
    @Operation(summary = "List supported meal/recipe categories")
    public ResponseEntity<List<MealCategoryDto>> getMealCategories() {
        return ResponseEntity.ok(buildMealCategories());
    }

    @GetMapping("/dietary-restrictions")
    @Operation(summary = "List supported dietary restrictions")
    public ResponseEntity<List<DietaryRestrictionDto>> getDietaryRestrictions() {
        return ResponseEntity.ok(buildDietaryRestrictions());
    }

    @GetMapping("/allergies")
    @Operation(summary = "List supported allergies")
    public ResponseEntity<List<AllergyDto>> getAllergies() {
        return ResponseEntity.ok(buildAllergies());
    }

    @GetMapping("/cuisine-types")
    @Operation(summary = "List supported cuisine types")
    public ResponseEntity<List<CuisineTypeDto>> getCuisineTypes() {
        return ResponseEntity.ok(buildCuisineTypes());
    }

    @GetMapping("/difficulty-levels")
    @Operation(summary = "List supported recipe difficulty levels")
    public ResponseEntity<List<DifficultyLevelDto>> getDifficultyLevels() {
        return ResponseEntity.ok(buildDifficultyLevels());
    }

    @GetMapping("/goal-types")
    @Operation(summary = "List supported nutritional goal types")
    public ResponseEntity<List<GoalTypeDto>> getGoalTypes() {
        return ResponseEntity.ok(buildGoalTypes());
    }

    @GetMapping("/subscription-statuses")
    @Operation(summary = "List supported subscription statuses")
    public ResponseEntity<List<SubscriptionStatusDto>> getSubscriptionStatuses() {
        return ResponseEntity.ok(buildSubscriptionStatuses());
    }

    @GetMapping("/meal-types")
    @Operation(summary = "List supported meal types for meal planning")
    public ResponseEntity<List<MealTypeDto>> getMealTypes() {
        return ResponseEntity.ok(buildMealTypes());
    }

    @GetMapping("/config")
    @Operation(summary = "Get the complete locale configuration bundle")
    public ResponseEntity<LocaleConfigDto> getLocaleConfig() {
        LocaleConfigDto config = LocaleConfigMapper.buildConfig(
                buildLocales(),
                buildCountries(),
                buildCurrencies(),
                buildTimezones(),
                buildFormattingRules(),
                buildDateFormats(),
                buildMeasurementSystems(),
                buildWeightUnits(),
                buildVolumeUnits(),
                buildCountUnits(),
                buildFirstDayOfWeek(),
                buildMealCategories(),
                buildDietaryRestrictions(),
                buildAllergies(),
                buildCuisineTypes(),
                buildDifficultyLevels(),
                buildGoalTypes(),
                buildSubscriptionStatuses(),
                buildMealTypes()
        );
        return ResponseEntity.ok(config);
    }

    private List<LocaleDto> buildLocales() {
        return SupportedLocale.getAllCodes().stream()
                .sorted()
                .map(code -> LocaleConfigMapper.toLocaleDto(SupportedLocale.fromCode(code)))
                .collect(Collectors.toList());
    }

    private List<CountryDto> buildCountries() {
        return SupportedCountry.getAll().stream()
                .sorted(Comparator.comparing(SupportedCountry::getCountryCode))
                .map(LocaleConfigMapper::toCountryDto)
                .collect(Collectors.toList());
    }

    private List<CurrencyDto> buildCurrencies() {
        return SupportedCurrency.getAll().stream()
                .sorted(Comparator.comparing(SupportedCurrency::getCode))
                .map(LocaleConfigMapper::toCurrencyDto)
                .collect(Collectors.toList());
    }

    private List<TimezoneDto> buildTimezones() {
        return SupportedTimezone.getAll().stream()
                .map(LocaleConfigMapper::toTimezoneDto)
                .collect(Collectors.toList());
    }

    private List<FormattingRuleDto> buildFormattingRules() {
        return SupportedCountry.getAll().stream()
                .map(LocaleConfigMapper::toFormattingRuleDto)
                .collect(Collectors.toList());
    }

    private List<DateFormatDto> buildDateFormats() {
        return SupportedDateFormat.getAll().stream()
                .map(LocaleConfigMapper::toDateFormatDto)
                .collect(Collectors.toList());
    }

    private List<MeasurementSystemDto> buildMeasurementSystems() {
        return SupportedMeasurementSystem.getAll().stream()
                .map(LocaleConfigMapper::toMeasurementSystemDto)
                .collect(Collectors.toList());
    }

    private List<UnitDto> buildWeightUnits() {
        return SupportedWeightUnit.getAll().stream()
                .map(LocaleConfigMapper::toWeightUnitDto)
                .collect(Collectors.toList());
    }

    private List<UnitDto> buildVolumeUnits() {
        return SupportedVolumeUnit.getAll().stream()
                .map(LocaleConfigMapper::toVolumeUnitDto)
                .collect(Collectors.toList());
    }

    private List<UnitDto> buildCountUnits() {
        return SupportedCountUnit.getAll().stream()
                .map(LocaleConfigMapper::toCountUnitDto)
                .collect(Collectors.toList());
    }

    private List<FirstDayOfWeekDto> buildFirstDayOfWeek() {
        return Arrays.stream(SupportedFirstDayOfWeek.values())
                .map(LocaleConfigMapper::toFirstDayOfWeekDto)
                .collect(Collectors.toList());
    }

    private List<MealCategoryDto> buildMealCategories() {
        return SupportedMealCategory.getAll().stream()
                .map(LocaleConfigMapper::toMealCategoryDto)
                .collect(Collectors.toList());
    }

    private List<DietaryRestrictionDto> buildDietaryRestrictions() {
        return Arrays.stream(DietaryRestriction.values())
                .map(LocaleConfigMapper::toDietaryRestrictionDto)
                .collect(Collectors.toList());
    }

    private List<AllergyDto> buildAllergies() {
        return Arrays.stream(Allergy.values())
                .map(LocaleConfigMapper::toAllergyDto)
                .collect(Collectors.toList());
    }

    private List<CuisineTypeDto> buildCuisineTypes() {
        return Arrays.stream(CuisineType.values())
                .map(LocaleConfigMapper::toCuisineTypeDto)
                .collect(Collectors.toList());
    }

    private List<DifficultyLevelDto> buildDifficultyLevels() {
        return Arrays.stream(DifficultyLevel.values())
                .map(LocaleConfigMapper::toDifficultyLevelDto)
                .collect(Collectors.toList());
    }

    private List<GoalTypeDto> buildGoalTypes() {
        return Arrays.stream(GoalType.values())
                .map(LocaleConfigMapper::toGoalTypeDto)
                .collect(Collectors.toList());
    }

    private List<SubscriptionStatusDto> buildSubscriptionStatuses() {
        return Arrays.stream(SubscriptionStatus.values())
                .map(LocaleConfigMapper::toSubscriptionStatusDto)
                .collect(Collectors.toList());
    }

    private List<MealTypeDto> buildMealTypes() {
        return Arrays.stream(MealType.values())
                .map(LocaleConfigMapper::toMealTypeDto)
                .collect(Collectors.toList());
    }
}
