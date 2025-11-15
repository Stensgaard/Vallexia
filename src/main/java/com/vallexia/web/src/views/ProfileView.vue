<template>
  <div class="space-y-6">
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-gray-900">{{ $t('profile.title') }}</h1>
      <p class="mt-2 text-gray-600">{{ $t('profile.description') }}</p>
    </div>

    <!-- Profile Tabs -->
    <div class="bg-white shadow rounded-lg">
      <div class="border-b border-gray-200">
        <nav class="-mb-px flex space-x-8 px-6" :aria-label="$t('profile.tabs.ariaLabel')">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            @click="activeTab = tab.id"
            :class="tabClasses(tab.id)"
          >
            {{ tab.name }}
          </button>
        </nav>
      </div>

      <div class="p-6">
        <!-- Personal Information Tab -->
        <div v-if="activeTab === 'personal'" class="space-y-6">
          <h2 class="text-xl font-semibold text-gray-900">{{ $t('profile.personal.title') }}</h2>
          <p class="text-gray-600">{{ $t('profile.personal.description') }}</p>
          
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
              {{ $t('profile.personal.mealTypes') }}
              <span class="text-red-500">*</span>
            </label>
            <div class="grid grid-cols-2 gap-3 sm:grid-cols-4">
              <label
                v-for="type in Object.keys(MEAL_TYPES)"
                :key="type"
                class="flex items-center p-3 border rounded-lg cursor-pointer hover:bg-gray-50 transition-colors"
                :class="personalForm.mealTypes.includes(type) ? 'border-blue-500 bg-blue-50' : 'border-gray-300'"
              >
                <input
                  type="checkbox"
                  :value="type"
                  v-model="personalForm.mealTypes"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <span class="ml-2 text-sm text-gray-700">{{ $t(`constants.mealTypes.${type}`) }}</span>
              </label>
            </div>
            <p v-if="personalErrors.mealTypes" class="text-sm text-red-600">{{ personalErrors.mealTypes }}</p>
            <p v-else class="text-sm text-gray-500">{{ $t('profile.personal.mealTypesDescription') }}</p>
          </div>
          
          <!-- Subscription Status Display -->
          <div class="mt-6 p-4 bg-gray-50 rounded-lg">
            <h3 class="text-sm font-medium text-gray-900 mb-2">{{ $t('profile.personal.subscriptionStatus') }}</h3>
            <div class="flex items-center">
              <span 
                :class="subscriptionStatusClasses"
                class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
              >
                {{ subscriptionStatusLabel }}
              </span>
              <span v-if="personalForm.subscriptionExpiresAt" class="ml-2 text-sm text-gray-500">
                {{ $t('common.expiresAt', { date: formatDate(personalForm.subscriptionExpiresAt) }) }}
              </span>
            </div>
          </div>
          
          <div class="flex justify-end">
            <button
              @click="updatePersonalInfo"
              :disabled="isPersonalLoading"
              class="btn btn-primary"
            >
              <LoadingSpinner v-if="isPersonalLoading" size="small" color="white" />
              <span v-else>{{ $t('common.saveChanges') }}</span>
            </button>
          </div>
        </div>

        <!-- Dietary Preferences Tab -->
        <div v-if="activeTab === 'dietary'" class="space-y-6">
          <h2 class="text-xl font-semibold text-gray-900">{{ $t('profile.dietary.title') }}</h2>
          <p class="text-gray-600">{{ $t('profile.dietary.description') }}</p>
          
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
              @click="updateDietaryPreferences"
              :disabled="isDietaryLoading"
              class="btn btn-primary"
            >
              <LoadingSpinner v-if="isDietaryLoading" size="small" color="white" />
              <span v-else>{{ $t('profile.dietary.savePreferences') }}</span>
            </button>
          </div>
        </div>

        <!-- Nutritional Goals Tab -->
        <div v-if="activeTab === 'nutritional'" class="space-y-6">
          <h2 class="text-xl font-semibold text-gray-900">{{ $t('profile.nutritional.title') }}</h2>
          <p class="text-gray-600">{{ $t('profile.nutritional.description') }}</p>
          
          <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
            <NutritionGoalInput
              id="daily-calories"
              v-model="nutritionForm.dailyCalories"
              :label="$t('profile.nutritional.dailyCalories')"
              unit="cal"
              :min="800"
              :max="5000"
              :error="nutritionErrors.dailyCalories"
            />
            
            <div class="space-y-1">
              <label class="block text-sm font-medium text-gray-700">{{ $t('profile.nutritional.goalType') }}</label>
              <select
                v-model="nutritionForm.goalType"
                class="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
              >
                <option v-for="(value, key) in goalTypeOptions" :key="key" :value="value">
                  {{ getGoalTypeLabel(value) }}
                </option>
              </select>
            </div>
          </div>
          
          <div class="grid grid-cols-1 gap-6 sm:grid-cols-3">
            <NutritionGoalInput
              id="daily-protein"
              v-model="nutritionForm.dailyProtein"
              :label="$t('profile.nutritional.dailyProtein')"
              :min="0"
              :max="500"
              :show-percentage="true"
              :daily-calories="nutritionForm.dailyCalories"
              :calories-per-gram="4"
              :error="nutritionErrors.dailyProtein"
            />
            
            <NutritionGoalInput
              id="daily-carbs"
              v-model="nutritionForm.dailyCarbs"
              :label="$t('profile.nutritional.dailyCarbs')"
              :min="0"
              :max="1000"
              :show-percentage="true"
              :daily-calories="nutritionForm.dailyCalories"
              :calories-per-gram="4"
              :error="nutritionErrors.dailyCarbs"
            />
            
            <NutritionGoalInput
              id="daily-fats"
              v-model="nutritionForm.dailyFats"
              :label="$t('profile.nutritional.dailyFats')"
              :min="0"
              :max="500"
              :show-percentage="true"
              :daily-calories="nutritionForm.dailyCalories"
              :calories-per-gram="9"
              :error="nutritionErrors.dailyFats"
            />
          </div>
          
          <div class="grid grid-cols-1 gap-6 sm:grid-cols-3">
            <NutritionGoalInput
              id="daily-fiber"
              v-model="nutritionForm.dailyFiber"
              :label="$t('profile.nutritional.dailyFiber')"
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
              :min="0"
              :max="200"
              :error="nutritionErrors.dailySugar"
            />
          </div>
          
          <div class="flex justify-end">
            <button
              @click="updateNutritionalGoals"
              :disabled="isNutritionLoading"
              class="btn btn-primary"
            >
              <LoadingSpinner v-if="isNutritionLoading" size="small" color="white" />
              <span v-else>{{ $t('profile.nutritional.saveGoals') }}</span>
            </button>
          </div>
        </div>

        <!-- Settings Tab -->
        <div v-if="activeTab === 'settings'" class="space-y-6">
          <h2 class="text-xl font-semibold text-gray-900">{{ $t('profile.settings.title') }}</h2>
          <p class="text-gray-600">{{ $t('profile.settings.description') }}</p>
          
          <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
            <!-- Language Selector -->
            <div class="space-y-1">
              <label class="block text-sm font-medium text-gray-700">{{ $t('profile.settings.language') }}</label>
              <select
                v-model="settingsForm.language"
                class="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
              >
                <option v-for="lang in translatedLanguages" :key="lang.code" :value="lang.code">
                  {{ lang.name }}
                </option>
              </select>
              <p v-if="settingsErrors.language" class="text-sm text-red-600">{{ settingsErrors.language }}</p>
            </div>

            <!-- Country Selector -->
            <div class="space-y-1">
              <label class="block text-sm font-medium text-gray-700">{{ $t('profile.settings.country') }}</label>
              <select
                v-model="settingsForm.country"
                class="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
              >
                <option value="">{{ $t('profile.settings.countryPlaceholder') }}</option>
                <option v-for="country in translatedCountries" :key="country.code" :value="country.code">
                  {{ country.name }}
                </option>
              </select>
              <p v-if="settingsErrors.country" class="text-sm text-red-600">{{ settingsErrors.country }}</p>
            </div>

            <!-- Date Format Selector -->
            <div class="space-y-1">
              <label class="block text-sm font-medium text-gray-700">{{ $t('profile.settings.dateFormat') }}</label>
              <select
                v-model="settingsForm.dateFormat"
                class="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
              >
                <option v-for="(label, format) in DATE_FORMATS_LABELS" :key="format" :value="format">
                  {{ label }}
                </option>
              </select>
              <p v-if="settingsErrors.dateFormat" class="text-sm text-red-600">{{ settingsErrors.dateFormat }}</p>
            </div>

            <!-- Timezone Selector -->
            <div class="space-y-1">
              <label class="block text-sm font-medium text-gray-700">{{ $t('profile.settings.timezone') }}</label>
              <select
                v-model="settingsForm.timezone"
                class="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
              >
                <option v-for="tz in COMMON_TIMEZONES" :key="tz.value" :value="tz.value">
                  {{ tz.label }}
                </option>
              </select>
              <p v-if="settingsErrors.timezone" class="text-sm text-red-600">{{ settingsErrors.timezone }}</p>
            </div>

            <!-- First Day of Week Selector -->
            <div class="space-y-1">
              <label class="block text-sm font-medium text-gray-700">{{ $t('profile.settings.firstDayOfWeekLabel') }}</label>
              <div class="flex space-x-4">
                <label class="flex items-center">
                  <input
                    type="radio"
                    v-model="settingsForm.firstDayOfWeek"
                    :value="FIRST_DAY_OF_WEEK.SUNDAY"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300"
                  />
                  <span class="ml-2 text-sm text-gray-700">{{ $t('constants.firstDayOfWeek.SUNDAY') }}</span>
                </label>
                <label class="flex items-center">
                  <input
                    type="radio"
                    v-model="settingsForm.firstDayOfWeek"
                    :value="FIRST_DAY_OF_WEEK.MONDAY"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300"
                  />
                  <span class="ml-2 text-sm text-gray-700">{{ $t('constants.firstDayOfWeek.MONDAY') }}</span>
                </label>
              </div>
              <p v-if="settingsErrors.firstDayOfWeek" class="text-sm text-red-600">{{ settingsErrors.firstDayOfWeek }}</p>
            </div>

            <!-- Measurement System Selector -->
            <div class="space-y-1">
              <label class="block text-sm font-medium text-gray-700">{{ $t('profile.settings.measurementSystem') }}</label>
              <div class="flex space-x-4">
                <label class="flex items-center">
                  <input
                    type="radio"
                    v-model="settingsForm.measurementSystem"
                    :value="MEASUREMENT_SYSTEMS.METRIC"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300"
                  />
                  <span class="ml-2 text-sm text-gray-700">{{ $t('constants.measurementSystems.METRIC') }}</span>
                </label>
                <label class="flex items-center">
                  <input
                    type="radio"
                    v-model="settingsForm.measurementSystem"
                    :value="MEASUREMENT_SYSTEMS.IMPERIAL"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300"
                  />
                  <span class="ml-2 text-sm text-gray-700">{{ $t('constants.measurementSystems.IMPERIAL') }}</span>
                </label>
              </div>
              <p v-if="settingsErrors.measurementSystem" class="text-sm text-red-600">{{ settingsErrors.measurementSystem }}</p>
            </div>

          </div>
          
          <div class="flex justify-end">
            <button
              @click="updateSettings"
              :disabled="isSettingsLoading"
              class="btn btn-primary"
            >
              <LoadingSpinner v-if="isSettingsLoading" size="small" color="white" />
              <span v-else>{{ $t('profile.settings.saveSettings') }}</span>
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
import { reactive, ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { userService } from '@/services/userService'
import FormInput from '@/components/common/FormInput.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import DietaryRestrictionsSelector from '@/components/profile/DietaryRestrictionsSelector.vue'
import AllergiesSelector from '@/components/profile/AllergiesSelector.vue'
import CuisinePreferencesSelector from '@/components/profile/CuisinePreferencesSelector.vue'
import NutritionGoalInput from '@/components/profile/NutritionGoalInput.vue'
import Toast from '@/components/common/Toast.vue'
import { 
  GOAL_TYPES,
  SUBSCRIPTION_STATUS,
  MEAL_TYPES,
  DATE_FORMATS,
  DATE_FORMATS_LABELS,
  MEASUREMENT_SYSTEMS,
  FIRST_DAY_OF_WEEK,
  COMMON_TIMEZONES,
  COUNTRIES,
  DIETARY_RESTRICTIONS,
  ALLERGIES,
  CUISINE_TYPES
} from '@/utils/constants'
import { SUPPORTED_LANGUAGES } from '@/i18n'
import { useSettingsStore } from '@/stores/settings'
import { getErrorMessage } from '@/utils/errorUtils'
import { validateValue, filterValidValues, validateEnumValue } from '@/utils/validationUtils'

const { t } = useI18n()
const settingsStore = useSettingsStore()

const activeTab = ref('personal')
const isPersonalLoading = ref(false)
const isDietaryLoading = ref(false)
const isNutritionLoading = ref(false)
const isSettingsLoading = ref(false)

// Toast state
const toast = reactive({
  show: false,
  type: 'success',
  title: '',
  message: ''
})

const tabs = computed(() => [
  { id: 'personal', name: t('profile.tabs.personal') },
  { id: 'dietary', name: t('profile.tabs.dietary') },
  { id: 'nutritional', name: t('profile.tabs.nutritional') },
  { id: 'settings', name: t('profile.tabs.settings') }
])

// Personal Info Form
const personalForm = reactive({
  email: '',
  householdSize: 1,
  mealTypes: [],
  subscriptionStatus: SUBSCRIPTION_STATUS.FREE,
  subscriptionExpiresAt: null
})

const personalErrors = reactive({
  email: '',
  householdSize: '',
  mealTypes: ''
})

// Dietary Preferences Form
const dietaryForm = reactive({
  restrictions: [],
  allergies: [],
  preferredCuisines: []
})

const dietaryErrors = reactive({
  restrictions: '',
  allergies: '',
  preferredCuisines: ''
})

// Nutritional Goals Form
const nutritionForm = reactive({
  dailyCalories: 2000,
  dailyProtein: 150,
  dailyCarbs: 250,
  dailyFats: 67,
  dailyFiber: 25,
  dailySodium: 2300,
  dailySugar: 50,
  goalType: GOAL_TYPES.MAINTENANCE
})

const nutritionErrors = reactive({
  dailyCalories: '',
  dailyProtein: '',
  dailyCarbs: '',
  dailyFats: '',
  dailyFiber: '',
  dailySodium: '',
  dailySugar: '',
  goalType: ''
})

// Settings Form
const settingsForm = reactive({
  language: 'en',
  country: 'US',
  dateFormat: DATE_FORMATS.MM_DD_YYYY,
  timezone: 'UTC',
  firstDayOfWeek: FIRST_DAY_OF_WEEK.MONDAY,
  measurementSystem: MEASUREMENT_SYSTEMS.METRIC
})

const settingsErrors = reactive({
  language: '',
  country: '',
  dateFormat: '',
  timezone: '',
  firstDayOfWeek: '',
  measurementSystem: ''
})

// Options
const goalTypeOptions = GOAL_TYPES

// Computed properties
const translatedLanguages = computed(() => {
  return SUPPORTED_LANGUAGES.map(lang => ({
    code: lang.code,
    name: t(`constants.languages.${lang.code}`) || lang.name
  }))
})

const translatedCountries = computed(() => {
  return COUNTRIES.map(country => ({
    code: country.code,
    name: t(`constants.countries.${country.code}`) || country.name
  }))
})

const subscriptionStatusLabel = computed(() => {
  const status = personalForm.subscriptionStatus
  return t(`constants.subscriptionStatus.${status}`) || status
})

const subscriptionStatusClasses = computed(() => {
  const status = personalForm.subscriptionStatus
  switch (status) {
    case SUBSCRIPTION_STATUS.FREE:
      return 'bg-gray-100 text-gray-800'
    case SUBSCRIPTION_STATUS.PREMIUM:
      return 'bg-blue-100 text-blue-800'
    case SUBSCRIPTION_STATUS.FAMILY:
      return 'bg-green-100 text-green-800'
    case SUBSCRIPTION_STATUS.CANCELLED:
      return 'bg-yellow-100 text-yellow-800'
    case SUBSCRIPTION_STATUS.EXPIRED:
      return 'bg-red-100 text-red-800'
    default:
      return 'bg-gray-100 text-gray-800'
  }
})

// Methods
const tabClasses = (tabId) => {
  const baseClasses = 'whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm'
  const activeClasses = 'border-blue-500 text-blue-600'
  const inactiveClasses = 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
  
  return `${baseClasses} ${activeTab.value === tabId ? activeClasses : inactiveClasses}`
}

const getGoalTypeLabel = (value) => {
  return t(`constants.goalTypes.${value}`) || value
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return settingsStore.formatDateFn(new Date(dateString))
}

const showToast = (type, title, message) => {
  toast.type = type
  toast.title = title
  toast.message = message
  toast.show = true
}

const updatePersonalInfo = async () => {
  isPersonalLoading.value = true
  
  try {
    await userService.updateProfile(personalForm)
    showToast('success', t('common.success'), t('profile.settings.personalInfoUpdatedSuccess'))
  } catch (error) {
    const errorMessage = getErrorMessage(error)
    showToast('error', t('common.error'), errorMessage)
  } finally {
    isPersonalLoading.value = false
  }
}

const updateDietaryPreferences = async () => {
  isDietaryLoading.value = true
  
  try {
    await userService.updateDietaryPreferences(dietaryForm)
    showToast('success', t('common.success'), t('profile.settings.dietaryPreferencesUpdatedSuccess'))
  } catch (error) {
    const errorMessage = getErrorMessage(error)
    showToast('error', t('common.error'), errorMessage)
  } finally {
    isDietaryLoading.value = false
  }
}

const updateNutritionalGoals = async () => {
  isNutritionLoading.value = true
  
  try {
    await userService.updateNutritionalGoals(nutritionForm)
    showToast('success', t('common.success'), t('profile.settings.nutritionalGoalsUpdatedSuccess'))
  } catch (error) {
    const errorMessage = getErrorMessage(error)
    showToast('error', t('common.error'), errorMessage)
  } finally {
    isNutritionLoading.value = false
  }
}

const updateSettings = async () => {
  isSettingsLoading.value = true
  
  try {
    // Convert enum-like values to format strings for backend
    const settingsData = {
      ...settingsForm,
      dateFormat: DATE_FORMATS_LABELS[settingsForm.dateFormat] || settingsForm.dateFormat,
    }
    await settingsStore.updateSettings(settingsData)
    showToast('success', t('common.success'), t('profile.settings.settingsUpdatedSuccess'))
  } catch (error) {
    const errorMessage = getErrorMessage(error)
    showToast('error', t('common.error'), errorMessage)
  } finally {
    isSettingsLoading.value = false
  }
}

onMounted(async () => {
  try {
    // Load personal profile
    const profile = await userService.getProfile()
    personalForm.email = profile.email || ''
    personalForm.householdSize = profile.householdSize || 1
    
    // Validate mealTypes array - filter invalid values and ensure at least one valid meal type
    const validMealTypes = filterValidValues(
      profile.mealTypes || [],
      MEAL_TYPES,
      'mealTypes'
    )
    personalForm.mealTypes = validMealTypes.length > 0 
      ? validMealTypes 
      : [MEAL_TYPES.BREAKFAST, MEAL_TYPES.LUNCH, MEAL_TYPES.DINNER]
    
    // Validate subscriptionStatus
    personalForm.subscriptionStatus = validateEnumValue(
      profile.subscriptionStatus,
      SUBSCRIPTION_STATUS,
      SUBSCRIPTION_STATUS.FREE,
      'subscriptionStatus'
    )
    personalForm.subscriptionExpiresAt = profile.subscriptionExpiresAt || null
    
    // Load dietary preferences
    const dietaryPreferences = await userService.getDietaryPreferences()
    
    // Filter restrictions array to only include valid values
    dietaryForm.restrictions = filterValidValues(
      dietaryPreferences.restrictions || [],
      DIETARY_RESTRICTIONS,
      'dietary restrictions'
    )
    
    // Filter allergies array to only include valid values
    dietaryForm.allergies = filterValidValues(
      dietaryPreferences.allergies || [],
      ALLERGIES,
      'allergies'
    )
    
    // Filter preferredCuisines array to only include valid values
    dietaryForm.preferredCuisines = filterValidValues(
      dietaryPreferences.preferredCuisines || [],
      CUISINE_TYPES,
      'preferred cuisines'
    )
    
    // Load nutritional goals
    const nutritionalGoals = await userService.getNutritionalGoals()
    nutritionForm.dailyCalories = nutritionalGoals.dailyCalories || 2000
    nutritionForm.dailyProtein = nutritionalGoals.dailyProtein || 150
    nutritionForm.dailyCarbs = nutritionalGoals.dailyCarbs || 250
    nutritionForm.dailyFats = nutritionalGoals.dailyFats || 67
    nutritionForm.dailyFiber = nutritionalGoals.dailyFiber || 25
    nutritionForm.dailySodium = nutritionalGoals.dailySodium || 2300
    nutritionForm.dailySugar = nutritionalGoals.dailySugar || 50
    
    // Validate goalType
    nutritionForm.goalType = validateEnumValue(
      nutritionalGoals.goalType,
      GOAL_TYPES,
      GOAL_TYPES.MAINTENANCE,
      'goalType'
    )
    
    // Load settings
    const settings = await settingsStore.loadSettings()
    if (settings) {
      // Validate language against SUPPORTED_LANGUAGES
      settingsForm.language = validateValue(
        settings.language,
        SUPPORTED_LANGUAGES,
        'en',
        'language'
      )
      
      // Validate country against COUNTRIES
      settingsForm.country = validateValue(
        settings.country,
        COUNTRIES,
        'US',
        'country'
      )
      
      // Convert format strings to enum-like keys for frontend and validate
      const dateFormatKey = Object.keys(DATE_FORMATS_LABELS).find(
        key => DATE_FORMATS_LABELS[key] === settings.dateFormat
      ) || DATE_FORMATS.MM_DD_YYYY
      settingsForm.dateFormat = validateEnumValue(
        dateFormatKey,
        DATE_FORMATS,
        DATE_FORMATS.MM_DD_YYYY,
        'dateFormat'
      )
      
      // Validate timezone against COMMON_TIMEZONES
      settingsForm.timezone = validateValue(
        settings.timezone,
        COMMON_TIMEZONES,
        'UTC',
        'timezone'
      )
      
      // Validate firstDayOfWeek
      settingsForm.firstDayOfWeek = validateEnumValue(
        settings.firstDayOfWeek,
        FIRST_DAY_OF_WEEK,
        FIRST_DAY_OF_WEEK.MONDAY,
        'firstDayOfWeek'
      )
      
      // Validate measurementSystem
      settingsForm.measurementSystem = validateEnumValue(
        settings.measurementSystem,
        MEASUREMENT_SYSTEMS,
        MEASUREMENT_SYSTEMS.METRIC,
        'measurementSystem'
      )
    }
  } catch (error) {
    const errorMessage = getErrorMessage(error)
    showToast('error', t('common.error'), errorMessage)
  }
})
</script>
