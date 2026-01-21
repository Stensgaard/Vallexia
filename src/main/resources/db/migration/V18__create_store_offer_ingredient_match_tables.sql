-- Migration: Create tables for mapping store offers to canonical ingredients
-- Version: V18
-- Description: Adds 1:1 offer->ingredient matching with multilingual alias support.

-- ============================================================================
-- INGREDIENT ALIASES TABLE
-- ============================================================================
-- Stores known aliases/synonyms for ingredient names in specific locales.
-- This is used to match noisy flyer offer names to canonical ingredients reliably.
-- ============================================================================
CREATE TABLE ingredient_aliases (
    id BIGSERIAL PRIMARY KEY,
    ingredient_id BIGINT NOT NULL,
    locale VARCHAR(10) NOT NULL,
    alias VARCHAR(255) NOT NULL,
    priority INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ingredient_aliases_ingredient FOREIGN KEY (ingredient_id)
        REFERENCES ingredients(id) ON DELETE CASCADE,
    CONSTRAINT uk_ingredient_aliases_locale_alias UNIQUE (locale, alias)
);

COMMENT ON TABLE ingredient_aliases IS 'Aliases/synonyms for ingredient names by locale';
COMMENT ON COLUMN ingredient_aliases.locale IS 'Locale of this alias (e.g., da, en)';
COMMENT ON COLUMN ingredient_aliases.alias IS 'Alias text as it appears in flyers/search';
COMMENT ON COLUMN ingredient_aliases.priority IS 'Higher means preferred when multiple match';

CREATE INDEX idx_ingredient_aliases_ingredient_id ON ingredient_aliases(ingredient_id);
CREATE INDEX idx_ingredient_aliases_locale_alias_lower ON ingredient_aliases(locale, lower(alias));

-- ============================================================================
-- STORE OFFER -> INGREDIENT MATCH TABLE (1:1)
-- ============================================================================
-- Stores the chosen canonical ingredient for each scraped offer (one offer maps to exactly one ingredient).
-- ============================================================================
CREATE TABLE store_offer_ingredient_match (
    id BIGSERIAL PRIMARY KEY,
    offer_id BIGINT NOT NULL,
    ingredient_id BIGINT NOT NULL,
    locale VARCHAR(10) NOT NULL,
    match_method VARCHAR(30) NOT NULL,
    confidence DECIMAL(5,4) NOT NULL,
    matched_text VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_match_offer FOREIGN KEY (offer_id)
        REFERENCES store_offers(id) ON DELETE CASCADE,
    CONSTRAINT fk_match_ingredient FOREIGN KEY (ingredient_id)
        REFERENCES ingredients(id) ON DELETE RESTRICT,
    CONSTRAINT uk_match_offer UNIQUE (offer_id)
);

COMMENT ON TABLE store_offer_ingredient_match IS '1:1 mapping from scraped store offers to canonical ingredients';
COMMENT ON COLUMN store_offer_ingredient_match.match_method IS 'EXACT_TRANSLATION, EXACT_ALIAS, TRANSLATED_EXACT, TRANSLATED_ALIAS, FUZZY, MANUAL';
COMMENT ON COLUMN store_offer_ingredient_match.confidence IS '0..1 confidence score';

CREATE INDEX idx_match_ingredient_id ON store_offer_ingredient_match(ingredient_id);
CREATE INDEX idx_match_offer_id ON store_offer_ingredient_match(offer_id);

