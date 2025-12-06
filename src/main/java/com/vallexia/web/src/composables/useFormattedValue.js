/**
 * Composable for formatting values with unit conversion.
 * Handles async conversions with proper Vue reactivity.
 * 
 * @module useFormattedValue
 */

import { ref, computed } from 'vue'
import { formatWeight, isWeightUnit } from '@/utils/unitConversionUtils'
import { useSettingsStore } from '@/stores/settings'

/**
 * Composable for formatting ingredient quantities and nutritional values.
 * Provides reactive caching that properly triggers Vue re-renders.
 * 
 * @returns {Object} Object with formatting functions
 */
export function useFormattedValue() {
  const settingsStore = useSettingsStore()
  
  // Reactive cache - Vue will detect changes to this ref
  const formattedCache = ref({})
  
  /**
   * Generate cache key from value, unit, and measurement system.
   * @param {number} value - The value to format
   * @param {string} unit - The unit
   * @param {string} system - Measurement system
   * @returns {string} Cache key
   */
  const getCacheKey = (value, unit, system) => {
    return `${value}-${unit}-${system}`
  }
  
  /**
   * Format number with appropriate decimal places.
   * @param {number} number - Number to format
   * @param {number} decimals - Decimal places
   * @returns {string} Formatted number string
   */
  const formatNumber = (number, decimals = 0) => {
    return settingsStore.formatNumberFn(number, decimals)
  }
  
  /**
   * Format ingredient quantity with unit conversion.
   * Uses reactive cache that triggers Vue re-renders.
   * 
   * @param {number} quantity - The quantity value
   * @param {string} unit - The unit
   * @returns {string} Formatted string with value and unit
   */
  const formatIngredientQuantity = (quantity, unit) => {
    if (!quantity && quantity !== 0) {
      return ''
    }
    
    const cacheKey = getCacheKey(quantity, unit, settingsStore.measurementSystem)
    
    // Return cached value if available
    if (formattedCache.value[cacheKey]) {
      return formattedCache.value[cacheKey]
    }
    
    // Start async formatting - will update reactive cache
    formatIngredientQuantityAsync(quantity, unit, cacheKey)
    
    // Return placeholder immediately
    const decimals = quantity % 1 === 0 ? 0 : 2
    return `${formatNumber(quantity, decimals)} ${unit || ''}`
  }
  
  /**
   * Async formatter for ingredient quantities.
   * Updates reactive cache which triggers Vue re-renders.
   * 
   * @param {number} quantity - The quantity value
   * @param {string} unit - The unit
   * @param {string} cacheKey - Cache key for this value
   */
  const formatIngredientQuantityAsync = async (quantity, unit, cacheKey) => {
    try {
      const isWeight = await isWeightUnit(unit)
      if (isWeight) {
        const formatted = await formatWeight(
          quantity,
          unit,
          settingsStore.measurementSystem,
          2
        )
        // Update reactive ref - Vue will detect this change
        formattedCache.value[cacheKey] = formatted
      } else {
        // Volume/count units - simple formatting
        const decimals = quantity % 1 === 0 ? 0 : 2
        formattedCache.value[cacheKey] = `${formatNumber(quantity, decimals)} ${unit || ''}`
      }
    } catch (error) {
      console.error('Failed to format ingredient quantity:', error)
      // Keep placeholder on error
    }
  }
  
  /**
   * Format nutritional value with unit conversion.
   * Nutritional values are stored in grams, converted based on measurement system.
   * Uses reactive cache that triggers Vue re-renders.
   * 
   * @param {number} value - The nutritional value in grams
   * @returns {string} Formatted string with value and unit
   */
  const formatNutritionalValue = (value) => {
    if (!value && value !== 0) {
      return ''
    }
    
    const cacheKey = getCacheKey(value, 'g', settingsStore.measurementSystem)
    
    // Return cached value if available
    if (formattedCache.value[cacheKey]) {
      return formattedCache.value[cacheKey]
    }
    
    // Start async formatting - will update reactive cache
    formatNutritionalValueAsync(value, cacheKey)
    
    // Return placeholder immediately
    const unit = settingsStore.measurementSystem === 'IMPERIAL' ? 'oz' : 'g'
    return `${formatNumber(value, 1)}${unit}`
  }
  
  /**
   * Async formatter for nutritional values.
   * Updates reactive cache which triggers Vue re-renders.
   * 
   * @param {number} value - The nutritional value in grams
   * @param {string} cacheKey - Cache key for this value
   */
  const formatNutritionalValueAsync = async (value, cacheKey) => {
    try {
      const formatted = await formatWeight(
        value,
        'g',
        settingsStore.measurementSystem,
        1
      )
      // Update reactive ref - Vue will detect this change
      formattedCache.value[cacheKey] = formatted
    } catch (error) {
      console.error('Failed to format nutritional value:', error)
      // Keep placeholder on error
    }
  }
  
  /**
   * Clear the formatted cache.
   * Useful when measurement system changes.
   */
  const clearCache = () => {
    formattedCache.value = {}
  }
  
  return {
    formatIngredientQuantity,
    formatNutritionalValue,
    clearCache,
    formattedCache: computed(() => formattedCache.value)
  }
}
