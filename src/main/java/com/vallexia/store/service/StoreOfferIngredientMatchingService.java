package com.vallexia.store.service;

import com.vallexia.store.dto.DiscountedIngredientDto;
import com.vallexia.store.dto.UnmatchedStoreOfferDto;
import com.vallexia.store.entity.Store;
import com.vallexia.store.entity.StoreOffer;
import com.vallexia.store.entity.StoreOfferIngredientMatch;
import com.vallexia.store.repository.StoreOfferIngredientMatchRepository;
import com.vallexia.store.repository.StoreOfferRepository;
import com.vallexia.recipe.util.TranslationResolver;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates matching scraped store offers to canonical ingredients and exposes ingredient-deal queries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StoreOfferIngredientMatchingService {

  private static final Pattern TRAILING_SPACE_PUNCT = Pattern.compile("[\\s,;:]+$");
  private static final Pattern LEADING_WRAP = Pattern.compile("(?i)^i\\s+en\\s+wrap\\s+");
  private static final Pattern LEADING_MESTERHAKKET = Pattern.compile("(?i)^mesterhakket\\s+");

  private final StoreOfferRepository storeOfferRepository;
  private final StoreOfferIngredientMatchRepository matchRepository;
  private final OfferToIngredientMatcher matcher;
  private final OfferNormalizationService normalizationService;
  private final TranslationResolver translationResolver;

  /**
   * Match all current offers that don't yet have a stored offer->ingredient match.
   * Excludes dismissed offers.
   */
  @Transactional
  public int matchCurrentOffers(LocalDate date) {
    List<StoreOffer> offers = storeOfferRepository.findCurrentOffersWithoutIngredientMatch(date);
    int matched = 0;
    for (StoreOffer offer : offers) {
      String offerLocale = resolveOfferLocale(offer.getStore());
      Optional<StoreOfferIngredientMatch> match = matcher.matchAndPersist(offer, offerLocale);
      if (match.isPresent()) {
        matched++;
      }
    }
    if (matched > 0) {
      log.info("Matched {} offers to ingredients for date {}", matched, date);
    }
    return matched;
  }

  /**
   * List offers (valid on date) that do not have an ingredient match yet.
   * Excludes dismissed offers by default.
   */
  @Transactional(readOnly = true)
  public List<UnmatchedStoreOfferDto> getUnmatchedCurrentOffers(LocalDate date, Long storeId, boolean includeDismissed) {
    List<StoreOffer> offers;
    if (storeId == null) {
      offers = storeOfferRepository.findAllCurrentOffers(date);
    } else {
      offers = storeOfferRepository.findCurrentOffersByStore(storeId, date);
    }

    List<UnmatchedStoreOfferDto> result = offers.stream()
        .filter(o -> matchRepository.findByOfferIdFetchAll(o.getId()).isEmpty())
        .filter(o -> includeDismissed || !o.isDismissed())
        .map(o -> new UnmatchedStoreOfferDto(
            o.getId(),
            o.getStore().getId(),
            o.getStore().getDisplayName(),
            o.getProductName(),
            toDisplayProductName(o),
            normalizationService.normalizeForMatching(o.getProductName()),
            o.getPrice(),
            o.getBundlePrice(),
            o.getUnitPrice(),
            o.getMinPurchaseQty(),
            o.getMinPurchaseUnit(),
            o.getRawPriceText(),
            o.getPackageQtyMin(),
            o.getPackageQtyMax(),
            o.getPackageUnit(),
            o.getValidFrom(),
            o.getValidTo(),
            o.isDismissed(),
            o.getDismissedAt()))
        .collect(Collectors.toList());
    return result;
  }

  /**
   * List offers (valid on date) that do not have an ingredient match yet.
   * Excludes dismissed offers by default (backward compatibility).
   */
  @Transactional(readOnly = true)
  public List<UnmatchedStoreOfferDto> getUnmatchedCurrentOffers(LocalDate date, Long storeId) {
    return getUnmatchedCurrentOffers(date, storeId, false);
  }

  private String toDisplayProductName(StoreOffer o) {
    String name = o.getProductName();
    if (name == null || name.isBlank()) {
      return "";
    }
    String n = name.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();

    // iPaper OCR sometimes prepends context like "i en wrap" before the real product name.
    n = LEADING_WRAP.matcher(n).replaceFirst("").trim();
    // Marketing/brand qualifier that is rarely useful for ingredient-level matching.
    n = LEADING_MESTERHAKKET.matcher(n).replaceFirst("").trim();

    if (o.getPackageQtyMin() == null) {
      return n;
    }

    // If we extracted a size, remove trailing "400 g", "1,4 kg", "1,6-2,8 kg", or "1,6-2,8" (OCR missing unit).
    String unit = o.getPackageUnit();
    if (unit != null && !unit.isBlank()) {
      String u = Pattern.quote(unit.trim());
      n = n.replaceAll("(?i)\\s+\\d+(?:[\\.,]\\d+)?\\s*[-–]\\s*\\d+(?:[\\.,]\\d+)?\\s*" + u + "\\s*$", "");
      n = n.replaceAll("(?i)\\s+\\d+(?:[\\.,]\\d+)?\\s*" + u + "\\s*$", "");
    }

    // Also handle OCR where the unit was extracted from snippet (packageUnit), but not present in productName.
    if (o.getPackageQtyMax() != null) {
      n = n.replaceAll("(?i)\\s+\\d+(?:[\\.,]\\d+)?\\s*[-–]\\s*\\d+(?:[\\.,]\\d+)?\\s*$", "");
    }

    n = TRAILING_SPACE_PUNCT.matcher(n).replaceAll("").trim();
    return n.isBlank() ? name.trim() : n;
  }

  /**
   * Get discounted ingredients for a given date, optionally limited to storeIds.
   *
   * <p>Returns one entry per ingredient with the best (lowest) price offer.</p>
   */
  @Transactional(readOnly = true)
  public List<DiscountedIngredientDto> getDiscountedIngredients(
      LocalDate date,
      List<Long> storeIds,
      int limit,
      String userLocale) {

    List<StoreOfferIngredientMatch> matches = (storeIds == null || storeIds.isEmpty())
        ? matchRepository.findAllCurrentMatches(date)
        : matchRepository.findCurrentMatchesByStoreIds(date, storeIds);

    Map<Long, DiscountedIngredientDto> bestByIngredient = new HashMap<>();
    for (StoreOfferIngredientMatch m : matches) {
      StoreOffer offer = m.getOffer();
      if (offer.getPrice() == null) {
        continue;
      }
      Long ingredientId = m.getIngredient().getId();
      DiscountedIngredientDto existing = bestByIngredient.get(ingredientId);
      if (existing == null || offer.getPrice().compareTo(existing.getBestPrice()) < 0) {
        String ingredientName = translationResolver.resolveIngredientName(m.getIngredient(), userLocale);
        bestByIngredient.put(
            ingredientId,
            new DiscountedIngredientDto(
                ingredientId,
                ingredientName,
                offer.getPrice(),
                offer.getStore().getId(),
                offer.getStore().getDisplayName(),
                offer.getValidFrom(),
                offer.getValidTo()));
      }
    }

    return bestByIngredient.values().stream()
        .sorted(Comparator.comparing(DiscountedIngredientDto::getBestPrice, Comparator.nullsLast(BigDecimal::compareTo)))
        .limit(Math.max(1, limit))
        .collect(Collectors.toList());
  }

  private String resolveOfferLocale(Store store) {
    // Phase 1 default: Danish supermarkets. Extend later by storing locale on Store.
    return "da";
  }

  public String normalizeForAdminAlias(String raw) {
    return normalizationService.normalizeForMatching(raw);
  }

  public String normalizeLocale(String locale) {
    if (locale == null || locale.isBlank()) {
      return "en";
    }
    String l = locale.toLowerCase(Locale.ROOT);
    if (l.contains("-")) {
      l = l.substring(0, l.indexOf('-'));
    }
    return l;
  }
}

