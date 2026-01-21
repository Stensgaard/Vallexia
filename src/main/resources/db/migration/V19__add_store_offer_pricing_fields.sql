-- Migration: Add bundle/unit pricing fields to store_offers
-- Version: V19
-- Description: Stores minimum purchase requirements (e.g., "2 stk", "5 pakker")
--              and derived unit price for better meal scoring.

ALTER TABLE store_offers
  ADD COLUMN IF NOT EXISTS bundle_price DECIMAL(10,2) NULL,
  ADD COLUMN IF NOT EXISTS unit_price DECIMAL(10,2) NULL,
  ADD COLUMN IF NOT EXISTS min_purchase_qty INT NULL,
  ADD COLUMN IF NOT EXISTS min_purchase_unit VARCHAR(20) NULL,
  ADD COLUMN IF NOT EXISTS raw_price_text VARCHAR(255) NULL;

COMMENT ON COLUMN store_offers.bundle_price IS 'Flyer bundle price (may require buying multiple units)';
COMMENT ON COLUMN store_offers.unit_price IS 'Derived per-unit price when min_purchase_qty is known';
COMMENT ON COLUMN store_offers.min_purchase_qty IS 'Minimum purchase quantity for bundle price (e.g., 2)';
COMMENT ON COLUMN store_offers.min_purchase_unit IS 'Unit for minimum purchase (e.g., stk, pakke)';
COMMENT ON COLUMN store_offers.raw_price_text IS 'Original price text as seen on flyer page';

