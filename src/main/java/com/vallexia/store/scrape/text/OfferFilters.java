package com.vallexia.store.scrape.text;

/**
 * Utility class for filtering offers based on text content.
 * Provides heuristics to identify food items vs non-food items,
 * store information, and invalid product names.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-01-XX
 */
public class OfferFilters {
    
    /**
     * Check if text is likely to represent a food item.
     * 
     * @param text the text to check
     * @return true if the text appears to be food-related
     */
    public static boolean isLikelyFoodText(String text) {
        if (text == null) {
            return false;
        }
        String t = text.toLowerCase();

        // Hard reject: typical non-food categories/keywords seen in flyer text.
        String[] nonFood = new String[]{
            "rengøring", "rengørings", "vask", "vaskemiddel", "affaldsposer", "køkkenrulle", "toiletpapir",
            "serviet", "lys", "strømpe", "strømpebuk", "undertøj", "tights",
            "shampoo", "balsam", "barber", "barberblade", "gillette", "gosh", "concealer", "øjen", "øjencreme",
            "makeup", "bryn", "got2b", "tv", "watch", "ipad", "legetøj", "twinmarkers", "marker", "pen",
            "opbevaringsboks", "smartstore", "motivationsflaske",
            "papkop", "paptallerk", "papkrus", "flag"
        };
        for (String k : nonFood) {
            if (t.contains(k)) {
                return false;
            }
        }

        // Marketing / site copy that should never become a product.
        // OCR sometimes inserts invisible characters; normalize to alphanumerics for matching.
        String normalized = t.replaceAll("[^a-z0-9]+", "");
        if (normalized.contains("bilkatogo") || normalized.contains("dagligvareronline")) {
            return false;
        }

        return true;
    }
    
    /**
     * Check if text looks like store information (opening hours, service center, etc.).
     * 
     * @param text the text to check
     * @return true if the text appears to be store information
     */
    public static boolean looksLikeStoreInfo(String text) {
        if (text == null) {
            return false;
        }
        String t = text.toLowerCase();
        String compact = t.replaceAll("[^a-z0-9æøå]+", "");
        // Also create an ASCII-ish version because OCR often drops Danish diacritics.
        String ascii = compact
            .replace("æ", "ae")
            .replace("ø", "oe")
            .replace("å", "aa");

        // Danish store info patterns seen in iPaper OCR.
        if (t.contains("åbent alle dage")
            || t.contains("abent alle dage")
            || t.contains("åbner kl")
            || t.contains("abner kl")
            || t.contains("åbningstider")
            || t.contains("abningstider")
            || t.contains("servicecenter")
            || t.contains("service center")
            || t.contains("servicecentret")
            || t.contains("servicecentrets")
            || t.contains("bageren")
            || t.contains("bilka.dk")
            || t.contains("bilka dk")
            || compact.contains("bilkadkservicecenter")
            || ascii.contains("bilkadkservicecenter")
            || compact.contains("bilkadk")
            || ascii.contains("bilkadk")) {
            return true;
        }

        // Generic "open hours" patterns.
        if (t.matches(".*\\b\\d{1,2}\\s*[-–]\\s*\\d{1,2}\\b.*")
            && (t.contains("åbent") || t.contains("abent") || t.contains("open"))) {
            return true;
        }

        // Even if OCR drops the "open" keyword, opening hours + bakery/servicecenter tokens are strong signals.
        if (t.matches(".*\\b\\d{1,2}\\s*[-–]\\s*\\d{1,2}\\b.*")
            && (t.contains("bageren") || t.contains("servicecenter") || t.contains("bilka.dk") || t.contains("bilka dk"))) {
            return true;
        }

        return false;
    }
    
