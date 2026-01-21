package com.vallexia.store.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Splits flyer offer product names into multiple variants when an offer lists alternatives
 * (e.g. "kyllingebryst eller hakket kyllingekød", "grise-/kalvekød").
 *
 * <p>This is used to improve ingredient matching accuracy for recipe search, by producing one
 * offer row per variant.</p>
 */
@Component
public class OfferVariantSplitter {

  private static final Pattern OR_SPLIT = Pattern.compile("(?i)\\s+eller\\s+");
  // Match patterns like "grise-/kalvekød" or "okse-/kalvekød"
  private static final Pattern DASH_SLASH_WORD = Pattern.compile("(?i)\\b([\\p{L}]{2,})-\\s*/\\s*([\\p{L}]{2,})\\b");
  // Match patterns like "grise- og kalvekød" / "okse- og kalvekød"
  private static final Pattern DASH_OG_WORD =
      Pattern.compile("(?i)\\b([\\p{L}]{2,})-\\s+og\\s+([\\p{L}]{2,})\\b");
  private static final Pattern LEADING_DASH = Pattern.compile("^[\\-–—]+\\s*");
  // Split commas that are option separators, not decimals (requires whitespace + letter after comma).
  private static final Pattern COMMA_OPTION_SPLIT = Pattern.compile("\\s*,\\s+(?=\\p{L})");
  // Special-case: OCR sometimes drops the comma, producing "...kød dansk nakkefilet" (should be two options).
  private static final Pattern DANISH_NAKKEFILET = Pattern.compile("(?i)\\b(dansk\\s+nakkefilet)\\b");

  private static final Set<String> GENERIC_SINGLE_OPTIONS = Set.of(
      "grøntsager",
      "groentsager",
      "brød",
      "brod",
      "baguette",
      "baguettes"
  );

  private static final int MAX_VARIANTS_PER_OFFER = 4;

  // Suffixes where Danish flyers commonly use shorthand like "kyllingebrystfilet eller -inderfilet".
  // If the first variant ends with one of these, we can derive a better prefix for the dashed variant.
  private static final String[] DASH_SHORTHAND_SUFFIXES = new String[]{
      "brystfilet",
      "inderfilet",
      "lårfilet",
      "filet",
      "kød"
  };

  public List<String> splitVariants(String productName) {
    if (productName == null) {
      return List.of();
    }
    String raw = normalizeWhitespace(productName);
    if (raw.isBlank()) {
      return List.of();
    }

    Set<String> out = new LinkedHashSet<>();
    boolean sourceHasMultipleOptions = raw.toLowerCase(Locale.ROOT).contains(" eller ") || raw.contains(",");

    // First expand dash-slash word patterns inside the string.
    List<String> expanded = expandDashSlashWords(raw);
    // Then expand dash-og word patterns inside the string (common in meat: "grise- og kalvekød").
    expanded = expandDashOgWords(expanded);

    // Then split on "eller" at top-level.
    for (String s : expanded) {
      String[] parts = OR_SPLIT.split(s);
      if (parts.length <= 1) {
        emitVariantsFromPart(cleanVariant(s), cleanVariant(s), 0, sourceHasMultipleOptions, out);
        continue;
      }

      String base = cleanVariant(parts[0]);

      for (int i = 0; i < parts.length; i++) {
        emitVariantsFromPart(base, cleanVariant(parts[i]), i, sourceHasMultipleOptions, out);
        if (out.size() >= MAX_VARIANTS_PER_OFFER) {
          break;
        }
      }
      if (out.size() >= MAX_VARIANTS_PER_OFFER) {
        break;
      }
    }

    // If splitting produced nothing, fall back to original cleaned.
    if (out.isEmpty()) {
      out.add(cleanVariant(raw));
    }

    return new ArrayList<>(out);
  }

  private void emitVariantsFromPart(
      String base,
      String part,
      int index,
      boolean sourceHasMultipleOptions,
      Set<String> out) {

    if (part == null || part.isBlank()) {
      return;
    }

    // Handle "A, B ..." where comma separates options (avoid breaking decimals like "1,6").
    String[] commaParts = COMMA_OPTION_SPLIT.split(part);
    for (String cp0 : commaParts) {
      String cp = cleanVariant(cp0);
      if (cp.isBlank()) {
        continue;
      }

      // If OCR dropped the comma: split "... dansk nakkefilet" into [before, "dansk nakkefilet"].
      List<String> subParts = new ArrayList<>();
      Matcher dn = DANISH_NAKKEFILET.matcher(cp);
      if (dn.find() && dn.start() > 3 && !cp.contains(",")) {
        String before = cleanVariant(cp.substring(0, dn.start()));
        String nakkefilet = cleanVariant(dn.group(1));
        if (!before.isBlank()) {
          subParts.add(before);
        }
        if (!nakkefilet.isBlank()) {
          subParts.add(nakkefilet);
        }
      } else {
        subParts.add(cp);
      }

      for (String sub : subParts) {
        String v = cleanVariant(sub);
        if (v.isBlank()) {
          continue;
        }
        if (index > 0 && base != null && !base.isBlank()) {
          v = expandDashShorthand(base, v);
        }

        // Drop ultra-generic single-word options when the source clearly lists multiple options.
        if (sourceHasMultipleOptions) {
          String lower = v.toLowerCase(Locale.ROOT);
          if (GENERIC_SINGLE_OPTIONS.contains(lower)) {
            continue;
          }
        }

        if (!v.isBlank()) {
          out.add(v);
        }
        if (out.size() >= MAX_VARIANTS_PER_OFFER) {
          return;
        }
      }
    }
  }

