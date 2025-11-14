<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Nutrition Tracking</h1>
        <p class="text-gray-600">Monitor your nutritional goals and progress</p>
      </div>
      <button class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md font-medium">
        Set Goals
      </button>
    </div>

    <!-- Multi-person household info banner -->
    <div
      v-if="showFamilyUpgradeBanner"
      class="bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 rounded-lg p-4 flex items-start gap-4"
    >
      <div class="flex-shrink-0">
        <svg class="h-6 w-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
        </svg>
      </div>
      <div class="flex-1">
        <h3 class="text-sm font-semibold text-gray-900 mb-1">
          Planning for multiple people?
        </h3>
        <p class="text-sm text-gray-700 mb-3">
          Currently tracking nutrition for your household. Upgrade to <strong>Family</strong> subscription for per-person nutrition goals and individual progress tracking.
        </p>
        <router-link
          to="/subscription"
          class="inline-flex items-center text-sm font-medium text-blue-600 hover:text-blue-700"
        >
          Learn more about Family subscription
          <svg class="ml-1 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </router-link>
      </div>
    </div>

    <!-- Daily nutrition overview -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 text-center">
        <div class="text-2xl font-bold text-gray-900 mb-2">1,240</div>
        <div class="text-sm text-gray-600">Calories</div>
        <div class="text-xs text-gray-500 mt-1">Goal: 2,000</div>
        <div class="w-full bg-gray-200 rounded-full h-2 mt-2">
          <div class="bg-blue-600 h-2 rounded-full" style="width: 62%"></div>
        </div>
      </div>
      
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 text-center">
        <div class="text-2xl font-bold text-gray-900 mb-2">85g</div>
        <div class="text-sm text-gray-600">Protein</div>
        <div class="text-xs text-gray-500 mt-1">Goal: 120g</div>
        <div class="w-full bg-gray-200 rounded-full h-2 mt-2">
          <div class="bg-green-600 h-2 rounded-full" style="width: 71%"></div>
        </div>
      </div>
      
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 text-center">
        <div class="text-2xl font-bold text-gray-900 mb-2">145g</div>
        <div class="text-sm text-gray-600">Carbs</div>
        <div class="text-xs text-gray-500 mt-1">Goal: 200g</div>
        <div class="w-full bg-gray-200 rounded-full h-2 mt-2">
          <div class="bg-yellow-600 h-2 rounded-full" style="width: 73%"></div>
        </div>
      </div>
      
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 text-center">
        <div class="text-2xl font-bold text-gray-900 mb-2">45g</div>
        <div class="text-sm text-gray-600">Fat</div>
        <div class="text-xs text-gray-500 mt-1">Goal: 65g</div>
        <div class="w-full bg-gray-200 rounded-full h-2 mt-2">
          <div class="bg-purple-600 h-2 rounded-full" style="width: 69%"></div>
        </div>
      </div>
    </div>

    <!-- Weekly progress -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h3 class="text-lg font-semibold text-gray-900 mb-4">Weekly Progress</h3>
      <div class="grid grid-cols-7 gap-4">
        <div
          v-for="day in weekProgress"
          :key="day.name"
          class="text-center"
        >
          <div class="text-sm font-medium text-gray-900 mb-2">{{ day.name }}</div>
          <div class="w-full bg-gray-200 rounded-full h-2 mb-2">
            <div
              class="bg-blue-600 h-2 rounded-full"
              :style="`width: ${day.progress}%`"
            ></div>
          </div>
          <div class="text-xs text-gray-500">{{ day.calories }} cal</div>
        </div>
      </div>
    </div>

    <!-- Meal breakdown -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">Today's Meals</h3>
        <div class="space-y-4">
          <div
            v-for="meal in todaysMeals"
            :key="meal.type"
            class="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
          >
            <div>
              <div class="font-medium text-gray-900">{{ meal.type }}</div>
              <div class="text-sm text-gray-600">{{ meal.name }}</div>
            </div>
            <div class="text-right">
              <div class="font-medium text-gray-900">{{ meal.calories }} cal</div>
              <div class="text-xs text-gray-500">{{ meal.protein }}g protein</div>
            </div>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">Nutritional Goals</h3>
        <div class="space-y-4">
          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-600">Daily Calories</span>
            <span class="text-sm font-medium text-gray-900">2,000</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-600">Protein (g)</span>
            <span class="text-sm font-medium text-gray-900">120</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-600">Carbs (g)</span>
            <span class="text-sm font-medium text-gray-900">200</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-600">Fat (g)</span>
            <span class="text-sm font-medium text-gray-900">65</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-600">Fiber (g)</span>
            <span class="text-sm font-medium text-gray-900">25</span>
          </div>
        </div>
        <button class="w-full mt-4 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md font-medium">
          Update Goals
        </button>
      </div>
    </div>

    <!-- Coming soon message -->
    <div class="bg-blue-50 border border-blue-200 rounded-lg p-6 text-center">
      <svg class="mx-auto h-12 w-12 text-blue-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
      </svg>
      <h3 class="text-lg font-semibold text-gray-900 mb-2">Advanced Nutrition Tracking Coming Soon</h3>
      <p class="text-gray-600">
        We're building comprehensive nutritional analysis with detailed macro tracking, micronutrient monitoring, and personalized recommendations.
      </p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { shouldShowFamilyUpgrade } from '@/utils/subscriptionUtils'

const authStore = useAuthStore()

// Check if user has multi-person household and is not on Family subscription
const showFamilyUpgradeBanner = computed(() => {
  return shouldShowFamilyUpgrade(authStore.user)
})

// Mock data for nutrition tracking
const weekProgress = [
  { name: 'Mon', calories: 1850, progress: 92 },
  { name: 'Tue', calories: 2100, progress: 105 },
  { name: 'Wed', calories: 1950, progress: 98 },
  { name: 'Thu', calories: 2200, progress: 110 },
  { name: 'Fri', calories: 1800, progress: 90 },
  { name: 'Sat', calories: 2400, progress: 120 },
  { name: 'Sun', calories: 1240, progress: 62 }
]

const todaysMeals = [
  {
    type: 'Breakfast',
    name: 'Oatmeal with Berries',
    calories: 320,
    protein: 12
  },
  {
    type: 'Lunch',
    name: 'Mediterranean Salad',
    calories: 280,
    protein: 15
  },
  {
    type: 'Dinner',
    name: 'Grilled Salmon',
    calories: 450,
    protein: 35
  },
  {
    type: 'Snacks',
    name: 'Greek Yogurt',
    calories: 190,
    protein: 18
  }
]
</script>
