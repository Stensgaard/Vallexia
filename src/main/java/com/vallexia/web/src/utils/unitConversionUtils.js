/**
 * Unit conversion utilities.
 * All functions delegate to the API service to avoid duplication with backend.
 * 
 * @module unitConversionUtils
 */

import { unitConversionService } from '@/services/unitConversionService'

/**
 * Convert weight value from one unit to another.
 * Uses API service with caching for performance.
 * 
 * @param {number} value - The value to convert
 * @param {string} fromUnit - Source unit (e.g., 'g', 'kg', 'oz', 'lb')
 * @param {string} toUnit - Target unit (e.g., 'g', 'kg', 'oz', 'lb')
 * @returns {Promise<number>} Converted value
 */
export async function convertWeight(value, fromUnit, toUnit) {
  if (value == null || value === undefined) {
    return value
  }

  if (!fromUnit || !toUnit) {
    return value
  }

  // If units are the same, return as-is
  if (fromUnit.toLowerCase() === toUnit.toLowerCase()) {
    return value
  }

  try {
    return await unitConversionService.convertWeight(value, fromUnit, toUnit)
  } catch (error) {
    console.error('Weight conversion failed:', error)
    // Return original value as fallback
    return value
  }
}

/**
 * Get appropriate display unit based on measurement system.
 * Uses API to avoid duplication with backend logic.
 * 
 * @param {string} unit - The unit to get display unit for
 * @param {string} measurementSystem - Measurement system ('METRIC' or 'IMPERIAL')
 * @returns {Promise<string>} Display unit
 */
export async function getDisplayUnit(unit, measurementSystem) {
  if (!unit || !measurementSystem) {
    return unit
  }

  try {
    return await unitConversionService.getDisplayUnit(unit, measurementSystem)
  } catch (error) {
    console.error('Failed to get display unit:', error)
    // Return original unit as fallback
    return unit
  }
}

/**
 * Format weight value with unit conversion based on measurement system.
 * Uses API service for conversions.
 * 
 * @param {number} value - The value to format
 * @param {string} originalUnit - Original unit
 * @param {string} measurementSystem - Measurement system ('METRIC' or 'IMPERIAL')
 * @param {number} decimals - Number of decimal places (default: 2)
 * @returns {Promise<string>} Formatted string with value and unit
 */
export async function formatWeight(value, originalUnit, measurementSystem, decimals = 2) {
  if (value == null || value === undefined) {
    return ''
  }

  const displayUnit = await getDisplayUnit(originalUnit, measurementSystem)
  let displayValue = value
  
  if (displayUnit !== originalUnit) {
    const isWeight = await unitConversionService.isWeightUnit(originalUnit)
    if (isWeight) {
      try {
        displayValue = await convertWeight(value, originalUnit, displayUnit)
      } catch (error) {
        console.error('Weight conversion failed in formatWeight:', error)
        // Use original value as fallback
      }
    }
  }

  const formattedValue = Number(displayValue)
    .toFixed(decimals)
    .replace(/\.?0+$/, '')

  return `${formattedValue} ${displayUnit}`
}

/**
 * Check if a unit is a weight unit.
 * Uses API to avoid duplication with backend logic.
 * 
 * @param {string} unit - The unit to check
 * @returns {Promise<boolean>} True if weight unit
 */
export async function isWeightUnit(unit) {
  if (!unit) {
    return false
  }

  try {
    return await unitConversionService.isWeightUnit(unit)
  } catch (error) {
    console.error('Failed to check if weight unit:', error)
    return false
  }
}

/**
 * Check if a unit is a volume unit.
 * Uses API to avoid duplication with backend logic.
 * 
 * @param {string} unit - The unit to check
 * @returns {Promise<boolean>} True if volume unit
 */
export async function isVolumeUnit(unit) {
  if (!unit) {
    return false
  }

  try {
    return await unitConversionService.isVolumeUnit(unit)
  } catch (error) {
    console.error('Failed to check if volume unit:', error)
    return false
  }
}
