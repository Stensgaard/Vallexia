import api from "./api";

/**
 * LRU Cache implementation for unit conversions.
 * Maintains a fixed-size cache with least-recently-used eviction.
 */
class ConversionCache {
  constructor(maxSize = 100) {
    this.maxSize = maxSize;
    this.cache = new Map();
  }

  /**
   * Generate cache key from conversion parameters.
   * @param {number} value - The value to convert
   * @param {string} fromUnit - Source unit
   * @param {string} toUnit - Target unit
   * @returns {string} Cache key
   */
  getKey(value, fromUnit, toUnit) {
    return `${value}-${fromUnit}-${toUnit}`;
  }

  /**
   * Get cached conversion result.
   * @param {string} key - Cache key
   * @returns {number|null} Cached value or null if not found
   */
  get(key) {
    if (this.cache.has(key)) {
      // Move to end (most recently used)
      const value = this.cache.get(key);
      this.cache.delete(key);
      this.cache.set(key, value);
      return value;
    }
    return null;
  }

  /**
   * Store conversion result in cache.
   * @param {string} key - Cache key
   * @param {number} value - Converted value
   */
  set(key, value) {
    if (this.cache.has(key)) {
      // Update existing entry
      this.cache.delete(key);
    } else if (this.cache.size >= this.maxSize) {
      // Remove least recently used (first entry)
      const firstKey = this.cache.keys().next().value;
      this.cache.delete(firstKey);
    }
    this.cache.set(key, value);
  }

  /**
   * Clear all cached entries.
   */
  clear() {
    this.cache.clear();
  }

  /**
   * Get current cache size.
   * @returns {number} Number of cached entries
   */
  size() {
    return this.cache.size;
  }
}

// Create singleton cache instance
const DEFAULT_CACHE_SIZE = 100;
const conversionCache = new ConversionCache(DEFAULT_CACHE_SIZE);

/**
 * Shared error handler for conversion API calls.
 * Provides consistent error handling across all conversion methods.
 *
 * @param {Error} error - The error object from API call
 * @param {string} operation - Description of the operation (for logging)
 * @throws {Error} Always throws an error with user-friendly message
 */
const handleConversionError = (error, operation = "conversion") => {
  logDevError(operation, error);

  if (error.response) {
    return buildResponseError(error, operation);
  }

  if (error.request) {
    return buildRequestError(error, operation);
  }

  return buildSetupError(error, operation);
};

const logDevError = (operation, error) => {
  if (!import.meta.env.DEV) return;
  // eslint-disable-next-line no-console
  console.error(`Unit ${operation} API error:`, error);
};

const buildResponseError = (error, operation) => {
  const status = error.response.status;
  const message =
    error.response.data?.message ||
    error.response.data?.error ||
    `${operation} failed`;

  if (status === 400) {
    logDevWarning(operation, message);
    return new Error(`Invalid ${operation}: ${message}`);
  }

  if (status === 500) {
    logDevServerError(operation, message);
    return new Error(`Server error during ${operation}. Please try again.`);
  }

  return new Error(message);
};

const buildRequestError = (error, operation) => {
  if (import.meta.env.DEV) {
    // eslint-disable-next-line no-console
    console.error(`No response from ${operation} API:`, error.message);
  }
  return new Error(
    `Unable to reach ${operation} service. Please check your connection.`,
  );
};

const buildSetupError = (error, operation) => {
  if (import.meta.env.DEV) {
    // eslint-disable-next-line no-console
    console.error(`Error setting up ${operation} request:`, error.message);
  }
  return new Error(`Failed to perform ${operation}: ${error.message}`);
};

const logDevWarning = (operation, message) => {
  if (!import.meta.env.DEV) return;
  // eslint-disable-next-line no-console
  console.warn(`Invalid ${operation} request:`, message);
};

const logDevServerError = (operation, message) => {
  if (!import.meta.env.DEV) return;
  // eslint-disable-next-line no-console
  console.error(`Server error during ${operation}:`, message);
};

/**
 * Service for unit conversion operations via API.
 * Provides caching to reduce API calls and improve performance.
 */
