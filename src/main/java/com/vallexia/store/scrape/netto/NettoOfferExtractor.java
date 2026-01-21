package com.vallexia.store.scrape.netto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vallexia.store.entity.Store;
import com.vallexia.store.entity.StoreOffer;
import com.vallexia.store.scrape.StoreOfferExtractor;
import com.vallexia.store.scrape.text.OfferFilters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extractor for NETTO store flyers.
 * NETTO uses Tjek "squid-api" catalogs with explicit run_from/run_till timestamps.
 * Offers are fetched from the Tjek API with pagination support.
 * Falls back to parsing Next.js offerItem data if Tjek API is unavailable.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-01-XX
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NettoOfferExtractor implements StoreOfferExtractor {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern TJEK_CATALOG_ID_PATTERN =
        Pattern.compile("squid-api\\.tjek\\.com/v2/catalogs/([A-Za-z0-9_-]+)/download",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern TJEK_CATALOG_ID_PATTERN_ESCAPED =
        Pattern.compile("squid-api\\\\.tjek\\\\.com\\\\/v2\\\\/catalogs\\\\/([A-Za-z0-9_-]+)\\\\/download",
            Pattern.CASE_INSENSITIVE);
    private static final DateTimeFormatter TJEK_OFFSET_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    private static final ZoneId DEFAULT_TJEK_ZONE = ZoneId.of("Europe/Copenhagen");
    private static final int MAX_OFFERS = 1200;
    
    @Override
    public boolean supports(Store store) {
        return store != null && "NETTO".equals(store.getName());
    }
    
    @Override
    public List<StoreOffer> extractOffers(Document doc, Store store, LocalDate validFrom, LocalDate validTo) {
        // Prefer Tjek squid-api offers (full offer set). The embedded Next.js offerItem list is only a preview.
        try {
            String catalogId = extractTjekCatalogId(doc);
            if (catalogId != null) {
                List<StoreOffer> tjekOffers = fetchNettoOffersFromTjek(catalogId, store, validFrom, validTo);
                if (!tjekOffers.isEmpty()) {
                    return tjekOffers;
                }
            }
        } catch (Exception e) {
            log.debug("Failed to fetch offers from Tjek API, falling back to Next.js parsing", e);
        }
        
        // Fallback: Parse Next.js offerItem data from script tags
        List<StoreOffer> offers = new ArrayList<>();
        Elements scriptTags = doc.select("script");
        
        for (org.jsoup.nodes.Element script : scriptTags) {
            String scriptText = script.html();
            if (scriptText == null || scriptText.trim().isEmpty()) {
                continue;
            }
            
            if (scriptText.contains("self.__next_f.push")) {
                offers.addAll(parseNettoJsonData(scriptText, store, validFrom, validTo));
            }
        }
        
        // If no offers found in individual script tags, search the entire document HTML
        if (offers.isEmpty() && doc.html().contains("offerItem")) {
            log.debug("No offers found in individual script tags for NETTO, searching entire document HTML");
            offers.addAll(parseNettoJsonData(doc.html(), store, validFrom, validTo));
        }
        
        return offers;
    }
    
    @Override
    public Optional<LocalDate[]> extractValidityDates(Document doc, Store store) {
        try {
            String catalogId = extractTjekCatalogId(doc);
            if (catalogId != null) {
                LocalDate[] dates = fetchTjekCatalogValidity(catalogId);
                if (dates != null) {
                    return Optional.of(dates);
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract validity dates from Tjek catalog", e);
        }
        return Optional.empty();
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
        while (offers.size() < MAX_OFFERS) {
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
                    if (!OfferFilters.isLikelyFoodText(combined) || OfferFilters.looksLikeStoreInfo(combined)) {
                        continue;
                    }

                    StoreOffer offer = new StoreOffer();
                    offer.setStore(store);
                    offer.setValidFrom(validFrom);
                    offer.setValidTo(validTo);
                    offer.setScrapedAt(LocalDateTime.now());
                    offer.setProductName(heading);
                    offer.setPrice(price);
                    // Keep description around for downstream enrichment (e.g. "10 stk", "400 g").
                    // NOTE: actual use of this field will be decided after log evidence.
                    offer.setRawPriceText(description.isBlank() ? null : description);

                    offers.add(offer);
                    if (offers.size() >= MAX_OFFERS) {
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
                        && OfferFilters.isLikelyFoodText(combined)) {
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
}
