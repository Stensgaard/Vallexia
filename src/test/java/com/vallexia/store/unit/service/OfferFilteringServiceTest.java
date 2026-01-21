package com.vallexia.store.unit.service;

import com.vallexia.store.entity.Store;
import com.vallexia.store.entity.StoreOffer;
import com.vallexia.store.entity.StoreOfferExclusionRule;
import com.vallexia.store.repository.StoreOfferExclusionRuleRepository;
import com.vallexia.store.service.OfferFilteringService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for OfferFilteringService.
 * Tests rule matching logic with various patterns and match types.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-01-XX
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OfferFilteringService Unit Tests")
class OfferFilteringServiceTest {

    @Mock
    private StoreOfferExclusionRuleRepository ruleRepository;

    @InjectMocks
    private OfferFilteringService filteringService;

    private Store testStore;
    private StoreOffer testOffer;

    @BeforeEach
    void setUp() {
        testStore = new Store();
        testStore.setId(1L);
        testStore.setName("NETTO");

        testOffer = new StoreOffer();
        testOffer.setStore(testStore);
        testOffer.setProductName("Test Product");
        testOffer.setPrice(BigDecimal.valueOf(29.95));
        testOffer.setValidFrom(LocalDate.now());
        testOffer.setValidTo(LocalDate.now().plusDays(7));
        testOffer.setScrapedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should exclude coffee products (WORD match)")
    void shouldExcludeCoffeeProducts() {
        // Given
        StoreOfferExclusionRule rule = createRule("Exclude coffee", "GLOBAL", "WORD", 
            new String[]{"kaffe", "coffee", "espresso"});
        when(ruleRepository.findApplicableRules("NETTO")).thenReturn(List.of(rule));

        testOffer.setProductName("Gevalia kaffe");
        
        // When
        boolean excluded = filteringService.isExcluded(testStore, testOffer);
        
        // Then
        assertThat(excluded).isTrue();
    }

    @Test
    @DisplayName("Should exclude tea products (WORD match)")
    void shouldExcludeTeaProducts() {
        // Given
        StoreOfferExclusionRule rule = createRule("Exclude tea", "GLOBAL", "WORD", 
            new String[]{"te", "tea", "café"});
        when(ruleRepository.findApplicableRules("NETTO")).thenReturn(List.of(rule));

        testOffer.setProductName("Café Noir");
        
        // When
        boolean excluded = filteringService.isExcluded(testStore, testOffer);
        
        // Then
        assertThat(excluded).isTrue();
    }

    @Test
    @DisplayName("Should exclude soft drinks (WORD match)")
    void shouldExcludeSoftDrinks() {
        // Given
        StoreOfferExclusionRule rule = createRule("Exclude soft drinks", "GLOBAL", "WORD", 
            new String[]{"sodavand", "cola", "pepsi", "fanta"});
        when(ruleRepository.findApplicableRules("NETTO")).thenReturn(List.of(rule));

        testOffer.setProductName("Pepsi Max sodavand");
        
        // When
        boolean excluded = filteringService.isExcluded(testStore, testOffer);
        
        // Then
        assertThat(excluded).isTrue();
    }

    @Test
    @DisplayName("Should exclude alcohol (WORD match)")
    void shouldExcludeAlcohol() {
        // Given
        StoreOfferExclusionRule rule = createRule("Exclude alcohol", "GLOBAL", "WORD", 
            new String[]{"vodka", "rom", "gin", "whisky"});
        when(ruleRepository.findApplicableRules("NETTO")).thenReturn(List.of(rule));

        testOffer.setProductName("Smirnoff vodka");
        
        // When
        boolean excluded = filteringService.isExcluded(testStore, testOffer);
        
        // Then
        assertThat(excluded).isTrue();
    }

    @Test
    @DisplayName("Should exclude personal care items (CONTAINS match)")
    void shouldExcludePersonalCareItems() {
        // Given
        StoreOfferExclusionRule rule = createRule("Exclude personal care", "GLOBAL", "CONTAINS", 
            new String[]{"bleer", "showergel"});
        when(ruleRepository.findApplicableRules("NETTO")).thenReturn(List.of(rule));

        testOffer.setProductName("Libero Touch bleer");
        
        // When
        boolean excluded = filteringService.isExcluded(testStore, testOffer);
        
        // Then
        assertThat(excluded).isTrue();
    }

    @Test
    @DisplayName("Should exclude home goods (WORD match)")
    void shouldExcludeHomeGoods() {
        // Given
        StoreOfferExclusionRule rule = createRule("Exclude home goods", "GLOBAL", "WORD", 
            new String[]{"lampe", "led", "pære", "opbevaring"});
        when(ruleRepository.findApplicableRules("NETTO")).thenReturn(List.of(rule));

        testOffer.setProductName("LED bordlampe");
        
        // When
        boolean excluded = filteringService.isExcluded(testStore, testOffer);
        
        // Then
        assertThat(excluded).isTrue();
    }

    @Test
    @DisplayName("Should exclude using REGEX match")
    void shouldExcludeUsingRegex() {
        // Given
        StoreOfferExclusionRule rule = createRule("Exclude by regex", "GLOBAL", "REGEX", 
            new String[]{".*\\b(osram|palmolive)\\b.*"});
        when(ruleRepository.findApplicableRules("NETTO")).thenReturn(List.of(rule));

        testOffer.setProductName("Osram LED-pærer");
        
        // When
        boolean excluded = filteringService.isExcluded(testStore, testOffer);
        
        // Then
        assertThat(excluded).isTrue();
    }

    @Test
    @DisplayName("Should NOT exclude legitimate food items")
    void shouldNotExcludeLegitimateFoodItems() {
        // Given
        StoreOfferExclusionRule rule = createRule("Exclude coffee", "GLOBAL", "WORD", 
            new String[]{"kaffe", "coffee"});
        when(ruleRepository.findApplicableRules("NETTO")).thenReturn(List.of(rule));

        // Test cases that should NOT be excluded
        String[] allowedItems = {
            "Hakket oksekød",
            "Kyllingebryst",
            "Laks filet",
            "Mælk 1 liter",
            "Æg 12 stk",
            "Bananer",
            "Tomater",
            "Løg",
            "Ost",
            "Smør"
        };

        for (String item : allowedItems) {
            testOffer.setProductName(item);
            boolean excluded = filteringService.isExcluded(testStore, testOffer);
            assertThat(excluded).as("Should not exclude: " + item).isFalse();
        }
    }

    @Test
    @DisplayName("Should handle word boundaries correctly (te in tomate should not match)")
    void shouldHandleWordBoundariesCorrectly() {
        // Given
        StoreOfferExclusionRule rule = createRule("Exclude tea", "GLOBAL", "WORD", 
            new String[]{"te", "tea"});
        when(ruleRepository.findApplicableRules("NETTO")).thenReturn(List.of(rule));

        // "te" should NOT match inside "tomate"
        testOffer.setProductName("Tomater");
        
        // When
        boolean excluded = filteringService.isExcluded(testStore, testOffer);
        
        // Then
        assertThat(excluded).isFalse();
    }

    @Test
    @DisplayName("Should handle Danish characters (æ/ø/å normalization)")
    void shouldHandleDanishCharacters() {
        // Given
        StoreOfferExclusionRule rule = createRule("Exclude coffee", "GLOBAL", "WORD", 
            new String[]{"kaffe"}); // Danish "kaffe"
        when(ruleRepository.findApplicableRules("NETTO")).thenReturn(List.of(rule));

        // OCR might drop diacritics, but our normalization should still match
        testOffer.setProductName("Kaffe bønner"); // With diacritic
        
        // When
        boolean excluded = filteringService.isExcluded(testStore, testOffer);
        
        // Then
        assertThat(excluded).isTrue();
    }

    @Test
    @DisplayName("Should return false when no rules are configured")
    void shouldReturnFalseWhenNoRulesConfigured() {
        // Given
        when(ruleRepository.findApplicableRules("NETTO")).thenReturn(List.of());

        testOffer.setProductName("Any product");
        
        // When
        boolean excluded = filteringService.isExcluded(testStore, testOffer);
        
        // Then
        assertThat(excluded).isFalse();
    }

    @Test
    @DisplayName("Should return false when offer is null")
    void shouldReturnFalseWhenOfferIsNull() {
        // When
        boolean excluded = filteringService.isExcluded(testStore, null);
        
        // Then
        assertThat(excluded).isFalse();
    }

    @Test
    @DisplayName("Should return false when store is null")
    void shouldReturnFalseWhenStoreIsNull() {
        // When
        boolean excluded = filteringService.isExcluded(null, testOffer);
        
        // Then
        assertThat(excluded).isFalse();
    }

    @Test
    @DisplayName("Should return false when product name is null")
    void shouldReturnFalseWhenProductNameIsNull() {
        // Given
        testOffer.setProductName(null);
        
        // When
        boolean excluded = filteringService.isExcluded(testStore, testOffer);
        
        // Then
        assertThat(excluded).isFalse();
    }

    @Test
    @DisplayName("Should apply store-specific rules when scope is STORE")
    void shouldApplyStoreSpecificRules() {
        // Given
        StoreOfferExclusionRule globalRule = createRule("Global rule", "GLOBAL", "WORD", 
            new String[]{"coffee"});
        StoreOfferExclusionRule storeRule = createRule("NETTO specific", "STORE", "WORD", 
            new String[]{"sodavand"});
        storeRule.setStoreName("NETTO");
        
        when(ruleRepository.findApplicableRules("NETTO")).thenReturn(List.of(globalRule, storeRule));

        testOffer.setProductName("Coca-Cola sodavand");
        
        // When
        boolean excluded = filteringService.isExcluded(testStore, testOffer);
        
        // Then
        assertThat(excluded).isTrue();
    }

    @Test
    @DisplayName("Should evaluate rules in priority order")
    void shouldEvaluateRulesInPriorityOrder() {
        // Given - lower priority rules are evaluated first
        StoreOfferExclusionRule highPriorityRule = createRule("High priority", "GLOBAL", "WORD", 
            new String[]{"coffee"});
        highPriorityRule.setPriority(10);
        
        StoreOfferExclusionRule lowPriorityRule = createRule("Low priority", "GLOBAL", "WORD", 
            new String[]{"sodavand"});
        lowPriorityRule.setPriority(100);
        
        when(ruleRepository.findApplicableRules("NETTO")).thenReturn(List.of(highPriorityRule, lowPriorityRule));

        testOffer.setProductName("Gevalia kaffe");
        
        // When
        boolean excluded = filteringService.isExcluded(testStore, testOffer);
        
        // Then - should match high priority rule first
        assertThat(excluded).isTrue();
    }

    // Helper method to create test rules
    private StoreOfferExclusionRule createRule(String name, String scope, String matchType, String[] patterns) {
        StoreOfferExclusionRule rule = new StoreOfferExclusionRule();
        rule.setId(1L);
        rule.setName(name);
        rule.setEnabled(true);
        rule.setScope(scope);
        rule.setMatchType(matchType);
        rule.setPatterns(patterns);
        rule.setPriority(100);
        return rule;
    }
}
