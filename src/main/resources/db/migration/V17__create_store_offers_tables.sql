-- Migration: Create store and store_offers tables for flyer scraping
-- Version: V17
-- Description: Store chain metadata, per-store scrape scheduling, and weekly offers scraped
--              from public flyer pages.

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
    scrape_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    scrape_cron VARCHAR(64) NOT NULL DEFAULT '0 0 2 * * MON',
    scrape_zone VARCHAR(64) NOT NULL DEFAULT 'Europe/Copenhagen',
    next_scrape_at TIMESTAMPTZ NULL,
    last_scraped_at TIMESTAMPTZ NULL,
    consecutive_failures INT NOT NULL DEFAULT 0,
    last_scrape_error TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE stores IS 'Store chain information for flyer scraping';
COMMENT ON COLUMN stores.name IS 'Store code (e.g., BILKA, NETTO, FOETEX)';
COMMENT ON COLUMN stores.flyer_url IS 'Public URL to the weekly flyer page';
COMMENT ON COLUMN stores.food_flyer_keywords IS 'Keywords to identify food flyer links on landing pages (multi-language support)';
COMMENT ON COLUMN stores.scrape_enabled IS 'Whether automated scraping is enabled for this store';
COMMENT ON COLUMN stores.scrape_cron IS 'Cron expression (Spring format) for next-scrape calculation';
COMMENT ON COLUMN stores.scrape_zone IS 'Time zone ID used for cron evaluation (e.g., Europe/Copenhagen)';
COMMENT ON COLUMN stores.next_scrape_at IS 'Next time this store should be scraped (computed from cron)';
COMMENT ON COLUMN stores.last_scraped_at IS 'Last time this store scraping was attempted';
COMMENT ON COLUMN stores.consecutive_failures IS 'Number of consecutive scraping failures for backoff';
COMMENT ON COLUMN stores.last_scrape_error IS 'Last scraping error message (for ops/debugging)';

-- ============================================================================
-- SHEDLOCK TABLE (distributed scheduler lock)
-- ============================================================================
-- Ensures scheduled jobs run only once across multiple backend instances.
-- ============================================================================
CREATE TABLE IF NOT EXISTS shedlock (
  name VARCHAR(64) NOT NULL,
  lock_until TIMESTAMPTZ NOT NULL,
  locked_at TIMESTAMPTZ NOT NULL,
  locked_by VARCHAR(255) NOT NULL,
  PRIMARY KEY (name)
);

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
    dismissed BOOLEAN NOT NULL DEFAULT FALSE,    -- Whether this offer has been dismissed by admin
    dismissed_at TIMESTAMPTZ NULL,               -- When the offer was dismissed
    CONSTRAINT fk_store_offers_store FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE,
    CONSTRAINT chk_store_offers_dates CHECK (valid_to >= valid_from)
);

COMMENT ON TABLE store_offers IS 'Weekly offers scraped from store public flyers';
COMMENT ON COLUMN store_offers.valid_from IS 'Offer valid-from date (typically Monday)';
COMMENT ON COLUMN store_offers.valid_to IS 'Offer valid-to date (typically Sunday)';
COMMENT ON COLUMN store_offers.dismissed IS 'Whether this offer has been dismissed by admin (excluded from matching/unmatched list)';
COMMENT ON COLUMN store_offers.dismissed_at IS 'Timestamp when the offer was dismissed';

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
    ('BILKA', 'Bilka', 'https://www.bilka.dk/tilbudsavis', 'https://www.bilka.dk', ARRAY['fødevarer']),
    ('FOETEX', 'Føtex', 'https://www.foetex.dk/tilbudsavis', 'https://www.foetex.dk', ARRAY['fødevarer']),
    ('NETTO', 'Netto', 'https://www.netto.dk/tilbudsavis', 'https://www.netto.dk', ARRAY['fødevarer']);
