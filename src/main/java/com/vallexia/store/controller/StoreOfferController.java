package com.vallexia.store.controller;

import com.vallexia.store.dto.StoreDto;
import com.vallexia.store.dto.DiscountedIngredientDto;
import com.vallexia.store.dto.StoreOfferDto;
import com.vallexia.store.dto.UnmatchedStoreOfferDto;
import com.vallexia.store.entity.Store;
import com.vallexia.store.entity.StoreOffer;
import com.vallexia.store.entity.StoreOfferIngredientMatch;
import com.vallexia.store.mapper.StoreOfferMapper;
import com.vallexia.store.repository.IngredientAliasRepository;
import com.vallexia.store.repository.StoreOfferRepository;
import com.vallexia.store.repository.StoreRepository;
import com.vallexia.store.service.StoreFlyerScrapingService;
import com.vallexia.store.service.OfferToIngredientMatcher;
import com.vallexia.store.service.StoreOfferIngredientMatchingService;
import com.vallexia.recipe.repository.IngredientRepository;
import com.vallexia.store.repository.StoreOfferIngredientMatchRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for store offers endpoints.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-01-XX
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/store-offers")
@Tag(name = "Store Offers", description = "Operations related to store offers from weekly flyers")
@RequiredArgsConstructor
public class StoreOfferController {
    
    private final StoreOfferRepository storeOfferRepository;
    private final StoreRepository storeRepository;
    private final StoreOfferMapper storeOfferMapper;
    private final StoreFlyerScrapingService scrapingService;
    private final StoreOfferIngredientMatchingService matchingService;
    private final StoreOfferIngredientMatchRepository matchRepository;
    private final IngredientRepository ingredientRepository;
    private final IngredientAliasRepository ingredientAliasRepository;
    
    /**
     * Get all current offers (valid today).
     * 
     * @param storeId optional store ID to filter by
     * @param categories optional comma-separated list of categories to filter by
     * @return list of current offers
     */
    @Operation(summary = "Get current offers", description = "Retrieve all current offers valid today, optionally filtered by store and categories")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Offers retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<StoreOfferDto>> getCurrentOffers(
            @Parameter(description = "Store ID to filter by") @RequestParam(required = false) Long storeId) {
        
        LocalDate today = LocalDate.now();
        List<StoreOffer> offers;
        
        if (storeId != null) {
            offers = storeOfferRepository.findCurrentOffersByStore(storeId, today);
        } else {
            offers = storeOfferRepository.findAllCurrentOffers(today);
        }
        
        List<StoreOfferDto> offerDtos = storeOfferMapper.toStoreOfferDtoList(offers);
        return ResponseEntity.ok(offerDtos);
    }
    
