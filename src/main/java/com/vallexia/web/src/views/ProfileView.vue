<template>
  <div class="space-y-6">
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-gray-900">
        {{ $t("profile.title") }}
      </h1>
      <p class="mt-2 text-gray-600">{{ $t("profile.description") }}</p>
    </div>

    <!-- Profile Tabs -->
    <div class="bg-white shadow rounded-lg">
      <div class="border-b border-gray-200">
        <nav
          class="-mb-px flex space-x-8 px-6"
          :aria-label="$t('profile.tabs.ariaLabel')"
        >
          <button
            v-for="tab in tabs"
            :key="tab.id"
            :class="tabClasses(tab.id)"
            @click="activeTab = tab.id"
          >
            {{ tab.name }}
          </button>
        </nav>
      </div>

      <div class="p-6">
        <!-- Personal Information Tab -->
        <div v-if="activeTab === 'personal'" class="space-y-6">
          <h2 class="text-xl font-semibold text-gray-900">
            {{ $t("profile.personal.title") }}
          </h2>
          <p class="text-gray-600">{{ $t("profile.personal.description") }}</p>

          <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
            <FormInput
              id="email"
              v-model="personalForm.email"
              type="email"
              :label="$t('profile.personal.email')"
              :placeholder="$t('profile.personal.emailPlaceholder')"
              :error="personalErrors.email"
            />

            <FormInput
              id="householdSize"
              v-model.number="personalForm.householdSize"
              type="number"
              :label="$t('profile.personal.householdSize')"
              :placeholder="$t('profile.personal.householdSizePlaceholder')"
              :error="personalErrors.householdSize"
              :min="1"
              :max="20"
            />
          </div>

          <!-- Meal Types Selection -->
          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-700">
              {{ $t("profile.personal.mealTypes") }}
              <span class="text-red-500">*</span>
            </label>
            <div class="grid grid-cols-2 gap-3 sm:grid-cols-4">
              <label
                v-for="mealType in translatedMealTypes"
                :key="mealType.code"
                class="flex items-center p-3 border rounded-lg cursor-pointer hover:bg-gray-50 transition-colors"
                :class="
                  personalForm.mealTypes.includes(mealType.code)
                    ? 'border-blue-500 bg-blue-50'
                    : 'border-gray-300'
                "
              >
                <input
                  v-model="personalForm.mealTypes"
                  type="checkbox"
                  :value="mealType.code"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <span class="ml-2 text-sm text-gray-700">{{
                  mealType.name
                }}</span>
              </label>
            </div>
            <p v-if="personalErrors.mealTypes" class="text-sm text-red-600">
              {{ personalErrors.mealTypes }}
            </p>
            <p v-else class="text-sm text-gray-500">
              {{ $t("profile.personal.mealTypesDescription") }}
            </p>
          </div>

          <!-- Subscription Status Display -->
          <div class="mt-6 p-4 bg-gray-50 rounded-lg">
            <h3 class="text-sm font-medium text-gray-900 mb-2">
              {{ $t("profile.personal.subscriptionStatus") }}
            </h3>
            <div class="flex items-center">
              <span
                :class="subscriptionStatusClasses"
                class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
              >
                {{ subscriptionStatusLabel }}
              </span>
              <span
                v-if="personalForm.subscriptionExpiresAt"
                class="ml-2 text-sm text-gray-500"
              >
                {{
                  $t("common.expiresAt", {
                    date: formatDate(personalForm.subscriptionExpiresAt),
                  })
                }}
              </span>
            </div>
          </div>

          <div class="flex justify-end">
            <button
              :disabled="isPersonalLoading"
              class="btn btn-primary"
              @click="updatePersonalInfo"
            >
              <LoadingSpinner
                v-if="isPersonalLoading"
                size="small"
                color="white"
              />
              <span v-else>{{ $t("common.saveChanges") }}</span>
            </button>
          </div>
        </div>

        <!-- Dietary Preferences Tab -->
        <div v-if="activeTab === 'dietary'" class="space-y-6">
          <h2 class="text-xl font-semibold text-gray-900">
            {{ $t("profile.dietary.title") }}
          </h2>
          <p class="text-gray-600">{{ $t("profile.dietary.description") }}</p>

          <DietaryRestrictionsSelector
            id="dietary-restrictions"
            v-model="dietaryForm.restrictions"
            :error="dietaryErrors.restrictions"
          />

          <AllergiesSelector
            id="allergies"
            v-model="dietaryForm.allergies"
            :error="dietaryErrors.allergies"
          />

          <CuisinePreferencesSelector
            id="cuisine-preferences"
            v-model="dietaryForm.preferredCuisines"
            :error="dietaryErrors.preferredCuisines"
          />

          <div class="flex justify-end">
            <button
              :disabled="isDietaryLoading"
              class="btn btn-primary"
              @click="updateDietaryPreferences"
            >
              <LoadingSpinner
                v-if="isDietaryLoading"
                size="small"
                color="white"
              />
              <span v-else>{{ $t("profile.dietary.savePreferences") }}</span>
            </button>
          </div>
        </div>

        <!-- Nutritional Goals Tab -->
        <div v-if="activeTab === 'nutritional'" class="space-y-6">
          <h2 class="text-xl font-semibold text-gray-900">
            {{ $t("profile.nutritional.title") }}
          </h2>
          <p class="text-gray-600">
            {{ $t("profile.nutritional.description") }}
          </p>

          <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
            <NutritionGoalInput
              id="daily-calories"
              v-model="nutritionForm.dailyCalories"
              :label="$t('profile.nutritional.dailyCalories')"
              unit="cal"
              :min="800"
              :max="5000"
              :error="nutritionErrors.dailyCalories"
              @blur="handleDailyCaloriesChange"
            />

            <div class="space-y-1">
              <label class="block text-sm font-medium text-gray-700">{{
                $t("profile.nutritional.goalType")
              }}</label>
              <select
                v-model="nutritionForm.goalType"
                class="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
                @change="handleGoalTypeChange"
              >
                <option
                  v-for="option in translatedGoalTypes"
                  :key="option.code"
                  :value="option.code"
                >
                  {{ option.name }}
                </option>
              </select>
            </div>
          </div>

          <div class="grid grid-cols-1 gap-6 sm:grid-cols-3">
            <NutritionGoalInput
              id="daily-protein"
              v-model="nutritionForm.dailyProtein"
              :label="$t('profile.nutritional.dailyProtein')"
              unit="g"
              :min="0"
              :max="500"
              :show-percentage="true"
              :daily-calories="normalizedDailyCalories"
              :macro-calories="nutritionForm.proteinCalories"
              macro-type="protein"
              :error="nutritionErrors.dailyProtein"
            />

            <NutritionGoalInput
              id="daily-carbs"
              v-model="nutritionForm.dailyCarbs"
              :label="$t('profile.nutritional.dailyCarbs')"
              unit="g"
              :min="0"
              :max="1000"
              :show-percentage="true"
              :daily-calories="normalizedDailyCalories"
              :macro-calories="nutritionForm.carbCalories"
              macro-type="carbs"
              :error="nutritionErrors.dailyCarbs"
            />

            <NutritionGoalInput
              id="daily-fats"
              v-model="nutritionForm.dailyFats"
              :label="$t('profile.nutritional.dailyFats')"
              unit="g"
              :min="0"
              :max="500"
              :show-percentage="true"
              :daily-calories="normalizedDailyCalories"
              :macro-calories="nutritionForm.fatCalories"
              macro-type="fats"
              :error="nutritionErrors.dailyFats"
            />
          </div>

          <div class="grid grid-cols-1 gap-6 sm:grid-cols-3">
            <NutritionGoalInput
              id="daily-fiber"
              v-model="nutritionForm.dailyFiber"
              :label="$t('profile.nutritional.dailyFiber')"
              unit="g"
              :min="0"
              :max="100"
              :error="nutritionErrors.dailyFiber"
            />

            <NutritionGoalInput
              id="daily-sodium"
              v-model="nutritionForm.dailySodium"
              :label="$t('profile.nutritional.dailySodium')"
              unit="mg"
              :min="0"
              :max="10000"
              :error="nutritionErrors.dailySodium"
            />

            <NutritionGoalInput
              id="daily-sugar"
              v-model="nutritionForm.dailySugar"
              :label="$t('profile.nutritional.dailySugar')"
              unit="g"
              :min="0"
              :max="200"
              :error="nutritionErrors.dailySugar"
            />
          </div>

          <div class="flex justify-end">
            <button
              :disabled="isNutritionLoading"
              class="btn btn-primary"
              @click="updateNutritionalGoals"
            >
              <LoadingSpinner
                v-if="isNutritionLoading"
                size="small"
                color="white"
              />
              <span v-else>{{ $t("profile.nutritional.saveGoals") }}</span>
            </button>
          </div>
        </div>

        <!-- Settings Tab -->
        <div v-if="activeTab === 'settings'" class="space-y-6">
          <h2 class="text-xl font-semibold text-gray-900">
            {{ $t("profile.settings.title") }}
          </h2>
          <p class="text-gray-600">{{ $t("profile.settings.description") }}</p>

          <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
            <!-- Language Selector -->
            <div class="space-y-1">
              <label class="block text-sm font-medium text-gray-700">{{
                $t("profile.settings.language")
              }}</label>
              <select
                v-model="settingsForm.language"
                class="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
              >
                <option
                  v-for="lang in translatedLanguages"
                  :key="lang.code"
                  :value="lang.code"
                >
                  {{ lang.name }}
                </option>
              </select>
              <p v-if="settingsErrors.language" class="text-sm text-red-600">
                {{ settingsErrors.language }}
              </p>
            </div>

            <!-- Country Selector -->
            <div class="space-y-1">
              <label class="block text-sm font-medium text-gray-700">{{
                $t("profile.settings.country")
              }}</label>
              <select
                v-model="settingsForm.country"
                class="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
              >
                <option
                  v-for="country in translatedCountries"
                  :key="country.code"
                  :value="country.code"
                >
                  {{ country.name }}
                </option>
              </select>
              <p v-if="settingsErrors.country" class="text-sm text-red-600">
                {{ settingsErrors.country }}
              </p>
            </div>

            <!-- Date Format Selector -->
            <div class="space-y-1">
              <label class="block text-sm font-medium text-gray-700">{{
                $t("profile.settings.dateFormat")
              }}</label>
              <select
                v-model="settingsForm.dateFormat"
                class="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
              >
                <option
                  v-for="dateFormat in dateFormatOptions"
                  :key="dateFormat.code"
                  :value="dateFormat.code"
                >
                  {{ dateFormat.format }}
                </option>
              </select>
              <p v-if="settingsErrors.dateFormat" class="text-sm text-red-600">
                {{ settingsErrors.dateFormat }}
              </p>
            </div>

            <!-- Timezone Selector -->
            <div class="space-y-1">
              <label class="block text-sm font-medium text-gray-700">{{
                $t("profile.settings.timezone")
              }}</label>
              <select
                v-model="settingsForm.timezone"
                class="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
              >
                <option
                  v-for="tz in timezoneOptions"
                  :key="tz.value"
                  :value="tz.value"
                >
                  {{ tz.label }}
                </option>
              </select>
              <p v-if="settingsErrors.timezone" class="text-sm text-red-600">
                {{ settingsErrors.timezone }}
              </p>
            </div>

            <!-- First Day of Week Selector -->
            <div class="space-y-1">
              <label class="block text-sm font-medium text-gray-700">{{
                $t("profile.settings.firstDayOfWeekLabel")
              }}</label>
              <div class="flex space-x-4">
                <label
                  v-for="day in translatedFirstDayOptions"
                  :key="day.code"
                  class="flex items-center"
                >
                  <input
                    v-model="settingsForm.firstDayOfWeek"
                    type="radio"
                    :value="day.code"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300"
                  />
                  <span class="ml-2 text-sm text-gray-700">{{ day.name }}</span>
                </label>
              </div>
              <p
                v-if="settingsErrors.firstDayOfWeek"
                class="text-sm text-red-600"
              >
                {{ settingsErrors.firstDayOfWeek }}
              </p>
            </div>

            <!-- Measurement System Selector -->
            <div class="space-y-1">
              <label class="block text-sm font-medium text-gray-700">{{
                $t("profile.settings.measurementSystem")
              }}</label>
              <div class="flex space-x-4">
                <label
                  v-for="system in translatedMeasurementSystems"
                  :key="system.code"
                  class="flex items-center"
                >
                  <input
                    v-model="settingsForm.measurementSystem"
                    type="radio"
                    :value="system.code"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300"
                  />
                  <span class="ml-2 text-sm text-gray-700">{{
                    system.name
                  }}</span>
                </label>
              </div>
              <p
                v-if="settingsErrors.measurementSystem"
                class="text-sm text-red-600"
              >
                {{ settingsErrors.measurementSystem }}
              </p>
            </div>

            <!-- Currency Selector -->
            <div class="space-y-1">
              <label class="block text-sm font-medium text-gray-700">{{
                $t("profile.settings.currency")
              }}</label>
              <select
                v-model="settingsForm.currency"
                class="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
              >
                <option
                  v-for="currency in currencyOptions"
                  :key="currency.code"
                  :value="currency.code"
                >
                  {{ currency.name }} ({{ currency.code }})
                </option>
              </select>
              <p v-if="settingsErrors.currency" class="text-sm text-red-600">
                {{ settingsErrors.currency }}
              </p>
            </div>
          </div>

          <div class="flex justify-end">
            <button
              :disabled="isSettingsLoading"
              class="btn btn-primary"
              @click="updateSettings"
            >
              <LoadingSpinner
                v-if="isSettingsLoading"
                size="small"
                color="white"
              />
              <span v-else>{{ $t("profile.settings.saveSettings") }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Toast Notifications -->
    <div class="fixed top-4 right-4 z-50">
      <Toast
        :show="toast.show"
        :type="toast.type"
        :title="toast.title"
        :message="toast.message"
        @dismiss="toast.show = false"
      />
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import { userService } from "@/services/userService";
import FormInput from "@/components/common/FormInput.vue";
import LoadingSpinner from "@/components/common/LoadingSpinner.vue";
import DietaryRestrictionsSelector from "@/components/profile/DietaryRestrictionsSelector.vue";
import AllergiesSelector from "@/components/profile/AllergiesSelector.vue";
import CuisinePreferencesSelector from "@/components/profile/CuisinePreferencesSelector.vue";
import NutritionGoalInput from "@/components/profile/NutritionGoalInput.vue";
import Toast from "@/components/common/Toast.vue";
import {
  SUPPORTED_LANGUAGES,
  getDefaultLanguage,
  getSupportedLanguageCodes,
} from "@/i18n";
import {
  getCountries,
  getTimezones,
  getDefaultCountry,
  getDefaultTimezone,
  getDateFormats,
  getDefaultDateFormatCode,
  getMeasurementSystems,
  getDefaultMeasurementSystemCode,
  getCurrencies,
  getMealTypes,
  getFirstDayOfWeek,
  getDefaultFirstDayOfWeekCode,
  getDietaryRestrictions,
  getAllergies,
  getCuisineTypes,
  getGoalTypes,
  getSubscriptionStatuses,
  getDefaultGoalTypeCode,
  getDefaultSubscriptionStatusCode,
  getDefaultMealTypeCodes,
  createEnumFromList,
  ensureLocaleConfigLoaded,
} from "@/utils/localeConfig";
import { useSettingsStore } from "@/stores/settings";
import { getErrorMessage } from "@/utils/errorUtils";
import {
  validateValue,
  filterValidValues,
  validateEnumValue,
} from "@/utils/validationUtils";
import { getSubscriptionDisplayName } from "@/utils/subscriptionUtils";

const { t, te } = useI18n();
const settingsStore = useSettingsStore();

const activeTab = ref("personal");
const isPersonalLoading = ref(false);
const isDietaryLoading = ref(false);
const isNutritionLoading = ref(false);
const isSettingsLoading = ref(false);

// Toast state
const toast = reactive({
  show: false,
  type: "success",
  title: "",
  message: "",
});

const countryOptions = ref([]);
const timezoneOptions = ref([]);
const dateFormatOptions = ref([]);
const measurementOptions = ref([]);
const currencyOptions = ref([]);
const mealTypeOptions = ref([]);
const firstDayOptions = ref([]);
const dietaryRestrictionOptions = ref([]);
const allergyOptions = ref([]);
const cuisineTypeOptions = ref([]);
const goalTypeOptions = ref([]);
const subscriptionStatusOptions = ref([]);

const refreshLocaleOptions = () => {
  countryOptions.value = [...getCountries()];
  timezoneOptions.value = [...getTimezones()];
  dateFormatOptions.value = [...getDateFormats()];
  measurementOptions.value = [...getMeasurementSystems()];
  currencyOptions.value = [...getCurrencies()];
  mealTypeOptions.value = [...getMealTypes()];
  firstDayOptions.value = [...getFirstDayOfWeek()];
  dietaryRestrictionOptions.value = [...getDietaryRestrictions()];
  allergyOptions.value = [...getAllergies()];
  cuisineTypeOptions.value = [...getCuisineTypes()];
  goalTypeOptions.value = [...getGoalTypes()];
  subscriptionStatusOptions.value = [...getSubscriptionStatuses()];
};

const tabs = computed(() => [
  { id: "personal", name: t("profile.tabs.personal") },
  { id: "dietary", name: t("profile.tabs.dietary") },
  { id: "nutritional", name: t("profile.tabs.nutritional") },
  { id: "settings", name: t("profile.tabs.settings") },
]);

// Personal Info Form
const defaultGoalTypeCode = getDefaultGoalTypeCode() || "MAINTENANCE";
const defaultSubscriptionStatusCode =
  getDefaultSubscriptionStatusCode() || "FREE";
const defaultMealTypeSelection = getDefaultMealTypeCodes();

const personalForm = reactive({
  email: "",
  householdSize: 1,
  mealTypes: defaultMealTypeSelection,
  subscriptionStatus: defaultSubscriptionStatusCode,
  subscriptionExpiresAt: null,
});

const personalErrors = reactive({
  email: "",
  householdSize: "",
  mealTypes: "",
});

// Dietary Preferences Form
const dietaryForm = reactive({
  restrictions: [],
  allergies: [],
  preferredCuisines: [],
});

const dietaryErrors = reactive({
  restrictions: "",
  allergies: "",
  preferredCuisines: "",
});

// Nutritional Goals Form
const nutritionForm = reactive({
  dailyCalories: 2000,
  dailyProtein: 150,
  dailyCarbs: 250,
  dailyFats: 67,
  dailyFiber: 25,
  dailySodium: 2300,
  dailySugar: 50,
  goalType: defaultGoalTypeCode,
  proteinCalories: null,
  carbCalories: null,
  fatCalories: null,
});

// Computed property to ensure dailyCalories is always a number or null
const normalizedDailyCalories = computed(() => {
  const value = nutritionForm.dailyCalories;
  if (value === null || value === undefined || value === "") {
    return null;
  }
  const numValue = Number(value);
  return isNaN(numValue) ? null : numValue;
});

const nutritionErrors = reactive({
  dailyCalories: "",
  dailyProtein: "",
  dailyCarbs: "",
  dailyFats: "",
  dailyFiber: "",
  dailySodium: "",
  dailySugar: "",
  goalType: "",
});

// Settings Form
const defaultFirstDayCode = getDefaultFirstDayOfWeekCode() || "MONDAY";

const settingsForm = reactive({
  language: getDefaultLanguage(),
  country: getDefaultCountry(),
  dateFormat: getDefaultDateFormatCode(),
  timezone: getDefaultTimezone(),
  firstDayOfWeek: defaultFirstDayCode,
  measurementSystem: getDefaultMeasurementSystemCode(),
  currency: null,
});

const settingsErrors = reactive({
  language: "",
  country: "",
  dateFormat: "",
  timezone: "",
  firstDayOfWeek: "",
  measurementSystem: "",
  currency: "",
});

// Computed properties
const translatedLanguages = computed(() => {
  return SUPPORTED_LANGUAGES.map((lang) => {
    const key = `constants.languages.${lang.code}`;
    return {
      code: lang.code,
      name: te(key) ? t(key) : lang.name,
    };
  });
});

const translatedCountries = computed(() => {
  return countryOptions.value.map((country) => {
    const key = `constants.countries.${country.code}`;
    return {
      code: country.code,
      name: te(key) ? t(key) : country.name,
    };
  });
});

const translatedMeasurementSystems = computed(() => {
  return measurementOptions.value.map((system) => {
    const key = `constants.measurementSystems.${system.code}`;
    return {
      code: system.code,
      name: te(key) ? t(key) : system.name,
    };
  });
});

const translatedMealTypes = computed(() => {
  return mealTypeOptions.value.map((type) => {
    const key = `constants.mealTypes.${type.code}`;
    return {
      code: type.code,
      name: te(key) ? t(key) : type.name,
    };
  });
});

const translatedFirstDayOptions = computed(() => {
  return firstDayOptions.value.map((option) => {
    const key = `constants.firstDayOfWeek.${option.code}`;
    return {
      code: option.code,
      name: te(key) ? t(key) : option.name,
    };
  });
});

const translatedGoalTypes = computed(() => {
  return goalTypeOptions.value.map((option) => {
    const key = `constants.goalTypes.${option.code}`;
    return {
      code: option.code,
      name: te(key) ? t(key) : option.name,
    };
  });
});

const subscriptionStatusEnum = computed(() =>
  createEnumFromList(subscriptionStatusOptions.value),
);
const goalTypeEnum = computed(() => createEnumFromList(goalTypeOptions.value));

const subscriptionStatusLabel = computed(() => {
  return getSubscriptionDisplayName(personalForm.subscriptionStatus);
});

const subscriptionStatusClasses = computed(() => {
  const status = personalForm.subscriptionStatus;
  switch (status) {
    case "FREE":
      return "bg-gray-100 text-gray-800";
    case "PREMIUM":
      return "bg-blue-100 text-blue-800";
    case "FAMILY":
      return "bg-green-100 text-green-800";
    case "CANCELLED":
      return "bg-yellow-100 text-yellow-800";
    case "EXPIRED":
      return "bg-red-100 text-red-800";
    default:
      return "bg-gray-100 text-gray-800";
  }
});

// Methods
const tabClasses = (tabId) => {
  const baseClasses =
    "whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm";
  const activeClasses = "border-blue-500 text-blue-600";
  const inactiveClasses =
    "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300";

  return `${baseClasses} ${activeTab.value === tabId ? activeClasses : inactiveClasses}`;
};

const formatDate = (dateString) => {
  if (!dateString) return "";
  return settingsStore.formatDateFn(new Date(dateString));
};

const showToast = (type, title, message) => {
  toast.type = type;
  toast.title = title;
  toast.message = message;
  toast.show = true;
};

const updatePersonalInfo = async () => {
  isPersonalLoading.value = true;

  try {
    await userService.updateProfile(personalForm);
    showToast(
      "success",
      t("common.success"),
      t("profile.settings.personalInfoUpdatedSuccess"),
    );
  } catch (error) {
    const errorMessage = getErrorMessage(error);
    showToast("error", t("common.error"), errorMessage);
  } finally {
    isPersonalLoading.value = false;
  }
};

const updateDietaryPreferences = async () => {
  isDietaryLoading.value = true;

  try {
    await userService.updateDietaryPreferences(dietaryForm);
    showToast(
      "success",
      t("common.success"),
      t("profile.settings.dietaryPreferencesUpdatedSuccess"),
    );
  } catch (error) {
    const errorMessage = getErrorMessage(error);
    showToast("error", t("common.error"), errorMessage);
  } finally {
    isDietaryLoading.value = false;
  }
};

const handleDailyCaloriesChange = async () => {
  if (!nutritionForm.goalType) {
    return;
  }

  if (!nutritionForm.dailyCalories) {
    showToast(
      "warning",
      t("common.warning"),
      t("profile.nutritional.setDailyCaloriesFirst"),
    );
    return;
  }

  try {
    const breakdown = await userService.calculateMacrosFromGoalType(
      nutritionForm.dailyCalories,
      nutritionForm.goalType,
    );

    nutritionForm.dailyProtein = breakdown.protein;
    nutritionForm.dailyCarbs = breakdown.carbs;
    nutritionForm.dailyFats = breakdown.fats;

    const goalTypeName =
      translatedGoalTypes.value.find((gt) => gt.code === nutritionForm.goalType)
        ?.name || nutritionForm.goalType;
    showToast(
      "success",
      t("common.success"),
      t("profile.nutritional.macrosRecalculated", { goalType: goalTypeName }),
    );
  } catch (error) {
    const errorMessage = getErrorMessage(error);
    showToast("error", t("common.error"), errorMessage);
  }
};

const handleGoalTypeChange = async () => {
  if (!nutritionForm.dailyCalories || !nutritionForm.goalType) {
    showToast(
      "warning",
      t("common.warning"),
      t("profile.nutritional.setDailyCaloriesFirst"),
    );
    return;
  }

  try {
    const breakdown = await userService.calculateMacrosFromGoalType(
      nutritionForm.dailyCalories,
      nutritionForm.goalType,
    );

    nutritionForm.dailyProtein = breakdown.protein;
    nutritionForm.dailyCarbs = breakdown.carbs;
    nutritionForm.dailyFats = breakdown.fats;

    const goalTypeName =
      translatedGoalTypes.value.find((gt) => gt.code === nutritionForm.goalType)
        ?.name || nutritionForm.goalType;
    showToast(
      "success",
      t("common.success"),
      t("profile.nutritional.macrosCalculated", { goalType: goalTypeName }),
    );
  } catch (error) {
    const errorMessage = getErrorMessage(error);
    showToast("error", t("common.error"), errorMessage);
  }
};

const updateNutritionalGoals = async () => {
  isNutritionLoading.value = true;

  try {
    const updatedGoals =
      await userService.updateNutritionalGoals(nutritionForm);
    nutritionForm.proteinCalories = updatedGoals.proteinCalories;
    nutritionForm.carbCalories = updatedGoals.carbCalories;
    nutritionForm.fatCalories = updatedGoals.fatCalories;
    showToast(
      "success",
      t("common.success"),
      t("profile.settings.nutritionalGoalsUpdatedSuccess"),
    );
  } catch (error) {
    const errorMessage = getErrorMessage(error);
    showToast("error", t("common.error"), errorMessage);
  } finally {
    isNutritionLoading.value = false;
  }
};

const updateSettings = async () => {
  isSettingsLoading.value = true;

  try {
    const settingsData = { ...settingsForm };
    await settingsStore.updateSettings(settingsData);
    showToast(
      "success",
      t("common.success"),
      t("profile.settings.settingsUpdatedSuccess"),
    );
  } catch (error) {
    const errorMessage = getErrorMessage(error);
    showToast("error", t("common.error"), errorMessage);
  } finally {
    isSettingsLoading.value = false;
  }
};

onMounted(async () => {
  await ensureLocaleConfigLoaded();
  refreshLocaleOptions();

  try {
    // Load personal profile
    const profile = await userService.getProfile();
    personalForm.email = profile.email || "";
    personalForm.householdSize = profile.householdSize || 1;

    // Validate mealTypes array - filter invalid values and ensure at least one valid meal type
    const validMealTypes = filterValidValues(
      profile.mealTypes || [],
      mealTypeOptions.value,
      "mealTypes",
    );
    const fallbackMealTypes = mealTypeOptions.value
      .slice(0, 3)
      .map((type) => type.code);
    personalForm.mealTypes =
      validMealTypes.length > 0 ? validMealTypes : fallbackMealTypes;

    // Validate subscriptionStatus
    personalForm.subscriptionStatus = validateEnumValue(
      profile.subscriptionStatus,
      subscriptionStatusEnum.value,
      defaultSubscriptionStatusCode,
      "subscriptionStatus",
    );
    personalForm.subscriptionExpiresAt = profile.subscriptionExpiresAt || null;

    // Load dietary preferences
    const dietaryPreferences = await userService.getDietaryPreferences();

    // Filter restrictions array to only include valid values
    dietaryForm.restrictions = filterValidValues(
      dietaryPreferences.restrictions || [],
      dietaryRestrictionOptions.value,
      "dietary restrictions",
    );

    // Filter allergies array to only include valid values
    dietaryForm.allergies = filterValidValues(
      dietaryPreferences.allergies || [],
      allergyOptions.value,
      "allergies",
    );

    // Filter preferredCuisines array to only include valid values
    dietaryForm.preferredCuisines = filterValidValues(
      dietaryPreferences.preferredCuisines || [],
      cuisineTypeOptions.value,
      "preferred cuisines",
    );

    // Load nutritional goals
    const nutritionalGoals = await userService.getNutritionalGoals();
    nutritionForm.dailyCalories = nutritionalGoals.dailyCalories
      ? Number(nutritionalGoals.dailyCalories)
      : 2000;
    nutritionForm.dailyProtein = nutritionalGoals.dailyProtein
      ? Number(nutritionalGoals.dailyProtein)
      : 150;
    nutritionForm.dailyCarbs = nutritionalGoals.dailyCarbs
      ? Number(nutritionalGoals.dailyCarbs)
      : 250;
    nutritionForm.dailyFats = nutritionalGoals.dailyFats
      ? Number(nutritionalGoals.dailyFats)
      : 67;
    nutritionForm.dailyFiber = nutritionalGoals.dailyFiber
      ? Number(nutritionalGoals.dailyFiber)
      : 25;
    nutritionForm.dailySodium = nutritionalGoals.dailySodium
      ? Number(nutritionalGoals.dailySodium)
      : 2300;
    nutritionForm.dailySugar = nutritionalGoals.dailySugar
      ? Number(nutritionalGoals.dailySugar)
      : 50;
    nutritionForm.proteinCalories = nutritionalGoals.proteinCalories;
    nutritionForm.carbCalories = nutritionalGoals.carbCalories;
    nutritionForm.fatCalories = nutritionalGoals.fatCalories;

    // Validate goalType
    nutritionForm.goalType = validateEnumValue(
      nutritionalGoals.goalType,
      goalTypeEnum.value,
      defaultGoalTypeCode,
      "goalType",
    );

    // Load settings
    const settings = await settingsStore.loadSettings();
    if (settings) {
      // Validate language against SUPPORTED_LANGUAGES
      settingsForm.language = validateValue(
        settings.language,
        getSupportedLanguageCodes(),
        getDefaultLanguage(),
        "language",
      );

      // Validate country against supported countries
      settingsForm.country = validateValue(
        settings.country,
        countryOptions.value,
        getDefaultCountry(),
        "country",
      );

      const dateFormatCode = settings.dateFormat || getDefaultDateFormatCode();
      settingsForm.dateFormat = validateValue(
        dateFormatCode,
        dateFormatOptions.value,
        getDefaultDateFormatCode(),
        "dateFormat",
      );

      // Validate timezone against supported timezones
      settingsForm.timezone = validateValue(
        settings.timezone,
        timezoneOptions.value,
        getDefaultTimezone(),
        "timezone",
      );

      // Validate firstDayOfWeek
      settingsForm.firstDayOfWeek = validateValue(
        settings.firstDayOfWeek,
        firstDayOptions.value,
        defaultFirstDayCode,
        "firstDayOfWeek",
      );

      // Validate measurementSystem
      settingsForm.measurementSystem = validateValue(
        settings.measurementSystem,
        measurementOptions.value,
        getDefaultMeasurementSystemCode(),
        "measurementSystem",
      );

      // Validate currency (default to null for country default)
      settingsForm.currency = validateValue(
        settings.currency,
        currencyOptions.value.map((c) => c.code),
        null,
        "currency",
      );
    }
  } catch (error) {
    const errorMessage = getErrorMessage(error);
    showToast("error", t("common.error"), errorMessage);
  }
});
</script>
