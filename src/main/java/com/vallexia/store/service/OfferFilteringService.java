package com.vallexia.store.service;

import com.vallexia.store.entity.Store;
import com.vallexia.store.entity.StoreOffer;
import com.vallexia.store.entity.StoreOfferExclusionRule;
import com.vallexia.store.repository.StoreOfferExclusionRuleRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for filtering store offers based on configurable exclusion rules.
 * Rules are loaded from the database and can be global or store-specific.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-01-XX
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OfferFilteringService {

    private final StoreOfferExclusionRuleRepository ruleRepository;

    // Cache compiled regex patterns to avoid recompiling on every check
    private final Map<String, Pattern> regexCache = new ConcurrentHashMap<>();

    // Per-run exclusion logging (to avoid log spam)
    private final ThreadLocal<Map<String, Integer>> exclusionCounts = ThreadLocal.withInitial(HashMap::new);
    private static final int MAX_EXCLUSIONS_LOGGED_PER_RULE = 3;

    /**
     * Check if an offer should be excluded based on configured rules.
     * 
     * @param store the store the offer belongs to
     * @param offer the offer to check
     * @return true if the offer should be excluded, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isExcluded(Store store, StoreOffer offer) {
        if (store == null || offer == null || offer.getProductName() == null) {
            return false;
        }

        String storeName = store.getName();
        List<StoreOfferExclusionRule> rules = ruleRepository.findApplicableRules(storeName);

        if (rules.isEmpty()) {
            return false;
        }

        // Normalize offer text once for all rules
        String normalized = normalizeOfferText(offer.getProductName());
        String asciiNormalized = toAsciiVariant(normalized);

        // Check each rule in priority order (lower priority first)
        for (StoreOfferExclusionRule rule : rules) {
            if (matchesRule(normalized, asciiNormalized, rule)) {
                logExclusion(rule, offer.getProductName());
                return true;
            }
        }

        return false;
    }

    /**
     * Normalize offer text for matching: lowercase, collapse whitespace, handle Danish characters.
     * 
     * @param text the original text
     * @return normalized text
     */
    private String normalizeOfferText(String text) {
        if (text == null) {
            return "";
        }
        return text
            .replace('\u00A0', ' ') // NBSP
            .toLowerCase()
            .replaceAll("\\s+", " ")
            .trim();
    }

    /**
     * Create ASCII variant for OCR resilience (æ/ø/å -> ae/oe/aa).
     * 
     * @param text normalized text
     * @return ASCII variant
     */
    private String toAsciiVariant(String text) {
        return text
            .replace("æ", "ae")
            .replace("ø", "oe")
            .replace("å", "aa");
    }

    /**
     * Check if normalized text matches a rule's patterns.
     * 
     * @param normalized the normalized text
     * @param asciiNormalized the ASCII variant
     * @param rule the rule to check
     * @return true if any pattern matches
     */
    private boolean matchesRule(String normalized, String asciiNormalized, StoreOfferExclusionRule rule) {
        String matchType = rule.getMatchType();
        String[] patterns = rule.getPatterns();

        if (patterns == null || patterns.length == 0) {
            return false;
        }

        for (String pattern : patterns) {
            if (pattern == null || pattern.isBlank()) {
                continue;
            }

            boolean matches = false;
            switch (matchType) {
                case "WORD":
                    matches = matchesWordBoundary(normalized, pattern) || matchesWordBoundary(asciiNormalized, pattern);
                    break;
                case "CONTAINS":
                    matches = normalized.contains(pattern.toLowerCase()) || asciiNormalized.contains(pattern.toLowerCase());
                    break;
                case "REGEX":
                    matches = matchesRegex(normalized, pattern) || matchesRegex(asciiNormalized, pattern);
                    break;
                default:
                    log.warn("Unknown match type: {} for rule: {}", matchType, rule.getName());
            }

            if (matches) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if text contains a word (word-boundary safe).
     * 
     * @param text the normalized text
     * @param word the word to find
     * @return true if word is found with word boundaries
     */
    private boolean matchesWordBoundary(String text, String word) {
        if (text == null || word == null || word.isBlank()) {
            return false;
        }
        String lowerWord = word.toLowerCase().trim();
        // Use word boundary regex to avoid false matches (e.g., "te" in "tomate")
        return text.matches(".*\\b" + Pattern.quote(lowerWord) + "\\b.*");
    }

    /**
     * Check if text matches a regex pattern (cached compilation).
     * 
     * @param text the normalized text
     * @param patternStr the regex pattern
     * @return true if pattern matches
     */
    private boolean matchesRegex(String text, String patternStr) {
        if (text == null || patternStr == null || patternStr.isBlank()) {
            return false;
        }

        Pattern pattern = regexCache.computeIfAbsent(patternStr, p -> {
            try {
                return Pattern.compile(p, Pattern.CASE_INSENSITIVE);
            } catch (Exception e) {
                log.warn("Invalid regex pattern: {} - {}", p, e.getMessage());
                return null;
            }
        });

        if (pattern == null) {
            return false;
        }

        return pattern.matcher(text).find();
    }

    /**
     * Log exclusion decision (with per-run caps to avoid spam).
     * 
     * @param rule the rule that matched
     * @param offerName the offer name that was excluded
     */
    private void logExclusion(StoreOfferExclusionRule rule, String offerName) {
        Map<String, Integer> counts = exclusionCounts.get();
        String key = rule.getId() + ":" + rule.getName();
        int count = counts.getOrDefault(key, 0);

        if (count < MAX_EXCLUSIONS_LOGGED_PER_RULE) {
            log.debug("Excluded offer '{}' by rule '{}' (id: {})", offerName, rule.getName(), rule.getId());
            counts.put(key, count + 1);
        }
    }

    /**
     * Reset per-run exclusion counters (call at start of scraping run).
     */
    public void resetExclusionCounters() {
        exclusionCounts.remove();
    }
}
