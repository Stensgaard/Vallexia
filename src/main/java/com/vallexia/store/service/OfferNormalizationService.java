package com.vallexia.store.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Normalizes scraped offer product names into a stable form for matching to ingredients.
 */
@Service
public class OfferNormalizationService {

  private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");
  private static final Pattern TRAILING_PUNCT = Pattern.compile("[\\p{Punct}]+$");
  private static final Pattern LEADING_MARKETING =
      Pattern.compile("^(?i)(spar|kun|tilbud|tilbudspris|frit\\s+valg)\\b\\s*");
  private static final Pattern UNIT_PHRASE =
      Pattern.compile("(?i)\\b(pr\\.?\\s*)?(kg|g|gram|l|liter|ml|cl|stk|st\\.|pcs?)\\b");
  private static final Pattern QUANTITY =
      Pattern.compile("(?i)\\b\\d+(?:[\\.,]\\d+)?\\s*(x\\s*)?\\b");

  /**
   * Normalize an offer name for matching.
   *
   * <p>Goal: remove common noise (marketing, units, excessive punctuation) while keeping the core
   * ingredient-like name.</p>
   */
  public String normalizeForMatching(String rawName) {
    if (rawName == null) {
      return "";
    }
    String s = rawName
        .replace('\u00A0', ' ')
        .replace('\n', ' ')
        .replace('\r', ' ')
        .trim();

    // Remove leading marketing copy.
    s = LEADING_MARKETING.matcher(s).replaceAll("");

    // Remove obvious unit/quantity fragments (kept conservative; can be expanded later).
    s = UNIT_PHRASE.matcher(s).replaceAll(" ");
    s = QUANTITY.matcher(s).replaceAll(" ");

    s = MULTI_SPACE.matcher(s).replaceAll(" ").trim();
    s = TRAILING_PUNCT.matcher(s).replaceAll("").trim();
    // OCR occasionally leaves a trailing single "a" token; treat it as noise.
    s = s.replaceAll("(?i)\\s+\\ba\\b$", "").trim();
    return s;
  }

  /**
   * Create a simplified key for case/diacritic-insensitive matching.
   */
  public String toMatchKey(String text) {
    String n = normalizeForMatching(text);
    if (n.isBlank()) {
      return "";
    }
    String deaccented = Normalizer.normalize(n, Normalizer.Form.NFD)
        .replaceAll("\\p{M}+", "");
    return deaccented.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]+", "").trim();
  }
}

