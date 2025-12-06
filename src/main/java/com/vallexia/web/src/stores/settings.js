import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userService } from '@/services/userService'
import { 
  formatDate, 
  formatNumber, 
  getLocaleFromSettings,
  getDecimalSeparator,
  getThousandsSeparator,
  getCurrencyFromCountry
} from '@/utils/formatUtils'
import {
  convertWeight,
  getDisplayUnit,
  formatWeight as formatWeightUtil,
  isWeightUnit
} from '@/utils/unitConversionUtils'
import { getErrorMessage } from '@/utils/errorUtils'
import { validateEnumValue, validateValue } from '@/utils/validationUtils'
import { 
  getMeasurementSystems, 
  getDefaultMeasurementSystemCode,
  getFirstDayOfWeek,
  getDefaultFirstDayOfWeekCode,
  getFormatForDateCode
} from '@/utils/localeConfig'

export const useSettingsStore = defineStore('settings', () => {
  // State
  const settings = ref(null)
  const isLoading = ref(false)
  const error = ref(null)

  // Getters
  const locale = computed(() => {
    return getLocaleFromSettings(settings.value)
  })

  const dateFormat = computed(() => {
    const code = settings.value?.dateFormat
    if (!code) {
      return 'MM/DD/YYYY'
    }
    return getFormatForDateCode(code) || code
  })

  const measurementSystem = computed(() => {
    const defaultCode = getDefaultMeasurementSystemCode() || 'METRIC'
    if (!settings.value?.measurementSystem) {
      return defaultCode
    }
    return validateValue(
      settings.value.measurementSystem,
      getMeasurementSystems(),
      defaultCode,
      'measurementSystem'
    )
  })

  const firstDayOfWeek = computed(() => {
    const defaultCode = getDefaultFirstDayOfWeekCode() || 'MONDAY'
    return validateValue(
      settings.value?.firstDayOfWeek || defaultCode,
      getFirstDayOfWeek(),
      defaultCode,
      'firstDayOfWeek'
    )
  })

  const numberDecimalSeparator = computed(() => {
    // Derive from country if not stored, otherwise use stored value
    if (settings.value?.numberDecimalSeparator) {
      return settings.value.numberDecimalSeparator
    }
    return getDecimalSeparator(settings.value?.country)
  })

  const numberThousandsSeparator = computed(() => {
    // Derive from country if not stored, otherwise use stored value
    if (settings.value?.numberThousandsSeparator) {
      return settings.value.numberThousandsSeparator
    }
    return getThousandsSeparator(settings.value?.country)
  })

  const currency = computed(() => {
    // Derive from country if not stored, otherwise use stored value
    if (settings.value?.currency) {
      return settings.value.currency
    }
    return getCurrencyFromCountry(settings.value?.country)
  })

  // Formatting functions - use regular functions that access computed values
  const formatDateFn = (date) => {
    return formatDate(date, dateFormat.value, locale.value)
  }

  const formatNumberFn = (number, decimals = 2) => {
    return formatNumber(
      number,
      numberDecimalSeparator.value,
      numberThousandsSeparator.value,
      decimals
    )
  }

  /**
   * Convert weight value from one unit to another.
   * Uses API service with caching.
   * 
   * @param {number} value - The value to convert
   * @param {string} fromUnit - Source unit
   * @param {string} toUnit - Target unit
   * @returns {Promise<number>} Converted value
   */
  const convertWeightFn = async (value, fromUnit, toUnit) => {
    try {
      return await convertWeight(value, fromUnit, toUnit)
    } catch (error) {
      console.error('Weight conversion failed in settings store:', error)
      // Return original value as fallback
      return value
    }
  }

  /**
   * Get appropriate display unit based on measurement system.
   * Uses API to avoid duplication with backend logic.
   * 
   * @param {string} unit - The unit to get display unit for
   * @returns {Promise<string>} Display unit
   */
  const getDisplayUnitFn = async (unit) => {
    try {
      return await getDisplayUnit(unit, measurementSystem.value)
    } catch (error) {
      console.error('Failed to get display unit in settings store:', error)
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
   * @param {number} decimals - Number of decimal places (default: 2)
   * @returns {Promise<string>} Formatted string with value and unit
   */
  const formatWeightFn = async (value, originalUnit, decimals = 2) => {
    if (!value && value !== 0) {
      return ''
    }
    
    const displayUnit = await getDisplayUnitFn(originalUnit)
    
    // If unit changed, convert the value
    let displayValue = value
    const isWeight = await isWeightUnit(originalUnit)
    if (displayUnit !== originalUnit && isWeight) {
      try {
        displayValue = await convertWeightFn(value, originalUnit, displayUnit)
      } catch (error) {
        console.error('Weight conversion failed in formatWeightFn:', error)
        // Use original value as fallback
      }
    }
    
    // Format the number using the settings store's number formatting
    const formattedValue = formatNumberFn(displayValue, decimals)
    
    return `${formattedValue} ${displayUnit}`
  }

  // Actions
  const loadSettings = async () => {
    try {
      isLoading.value = true
      error.value = null

      const settingsData = await userService.getSettings()
      settings.value = settingsData

      return settingsData
    } catch (err) {
      error.value = getErrorMessage(err)
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const updateSettings = async (settingsData) => {
    try {
      isLoading.value = true
      error.value = null

      const updatedSettings = await userService.updateSettings(settingsData)
      settings.value = updatedSettings

      return updatedSettings
    } catch (err) {
      error.value = getErrorMessage(err)
      throw err
    } finally {
      isLoading.value = false
    }
  }

  return {
    // State
    settings,
    isLoading,
    error,
    // Getters
    locale,
    dateFormat,
    measurementSystem,
    firstDayOfWeek,
    numberDecimalSeparator,
    numberThousandsSeparator,
    currency,
    formatDateFn,
    formatNumberFn,
    convertWeightFn,
    getDisplayUnitFn,
    formatWeightFn,
    // Actions
    loadSettings,
    updateSettings
  }
})