  /**
   * Expand flyer shorthand like "X eller -Y" into a full alternative.
   *
   * <p>Example: "Kyllingebrystfilet eller -inderfilet" -> "Kyllingebrystfilet", "Kyllingeinderfilet"</p>
   */
  private String expandDashShorthand(String base, String variant) {
    if (variant == null) {
      return "";
    }
    String v = normalizeWhitespace(variant);
    if (v.isBlank()) {
      return "";
    }
    if (!LEADING_DASH.matcher(v).find()) {
      return v;
    }

    String alt = LEADING_DASH.matcher(v).replaceFirst("").trim();
    if (alt.isBlank()) {
      return "";
    }

    String prefix = base;
    boolean suffixTrimmed = false;
    String lowerBase = base.toLowerCase(Locale.ROOT);
    for (String suf : DASH_SHORTHAND_SUFFIXES) {
      if (lowerBase.endsWith(suf)) {
        prefix = base.substring(0, Math.max(0, base.length() - suf.length())).trim();
        suffixTrimmed = true;
        break;
      }
    }

    // If we couldn't trim a known suffix and base has multiple words, use the first word as context.
    if (!suffixTrimmed && prefix.contains(" ")) {
      prefix = prefix.substring(0, prefix.indexOf(' ')).trim();
    }

    if (prefix.isBlank()) {
      return alt;
    }

    // If we trimmed a suffix, Danish compounds usually join without a space ("kyllinge" + "inderfilet").
    // Otherwise be conservative and insert a space.
    String combined = suffixTrimmed ? (prefix + alt) : (prefix + " " + alt);
    return cleanVariant(combined);
  }

  private List<String> expandDashSlashWords(String s) {
    Matcher m = DASH_SLASH_WORD.matcher(s);
    if (!m.find()) {
      return List.of(s);
    }

    // Replace each occurrence with variants; keep it conservative by handling one occurrence at a time.
    List<String> current = List.of(s);
    m.reset();

    while (m.find()) {
      String left = m.group(1);
      String right = m.group(2);
      List<String> next = new ArrayList<>();

      for (String cur : current) {
        // Find occurrence again in current string
        Matcher mm = DASH_SLASH_WORD.matcher(cur);
        if (!mm.find()) {
          next.add(cur);
          continue;
        }
        String before = cur.substring(0, mm.start());
        String after = cur.substring(mm.end());

        String v1 = inferLeftVariant(left, right);
        String v2 = right;

        next.add(before + v1 + after);
        next.add(before + v2 + after);
      }

      current = next;
      if (current.size() > MAX_VARIANTS_PER_OFFER) {
        return current.subList(0, MAX_VARIANTS_PER_OFFER);
      }
    }

    return current;
  }

  private List<String> expandDashOgWords(List<String> inputs) {
    if (inputs == null || inputs.isEmpty()) {
      return inputs == null ? List.of() : inputs;
    }
    List<String> current = inputs;
    // Replace each occurrence with variants; keep it conservative by handling one occurrence at a time.
    boolean foundAny = false;
    for (String s : inputs) {
      if (s != null && DASH_OG_WORD.matcher(s).find()) {
        foundAny = true;
        break;
      }
    }
    if (!foundAny) {
      return inputs;
    }

    for (int pass = 0; pass < 2; pass++) {
      List<String> next = new ArrayList<>();
      boolean replaced = false;
      for (String cur : current) {
        if (cur == null) {
          continue;
        }
        Matcher mm = DASH_OG_WORD.matcher(cur);
        if (!mm.find()) {
          next.add(cur);
          continue;
        }
        replaced = true;
        String left = mm.group(1);
        String right = mm.group(2);
        String before = cur.substring(0, mm.start());
        String after = cur.substring(mm.end());

        String v1 = inferLeftVariant(left, right);
        String v2 = right;
        next.add(before + v1 + after);
        next.add(before + v2 + after);
      }
      current = next;
      if (!replaced || current.size() > MAX_VARIANTS_PER_OFFER) {
        break;
      }
    }

    if (current.size() > MAX_VARIANTS_PER_OFFER) {
      return current.subList(0, MAX_VARIANTS_PER_OFFER);
    }
    return current;
  }

  private String inferLeftVariant(String left, String right) {
    String l = left.trim();
    String r = right.trim();

    // Common Danish pattern: second word ends with "kød" / "filet" and first is a prefix.
    String lowerR = r.toLowerCase(Locale.ROOT);
    if (lowerR.endsWith("kød") && !l.toLowerCase(Locale.ROOT).endsWith("kød")) {
      return l + "kød";
    }
    if (lowerR.endsWith("filet") && !l.toLowerCase(Locale.ROOT).endsWith("filet")) {
      return l + "filet";
    }
    return l;
  }

  private String normalizeWhitespace(String s) {
    return s.replace('\u00AD', ' ') // soft hyphen sometimes appears in OCR
        .replace('\u00A0', ' ')
        .replaceAll("\\s+", " ")
        .trim();
  }

  private String cleanVariant(String s) {
    if (s == null) {
      return "";
    }
    String v = normalizeWhitespace(s);
    // Remove trailing punctuation that is usually formatting noise.
    v = v.replaceAll("[\\s,;:]+$", "").trim();
    return v;
  }
}

