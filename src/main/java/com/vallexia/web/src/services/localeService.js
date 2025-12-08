import api from "./api";

class LocaleService {
  cachedLocales = null;
  fetchPromise = null;
  cachedConfig = null;
  configPromise = null;

  async getSupportedLocales(forceRefresh = false) {
    if (this.cachedLocales && !forceRefresh) {
      return this.cachedLocales;
    }

    if (this.fetchPromise && !forceRefresh) {
      return this.fetchPromise;
    }

    this.fetchPromise = api
      .get("/v1/locales")
      .then((response) => {
        this.cachedLocales = response.data || [];
        return this.cachedLocales;
      })
      .catch((error) => {
        // Clear cached promise so future calls can retry
        this.fetchPromise = null;
        throw error;
      });

    return this.fetchPromise;
  }

  async getLocaleConfig(forceRefresh = false) {
    if (this.cachedConfig && !forceRefresh) {
      return this.cachedConfig;
    }

    if (this.configPromise && !forceRefresh) {
      return this.configPromise;
    }

    this.configPromise = api
      .get("/v1/locales/config")
      .then((response) => {
        const data = response.data || {};
        this.cachedConfig = {
          locales: data.locales || [],
          countries: data.countries || [],
          currencies: data.currencies || [],
          timezones: data.timezones || [],
          formattingRules: data.formattingRules || [],
          dateFormats: data.dateFormats || [],
          measurementSystems: data.measurementSystems || [],
          weightUnits: data.weightUnits || [],
          volumeUnits: data.volumeUnits || [],
          countUnits: data.countUnits || [],
          firstDayOfWeek: data.firstDayOfWeek || [],
          mealCategories: data.mealCategories || [],
          dietaryRestrictions: data.dietaryRestrictions || [],
          allergies: data.allergies || [],
          cuisineTypes: data.cuisineTypes || [],
          difficultyLevels: data.difficultyLevels || [],
          goalTypes: data.goalTypes || [],
          subscriptionStatuses: data.subscriptionStatuses || [],
          mealTypes: data.mealCategories || [],
        };
        return this.cachedConfig;
      })
      .catch((error) => {
        this.configPromise = null;
        throw error;
      });

    return this.configPromise;
  }
}

export const localeService = new LocaleService();
