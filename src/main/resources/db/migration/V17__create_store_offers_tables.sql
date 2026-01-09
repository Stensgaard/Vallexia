-- Migration: Create store and store_offers tables for flyer scraping
-- Version: V17
-- Description: Store chain metadata and weekly offers scraped from public flyer pages.

-- ============================================================================
-- STORES TABLE
-- ============================================================================
-- Stores store chain information used by the scraping service.
-- ============================================================================
CREATE TABLE stores (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,           -- Internal name, e.g., BILKA, NETTO, FOETEX
    display_name VARCHAR(100) NOT NULL,         -- Human-friendly name
    flyer_url VARCHAR(255) NOT NULL,            -- Public flyer URL to scrape
    website_url VARCHAR(255),                   -- Base website URL (for resolving relative assets)
    food_flyer_keywords TEXT[],                 -- Keywords to identify food flyer links on landing pages
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE stores IS 'Store chain information for flyer scraping';
COMMENT ON COLUMN stores.name IS 'Store code (e.g., BILKA, NETTO, FOETEX)';
COMMENT ON COLUMN stores.flyer_url IS 'Public URL to the weekly flyer page';
COMMENT ON COLUMN stores.food_flyer_keywords IS 'Keywords to identify food flyer links on landing pages (multi-language support)';

-- ============================================================================
-- STORE OFFERS TABLE
-- ============================================================================
-- Stores weekly offers scraped from each store''s public flyer.
-- ============================================================================
CREATE TABLE store_offers (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2),                        -- Normalized offer price (API field: price)
    valid_from DATE NOT NULL,
    valid_to DATE NOT NULL,
    scraped_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_store_offers_store FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE,
    CONSTRAINT chk_store_offers_dates CHECK (valid_to >= valid_from)
);

COMMENT ON TABLE store_offers IS 'Weekly offers scraped from store public flyers';
COMMENT ON COLUMN store_offers.valid_from IS 'Offer valid-from date (typically Monday)';
COMMENT ON COLUMN store_offers.valid_to IS 'Offer valid-to date (typically Sunday)';

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================
CREATE INDEX idx_store_offers_store_id ON store_offers(store_id);
CREATE INDEX idx_store_offers_valid_dates ON store_offers(valid_from, valid_to);
CREATE INDEX idx_store_offers_product_name ON store_offers USING gin (to_tsvector('english', product_name));

-- ============================================================================
-- SEED INITIAL STORE DATA
-- ============================================================================
INSERT INTO stores (name, display_name, flyer_url, website_url, food_flyer_keywords)
VALUES
    ('BILKA', 'Bilka', 'https://www.bilka.dk/tilbudsavis', 'https://www.bilka.dk', ARRAY['food', 'fødevarer', 'groceries', 'grocery']),
    ('FOETEX', 'Føtex', 'https://www.foetex.dk/tilbudsavis', 'https://www.foetex.dk', ARRAY['food', 'fødevarer', 'groceries', 'grocery']),
    ('NETTO', 'Netto', 'https://www.netto.dk/tilbudsavis', 'https://www.netto.dk', ARRAY['food', 'fødevarer', 'groceries', 'grocery']);
