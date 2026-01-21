package com.vallexia.store.scrape.text;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for extracting and normalizing product names from OCR text.
 * Provides heuristics to derive meaningful product names from noisy OCR output.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-01-XX
 */
public class OcrNameExtractor {
    
    private static final Pattern IPAPER_PRICE_MARKER =
        Pattern.compile("\\b\\d{1,4}(?:[\\.,]\\d{1,2})?\\s*(?:kr\\b|,-|\\.-)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern LEADING_CODE_PATTERN = Pattern.compile("^\\d{5,}\\s+");
    
    /**
     * Derive product name by finding a product code near the price.
     * 
     * @param fullText the full OCR text
     * @param priceStart the start position of the price marker
     * @param priceEnd the end position of the price marker
     * @return the extracted product name, or empty string if not found
     */
    public static String deriveIpaperNameByProductCode(String fullText, int priceStart, int priceEnd) {
        if (fullText == null) {
            return "";
        }
        String text = fullText.replace("\n", " ").replace("\r", " ").replaceAll("\\s+", " ").trim();
        if (text.isBlank()) {
            return "";
        }

        int afterStart = Math.min(Math.max(priceEnd, 0), text.length());
        int afterEnd = Math.min(text.length(), afterStart + 220);
        String afterWindow = text.substring(afterStart, afterEnd);

        Matcher afterCode = Pattern.compile("\\b\\d{5,}\\b").matcher(afterWindow);
        if (afterCode.find()) {
            int codeAbsEnd = afterStart + afterCode.end();
            String candidate = text.substring(codeAbsEnd, Math.min(text.length(), codeAbsEnd + 180)).trim();
            candidate = cleanupOcrName(candidate);
            candidate = cutAtStopTokens(candidate);
            return cleanupOcrName(candidate);
        }

        int beforeEnd = Math.min(Math.max(priceStart, 0), text.length());
        int beforeStart = Math.max(0, beforeEnd - 220);
        String beforeWindow = text.substring(beforeStart, beforeEnd);
        Matcher beforeCode = Pattern.compile("\\b\\d{5,}\\b").matcher(beforeWindow);
        int lastCodeEnd = -1;
        while (beforeCode.find()) {
            lastCodeEnd = beforeStart + beforeCode.end();
        }
        if (lastCodeEnd >= 0 && lastCodeEnd < beforeEnd) {
            String candidate = text.substring(lastCodeEnd, beforeEnd).trim();
            candidate = cleanupOcrName(candidate);
            candidate = cutAtStopTokens(candidate);
            return cleanupOcrName(candidate);
        }

        return "";
    }
    
    /**
     * Derive product name from text after the price marker.
     * 
     * @param fullText the full OCR text
     * @param priceEnd the end position of the price marker
     * @return the extracted product name, or empty string if not found
     */
    public static String deriveIpaperNameAfter(String fullText, int priceEnd) {
        if (fullText == null) {
            return "";
        }
        int start = Math.min(Math.max(priceEnd, 0), fullText.length());
        int end = Math.min(fullText.length(), start + 260);
        String after = fullText.substring(start, end);
        after = after.replace("\n", " ").replace("\r", " ").replaceAll("\\s+", " ").trim();

        after = after.replaceAll("(?i)^\\s*(pr\\.?\\s*)?(kg|stk|st\\.|liter|l|ml)\\b", "").trim();
        after = after.replaceAll("(?i)^\\s*(pr\\.?\\s*)?(kg|stk|st\\.|liter|l|ml)\\b", "").trim();
        after = after.replaceAll("(?i)^\\s*\\d+(?:[\\.,]\\d+)?\\b", "").trim();
        after = after.replaceAll("(?i)^\\s*fr(it)?\\s+valg\\b", "").trim();

        Matcher codeMatcher = LEADING_CODE_PATTERN.matcher(after);
        if (codeMatcher.find()) {
            after = after.substring(codeMatcher.end()).trim();
        } else {
            Matcher anyCode = Pattern.compile("\\b\\d{5,}\\b").matcher(after);
            if (anyCode.find()) {
                after = after.substring(anyCode.end()).trim();
            }
        }

        Matcher nextPrice = IPAPER_PRICE_MARKER.matcher(after);
        if (nextPrice.find()) {
            after = after.substring(0, nextPrice.start()).trim();
        }

        return cleanupOcrName(after);
    }
    
    /**
     * Cut text at stop tokens (price markers, product codes, metadata).
     * 
     * @param text the text to process
     * @return the text cut at the first stop token
     */
    public static String cutAtStopTokens(String text) {
        if (text == null) {
            return "";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.isBlank()) {
            return normalized;
        }

        int cut = normalized.length();
        Matcher mPrice = IPAPER_PRICE_MARKER.matcher(normalized);
        if (mPrice.find()) {
            cut = Math.min(cut, mPrice.start());
        }
        Matcher mCode = Pattern.compile("\\b\\d{5,}\\b").matcher(normalized);
        if (mCode.find()) {
            cut = Math.min(cut, mCode.start());
        }
        int idxPr = normalized.toLowerCase().indexOf(" pr.");
        if (idxPr >= 0) {
            cut = Math.min(cut, idxPr);
        }
        int idxSide = normalized.toLowerCase().indexOf(" side ");
        if (idxSide >= 0) {
            cut = Math.min(cut, idxSide);
        }
        int idxAvi = normalized.toLowerCase().indexOf(" avisen");
        if (idxAvi >= 0) {
            cut = Math.min(cut, idxAvi);
        }

        normalized = normalized.substring(0, Math.max(0, cut)).trim();
        return normalized;
    }
    
    /**
     * Normalize an extracted name from iPaper OCR.
     * 
     * @param name the raw extracted name
     * @return the normalized name
     */
    public static String normalizeIpaperExtractedName(String name) {
        if (name == null) {
            return "";
        }
        String n = name.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
        String lower = n.toLowerCase();

        if (lower.startsWith("g. ")) {
            n = n.substring(3).trim();
            lower = n.toLowerCase();
        }
        if (lower.startsWith("kg ")) {
            n = n.substring(3).trim();
            lower = n.toLowerCase();
        }

        n = n.replaceAll("(?i)\\s+flere\\s+varianter\\.?$", "").trim();
        // OCR sometimes leaves a trailing single "a" token; treat it as noise.
        n = n.replaceAll("(?i)\\s+\\ba\\b$", "").trim();
        return n;
    }
    
    /**
     * Extract product name from a code segment (text before price).
     * 
     * @param beforePrice the text segment before the price marker
     * @return the extracted product name, or empty string if not found
     */
    public static String extractIpaperNameFromCodeSegment(String beforePrice) {
        if (beforePrice == null) {
            return "";
        }

        String text = beforePrice
            .replace('\u00A0', ' ')
            .replaceAll("\\s+", " ")
            .trim();
        if (text.isBlank()) {
            return "";
        }

        String lower = text.toLowerCase();
        if (lower.contains("søndag") || lower.contains("kosøndag")) {
            return "";
        }
        if (lower.matches("^\\d+(?:[\\.,]\\d+)?\\s*(kg|g|cl|ml|l|stk|st\\.)\\b.*")) {
            return "";
        }
        if (OfferFilters.looksLikeStoreInfo(lower)) {
            return "";
        }

        text = text.replaceAll("(?i)^\\s*(op\\s+til\\s+\\d+(?:[\\.,]\\d+)?)\\s*", "").trim();

        Pattern[] patterns = new Pattern[]{
            Pattern.compile("^(.+?)\\s+\\d+\\s*[-–]\\s*\\d+\\s*(kg|g|cl|ml|l)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^(.+?)\\s+\\d+(?:[\\.,]\\d+)?\\s*(kg|g|cl|ml|l)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^(.+?)\\s+\\d+\\s*(stk|st\\.|pcs?)\\b", Pattern.CASE_INSENSITIVE)
        };
        for (Pattern p : patterns) {
            Matcher m = p.matcher(text);
            if (m.find()) {
                String candidate = m.group(1).trim();
                candidate = candidate.replaceAll("[\\.,;:]+$", "").trim();
                if (candidate.length() >= 3) {
                    return candidate;
                }
            }
        }

        int prIdx = text.toLowerCase().indexOf(" pr.");
        if (prIdx > 3) {
            text = text.substring(0, prIdx).trim();
        }

        String[] parts = text.split("\\s+");
        if (parts.length > 12) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 12; i++) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(parts[i]);
            }
            text = sb.toString().trim();
        }

        String tokenBased = extractNameFromTokens(text);
        if (!tokenBased.isBlank()) {
            return tokenBased;
        }

        return text.replaceAll("[\\.,;:]+$", "").trim();
    }
    
    /**
     * Extract product name from tokens, skipping stop words and metadata.
     * 
     * @param text the text to extract from
     * @return the extracted product name, or empty string if not found
     */
    public static String extractNameFromTokens(String text) {
        if (text == null) {
            return "";
        }
        String[] tokens = text.replaceAll("\\s+", " ").trim().split("\\s+");
        if (tokens.length == 0) {
            return "";
        }

        Set<String> stop = Set.of(
            "pr", "pr.", "kg", "g", "stk", "stk.", "st", "st.", "pose", "pakke", "bakke",
            "op", "til", "max", "side", "frit", "rit", "valg", "plus", "pris", "spar"
        );

        int startIdx = -1;
        for (int i = 0; i < tokens.length; i++) {
            String raw = tokens[i];
            String cleaned = raw.replaceAll("[^\\p{L}0-9-]", "").toLowerCase();
            if (cleaned.isBlank()) {
                continue;
            }
            if (stop.contains(cleaned)) {
                continue;
            }
            String lettersOnly = cleaned.replaceAll("[^\\p{L}]+", "");
            if (lettersOnly.isBlank()) {
                continue;
            }
            if (lettersOnly.length() >= 3) {
                startIdx = i;
                break;
            }
            if (lettersOnly.length() == 2 && i + 1 < tokens.length) {
                String next = tokens[i + 1].replaceAll("[^\\p{L}0-9-]", "").toLowerCase();
                String nextLetters = next.replaceAll("[^\\p{L}]+", "");
                if (nextLetters.length() >= 3) {
                    startIdx = i;
                    break;
                }
            }
        }
        if (startIdx < 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int words = 0;
        for (int i = startIdx; i < tokens.length && words < 10; i++) {
            String raw = tokens[i];
            String cleaned = raw.replaceAll("[^\\p{L}0-9-]", "").toLowerCase();
            if (cleaned.isBlank()) {
                continue;
            }
            if (stop.contains(cleaned)) {
                break;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(raw.replaceAll("[\\.,;:]+$", ""));
            words++;
        }

        String result = sb.toString().replaceAll("[\\.,;:]+$", "").trim();
        // OCR sometimes leaves a trailing single "a" token; treat it as noise.
        result = result.replaceAll("(?i)\\s+\\ba\\b$", "").trim();
        return result;
    }
    
    /**
     * Find the last boundary (double space, period, or dash) in a text range.
     * 
     * @param text the text to search
     * @param fromInclusive the start position (inclusive)
     * @param toExclusive the end position (exclusive)
     * @return the position of the last boundary, or -1 if not found
     */
    public static int findLastBoundary(String text, int fromInclusive, int toExclusive) {
        if (text == null) {
            return -1;
        }
        int from = Math.max(0, fromInclusive);
        int to = Math.min(text.length(), toExclusive);
        if (from >= to) {
            return -1;
        }

        int idx = text.lastIndexOf("  ", to);
        if (idx >= from) {
            return idx;
        }
        idx = text.lastIndexOf(". ", to);
        if (idx >= from) {
            return idx;
        }
        idx = text.lastIndexOf(" - ", to);
        if (idx >= from) {
            return idx;
        }
        return -1;
    }
    
    /**
     * Clean up OCR-extracted name by removing metadata and limiting length.
     * 
     * @param namePart the raw name part
     * @return the cleaned name
     */
    public static String cleanupOcrName(String namePart) {
        if (namePart == null) {
            return "";
        }
        String name = namePart.replaceAll("\\s+", " ").trim();
        name = name.replaceAll("(?i)\\b(side)\\s*\\d+\\b", "").trim();
        String[] parts = name.split("\\s+");
        if (parts.length > 10) {
            StringBuilder sb = new StringBuilder();
            for (int i = parts.length - 10; i < parts.length; i++) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(parts[i]);
            }
            name = sb.toString().trim();
        }
        return name;
    }
    
    /**
     * Normalize OCR name for validation (used internally).
     * 
     * @param name the name to normalize
     * @return the normalized name
     */
    public static String normalizeOcrNameForValidation(String name) {
        if (name == null) {
            return "";
        }
        return name.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim().toLowerCase();
    }
}
