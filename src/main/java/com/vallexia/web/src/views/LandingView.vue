<template>
  <div class="min-h-screen bg-white">
    <!-- Navigation -->
    <nav class="bg-white shadow-sm">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <div class="flex-shrink-0 flex items-center">
              <svg
                class="h-8 w-8 text-blue-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
                />
              </svg>
              <span class="ml-2 text-xl font-bold text-gray-900">Vallexia</span>
            </div>
          </div>

          <div class="flex items-center space-x-4">
            <!-- Language Switcher -->
            <div class="relative">
              <select
                v-model="currentLocale"
                class="appearance-none bg-white border border-gray-300 rounded-md px-3 py-2 pr-8 text-sm text-gray-700 hover:border-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 cursor-pointer"
              >
                <option
                  v-for="lang in SUPPORTED_LANGUAGES"
                  :key="lang.code"
                  :value="lang.code"
                >
                  {{ $t(`constants.languages.${lang.code}`) }}
                </option>
              </select>
              <div
                class="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-gray-700"
              >
                <svg
                  class="fill-current h-4 w-4"
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 20 20"
                >
                  <path
                    d="M9.293 12.95l.707.707L15.657 8l-1.414-1.414L10 10.828 5.757 6.586 4.343 8z"
                  />
                </svg>
              </div>
            </div>
            <RouterLink
              to="/login"
              class="text-gray-700 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
            >
              {{ $t("landing.signIn") }}
            </RouterLink>
            <RouterLink
              to="/register"
              class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium"
            >
              {{ $t("landing.getStarted") }}
            </RouterLink>
          </div>
        </div>
      </div>
    </nav>

    <!-- Hero Section -->
    <div class="relative bg-gradient-to-r from-blue-600 to-blue-800">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24">
        <div class="text-center">
          <h1 class="text-4xl md:text-6xl font-bold text-white mb-6">
            {{ $t("landing.hero.title") }}
            <span class="block text-blue-200">{{
              $t("landing.hero.subtitle")
            }}</span>
          </h1>
          <p class="text-xl text-blue-100 mb-8 max-w-3xl mx-auto">
            {{ $t("landing.hero.description") }}
          </p>
          <div class="flex flex-col sm:flex-row gap-4 justify-center">
            <RouterLink
              to="/register"
              class="bg-white text-blue-600 px-8 py-3 rounded-lg text-lg font-semibold hover:bg-gray-50 transition-colors"
            >
              {{ $t("landing.hero.startPlanning") }}
            </RouterLink>
            <RouterLink
              to="/login"
              class="border-2 border-white text-white px-8 py-3 rounded-lg text-lg font-semibold hover:bg-white hover:text-blue-600 transition-colors"
            >
              {{ $t("landing.hero.signIn") }}
            </RouterLink>
          </div>
        </div>
      </div>
    </div>

    <!-- Features Section -->
    <div class="py-24 bg-gray-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-16">
          <h2 class="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
            {{ $t("landing.features.title") }}
          </h2>
          <p class="text-xl text-gray-600 max-w-2xl mx-auto">
            {{ $t("landing.features.description") }}
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          <!-- Recipe Management -->
          <div
            class="bg-white p-6 rounded-lg shadow-sm hover:shadow-md transition-shadow"
          >
            <div
              class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mb-4"
            >
              <svg
                class="w-6 h-6 text-blue-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                />
              </svg>
            </div>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">
              {{ $t("landing.features.recipeManagement.title") }}
            </h3>
            <p class="text-gray-600">
              {{ $t("landing.features.recipeManagement.description") }}
            </p>
          </div>

          <!-- Meal Planning -->
          <div
            class="bg-white p-6 rounded-lg shadow-sm hover:shadow-md transition-shadow"
          >
            <div
              class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center mb-4"
            >
              <svg
                class="w-6 h-6 text-green-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                />
              </svg>
            </div>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">
              {{ $t("landing.features.mealPlanning.title") }}
            </h3>
            <p class="text-gray-600">
              {{ $t("landing.features.mealPlanning.description") }}
            </p>
          </div>

          <!-- Grocery Lists -->
          <div
            class="bg-white p-6 rounded-lg shadow-sm hover:shadow-md transition-shadow"
          >
            <div
              class="w-12 h-12 bg-yellow-100 rounded-lg flex items-center justify-center mb-4"
            >
              <svg
                class="w-6 h-6 text-yellow-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"
                />
              </svg>
            </div>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">
              {{ $t("landing.features.groceryLists.title") }}
            </h3>
            <p class="text-gray-600">
              {{ $t("landing.features.groceryLists.description") }}
            </p>
          </div>

          <!-- Nutrition Tracking -->
          <div
            class="bg-white p-6 rounded-lg shadow-sm hover:shadow-md transition-shadow"
          >
            <div
              class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center mb-4"
            >
              <svg
                class="w-6 h-6 text-purple-600"
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
            </div>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">
              {{ $t("landing.features.nutritionTracking.title") }}
            </h3>
            <p class="text-gray-600">
              {{ $t("landing.features.nutritionTracking.description") }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Benefits Section -->
    <div class="py-24 bg-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          <div>
            <h2 class="text-3xl md:text-4xl font-bold text-gray-900 mb-6">
              {{ $t("landing.benefits.title") }}
            </h2>
            <div class="space-y-6">
              <div class="flex items-start">
                <div class="flex-shrink-0">
                  <svg
                    class="w-6 h-6 text-green-500"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M5 13l4 4L19 7"
                    />
                  </svg>
                </div>
                <div class="ml-3">
                  <h3 class="text-lg font-semibold text-gray-900">
                    {{ $t("landing.benefits.aiRecommendations.title") }}
                  </h3>
                  <p class="text-gray-600">
                    {{ $t("landing.benefits.aiRecommendations.description") }}
                  </p>
                </div>
              </div>
              <div class="flex items-start">
                <div class="flex-shrink-0">
                  <svg
                    class="w-6 h-6 text-green-500"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M5 13l4 4L19 7"
                    />
                  </svg>
                </div>
                <div class="ml-3">
                  <h3 class="text-lg font-semibold text-gray-900">
                    {{ $t("landing.benefits.timeSaving.title") }}
                  </h3>
                  <p class="text-gray-600">
                    {{ $t("landing.benefits.timeSaving.description") }}
                  </p>
                </div>
              </div>
              <div class="flex items-start">
                <div class="flex-shrink-0">
                  <svg
                    class="w-6 h-6 text-green-500"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M5 13l4 4L19 7"
                    />
                  </svg>
                </div>
                <div class="ml-3">
                  <h3 class="text-lg font-semibold text-gray-900">
                    {{ $t("landing.benefits.nutritionalBalance.title") }}
                  </h3>
                  <p class="text-gray-600">
                    {{ $t("landing.benefits.nutritionalBalance.description") }}
                  </p>
                </div>
              </div>
            </div>
          </div>
          <div
            class="bg-gradient-to-br from-blue-50 to-blue-100 p-8 rounded-2xl"
          >
            <div class="text-center">
              <svg
                class="w-24 h-24 text-blue-600 mx-auto mb-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="1.5"
                  d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
                />
              </svg>
              <h3 class="text-2xl font-bold text-gray-900 mb-4">
                {{ $t("landing.benefits.getStarted.title") }}
              </h3>
              <p class="text-gray-600 mb-6">
                {{ $t("landing.benefits.getStarted.description") }}
              </p>
              <RouterLink
                to="/register"
                class="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-semibold transition-colors"
              >
                {{ $t("landing.benefits.getStarted.button") }}
              </RouterLink>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <footer class="bg-gray-900 text-white py-12">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div class="col-span-1 md:col-span-2">
            <div class="flex items-center mb-4">
              <svg
                class="h-8 w-8 text-blue-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
                />
              </svg>
              <span class="ml-2 text-xl font-bold">Vallexia</span>
            </div>
            <p class="text-gray-400 mb-4">
              {{ $t("landing.footer.tagline") }}
            </p>
          </div>
          <div>
            <h3 class="text-lg font-semibold mb-4">
              {{ $t("landing.footer.product") }}
            </h3>
            <ul class="space-y-2 text-gray-400">
              <li>
                <a href="#" class="hover:text-white transition-colors">{{
                  $t("landing.footer.features")
                }}</a>
              </li>
              <li>
                <a href="#" class="hover:text-white transition-colors">{{
                  $t("landing.footer.pricing")
                }}</a>
              </li>
              <li>
                <a href="#" class="hover:text-white transition-colors">{{
                  $t("landing.footer.api")
                }}</a>
              </li>
            </ul>
          </div>
          <div>
            <h3 class="text-lg font-semibold mb-4">
              {{ $t("landing.footer.support") }}
            </h3>
            <ul class="space-y-2 text-gray-400">
              <li>
                <a href="#" class="hover:text-white transition-colors">{{
                  $t("landing.footer.helpCenter")
                }}</a>
              </li>
              <li>
                <a href="#" class="hover:text-white transition-colors">{{
                  $t("landing.footer.contactUs")
                }}</a>
              </li>
              <li>
                <a href="#" class="hover:text-white transition-colors">{{
                  $t("landing.footer.privacyPolicy")
                }}</a>
              </li>
            </ul>
          </div>
        </div>
        <div
          class="border-t border-gray-800 mt-8 pt-8 text-center text-gray-400"
        >
          <p>{{ $t("landing.footer.copyright") }}</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { RouterLink } from "vue-router";
import { useI18n } from "vue-i18n";
import { SUPPORTED_LANGUAGES } from "@/i18n";

const { locale } = useI18n();

const currentLocale = computed({
  get: () => locale.value,
  set: (value) => {
    locale.value = value;
  },
});
</script>
