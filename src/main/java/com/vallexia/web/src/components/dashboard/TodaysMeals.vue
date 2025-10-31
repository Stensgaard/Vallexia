<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
    <div class="flex items-center justify-between mb-4">
      <h3 class="text-lg font-semibold text-gray-900">Today's Meals</h3>
      <span class="text-sm text-gray-500">{{ todayDate }}</span>
    </div>

    <div class="space-y-4">
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
            <div>{{ meal.nutrition.calories }} cal</div>
            <div class="text-xs text-gray-500">{{ meal.nutrition.protein }}g protein</div>
          </div>
          <button
            v-else
            @click="addMeal(meal.type)"
            class="text-blue-600 hover:text-blue-700 text-sm font-medium"
          >
            Add Meal
          </button>
        </div>
      </div>
    </div>

    <!-- Daily nutrition summary -->
    <div v-if="dailyNutrition" class="mt-6 pt-4 border-t border-gray-200">
      <h4 class="text-sm font-medium text-gray-900 mb-3">Daily Nutrition</h4>
      <div class="grid grid-cols-4 gap-4">
        <div class="text-center">
          <div class="text-lg font-semibold text-gray-900">{{ dailyNutrition.calories }}</div>
          <div class="text-xs text-gray-500">Calories</div>
        </div>
        <div class="text-center">
          <div class="text-lg font-semibold text-gray-900">{{ dailyNutrition.protein }}g</div>
          <div class="text-xs text-gray-500">Protein</div>
        </div>
        <div class="text-center">
          <div class="text-lg font-semibold text-gray-900">{{ dailyNutrition.carbs }}g</div>
          <div class="text-xs text-gray-500">Carbs</div>
        </div>
        <div class="text-center">
          <div class="text-lg font-semibold text-gray-900">{{ dailyNutrition.fat }}g</div>
          <div class="text-xs text-gray-500">Fat</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const todayDate = computed(() => {
  return new Date().toLocaleDateString('en-US', { 
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
  console.log('Add meal for', mealType)
}
</script>
