<template>
  <div class="space-y-6">
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-gray-900">Profile Management</h1>
      <p class="mt-2 text-gray-600">Manage your personal information, dietary preferences, and nutritional goals.</p>
    </div>

    <!-- Profile Tabs -->
    <div class="bg-white shadow rounded-lg">
      <div class="border-b border-gray-200">
        <nav class="-mb-px flex space-x-8 px-6" aria-label="Tabs">
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
          <h2 class="text-xl font-semibold text-gray-900">Personal Information</h2>
          <p class="text-gray-600">Update your personal details and profile information.</p>
          
          <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
            <FormInput
              id="email"
              v-model="personalForm.email"
              type="email"
              label="Email Address"
              placeholder="Enter your email address"
              :error="personalErrors.email"
            />
            
            <FormInput
              id="householdSize"
              v-model.number="personalForm.householdSize"
              type="number"
              label="Household Size"
              placeholder="Number of people"
              :error="personalErrors.householdSize"
              :min="1"
              :max="20"
            />
          </div>
          
          <!-- Meal Types Selection -->
          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-700">
              Meal Types
              <span class="text-red-500">*</span>
            </label>
            <div class="grid grid-cols-2 gap-3 sm:grid-cols-4">
              <label
                v-for="(label, type) in MEAL_TYPES_LABELS"
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
                <span class="ml-2 text-sm text-gray-700">{{ label }}</span>
              </label>
            </div>
            <p v-if="personalErrors.mealTypes" class="text-sm text-red-600">{{ personalErrors.mealTypes }}</p>
            <p v-else class="text-sm text-gray-500">Select the meal types you want to track in your meal plans.</p>
          </div>
          
          <!-- Subscription Status Display -->
          <div class="mt-6 p-4 bg-gray-50 rounded-lg">
            <h3 class="text-sm font-medium text-gray-900 mb-2">Subscription Status</h3>
            <div class="flex items-center">
              <span 
                :class="subscriptionStatusClasses"
                class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
              >
                {{ subscriptionStatusLabel }}
              </span>
              <span v-if="personalForm.subscriptionExpiresAt" class="ml-2 text-sm text-gray-500">
                Expires: {{ formatDate(personalForm.subscriptionExpiresAt) }}
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
              <span v-else>Save Changes</span>
            </button>
          </div>
        </div>

        <!-- Dietary Preferences Tab -->
        <div v-if="activeTab === 'dietary'" class="space-y-6">
          <h2 class="text-xl font-semibold text-gray-900">Dietary Preferences</h2>
          <p class="text-gray-600">Set your dietary restrictions, allergies, and food preferences.</p>
          
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
              <span v-else>Save Preferences</span>
            </button>
          </div>
        </div>

        <!-- Nutritional Goals Tab -->
        <div v-if="activeTab === 'nutritional'" class="space-y-6">
          <h2 class="text-xl font-semibold text-gray-900">Nutritional Goals</h2>
          <p class="text-gray-600">Set your daily nutritional targets and health goals.</p>
          
          <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
            <NutritionGoalInput
              id="daily-calories"
              v-model="nutritionForm.dailyCalories"
              label="Daily Calories"
              unit="cal"
              :min="800"
              :max="5000"
              :error="nutritionErrors.dailyCalories"
            />
            
            <div class="space-y-1">
              <label class="block text-sm font-medium text-gray-700">Goal Type</label>
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
              label="Daily Protein"
              unit="g"
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
              label="Daily Carbs"
              unit="g"
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
              label="Daily Fats"
              unit="g"
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
              label="Daily Fiber"
              unit="g"
              :min="0"
              :max="100"
              :error="nutritionErrors.dailyFiber"
            />
            
            <NutritionGoalInput
              id="daily-sodium"
              v-model="nutritionForm.dailySodium"
              label="Daily Sodium"
              unit="mg"
              :min="0"
              :max="10000"
              :error="nutritionErrors.dailySodium"
            />
            
            <NutritionGoalInput
              id="daily-sugar"
              v-model="nutritionForm.dailySugar"
              label="Daily Sugar"
              unit="g"
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
              <span v-else>Save Goals</span>
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
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { userService } from '@/services/userService'
import FormInput from '@/components/common/FormInput.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import ErrorMessage from '@/components/common/ErrorMessage.vue'
import TagInput from '@/components/common/TagInput.vue'
import DietaryRestrictionsSelector from '@/components/profile/DietaryRestrictionsSelector.vue'
import AllergiesSelector from '@/components/profile/AllergiesSelector.vue'
import CuisinePreferencesSelector from '@/components/profile/CuisinePreferencesSelector.vue'
import NutritionGoalInput from '@/components/profile/NutritionGoalInput.vue'
import Toast from '@/components/common/Toast.vue'
import { 
  GOAL_TYPES,
  GOAL_TYPES_LABELS,
  SUBSCRIPTION_STATUS_LABELS,
  MEAL_TYPES,
  MEAL_TYPES_LABELS
} from '@/utils/constants'
import { getErrorMessage } from '@/utils/errorUtils'

