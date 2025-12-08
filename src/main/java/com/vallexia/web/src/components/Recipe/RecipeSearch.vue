<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
    <h3 class="text-lg font-semibold mb-4">{{ $t("recipes.search.title") }}</h3>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <!-- Search Query -->
      <div>
        <label for="recipe-search-query" class="block text-sm font-medium text-gray-700 mb-1">{{
          $t("recipes.search.search")
        }}</label>
        <input
          id="recipe-search-query"
          v-model="localCriteria.query"
          type="text"
          :placeholder="$t('recipes.search.searchPlaceholder')"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          @input="updateCriteria"
        />
      </div>

      <!-- Category -->
      <div>
        <label for="recipe-search-category" class="block text-sm font-medium text-gray-700 mb-1">{{
          $t("recipes.search.category")
        }}</label>
        <select
          id="recipe-search-category"
          v-model="localCriteria.category"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          @change="updateCriteria"
        >
          <option value="">{{ $t("recipes.search.allCategories") }}</option>
          <option
            v-for="category in mealCategories"
            :key="category.code"
            :value="category.code"
          >
            {{ $t(`recipes.categories.${category.code}`) || category.name }}
          </option>
        </select>
      </div>

      <!-- Cuisine Type -->
      <div>
        <div class="flex items-center gap-1 mb-1">
          <label for="recipe-search-cuisine-type" class="block text-sm font-medium text-gray-700">{{
            $t("recipes.search.cuisine")
          }}</label>
          <div class="relative group">
            <button
              type="button"
              class="inline-flex items-center justify-center w-4 h-4 rounded-full text-gray-400 hover:text-gray-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-1"
              :aria-label="$t('recipes.search.cuisine')"
            >
              <svg
                class="w-4 h-4"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </button>
            <!-- Tooltip -->
            <div
              class="absolute left-1/2 -translate-x-1/2 bottom-full mb-2 px-3 py-2 w-72 text-xs text-white bg-gray-900 rounded-lg shadow-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 pointer-events-none z-50"
            >
              {{ $t("recipes.search.cuisineTooltip") }}
              <!-- Tooltip arrow -->
              <div
                class="absolute left-1/2 -translate-x-1/2 top-full w-0 h-0 border-l-4 border-r-4 border-t-4 border-transparent border-t-gray-900"
              ></div>
            </div>
          </div>
        </div>
        <select
          id="recipe-search-cuisine-type"
          v-model="localCriteria.cuisineType"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          @change="updateCriteria"
        >
          <option value="">{{ $t("recipes.search.allCuisines") }}</option>
          <option
            v-for="cuisineType in cuisineOptions"
            :key="cuisineType.code"
            :value="cuisineType.code"
          >
            {{
              $t(`constants.cuisineTypes.${cuisineType.code}`) ||
              cuisineType.name
            }}
          </option>
        </select>
      </div>

      <!-- Difficulty Level -->
      <div>
        <label for="recipe-search-difficulty" class="block text-sm font-medium text-gray-700 mb-1">{{
          $t("recipes.search.difficulty")
        }}</label>
        <select
          id="recipe-search-difficulty"
          v-model="localCriteria.difficultyLevel"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          @change="updateCriteria"
        >
          <option value="">{{ $t("recipes.search.allLevels") }}</option>
          <option
            v-for="difficulty in difficultyOptions"
            :key="difficulty.code"
            :value="difficulty.code"
          >
            {{ $t(`recipes.difficulty.${difficulty.code}`) || difficulty.name }}
          </option>
        </select>
      </div>

      <!-- Prep Time Range -->
      <div>
        <label for="recipe-search-max-prep-time" class="block text-sm font-medium text-gray-700 mb-1">{{
          $t("recipes.search.maxPrepTime")
        }}</label>
        <input
          id="recipe-search-max-prep-time"
          v-model.number="localCriteria.maxPrepTime"
          type="number"
          :placeholder="$t('recipes.search.maxPrepTimePlaceholder')"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          @input="updateCriteria"
        />
      </div>

      <!-- Calories Range -->
      <div>
        <label for="recipe-search-max-calories" class="block text-sm font-medium text-gray-700 mb-1">{{
          $t("recipes.search.maxCalories")
        }}</label>
        <input
          id="recipe-search-max-calories"
          v-model.number="localCriteria.maxCalories"
          type="number"
          :placeholder="$t('recipes.search.maxCaloriesPlaceholder')"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          @input="updateCriteria"
        />
      </div>
    </div>

    <div class="flex items-center gap-2 mt-4">
      <button
        class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
        @click="applySearch"
      >
        {{ $t("recipes.search.searchButton") }}
      </button>
      <button
        class="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 transition-colors"
        @click="clearFilters"
      >
        {{ $t("recipes.search.clearFilters") }}
      </button>
      <span v-if="totalResults > 0" class="text-sm text-gray-600 ml-2">
        {{
          totalResults === 1
            ? $t("recipes.search.resultsFound", { count: totalResults })
            : $t("recipes.search.resultsFoundPlural", { count: totalResults })
        }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from "vue";
import { useI18n } from "vue-i18n";
import {
  getMealCategories,
  getCuisineTypes,
  getDifficultyLevels,
} from "@/utils/localeConfig";

useI18n();
const mealCategories = computed(() => getMealCategories());
const cuisineOptions = computed(() => getCuisineTypes());
const difficultyOptions = computed(() => getDifficultyLevels());

const props = defineProps({
  criteria: {
    type: Object,
    default: () => ({
      category: "",
      cuisineType: "",
      difficultyLevel: "",
    }),
  },
  totalResults: {
    type: Number,
    default: 0,
  },
});

const emit = defineEmits(["search", "criteria-changed"]);

const localCriteria = ref({
  category: "",
  cuisineType: "",
  difficultyLevel: "",
  ...props.criteria,
});

watch(
  () => props.criteria,
  (newVal) => {
    localCriteria.value = {
      category: "",
      cuisineType: "",
      difficultyLevel: "",
      ...newVal,
    };
  },
  { deep: true },
);

const updateCriteria = () => {
  emit("criteria-changed", { ...localCriteria.value });
};

const applySearch = () => {
  emit("search", { ...localCriteria.value });
};

const clearFilters = () => {
  localCriteria.value = {
    category: "",
    cuisineType: "",
    difficultyLevel: "",
  };
  emit("criteria-changed", { ...localCriteria.value });
  emit("search", { ...localCriteria.value });
};
</script>
