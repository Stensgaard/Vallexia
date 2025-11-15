/**
 * Formatting utilities for dates, times, and numbers based on user settings.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */

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
  
  if (normalizedFormat === 'MM/DD/YYYY' || normalizedFormat === 'MM_DD_YYYY') {
    return `${month}/${day}/${year}`
  } else if (normalizedFormat === 'DD/MM/YYYY' || normalizedFormat === 'DD_MM_YYYY') {
    return `${day}/${month}/${year}`
  } else if (normalizedFormat === 'YYYY-MM-DD' || normalizedFormat === 'YYYY_MM_DD') {
    return `${year}-${month}-${day}`
  } else if (normalizedFormat === 'DD.MM.YYYY' || normalizedFormat === 'DD_MM_YYYY_DOT') {
    return `${day}.${month}.${year}`
  } else {
    // Fallback to locale-based formatting
    return dateObj.toLocaleDateString(locale)
  }
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
  if (!settings) return 'en-US'
  
  const language = settings.language || 'en'
  const country = settings.country || 'US'
  
  return `${language}-${country}`
}

/**
 * Get decimal separator based on country/locale.
 * Most European countries use comma, others use period.
 * 
 * @param {string} country - Country code (e.g., 'US', 'DE', 'FR')
 * @returns {string} Decimal separator ('.' or ',')
 */
export function getDecimalSeparator(country) {
  if (!country) return '.'
  
  // Countries that use comma as decimal separator
  const commaCountries = [
    'DE', 'FR', 'IT', 'ES', 'PT', 'NL', 'BE', 'AT', 'CH', 'SE', 'NO', 'DK', 'FI',
    'PL', 'CZ', 'SK', 'HU', 'RO', 'BG', 'HR', 'SI', 'GR', 'RU', 'BR', 'AR', 'CL',
    'CO', 'PE', 'VE', 'EC', 'UY', 'PY', 'BO', 'ZA'
  ]
  
  return commaCountries.includes(country.toUpperCase()) ? ',' : '.'
}

/**
 * Get thousands separator based on country/locale.
 * Usually opposite of decimal separator.
 * 
 * @param {string} country - Country code (e.g., 'US', 'DE', 'FR')
 * @returns {string} Thousands separator (',' or '.' or ' ')
 */
export function getThousandsSeparator(country) {
  if (!country) return ','
  
  const decimalSep = getDecimalSeparator(country)
  
  // If decimal is comma, thousands is usually period or space
  if (decimalSep === ',') {
    // Some countries use space, others use period
    const spaceCountries = ['FR', 'SE', 'NO', 'FI', 'DK']
    return spaceCountries.includes(country.toUpperCase()) ? ' ' : '.'
  }
  
  // If decimal is period, thousands is usually comma
  return ','
}

/**
 * Get currency code based on country.
 * 
 * @param {string} country - Country code (e.g., 'US', 'GB', 'DE')
 * @returns {string|null} Currency code (e.g., 'USD', 'GBP', 'EUR') or null
 */
export function getCurrencyFromCountry(country) {
  if (!country) return null
  
  // Map of country codes to currency codes
  const countryToCurrency = {
    'US': 'USD',
    'GB': 'GBP',
    'CA': 'CAD',
    'AU': 'AUD',
    'NZ': 'NZD',
    'JP': 'JPY',
    'CN': 'CNY',
    'IN': 'INR',
    'BR': 'BRL',
    'MX': 'MXN',
    'AR': 'ARS',
    'CL': 'CLP',
    'CO': 'COP',
    'PE': 'PEN',
    'VE': 'VES',
    'EC': 'USD',
    'UY': 'UYU',
    'PY': 'PYG',
    'BO': 'BOB',
    'ZA': 'ZAR',
    'KR': 'KRW',
    'SG': 'SGD',
    'MY': 'MYR',
    'TH': 'THB',
    'ID': 'IDR',
    'PH': 'PHP',
    'VN': 'VND',
    'CH': 'CHF',
    'NO': 'NOK',
    'SE': 'SEK',
    'DK': 'DKK',
    'PL': 'PLN',
    'CZ': 'CZK',
    'HU': 'HUF',
    'RO': 'RON',
    'BG': 'BGN',
    'HR': 'HRK',
    'TR': 'TRY',
    'RU': 'RUB',
    'IL': 'ILS',
    'AE': 'AED',
    'SA': 'SAR'
  }
  
  // Check if country is in Eurozone
  const eurozoneCountries = [
    'AT', 'BE', 'CY', 'EE', 'FI', 'FR', 'DE', 'GR', 'IE', 'IT', 'LV', 'LT',
    'LU', 'MT', 'NL', 'PT', 'SK', 'SI', 'ES'
  ]
  
  if (eurozoneCountries.includes(country.toUpperCase())) {
    return 'EUR'
  }
  
  return countryToCurrency[country.toUpperCase()] || null
}
