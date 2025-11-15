package com.vallexia.common.controller;

import com.vallexia.common.enums.SupportedLocale;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for locale-related endpoints.
 * Provides API access to supported locales for frontend discovery.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/v1/locales")
public class LocaleController {
    
    /**
     * Get all supported locales.
     * 
     * @return list of supported locale information
     */
    @GetMapping
    public ResponseEntity<List<LocaleDto>> getSupportedLocales() {
        List<LocaleDto> locales = SupportedLocale.getAllCodes().stream()
                .sorted()
                .map(code -> {
                    SupportedLocale locale = SupportedLocale.fromCode(code);
                    return new LocaleDto(code, locale.name());
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(locales);
    }
    
    /**
     * Data Transfer Object for locale information.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocaleDto {
        private String code;
        private String name;
    }
}
