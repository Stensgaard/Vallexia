package com.vallexia.store.util;

import com.vallexia.store.entity.Store;
import com.vallexia.store.entity.StoreOffer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Utility component for parsing HTML flyer pages and extracting offer data.
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
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern IPAPER_PRICE_MARKER =
        Pattern.compile("\\b\\d{1,4}(?:[\\.,]\\d{1,2})?\\s*(?:kr\\b|,-|\\.-)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern TJEK_CATALOG_ID_PATTERN =
        Pattern.compile("squid-api\\.tjek\\.com/v2/catalogs/([A-Za-z0-9_-]+)/download",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern TJEK_CATALOG_ID_PATTERN_ESCAPED =
        Pattern.compile("squid-api\\\\.tjek\\\\.com\\\\/v2\\\\/catalogs\\\\/([A-Za-z0-9_-]+)\\\\/download",
            Pattern.CASE_INSENSITIVE);
    private static final DateTimeFormatter TJEK_OFFSET_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    private static final ZoneId DEFAULT_TJEK_ZONE = ZoneId.of("Europe/Copenhagen");

    private static final int MAX_OFFERS_FROM_IPAPER_OCR = 1200;
    private static final Pattern LEADING_CODE_PATTERN = Pattern.compile("^\\d{5,}\\s+");
    
    /**
     * Extract validity dates from the flyer page.
     * Falls back to calculating Monday-Sunday of current week if dates can't be extracted.
     * 
     * @param doc the HTML document
     * @param store the store entity
     * @return array of [validFrom, validTo] dates
     */
    public LocalDate[] extractValidityDates(Document doc, Store store) {
        // NETTO uses Tjek "squid-api" catalogs with explicit run_from/run_till timestamps.
        if ("NETTO".equals(store != null ? store.getName() : null)) {
            try {
                String catalogId = extractTjekCatalogId(doc);
                if (catalogId != null) {
                    LocalDate[] dates = fetchTjekCatalogValidity(catalogId);
                    if (dates != null) {
                        return dates;
                    }
                }
            } catch (Exception ignored) {}
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
     * 
     * @param doc the HTML document
     * @param store the store entity
     * @param validFrom validity start date
     * @param validTo validity end date
     * @return list of extracted offers
     */
    public List<StoreOffer> extractOffers(Document doc, Store store, 
                                          LocalDate validFrom, LocalDate validTo) {
        List<StoreOffer> offers = new ArrayList<>();
        
        // Select offer elements (customize selectors based on actual page structure)
        Elements offerElements = doc.select(".offer-item, .product-card, .offer, .product, [data-product]");
        
        if (offerElements.isEmpty()) {
            log.warn("No offer elements found for store: {}. Attempting to extract from embedded JSON data.", store.getName());
            
            // Try to extract from embedded JSON data
            List<StoreOffer> jsonOffers = extractOffersFromEmbeddedJson(doc, store, validFrom, validTo);
            if (!jsonOffers.isEmpty()) {
                log.info("Extracted {} offers from embedded JSON for store: {}", jsonOffers.size(), store.getName());
                return jsonOffers;
            }
            
            log.warn("Could not extract offers from embedded JSON for store: {}. Page structure may have changed.", store.getName());
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
            Optional<BigDecimal> price = offerDataProcessor.parsePrice(priceElement.text());
            price.ifPresent(offer::setPrice);
        }

        // We only persist offers that have a real numeric price.
        if (offer.getPrice() == null || offer.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }
        return Optional.of(offer);
    }
    
    /**
     * Extract offers from embedded JSON data in script tags.
     * This is a fallback when DOM selectors fail (e.g., for JavaScript-rendered pages).
     * 
     * @param doc the HTML document
     * @param store the store entity
     * @param validFrom validity start date
     * @param validTo validity end date
     * @return list of extracted offers
     */
    private List<StoreOffer> extractOffersFromEmbeddedJson(Document doc, Store store, 
                                                           LocalDate validFrom, LocalDate validTo) {
        List<StoreOffer> offers = new ArrayList<>();
        Elements scriptTags = doc.select("script");

        // NETTO: Prefer Tjek squid-api offers (full offer set). The embedded Next.js offerItem list is only a preview.
        if ("NETTO".equals(store != null ? store.getName() : null)) {
            try {
                String catalogId = extractTjekCatalogId(doc);
                if (catalogId != null) {
                    List<StoreOffer> tjekOffers = fetchNettoOffersFromTjek(catalogId, store, validFrom, validTo);
                    if (!tjekOffers.isEmpty()) {
                        return tjekOffers;
                    }
                }
            } catch (Exception ignored) {}
        }
        
        for (Element script : scriptTags) {
            String scriptText = script.html();
            if (scriptText == null || scriptText.trim().isEmpty()) {
                continue;
            }
            
            try {
                // Try to extract JSON from different patterns
                // Pattern 1: window.staticSettings = {...} (BILKA)
                if (scriptText.contains("window.staticSettings")) {
                    String jsonStr = extractJsObjectAssignment(scriptText, "window.staticSettings");
                    if (jsonStr != null && !jsonStr.isBlank()) {
                        JsonNode jsonNode = objectMapper.readTree(jsonStr);
                        // Extract offers from iPaper structure (BILKA + FOETEX)
                        offers.addAll(parseBilkaJsonData(jsonNode, store, validFrom, validTo));
                    }
                }
                
                // Pattern 2: window.__NUXT__ = {...} (FOETEX)
                if (scriptText.contains("window.__NUXT__")) {
                    Pattern pattern = Pattern.compile("window\\.__NUXT__\\s*=\\s*function.*?return\\s*(\\{.*?\\})", Pattern.DOTALL);
                    Matcher matcher = pattern.matcher(scriptText);
                    if (matcher.find()) {
                        String jsonStr = matcher.group(1);
                        JsonNode jsonNode = objectMapper.readTree(jsonStr);
                        // Extract offers from FOETEX structure
                        offers.addAll(parseFoetexJsonData(jsonNode, store, validFrom, validTo));
                    }
                }
                
                // Pattern 3: self.__next_f.push([...]) (NETTO)
                // Process ALL push calls in the script, not just the first one
                if (scriptText.contains("self.__next_f.push")) {
                    // Next.js data is more complex - try to extract product data
                    offers.addAll(parseNettoJsonData(scriptText, store, validFrom, validTo));
                }
                
            } catch (Exception e) {
                log.debug("Failed to parse JSON from script tag for store: {}", store != null ? store.getName() : null,
                    e);
            }
        }
        
        // For NETTO, if no offers found in individual script tags, search the entire document HTML
        // (the offerItem data might be in a very large script tag that spans the entire document)
        if ("NETTO".equals(store != null ? store.getName() : null) && offers.isEmpty() && doc.html().contains("offerItem")) {
            log.debug("No offers found in individual script tags for NETTO, searching entire document HTML");
            offers.addAll(parseNettoJsonData(doc.html(), store, validFrom, validTo));
        }

        // For iPaper-backed flyers (BILKA/FOETEX), enrichments JSON often contains only hotspots/links.
        // As a last resort, parse offers from meta descriptions (OpenGraph description often includes
        // a few offer text snippets with prices like "20.-" on page 1).
        if (("BILKA".equals(store != null ? store.getName() : null) || "FOETEX".equals(store != null ? store.getName() : null))
            && offers.isEmpty()) {
            offers.addAll(extractOffersFromIpaperMetaDescriptions(doc, store, validFrom, validTo));
        }
        
        return offers;
    }

    private List<StoreOffer> extractOffersFromIpaperMetaDescriptions(Document doc, Store store,
                                                                     LocalDate validFrom, LocalDate validTo) {
        List<StoreOffer> offers = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        List<String> candidates = new ArrayList<>();
        Elements meta = doc.select("meta[name=description], meta[property=og:description]");
        for (Element m : meta) {
            String content = m.attr("content");
            if (content != null && !content.isBlank()) {
                candidates.add(content);
            }
        }

        Pattern pricePattern = Pattern.compile("\\b(\\d{1,4})(?:[\\.,](\\d{1,2}))?\\s*(?:kr\\b|,-|\\.-)",
            Pattern.CASE_INSENSITIVE);
        // We only persist offers that contain an actual numeric price.

        for (String raw : candidates) {
            for (String part : raw.split("\\s*\\|\\s*")) {
                String text = part.replace("\n", " ").replace("\r", " ").replaceAll("\\s+", " ").trim();
                if (text.isBlank()) {
                    continue;
                }

                Matcher pm = pricePattern.matcher(text);
                if (pm.find()) {
                    String whole = pm.group(1);
                    String frac = pm.group(2);
                    BigDecimal price = new BigDecimal(whole + "." + (frac != null ? frac : "00"));
                    String name = text.substring(0, pm.start()).trim();
                    if (name.length() < 3) {
                        continue;
                    }
                    String key = ("p|" + name + "|" + price.toPlainString()).toLowerCase();
                    if (seen.add(key)) {
                        StoreOffer offer = new StoreOffer();
                        offer.setStore(store);
                        offer.setValidFrom(validFrom);
                        offer.setValidTo(validTo);
                        offer.setScrapedAt(LocalDateTime.now());
                        offer.setProductName(name);
                        offer.setPrice(price);
                        offers.add(offer);
                    }
                    continue;
                }
            }
        }

        return offers;
    }

    /**
     * Extract a JavaScript object literal assigned to a variable/expression like:
     * window.staticSettings = { ... };
     *
     * @param scriptText full script contents
     * @param lhs left-hand side expression to locate (e.g., "window.staticSettings")
     * @return the JSON object literal string (starting with '{' and ending with matching '}'), or null
     */
    private String extractJsObjectAssignment(String scriptText, String lhs) {
        if (scriptText == null || lhs == null) {
            return null;
        }

        int lhsIdx = scriptText.indexOf(lhs);
        if (lhsIdx < 0) {
            return null;
        }

        int eqIdx = scriptText.indexOf("=", lhsIdx);
        if (eqIdx < 0) {
            return null;
        }

        int startBrace = scriptText.indexOf("{", eqIdx);
        if (startBrace < 0) {
            return null;
        }

        int braceCount = 1;
        int pos = startBrace + 1;
        boolean inString = false;
        boolean escaped = false;

        while (pos < scriptText.length() && braceCount > 0) {
            char c = scriptText.charAt(pos);

            if (escaped) {
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"' && !escaped) {
                inString = !inString;
            } else if (!inString) {
                if (c == '{') {
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                }
            }
            pos++;
        }

        if (braceCount != 0) {
            return null;
        }

        return scriptText.substring(startBrace, pos);
    }
    
    /**
     * Parse BILKA JSON data structure.
     */
    private List<StoreOffer> parseBilkaJsonData(JsonNode jsonNode, Store store, 
                                               LocalDate validFrom, LocalDate validTo) {
        return parseIpaperStaticSettings(jsonNode, store, validFrom, validTo);
    }
    
    /**
     * Parse FOETEX JSON data structure (Nuxt.js).
     */
    private List<StoreOffer> parseFoetexJsonData(JsonNode jsonNode, Store store, 
                                                LocalDate validFrom, LocalDate validTo) {
        // FOETEX flyer pages are also iPaper (window.staticSettings)
        return parseIpaperStaticSettings(jsonNode, store, validFrom, validTo);
    }

    /**
     * Parse iPaper "window.staticSettings" data structure.
     * Both BILKA and FOETEX flyer pages are hosted on iPaper and require fetching enrichments JSON.
     */
    private List<StoreOffer> parseIpaperStaticSettings(JsonNode jsonNode, Store store,
                                                       LocalDate validFrom, LocalDate validTo) {
        List<StoreOffer> offers = new ArrayList<>();

        try {
            // First attempt: iPaper "pageTexts" contains OCR-like text per page with prices.
            List<StoreOffer> pageTextOffers = extractOffersFromIpaperPageTexts(jsonNode, store, validFrom, validTo);
            if (!pageTextOffers.isEmpty()) {
                return pageTextOffers;
            }

            JsonNode chunkUrls = (jsonNode == null ? objectMapper.createObjectNode() : jsonNode)
                .path("enrichments")
                .path("chunkUrls");
            if (chunkUrls == null || chunkUrls.isMissingNode() || chunkUrls.properties().isEmpty()) {
                return offers;
            }

            // Pick the first chunk URL (typically "1-47" -> Page1-47.json?...)
            String chunkKey = chunkUrls.properties().iterator().next().getKey();
            String enrichmentsUrl = chunkUrls.path(chunkKey).asText(null);
            if (enrichmentsUrl == null || enrichmentsUrl.isBlank()) {
                return offers;
            }

            String jsonText = org.jsoup.Jsoup.connect(enrichmentsUrl)
                .ignoreContentType(true)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(30000)
                .execute()
                .body();

            JsonNode enrichmentsJson = objectMapper.readTree(jsonText);
            List<String> snippets = new ArrayList<>();
            Map<String, Object> scan = new HashMap<>();
            scanIpaperEnrichments(enrichmentsJson, scan, snippets);

            offers.addAll(extractOffersFromIpaperSnippets(snippets, store, validFrom, validTo));
        } catch (Exception e) {
            log.debug("Failed to parse iPaper staticSettings for store: {}", store.getName(), e);
        }

        return offers;
    }

    private List<StoreOffer> extractOffersFromIpaperPageTexts(JsonNode staticSettingsNode, Store store,
                                                            LocalDate validFrom, LocalDate validTo) {
        // Prefer a "code segmented" strategy: split OCR text by product codes and extract the first
        // price marker inside each segment. This avoids many "meta"/marketing fragments that appear
        // around the page-level price markers.
        List<StoreOffer> offers = extractOffersFromIpaperPageTextsByCodes(staticSettingsNode, store, validFrom, validTo);
        if (!offers.isEmpty()) {
            return offers;
        }

        // Fallback: older price-marker scanning (kept for safety if a flyer has no codes at all).
        List<StoreOffer> offersFallback = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        JsonNode root = staticSettingsNode == null ? objectMapper.createObjectNode() : staticSettingsNode;
        JsonNode pageTexts = root.path("pageTexts");
        if (pageTexts == null || !pageTexts.isArray() || pageTexts.isEmpty()) {
            return offersFallback;
        }

        Pattern pricePattern =
            Pattern.compile("\\b(\\d{1,4})(?:[\\.,](\\d{1,2}))?\\s*(?:kr\\b|,-|\\.-)",
                Pattern.CASE_INSENSITIVE);

        for (JsonNode pageTextNode : pageTexts) {
            if (offers.size() >= MAX_OFFERS_FROM_IPAPER_OCR) {
                break;
            }
            if (pageTextNode == null || !pageTextNode.isTextual()) {
                continue;
            }

            String raw = pageTextNode.asText("");
            if (raw.isBlank()) {
                continue;
            }

            String text = raw.replace("\n", " ").replace("\r", " ").replaceAll("\\s+", " ").trim();
            if (text.length() < 10) {
                continue;
            }

            Matcher m = pricePattern.matcher(text);
            int lastMatchEnd = 0;
            while (m.find() && offers.size() < MAX_OFFERS_FROM_IPAPER_OCR) {
                int priceStart = m.start();
                int priceEnd = m.end();

                BigDecimal price = new BigDecimal(m.group(1) + "." + (m.group(2) != null ? m.group(2) : "00"));

                int windowStart = Math.max(lastMatchEnd, priceStart - 200);
                int boundary = findLastBoundary(text, windowStart, priceStart);
                if (boundary >= 0) {
                    windowStart = boundary + 1;
                }

                int windowEnd = Math.min(text.length(), priceEnd + 60);
                String segment = text.substring(windowStart, windowEnd).trim();
                String nameBefore = text.substring(windowStart, priceStart).trim();

                nameBefore = LEADING_CODE_PATTERN.matcher(nameBefore).replaceFirst("");
                nameBefore = cleanupOcrName(nameBefore);

                String nameAfter = deriveIpaperNameAfter(text, priceEnd);
                String nameByCode = deriveIpaperNameByProductCode(text, priceStart, priceEnd);

                boolean beforeBad = isBadIpaperName(nameBefore);
                boolean afterBad = isBadIpaperName(nameAfter);
                boolean codeBad = isBadIpaperName(nameByCode);

                String namePart = nameBefore;

                // Prefer code-anchored name when present; this is the most reliable on iPaper OCR.
                if (!codeBad) {
                    namePart = nameByCode;
                } else if (!afterBad && (beforeBad || nameBefore.length() < 6)) {
                    namePart = nameAfter;
                }

                if (namePart.length() < 3) {
                    lastMatchEnd = priceEnd;
                    continue;
                }

                if (isBadIpaperName(namePart)) {
                    lastMatchEnd = priceEnd;
                    continue;
                }

                if (!isLikelyFoodText(namePart + " " + segment)) {
                    lastMatchEnd = priceEnd;
                    continue;
                }

                String key = (namePart + "|" + price.toPlainString()).toLowerCase();
                if (!seen.add(key)) {
                    lastMatchEnd = priceEnd;
                    continue;
                }

                StoreOffer offer = new StoreOffer();
                offer.setStore(store);
                offer.setValidFrom(validFrom);
                offer.setValidTo(validTo);
                offer.setScrapedAt(LocalDateTime.now());
                offer.setProductName(namePart);
                offer.setPrice(price);
                offersFallback.add(offer);

                lastMatchEnd = priceEnd;
            }
        }

        return offersFallback;
    }

    private List<StoreOffer> extractOffersFromIpaperPageTextsByCodes(JsonNode staticSettingsNode, Store store,
                                                                    LocalDate validFrom, LocalDate validTo) {
        List<StoreOffer> offers = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        JsonNode root = staticSettingsNode == null ? objectMapper.createObjectNode() : staticSettingsNode;
        JsonNode pageTexts = root.path("pageTexts");
        if (pageTexts == null || !pageTexts.isArray() || pageTexts.isEmpty()) {
            return offers;
        }

        Pattern codePattern = Pattern.compile("\\b\\d{5,}\\b");
        Pattern pricePattern =
            Pattern.compile("\\b(\\d{1,4})(?:[\\.,](\\d{1,2}))?\\s*(?:kr\\b|,-|\\.-)",
                Pattern.CASE_INSENSITIVE);

        for (JsonNode pageTextNode : pageTexts) {
            if (offers.size() >= MAX_OFFERS_FROM_IPAPER_OCR) {
                break;
            }
            if (pageTextNode == null || !pageTextNode.isTextual()) {
                continue;
            }

            String raw = pageTextNode.asText("");
            if (raw.isBlank()) {
                continue;
            }

            String text = raw.replace("\n", " ").replace("\r", " ").replaceAll("\\s+", " ").trim();
            if (text.length() < 20) {
                continue;
            }

            List<int[]> codes = new ArrayList<>();
            Matcher cm = codePattern.matcher(text);
            while (cm.find()) {
                codes.add(new int[]{cm.start(), cm.end()});
                if (codes.size() >= 200) {
                    break; // avoid pathological OCR strings
                }
            }

            for (int i = 0; i < codes.size() && offers.size() < MAX_OFFERS_FROM_IPAPER_OCR; i++) {
                int segStart = codes.get(i)[1];
                int segEnd = (i + 1 < codes.size()) ? codes.get(i + 1)[0] : Math.min(text.length(), segStart + 320);
                if (segEnd <= segStart) {
                    continue;
                }
                String segment = text.substring(segStart, segEnd).trim();
                if (segment.length() < 8) {
                    continue;
                }

                Matcher pm = pricePattern.matcher(segment);
                if (!pm.find()) {
                    continue;
                }

                BigDecimal price = new BigDecimal(pm.group(1) + "." + (pm.group(2) != null ? pm.group(2) : "00"));
                String beforePrice = segment.substring(0, pm.start()).trim();
                beforePrice = cutAtStopTokens(beforePrice);
                String nameRaw = extractIpaperNameFromCodeSegment(beforePrice);
                nameRaw = normalizeIpaperExtractedName(nameRaw);

                if (isBadIpaperName(nameRaw) || nameRaw.length() < 3) {
                    continue;
                }

                String offerText = segment.substring(0, Math.min(segment.length(), pm.end() + 80)).trim();
                if (looksLikeStoreInfo(nameRaw + " " + offerText)) {
                    continue;
                }
                if (!isLikelyFoodText(nameRaw + " " + offerText)) {
                    continue;
                }

                String key = (nameRaw + "|" + price.toPlainString()).toLowerCase();
                if (!seen.add(key)) {
                    continue;
                }

                StoreOffer offer = new StoreOffer();
                offer.setStore(store);
                offer.setValidFrom(validFrom);
                offer.setValidTo(validTo);
                offer.setScrapedAt(LocalDateTime.now());
                offer.setProductName(nameRaw);
                offer.setPrice(price);
                offers.add(offer);
            }
        }

        return offers;
    }

    private String extractIpaperNameFromCodeSegment(String beforePrice) {
        if (beforePrice == null) {
            return "";
        }

        String text = beforePrice
            .replace('\u00A0', ' ')
            .replaceAll("\\s+", " ")
            .trim();
        if (text.isBlank()) {
            return "";
        }

        String lower = text.toLowerCase();
        // Drop obvious promo/meta segments that OCR frequently includes as "products".
        if (lower.contains("søndag") || lower.contains("kosøndag")) {
            return "";
        }
        // If the "name" starts with a size/qty, it's not a product name.
        if (lower.matches("^\\d+(?:[\\.,]\\d+)?\\s*(kg|g|cl|ml|l|stk|st\\.)\\b.*")) {
            return "";
        }
        // Store info / opening hours / service center copy.
        if (looksLikeStoreInfo(lower)) {
            return "";
        }

        // Remove leading fragments that are frequently not part of the actual product name.
        text = text.replaceAll("(?i)^\\s*(op\\s+til\\s+\\d+(?:[\\.,]\\d+)?)\\s*", "").trim();

        // Prefer capturing the product name before a qty/weight indicator.
        Pattern[] patterns = new Pattern[]{
            Pattern.compile("^(.+?)\\s+\\d+\\s*[-–]\\s*\\d+\\s*(kg|g|cl|ml|l)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^(.+?)\\s+\\d+(?:[\\.,]\\d+)?\\s*(kg|g|cl|ml|l)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^(.+?)\\s+\\d+\\s*(stk|st\\.|pcs?)\\b", Pattern.CASE_INSENSITIVE)
        };
        for (Pattern p : patterns) {
            Matcher m = p.matcher(text);
            if (m.find()) {
                String candidate = m.group(1).trim();
                candidate = candidate.replaceAll("[\\.,;:]+$", "").trim();
                if (candidate.length() >= 3) {
                    return candidate;
                }
            }
        }

        // If there's a "Pr." segment, take the part before it.
        int prIdx = text.toLowerCase().indexOf(" pr.");
        if (prIdx > 3) {
            text = text.substring(0, prIdx).trim();
        }

        // If we still have long text, keep the first ~10 words (code-segment usually starts with the product name).
        String[] parts = text.split("\\s+");
        if (parts.length > 12) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 12; i++) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(parts[i]);
            }
            text = sb.toString().trim();
        }

        // Last resort: token-based extraction to skip OCR boilerplate like "PR. PAKKE OP TIL ...".
        String tokenBased = extractNameFromTokens(text);
        if (!tokenBased.isBlank()) {
            return tokenBased;
        }

        return text.replaceAll("[\\.,;:]+$", "").trim();
    }

    private String extractNameFromTokens(String text) {
        if (text == null) {
            return "";
        }
        String[] tokens = text.replaceAll("\\s+", " ").trim().split("\\s+");
        if (tokens.length == 0) {
            return "";
        }

        // Stopwords / boilerplate around iPaper OCR.
        Set<String> stop = Set.of(
            "pr", "pr.", "kg", "g", "stk", "stk.", "st", "st.", "pose", "pakke", "bakke",
            "op", "til", "max", "side", "frit", "rit", "valg", "plus", "pris", "spar"
        );

        int startIdx = -1;
        for (int i = 0; i < tokens.length; i++) {
            String raw = tokens[i];
            String cleaned = raw.replaceAll("[^\\p{L}0-9-]", "").toLowerCase();
            if (cleaned.isBlank()) {
                continue;
            }
            if (stop.contains(cleaned)) {
                continue;
            }
            String lettersOnly = cleaned.replaceAll("[^\\p{L}]+", "");
            if (lettersOnly.isBlank()) {
                continue;
            }
            // Skip very short OCR fragments (e.g. "p0r0", "rit") and only start when we have
            // enough letters to be a plausible product word.
            if (lettersOnly.length() >= 3) {
                startIdx = i;
                break;
            }
            if (lettersOnly.length() == 2 && i + 1 < tokens.length) {
                String next = tokens[i + 1].replaceAll("[^\\p{L}0-9-]", "").toLowerCase();
                String nextLetters = next.replaceAll("[^\\p{L}]+", "");
                if (nextLetters.length() >= 3) {
                    startIdx = i;
                    break;
                }
            }
        }
        if (startIdx < 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int words = 0;
        for (int i = startIdx; i < tokens.length && words < 10; i++) {
            String raw = tokens[i];
            String cleaned = raw.replaceAll("[^\\p{L}0-9-]", "").toLowerCase();
            if (cleaned.isBlank()) {
                continue;
            }
            if (stop.contains(cleaned)) {
                break;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(raw.replaceAll("[\\.,;:]+$", ""));
            words++;
        }

        return sb.toString().replaceAll("[\\.,;:]+$", "").trim();
    }

    private String deriveIpaperNameAfter(String fullText, int priceEnd) {
        if (fullText == null) {
            return "";
        }
        int start = Math.min(Math.max(priceEnd, 0), fullText.length());
        int end = Math.min(fullText.length(), start + 260);
        String after = fullText.substring(start, end);
        after = after.replace("\n", " ").replace("\r", " ").replaceAll("\\s+", " ").trim();

        // Remove common unit/price boilerplate that sits between the deal price and the product name.
        after = after.replaceAll("(?i)^\\s*(pr\\.?\\s*)?(kg|stk|st\\.|liter|l|ml)\\b", "").trim();
        after = after.replaceAll("(?i)^\\s*(pr\\.?\\s*)?(kg|stk|st\\.|liter|l|ml)\\b", "").trim();
        after = after.replaceAll("(?i)^\\s*\\d+(?:[\\.,]\\d+)?\\b", "").trim(); // e.g. "68,85"
        after = after.replaceAll("(?i)^\\s*fr(it)?\\s+valg\\b", "").trim();

        // Prefer name after a leading product code if present.
        Matcher codeMatcher = LEADING_CODE_PATTERN.matcher(after);
        if (codeMatcher.find()) {
            after = after.substring(codeMatcher.end()).trim();
        } else {
            // Otherwise, if a code exists later, jump to it.
            Matcher anyCode = Pattern.compile("\\b\\d{5,}\\b").matcher(after);
            if (anyCode.find()) {
                after = after.substring(anyCode.end()).trim();
            }
        }

        // Cut at next price marker, if any.
        Matcher nextPrice = IPAPER_PRICE_MARKER.matcher(after);
        if (nextPrice.find()) {
            after = after.substring(0, nextPrice.start()).trim();
        }

        return cleanupOcrName(after);
    }

    private boolean isBadIpaperName(String name) {
        if (name == null) {
            return true;
        }
        String n = normalizeOcrNameForValidation(name);
        if (n.isBlank()) {
            return true;
        }
        String lower = n.toLowerCase();
        String compact = lower.replaceAll("[^a-z0-9]+", "");
        String lettersOnly = lower.replaceAll("[^\\p{L}]+", "");
        // Obvious non-name tokens commonly seen in iPaper OCR around prices.
        if (lower.startsWith("kg") || lower.startsWith("pr.") || lower.startsWith("pr ")) {
            return true;
        }
        // Reject names that start with a number + unit (usually just the pack size).
        if (lower.matches("^\\d+(?:[\\.,]\\d+)?\\s*(kg|g|cl|ml|l|stk|st\\.)\\b.*")) {
            return true;
        }
        if (lower.contains("frit valg") || compact.contains("fritvalg")) {
            return true;
        }
        if (lettersOnly.equals("frit") || lettersOnly.equals("rit")) {
            return true;
        }
        // OCR noise token often seen in Bilka promos: "P 0 R 0 ..."
        if (compact.contains("p0r0")) {
            return true;
        }

        // Reject obvious generic filler names.
        if (lower.equals("plus pris") || lower.equals("pris") || lower.equals("normalpris") || lower.equals("spar")) {
            return true;
        }
        if (lower.equals("flere varianter") || lower.equals("flere varianter.")) {
            return true;
        }

        // Must contain at least one letter to be a plausible product name.
        if (!n.matches(".*\\p{L}.*")) {
            return true;
        }

        // Reject short unit/quantity-only phrases like "2 STK." or "550-700 g." or "70 cl."
        if (lower.matches("^\\d+\\s*(stk\\.?|st\\.?|pcs?\\b|g\\b|kg\\b|cl\\b|ml\\b|l\\b)\\.?$")) {
            return true;
        }
        if (lower.matches("^\\d+(?:[\\.,]\\d+)?\\s*(g|kg|cl|ml|l)\\.?$")) {
            return true;
        }
        if (lower.matches("^\\d+\\s*[-–]\\s*\\d+\\s*(g|kg|cl|ml|l)\\.?$")) {
            return true;
        }
        if (lower.matches("^\\d+\\s*(stk\\.?|st\\.?|pcs?)\\s*$")) {
            return true;
        }
        if (lower.matches("^\\d+\\s*(x|pak(?:ke)?r?)\\b.*")) {
            return true;
        }

        // Packaging/unit-only tokens frequently mis-identified as names.
        String[] unitish = new String[]{
            "bakke", "pose", "pakke", "stk", "stk.", "kg", "g", "l", "ml", "pr", "pr.", "max.", "op til"
        };
        if (n.length() <= 8) {
            for (String u : unitish) {
                if (lower.equals(u) || lower.startsWith(u + " ")) {
                    return true;
                }
            }
        }

        // Common OCR "meta"/marketing copy that should never become a product name.
        String[] meta = new String[]{
            "priserne gælder", "vi tager forbehold", "trykfejl", "bilka marketing", "læs mere",
            "gælder kun", "gælder fra", "app", "plus appen", "side ", "avisen", "kundeservice"
        };
        for (String k : meta) {
            if (lower.contains(k)) {
                return true;
            }
        }
        if (looksLikeStoreInfo(lower)) {
            return true;
        }
        // Promo boilerplate often parsed as "name" (OCR-split).
        if (compact.contains("optil") || compact.contains("kosondag") || compact.contains("sondag")
            || lower.contains("søndag") || lower.contains("kosøndag")) {
            return true;
        }
        // Purely numeric-ish or units-ish.
        if (n.matches("^[0-9\\s\\.,-]+$")) {
            return true;
        }
        if (n.matches("^(?i)(kg|stk|st\\.|liter|l|ml)\\b.*")) {
            return true;
        }
        return false;
    }

    private boolean looksLikeStoreInfo(String text) {
        if (text == null) {
            return false;
        }
        String t = text.toLowerCase();
        String compact = t.replaceAll("[^a-z0-9æøå]+", "");
        // Also create an ASCII-ish version because OCR often drops Danish diacritics.
        String ascii = compact
            .replace("æ", "ae")
            .replace("ø", "oe")
            .replace("å", "aa");

        // Danish store info patterns seen in iPaper OCR.
        if (t.contains("åbent alle dage")
            || t.contains("abent alle dage")
            || t.contains("åbner kl")
            || t.contains("abner kl")
            || t.contains("åbningstider")
            || t.contains("abningstider")
            || t.contains("servicecenter")
            || t.contains("service center")
            || t.contains("servicecentret")
            || t.contains("servicecentrets")
            || t.contains("bageren")
            || t.contains("bilka.dk")
            || t.contains("bilka dk")
            || compact.contains("bilkadkservicecenter")
            || ascii.contains("bilkadkservicecenter")
            || compact.contains("bilkadk")
            || ascii.contains("bilkadk")) {
            return true;
        }

        // Generic "open hours" patterns.
        if (t.matches(".*\\b\\d{1,2}\\s*[-–]\\s*\\d{1,2}\\b.*")
            && (t.contains("åbent") || t.contains("abent") || t.contains("open"))) {
            return true;
        }

        // Even if OCR drops the "open" keyword, opening hours + bakery/servicecenter tokens are strong signals.
        if (t.matches(".*\\b\\d{1,2}\\s*[-–]\\s*\\d{1,2}\\b.*")
            && (t.contains("bageren") || t.contains("servicecenter") || t.contains("bilka.dk") || t.contains("bilka dk"))) {
            return true;
        }

        return false;
    }

    private String normalizeIpaperExtractedName(String s) {
        if (s == null) {
            return "";
        }
        String n = s.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
        String lower = n.toLowerCase();

        // If OCR starts with an isolated unit token like "g." then drop it.
        if (lower.startsWith("g. ")) {
            n = n.substring(3).trim();
            lower = n.toLowerCase();
        }
        if (lower.startsWith("kg ")) {
            n = n.substring(3).trim();
            lower = n.toLowerCase();
        }

        // Strip trailing "Flere varianter" if the name has meaningful content.
        n = n.replaceAll("(?i)\\s+flere\\s+varianter\\.?$", "").trim();
        return n;
    }

    private String normalizeOcrNameForValidation(String s) {
        if (s == null) {
            return "";
        }
        String n = s
            .replace('\u00A0', ' ') // NBSP
            .replaceAll("\\s+", " ")
            .trim();
        // Drop trailing punctuation noise.
        n = n.replaceAll("[\\.,;:]+$", "").trim();
        return n;
    }

    private String deriveIpaperNameByProductCode(String fullText, int priceStart, int priceEnd) {
        if (fullText == null) {
            return "";
        }
        String text = fullText.replace("\n", " ").replace("\r", " ").replaceAll("\\s+", " ").trim();
        if (text.isBlank()) {
            return "";
        }

        // Prefer a product code shortly AFTER the price (common pattern: "... 179.- ... 10755335 Kyllinge...")
        int afterStart = Math.min(Math.max(priceEnd, 0), text.length());
        int afterEnd = Math.min(text.length(), afterStart + 220);
        String afterWindow = text.substring(afterStart, afterEnd);

        Matcher afterCode = Pattern.compile("\\b\\d{5,}\\b").matcher(afterWindow);
        if (afterCode.find()) {
            int codeAbsEnd = afterStart + afterCode.end();
            String candidate = text.substring(codeAbsEnd, Math.min(text.length(), codeAbsEnd + 180)).trim();
            candidate = cleanupOcrName(candidate);
            candidate = cutAtStopTokens(candidate);
            return cleanupOcrName(candidate);
        }

        // Otherwise, look for a product code shortly BEFORE the price and take the text between code and price.
        int beforeEnd = Math.min(Math.max(priceStart, 0), text.length());
        int beforeStart = Math.max(0, beforeEnd - 220);
        String beforeWindow = text.substring(beforeStart, beforeEnd);
        Matcher beforeCode = Pattern.compile("\\b\\d{5,}\\b").matcher(beforeWindow);
        int lastCodeEnd = -1;
        while (beforeCode.find()) {
            lastCodeEnd = beforeStart + beforeCode.end();
        }
        if (lastCodeEnd >= 0 && lastCodeEnd < beforeEnd) {
            String candidate = text.substring(lastCodeEnd, beforeEnd).trim();
            candidate = cleanupOcrName(candidate);
            candidate = cutAtStopTokens(candidate);
            return cleanupOcrName(candidate);
        }

        return "";
    }

    private String cutAtStopTokens(String s) {
        if (s == null) {
            return "";
        }
        String text = s.replaceAll("\\s+", " ").trim();
        if (text.isBlank()) {
            return text;
        }

        // Stop when we hit another price marker, another long code, or obvious metadata tokens.
        int cut = text.length();
        Matcher mPrice = IPAPER_PRICE_MARKER.matcher(text);
        if (mPrice.find()) {
            cut = Math.min(cut, mPrice.start());
        }
        Matcher mCode = Pattern.compile("\\b\\d{5,}\\b").matcher(text);
        if (mCode.find()) {
            cut = Math.min(cut, mCode.start());
        }
        int idxPr = text.toLowerCase().indexOf(" pr.");
        if (idxPr >= 0) {
            cut = Math.min(cut, idxPr);
        }
        int idxSide = text.toLowerCase().indexOf(" side ");
        if (idxSide >= 0) {
            cut = Math.min(cut, idxSide);
        }
        int idxAvi = text.toLowerCase().indexOf(" avisen");
        if (idxAvi >= 0) {
            cut = Math.min(cut, idxAvi);
        }

        text = text.substring(0, Math.max(0, cut)).trim();
        return text;
    }

    private int findLastBoundary(String text, int fromInclusive, int toExclusive) {
        if (text == null) {
            return -1;
        }
        int from = Math.max(0, fromInclusive);
        int to = Math.min(text.length(), toExclusive);
        if (from >= to) {
            return -1;
        }

        int idx = text.lastIndexOf("  ", to);
        if (idx >= from) {
            return idx;
        }
        idx = text.lastIndexOf(". ", to);
        if (idx >= from) {
            return idx;
        }
        idx = text.lastIndexOf(" - ", to);
        if (idx >= from) {
            return idx;
        }
        return -1;
    }

    private String cleanupOcrName(String namePart) {
        if (namePart == null) {
            return "";
        }
        String name = namePart.replaceAll("\\s+", " ").trim();
        // Drop obvious OCR fragments / navigation tokens.
        name = name.replaceAll("(?i)\\b(side)\\s*\\d+\\b", "").trim();
        // Keep name length reasonable: take the last ~10 words which usually contains the product.
        String[] parts = name.split("\\s+");
        if (parts.length > 10) {
            StringBuilder sb = new StringBuilder();
            for (int i = parts.length - 10; i < parts.length; i++) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(parts[i]);
            }
            name = sb.toString().trim();
        }
        return name;
    }

    private boolean isLikelyFoodText(String text) {
        if (text == null) {
            return false;
        }
        String t = text.toLowerCase();

        // Hard reject: typical non-food categories/keywords seen in flyer text.
        String[] nonFood = new String[]{
            "rengøring", "rengørings", "vask", "vaskemiddel", "affaldsposer", "køkkenrulle", "toiletpapir",
            "serviet", "lys", "strømpe", "strømpebuk", "undertøj", "tights",
            "shampoo", "balsam", "barber", "barberblade", "gillette", "gosh", "concealer", "øjen", "øjencreme",
            "makeup", "bryn", "got2b", "tv", "watch", "ipad", "legetøj", "twinmarkers", "marker", "pen",
            "opbevaringsboks", "smartstore", "motivationsflaske",
            "papkop", "paptallerk", "papkrus", "flag"
        };
        for (String k : nonFood) {
            if (t.contains(k)) {
                return false;
            }
        }

        // Marketing / site copy that should never become a product.
        // OCR sometimes inserts invisible characters; normalize to alphanumerics for matching.
        String normalized = t.replaceAll("[^a-z0-9]+", "");
        if (normalized.contains("bilkatogo") || normalized.contains("dagligvareronline")) {
            return false;
        }

        return true;
    }

    // Note: previous "collectIpaperTextSnippets" implementation removed in favor of
    // price-marker scanning to keep candidate sets small and relevant.

    private void scanIpaperEnrichments(JsonNode node, Map<String, Object> scan,
                                      List<String> candidateSnippets) {
        scan.put("rootType",
            node == null ? "null" : (node.isObject() ? "object" : node.isArray() ? "array" : node.getNodeType().toString()));
        if (node != null && node.isObject()) {
            // log up to 30 root keys
            List<String> keys = new ArrayList<>();
            int i = 0;
            for (Map.Entry<String, JsonNode> e : node.properties()) {
                keys.add(e.getKey());
                i++;
                if (i >= 30) {
                    break;
                }
            }
            scan.put("rootKeys", keys);
        } else if (node != null && node.isArray()) {
            scan.put("rootArraySize", node.size());
        }

        Map<String, Integer> counts = new HashMap<>();
        counts.put("textNodesTotal", 0);
        counts.put("textNodesWithPriceMarker", 0);
        scan.put("counts", counts);

        collectIpaperOfferCandidates(node, candidateSnippets, counts);
    }

    private void collectIpaperOfferCandidates(JsonNode node, List<String> candidateSnippets,
                                             Map<String, Integer> counts) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return;
        }

        if (node.isTextual()) {
            counts.put("textNodesTotal", counts.get("textNodesTotal") + 1);
            String text = node.asText("");
            if (!text.isBlank() && IPAPER_PRICE_MARKER.matcher(text).find()) {
                counts.put("textNodesWithPriceMarker", counts.get("textNodesWithPriceMarker") + 1);
                if (candidateSnippets.size() < 2000) {
                    candidateSnippets.add(text);
                }
            }
            return;
        }

        if (node.isObject()) {
            for (Map.Entry<String, JsonNode> entry : node.properties()) {
                collectIpaperOfferCandidates(entry.getValue(), candidateSnippets, counts);
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                collectIpaperOfferCandidates(item, candidateSnippets, counts);
            }
        }
    }

    private List<StoreOffer> extractOffersFromIpaperSnippets(List<String> snippets, Store store,
                                                            LocalDate validFrom, LocalDate validTo) {
        List<StoreOffer> offers = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        Pattern pricePattern = Pattern.compile("\\b(\\d{1,3})(?:[\\.,](\\d{1,2}))?\\s*(?:kr\\b|,-|\\.-)");
        Pattern leadingCodePattern = Pattern.compile("^\\d{6,}\\s+");

        for (String raw : snippets) {
            if (raw == null) {
                continue;
            }
            String text = raw.replace("\n", " ").replace("\r", " ").replaceAll("\\s+", " ").trim();
            if (text.length() < 8 || text.length() > 250) {
                continue;
            }

            Matcher priceMatcher = pricePattern.matcher(text);
            if (!priceMatcher.find()) {
                continue;
            }

            String whole = priceMatcher.group(1);
            String frac = priceMatcher.group(2);
            BigDecimal price = new BigDecimal(whole + "." + (frac != null ? frac : "00"));

            String namePart = text.substring(0, priceMatcher.start()).trim();
            namePart = leadingCodePattern.matcher(namePart).replaceFirst("");
            if (namePart.length() < 3) {
                continue;
            }

            String key = (namePart + "|" + price.toPlainString()).toLowerCase();
            if (seen.contains(key)) {
                continue;
            }
            seen.add(key);

            StoreOffer offer = new StoreOffer();
            offer.setStore(store);
            offer.setValidFrom(validFrom);
            offer.setValidTo(validTo);
            offer.setScrapedAt(LocalDateTime.now());
            offer.setProductName(namePart);
            offer.setPrice(price);
            offers.add(offer);
        }

        return offers;
    }
    
    /**
     * Parse NETTO JSON data structure (Next.js).
     * NETTO embeds product data in self.__next_f.push() calls with offerItem objects.
     */
    private List<StoreOffer> parseNettoJsonData(String scriptText, Store store, 
                                               LocalDate validFrom, LocalDate validTo) {
        List<StoreOffer> offers = new ArrayList<>();
        if (scriptText == null) {
            return offers;
        }
        
        try {
            // NETTO uses Next.js format: self.__next_f.push([1,"...JSON..."])
            // The offerItem data is deeply nested in the React component structure
            // Check for offerItem in various formats (escaped and unescaped)
            boolean hasOfferItem = scriptText.contains("offerItem") 
                || scriptText.contains("\\\"offerItem\\\"")
                || scriptText.contains("'offerItem'");
            
            if (!hasOfferItem) {
                return offers; // Skip if no offerItem data
            }
            
            // The offerItem appears as "offerItem":{...} with nested JSON structure
            // We need to extract the entire JSON object by finding the start and counting braces
            // NETTO embeds JSON inside an escaped string (e.g. \\\"offerItem\\\":{...}), so normalize first.
            String normalizedText = scriptText;
            if (scriptText.contains("\\\"offerItem\\\"") && !scriptText.contains("\"offerItem\"")) {
                normalizedText = scriptText.replace("\\\"", "\"");
            }

            Pattern offerItemPattern = Pattern.compile("\"offerItem\"\\s*:\\s*\\{", Pattern.DOTALL);
            Matcher offerItemMatcher = offerItemPattern.matcher(normalizedText);
            
            // Count of offerItem objects seen (not used beyond controlling the loop).
            
            while (offerItemMatcher.find()) {
                try {
                    // Find the start position of the JSON object (after the opening brace)
                    int startPos = offerItemMatcher.end();
                    
                    // Extract the entire JSON object by counting braces
                    int braceCount = 1; // We already found the opening brace
                    int pos = startPos;
                    boolean inString = false;
                    boolean escaped = false;
                    
                    while (pos < normalizedText.length() && braceCount > 0) {
                        char c = normalizedText.charAt(pos);
                        
                        if (escaped) {
                            escaped = false;
                        } else if (c == '\\') {
                            escaped = true;
                        } else if (c == '"' && !escaped) {
                            inString = !inString;
                        } else if (!inString) {
                            if (c == '{') {
                                braceCount++;
                            } else if (c == '}') {
                                braceCount--;
                            }
                        }
                        pos++;
                    }
                    
                    // Extract the offerItem JSON object (from { to })
                    String offerItemContent = normalizedText.substring(startPos - 1, pos);
                    
                    // Try to extract individual fields using regex since the JSON might be malformed
                    StoreOffer offer = new StoreOffer();
                    offer.setStore(store);
                    offer.setValidFrom(validFrom);
                    offer.setValidTo(validTo);
                    offer.setScrapedAt(LocalDateTime.now());
                    
                    // Extract title
                    Pattern titlePattern = Pattern.compile("\"title\"\\s*:\\s*\"([^\"]+)\"");
                    Matcher titleMatcher = titlePattern.matcher(offerItemContent);
                    if (titleMatcher.find()) {
                        offer.setProductName(titleMatcher.group(1));
                    }
                    
                    // Extract description
                    Pattern descPattern = Pattern.compile("\"description\"\\s*:\\s*\"([^\"]+)\"");
                    Matcher descMatcher = descPattern.matcher(offerItemContent);
                    String description = descMatcher.find() ? descMatcher.group(1) : "";
                    
                    // Extract price
                    Pattern pricePattern = Pattern.compile("\"price\"\\s*:\\s*(\\d+(?:\\.\\d+)?)");
                    Matcher priceMatcher = pricePattern.matcher(offerItemContent);
                    if (priceMatcher.find()) {
                        offer.setPrice(BigDecimal.valueOf(Double.parseDouble(priceMatcher.group(1))));
                    }
                    
                    // Only add if we have at least a product name and it looks like food.
                    String combined = (offer.getProductName() != null ? offer.getProductName() : "") + " " + description;
                    if (offer.getProductName() != null
                        && !offer.getProductName().isEmpty()
                        && offer.getPrice() != null
                        && offer.getPrice().compareTo(BigDecimal.ZERO) > 0
                        && isLikelyFoodText(combined)) {
                        offers.add(offer);
                    }
                } catch (Exception e) {
                    log.debug("Failed to parse offerItem from NETTO JSON", e);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse NETTO JSON data", e);
        }

        return offers;
    }

    private String extractTjekCatalogId(Document doc) {
        if (doc == null) {
            return null;
        }
        String html = doc.html();
        if (html == null || html.isBlank()) {
            return null;
        }
        Matcher m = TJEK_CATALOG_ID_PATTERN.matcher(html);
        if (m.find()) {
            return m.group(1);
        }
        Matcher me = TJEK_CATALOG_ID_PATTERN_ESCAPED.matcher(html);
        if (me.find()) {
            return me.group(1);
        }
        return null;
    }

    private LocalDate[] fetchTjekCatalogValidity(String catalogId) {
        if (catalogId == null || catalogId.isBlank()) {
            return null;
        }
        try {
            String url = "https://squid-api.tjek.com/v2/catalogs/" + catalogId;
            String jsonText = org.jsoup.Jsoup.connect(url)
                .ignoreContentType(true)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(30000)
                .execute()
                .body();
            JsonNode json = objectMapper.readTree(jsonText);
            String runFrom = json.path("run_from").asText(null);
            String runTill = json.path("run_till").asText(null);
            if (runFrom == null || runTill == null) {
                return null;
            }
            LocalDate from = OffsetDateTime.parse(runFrom, TJEK_OFFSET_FORMAT)
                .toInstant()
                .atZone(DEFAULT_TJEK_ZONE)
                .toLocalDate();
            LocalDate till = OffsetDateTime.parse(runTill, TJEK_OFFSET_FORMAT)
                .toInstant()
                .atZone(DEFAULT_TJEK_ZONE)
                .toLocalDate();
            return new LocalDate[]{from, till};
        } catch (Exception e) {
            return null;
        }
    }

    private List<StoreOffer> fetchNettoOffersFromTjek(String catalogId, Store store,
                                                      LocalDate validFrom, LocalDate validTo) {
        List<StoreOffer> offers = new ArrayList<>();
        if (catalogId == null || catalogId.isBlank()) {
            return offers;
        }

        // NOTE: limit=100 works reliably for squid-api offers; larger limits may be rejected.
        int limit = 100;
        int offset = 0;
        while (offers.size() < MAX_OFFERS_FROM_IPAPER_OCR) {
            String url = "https://squid-api.tjek.com/v2/offers?catalog_id=" + catalogId
                + "&limit=" + limit
                + "&offset=" + offset;
            try {
                org.jsoup.Connection.Response resp = org.jsoup.Jsoup.connect(url)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "application/json")
                    .timeout(30000)
                    .execute();
                int status = resp.statusCode();
                String jsonText = resp.body();
                if (status != 200) {
                    break;
                }
                JsonNode arr = objectMapper.readTree(jsonText);
                if (arr == null || !arr.isArray() || arr.isEmpty()) {
                    break;
                }
                for (JsonNode node : arr) {
                    if (node == null || !node.isObject()) {
                        continue;
                    }
                    String heading = node.path("heading").asText("").trim();
                    String description = node.path("description").asText("").trim();
                    JsonNode pricing = node.path("pricing");
                    BigDecimal price = pricing.path("price").isNumber()
                        ? BigDecimal.valueOf(pricing.path("price").asDouble())
                        : null;

                    if (heading.isBlank() || price == null) {
                        continue;
                    }
                    String combined = heading + " " + description;
                    if (!isLikelyFoodText(combined) || looksLikeStoreInfo(combined)) {
                        continue;
                    }

                    StoreOffer offer = new StoreOffer();
                    offer.setStore(store);
                    offer.setValidFrom(validFrom);
                    offer.setValidTo(validTo);
                    offer.setScrapedAt(LocalDateTime.now());
                    offer.setProductName(heading);
                    offer.setPrice(price);

                    offers.add(offer);
                    if (offers.size() >= MAX_OFFERS_FROM_IPAPER_OCR) {
                        break;
                    }
                }
            } catch (Exception e) {
                break;
            }
            offset += limit;
        }

        return offers;
    }
}
