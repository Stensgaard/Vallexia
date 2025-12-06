<template>
  <div class="space-y-6">
    <!-- Welcome section -->
    <div class="bg-gradient-to-r from-blue-600 to-blue-800 rounded-lg p-6 text-white">
      <h1 class="text-2xl font-bold mb-2">
        {{ $t('dashboard.welcomeBack', { name: userFullName }) }}
      </h1>
      <p class="text-blue-100">
        {{ $t('dashboard.welcomeMessage') }}
      </p>
    </div>

    <!-- Quick stats -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <QuickStatsCard
        :title="$t('dashboard.stats.recipesSaved')"
        :subtitle="$t('dashboard.stats.recipesSavedSubtitle')"
        :value="stats.recipesSaved"
        :change="12"
        :action-text="$t('dashboard.stats.browseRecipes')"
        @action="router.push('/recipes')"
      />
      <QuickStatsCard
        :title="$t('dashboard.stats.mealsPlanned')"
        :subtitle="$t('dashboard.stats.mealsPlannedSubtitle')"
        :value="stats.mealsPlanned"
        :change="-5"
        :action-text="$t('dashboard.stats.viewPlans')"
        @action="router.push('/meal-plans')"
      />
      <QuickStatsCard
        :title="$t('dashboard.stats.groceryItems')"
        :subtitle="$t('dashboard.stats.groceryItemsSubtitle')"
        :value="stats.groceryItems"
        :change="8"
        :action-text="$t('dashboard.stats.viewList')"
        @action="router.push('/grocery-lists')"
      />
      <QuickStatsCard
        :title="$t('dashboard.stats.caloriesToday')"
        :subtitle="$t('dashboard.stats.caloriesTodaySubtitle', { goal: 2000 })"
        :value="stats.caloriesToday"
        :change="null"
        :action-text="$t('dashboard.stats.trackNutrition')"
        @action="router.push('/nutrition')"
      />
    </div>

    <!-- Main content grid -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 items-stretch">
      <!-- Weekly meal plan (primary focus) -->
      <div class="lg:col-span-2 flex">
        <WeeklyMealPlanOverview class="flex-1" />
      </div>

      <!-- Today's meals -->
      <div class="lg:col-span-1 flex">
        <TodaysMeals class="flex-1" />
      </div>
    </div>

    <!-- Quick actions and recent activity -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Quick actions -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">{{ $t('dashboard.quickActions.title') }}</h3>
        <div class="grid grid-cols-2 gap-4">
          <button
            @click="router.push('/recipes')"
            class="p-4 bg-blue-50 rounded-lg hover:bg-blue-100 transition-colors text-left"
          >
            <div class="flex items-center mb-2">
              <svg class="h-6 w-6 text-blue-600 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
              </svg>
              <span class="font-medium text-gray-900">{{ $t('dashboard.quickActions.browseRecipes') }}</span>
            </div>
            <p class="text-sm text-gray-600">{{ $t('dashboard.quickActions.browseRecipesDesc') }}</p>
          </button>

          <button
            @click="router.push('/meal-plans')"
            class="p-4 bg-green-50 rounded-lg hover:bg-green-100 transition-colors text-left"
          >
            <div class="flex items-center mb-2">
              <svg class="h-6 w-6 text-green-600 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              <span class="font-medium text-gray-900">{{ $t('dashboard.quickActions.planMeals') }}</span>
            </div>
            <p class="text-sm text-gray-600">{{ $t('dashboard.quickActions.planMealsDesc') }}</p>
          </button>

          <button
            @click="router.push('/grocery-lists')"
            class="p-4 bg-yellow-50 rounded-lg hover:bg-yellow-100 transition-colors text-left"
          >
            <div class="flex items-center mb-2">
              <svg class="h-6 w-6 text-yellow-600 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
              </svg>
              <span class="font-medium text-gray-900">{{ $t('dashboard.quickActions.createList') }}</span>
            </div>
            <p class="text-sm text-gray-600">{{ $t('dashboard.quickActions.createListDesc') }}</p>
          </button>

          <button
            @click="router.push('/nutrition')"
            class="p-4 bg-purple-50 rounded-lg hover:bg-purple-100 transition-colors text-left"
          >
            <div class="flex items-center mb-2">
              <svg class="h-6 w-6 text-purple-600 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
              <span class="font-medium text-gray-900">{{ $t('dashboard.quickActions.trackNutrition') }}</span>
            </div>
            <p class="text-sm text-gray-600">{{ $t('dashboard.quickActions.trackNutritionDesc') }}</p>
          </button>
        </div>
      </div>

      <!-- Recent activity -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">{{ $t('dashboard.recentActivity.title') }}</h3>
        <div class="space-y-4">
          <div
            v-for="activity in recentActivity"
            :key="activity.id"
            class="flex items-start"
          >
            <div class="flex-shrink-0">
              <div class="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center">
                <component :is="activity.icon" class="h-4 w-4 text-gray-600" />
              </div>
            </div>
            <div class="ml-3 flex-1">
              <p class="text-sm text-gray-900">{{ activity.description }}</p>
              <p class="text-xs text-gray-500">{{ activity.time }}</p>
            </div>
          </div>
        </div>
        <button class="mt-4 text-sm text-blue-600 hover:text-blue-700 font-medium">
          {{ $t('dashboard.recentActivity.viewAll') }}
        </button>
      </div>
    </div>

    <!-- Recommendations -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-semibold text-gray-900">{{ $t('dashboard.recommendations.title') }}</h3>
        <button class="text-sm text-blue-600 hover:text-blue-700 font-medium">
          {{ $t('dashboard.recommendations.refresh') }}
        </button>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div
          v-for="recommendation in recommendations"
          :key="recommendation.id"
          class="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer"
        >
          <div class="flex items-center mb-2">
            <div class="w-10 h-10 bg-gray-100 rounded-lg flex items-center justify-center mr-3">
              <svg class="h-5 w-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
            <div>
              <h4 class="font-medium text-gray-900">{{ recommendation.name }}</h4>
              <p class="text-xs text-gray-500">{{ recommendation.type }}</p>
            </div>
          </div>
          <p class="text-sm text-gray-600">{{ recommendation.description }}</p>
          <div class="mt-2 flex items-center justify-between">
            <span class="text-xs text-gray-500">{{ recommendation.calories }} cal</span>
            <button class="text-xs text-blue-600 hover:text-blue-700 font-medium">
              {{ $t('dashboard.recommendations.addToPlan') }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import QuickStatsCard from '@/components/dashboard/QuickStatsCard.vue'
import WeeklyMealPlanOverview from '@/components/dashboard/WeeklyMealPlanOverview.vue'
import TodaysMeals from '@/components/dashboard/TodaysMeals.vue'

const router = useRouter()
const authStore = useAuthStore()

const userFullName = computed(() => authStore.user?.username || '')

// Mock data - replace with actual data from store/API
const stats = {
  recipesSaved: 24,
  mealsPlanned: 18,
  groceryItems: 32,
  caloriesToday: '1,240 / 2,000'
}

const recentActivity = [
  {
    id: 1,
    description: 'Added "Mediterranean Quinoa Bowl" to your recipes',
    time: '2 hours ago',
    icon: 'svg'
  },
  {
    id: 2,
    description: 'Planned dinner for Wednesday',
    time: '4 hours ago',
    icon: 'svg'
  },
  {
    id: 3,
    description: 'Generated grocery list for this week',
    time: '1 day ago',
    icon: 'svg'
  },
  {
    id: 4,
    description: 'Completed nutrition goal for yesterday',
    time: '2 days ago',
    icon: 'svg'
  }
]

const recommendations = [
  {
    id: 1,
    name: 'Grilled Salmon',
    type: 'Dinner',
    description: 'High protein, omega-3 rich meal perfect for your goals',
    calories: 350
  },
  {
    id: 2,
    name: 'Overnight Oats',
    type: 'Breakfast',
    description: 'Quick, nutritious breakfast to start your day',
    calories: 280
  },
  {
    id: 3,
    name: 'Caesar Salad',
    type: 'Lunch',
    description: 'Fresh greens with homemade dressing',
    calories: 220
  }
]
</script>
