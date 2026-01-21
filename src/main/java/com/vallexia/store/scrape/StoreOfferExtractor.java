package com.vallexia.store.scrape;

import com.vallexia.store.entity.Store;
import com.vallexia.store.entity.StoreOffer;
import org.jsoup.nodes.Document;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interface for extracting store offers from flyer pages.
 * Each implementation handles a specific store or flyer format.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-01-XX
 */
public interface StoreOfferExtractor {
    
    /**
     * Check if this extractor supports the given store.
     * 
     * @param store the store entity
     * @return true if this extractor can handle the store's flyer format
     */
    boolean supports(Store store);
    
    /**
     * Extract offers from the HTML document.
     * 
     * @param doc the HTML document
     * @param store the store entity
     * @param validFrom validity start date
     * @param validTo validity end date
     * @return list of extracted offers
     */
    List<StoreOffer> extractOffers(Document doc, Store store, LocalDate validFrom, LocalDate validTo);
    
    /**
     * Extract validity dates from the flyer page (optional).
     * If not implemented, the caller will use a default fallback.
     * 
     * @param doc the HTML document
     * @param store the store entity
     * @return Optional containing array of [validFrom, validTo] dates, empty if not extractable
     */
    default Optional<LocalDate[]> extractValidityDates(Document doc, Store store) {
        return Optional.empty();
    }
}
