import { localeService } from "@/services/localeService";

const localeConfig = {
  locales: [],
  countries: [],
  currencies: [],
  timezones: [],
  formattingRules: [],
  dateFormats: [],
  measurementSystems: [],
  weightUnits: [],
  volumeUnits: [],
  countUnits: [],
  firstDayOfWeek: [],
  mealCategories: [],
  dietaryRestrictions: [],
  allergies: [],
  cuisineTypes: [],
  goalTypes: [],
  subscriptionStatuses: [],
  mealTypes: [],
};

let configPromise = null;

const normalizeConfig = (config) => {
  if (!config) {
    return;
  }

  localeConfig.locales = config.locales || [];
  localeConfig.countries = config.countries || [];
  localeConfig.currencies = config.currencies || [];
  localeConfig.timezones = config.timezones || [];
  localeConfig.formattingRules = config.formattingRules || [];
  localeConfig.dateFormats = config.dateFormats || [];
  localeConfig.measurementSystems = config.measurementSystems || [];
  localeConfig.weightUnits = config.weightUnits || [];
  localeConfig.volumeUnits = config.volumeUnits || [];
  localeConfig.countUnits = config.countUnits || [];
  localeConfig.firstDayOfWeek = config.firstDayOfWeek || [];
  localeConfig.mealCategories = config.mealCategories || [];
  localeConfig.dietaryRestrictions = config.dietaryRestrictions || [];
  localeConfig.allergies = config.allergies || [];
  localeConfig.cuisineTypes = config.cuisineTypes || [];
  localeConfig.goalTypes = config.goalTypes || [];
  localeConfig.subscriptionStatuses = config.subscriptionStatuses || [];
  localeConfig.mealTypes = config.mealCategories || [];
};

export const ensureLocaleConfigLoaded = async (forceRefresh = false) => {
  if (forceRefresh) {
    configPromise = null;
  }
  
  if (!configPromise) {
    configPromise = localeService
      .getLocaleConfig(forceRefresh)
      .then((config) => {
        normalizeConfig(config);
        return localeConfig;
      })
      .catch((error) => {
        configPromise = null;
        throw error;
      });
  }

  return configPromise;
};

export const getCountries = () => localeConfig.countries;
export const getCurrencies = () => localeConfig.currencies;
export const getTimezones = () => localeConfig.timezones;
export const getFormattingRules = () => localeConfig.formattingRules;
export const getDateFormats = () => localeConfig.dateFormats;
export const getMeasurementSystems = () => localeConfig.measurementSystems;
export const getWeightUnits = () => localeConfig.weightUnits;
export const getVolumeUnits = () => localeConfig.volumeUnits;
export const getCountUnits = () => localeConfig.countUnits;
export const getFirstDayOfWeek = () => localeConfig.firstDayOfWeek;
export const getMealCategories = () => localeConfig.mealCategories;
export const getDietaryRestrictions = () => localeConfig.dietaryRestrictions;
export const getAllergies = () => localeConfig.allergies;
export const getCuisineTypes = () => localeConfig.cuisineTypes;
export const getGoalTypes = () => localeConfig.goalTypes;
export const getSubscriptionStatuses = () => localeConfig.subscriptionStatuses;
export const getMealTypes = () => localeConfig.mealTypes;

export const getDefaultCountry = () => getCountries()[0]?.code || null;
export const getDefaultTimezone = () => getTimezones()[0]?.value || null;
export const getDefaultDateFormatCode = () => getDateFormats()[0]?.code || null;
export const getDefaultMeasurementSystemCode = () =>
  getMeasurementSystems()[0]?.code || null;
export const getDefaultFirstDayOfWeekCode = () =>
  getFirstDayOfWeek()[0]?.code || null;
export const getDefaultGoalTypeCode = () => getGoalTypes()[0]?.code || null;
export const getDefaultSubscriptionStatusCode = () =>
  getSubscriptionStatuses()[0]?.code || null;
export const getDefaultMealTypeCodes = (count = 3) =>
  getMealTypes()
    .slice(0, count)
    .map((item) => item.code);

export const getFormattingRuleForCountry = (countryCode) => {
  if (!countryCode) {
    return null;
  }
  return (
    getFormattingRules().find(
      (rule) => rule.countryCode?.toUpperCase() === countryCode.toUpperCase(),
    ) || null
  );
};

export const getDecimalSeparatorForCountry = (countryCode) => {
  const rule = getFormattingRuleForCountry(countryCode);
  return rule?.decimalSeparator || ".";
};

export const getThousandsSeparatorForCountry = (countryCode) => {
  const rule = getFormattingRuleForCountry(countryCode);
  if (!rule) {
    return ",";
  }

  return rule.thousandsSeparator || ",";
};

export const getCurrencyForCountry = (countryCode) => {
  const rule = getFormattingRuleForCountry(countryCode);
  return rule?.currencyCode || null;
};

export const getFormatForDateCode = (code) => {
  if (!code) {
    return null;
  }
  return getDateFormats().find((item) => item.code === code)?.format || null;
};

export const getDateCodeForFormat = (format) => {
  if (!format) {
    return null;
  }
  return getDateFormats().find((item) => item.format === format)?.code || null;
};

export const getTokensForDateCode = (code) => {
  if (!code) {
    return null;
  }
  return getDateFormats().find((item) => item.code === code)?.tokens || null;
};

export const getTokensForDateFormat = (format) => {
  if (!format) {
    return null;
  }
  return (
    getDateFormats().find((item) => item.format === format)?.tokens || null
  );
};

export const findMeasurementSystemByCode = (code) => {
  if (!code) {
    return null;
  }
  return getMeasurementSystems().find((item) => item.code === code) || null;
};

export const findByCode = (list, code) => {
  if (!code || !Array.isArray(list)) {
    return null;
  }
  return list.find((item) => item.code === code) || null;
};

export const createEnumFromList = (list) => {
  if (!Array.isArray(list)) {
    return {};
  }
  return list.reduce((acc, item) => {
    if (item?.code) {
      acc[item.code] = item.code;
    }
    return acc;
  }, {});
};
