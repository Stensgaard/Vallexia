<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">
          {{ $t("nutrition.tracking") }}
        </h1>
        <p class="text-gray-600">{{ $t("nutrition.description") }}</p>
      </div>
      <button
        class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md font-medium"
      >
        {{ $t("nutrition.setGoals") }}
      </button>
    </div>

    <!-- Multi-person household info banner -->
    <div
      v-if="showFamilyUpgradeBanner"
      class="bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 rounded-lg p-4 flex items-start gap-4"
    >
      <div class="flex-shrink-0">
        <svg
          class="h-6 w-6 text-blue-600"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
          />
        </svg>
      </div>
      <div class="flex-1">
        <h3 class="text-sm font-semibold text-gray-900 mb-1">
          {{ $t("nutrition.familyUpgrade.title") }}
        </h3>
        <p
          class="text-sm text-gray-700 mb-3"
          v-html="$t('nutrition.familyUpgrade.description')"
        ></p>
        <router-link
          to="/subscription"
          class="inline-flex items-center text-sm font-medium text-blue-600 hover:text-blue-700"
        >
          {{ $t("nutrition.familyUpgrade.learnMore") }}
          <svg
            class="ml-1 h-4 w-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M9 5l7 7-7 7"
            />
          </svg>
        </router-link>
      </div>
    </div>

    <!-- Daily nutrition overview -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div
        class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 text-center"
      >
        <div class="text-2xl font-bold text-gray-900 mb-2">
          {{ formatNumber(1240, 0) }}
        </div>
        <div class="text-sm text-gray-600">{{ $t("nutrition.calories") }}</div>
        <div class="text-xs text-gray-500 mt-1">
          {{ $t("nutrition.goal") }} {{ formatNumber(2000, 0) }}
        </div>
        <div class="w-full bg-gray-200 rounded-full h-2 mt-2">
          <div class="bg-blue-600 h-2 rounded-full" style="width: 62%"></div>
        </div>
      </div>

      <div
        class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 text-center"
      >
        <div class="text-2xl font-bold text-gray-900 mb-2">
          {{ formatNutritionalValue(85) }}
        </div>
        <div class="text-sm text-gray-600">{{ $t("nutrition.protein") }}</div>
        <div class="text-xs text-gray-500 mt-1">
          {{ $t("nutrition.goal") }} {{ formatNutritionalValue(120) }}
        </div>
        <div class="w-full bg-gray-200 rounded-full h-2 mt-2">
          <div class="bg-green-600 h-2 rounded-full" style="width: 71%"></div>
        </div>
      </div>

      <div
        class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 text-center"
      >
        <div class="text-2xl font-bold text-gray-900 mb-2">
          {{ formatNutritionalValue(145) }}
        </div>
        <div class="text-sm text-gray-600">{{ $t("nutrition.carbs") }}</div>
        <div class="text-xs text-gray-500 mt-1">
          {{ $t("nutrition.goal") }} {{ formatNutritionalValue(200) }}
        </div>
        <div class="w-full bg-gray-200 rounded-full h-2 mt-2">
          <div class="bg-yellow-600 h-2 rounded-full" style="width: 73%"></div>
        </div>
      </div>

      <div
        class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 text-center"
      >
        <div class="text-2xl font-bold text-gray-900 mb-2">
          {{ formatNutritionalValue(45) }}
        </div>
        <div class="text-sm text-gray-600">{{ $t("nutrition.fat") }}</div>
        <div class="text-xs text-gray-500 mt-1">
          {{ $t("nutrition.goal") }} {{ formatNutritionalValue(65) }}
        </div>
        <div class="w-full bg-gray-200 rounded-full h-2 mt-2">
          <div class="bg-purple-600 h-2 rounded-full" style="width: 69%"></div>
        </div>
      </div>
    </div>

    <!-- Weekly progress -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h3 class="text-lg font-semibold text-gray-900 mb-4">
        {{ $t("nutrition.weeklyProgress") }}
      </h3>
      <div class="grid grid-cols-7 gap-4">
        <div v-for="day in weekProgress" :key="day.name" class="text-center">
          <div class="text-sm font-medium text-gray-900 mb-2">
            {{ day.name }}
          </div>
          <div class="w-full bg-gray-200 rounded-full h-2 mb-2">
            <div
              class="bg-blue-600 h-2 rounded-full"
              :style="`width: ${day.progress}%`"
            ></div>
          </div>
          <div class="text-xs text-gray-500">
            {{ formatNumber(day.calories, 0) }} cal
          </div>
        </div>
      </div>
    </div>

    <!-- Meal breakdown -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">
          {{ $t("nutrition.todaysMeals") }}
        </h3>
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
              <div class="font-medium text-gray-900">
                {{ formatNumber(meal.calories, 0) }} cal
              </div>
              <div class="text-xs text-gray-500">
                {{ formatNutritionalValue(meal.protein) }} protein
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">
          {{ $t("nutrition.nutritionalGoals") }}
        </h3>
        <div class="space-y-4">
          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-600">{{
              $t("nutrition.dailyCalories")
            }}</span>
            <span class="text-sm font-medium text-gray-900">{{
              formatNumber(2000, 0)
            }}</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-600">{{
              $t("nutrition.protein")
            }}</span>
            <span class="text-sm font-medium text-gray-900">{{
              formatNutritionalValue(120)
            }}</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-600">{{
              $t("nutrition.carbs")
            }}</span>
            <span class="text-sm font-medium text-gray-900">{{
              formatNutritionalValue(200)
            }}</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-600">{{ $t("nutrition.fat") }}</span>
            <span class="text-sm font-medium text-gray-900">{{
              formatNutritionalValue(65)
            }}</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-600">{{
              $t("nutrition.fiber")
            }}</span>
            <span class="text-sm font-medium text-gray-900">{{
              formatNutritionalValue(25)
            }}</span>
          </div>
        </div>
        <button
          class="w-full mt-4 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md font-medium"
        >
          {{ $t("nutrition.updateGoals") }}
        </button>
      </div>
    </div>

    <!-- Coming soon message -->
    <div class="bg-blue-50 border border-blue-200 rounded-lg p-6 text-center">
      <svg
        class="mx-auto h-12 w-12 text-blue-400 mb-4"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
        />
      </svg>
      <h3 class="text-lg font-semibold text-gray-900 mb-2">
        {{ $t("nutrition.comingSoon") }}
      </h3>
      <p class="text-gray-600">
        {{ $t("nutrition.comingSoonDescription") }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useAuthStore } from "@/stores/auth";
