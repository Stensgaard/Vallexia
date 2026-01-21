package com.vallexia.store.service;

import com.vallexia.store.entity.StoreOffer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Enriches scraped offers with bundle/unit price information.
 *
 * <p>Detects patterns like \"2 stk\" or \"5 pakker\" and computes unitPrice from bundlePrice.</p>
 */
@Service
public class OfferPricingEnricher {

  // Multi-buy should look like "2 stk for 30" / "3 pk. til 50" etc.
  // Avoid parsing plain counts ("8 stk", "3-pak") as min-purchase; those are package counts.
  private static final Pattern MULTIBUY_QTY_UNIT_FOR =
      Pattern.compile("(?i)\\b(\\d{1,2})\\s*(stk\\.?|styk(?:ker)?|pakker?|pk\\.?)\\b\\s*(?:for|til)\\b");

  private static final Pattern PER_UNIT =
      Pattern.compile("(?i)\\bpr\\.?\\s*(stk\\.?|styk(?:ker)?|pakke(?:r)?|pk\\.?)\\b");

  public void enrich(StoreOffer offer) {
    if (offer == null) {
      return;
    }

    String name = offer.getProductName();
    String raw = offer.getRawPriceText();
    if ((name == null || name.isBlank()) && (raw == null || raw.isBlank())) {
      return;
    }

    // Use both name and raw snippet (Netto/Tjek often stores qty in description).
    String combined = ((name == null ? "" : name) + " " + (raw == null ? "" : raw)).trim();
    if (combined.isBlank()) {
      return;
    }

    // If the text explicitly says "pr. stk"/"pr. pakke", do not treat qty token as min-purchase.
    Matcher per = PER_UNIT.matcher(combined);
    boolean isPerUnit = per.find();

    Matcher m = MULTIBUY_QTY_UNIT_FOR.matcher(combined);
    if (!m.find()) {
      return;
    }

    if (isPerUnit) {
      return;
    }

    int qty;
    try {
      qty = Integer.parseInt(m.group(1));
    } catch (Exception e) {
      return;
    }
    if (qty <= 1) {
      return;
    }

    String unitRaw = m.group(2) != null ? m.group(2).trim() : "";
    String unit = normalizeUnit(unitRaw);

    offer.setMinPurchaseQty(qty);
    offer.setMinPurchaseUnit(unit);

    // For real multi-buy offers, treat price as the bundle price.
    if (offer.getPrice() != null && offer.getBundlePrice() == null) {
      offer.setBundlePrice(offer.getPrice());
    }

    BigDecimal bundle = offer.getBundlePrice();
    if (bundle != null && bundle.compareTo(BigDecimal.ZERO) > 0) {
      BigDecimal unitPrice = bundle.divide(BigDecimal.valueOf(qty), 2, RoundingMode.HALF_UP);
      offer.setUnitPrice(unitPrice);
    }
  }

  private String normalizeUnit(String unitRaw) {
    String u = unitRaw == null ? "" : unitRaw.toLowerCase(Locale.ROOT);
    u = u.replace(".", "");
    if (u.startsWith("styk") || u.startsWith("stk")) {
      return "stk";
    }
    if (u.startsWith("pak")) {
      return "pakke";
    }
    if (u.startsWith("pk")) {
      return "pakke";
    }
    return u;
  }
}

