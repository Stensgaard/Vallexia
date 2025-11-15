/**
 * Validation utility functions for validating values against constants
 * and providing safe fallbacks.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */

/**
 * Validates a single value against a constants object or array.
 * Returns the value if valid, otherwise returns the fallback.
 * 
 * @param {string} value - The value to validate
 * @param {Object|Array} constants - Constants object or array to validate against
 * @param {string} fallback - Fallback value if validation fails
 * @param {string} fieldName - Name of the field for logging (optional)
 * @returns {string} Valid value or fallback
 */
export function validateValue(value, constants, fallback, fieldName = '') {
  if (!value) {
    return fallback
  }

  // Check if constants is an object (like DIETARY_RESTRICTIONS)
  if (typeof constants === 'object' && !Array.isArray(constants)) {
    const validValues = Object.values(constants)
    if (validValues.includes(value)) {
      return value
    }
  }
  // Check if constants is an array of objects (like SUPPORTED_LANGUAGES, COUNTRIES)
  else if (Array.isArray(constants)) {
    // Check if it's an array of objects with 'code' or 'value' property
    if (constants.length > 0 && typeof constants[0] === 'object') {
      const hasCode = constants[0].hasOwnProperty('code')
      const hasValue = constants[0].hasOwnProperty('value')
      
      if (hasCode && constants.some(item => item.code === value)) {
        return value
      }
      if (hasValue && constants.some(item => item.value === value)) {
        return value
      }
    }
    // Check if it's an array of strings
    else if (constants.includes(value)) {
      return value
    }
  }

  return fallback
}

/**
 * Filters an array to only include values that exist in the constants.
 * 
 * @param {Array} values - Array of values to filter
 * @param {Object|Array} constants - Constants object or array to validate against
 * @param {string} fieldName - Name of the field for logging (optional)
 * @returns {Array} Filtered array with only valid values
 */
export function filterValidValues(values, constants, fieldName = '') {
  if (!Array.isArray(values)) {
    return []
  }

  const validValues = []

  // Check if constants is an object (like DIETARY_RESTRICTIONS)
  if (typeof constants === 'object' && !Array.isArray(constants)) {
    const constantValues = Object.values(constants)
    values.forEach(value => {
      if (constantValues.includes(value)) {
        validValues.push(value)
      }
    })
  }
  // Check if constants is an array of objects (like SUPPORTED_LANGUAGES, COUNTRIES)
  else if (Array.isArray(constants) && constants.length > 0) {
    if (typeof constants[0] === 'object') {
      const hasCode = constants[0].hasOwnProperty('code')
      const hasValue = constants[0].hasOwnProperty('value')
      
      values.forEach(value => {
        if (hasCode && constants.some(item => item.code === value)) {
          validValues.push(value)
        } else if (hasValue && constants.some(item => item.value === value)) {
          validValues.push(value)
        }
      })
    }
    // Check if it's an array of strings
    else {
      values.forEach(value => {
        if (constants.includes(value)) {
          validValues.push(value)
        }
      })
    }
  }

  return validValues
}

/**
 * Validates a value against an object's keys (for enum-like constants).
 * 
 * @param {string} value - The value to validate
 * @param {Object} constantsObject - Constants object to validate against (checks keys)
 * @param {string} fallback - Fallback value if validation fails
 * @param {string} fieldName - Name of the field for logging (optional)
 * @returns {string} Valid value or fallback
 */
export function validateEnumValue(value, constantsObject, fallback, fieldName = '') {
  if (!value) {
    return fallback
  }

  const validKeys = Object.keys(constantsObject)
  if (validKeys.includes(value)) {
    return value
  }

  return fallback
}
