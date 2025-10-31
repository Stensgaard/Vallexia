<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Meal Plans</h1>
        <p class="text-gray-600">Plan your weekly meals</p>
      </div>
      <button class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md font-medium">
        Create Plan
      </button>
    </div>

    <!-- Week selector -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div class="flex items-center justify-between">
        <button class="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-md">
          <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <h2 class="text-lg font-semibold text-gray-900">Week of January 15, 2024</h2>
        <button class="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-md">
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
          :key="day.name"
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
              <div class="text-xs text-gray-600">{{ meal.name || 'Add meal' }}</div>
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
        <h3 class="text-lg font-semibold text-gray-900 mb-2">Auto-Generate Plan</h3>
        <p class="text-gray-600 mb-4">Let AI create a balanced meal plan based on your preferences.</p>
        <button class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md font-medium">
          Generate Plan
        </button>
      </div>

      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 text-center">
        <svg class="mx-auto h-12 w-12 text-green-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
        </svg>
        <h3 class="text-lg font-semibold text-gray-900 mb-2">Use Template</h3>
        <p class="text-gray-600 mb-4">Start with a pre-made meal plan template.</p>
        <button class="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-md font-medium">
          Browse Templates
        </button>
      </div>

      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 text-center">
        <svg class="mx-auto h-12 w-12 text-purple-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
        </svg>
        <h3 class="text-lg font-semibold text-gray-900 mb-2">Copy Previous Week</h3>
        <p class="text-gray-600 mb-4">Duplicate a previous week's meal plan.</p>
        <button class="bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded-md font-medium">
          Copy Week
        </button>
      </div>
    </div>

    <!-- Coming soon message -->
    <div class="bg-blue-50 border border-blue-200 rounded-lg p-6 text-center">
      <svg class="mx-auto h-12 w-12 text-blue-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
      </svg>
      <h3 class="text-lg font-semibold text-gray-900 mb-2">Advanced Meal Planning Coming Soon</h3>
      <p class="text-gray-600">
        We're building an intuitive drag-and-drop meal planning interface with nutritional optimization and smart recommendations.
      </p>
    </div>
  </div>
</template>

<script setup>
// Mock data for week days
const weekDays = [
  {
    name: 'Monday',
    meals: [
      { type: 'Breakfast', name: 'Oatmeal' },
      { type: 'Lunch', name: 'Salad' },
      { type: 'Dinner', name: 'Pasta' }
    ]
  },
  {
    name: 'Tuesday',
    meals: [
      { type: 'Breakfast', name: 'Toast' },
      { type: 'Lunch', name: 'Sandwich' },
      { type: 'Dinner', name: 'Chicken' }
    ]
  },
  {
    name: 'Wednesday',
    meals: [
      { type: 'Breakfast', name: '' },
      { type: 'Lunch', name: '' },
      { type: 'Dinner', name: '' }
    ]
  },
  {
    name: 'Thursday',
    meals: [
      { type: 'Breakfast', name: '' },
      { type: 'Lunch', name: '' },
      { type: 'Dinner', name: '' }
    ]
  },
  {
    name: 'Friday',
    meals: [
      { type: 'Breakfast', name: '' },
      { type: 'Lunch', name: '' },
      { type: 'Dinner', name: '' }
    ]
  },
  {
    name: 'Saturday',
    meals: [
      { type: 'Breakfast', name: '' },
      { type: 'Lunch', name: '' },
      { type: 'Dinner', name: '' }
    ]
  },
  {
    name: 'Sunday',
    meals: [
      { type: 'Breakfast', name: '' },
      { type: 'Lunch', name: '' },
      { type: 'Dinner', name: '' }
    ]
  }
]
</script>
