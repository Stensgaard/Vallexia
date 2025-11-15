<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">{{ $t('mealPlans.title') }}</h1>
        <p class="text-gray-600">{{ $t('mealPlans.description') }}</p>
      </div>
      <button class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md font-medium">
        {{ $t('mealPlans.createPlan') }}
      </button>
    </div>

    <!-- Week selector -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div class="flex items-center justify-between">
        <button 
          @click="previousWeek"
          class="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-md"
        >
          <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <h2 class="text-lg font-semibold text-gray-900">{{ $t('mealPlans.weekOf', { range: weekRange }) }}</h2>
        <button 
          @click="nextWeek"
          class="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-md"
        >
          <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </button>
      </div>
    </div>

    <!-- Calendar grid -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div class="grid grid-cols-7 gap-4">
        <div
          v-for="day in weekDays"
          :key="day.date"
          class="text-center"
        >
          <h3 class="font-semibold text-gray-900 mb-4">{{ day.name }}</h3>
          <div class="space-y-2">
            <div
              v-for="meal in day.meals"
              :key="meal.type"
              class="p-3 bg-gray-50 rounded-lg border border-gray-200 hover:bg-gray-100 cursor-pointer"
            >
              <div class="text-sm font-medium text-gray-900">{{ meal.type }}</div>
              <div class="text-xs text-gray-600">{{ meal.name || $t('mealPlans.addMeal') }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick actions -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 text-center">
        <svg class="mx-auto h-12 w-12 text-blue-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
        </svg>
        <h3 class="text-lg font-semibold text-gray-900 mb-2">{{ $t('mealPlans.autoGenerate.title') }}</h3>
        <p class="text-gray-600 mb-4">{{ $t('mealPlans.autoGenerate.description') }}</p>
        <button class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md font-medium">
          {{ $t('mealPlans.autoGenerate.button') }}
        </button>
      </div>

      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 text-center">
        <svg class="mx-auto h-12 w-12 text-green-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
        </svg>
        <h3 class="text-lg font-semibold text-gray-900 mb-2">{{ $t('mealPlans.useTemplate.title') }}</h3>
        <p class="text-gray-600 mb-4">{{ $t('mealPlans.useTemplate.description') }}</p>
        <button class="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-md font-medium">
          {{ $t('mealPlans.useTemplate.button') }}
        </button>
      </div>

      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 text-center">
        <svg class="mx-auto h-12 w-12 text-purple-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
        </svg>
        <h3 class="text-lg font-semibold text-gray-900 mb-2">{{ $t('mealPlans.copyWeek.title') }}</h3>
        <p class="text-gray-600 mb-4">{{ $t('mealPlans.copyWeek.description') }}</p>
        <button class="bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded-md font-medium">
          {{ $t('mealPlans.copyWeek.button') }}
        </button>
      </div>
    </div>

    <!-- Coming soon message -->
    <div class="bg-blue-50 border border-blue-200 rounded-lg p-6 text-center">
      <svg class="mx-auto h-12 w-12 text-blue-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
      </svg>
      <h3 class="text-lg font-semibold text-gray-900 mb-2">{{ $t('mealPlans.comingSoon.title') }}</h3>
      <p class="text-gray-600">
        {{ $t('mealPlans.comingSoon.description') }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useSettingsStore } from '@/stores/settings'
import { FIRST_DAY_OF_WEEK } from '@/utils/constants'

const settingsStore = useSettingsStore()

const currentWeekStart = ref(new Date())

// Calculate current week start based on user's first day of week setting
const getWeekStart = (date) => {
  const d = new Date(date)
  const day = d.getDay()
  const firstDay = settingsStore.firstDayOfWeek === FIRST_DAY_OF_WEEK.SUNDAY ? 0 : 1
  
  if (firstDay === 1) {
    // Monday is first day
    const diff = d.getDate() - day + (day === 0 ? -6 : 1)
    return new Date(d.setDate(diff))
  } else {
    // Sunday is first day
    const diff = d.getDate() - day
    return new Date(d.setDate(diff))
  }
}

// Initialize with current week
currentWeekStart.value = getWeekStart(new Date())

// Generate week days dynamically based on first day of week setting
const weekDays = computed(() => {
  const days = []
  const start = new Date(currentWeekStart.value)
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(start)
    date.setDate(start.getDate() + i)
    
    // Get meal data for this date (mock data - replace with actual API call)
    const dateKey = date.toISOString().split('T')[0]
    const mealData = mealPlans.value[dateKey] || {}
    
    days.push({
      date: dateKey,
      name: date.toLocaleDateString(settingsStore.locale, { weekday: 'long' }),
      day: date.getDate(),
      meals: [
        { type: 'Breakfast', name: mealData.breakfast || '' },
        { type: 'Lunch', name: mealData.lunch || '' },
        { type: 'Dinner', name: mealData.dinner || '' }
      ]
    })
  }
  
  return days
})

const weekRange = computed(() => {
  const start = weekDays.value[0]
  const end = weekDays.value[6]
  // Use settings store to format dates properly
  const startDate = new Date(start.date)
  const endDate = new Date(end.date)
  return `${settingsStore.formatDateFn(startDate)} - ${settingsStore.formatDateFn(endDate)}`
})

// Mock meal data - replace with actual data from store/API
const mealPlans = ref({
  '2024-01-15': {
    breakfast: 'Oatmeal',
    lunch: 'Salad',
    dinner: 'Pasta'
  },
  '2024-01-16': {
    breakfast: 'Toast',
    lunch: 'Sandwich',
    dinner: 'Chicken'
  }
})

const previousWeek = () => {
  const newDate = new Date(currentWeekStart.value)
  newDate.setDate(newDate.getDate() - 7)
  currentWeekStart.value = getWeekStart(newDate)
}

const nextWeek = () => {
  const newDate = new Date(currentWeekStart.value)
  newDate.setDate(newDate.getDate() + 7)
  currentWeekStart.value = getWeekStart(newDate)
}
</script>
