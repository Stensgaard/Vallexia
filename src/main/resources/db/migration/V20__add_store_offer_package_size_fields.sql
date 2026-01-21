-- Migration: Add package size fields to store_offers
-- Version: V20
-- Description: Stores extracted package size information (e.g., 400 g, 1.4 kg, 1.6-2.8 kg)
--              for later scoring/comparison and improved admin debugging.

ALTER TABLE store_offers
  ADD COLUMN IF NOT EXISTS package_qty_min DECIMAL(10,3) NULL,
  ADD COLUMN IF NOT EXISTS package_qty_max DECIMAL(10,3) NULL,
  ADD COLUMN IF NOT EXISTS package_unit VARCHAR(10) NULL;

COMMENT ON COLUMN store_offers.package_qty_min IS 'Extracted package size minimum quantity (e.g., 0.4 or 1.6)';
COMMENT ON COLUMN store_offers.package_qty_max IS 'Extracted package size maximum quantity for ranges (e.g., 2.8)';
COMMENT ON COLUMN store_offers.package_unit IS 'Extracted package size unit (e.g., g, kg, ml, cl, l)';

