/**
 * Unit conversion utilities for converting between metric and imperial units.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */

// Conversion factors
const OUNCES_TO_GRAMS = 28.35
const POUNDS_TO_GRAMS = 453.59
const KILOGRAMS_TO_GRAMS = 1000.0
const MILLIGRAMS_TO_GRAMS = 0.001

// Weight units
const METRIC_WEIGHT_UNITS = new Set([
  'g', 'gram', 'grams', 'kg', 'kilogram', 'kilograms', 'mg', 'milligram', 'milligrams'
])

const IMPERIAL_WEIGHT_UNITS = new Set([
  'oz', 'ounce', 'ounces', 'lb', 'pound', 'pounds', 'lbs'
])

// Volume units (universal, no conversion needed)
const VOLUME_UNITS = new Set([
  'cup', 'cups', 'tbsp', 'tablespoon', 'tablespoons', 'tsp', 'teaspoon', 'teaspoons',
  'ml', 'milliliter', 'milliliters', 'l', 'liter', 'liters', 'fl oz', 'fluid ounce', 'fluid ounces'
])

// Count units (universal, no conversion needed)
const COUNT_UNITS = new Set([
  'piece', 'pieces', 'item', 'items', 'whole', 'wholes', 'pcs', 'pc'
])

/**
 * Convert weight value from one unit to another.
 * 
 * @param {number} value - The value to convert
 * @param {string} fromUnit - The source unit
 * @param {string} toUnit - The target unit
 * @returns {number} Converted value
 */
export function convertWeight(value, fromUnit, toUnit) {
  if (value == null || value === undefined) {
    return value
  }
  
  const fromUnitLower = (fromUnit || '').toLowerCase()
  const toUnitLower = (toUnit || '').toLowerCase()
  
  // If units are the same, return as-is
  if (fromUnitLower === toUnitLower) {
    return value
  }
  
  // Convert to grams first (intermediate unit)
  const valueInGrams = convertToGrams(value, fromUnit)
  
  // Convert from grams to target unit
  return convertFromGrams(valueInGrams, toUnit)
}

/**
 * Convert any unit to grams (metric base unit).
 * 
 * @param {number} value - The value to convert
 * @param {string} unit - The source unit
 * @returns {number} Value in grams
 */
export function convertToGrams(value, unit) {
  if (value == null || value === undefined || !unit) {
    return value
  }
  
  const unitLower = unit.toLowerCase()
  
  // Already in grams
  if (unitLower === 'g' || unitLower === 'gram' || unitLower === 'grams') {
    return value
  }
  
  // Metric units
  if (unitLower === 'kg' || unitLower === 'kilogram' || unitLower === 'kilograms') {
    return value * KILOGRAMS_TO_GRAMS
  }
  if (unitLower === 'mg' || unitLower === 'milligram' || unitLower === 'milligrams') {
    return value * MILLIGRAMS_TO_GRAMS
  }
  
  // Imperial units
  if (unitLower === 'oz' || unitLower === 'ounce' || unitLower === 'ounces') {
    return value * OUNCES_TO_GRAMS
  }
  if (unitLower === 'lb' || unitLower === 'lbs' || unitLower === 'pound' || unitLower === 'pounds') {
    return value * POUNDS_TO_GRAMS
  }
  
  // Unknown unit, assume grams
  return value
}

/**
 * Convert grams to target unit.
 * 
 * @param {number} valueInGrams - Value in grams
 * @param {string} toUnit - Target unit
 * @returns {number} Converted value
 */
function convertFromGrams(valueInGrams, toUnit) {
  if (valueInGrams == null || valueInGrams === undefined || !toUnit) {
    return valueInGrams
  }
  
  const unitLower = toUnit.toLowerCase()
  
  // Metric units
  if (unitLower === 'g' || unitLower === 'gram' || unitLower === 'grams') {
    return valueInGrams
  }
  if (unitLower === 'kg' || unitLower === 'kilogram' || unitLower === 'kilograms') {
    return valueInGrams / KILOGRAMS_TO_GRAMS
  }
  if (unitLower === 'mg' || unitLower === 'milligram' || unitLower === 'milligrams') {
    return valueInGrams / MILLIGRAMS_TO_GRAMS
  }
  
  // Imperial units
  if (unitLower === 'oz' || unitLower === 'ounce' || unitLower === 'ounces') {
    return valueInGrams / OUNCES_TO_GRAMS
  }
  if (unitLower === 'lb' || unitLower === 'lbs' || unitLower === 'pound' || unitLower === 'pounds') {
    return valueInGrams / POUNDS_TO_GRAMS
  }
  
  // Unknown unit, return as grams
  return valueInGrams
}

