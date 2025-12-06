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
import com.vallexia.common.enums.SupportedAllergy;
import com.vallexia.common.enums.SupportedCuisineType;
import com.vallexia.common.enums.SupportedDietaryRestriction;
import com.vallexia.nutrition.enums.GoalType;
import com.vallexia.user.entity.enums.SubscriptionStatus;
import com.vallexia.common.mapper.DomainEnumMapper;
import com.vallexia.common.mapper.LocaleConfigBuilder;
import com.vallexia.common.mapper.LocaleMapper;
import com.vallexia.common.mapper.UnitMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * REST controller for locale-related configuration.
 *
 * <p>To expose a new configuration resource:
 * <ol>
 *   <li>Add or update the relevant enum/DTO under {@code com.vallexia.common}</li>
 *   <li>Add a mapper method to the appropriate mapper ({@link LocaleMapper}, {@link UnitMapper}, or {@link DomainEnumMapper})</li>
 *   <li>Add a {@code buildXYZ()} helper and matching {@code @GetMapping} in this controller</li>
 *   <li>Wire the builder into {@link #buildLocaleConfigSnapshot()} so /config stays in sync</li>
 * </ol>
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-20
 */
@RestController
@RequestMapping("/api/v1/locales")
@Tag(
    name = "Locale Configuration", 
    description = "Localized configuration such as countries, currencies, timezones, " +
        "and date formats")
public class LocaleConfigController {

    private static final CacheControl LOCALE_CACHE_CONTROL =
            CacheControl.maxAge(6, TimeUnit.HOURS).cachePublic();

    private final LocaleConfigDto cachedConfig;

    public LocaleConfigController() {
        this.cachedConfig = buildLocaleConfigSnapshot();
    }

    @GetMapping
    @Operation(
        summary = "List supported locales", 
        description = "Returns the complete list of locale codes accepted by UserSettingsDto.language" + 
            "and other endpoints using @ValidLocale")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported locales retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<LocaleDto>> getSupportedLocales() {
        return ResponseEntity.ok(buildLocales());
    }

    @GetMapping("/countries")
    @Operation(
        summary = "List supported countries", 
        description = "Returns the complete list of country codes accepted by UserSettingsDto.country," + 
            "RegisterRequestDto.country and other endpoints using @ValidCountry")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported countries retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CountryDto>> getSupportedCountries() {
        return ResponseEntity.ok(buildCountries());
    }

    @GetMapping("/currencies")
    @Operation(
        summary = "List supported currencies",  
        description = "Returns the complete list of currency codes accepted by UserSettingsDto.currency" + 
            " and other endpoints using @ValidCurrency")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported currencies retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CurrencyDto>> getSupportedCurrencies() {
        return ResponseEntity.ok(buildCurrencies());
    }

    @GetMapping("/timezones")
    @Operation(
        summary = "List supported timezones", 
        description = "Returns the complete list of timezone identifiers accepted by" + 
            "UserSettingsDto.timezone and other endpoints using @ValidTimezone")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported timezones retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<TimezoneDto>> getSupportedTimezones() {
        return ResponseEntity.ok(buildTimezones());
    }

    @GetMapping("/formatting-rules")
    @Operation(
        summary = "List formatting rules derived from countries", 
        description = "Returns the complete list of formatting rules derived from countries")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Formatting rules retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<FormattingRuleDto>> getFormattingRules() {
        return ResponseEntity.ok(buildFormattingRules());
    }

    @GetMapping("/date-formats")
    @Operation(
        summary = "List supported date formats with tokens", 
        description = "Returns the complete list of date format codes accepted by" + 
            "UserSettingsDto.dateFormat and other endpoints using @ValidDateFormat")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported date formats retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<DateFormatDto>> getDateFormats() {
        return ResponseEntity.ok(buildDateFormats());
    }

    @GetMapping("/measurement-systems")
    @Operation(
        summary = "List supported measurement systems", 
        description = "Returns the complete list of measurement system codes" + 
            "accepted by UserSettingsDto.measurementSystem and other endpoints" + 
            "using @ValidMeasurementSystem")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported measurement systems retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<MeasurementSystemDto>> getMeasurementSystems() {
        return ResponseEntity.ok(buildMeasurementSystems());
    }

    @GetMapping("/first-day-of-week")
    @Operation(
        summary = "List supported first-day-of-week options", 
        description = "Returns the complete list of first day of week codes accepted by" +  
        "UserSettingsDto.firstDayOfWeek and other endpoints using @ValidFirstDayOfWeek")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported first day of week options retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<FirstDayOfWeekDto>> getFirstDayOfWeek() {
        return ResponseEntity.ok(buildFirstDayOfWeek());
    }

    @GetMapping("/meal-categories")
    @Operation(
        summary = "List supported meal/recipe categories", 
        description = "Returns the complete list of meal category codes accepted by" + 
            "CreateRecipeDto.category, UpdateRecipeDto.category, RecipeSearchCriteria.category and" + 
            "other endpoints using @ValidMealCategory")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported meal categories retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<MealCategoryDto>> getMealCategories() {
        return ResponseEntity.ok(buildMealCategories());
    }

    @GetMapping("/dietary-restrictions")
    @Operation(
        summary = "List supported dietary restrictions", 
        description = "Returns the complete list of dietary restriction codes available for use" + 
            "in recipes and user preferences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported dietary restrictions retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<DietaryRestrictionDto>> getDietaryRestrictions() {
        return ResponseEntity.ok(buildDietaryRestrictions());
    }

    @GetMapping("/allergies")
    @Operation(
        summary = "List supported allergies", 
        description = "Returns the complete list of allergy codes available for use in" + 
        "recipes and user preferences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported allergies retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<AllergyDto>> getAllergies() {
        return ResponseEntity.ok(buildAllergies());
    }

    @GetMapping("/cuisine-types")
    @Operation(
        summary = "List supported cuisine types", 
        description = "Returns the complete list of cuisine type codes available for use in recipes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported cuisine types retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CuisineTypeDto>> getCuisineTypes() {
        return ResponseEntity.ok(buildCuisineTypes());
    }

    @GetMapping("/difficulty-levels")
    @Operation(
        summary = "List supported recipe difficulty levels", 
        description = "Returns the complete list of difficulty level codes available for use in recipes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported difficulty levels retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<DifficultyLevelDto>> getDifficultyLevels() {
        return ResponseEntity.ok(buildDifficultyLevels());
    }

    @GetMapping("/goal-types")
    @Operation(
        summary = "List supported nutritional goal types", 
        description = "Returns the complete list of goal type codes available for use in nutritional goals")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported goal types retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<GoalTypeDto>> getGoalTypes() {
        return ResponseEntity.ok(buildGoalTypes());
    }

    @GetMapping("/subscription-statuses")
    @Operation(
        summary = "List supported subscription statuses", 
        description = "Returns the complete list of subscription status codes accepted by" + 
            "JwtResponseDto.subscriptionStatus, UserProfileDto.subscriptionStatus and other" + 
            "endpoints using @ValidSubscriptionStatus")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported subscription statuses retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<SubscriptionStatusDto>> getSubscriptionStatuses() {
        return ResponseEntity.ok(buildSubscriptionStatuses());
    }

    @GetMapping("/config")
    @Operation(
        summary = "Get the complete locale configuration bundle", 
        description = "Returns the complete locale configuration bundle")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Locale configuration bundle retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<LocaleConfigDto> getLocaleConfig() {
        return ResponseEntity.ok()
                .cacheControl(LOCALE_CACHE_CONTROL)
                .body(cachedConfig);
    }

    private LocaleConfigDto buildLocaleConfigSnapshot() {
        return LocaleConfigBuilder.buildConfig(
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
                buildSubscriptionStatuses()
        );
    }

    private List<LocaleDto> buildLocales() {
        return SupportedLocale.getAll().stream()
                .sorted(Comparator.comparing(SupportedLocale::getCode))
                .map(LocaleMapper::toLocaleDto)
                .collect(Collectors.toList());
    }

    private List<CountryDto> buildCountries() {
        return SupportedCountry.getAll().stream()
                .sorted(Comparator.comparing(SupportedCountry::getCountryCode))
                .map(LocaleMapper::toCountryDto)
                .collect(Collectors.toList());
    }

    private List<CurrencyDto> buildCurrencies() {
        return SupportedCurrency.getAll().stream()
                .sorted(Comparator.comparing(SupportedCurrency::getCode))
                .map(LocaleMapper::toCurrencyDto)
                .collect(Collectors.toList());
    }

    private List<TimezoneDto> buildTimezones() {
        return SupportedTimezone.getAll().stream()
                .map(LocaleMapper::toTimezoneDto)
                .collect(Collectors.toList());
    }

    private List<FormattingRuleDto> buildFormattingRules() {
        return SupportedCountry.getAll().stream()
                .map(LocaleMapper::toFormattingRuleDto)
                .collect(Collectors.toList());
    }

    private List<DateFormatDto> buildDateFormats() {
        return SupportedDateFormat.getAll().stream()
                .map(LocaleMapper::toDateFormatDto)
                .collect(Collectors.toList());
    }

    private List<MeasurementSystemDto> buildMeasurementSystems() {
        return SupportedMeasurementSystem.getAll().stream()
                .map(LocaleMapper::toMeasurementSystemDto)
                .collect(Collectors.toList());
    }

    private List<UnitDto> buildWeightUnits() {
        return SupportedWeightUnit.getAll().stream()
                .map(UnitMapper::toWeightUnitDto)
                .collect(Collectors.toList());
    }

    private List<UnitDto> buildVolumeUnits() {
        return SupportedVolumeUnit.getAll().stream()
                .map(UnitMapper::toVolumeUnitDto)
                .collect(Collectors.toList());
    }

    private List<UnitDto> buildCountUnits() {
        return SupportedCountUnit.getAll().stream()
                .map(UnitMapper::toCountUnitDto)
                .collect(Collectors.toList());
    }

    private List<FirstDayOfWeekDto> buildFirstDayOfWeek() {
        return SupportedFirstDayOfWeek.getAll().stream()
                .map(LocaleMapper::toFirstDayOfWeekDto)
                .collect(Collectors.toList());
    }

    private List<MealCategoryDto> buildMealCategories() {
        return SupportedMealCategory.getAll().stream()
                .map(LocaleMapper::toMealCategoryDto)
                .collect(Collectors.toList());
    }

    private List<DietaryRestrictionDto> buildDietaryRestrictions() {
        return SupportedDietaryRestriction.getAll().stream()
                .map(DomainEnumMapper::toDietaryRestrictionDto)
                .collect(Collectors.toList());
    }

    private List<AllergyDto> buildAllergies() {
        return SupportedAllergy.getAll().stream()
                .map(DomainEnumMapper::toAllergyDto)
                .collect(Collectors.toList());
    }

    private List<CuisineTypeDto> buildCuisineTypes() {
        return SupportedCuisineType.getAll().stream()
                .map(DomainEnumMapper::toCuisineTypeDto)
                .collect(Collectors.toList());
    }

    private List<DifficultyLevelDto> buildDifficultyLevels() {
        return Arrays.stream(DifficultyLevel.values())
                .map(DomainEnumMapper::toDifficultyLevelDto)
                .collect(Collectors.toList());
    }

    private List<GoalTypeDto> buildGoalTypes() {
        return Arrays.stream(GoalType.values())
                .map(DomainEnumMapper::toGoalTypeDto)
                .collect(Collectors.toList());
    }

    private List<SubscriptionStatusDto> buildSubscriptionStatuses() {
        return Arrays.stream(SubscriptionStatus.values())
                .map(DomainEnumMapper::toSubscriptionStatusDto)
                .collect(Collectors.toList());
    }

}
