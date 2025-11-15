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
import { 
  MEASUREMENT_SYSTEMS, 
  FIRST_DAY_OF_WEEK
} from '@/utils/constants'
import { validateEnumValue } from '@/utils/validationUtils'

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
    // Backend returns format strings like 'MM/DD/YYYY', but we may need enum keys for frontend
    return settings.value?.dateFormat || 'MM/DD/YYYY'
  })

  const measurementSystem = computed(() => {
    if (!settings.value?.measurementSystem) {
      return MEASUREMENT_SYSTEMS.METRIC
    }
    return validateEnumValue(
      settings.value.measurementSystem,
      MEASUREMENT_SYSTEMS,
      MEASUREMENT_SYSTEMS.METRIC,
      'measurementSystem'
    )
  })

  const firstDayOfWeek = computed(() => {
    if (!settings.value?.firstDayOfWeek) {
      return FIRST_DAY_OF_WEEK.MONDAY
    }
    return validateEnumValue(
      settings.value.firstDayOfWeek,
      FIRST_DAY_OF_WEEK,
      FIRST_DAY_OF_WEEK.MONDAY,
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

  const convertWeightFn = (value, fromUnit, toUnit) => {
    return convertWeight(value, fromUnit, toUnit)
  }

  const getDisplayUnitFn = (unit) => {
    return getDisplayUnit(unit, measurementSystem.value)
  }

  const formatWeightFn = (value, originalUnit, decimals = 2) => {
    if (!value && value !== 0) {
      return ''
    }
    
    const displayUnit = getDisplayUnitFn(originalUnit)
    const isImperial = measurementSystem.value === MEASUREMENT_SYSTEMS.IMPERIAL
    
    // If unit changed, convert the value
    let displayValue = value
    if (displayUnit !== originalUnit && isWeightUnit(originalUnit)) {
      displayValue = convertWeightFn(value, originalUnit, displayUnit)
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