import { useSettingsStore } from "@/stores/settings";
import { shouldShowFamilyUpgrade } from "@/utils/subscriptionUtils";
import { useFormattedValue } from "@/composables/useFormattedValue";

const { t } = useI18n();
const authStore = useAuthStore();
const settingsStore = useSettingsStore();

// Use composable for formatted values with proper Vue reactivity
const { formatNutritionalValue } = useFormattedValue();

const formatNumber = (number, decimals = 0) => {
  return settingsStore.formatNumberFn(number, decimals);
};

// Check if user has multi-person household and is not on Family subscription
const showFamilyUpgradeBanner = computed(() => {
  return shouldShowFamilyUpgrade(authStore.user);
});

// Mock data for nutrition tracking
const weekProgress = computed(() => {
  const days = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];
  const data = [
    { calories: 1850, progress: 92 },
    { calories: 2100, progress: 105 },
    { calories: 1950, progress: 98 },
    { calories: 2200, progress: 110 },
    { calories: 1800, progress: 90 },
    { calories: 2400, progress: 120 },
    { calories: 1240, progress: 62 },
  ];
  return days.map((day, index) => ({
    name: day,
    ...data[index],
  }));
});

const todaysMeals = computed(() => [
  {
    type: t("nutrition.mealTypes.breakfast"),
    name: "Oatmeal with Berries",
    calories: 320,
    protein: 12,
  },
  {
    type: t("nutrition.mealTypes.lunch"),
    name: "Mediterranean Salad",
    calories: 280,
    protein: 15,
  },
  {
    type: t("nutrition.mealTypes.dinner"),
    name: "Grilled Salmon",
    calories: 450,
    protein: 35,
  },
  {
    type: t("nutrition.mealTypes.snacks"),
    name: "Greek Yogurt",
    calories: 190,
    protein: 18,
  },
]);
</script>
