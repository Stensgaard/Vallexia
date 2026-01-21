package com.vallexia.store.scrape.ipaper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vallexia.store.entity.Store;
import com.vallexia.store.entity.StoreOffer;
import com.vallexia.store.scrape.StoreOfferExtractor;
import com.vallexia.store.scrape.text.OfferFilters;
import com.vallexia.store.scrape.text.OcrNameExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extractor for iPaper-based flyers (BILKA and FOETEX).
 * iPaper flyers embed offer data in window.staticSettings.pageTexts (OCR text)
 * and enrichments (hotspots/links metadata).
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-01-XX
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IpaperOfferExtractor implements StoreOfferExtractor {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern IPAPER_PRICE_MARKER =
        Pattern.compile("\\b\\d{1,4}(?:[\\.,]\\d{1,2})?\\s*(?:kr\\b|,-|\\.-|-(?!\\d))",
            Pattern.CASE_INSENSITIVE);
    private static final int MAX_OFFERS_FROM_IPAPER_OCR = 1200;
    private static final Pattern LEADING_CODE_PATTERN = Pattern.compile("^\\d{5,}\\s+");
    
    @Override
    public boolean supports(Store store) {
        return store != null && ("BILKA".equals(store.getName()) || "FOETEX".equals(store.getName()));
    }
    
    @Override
    public List<StoreOffer> extractOffers(Document doc, Store store, LocalDate validFrom, LocalDate validTo) {
        List<StoreOffer> offers = new ArrayList<>();
        Elements scriptTags = doc.select("script");
        
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
                        offers.addAll(parseIpaperStaticSettings(jsonNode, store, validFrom, validTo));
                    }
                }
                
                // Pattern 2: window.__NUXT__ = {...} (FOETEX)
                if (scriptText.contains("window.__NUXT__")) {
                    Pattern pattern = Pattern.compile("window\\.__NUXT__\\s*=\\s*function.*?return\\s*(\\{.*?\\})", Pattern.DOTALL);
                    Matcher matcher = pattern.matcher(scriptText);
                    if (matcher.find()) {
                        String jsonStr = matcher.group(1);
                        JsonNode jsonNode = objectMapper.readTree(jsonStr);
                        offers.addAll(parseIpaperStaticSettings(jsonNode, store, validFrom, validTo));
                    }
                }
                
            } catch (Exception e) {
                log.debug("Failed to parse JSON from script tag for store: {}", store != null ? store.getName() : null, e);
            }
        }

        // For iPaper-backed flyers (BILKA/FOETEX), enrichments JSON often contains only hotspots/links.
        // As a last resort, parse offers from meta descriptions (OpenGraph description often includes
        // a few offer text snippets with prices like "20.-" on page 1).
        if (offers.isEmpty()) {
            offers.addAll(extractOffersFromIpaperMetaDescriptions(doc, store, validFrom, validTo));
        }
        
        return offers;
    }
    
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
        List<StoreOffer> byCodes = extractOffersFromIpaperPageTextsByCodes(staticSettingsNode, store, validFrom, validTo);
        List<StoreOffer> byPriceMarkers = extractOffersFromIpaperPageTextsByPriceMarkers(staticSettingsNode, store, validFrom, validTo);

        // Merge results: the code-segmented path misses offers that have no product code near the OCR text
        // (e.g. some big hero offers like "hakket oksekød").
        if (byCodes.isEmpty()) {
            return byPriceMarkers;
        }
        if (byPriceMarkers.isEmpty()) {
            return byCodes;
        }

        Set<String> seen = new HashSet<>();
        List<StoreOffer> merged = new ArrayList<>();
        for (StoreOffer o : byCodes) {
            if (o == null) {
                continue;
            }
            String key = (o.getProductName() + "|" + o.getPrice()).toLowerCase();
            if (seen.add(key)) {
                merged.add(o);
            }
        }
        for (StoreOffer o : byPriceMarkers) {
            if (o == null) {
                continue;
            }
            String key = (o.getProductName() + "|" + o.getPrice()).toLowerCase();
            if (seen.add(key)) {
                merged.add(o);
            }
            if (merged.size() >= MAX_OFFERS_FROM_IPAPER_OCR) {
                break;
            }
        }
        return merged;
    }

    // Older price-marker scanning (kept for safety). This catches offers that don't have product codes nearby.
    private List<StoreOffer> extractOffersFromIpaperPageTextsByPriceMarkers(JsonNode staticSettingsNode, Store store,
                                                                          LocalDate validFrom, LocalDate validTo) {
        List<StoreOffer> offersFallback = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        JsonNode root = staticSettingsNode == null ? objectMapper.createObjectNode() : staticSettingsNode;
        JsonNode pageTexts = root.path("pageTexts");
        if (pageTexts == null || !pageTexts.isArray() || pageTexts.isEmpty()) {
            return offersFallback;
        }

        Pattern pricePattern =
        Pattern.compile("\\b(\\d{1,4})(?:[\\.,](\\d{1,2}))?\\s*(?:kr\\b|,-|\\.-|-(?!\\d))",
                Pattern.CASE_INSENSITIVE);

        for (JsonNode pageTextNode : pageTexts) {
            if (offersFallback.size() >= MAX_OFFERS_FROM_IPAPER_OCR) {
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
            while (m.find() && offersFallback.size() < MAX_OFFERS_FROM_IPAPER_OCR) {
                int priceStart = m.start();
                int priceEnd = m.end();

                BigDecimal price = new BigDecimal(m.group(1) + "." + (m.group(2) != null ? m.group(2) : "00"));

                int windowStart = Math.max(lastMatchEnd, priceStart - 200);
                int boundary = OcrNameExtractor.findLastBoundary(text, windowStart, priceStart);
                if (boundary >= 0) {
                    windowStart = boundary + 1;
                }

                int windowEnd = Math.min(text.length(), priceEnd + 60);
                String segment = text.substring(windowStart, windowEnd).trim();
                String nameBefore = text.substring(windowStart, priceStart).trim();

                nameBefore = LEADING_CODE_PATTERN.matcher(nameBefore).replaceFirst("");
                nameBefore = OcrNameExtractor.cleanupOcrName(nameBefore);

                String nameAfter = OcrNameExtractor.deriveIpaperNameAfter(text, priceEnd);
                String nameByCode = OcrNameExtractor.deriveIpaperNameByProductCode(text, priceStart, priceEnd);

                boolean beforeBad = OfferFilters.isBadIpaperName(nameBefore);
                boolean afterBad = OfferFilters.isBadIpaperName(nameAfter);
                boolean codeBad = OfferFilters.isBadIpaperName(nameByCode);

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

                if (OfferFilters.isBadIpaperName(namePart)) {
                    lastMatchEnd = priceEnd;
                    continue;
                }

                if (!OfferFilters.isLikelyFoodText(namePart + " " + segment)) {
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
                offer.setRawPriceText(buildRawSnippet(segment, priceStart - windowStart, priceEnd - windowStart));
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
            Pattern.compile("\\b(\\d{1,4})(?:[\\.,](\\d{1,2}))?\\s*(?:kr\\b|,-|\\.-|-(?!\\d))",
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
                beforePrice = OcrNameExtractor.cutAtStopTokens(beforePrice);
                String nameRaw = OcrNameExtractor.extractIpaperNameFromCodeSegment(beforePrice);
                nameRaw = OcrNameExtractor.normalizeIpaperExtractedName(nameRaw);

                if (OfferFilters.isBadIpaperName(nameRaw) || nameRaw.length() < 3) {
                    continue;
                }

                String offerText = segment.substring(0, Math.min(segment.length(), pm.end() + 80)).trim();
                if (OfferFilters.looksLikeStoreInfo(nameRaw + " " + offerText)) {
                    continue;
                }
                if (!OfferFilters.isLikelyFoodText(nameRaw + " " + offerText)) {
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
                offer.setRawPriceText(buildRawSnippet(segment, pm.start(), pm.end()));
                offers.add(offer);
            }
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

        Pattern pricePattern = Pattern.compile("\\b(\\d{1,4})(?:[\\.,](\\d{1,2}))?\\s*(?:kr\\b|,-|\\.-|-(?!\\d))",
            Pattern.CASE_INSENSITIVE);

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
                        offer.setRawPriceText(buildRawSnippet(text, pm.start(), pm.end()));
                        offers.add(offer);
                    }
                }
            }
        }

        return offers;
    }

    private String buildRawSnippet(String text, int priceStartInclusive, int priceEndExclusive) {
        if (text == null) {
            return null;
        }
        String t = text.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
        if (t.isBlank()) {
            return null;
        }

        int from = Math.max(0, priceStartInclusive - 120);
        int to = Math.min(t.length(), priceEndExclusive + 120);
        if (to <= from) {
            return truncateTo255(t);
        }

        String window = t.substring(from, to).trim();
        int primaryEndInWindow = Math.max(0, priceEndExclusive - from);

        // Cut at the next price marker after the current one to avoid leaking prices from nearby offers.
        int cut = window.length();
        Matcher m = IPAPER_PRICE_MARKER.matcher(window);
        while (m.find()) {
            if (m.start() > primaryEndInWindow + 1) {
                cut = m.start();
                break;
            }
        }
        window = window.substring(0, cut).trim();
        return truncateTo255(window);
    }

    private String truncateTo255(String text) {
        if (text == null) {
            return null;
        }
        String t = text.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
        if (t.length() <= 255) {
            return t;
        }
        return t.substring(0, 255);
    }

    private void scanIpaperEnrichments(JsonNode node, Map<String, Object> scan,
                                      List<String> candidateSnippets) {
        scan.put("rootType",
            node == null ? "null" : (node.isObject() ? "object" : node.isArray() ? "array" : node.getNodeType().toString()));
        if (node != null && node.isObject()) {
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

        Pattern pricePattern = Pattern.compile("\\b(\\d{1,3})(?:[\\.,](\\d{1,2}))?\\s*(?:kr\\b|,-|\\.-|-(?!\\d))");
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
            offer.setRawPriceText(buildRawSnippet(text, priceMatcher.start(), priceMatcher.end()));
            offers.add(offer);
        }

        return offers;
    }
}
