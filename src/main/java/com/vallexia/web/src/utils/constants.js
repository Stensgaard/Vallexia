/**
 * Constants configurations.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */

/**
 * Helper function to create enum and labels from a single source of truth.
 * The labels object keys become the enum values, and values become display labels.
 * 
 * @param {Object} labels - Object where keys are enum values and values are display labels
 * @returns {Object} Object with 'enum' and 'labels' properties
 */
function createEnumWithLabels(labels) {
  const enumObj = {}
  Object.keys(labels).forEach(key => {
    enumObj[key] = key
  })
  return {
    enum: enumObj,
    labels: labels
  }
}

// Dietary Restrictions Labels
const DIETARY_RESTRICTIONS_LABELS_DATA = {
  VEGETARIAN: 'Vegetarian',
  VEGAN: 'Vegan',
  GLUTEN_FREE: 'Gluten-Free',
  DAIRY_FREE: 'Dairy-Free',
  NUT_FREE: 'Nut-Free',
  SOY_FREE: 'Soy-Free',
  EGG_FREE: 'Egg-Free',
  LOW_CARB: 'Low-Carb',
  KETO: 'Keto',
  PALEO: 'Paleo',
  MEDITERRANEAN: 'Mediterranean',
  LOW_SODIUM: 'Low-Sodium',
  LOW_FAT: 'Low-Fat',
  HIGH_PROTEIN: 'High-Protein',
  HALAL: 'Halal',
  KOSHER: 'Kosher'
}

const dietaryRestrictionsData = createEnumWithLabels(DIETARY_RESTRICTIONS_LABELS_DATA)
export const DIETARY_RESTRICTIONS = dietaryRestrictionsData.enum
export const DIETARY_RESTRICTIONS_LABELS = dietaryRestrictionsData.labels

// Allergies Labels
const ALLERGIES_LABELS_DATA = {
  PEANUTS: 'Peanuts',
  TREE_NUTS: 'Tree Nuts',
  MILK: 'Milk',
  EGGS: 'Eggs',
  FISH: 'Fish',
  SHELLFISH: 'Shellfish',
  SOY: 'Soy',
  WHEAT: 'Wheat',
  SESAME: 'Sesame',
  MUSTARD: 'Mustard',
  CELERY: 'Celery',
  LUPIN: 'Lupin',
  SULFITES: 'Sulfites'
}

const allergiesData = createEnumWithLabels(ALLERGIES_LABELS_DATA)
export const ALLERGIES = allergiesData.enum
export const ALLERGIES_LABELS = allergiesData.labels

// Cuisine Types Labels
const CUISINE_TYPES_LABELS_DATA = {
  AMERICAN: 'American',
  ITALIAN: 'Italian',
  MEXICAN: 'Mexican',
  CHINESE: 'Chinese',
  JAPANESE: 'Japanese',
  THAI: 'Thai',
  INDIAN: 'Indian',
  FRENCH: 'French',
  MEDITERRANEAN: 'Mediterranean',
  GREEK: 'Greek',
  SPANISH: 'Spanish',
  GERMAN: 'German',
  BRITISH: 'British',
  KOREAN: 'Korean',
  VIETNAMESE: 'Vietnamese',
  MIDDLE_EASTERN: 'Middle Eastern',
  CARIBBEAN: 'Caribbean',
  AFRICAN: 'African',
  SOUTH_AMERICAN: 'South American'
}

const cuisineTypesData = createEnumWithLabels(CUISINE_TYPES_LABELS_DATA)
export const CUISINE_TYPES = cuisineTypesData.enum
export const CUISINE_TYPES_LABELS = cuisineTypesData.labels

// Recipe Categories Labels
const RECIPE_CATEGORIES_LABELS_DATA = {
  BREAKFAST: 'Breakfast',
  LUNCH: 'Lunch',
  DINNER: 'Dinner',
  SNACK: 'Snack',
  DESSERT: 'Dessert',
  APPETIZER: 'Appetizer',
  BEVERAGE: 'Beverage'
}

const recipeCategoriesData = createEnumWithLabels(RECIPE_CATEGORIES_LABELS_DATA)
export const RECIPE_CATEGORIES = recipeCategoriesData.enum
export const RECIPE_CATEGORIES_LABELS = recipeCategoriesData.labels

// Difficulty Levels Labels
const DIFFICULTY_LEVELS_LABELS_DATA = {
  EASY: 'Easy',
  MEDIUM: 'Medium',
  HARD: 'Hard',
  EXPERT: 'Expert'
}

const difficultyLevelsData = createEnumWithLabels(DIFFICULTY_LEVELS_LABELS_DATA)
export const DIFFICULTY_LEVELS = difficultyLevelsData.enum
export const DIFFICULTY_LEVELS_LABELS = difficultyLevelsData.labels

// Goal Types Labels
const GOAL_TYPES_LABELS_DATA = {
  WEIGHT_LOSS: 'Weight Loss',
  WEIGHT_GAIN: 'Weight Gain',
  MUSCLE_GAIN: 'Muscle Gain',
  MAINTENANCE: 'Maintenance',
  ATHLETIC_PERFORMANCE: 'Athletic Performance',
  GENERAL_HEALTH: 'General Health'
}

