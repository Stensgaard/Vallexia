package com.vallexia.common.controller;

import com.vallexia.common.dto.*;
import com.vallexia.common.service.LocaleConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * REST controller for locale-related configuration.
 * 
 * <p>This controller delegates all locale configuration operations to {@link LocaleConfigService}.
 *
 * <p>To expose a new configuration resource:
 * <ol>
 *   <li>Add or update the relevant enum/DTO under {@code com.vallexia.common}</li>
 *   <li>Add a mapper method to the appropriate mapper ({@link com.vallexia.common.mapper.LocaleMapper}, {@link com.vallexia.common.mapper.UnitMapper}, or {@link com.vallexia.common.mapper.DomainEnumMapper})</li>
 *   <li>Add a {@code getXYZ()} method in {@link LocaleConfigService}</li>
 *   <li>Add a matching {@code @GetMapping} endpoint in this controller that delegates to the service</li>
 *   <li>Wire the service method into {@link LocaleConfigService#buildLocaleConfigSnapshot()} so /config stays in sync</li>
 * </ol>
 *
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-11-20
 */
@RestController
@RequestMapping("/api/v1/locales")
@Tag(name = "Locale Configuration", description = "Localized configuration such as countries, currencies, timezones, and date formats")
public class LocaleConfigController {

    private static final CacheControl LOCALE_CACHE_CONTROL =
            CacheControl.maxAge(6, TimeUnit.HOURS).cachePublic();

    private final LocaleConfigService localeConfigService;
    private final LocaleConfigDto cachedConfig;

    /**
     * Constructor for dependency injection.
     * 
     * @param localeConfigService the locale configuration service
     */
    public LocaleConfigController(LocaleConfigService localeConfigService) {
        this.localeConfigService = localeConfigService;
        this.cachedConfig = localeConfigService.buildLocaleConfigSnapshot();
    }

    @GetMapping
    @Operation(summary = "List supported locales", description = "Returns the complete list of locale codes accepted by UserSettingsDto.language and other endpoints using @ValidLocale")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported locales retrieved successfully")
    })
    public ResponseEntity<List<LocaleDto>> getSupportedLocales() {
        return ResponseEntity.ok(localeConfigService.getSupportedLocales());
    }

    @GetMapping("/countries")
    @Operation(summary = "List supported countries", description = "Returns the complete list of country codes accepted by UserSettingsDto.country, RegisterRequestDto.country and other endpoints using @ValidCountry")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported countries retrieved successfully")
    })
    public ResponseEntity<List<CountryDto>> getSupportedCountries() {
        return ResponseEntity.ok(localeConfigService.getSupportedCountries());
    }

    @GetMapping("/currencies")
    @Operation(summary = "List supported currencies", description = "Returns the complete list of currency codes accepted by UserSettingsDto.currency and other endpoints using @ValidCurrency")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported currencies retrieved successfully")
    })
    public ResponseEntity<List<CurrencyDto>> getSupportedCurrencies() {
        return ResponseEntity.ok(localeConfigService.getSupportedCurrencies());
    }

    @GetMapping("/timezones")
    @Operation(summary = "List supported timezones", description = "Returns the complete list of timezone identifiers accepted by UserSettingsDto.timezone and other endpoints using @ValidTimezone")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported timezones retrieved successfully")
    })
    public ResponseEntity<List<TimezoneDto>> getSupportedTimezones() {
        return ResponseEntity.ok(localeConfigService.getSupportedTimezones());
    }

    @GetMapping("/formatting-rules")
    @Operation(summary = "List formatting rules derived from countries", description = "Returns the complete list of formatting rules derived from countries")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Formatting rules retrieved successfully")
    })
    public ResponseEntity<List<FormattingRuleDto>> getFormattingRules() {
        return ResponseEntity.ok(localeConfigService.getFormattingRules());
    }

    @GetMapping("/date-formats")
    @Operation(summary = "List supported date formats with tokens", description = "Returns the complete list of date format codes accepted by UserSettingsDto.dateFormat and other endpoints using @ValidDateFormat")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported date formats retrieved successfully")
    })
    public ResponseEntity<List<DateFormatDto>> getDateFormats() {
        return ResponseEntity.ok(localeConfigService.getSupportedDateFormats());
    }

    @GetMapping("/measurement-systems")
    @Operation(summary = "List supported measurement systems", description = "Returns the complete list of measurement system codes accepted by UserSettingsDto.measurementSystem and other endpoints using @ValidMeasurementSystem")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported measurement systems retrieved successfully")
    })
    public ResponseEntity<List<MeasurementSystemDto>> getMeasurementSystems() {
        return ResponseEntity.ok(localeConfigService.getSupportedMeasurementSystems());
    }

    @GetMapping("/first-day-of-week")
    @Operation(summary = "List supported first-day-of-week options", description = "Returns the complete list of first day of week codes accepted by UserSettingsDto.firstDayOfWeek and other endpoints using @ValidFirstDayOfWeek")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported first day of week options retrieved successfully")
    })
    public ResponseEntity<List<FirstDayOfWeekDto>> getFirstDayOfWeek() {
        return ResponseEntity.ok(localeConfigService.getFirstDayOfWeek());
    }

    @GetMapping("/meal-categories")
    @Operation(summary = "List supported meal/recipe categories", description = "Returns the complete list of meal category codes accepted by endpoints using @ValidMealCategory")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported meal categories retrieved successfully")
    })
    public ResponseEntity<List<MealCategoryDto>> getMealCategories() {
        return ResponseEntity.ok(localeConfigService.getMealCategories());
    }

    @GetMapping("/dietary-restrictions")
    @Operation(summary = "List supported dietary restrictions", description = "Returns the complete list of dietary restriction codes available for use in recipes and user preferences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported dietary restrictions retrieved successfully")
    })
    public ResponseEntity<List<DietaryRestrictionDto>> getDietaryRestrictions() {
        return ResponseEntity.ok(localeConfigService.getDietaryRestrictions());
    }

    @GetMapping("/allergies")
    @Operation(summary = "List supported allergies", description = "Returns the complete list of allergy codes available for use in recipes and user preferences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported allergies retrieved successfully")
    })
    public ResponseEntity<List<AllergyDto>> getAllergies() {
        return ResponseEntity.ok(localeConfigService.getAllergies());
    }

    @GetMapping("/cuisine-types")
    @Operation(summary = "List supported cuisine types", description = "Returns the complete list of cuisine type codes available for use in recipes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported cuisine types retrieved successfully")
    })
    public ResponseEntity<List<CuisineTypeDto>> getCuisineTypes() {
        return ResponseEntity.ok(localeConfigService.getCuisineTypes());
    }

    @GetMapping("/goal-types")
    @Operation(summary = "List supported nutritional goal types", description = "Returns the complete list of goal type codes available for use in nutritional goals")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported goal types retrieved successfully")
    })
    public ResponseEntity<List<GoalTypeDto>> getGoalTypes() {
        return ResponseEntity.ok(localeConfigService.getGoalTypes());
    }

    @GetMapping("/subscription-statuses")
    @Operation(summary = "List supported subscription statuses", description = "Returns the complete list of subscription status codes accepted by JwtResponseDto.subscriptionStatus, UserProfileDto.subscriptionStatus and other endpoints using @ValidSubscriptionStatus")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported subscription statuses retrieved successfully")
    })
    public ResponseEntity<List<SubscriptionStatusDto>> getSubscriptionStatuses() {
        return ResponseEntity.ok(localeConfigService.getSubscriptionStatuses());
    }

    @GetMapping("/config")
    @Operation(summary = "Get the complete locale configuration bundle", description = "Returns the complete locale configuration bundle")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Locale configuration bundle retrieved successfully")
    })
    public ResponseEntity<LocaleConfigDto> getLocaleConfig() {
        return ResponseEntity.ok()
                .cacheControl(LOCALE_CACHE_CONTROL)
                .body(cachedConfig);
    }
}
