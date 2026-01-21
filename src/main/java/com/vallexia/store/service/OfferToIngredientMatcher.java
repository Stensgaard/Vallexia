package com.vallexia.store.service;

import com.vallexia.recipe.entity.Ingredient;
import com.vallexia.recipe.entity.IngredientTranslation;
import com.vallexia.recipe.integration.client.GoogleTranslationClient;
import com.vallexia.recipe.integration.exception.GoogleTranslationException;
import com.vallexia.recipe.repository.IngredientTranslationRepository;
import com.vallexia.store.entity.IngredientAlias;
import com.vallexia.store.entity.StoreOffer;
import com.vallexia.store.entity.StoreOfferIngredientMatch;
import com.vallexia.store.repository.IngredientAliasRepository;
import com.vallexia.store.repository.StoreOfferIngredientMatchRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Matches one scraped offer to exactly one canonical Ingredient.
 *
 * <p>Strategy: exact translation match -> exact alias match -> translate-if-needed -> simple fuzzy fallback.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OfferToIngredientMatcher {

  public static final String METHOD_EXACT_TRANSLATION = "EXACT_TRANSLATION";
  public static final String METHOD_EXACT_ALIAS = "EXACT_ALIAS";
  public static final String METHOD_TRANSLATED_EXACT = "TRANSLATED_EXACT";
  public static final String METHOD_TRANSLATED_ALIAS = "TRANSLATED_ALIAS";
  public static final String METHOD_FUZZY = "FUZZY";
  public static final String METHOD_MANUAL = "MANUAL";

  private final OfferNormalizationService normalizationService;
  private final IngredientTranslationRepository ingredientTranslationRepository;
  private final IngredientAliasRepository ingredientAliasRepository;
  private final StoreOfferIngredientMatchRepository matchRepository;
  private final GoogleTranslationClient translationClient;

  @Transactional
  public Optional<StoreOfferIngredientMatch> matchAndPersist(StoreOffer offer, String offerLocale) {
    if (offer == null || offer.getId() == null) {
      return Optional.empty();
    }
    if (offer.getProductName() == null || offer.getProductName().isBlank()) {
      return Optional.empty();
    }

    // Do not overwrite manual matches.
    Optional<StoreOfferIngredientMatch> existing = matchRepository.findByOfferIdFetchAll(offer.getId());
    if (existing.isPresent() && METHOD_MANUAL.equals(existing.get().getMatchMethod())) {
      return existing;
    }

    String locale = normalizeLocale(offerLocale);
    String normalized = normalizationService.normalizeForMatching(offer.getProductName());
    if (normalized.isBlank()) {
      return Optional.empty();
    }

    Optional<MatchCandidate> candidate = tryExact(locale, normalized)
        .or(() -> tryAlias(locale, normalized))
        .or(() -> tryTranslateThenMatch(locale, normalized))
        .or(() -> tryFuzzy(locale, normalized));

    if (candidate.isEmpty()) {
      return Optional.empty();
    }

    StoreOfferIngredientMatch match = existing.orElseGet(StoreOfferIngredientMatch::new);
    match.setOffer(offer);
    match.setIngredient(candidate.get().ingredient());
    match.setLocale(candidate.get().locale());
    match.setMatchMethod(candidate.get().method());
    match.setConfidence(BigDecimal.valueOf(candidate.get().confidence()));
    match.setMatchedText(candidate.get().matchedText());

    return Optional.of(matchRepository.save(match));
  }

  private Optional<MatchCandidate> tryExact(String locale, String normalized) {
    Optional<IngredientTranslation> t =
        ingredientTranslationRepository.findByLocaleAndNameIgnoreCase(locale, normalized);
    return t.map(tr -> new MatchCandidate(tr.getIngredient(), locale, METHOD_EXACT_TRANSLATION, 1.0, normalized));
  }

  private Optional<MatchCandidate> tryAlias(String locale, String normalized) {
    Optional<IngredientAlias> a =
        ingredientAliasRepository.findBestByLocaleAndAliasIgnoreCase(locale, normalized);
    return a.map(alias -> new MatchCandidate(alias.getIngredient(), locale, METHOD_EXACT_ALIAS, 0.98, normalized));
  }

  private Optional<MatchCandidate> tryTranslateThenMatch(String locale, String normalized) {
    String other = switchLocale(locale);
    if (other == null) {
      return Optional.empty();
    }

    try {
      String translated = translationClient.translateText(normalized, locale, other);
      String translatedNormalized = normalizationService.normalizeForMatching(translated);
      if (translatedNormalized.isBlank()) {
        return Optional.empty();
      }

      Optional<IngredientTranslation> t =
          ingredientTranslationRepository.findByLocaleAndNameIgnoreCase(other, translatedNormalized);
      if (t.isPresent()) {
        return Optional.of(new MatchCandidate(
            t.get().getIngredient(), other, METHOD_TRANSLATED_EXACT, 0.90, translatedNormalized));
      }

      Optional<IngredientAlias> a =
          ingredientAliasRepository.findBestByLocaleAndAliasIgnoreCase(other, translatedNormalized);
      return a.map(alias -> new MatchCandidate(
          alias.getIngredient(), other, METHOD_TRANSLATED_ALIAS, 0.88, translatedNormalized));
    } catch (GoogleTranslationException e) {
      log.debug("Translation failed for offer match (locale {}): {}", locale, e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Very simple fuzzy fallback using token containment + Jaro-Winkler on candidates.
   *
   * <p>This is intentionally conservative and can be improved later.</p>
   */
  private Optional<MatchCandidate> tryFuzzy(String locale, String normalized) {
    String key = normalizationService.toMatchKey(normalized);
    if (key.isBlank()) {
      return Optional.empty();
    }

    String needle = firstToken(key);
    if (needle == null || needle.length() < 3) {
      return Optional.empty();
    }

    // Candidate generation: scan current ingredients by simple contains.
    // We intentionally keep the candidate set small.
    List<IngredientTranslation> candidates =
        ingredientTranslationRepository.findByLocaleAndNameContainsIgnoreCase(locale, needle);
    if (candidates.isEmpty()) {
      return Optional.empty();
    }

    return candidates.stream()
        .map(t -> {
          String candKey = normalizationService.toMatchKey(t.getName());
          double score = jaroWinkler(key, candKey);
          return new Scored(t, score);
        })
        .max(Comparator.comparingDouble(Scored::score))
        .filter(s -> s.score() >= 0.93)
        .map(best -> new MatchCandidate(
            best.translation().getIngredient(), locale, METHOD_FUZZY, best.score(), normalized));
  }

  private String normalizeLocale(String locale) {
    if (locale == null || locale.isBlank()) {
      return "en";
    }
    String l = locale.toLowerCase(Locale.ROOT);
    // accept da-DK style
    if (l.contains("-")) {
      l = l.substring(0, l.indexOf('-'));
    }
    return l;
  }

  private String switchLocale(String locale) {
    if ("da".equals(locale)) {
      return "en";
    }
    if ("en".equals(locale)) {
      return "da";
    }
    return null;
  }

  private String firstToken(String key) {
    String[] parts = key.split("\\s+");
    return parts.length > 0 ? parts[0] : null;
  }

  // Jaro-Winkler implementation (small, dependency-free)
  private double jaroWinkler(String s1, String s2) {
    if (s1 == null || s2 == null) {
      return 0.0;
    }
    if (s1.equals(s2)) {
      return 1.0;
    }
    int s1Len = s1.length();
    int s2Len = s2.length();
    if (s1Len == 0 || s2Len == 0) {
      return 0.0;
    }
    int matchDistance = Math.max(s1Len, s2Len) / 2 - 1;

    boolean[] s1Matches = new boolean[s1Len];
    boolean[] s2Matches = new boolean[s2Len];

    int matches = 0;
    for (int i = 0; i < s1Len; i++) {
      int start = Math.max(0, i - matchDistance);
      int end = Math.min(i + matchDistance + 1, s2Len);
      for (int j = start; j < end; j++) {
        if (s2Matches[j]) {
          continue;
        }
        if (s1.charAt(i) != s2.charAt(j)) {
          continue;
        }
        s1Matches[i] = true;
        s2Matches[j] = true;
        matches++;
        break;
      }
    }
    if (matches == 0) {
      return 0.0;
    }

    double t = 0;
    int k = 0;
    for (int i = 0; i < s1Len; i++) {
      if (!s1Matches[i]) {
        continue;
      }
      while (!s2Matches[k]) {
        k++;
      }
      if (s1.charAt(i) != s2.charAt(k)) {
        t++;
      }
      k++;
    }
    t /= 2.0;

    double jaro = ((matches / (double) s1Len)
        + (matches / (double) s2Len)
        + ((matches - t) / matches)) / 3.0;

    // Winkler adjustment
    int prefix = 0;
    for (int i = 0; i < Math.min(4, Math.min(s1Len, s2Len)); i++) {
      if (s1.charAt(i) == s2.charAt(i)) {
        prefix++;
      } else {
        break;
      }
    }
    return jaro + (prefix * 0.1 * (1 - jaro));
  }

  private record MatchCandidate(
      Ingredient ingredient, String locale, String method, double confidence, String matchedText) {}

  private record Scored(IngredientTranslation translation, double score) {}
}