const goalTypesData = createEnumWithLabels(GOAL_TYPES_LABELS_DATA)
export const GOAL_TYPES = goalTypesData.enum
export const GOAL_TYPES_LABELS = goalTypesData.labels

// Subscription Status Labels
const SUBSCRIPTION_STATUS_LABELS_DATA = {
  FREE: 'Free',
  PREMIUM: 'Premium',
  FAMILY: 'Family',
  CANCELLED: 'Cancelled',
  EXPIRED: 'Expired'
}

const subscriptionStatusData = createEnumWithLabels(SUBSCRIPTION_STATUS_LABELS_DATA)
export const SUBSCRIPTION_STATUS = subscriptionStatusData.enum
export const SUBSCRIPTION_STATUS_LABELS = subscriptionStatusData.labels

// Meal Types Labels
const MEAL_TYPES_LABELS_DATA = {
  BREAKFAST: 'Breakfast',
  LUNCH: 'Lunch',
  DINNER: 'Dinner',
  SNACK: 'Snack'
}

const mealTypesData = createEnumWithLabels(MEAL_TYPES_LABELS_DATA)
export const MEAL_TYPES = mealTypesData.enum
export const MEAL_TYPES_LABELS = mealTypesData.labels

// Date Formats Labels
const DATE_FORMATS_LABELS_DATA = {
  MM_DD_YYYY: 'MM/DD/YYYY',
  DD_MM_YYYY: 'DD/MM/YYYY',
  YYYY_MM_DD: 'YYYY-MM-DD',
  DD_MM_YYYY_DOT: 'DD.MM.YYYY'
}

const dateFormatsData = createEnumWithLabels(DATE_FORMATS_LABELS_DATA)
export const DATE_FORMATS = dateFormatsData.enum
export const DATE_FORMATS_LABELS = dateFormatsData.labels

// Measurement Systems Labels
const MEASUREMENT_SYSTEMS_LABELS_DATA = {
  METRIC: 'Metric',
  IMPERIAL: 'Imperial'
}

const measurementSystemsData = createEnumWithLabels(MEASUREMENT_SYSTEMS_LABELS_DATA)
export const MEASUREMENT_SYSTEMS = measurementSystemsData.enum
export const MEASUREMENT_SYSTEMS_LABELS = measurementSystemsData.labels

// First Day of Week Labels
const FIRST_DAY_OF_WEEK_LABELS_DATA = {
  SUNDAY: 'Sunday',
  MONDAY: 'Monday'
}

const firstDayOfWeekData = createEnumWithLabels(FIRST_DAY_OF_WEEK_LABELS_DATA)
export const FIRST_DAY_OF_WEEK = firstDayOfWeekData.enum
export const FIRST_DAY_OF_WEEK_LABELS = firstDayOfWeekData.labels

// Common Timezones (IANA timezone identifiers)
export const COMMON_TIMEZONES = [
  { value: 'UTC', label: 'UTC (Coordinated Universal Time)' },
  { value: 'America/New_York', label: 'Eastern Time (US & Canada)' },
  { value: 'America/Chicago', label: 'Central Time (US & Canada)' },
  { value: 'America/Denver', label: 'Mountain Time (US & Canada)' },
  { value: 'America/Los_Angeles', label: 'Pacific Time (US & Canada)' },
  { value: 'Europe/Copenhagen', label: 'Copenhagen' }
]

// Countries (ISO 3166-1 alpha-2 codes with names)
export const COUNTRIES = [
  { code: 'US', name: 'United States' },
  { code: 'DK', name: 'Denmark' }
]

// Currencies (ISO 4217 currency codes)
export const CURRENCIES = [
  { code: 'USD', name: 'US Dollar' },
  { code: 'DKK', name: 'Danish Krone' }
]

// Weight Units
export const WEIGHT_UNITS = {
  // Metric
  GRAM: 'g',
  KILOGRAM: 'kg',
  MILLIGRAM: 'mg',
  // Imperial
  OUNCE: 'oz',
  POUND: 'lb'
}

// Volume Units (universal, no conversion needed)
export const VOLUME_UNITS = {
  CUP: 'cup',
  TABLESPOON: 'tbsp',
  TEASPOON: 'tsp',
  MILLILITER: 'ml',
  LITER: 'l',
  FLUID_OUNCE: 'fl oz'
}

// Count Units (universal, no conversion needed)
export const COUNT_UNITS = {
  PIECE: 'piece',
  ITEM: 'item',
  WHOLE: 'whole'
}

// Unit Conversion Factors
export const UNIT_CONVERSIONS = {
  OUNCES_TO_GRAMS: 28.35,
  POUNDS_TO_GRAMS: 453.59,
  KILOGRAMS_TO_GRAMS: 1000.0,
  MILLIGRAMS_TO_GRAMS: 0.001
}
