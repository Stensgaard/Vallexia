package com.vallexia.store.service;

import com.vallexia.store.entity.Store;
import com.vallexia.store.entity.StoreOffer;
import com.vallexia.store.exception.StoreScrapingException;
import com.vallexia.store.repository.StoreOfferRepository;
import com.vallexia.store.repository.StoreRepository;
import com.vallexia.store.util.FlyerPageParser;
import com.vallexia.store.util.FlyerUrlResolver;
import com.vallexia.store.util.OfferVariantSplitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for scraping weekly offers from store flyer pages.
 * Supports multiple locales and number formats.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-01-XX
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StoreFlyerScrapingService {
    
    private final StoreRepository storeRepository;
    private final StoreOfferRepository storeOfferRepository;
    private final FlyerUrlResolver flyerUrlResolver;
    private final FlyerPageParser flyerPageParser;
    private final OfferVariantSplitter offerVariantSplitter;
    private final OfferPricingEnricher offerPricingEnricher;
    private final OfferPackageSizeEnricher offerPackageSizeEnricher;
    private final OfferNormalizationService offerNormalizationService;
    private final OfferFilteringService offerFilteringService;
    
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final int REQUEST_TIMEOUT_MS = 30000;
    private static final int REQUEST_DELAY_MS = 2000;
    
    /**
     * Scrape offers for all stores.
     * 
     * @return number of offers scraped across all stores
     */
    @Transactional
    public int scrapeAllStores() {
        log.info("Starting store flyer scraping for all stores");
        
        List<Store> stores = storeRepository.findAll();
        int totalOffers = 0;
        
        for (Store store : stores) {
            try {
                // Add delay between requests to be respectful
                if (totalOffers > 0) {
                    Thread.sleep(REQUEST_DELAY_MS);
                }
                
                int offersCount = scrapeStoreOffers(store);
                totalOffers += offersCount;
                log.info("Scraped {} offers for store: {}", offersCount, store.getName());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Scraping interrupted for store: {}", store.getName());
            } catch (Exception e) {
                log.error("Failed to scrape offers for store: {}", store.getName(), e);
                // Continue with other stores even if one fails
            }
        }
        
        log.info("Completed store flyer scraping. Total offers scraped: {}", totalOffers);
        return totalOffers;
    }
    
    /**
     * Scrape offers for a specific store.
     * 
     * @param store the store to scrape
     * @return number of offers scraped
     */
    @Transactional
    public int scrapeStoreOffers(Store store) {
        if (store == null) {
            throw new IllegalArgumentException("store is required");
        }

        log.info("Scraping offers for store: {}", store.getName());
        
        // Reset exclusion counters for this scraping run
        offerFilteringService.resetExclusionCounters();
        
        try {
            // Fetch the initial page (may be landing page or direct flyer)
            Document doc = Jsoup.connect(store.getFlyerUrl())
                .userAgent(USER_AGENT)
                .timeout(REQUEST_TIMEOUT_MS)
                .get();
            
            // Check if this is a landing page with multiple flyers
            String actualFlyerUrl = store.getFlyerUrl();
            boolean isLanding = flyerUrlResolver.isLandingPage(doc);
            
            if (isLanding) {
                log.debug("Detected landing page for store: {}", store.getName());
                
                String foodFlyerUrl = flyerUrlResolver.findFoodFlyerUrl(doc, store);
                
                if (foodFlyerUrl == null) {
                    log.warn("Could not find food flyer link for store: {}. " +
                             "No URL matched keywords: {}. Skipping scraping to avoid wrong data.",
                             store.getName(), 
                             store.getFoodFlyerKeywords() != null 
                                 ? Arrays.asList(store.getFoodFlyerKeywords()) 
                                 : "none");
                    return 0;
                }
                
                actualFlyerUrl = foodFlyerUrl;
                log.debug("Found food flyer URL for store {}: {}", store.getName(), actualFlyerUrl);
                
                // Fetch the actual food flyer page
                doc = Jsoup.connect(actualFlyerUrl)
                    .userAgent(USER_AGENT)
                    .timeout(REQUEST_TIMEOUT_MS)
                    .get();
            }
            
            // Extract validity dates (usually shown on the page)
            LocalDate[] validityDates = flyerPageParser.extractValidityDates(doc, store);
            LocalDate validFrom = validityDates[0];
            LocalDate validTo = validityDates[1];
            
            log.debug("Extracted validity period: {} to {} for store: {}", validFrom, validTo, store.getName());
            
            // Extract offers from the page
            List<StoreOffer> offers = flyerPageParser.extractOffers(doc, store, validFrom, validTo);
            
            // Split "A eller B" style offers into multiple variants for better ingredient matching.
            offers = expandOfferVariants(offers);

            // Apply exclusion rules to filter out low-value offers (coffee/tea, soda, alcohol, non-food, etc.)
            int beforeFilter = offers.size();
            offers = offers.stream()
                .filter(offer -> !offerFilteringService.isExcluded(store, offer))
                .collect(Collectors.toList());
            int excluded = beforeFilter - offers.size();
            if (excluded > 0) {
                log.debug("Excluded {} offers for store: {} ({} remaining)", excluded, store.getName(), offers.size());
            }

            // Enrich offers with min-purchase/unit price information (e.g., "2 stk").
            for (StoreOffer offer : offers) {
                offerPricingEnricher.enrich(offer);
                offerPackageSizeEnricher.enrich(offer);
            }

            // Deduplicate offers that collapse to the same match-key+price (common OCR variations like
            // "Creme fraiche 18%" vs "Creme fraiche 18% 500 g.").
            offers = dedupeByMatchKeyPrice(offers);

            log.debug("Extracted {} offers (after splitting) for store: {}", offers.size(), store.getName());
            
            // Delete old offers for this validity period (if re-scraping)
            storeOfferRepository.deleteByStoreIdAndValidFrom(store.getId(), validFrom);
            
            // Save new offers
            if (!offers.isEmpty()) {
                storeOfferRepository.saveAll(offers);
            }
            
            return offers.size();
            
        } catch (StoreScrapingException e) {
            // Re-throw custom exceptions as-is
            throw e;
        } catch (Exception e) {
            log.error("Error scraping offers for store: {}", store.getName(), e);
            throw new StoreScrapingException("Failed to scrape offers for store: " + store.getName(), e);
        }
    }

    private List<StoreOffer> expandOfferVariants(List<StoreOffer> offers) {
        if (offers == null || offers.isEmpty()) {
            return offers;
        }

        // Safety caps to avoid runaway expansions.
        int maxTotal = 2000;
        Set<String> seen = new HashSet<>();
        java.util.ArrayList<StoreOffer> out = new java.util.ArrayList<>();

        for (StoreOffer offer : offers) {
            if (offer == null) {
                continue;
            }
            String name = offer.getProductName();
            if (name == null || name.isBlank()) {
                continue;
            }
            List<String> variants = offerVariantSplitter.splitVariants(name);
            if (variants.isEmpty()) {
                variants = List.of(name);
            }

            boolean isAmbiguousMultiVariant = shouldAutoDismissAmbiguousMultiVariant(offer, name, variants);

            for (String v : variants) {
                if (v == null || v.isBlank()) {
                    continue;
                }
                // Deduplicate by store+validFrom+name+price to keep list stable.
                // Normalize variant name (case-insensitive, trimmed) for deduplication.
                String normalizedV = v.trim().toLowerCase();
                String key = offer.getStore().getId() + "|" + offer.getValidFrom() + "|" + normalizedV + "|" + offer.getPrice();
                if (!seen.add(key)) {
                    continue;
                }

                StoreOffer clone = new StoreOffer();
                clone.setStore(offer.getStore());
                clone.setValidFrom(offer.getValidFrom());
                clone.setValidTo(offer.getValidTo());
                clone.setScrapedAt(offer.getScrapedAt());
                clone.setPrice(offer.getPrice());
                clone.setBundlePrice(offer.getBundlePrice());
                clone.setUnitPrice(offer.getUnitPrice());
                clone.setMinPurchaseQty(offer.getMinPurchaseQty());
                clone.setMinPurchaseUnit(offer.getMinPurchaseUnit());
                clone.setRawPriceText(offer.getRawPriceText());
                clone.setProductName(v);
                if (isAmbiguousMultiVariant) {
                    clone.setDismissed(true);
                    clone.setDismissedAt(OffsetDateTime.now());
                }
                out.add(clone);

                if (out.size() >= maxTotal) {
                    return out;
                }
            }
        }

        return out;
    }

    //region agent log (helpers)
    private static boolean containsMultiVariantMarker(String text) {
        if (text == null) {
            return false;
        }
        String t = text.toLowerCase(java.util.Locale.ROOT);
        return t.contains("frit valg") || t.contains("flere varianter");
    }

    private static boolean shouldAutoDismissAmbiguousMultiVariant(StoreOffer offer, String name, List<String> variants) {
        if (offer == null || name == null || variants == null) {
            return false;
        }
        if (!containsMultiVariantMarker(offer.getRawPriceText())) {
            return false;
        }
        String lowerName = name.toLowerCase(java.util.Locale.ROOT);
        boolean nameHasOptions = lowerName.contains(" eller ") || lowerName.contains(",");
        boolean nameMentionsMulti = lowerName.contains("frit valg") || lowerName.contains("flere varianter");
        boolean noRealSplit = variants.size() == 1 && name.equals(variants.getFirst());
        return noRealSplit && (nameHasOptions || nameMentionsMulti);
    }

    //endregion

    private List<StoreOffer> dedupeByMatchKeyPrice(List<StoreOffer> offers) {
        if (offers == null || offers.isEmpty()) {
            return offers;
        }
        Map<String, StoreOffer> bestByKey = new LinkedHashMap<>();
        for (StoreOffer o : offers) {
            if (o == null) {
                continue;
            }
            String matchKey = offerNormalizationService.toMatchKey(o.getProductName());
            String price = o.getPrice() == null ? "null" : o.getPrice().toPlainString();
            String key = o.getStore().getId() + "|" + o.getValidFrom() + "|" + matchKey + "|" + price;

            StoreOffer existing = bestByKey.get(key);
            if (existing == null) {
                bestByKey.put(key, o);
                continue;
            }
            if (semanticOfferScore(o) > semanticOfferScore(existing)) {
                bestByKey.put(key, o);
            }
        }
        return new java.util.ArrayList<>(bestByKey.values());
    }

    private int semanticOfferScore(StoreOffer o) {
        if (o == null) {
            return Integer.MIN_VALUE;
        }
        int score = 0;
        if (o.getPackageUnit() != null && !o.getPackageUnit().isBlank()) {
            score += 100;
        }
        if (o.getPackageQtyMin() != null) {
            score += 10;
        }
        if (o.getRawPriceText() != null && !o.getRawPriceText().isBlank()) {
            score += 1;
        }
        String name = o.getProductName() == null ? "" : o.getProductName().trim();
        score -= Math.min(200, name.length());
        return score;
    }
}