    /**
     * Check if a product name extracted from iPaper OCR is invalid.
     * 
     * @param name the product name to check
     * @return true if the name should be rejected
     */
    public static boolean isBadIpaperName(String name) {
        if (name == null) {
            return true;
        }
        String n = normalizeOcrNameForValidation(name);
        if (n.isBlank()) {
            return true;
        }
        String lower = n.toLowerCase();
        String compact = lower.replaceAll("[^a-z0-9]+", "");
        String lettersOnly = lower.replaceAll("[^\\p{L}]+", "");
        // Obvious non-name tokens commonly seen in iPaper OCR around prices.
        if (lower.startsWith("kg") || lower.startsWith("pr.") || lower.startsWith("pr ")) {
            return true;
        }
        // Reject names that start with a number + unit (usually just the pack size).
        if (lower.matches("^\\d+(?:[\\.,]\\d+)?\\s*(kg|g|cl|ml|l|stk|st\\.)\\b.*")) {
            return true;
        }
        if (lower.contains("frit valg") || compact.contains("fritvalg")) {
            return true;
        }
        if (lettersOnly.equals("frit") || lettersOnly.equals("rit")) {
            return true;
        }
        // OCR noise token often seen in Bilka promos: "P 0 R 0 ..."
        if (compact.contains("p0r0")) {
            return true;
        }

        // Reject obvious generic filler names.
        if (lower.equals("plus pris") || lower.equals("pris") || lower.equals("normalpris") || lower.equals("spar")) {
            return true;
        }
        if (lower.equals("flere varianter") || lower.equals("flere varianter.")) {
            return true;
        }

        // Must contain at least one letter to be a plausible product name.
        if (!n.matches(".*\\p{L}.*")) {
            return true;
        }

        // Reject short unit/quantity-only phrases like "2 STK." or "550-700 g." or "70 cl."
        if (lower.matches("^\\d+\\s*(stk\\.?|st\\.?|pcs?\\b|g\\b|kg\\b|cl\\b|ml\\b|l\\b)\\.?$")) {
            return true;
        }
        if (lower.matches("^\\d+(?:[\\.,]\\d+)?\\s*(g|kg|cl|ml|l)\\.?$")) {
            return true;
        }
        if (lower.matches("^\\d+\\s*[-–]\\s*\\d+\\s*(g|kg|cl|ml|l)\\.?$")) {
            return true;
        }
        if (lower.matches("^\\d+\\s*(stk\\.?|st\\.?|pcs?)\\s*$")) {
            return true;
        }
        if (lower.matches("^\\d+\\s*(x|pak(?:ke)?r?)\\b.*")) {
            return true;
        }

        // Packaging/unit-only tokens frequently mis-identified as names.
        String[] unitish = new String[]{
            "bakke", "pose", "pakke", "stk", "stk.", "kg", "g", "l", "ml", "pr", "pr.", "max.", "op til"
        };
        if (n.length() <= 8) {
            for (String u : unitish) {
                if (lower.equals(u) || lower.startsWith(u + " ")) {
                    return true;
                }
            }
        }

        // Common OCR "meta"/marketing copy that should never become a product name.
        String[] meta = new String[]{
            "priserne gælder", "vi tager forbehold", "trykfejl", "bilka marketing", "læs mere",
            "gælder kun", "gælder fra", "app", "plus appen", "side ", "avisen", "kundeservice"
        };
        for (String k : meta) {
            if (lower.contains(k)) {
                return true;
            }
        }
        if (looksLikeStoreInfo(lower)) {
            return true;
        }
        // Promo boilerplate often parsed as "name" (OCR-split).
        if (compact.contains("optil") || compact.contains("kosondag") || compact.contains("sondag")
            || lower.contains("søndag") || lower.contains("kosøndag")) {
            return true;
        }
        // Purely numeric-ish or units-ish.
        if (n.matches("^[0-9\\s\\.,-]+$")) {
            return true;
        }
        if (n.matches("^(?i)(kg|stk|st\\.|liter|l|ml)\\b.*")) {
            return true;
        }
        return false;
    }
    
    private static String normalizeOcrNameForValidation(String s) {
        if (s == null) {
            return "";
        }
        String n = s
            .replace('\u00A0', ' ') // NBSP
            .replaceAll("\\s+", " ")
            .trim();
        // Drop trailing punctuation noise.
        n = n.replaceAll("[\\.,;:]+$", "").trim();
        return n;
    }
}