/**
 * Convert to metric unit (grams for weights).
 * 
 * @param {number} value - The value to convert
 * @param {string} unit - The source unit
 * @returns {number} Value in metric units
 */
export function convertToMetric(value, unit) {
  return convertToGrams(value, unit)
}

/**
 * Convert to imperial unit (ounces for weights).
 * 
 * @param {number} value - The value to convert
 * @param {string} unit - The source unit
 * @returns {number} Value in imperial units (ounces)
 */
export function convertToImperial(value, unit) {
  if (value == null || value === undefined || !unit) {
    return value
  }
  
  // Convert to grams first
  const valueInGrams = convertToGrams(value, unit)
  
  // Convert to ounces
  return valueInGrams / OUNCES_TO_GRAMS
}

/**
 * Get appropriate display unit based on measurement system.
 * For weight units, returns metric or imperial equivalent.
 * For volume and count units, returns original unit (no conversion).
 * 
 * @param {string} unit - The original unit
 * @param {string} measurementSystem - The measurement system ('METRIC' or 'IMPERIAL')
 * @returns {string} Display unit
 */
export function getDisplayUnit(unit, measurementSystem) {
  if (!unit || !measurementSystem) {
    return unit
  }
  
  const unitLower = unit.toLowerCase()
  const isImperial = measurementSystem.toUpperCase() === 'IMPERIAL'
  
  // Weight units - convert based on system
  if (METRIC_WEIGHT_UNITS.has(unitLower)) {
    if (isImperial) {
      // Convert metric to imperial
      if (unitLower === 'g' || unitLower === 'gram' || unitLower === 'grams') {
        return 'oz'
      }
      if (unitLower === 'kg' || unitLower === 'kilogram' || unitLower === 'kilograms') {
        return 'lb'
      }
      if (unitLower === 'mg' || unitLower === 'milligram' || unitLower === 'milligrams') {
        return 'oz' // Convert mg to oz (very small, but still convert)
      }
    }
    // Metric system - keep as-is
    return unit
  }
  
  if (IMPERIAL_WEIGHT_UNITS.has(unitLower)) {
    if (!isImperial) {
      // Convert imperial to metric
      if (unitLower === 'oz' || unitLower === 'ounce' || unitLower === 'ounces') {
        return 'g'
      }
      if (unitLower === 'lb' || unitLower === 'lbs' || unitLower === 'pound' || unitLower === 'pounds') {
        return 'kg'
      }
    }
    // Imperial system - keep as-is
    return unit
  }
  
  // Volume and count units - keep as-is (universal)
  return unit
}

/**
 * Format weight value with appropriate unit based on measurement system.
 * 
 * @param {number} value - The value to format
 * @param {string} originalUnit - The original unit (e.g., 'g', 'oz')
 * @param {string} measurementSystem - The measurement system ('METRIC' or 'IMPERIAL')
 * @param {number} decimals - Number of decimal places (default: 2)
 * @returns {string} Formatted string with value and unit
 */
export function formatWeight(value, originalUnit, measurementSystem, decimals = 2) {
  if (value == null || value === undefined) {
    return ''
  }
  
  const displayUnit = getDisplayUnit(originalUnit, measurementSystem)
  const isImperial = measurementSystem.toUpperCase() === 'IMPERIAL'
  
  // If unit changed, convert the value
  let displayValue = value
  if (displayUnit !== originalUnit) {
    displayValue = convertWeight(value, originalUnit, displayUnit)
  }
  
  // Format the number (basic formatting, use formatNumber from settings for separators)
  const formattedValue = displayValue.toFixed(decimals).replace(/\.?0+$/, '')
  
  return `${formattedValue} ${displayUnit}`
}

/**
 * Check if a unit is a weight unit.
 * 
 * @param {string} unit - The unit to check
 * @returns {boolean} True if weight unit, false otherwise
 */
export function isWeightUnit(unit) {
  if (!unit) {
    return false
  }
  const unitLower = unit.toLowerCase()
  return METRIC_WEIGHT_UNITS.has(unitLower) || IMPERIAL_WEIGHT_UNITS.has(unitLower)
}

/**
 * Check if a unit is a volume unit.
 * 
 * @param {string} unit - The unit to check
 * @returns {boolean} True if volume unit, false otherwise
 */
export function isVolumeUnit(unit) {
  if (!unit) {
    return false
  }
  return VOLUME_UNITS.has(unit.toLowerCase())
}

/**
 * Check if a unit is a count unit.
 * 
 * @param {string} unit - The unit to check
 * @returns {boolean} True if count unit, false otherwise
 */
export function isCountUnit(unit) {
  if (!unit) {
    return false
  }
  return COUNT_UNITS.has(unit.toLowerCase())
}
