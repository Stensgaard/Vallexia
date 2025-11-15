<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 flex flex-col h-full">
    <div class="flex items-center justify-between mb-4">
      <h3 class="text-lg font-semibold text-gray-900">{{ $t('dashboard.todaysMeals.title') }}</h3>
      <span class="text-sm text-gray-500">{{ todayDate }}</span>
    </div>

    <div class="flex-1 space-y-4">
      <div
        v-for="meal in todaysMeals"
        :key="meal.type"
        class="flex items-center justify-between p-4 bg-gray-50 rounded-lg"
      >
        <div class="flex items-center">
          <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center mr-3">
            <component :is="meal.icon" class="h-5 w-5 text-blue-600" />
          </div>
          <div>
            <h4 class="font-medium text-gray-900">{{ meal.name }}</h4>
            <p class="text-sm text-gray-500">{{ meal.description }}</p>
          </div>
        </div>
        
        <div class="text-right">
          <div v-if="meal.nutrition" class="text-sm text-gray-600">
            <div>{{ formatNumber(meal.nutrition.calories, 0) }} cal</div>
            <div class="text-xs text-gray-500">{{ formatNutritionalValue(meal.nutrition.protein) }} protein</div>
          </div>
          <button
            v-else
            @click="addMeal(meal.type)"
            class="text-blue-600 hover:text-blue-700 text-sm font-medium"
          >
            {{ $t('dashboard.todaysMeals.addMeal') }}
          </button>
        </div>
      </div>
    </div>

    <!-- Daily nutrition summary -->
    <div v-if="dailyNutrition" class="mt-6 pt-4 border-t border-gray-200">
      <h4 class="text-sm font-medium text-gray-900 mb-3">{{ $t('dashboard.todaysMeals.dailyNutrition') }}</h4>
      <div class="grid grid-cols-4 gap-4">
        <div class="text-center">
          <div class="text-lg font-semibold text-gray-900">{{ formatNumber(dailyNutrition.calories, 0) }}</div>
          <div class="text-xs text-gray-500">{{ $t('dashboard.todaysMeals.calories') }}</div>
        </div>
        <div class="text-center">
          <div class="text-lg font-semibold text-gray-900">{{ formatNutritionalValue(dailyNutrition.protein) }}</div>
          <div class="text-xs text-gray-500">{{ $t('dashboard.todaysMeals.protein') }}</div>
        </div>
        <div class="text-center">
          <div class="text-lg font-semibold text-gray-900">{{ formatNutritionalValue(dailyNutrition.carbs) }}</div>
          <div class="text-xs text-gray-500">{{ $t('dashboard.todaysMeals.carbs') }}</div>
        </div>
        <div class="text-center">
          <div class="text-lg font-semibold text-gray-900">{{ formatNutritionalValue(dailyNutrition.fat) }}</div>
          <div class="text-xs text-gray-500">{{ $t('dashboard.todaysMeals.fat') }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSettingsStore } from '@/stores/settings'

const { t } = useI18n()
const settingsStore = useSettingsStore()

const formatNumber = (number, decimals = 0) => {
  return settingsStore.formatNumberFn(number, decimals)
}

const formatNutritionalValue = (value) => {
  if (!value && value !== 0) {
    return ''
  }
  
  // Nutritional values are stored in grams, convert to ounces if imperial
  const unit = settingsStore.measurementSystem === 'IMPERIAL' ? 'oz' : 'g'
  const displayValue = unit === 'oz' 
    ? settingsStore.convertWeightFn(value, 'g', 'oz')
    : value
  
  return `${formatNumber(displayValue, 1)}${unit}`
}

const todayDate = computed(() => {
  return new Date().toLocaleDateString(settingsStore.locale, { 
    weekday: 'long', 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  })
})

// Mock data - replace with actual data from store/API
const todaysMeals = ref([
  {
    type: 'breakfast',
    name: 'Oatmeal with Berries',
    description: 'Steel-cut oats with fresh blueberries and honey',
    nutrition: {
      calories: 320,
      protein: 12
    },
    icon: 'svg'
  },
  {
    type: 'lunch',
    name: 'Mediterranean Salad',
    description: 'Mixed greens with tomatoes, olives, and feta',
    nutrition: {
      calories: 280,
      protein: 15
    },
    icon: 'svg'
  },
  {
    type: 'dinner',
    name: '',
    description: '',
    nutrition: null,
    icon: 'svg'
  }
])

const dailyNutrition = computed(() => {
  const meals = todaysMeals.value.filter(meal => meal.nutrition)
  if (meals.length === 0) return null
  
  return meals.reduce((total, meal) => ({
    calories: total.calories + meal.nutrition.calories,
    protein: total.protein + meal.nutrition.protein,
    carbs: total.carbs + (meal.nutrition.carbs || 0),
    fat: total.fat + (meal.nutrition.fat || 0)
  }), { calories: 0, protein: 0, carbs: 0, fat: 0 })
})

const addMeal = (mealType) => {
  // TODO: Open meal selection modal
}
</script>