    /**
     * Get offers for a specific store.
     * 
     * @param storeId the store ID
     * @return list of current offers for the store
     */
    @Operation(summary = "Get offers by store", description = "Retrieve current offers for a specific store")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Offers retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Store not found")
    })
    @GetMapping("/stores/{storeId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<StoreOfferDto>> getOffersByStore(
            @Parameter(description = "Store ID", required = true) @PathVariable Long storeId) {
        
        if (!storeRepository.existsById(storeId)) {
            return ResponseEntity.notFound().build();
        }
        
        LocalDate today = LocalDate.now();
        List<StoreOffer> offers = storeOfferRepository.findCurrentOffersByStore(storeId, today);
        List<StoreOfferDto> offerDtos = storeOfferMapper.toStoreOfferDtoList(offers);
        
        return ResponseEntity.ok(offerDtos);
    }
    
    /**
     * Get all stores.
     * 
     * @return list of all stores
     */
    @Operation(summary = "Get all stores", description = "Retrieve all store chains configured in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stores retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/stores")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<StoreDto>> getAllStores() {
        List<Store> stores = storeRepository.findAll();
        List<StoreDto> storeDtos = storeOfferMapper.toStoreDtoList(stores);
        return ResponseEntity.ok(storeDtos);
    }
    
    /**
     * Manually trigger scraping for all stores (admin only).
     * 
     * @return scraping result with number of offers scraped
     */
    @Operation(summary = "Trigger scraping (Admin only)", description = "Manually trigger store flyer scraping for all stores. Requires ADMIN role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Scraping completed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
    })
    @PostMapping("/scrape")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> triggerScraping() {
        log.info("Manual scraping triggered by admin");
        
        try {
            int totalOffers = scrapingService.scrapeAllStores();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalOffers", totalOffers);
            response.put("message", "Scraping completed successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during manual scraping", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Scraping failed: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Manually trigger scraping for a specific store (admin only).
     * 
     * @param storeId the store ID to scrape
     * @return scraping result with number of offers scraped
     */
    @Operation(summary = "Trigger scraping for specific store (Admin only)", description = "Manually trigger store flyer scraping for a specific store. Requires ADMIN role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Scraping completed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "Store not found")
    })
    @PostMapping("/scrape/stores/{storeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> triggerScrapingForStore(
            @Parameter(description = "Store ID", required = true) @PathVariable Long storeId) {
        
        Store store = storeRepository.findById(storeId)
            .orElse(null);
        
        if (store == null) {
            return ResponseEntity.notFound().build();
        }
        
        log.info("Manual scraping triggered for store: {}", store.getName());
        
        try {
            int offersCount = scrapingService.scrapeStoreOffers(store);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("storeId", storeId);
            response.put("storeName", store.getName());
            response.put("offersCount", offersCount);
            response.put("message", "Scraping completed successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during manual scraping for store: {}", store.getName(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("storeId", storeId);
            response.put("message", "Scraping failed: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get discounted ingredients (ingredients with at least one current matched offer).
     */
    @Operation(
        summary = "Get discounted ingredients",
        description =
            "Returns ingredients that have at least one current flyer offer. Optionally filter by storeIds.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Discounted ingredients retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/discounted-ingredients")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<DiscountedIngredientDto>> getDiscountedIngredients(
        @RequestParam(required = false) List<Long> storeIds,
        @RequestParam(required = false) LocalDate date,
        @RequestParam(required = false, defaultValue = "50") int limit,
        @RequestHeader(value = "Accept-Language", required = false) String locale) {

        LocalDate d = date != null ? date : LocalDate.now();
        List<DiscountedIngredientDto> result =
            matchingService.getDiscountedIngredients(d, storeIds, limit, locale);
        return ResponseEntity.ok(result);
    }

    /**
     * List current offers that have not yet been matched to an ingredient.
     * Admin-only for debugging/curation.
     */
    @Operation(summary = "Get unmatched offers (Admin only)")
    @GetMapping("/unmatched")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UnmatchedStoreOfferDto>> getUnmatchedOffers(
        @RequestParam(required = false) Long storeId,
        @RequestParam(required = false) LocalDate date,
        @RequestParam(required = false, defaultValue = "false") boolean includeDismissed) {

        LocalDate d = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(matchingService.getUnmatchedCurrentOffers(d, storeId, includeDismissed));
    }

    /**
     * Manually trigger offer->ingredient matching (admin only).
     */
    @Operation(summary = "Trigger matching (Admin only)")
    @PostMapping("/match")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> triggerMatching(
        @RequestParam(required = false) LocalDate date) {

        LocalDate d = date != null ? date : LocalDate.now();

        int matchedCount = matchingService.matchCurrentOffers(d);
        int unmatchedRemaining = matchingService.getUnmatchedCurrentOffers(d, null).size();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("date", d);
        response.put("matchedCount", matchedCount);
        response.put("unmatchedRemaining", unmatchedRemaining);
        return ResponseEntity.ok(response);
    }

    /**
     * Manually assign an ingredient to an offer (Admin only).
     * Optionally create an alias so future matches improve.
     */
    @Operation(summary = "Manually match offer to ingredient (Admin only)")
    @PostMapping("/offers/{offerId}/match-ingredient")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> manualMatchOfferToIngredient(
        @PathVariable Long offerId,
        @RequestParam Long ingredientId,
        @RequestParam(required = false) String locale,
        @RequestParam(required = false, defaultValue = "false") boolean createAlias) {

        StoreOffer offer = storeOfferRepository.findById(offerId).orElse(null);
        if (offer == null) {
            return ResponseEntity.notFound().build();
        }

        var ingredient = ingredientRepository.findById(ingredientId).orElse(null);
        if (ingredient == null) {
            return ResponseEntity.notFound().build();
        }

        String loc = matchingService.normalizeLocale(locale);
        String matchedText = matchingService.normalizeForAdminAlias(offer.getProductName());

        StoreOfferIngredientMatch match = matchRepository.findByOfferIdFetchAll(offerId)
            .orElseGet(StoreOfferIngredientMatch::new);
        match.setOffer(offer);
        match.setIngredient(ingredient);
        match.setLocale(loc);
        match.setMatchMethod(OfferToIngredientMatcher.METHOD_MANUAL);
        match.setConfidence(java.math.BigDecimal.ONE);
        match.setMatchedText(matchedText.isBlank() ? offer.getProductName() : matchedText);
        matchRepository.save(match);

        if (createAlias) {
            // Create an alias entry (best-effort; ignore if it violates uniqueness).
            try {
                com.vallexia.store.entity.IngredientAlias alias = new com.vallexia.store.entity.IngredientAlias();
                alias.setIngredient(ingredient);
                alias.setLocale(loc);
                alias.setAlias(match.getMatchedText());
                alias.setPriority(100);
                ingredientAliasRepository.save(alias);
            } catch (Exception ignored) {
                // ignore duplicate alias errors
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("offerId", offerId);
        response.put("ingredientId", ingredientId);
        response.put("createAlias", createAlias);
        return ResponseEntity.ok(response);
    }

    /**
     * Dismiss an unmatched offer (Admin only).
     * Dismissed offers are excluded from matching and the unmatched list by default.
     */
    @Operation(summary = "Dismiss offer (Admin only)")
    @PostMapping("/offers/{offerId}/dismiss")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> dismissOffer(@PathVariable Long offerId) {

        StoreOffer offer = storeOfferRepository.findById(offerId).orElse(null);
        if (offer == null) {
            return ResponseEntity.notFound().build();
        }

        offer.setDismissed(true);
        offer.setDismissedAt(OffsetDateTime.now());
        storeOfferRepository.save(offer);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("offerId", offerId);
        response.put("dismissed", true);
        return ResponseEntity.ok(response);
    }

    /**
     * Undo dismissal of an offer (Admin only).
     * Restores the offer to the unmatched list.
     */
    @Operation(summary = "Undo dismiss offer (Admin only)")
    @PostMapping("/offers/{offerId}/undo-dismiss")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> undoDismissOffer(@PathVariable Long offerId) {

        StoreOffer offer = storeOfferRepository.findById(offerId).orElse(null);
        if (offer == null) {
            return ResponseEntity.notFound().build();
        }

        offer.setDismissed(false);
        offer.setDismissedAt(null);
        storeOfferRepository.save(offer);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("offerId", offerId);
        response.put("dismissed", false);
        return ResponseEntity.ok(response);
    }
}
