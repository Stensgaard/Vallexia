import { getDefaultLanguage } from '@/i18n'
import {
  getDefaultCountry,
  getDecimalSeparatorForCountry,
  getThousandsSeparatorForCountry,
  getCurrencyForCountry as getCurrencyFromConfig,
  getTokensForDateCode,
  getTokensForDateFormat
} from '@/utils/localeConfig'

/**
 * Format a date according to the specified format and locale.
 * Supports both format strings from backend (MM/DD/YYYY) and enum-like keys (MM_DD_YYYY).
 * 
 * @param {Date|string} date - Date to format
 * @param {string} format - Date format string (MM/DD/YYYY, DD/MM/YYYY, YYYY-MM-DD, DD.MM.YYYY) or enum key (MM_DD_YYYY, etc.)
 * @param {string} locale - Locale string (e.g., 'en-US', 'en-GB')
 * @returns {string} Formatted date string
 */
export function formatDate(date, format, locale = 'en-US') {
  if (!date) return ''
  
  const dateObj = date instanceof Date ? date : new Date(date)
  if (isNaN(dateObj.getTime())) return ''
  
  const year = dateObj.getFullYear()
  const month = String(dateObj.getMonth() + 1).padStart(2, '0')
  const day = String(dateObj.getDate()).padStart(2, '0')
  
  // Handle both format strings (from backend) and enum-like keys (from frontend)
  const normalizedFormat = format || ''
  
  const tokens = getTokensForDateCode(normalizedFormat) || getTokensForDateFormat(normalizedFormat)
  if (tokens?.length) {
    return formatDateWithTokens(dateObj, tokens)
  }

  return dateObj.toLocaleDateString(locale)
}

/**
 * Format a number according to the specified decimal and thousands separators.
 * 
 * @param {number} number - Number to format
 * @param {string} decimalSep - Decimal separator (default: '.')
 * @param {string} thousandsSep - Thousands separator (default: ',')
 * @param {number} decimals - Number of decimal places (default: 2)
 * @returns {string} Formatted number string
 */
export function formatNumber(number, decimalSep = '.', thousandsSep = ',', decimals = 2) {
  if (number === null || number === undefined || isNaN(number)) return ''
  
  // Convert to fixed decimal places
  const fixed = number.toFixed(decimals)
  
  // Split into integer and decimal parts
  const parts = fixed.split('.')
  const integerPart = parts[0]
  const decimalPart = parts[1]
  
  // Add thousands separator
  const formattedInteger = integerPart.replace(/\B(?=(\d{3})+(?!\d))/g, thousandsSep)
  
  // Combine with decimal separator
  if (decimalPart && decimalPart !== '00') {
    return `${formattedInteger}${decimalSep}${decimalPart}`
  }
  
  return formattedInteger
}

/**
 * Get locale string from user settings (language and country).
 * 
 * @param {Object} settings - User settings object
 * @param {string} settings.language - Language code (e.g., 'en', 'es')
 * @param {string} settings.country - Country code (e.g., 'US', 'GB')
 * @returns {string} Locale string (e.g., 'en-US', 'en-GB')
 */
export function getLocaleFromSettings(settings) {
  if (!settings) {
    const language = getDefaultLanguage()
    const country = getDefaultCountry()
    return country ? `${language}-${country}` : language
  }
  
  const language = settings.language || getDefaultLanguage()
  const country = settings.country || getDefaultCountry()
  
  return country ? `${language}-${country}` : language
}

/**
 * Get decimal separator based on country/locale.
 * Most European countries use comma, others use period.
 * 
 * @param {string} country - Country code (e.g., 'US', 'DE', 'FR')
 * @returns {string} Decimal separator ('.' or ',')
 */
export function getDecimalSeparator(country) {
  return getDecimalSeparatorForCountry(country) || '.'
}

/**
 * Get thousands separator based on country/locale.
 * Usually opposite of decimal separator.
 * 
 * @param {string} country - Country code (e.g., 'US', 'DE', 'FR')
 * @returns {string} Thousands separator (',' or '.' or ' ')
 */
export function getThousandsSeparator(country) {
  return getThousandsSeparatorForCountry(country) || ','
}

/**
 * Get currency code based on country.
 * 
 * @param {string} country - Country code (e.g., 'US', 'GB', 'DE')
 * @returns {string|null} Currency code (e.g., 'USD', 'GBP', 'EUR') or null
 */
export function getCurrencyFromCountry(country) {
  return getCurrencyFromConfig(country)
}

const formatDateWithTokens = (dateObj, tokens) => {
  const year = String(dateObj.getFullYear())
  const month = String(dateObj.getMonth() + 1).padStart(2, '0')
  const day = String(dateObj.getDate()).padStart(2, '0')

  return tokens.map((token) => {
    const type = token?.type?.toUpperCase()
    switch (type) {
      case 'DAY':
        return day
      case 'MONTH':
        return month
      case 'YEAR':
        return year
      case 'LITERAL':
        return token?.value || ''
      default:
        return ''
    }
  }).join('')
}
