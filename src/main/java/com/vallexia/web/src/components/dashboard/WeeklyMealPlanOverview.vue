<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
    <div class="flex items-center justify-between mb-4">
      <h3 class="text-lg font-semibold text-gray-900">This Week's Meal Plan</h3>
      <RouterLink
        to="/meal-plans"
        class="text-sm text-blue-600 hover:text-blue-700 font-medium"
      >
        View All →
      </RouterLink>
    </div>

    <!-- Week navigation -->
    <div class="flex items-center justify-between mb-4">
      <button
        @click="previousWeek"
        class="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-md"
      >
        <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
        </svg>
      </button>
      <h4 class="text-sm font-medium text-gray-900">{{ weekRange }}</h4>
      <button
        @click="nextWeek"
        class="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-md"
      >
        <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
        </svg>
      </button>
    </div>

    <!-- Calendar grid -->
    <div class="grid grid-cols-7 gap-2">
      <div
        v-for="day in weekDays"
        :key="day.date"
        class="text-center"
      >
        <div class="text-xs font-medium text-gray-500 mb-2">{{ day.name }}</div>
        <div class="text-sm font-semibold text-gray-900 mb-2">{{ day.day }}</div>
        
        <!-- Meal slots -->
        <div class="space-y-1">
          <div
            v-for="mealType in ['breakfast', 'lunch', 'dinner']"
            :key="`${day.date}-${mealType}`"
            class="h-8 bg-gray-50 rounded border border-gray-200 flex items-center justify-center cursor-pointer hover:bg-gray-100 transition-colors"
            @click="openMealSelector(day.date, mealType)"
          >
            <span v-if="getMealForDay(day.date, mealType)" class="text-xs text-gray-700 truncate px-1">
              {{ getMealForDay(day.date, mealType) }}
            </span>
            <svg v-else class="h-4 w-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
            </svg>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick actions -->
    <div class="mt-4 flex space-x-2">
      <button
        @click="generateWeeklyPlan"
        class="flex-1 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
      >
        Generate Plan
      </button>
      <button
        @click="clearWeek"
        class="px-4 py-2 border border-gray-300 text-gray-700 rounded-md text-sm font-medium hover:bg-gray-50 transition-colors"
      >
        Clear
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { RouterLink } from 'vue-router'

const currentWeekStart = ref(new Date())

// Calculate current week start (Monday)
const getWeekStart = (date) => {
  const d = new Date(date)
  const day = d.getDay()
  const diff = d.getDate() - day + (day === 0 ? -6 : 1) // Adjust when day is Sunday
  return new Date(d.setDate(diff))
}

// Initialize with current week
currentWeekStart.value = getWeekStart(new Date())

const weekDays = computed(() => {
  const days = []
  const start = new Date(currentWeekStart.value)
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(start)
    date.setDate(start.getDate() + i)
    
    days.push({
      date: date.toISOString().split('T')[0],
      name: date.toLocaleDateString('en-US', { weekday: 'short' }),
      day: date.getDate()
    })
  }
  
  return days
})

const weekRange = computed(() => {
  const start = weekDays.value[0]
  const end = weekDays.value[6]
  return `${start.day}/${start.date.split('-')[1]} - ${end.day}/${end.date.split('-')[1]}`
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

const getMealForDay = (date, mealType) => {
  return mealPlans.value[date]?.[mealType] || null
}

const previousWeek = () => {
  const newDate = new Date(currentWeekStart.value)
  newDate.setDate(newDate.getDate() - 7)
  currentWeekStart.value = newDate
}

const nextWeek = () => {
  const newDate = new Date(currentWeekStart.value)
  newDate.setDate(newDate.getDate() + 7)
  currentWeekStart.value = newDate
}

const openMealSelector = (date, mealType) => {
  // TODO: Open meal selection modal
  console.log('Open meal selector for', date, mealType)
}

const generateWeeklyPlan = () => {
  // TODO: Generate AI-powered weekly meal plan
  console.log('Generate weekly plan')
}

const clearWeek = () => {
  // TODO: Clear current week's meal plan
  console.log('Clear week')
}
</script>
