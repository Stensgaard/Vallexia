package com.vallexia.store.service;

import com.vallexia.store.entity.Store;
import com.vallexia.store.entity.StoreOffer;
import com.vallexia.store.exception.StoreScrapingException;
import com.vallexia.store.repository.StoreOfferRepository;
import com.vallexia.store.repository.StoreRepository;
import com.vallexia.store.util.FlyerPageParser;
import com.vallexia.store.util.FlyerUrlResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

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
        log.info("Scraping offers for store: {}", store.getName());
        
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
            
            log.debug("Extracted {} offers for store: {}", offers.size(), store.getName());
            
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
}
