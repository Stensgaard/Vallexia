package com.vallexia.store.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Utility component for processing offer data (price parsing).
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-01-XX
 */
@Component
@Slf4j
public class OfferDataProcessor {
    
    /**
     * Parse price from text.
     * Handles multiple number formats:
     * - Comma as decimal separator (European): "29,95" -> 29.95
     * - Dot as decimal separator (US/UK): "29.95" -> 29.95
     * - Thousands separators: "1.299,95" or "1,299.95"
     * 
     * @param priceText the price text to parse
     * @return Optional containing the parsed price, empty if parsing fails
     */
    public Optional<BigDecimal> parsePrice(String priceText) {
        if (priceText == null || priceText.trim().isEmpty()) {
            return Optional.empty();
        }
        
        // Remove currency symbols, whitespace, and common text
        String cleaned = priceText.replaceAll("[^0-9,.-]", "").trim();
        
        if (cleaned.isEmpty()) {
            return Optional.empty();
        }
        
        // Detect format: if last comma/dot is followed by 2 digits, it's likely a decimal separator
        // Otherwise, it might be a thousands separator
        int lastCommaIndex = cleaned.lastIndexOf(',');
        int lastDotIndex = cleaned.lastIndexOf('.');
        int lastSeparatorIndex = Math.max(lastCommaIndex, lastDotIndex);
        
        if (lastSeparatorIndex > 0 && lastSeparatorIndex < cleaned.length() - 3) {
            // Has thousands separator, need to handle both formats
            if (lastCommaIndex > lastDotIndex) {
                // European format: comma is decimal, dot is thousands (e.g., "1.299,95")
                cleaned = cleaned.replace(".", "").replace(",", ".");
            } else {
                // US format: dot is decimal, comma is thousands (e.g., "1,299.95")
                cleaned = cleaned.replace(",", "");
            }
        } else if (lastSeparatorIndex > 0) {
            // Likely decimal separator only
            if (lastCommaIndex > lastDotIndex) {
                // Comma is decimal separator
                cleaned = cleaned.replace(",", ".");
            }
            // If dot is last, it's already in correct format
        }
        
        try {
            return Optional.of(new BigDecimal(cleaned));
        } catch (NumberFormatException e) {
            log.warn("Failed to parse price: {}", priceText);
            return Optional.empty();
        }
    }
}

