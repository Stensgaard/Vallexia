-- Migration: Create store offer exclusion rules table
-- Version: V21
-- Description: Stores configurable text-based exclusion rules for filtering out low-value offers
--              (e.g., coffee/tea, soft drinks, alcohol, non-food items) during scraping.

CREATE TABLE store_offer_exclusion_rule (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    scope VARCHAR(20) NOT NULL DEFAULT 'GLOBAL' CHECK (scope IN ('GLOBAL', 'STORE')),
    store_name VARCHAR(100) NULL,
    match_type VARCHAR(20) NOT NULL CHECK (match_type IN ('WORD', 'CONTAINS', 'REGEX')),
    patterns TEXT[] NOT NULL,
    priority INTEGER NOT NULL DEFAULT 100,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_store_scope CHECK (
        (scope = 'STORE' AND store_name IS NOT NULL) OR
        (scope = 'GLOBAL' AND store_name IS NULL)
    )
);

CREATE INDEX idx_store_offer_exclusion_rule_lookup 
    ON store_offer_exclusion_rule(enabled, scope, store_name, priority);

COMMENT ON TABLE store_offer_exclusion_rule IS 'Configurable rules for excluding offers during scraping based on text patterns';
COMMENT ON COLUMN store_offer_exclusion_rule.name IS 'Human-readable rule name (e.g., "Exclude coffee/tea")';
COMMENT ON COLUMN store_offer_exclusion_rule.enabled IS 'Whether this rule is currently active';
COMMENT ON COLUMN store_offer_exclusion_rule.scope IS 'GLOBAL applies to all stores, STORE applies only to specific store_name';
COMMENT ON COLUMN store_offer_exclusion_rule.store_name IS 'Store name when scope=STORE (e.g., "NETTO")';
COMMENT ON COLUMN store_offer_exclusion_rule.match_type IS 'WORD: word-boundary safe, CONTAINS: substring, REGEX: pattern matching';
COMMENT ON COLUMN store_offer_exclusion_rule.patterns IS 'Array of text patterns to match against offer text';
COMMENT ON COLUMN store_offer_exclusion_rule.priority IS 'Lower priority rules are evaluated first (default 100)';

-- Seed initial global exclusion rules
INSERT INTO store_offer_exclusion_rule (name, enabled, scope, match_type, patterns, priority) VALUES
    ('Exclude coffee and tea', true, 'GLOBAL', 'WORD', ARRAY['kaffe', 'coffee', 'espresso', 'cappuccino', 'latte', 'te', 'tea', 'café', 'cafe'], 10),
    ('Exclude soft drinks and energy drinks', true, 'GLOBAL', 'WORD', ARRAY['sodavand', 'cola', 'pepsi', 'fanta', 'squash', 'monster', 'energy', 'energi', 'hydration drik', 'sportsdrik'], 20),
    ('Exclude alcohol', true, 'GLOBAL', 'WORD', ARRAY['vodka', 'rom', 'gin', 'whisky', 'whiskey', 'øl', 'ol', 'beer', 'vin', 'wine', 'champagne', 'cognac'], 30),
    ('Exclude personal care and hygiene', true, 'GLOBAL', 'CONTAINS', ARRAY['bleer', 'showergel', 'shampoo', 'balsam', 'barber', 'deodorant', 'tandpasta', 'toothpaste'], 40),
    ('Exclude home goods and electronics', true, 'GLOBAL', 'WORD', ARRAY['lampe', 'led', 'pære', 'solcellelampe', 'bordlampe', 'gulvlampe', 'væglampe'], 50),
    ('Exclude storage and organization', true, 'GLOBAL', 'WORD', ARRAY['opbevaring', 'kasse', 'kurv', 'bakke', 'opbevaringskasse', 'opbevaringskurv', 'opbevaringsbakker', 'opbevaringsbakke'], 60),
    ('Exclude furniture and home textiles', true, 'GLOBAL', 'WORD', ARRAY['madras', 'tæppe', 'varmetæppe', 'foldemadras', 'yogamåtte', 'massagepude'], 70),
    ('Exclude toys and games', true, 'GLOBAL', 'WORD', ARRAY['puslespil', 'malebog', 'tøjdyr', 'legetøj', 'dukke'], 80),
    ('Exclude office and stationery', true, 'GLOBAL', 'WORD', ARRAY['notesbog', 'tegneredskaber', 'pen', 'marker'], 90),
    ('Exclude fitness and wellness', true, 'GLOBAL', 'WORD', ARRAY['massagepistol', 'massagepude', 'yogablok', 'yogamåtte', 'triggerbold', 'stænger', 'hulten bånd'], 100),
    ('Exclude clothing', true, 'GLOBAL', 'WORD', ARRAY['bukser', 'tøj', 'top', 'badeponcho'], 110),
    ('Exclude miscellaneous non-food', true, 'GLOBAL', 'CONTAINS', ARRAY['tubetørklæde', 'plastkasse', 'plakat', 'køleskabsopbevaring', 'osram'], 120);
