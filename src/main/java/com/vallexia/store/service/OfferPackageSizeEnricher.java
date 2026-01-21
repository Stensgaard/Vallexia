package com.vallexia.store.service;

import com.vallexia.store.entity.StoreOffer;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Extracts package size (e.g. "400 g", "1.4 kg", "1,6-2,8 kg") from offer product text.
 *
 * <p>This is intentionally heuristic: we store the best-effort parsed size so we can show it in
 * admin UI and use it later in scoring (e.g. cost per kg).</p>
 */
@Service
public class OfferPackageSizeEnricher {

  private static final Pattern RANGE =
      Pattern.compile(
          "(?i)\\b(\\d+(?:[\\.,]\\d+)?)\\s*[-–]\\s*(\\d+(?:[\\.,]\\d+)?)\\s*(kg|g|gram|l|liter|ml|cl)\\b");

  // Some OCR strings drop the unit but keep the range (e.g. "1,6-2,8" at end of name).
  // We still store qtyMin/qtyMax so it can be shown in admin UI.
  private static final Pattern RANGE_NO_UNIT_AT_END =
      Pattern.compile("(?i)\\b(\\d+(?:[\\.,]\\d+)?)\\s*[-–]\\s*(\\d+(?:[\\.,]\\d+)?)\\b\\s*$");

  private static final Pattern SINGLE =
      Pattern.compile(
          "(?i)\\b(\\d+(?:[\\.,]\\d+)?)\\s*(kg|g|gram|l|liter|ml|cl)\\b");

  // Counts like "8 stk." (eggs), "30-36 stk." (diapers), "3-pak.".
  // We treat these as package size/count (NOT multi-buy min purchase).
  private static final Pattern COUNT_STK =
      Pattern.compile("(?i)\\b(\\d{1,3})\\s*(stk\\.?|styk(?:ker)?)\\b");

  private static final Pattern COUNT_PAK =
      Pattern.compile("(?i)\\b(\\d{1,2})\\s*[-–]?\\s*(?:pak\\.?|pakke(?:r)?)\\b");

  public void enrich(StoreOffer offer) {
    if (offer == null) {
      return;
    }
    String text = offer.getProductName();
    String rawSnippet = offer.getRawPriceText();
    if ((text == null || text.isBlank()) && (rawSnippet == null || rawSnippet.isBlank())) {
      return;
    }
    String combined = ((text == null ? "" : text) + " " + (rawSnippet == null ? "" : rawSnippet)).trim();
    if (combined.isBlank()) {
      return;
    }

    // If offer is eggs, prefer a count (e.g. "8 stk") over weight/volume.
    boolean isEgg = (text == null ? "" : text).toLowerCase(Locale.ROOT).contains("æg")
        || (text == null ? "" : text).toLowerCase(Locale.ROOT).contains("egg");

    // First: try to capture explicit counts when relevant (eggs) or when no weight/volume exists.
    if (isEgg || offer.getPackageQtyMin() == null) {
      Matcher c = COUNT_STK.matcher(combined);
      if (c.find()) {
        BigDecimal qty = parseNumber(c.group(1));
        if (qty != null) {
          offer.setPackageQtyMin(qty);
          offer.setPackageQtyMax(null);
          offer.setPackageUnit("stk");
          return;
        }
      }

      Matcher p = COUNT_PAK.matcher(combined);
      if (p.find()) {
        BigDecimal qty = parseNumber(p.group(1));
        if (qty != null) {
          offer.setPackageQtyMin(qty);
          offer.setPackageQtyMax(null);
          offer.setPackageUnit("stk");
          return;
        }
      }
    }

    // Prefer ranges (1.6-2.8 kg) if present.
    Matcher r = RANGE.matcher(combined);
    if (r.find()) {
      BigDecimal min = parseNumber(r.group(1));
      BigDecimal max = parseNumber(r.group(2));
      String unit = normalizeUnit(r.group(3));
      if (min != null && unit != null) {
        offer.setPackageQtyMin(min);
        offer.setPackageQtyMax(max);
        offer.setPackageUnit(unit);
      }
      return;
    }

    // Range without unit at end of string (best-effort).
    Matcher rn = RANGE_NO_UNIT_AT_END.matcher(combined);
    if (rn.find()) {
      BigDecimal min = parseNumber(rn.group(1));
      BigDecimal max = parseNumber(rn.group(2));
      if (min != null && max != null) {
        offer.setPackageQtyMin(min);
        offer.setPackageQtyMax(max);
        offer.setPackageUnit(null);
      }
      return;
    }

    // Otherwise take the first single value (400 g, 1.4 kg, 250 ml, ...).
    Matcher s = SINGLE.matcher(combined);
    if (s.find()) {
      BigDecimal qty = parseNumber(s.group(1));
      String unit = normalizeUnit(s.group(2));
      if (qty != null && unit != null) {
        offer.setPackageQtyMin(qty);
        offer.setPackageQtyMax(null);
        offer.setPackageUnit(unit);
      }
    }
  }

  private BigDecimal parseNumber(String raw) {
    if (raw == null) {
      return null;
    }
    String n = raw.trim().replace(',', '.');
    if (n.isBlank()) {
      return null;
    }
    try {
      return new BigDecimal(n);
    } catch (Exception e) {
      return null;
    }
  }

  private String normalizeUnit(String raw) {
    if (raw == null) {
      return null;
    }
    String u = raw.trim().toLowerCase(Locale.ROOT);
    if (u.isBlank()) {
      return null;
    }
    if (u.equals("gram")) {
      return "g";
    }
    if (u.equals("liter")) {
      return "l";
    }
    return u;
  }
}

