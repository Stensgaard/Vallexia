/**
 * Validation utility functions for validating values against constants
 * and providing safe fallbacks.
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
export function validateValue(value, constants, fallback, _fieldName = "") {
  if (!value) {
    return fallback;
  }

  if (isObjectConstants(constants)) {
    return objectContainsValue(constants, value) ? value : fallback;
  }

  if (Array.isArray(constants)) {
    if (isArrayOfObjects(constants)) {
      return arrayObjectsContainValue(constants, value) ? value : fallback;
    }
    return constants.includes(value) ? value : fallback;
  }

  return fallback;
}

/**
 * Filters an array to only include values that exist in the constants.
 *
 * @param {Array} values - Array of values to filter
 * @param {Object|Array} constants - Constants object or array to validate against
 * @param {string} fieldName - Name of the field for logging (optional)
 * @returns {Array} Filtered array with only valid values
 */
export function filterValidValues(values, constants, _fieldName = "") {
  if (!Array.isArray(values)) {
    return [];
  }

  if (isObjectConstants(constants)) {
    const allowed = new Set(Object.values(constants));
    return values.filter((value) => allowed.has(value));
  }

  if (Array.isArray(constants) && constants.length > 0) {
    if (isArrayOfObjects(constants)) {
      const matcher = createObjectArrayMatcher(constants);
      return values.filter(matcher);
    }
    return values.filter((value) => constants.includes(value));
  }

  return [];
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
export function validateEnumValue(
  value,
  constantsObject,
  fallback,
  _fieldName = "",
) {
  if (!value) {
    return fallback;
  }

  const validKeys = Object.keys(constantsObject);
  if (validKeys.includes(value)) {
    return value;
  }

  return fallback;
}

function isObjectConstants(constants) {
  return typeof constants === "object" && !Array.isArray(constants);
}

function objectContainsValue(constants, value) {
  return Object.values(constants).includes(value);
}

function isArrayOfObjects(constants) {
  return Array.isArray(constants) && constants.length > 0 && typeof constants[0] === "object";
}

function createObjectArrayMatcher(constants) {
  const hasCode = Object.prototype.hasOwnProperty.call(constants[0], "code");
  const hasValue = Object.prototype.hasOwnProperty.call(constants[0], "value");

  return (value) =>
    (hasCode && constants.some((item) => item.code === value)) ||
    (hasValue && constants.some((item) => item.value === value));
}

function arrayObjectsContainValue(constants, value) {
  return createObjectArrayMatcher(constants)(value);
}