const router = useRouter()
const authStore = useAuthStore()

const activeTab = ref('personal')
const isPersonalLoading = ref(false)
const isDietaryLoading = ref(false)
const isNutritionLoading = ref(false)

// Toast state
const toast = reactive({
  show: false,
  type: 'success',
  title: '',
  message: ''
})

const tabs = [
  { id: 'personal', name: 'Personal Info' },
  { id: 'dietary', name: 'Dietary Preferences' },
  { id: 'nutritional', name: 'Nutritional Goals' }
]

// Personal Info Form
const personalForm = reactive({
  email: '',
  householdSize: 1,
  mealTypes: [],
  subscriptionStatus: 'FREE',
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
  goalType: 'MAINTENANCE'
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

// Options
const goalTypeOptions = GOAL_TYPES

// Computed properties
const subscriptionStatusLabel = computed(() => {
  return SUBSCRIPTION_STATUS_LABELS[personalForm.subscriptionStatus] || personalForm.subscriptionStatus
})

const subscriptionStatusClasses = computed(() => {
  const status = personalForm.subscriptionStatus
  switch (status) {
    case 'FREE':
      return 'bg-gray-100 text-gray-800'
    case 'PREMIUM':
      return 'bg-blue-100 text-blue-800'
    case 'FAMILY':
      return 'bg-green-100 text-green-800'
    case 'CANCELLED':
      return 'bg-yellow-100 text-yellow-800'
    case 'EXPIRED':
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
  return GOAL_TYPES_LABELS[value] || value
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleDateString()
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
    showToast('success', 'Success', 'Personal information updated successfully')
  } catch (error) {
    const errorMessage = getErrorMessage(error)
    showToast('error', 'Error', errorMessage)
  } finally {
    isPersonalLoading.value = false
  }
}

const updateDietaryPreferences = async () => {
  isDietaryLoading.value = true
  
  try {
    await userService.updateDietaryPreferences(dietaryForm)
    showToast('success', 'Success', 'Dietary preferences updated successfully')
  } catch (error) {
    const errorMessage = getErrorMessage(error)
    showToast('error', 'Error', errorMessage)
  } finally {
    isDietaryLoading.value = false
  }
}

const updateNutritionalGoals = async () => {
  isNutritionLoading.value = true
  
  try {
    await userService.updateNutritionalGoals(nutritionForm)
    showToast('success', 'Success', 'Nutritional goals updated successfully')
  } catch (error) {
    const errorMessage = getErrorMessage(error)
    showToast('error', 'Error', errorMessage)
  } finally {
    isNutritionLoading.value = false
  }
}

onMounted(async () => {
  try {
    // Load personal profile
    const profile = await userService.getProfile()
    personalForm.email = profile.email || ''
    personalForm.householdSize = profile.householdSize || 1
    personalForm.mealTypes = profile.mealTypes || [MEAL_TYPES.BREAKFAST, MEAL_TYPES.LUNCH, MEAL_TYPES.DINNER]
    personalForm.subscriptionStatus = profile.subscriptionStatus || 'FREE'
    personalForm.subscriptionExpiresAt = profile.subscriptionExpiresAt || null
    
    // Load dietary preferences
    const dietaryPreferences = await userService.getDietaryPreferences()
    dietaryForm.restrictions = dietaryPreferences.restrictions || []
    dietaryForm.allergies = dietaryPreferences.allergies || []
    dietaryForm.preferredCuisines = dietaryPreferences.preferredCuisines || []
    
    // Load nutritional goals
    const nutritionalGoals = await userService.getNutritionalGoals()
    nutritionForm.dailyCalories = nutritionalGoals.dailyCalories || 2000
    nutritionForm.dailyProtein = nutritionalGoals.dailyProtein || 150
    nutritionForm.dailyCarbs = nutritionalGoals.dailyCarbs || 250
    nutritionForm.dailyFats = nutritionalGoals.dailyFats || 67
    nutritionForm.dailyFiber = nutritionalGoals.dailyFiber || 25
    nutritionForm.dailySodium = nutritionalGoals.dailySodium || 2300
    nutritionForm.dailySugar = nutritionalGoals.dailySugar || 50
    nutritionForm.goalType = nutritionalGoals.goalType || 'MAINTENANCE'
  } catch (error) {
    const errorMessage = getErrorMessage(error)
    showToast('error', 'Error', errorMessage)
  }
})
</script>