export const unitConversionService = {
  /**
   * Convert weight value from one unit to another.
   * Uses API with caching for performance.
   *
   * @param {number} value - The value to convert
   * @param {string} fromUnit - Source unit (e.g., 'g', 'kg', 'oz', 'lb')
   * @param {string} toUnit - Target unit (e.g., 'g', 'kg', 'oz', 'lb')
   * @returns {Promise<number>} Converted value
   * @throws {Error} If conversion fails or API call fails
   */
  async convertWeight(value, fromUnit, toUnit) {
    if (value == null || value === undefined) {
      return value;
    }

    if (!fromUnit || !toUnit) {
      return value;
    }

    // Check cache first
    const cacheKey = conversionCache.getKey(value, fromUnit, toUnit);
    const cached = conversionCache.get(cacheKey);
    if (cached !== null) {
      return cached;
    }

    try {
      const response = await api.post("/v1/units/convert", {
        value: value,
        fromUnit: fromUnit,
        toUnit: toUnit,
      });

      const convertedValue = response.data.convertedValue;

      // Cache the result
      conversionCache.set(cacheKey, convertedValue);

      return convertedValue;
    } catch (error) {
      throw handleConversionError(error, "weight conversion");
    }
  },

  /**
   * Convert volume value from one unit to another.
   * Uses API with caching for performance.
   *
   * @param {number} value - The value to convert
   * @param {string} fromUnit - Source unit (e.g., 'ml', 'l', 'cup', 'tbsp')
   * @param {string} toUnit - Target unit (e.g., 'ml', 'l', 'cup', 'tbsp')
   * @returns {Promise<number>} Converted value
   * @throws {Error} If conversion fails or API call fails
   */
  async convertVolume(value, fromUnit, toUnit) {
    if (value == null || value === undefined) {
      return value;
    }

    if (!fromUnit || !toUnit) {
      return value;
    }

    // Check cache first
    const cacheKey = conversionCache.getKey(value, fromUnit, toUnit);
    const cached = conversionCache.get(cacheKey);
    if (cached !== null) {
      return cached;
    }

    try {
      const response = await api.post("/v1/units/convert", {
        value: value,
        fromUnit: fromUnit,
        toUnit: toUnit,
      });

      const convertedValue = response.data.convertedValue;

      // Cache the result
      conversionCache.set(cacheKey, convertedValue);

      return convertedValue;
    } catch (error) {
      throw handleConversionError(error, "volume conversion");
    }
  },

  /**
   * Clear the conversion cache.
   * Useful for testing or when cache needs to be reset.
   */
  clearCache() {
    conversionCache.clear();
  },

  /**
   * Get current cache size.
   * @returns {number} Number of cached entries
   */
  getCacheSize() {
    return conversionCache.size();
  },

  /**
   * Get appropriate display unit based on measurement system.
   * Uses API to avoid duplication with backend logic.
   *
   * @param {string} unit - The unit to get display unit for
   * @param {string} measurementSystem - Measurement system ('METRIC' or 'IMPERIAL')
   * @returns {Promise<string>} Display unit
   */
  async getDisplayUnit(unit, measurementSystem) {
    if (!unit || !measurementSystem) {
      return unit;
    }

    // Normalize measurement system (backend will validate against enum)
    const normalizedSystem = measurementSystem.toUpperCase();

    try {
      const response = await api.post("/v1/units/display-unit", {
        unit: unit,
        measurementSystem: normalizedSystem,
      });
      return response.data.displayUnit;
    } catch (error) {
      if (import.meta.env.DEV) {
        // eslint-disable-next-line no-console
        console.error("Failed to get display unit:", error);
      }
      // Return original unit as fallback
      return unit;
    }
  },

  /**
   * Check unit type (weight, volume, count) for a given unit.
   * Uses API with caching to avoid duplicate calls.
   *
   * @param {string} unit - The unit to check
   * @returns {Promise<{isWeightUnit: boolean, isVolumeUnit: boolean, isCountUnit: boolean}>} Unit type information
   */
  async checkUnitType(unit) {
    if (!unit) {
      return {
        isWeightUnit: false,
        isVolumeUnit: false,
        isCountUnit: false,
      };
    }

    // Check cache first
    const cacheKey = `unit-type-${unit}`;
    const cached = conversionCache.get(cacheKey);
    if (cached !== null) {
      return cached;
    }

    try {
      const response = await api.post("/v1/units/check-type", {
        unit: unit,
      });

      const unitType = {
        isWeightUnit: response.data.isWeightUnit,
        isVolumeUnit: response.data.isVolumeUnit,
        isCountUnit: response.data.isCountUnit,
      };

      // Cache the result
      conversionCache.set(cacheKey, unitType);

      return unitType;
    } catch (error) {
      if (import.meta.env.DEV) {
        // eslint-disable-next-line no-console
        console.error("Failed to check unit type:", error);
      }
      // Return default values on error
      return {
        isWeightUnit: false,
        isVolumeUnit: false,
        isCountUnit: false,
      };
    }
  },

  /**
   * Check if a unit is a weight unit.
   * Uses cached unit type check to avoid duplicate API calls.
   *
   * @param {string} unit - The unit to check
   * @returns {Promise<boolean>} True if weight unit
   */
  async isWeightUnit(unit) {
    if (!unit) {
      return false;
    }

    const unitType = await this.checkUnitType(unit);
    return unitType.isWeightUnit;
  },

  /**
   * Check if a unit is a volume unit.
   * Uses cached unit type check to avoid duplicate API calls.
   *
   * @param {string} unit - The unit to check
   * @returns {Promise<boolean>} True if volume unit
   */
  async isVolumeUnit(unit) {
    if (!unit) {
      return false;
    }

    const unitType = await this.checkUnitType(unit);
    return unitType.isVolumeUnit;
  },

  /**
   * Check if a unit is a count unit.
   * Uses cached unit type check to avoid duplicate API calls.
   *
   * @param {string} unit - The unit to check
   * @returns {Promise<boolean>} True if count unit
   */
  async isCountUnit(unit) {
    if (!unit) {
      return false;
    }

    const unitType = await this.checkUnitType(unit);
    return unitType.isCountUnit;
  },
};
