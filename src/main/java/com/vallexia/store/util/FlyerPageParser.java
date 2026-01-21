package com.vallexia.store.util;

import com.vallexia.store.entity.Store;
import com.vallexia.store.entity.StoreOffer;
import com.vallexia.store.scrape.StoreOfferExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility component for parsing HTML flyer pages and extracting offer data.
 * Delegates to specialized extractors for each store type.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-01-XX
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FlyerPageParser {
    
    private final OfferDataProcessor offerDataProcessor;
    private final List<StoreOfferExtractor> extractors;
    
    /**
     * Extract validity dates from the flyer page.
     * Falls back to calculating Monday-Sunday of current week if dates can't be extracted.
     * 
     * @param doc the HTML document
     * @param store the store entity
     * @return array of [validFrom, validTo] dates
     */
    public LocalDate[] extractValidityDates(Document doc, Store store) {
        // Try specialized extractors first
        for (StoreOfferExtractor extractor : extractors) {
            if (extractor.supports(store)) {
                Optional<LocalDate[]> dates = extractor.extractValidityDates(doc, store);
                if (dates.isPresent()) {
                    return dates.get();
                }
            }
        }
        

        LocalDate today = LocalDate.now();
        
        // Try to extract dates from page (customize based on actual page structure)
        // Look for common patterns like date ranges
        Elements dateElements = doc.select(".validity-dates, .offer-period, .period, .date-range, .valid-from, .valid-to");
        
        if (!dateElements.isEmpty()) {
            String dateText = dateElements.first().text();
            log.debug("Found date text: {} for store: {}", dateText, store != null ? store.getName() : null);
            
            Optional<LocalDate[]> parsedDates = tryParseDates(dateText);
            if (parsedDates.isPresent()) {
                return parsedDates.get();
            }
        }
        
        // Fallback: Calculate Monday-Sunday of current week
        LocalDate weekStart = today;
        while (weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            weekStart = weekStart.minusDays(1);
        }
        LocalDate weekEnd = weekStart.plusDays(6);
        
        log.debug("Using calculated week: {} to {} for store: {}", weekStart, weekEnd,
            store != null ? store.getName() : null);
        return new LocalDate[]{weekStart, weekEnd};
    }
    
    /**
     * Try to parse dates from text.
     * Supports multiple date formats: DD.MM.YYYY, MM/DD/YYYY, YYYY-MM-DD, etc.
     * 
     * @param dateText the text containing date range
     * @return Optional containing array of [startDate, endDate] if parsing succeeds, empty otherwise
     */
    private Optional<LocalDate[]> tryParseDates(String dateText) {
        // Pattern 1: "15.01 - 21.01" or "15.01.2024 - 21.01.2024" (DD.MM format)
        Pattern dateRangePattern1 = Pattern.compile(
            "(\\d{1,2})[\\./](\\d{1,2})(?:[\\./](\\d{4}))?\\s*-\\s*(\\d{1,2})[\\./](\\d{1,2})(?:[\\./](\\d{4}))?");
        Matcher matcher1 = dateRangePattern1.matcher(dateText);
        
        if (matcher1.find()) {
            try {
                int day1 = Integer.parseInt(matcher1.group(1));
                int month1 = Integer.parseInt(matcher1.group(2));
                int year1 = matcher1.group(3) != null ? Integer.parseInt(matcher1.group(3)) : LocalDate.now().getYear();
                
                int day2 = Integer.parseInt(matcher1.group(4));
                int month2 = Integer.parseInt(matcher1.group(5));
                int year2 = matcher1.group(6) != null ? Integer.parseInt(matcher1.group(6)) : LocalDate.now().getYear();
                
                // Try DD.MM format first (European)
                try {
                    LocalDate start = LocalDate.of(year1, month1, day1);
                    LocalDate end = LocalDate.of(year2, month2, day2);
                    return Optional.of(new LocalDate[]{start, end});
                } catch (Exception e) {
                    // If that fails, try MM.DD format (US)
                    try {
                        LocalDate start = LocalDate.of(year1, day1, month1);
                        LocalDate end = LocalDate.of(year2, day2, month2);
                        return Optional.of(new LocalDate[]{start, end});
                    } catch (Exception e2) {
                        log.debug("Failed to parse date range: {}", dateText);
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to parse date range: {}", dateText, e);
            }
        }
        
        // Pattern 2: ISO format "2024-01-15 - 2024-01-21"
        Pattern dateRangePattern2 = Pattern.compile(
            "(\\d{4})-(\\d{1,2})-(\\d{1,2})\\s*-\\s*(\\d{4})-(\\d{1,2})-(\\d{1,2})");
        Matcher matcher2 = dateRangePattern2.matcher(dateText);
        
        if (matcher2.find()) {
            try {
                int year1 = Integer.parseInt(matcher2.group(1));
                int month1 = Integer.parseInt(matcher2.group(2));
                int day1 = Integer.parseInt(matcher2.group(3));
                
                int year2 = Integer.parseInt(matcher2.group(4));
                int month2 = Integer.parseInt(matcher2.group(5));
                int day2 = Integer.parseInt(matcher2.group(6));
                
                LocalDate start = LocalDate.of(year1, month1, day1);
                LocalDate end = LocalDate.of(year2, month2, day2);
                return Optional.of(new LocalDate[]{start, end});
            } catch (Exception e) {
                log.debug("Failed to parse ISO date range: {}", dateText, e);
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Extract offers from the HTML document.
     * Delegates to specialized extractors first, falls back to generic DOM parsing.
     * 
     * @param doc the HTML document
     * @param store the store entity
     * @param validFrom validity start date
     * @param validTo validity end date
     * @return list of extracted offers
     */
    public List<StoreOffer> extractOffers(Document doc, Store store, 
                                          LocalDate validFrom, LocalDate validTo) {
        // Try specialized extractors first
        for (StoreOfferExtractor extractor : extractors) {
            if (extractor.supports(store)) {
                List<StoreOffer> offers = extractor.extractOffers(doc, store, validFrom, validTo);
                if (!offers.isEmpty()) {
                    return offers;
                }
            }
        }
        
        // Fallback to generic DOM parsing
        List<StoreOffer> offers = new ArrayList<>();
        
        // Select offer elements (customize selectors based on actual page structure)
        Elements offerElements = doc.select(".offer-item, .product-card, .offer, .product, [data-product]");
        
        if (offerElements.isEmpty()) {
            log.warn(
                "No offer elements found for store: {}. Page structure may have changed.",
                store.getName());
            return offers;
        }
        
        log.debug("Found {} offer elements for store: {}", offerElements.size(), store.getName());
        
        for (Element element : offerElements) {
            try {
                Optional<StoreOffer> offer = parseOfferElement(element, store, validFrom, validTo);
                offer.ifPresent(offers::add);
            } catch (Exception e) {
                log.warn("Failed to parse offer element for store: {}", store.getName(), e);
            }
        }
        
        return offers;
    }
    
    /**
     * Parse a single offer element into a StoreOffer entity.
     * 
     * @param element the HTML element containing offer data
     * @param store the store entity
     * @param validFrom validity start date
     * @param validTo validity end date
     * @return Optional containing the parsed offer, empty if parsing fails
     */
    private Optional<StoreOffer> parseOfferElement(Element element, Store store, 
                                        LocalDate validFrom, LocalDate validTo) {
        StoreOffer offer = new StoreOffer();
        offer.setStore(store);
        offer.setValidFrom(validFrom);
        offer.setValidTo(validTo);
        offer.setScrapedAt(LocalDateTime.now());
        
        // Extract product name (try multiple selectors)
        Element nameElement = element.selectFirst(".product-name, .offer-title, h3, h4, .title, [data-name]");
        if (nameElement != null) {
            offer.setProductName(nameElement.text().trim());
        } else {
            // Fallback: use text content if no specific name element
            String text = element.text().trim();
            if (!text.isEmpty()) {
                offer.setProductName(text.split("\n")[0].trim()); // First line as name
            } else {
                return Optional.empty(); // Skip if no product name
            }
        }
        
        // Extract price
        Element priceElement = element.selectFirst(".price, .offer-price, .discounted-price, .current-price, [data-price]");
        if (priceElement != null) {
            offer.setRawPriceText(priceElement.text());
            Optional<BigDecimal> price = offerDataProcessor.parsePrice(priceElement.text());
            price.ifPresent(offer::setPrice);
        }

        // We only persist offers that have a real numeric price.
        if (offer.getPrice() == null || offer.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }
        return Optional.of(offer);
    }
}
