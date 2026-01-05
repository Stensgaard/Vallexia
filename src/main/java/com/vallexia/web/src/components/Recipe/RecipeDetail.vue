<template>
  <div v-if="recipe" class="max-w-4xl mx-auto">
    <!-- Header -->
    <div class="mb-6">
      <div class="flex items-start justify-between mb-4">
        <div class="flex-1">
          <h1 class="text-3xl font-bold text-gray-900 mb-2">
            {{ recipe.name }}
          </h1>
          <div class="flex items-center gap-3 flex-wrap">
            <!-- Category and Cuisine Tags -->
            <span
              v-if="recipe.category"
              class="px-3 py-1 text-sm bg-blue-100 text-blue-700 rounded-full font-medium"
            >
              {{ $t(`recipes.categories.${recipe.category}`) }}
            </span>
            <span
              v-if="recipe.cuisineType"
              class="px-3 py-1 text-sm bg-blue-100 text-blue-700 rounded-full font-medium"
            >
              {{ $t(`constants.cuisineTypes.${recipe.cuisineType}`) }}
            </span>
            
            <!-- Separator if both category/cuisine and tags exist -->
            <span
              v-if="(recipe.category || recipe.cuisineType) && recipe.tags && recipe.tags.length > 0"
              class="text-gray-400"
            >
              •
            </span>
            
            <!-- Recipe Tags (dish types) -->
            <span
              v-for="tag in recipe.tags"
              :key="tag"
              class="px-3 py-1 text-sm bg-gray-100 text-gray-700 rounded-full"
            >
              {{ tag }}
            </span>
          </div>
        </div>
        <button
          class="p-2 rounded-full hover:bg-gray-100 transition-colors"
          :class="{
            'text-red-500': recipe.isFavorite,
            'text-gray-400': !recipe.isFavorite,
          }"
          @click="$emit('favorite-toggled', recipe.spoonacularId)"
        >
          <svg
            class="w-6 h-6"
            :fill="recipe.isFavorite ? 'currentColor' : 'none'"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
            />
          </svg>
        </button>
      </div>
    </div>

    <!-- Image -->
    <div v-if="recipe.imageUrl" class="mb-6 rounded-lg overflow-hidden">
      <img
        :src="recipe.imageUrl"
        :alt="recipe.name"
        class="w-full h-96 object-cover"
      />
    </div>

    <!-- Meta Info -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
      <div v-if="recipe.prepTimeMinutes" class="bg-gray-50 p-4 rounded-lg">
        <div class="text-sm text-gray-600">
          {{ $t("recipes.detail.prepTime") }}
        </div>
        <div class="text-xl font-semibold">
          {{ recipe.prepTimeMinutes }} {{ $t("recipes.detail.minutes") }}
        </div>
      </div>
      <div v-if="recipe.cookTimeMinutes" class="bg-gray-50 p-4 rounded-lg">
        <div class="text-sm text-gray-600">
          {{ $t("recipes.detail.cookTime") }}
        </div>
        <div class="text-xl font-semibold">
          {{ recipe.cookTimeMinutes }} {{ $t("recipes.detail.minutes") }}
        </div>
      </div>
      <div v-if="recipe.servings" class="bg-gray-50 p-4 rounded-lg">
        <div class="text-sm text-gray-600">
          {{ $t("recipes.detail.servings") }}
        </div>
        <div class="text-xl font-semibold">
          {{ recipe.servings }}
        </div>
      </div>
    </div>

    <!-- Ingredients and Instructions (Side by side on large screens) -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
      <!-- Ingredients -->
      <div v-if="recipe.ingredients && recipe.ingredients.length > 0">
        <h2 class="text-2xl font-semibold mb-4">
          {{ $t("recipes.detail.ingredients") }}
        </h2>
        <ul class="space-y-2">
          <li
            v-for="(ingredient, index) in recipe.ingredients"
            :key="index"
            class="flex items-start gap-3 p-3 bg-gray-50 rounded-lg"
          >
            <span
              class="flex-shrink-0 w-6 h-6 rounded-full bg-blue-500 text-white flex items-center justify-center text-sm font-medium"
            >
              {{ index + 1 }}
            </span>
            <div class="flex-1">
              <span class="font-medium">{{ ingredient.name }}</span>
              <span v-if="ingredient.quantity" class="text-gray-700 ml-2">
                {{
                  formatIngredientQuantity(ingredient.quantity, ingredient.unit)
                }}
              </span>
              <p v-if="ingredient.notes" class="text-sm text-gray-600 mt-1">
                {{ ingredient.notes }}
              </p>
            </div>
          </li>
        </ul>
      </div>

      <!-- Instructions -->
      <div v-if="recipe.instructions">
        <div class="mb-4">
          <div class="flex items-center justify-between mb-2">
            <h2 class="text-2xl font-semibold">
              {{ $t("recipes.detail.instructions") }}
            </h2>
            <button
              v-if="!isCookingMode"
              class="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors text-sm font-medium flex items-center gap-2"
              @click="startCookingMode"
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
                  d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"
                />
              </svg>
              {{ $t("recipes.detail.startCooking") }}
            </button>
            <button
              v-else
              class="px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 transition-colors text-sm font-medium flex items-center gap-2"
              @click="exitCookingMode"
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
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
              {{ $t("recipes.detail.exitCookingMode") }}
            </button>
          </div>
          <div v-if="isCookingMode" class="text-sm text-gray-600">
            {{ $t("recipes.detail.step") }} {{ completedStepsCount }}
            {{ $t("recipes.detail.of") }} {{ parsedInstructions.length }}
          </div>
        </div>
        <ul class="space-y-2">
          <li
            v-for="(step, index) in parsedInstructions"
            :key="index"
            class="flex items-start gap-3 p-3 bg-gray-50 rounded-lg transition-colors"
            :class="{ 'bg-green-50': isCookingMode && checkedSteps[index] }"
          >
            <div
              v-if="!isCookingMode"
              class="flex-shrink-0 w-6 h-6 rounded-full bg-blue-500 text-white flex items-center justify-center text-sm font-medium"
            >
              {{ index + 1 }}
            </div>
            <label v-else class="flex-shrink-0 cursor-pointer">
              <input
                type="checkbox"
                :checked="checkedSteps[index] || false"
                class="w-6 h-6 rounded border-gray-300 text-green-600 focus:ring-green-500 focus:ring-2 cursor-pointer"
                @change="toggleStep(index)"
              />
            </label>
            <div
              class="flex-1 text-gray-700"
              :class="{
                'line-through text-gray-500':
                  isCookingMode && checkedSteps[index],
              }"
              v-html="sanitizeRecipeInstructions(step)"
            ></div>
          </li>
        </ul>
        <div
          v-if="
            isCookingMode && completedStepsCount === parsedInstructions.length
          "
          class="mt-4 p-4 bg-green-100 border border-green-300 rounded-lg"
        >
          <div class="flex items-center gap-2 text-green-800">
            <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
              <path
                fill-rule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                clip-rule="evenodd"
              />
            </svg>
            <span class="font-semibold">{{
              $t("recipes.detail.allStepsCompleted")
            }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Nutritional Info -->
    <div v-if="recipe.nutritionalInfo" class="mb-6">
      <div class="flex items-center gap-2 mb-4">
        <h2 class="text-2xl font-semibold">
          {{ $t("recipes.detail.nutritionalInfo") }}
        </h2>
        <span class="text-sm text-gray-500">{{
          $t("recipes.detail.perServing")
        }}</span>
      </div>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div
          v-if="recipe.nutritionalInfo.calories"
          class="bg-blue-50 p-4 rounded-lg"
        >
          <div class="text-sm text-blue-600">
            {{ $t("recipes.detail.calories") }}
          </div>
          <div class="text-xl font-semibold">
            {{ formatNumber(recipe.nutritionalInfo.calories, 0) }}
          </div>
        </div>
        <div
          v-if="recipe.nutritionalInfo.protein"
          class="bg-green-50 p-4 rounded-lg"
        >
          <div class="text-sm text-green-600">
            {{ $t("recipes.detail.protein") }}
          </div>
          <div class="text-xl font-semibold">
            {{ formatNutritionalValue(recipe.nutritionalInfo.protein) }}
          </div>
        </div>
        <div
          v-if="recipe.nutritionalInfo.carbs"
          class="bg-purple-50 p-4 rounded-lg"
        >
          <div class="text-sm text-purple-600">
            {{ $t("recipes.detail.carbs") }}
          </div>
          <div class="text-xl font-semibold">
            {{ formatNutritionalValue(recipe.nutritionalInfo.carbs) }}
          </div>
        </div>
        <div
          v-if="recipe.nutritionalInfo.fats"
          class="bg-orange-50 p-4 rounded-lg"
        >
          <div class="text-sm text-orange-600">
            {{ $t("recipes.detail.fats") }}
          </div>
          <div class="text-xl font-semibold">
            {{ formatNutritionalValue(recipe.nutritionalInfo.fats) }}
          </div>
        </div>
      </div>
    </div>
  </div>

  <div v-else class="text-center py-12">
    <p class="text-gray-500">{{ $t("recipes.detail.loading") }}</p>
  </div>
</template>

<script setup>
import { computed, ref, watch, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import { useSettingsStore } from "@/stores/settings";
import { useFormattedValue } from "@/composables/useFormattedValue";
import { sanitizeRecipeInstructions } from "@/utils/sanitizeUtils";

useI18n();
const settingsStore = useSettingsStore();

const formatNumber = (number, decimals = 0) => {
  return settingsStore.formatNumberFn(number, decimals);
};

// Use composable for formatted values with proper Vue reactivity
const { formatIngredientQuantity, formatNutritionalValue } =
  useFormattedValue();

const props = defineProps({
  recipe: {
    type: Object,
    default: null,
  },
});

defineEmits(["favorite-toggled"]);

// Cooking mode state
const isCookingMode = ref(false);
const checkedSteps = ref({});

// Get localStorage key for this recipe
const getStorageKey = (recipeId) => `recipe_cooking_${recipeId}`;

// Load saved state from localStorage
const loadSavedState = () => {
  if (!props.recipe?.spoonacularId) {
    return;
  }

  const storageKey = getStorageKey(props.recipe.spoonacularId);
  const saved = localStorage.getItem(storageKey);

  if (saved) {
    const parsed = JSON.parse(saved);
    isCookingMode.value = parsed.isCookingMode || false;
    checkedSteps.value = parsed.checkedSteps || {};
  }
};

// Save state to localStorage
const saveState = () => {
  if (!props.recipe?.spoonacularId) {
    return;
  }

  const storageKey = getStorageKey(props.recipe.spoonacularId);
  const state = {
    isCookingMode: isCookingMode.value,
    checkedSteps: checkedSteps.value,
  };

  localStorage.setItem(storageKey, JSON.stringify(state));
};

// Start cooking mode
const startCookingMode = () => {
  isCookingMode.value = true;
  saveState();
};

// Exit cooking mode
const exitCookingMode = () => {
  isCookingMode.value = false;
  checkedSteps.value = {};
  saveState();
};

// Toggle step completion
const toggleStep = (index) => {
  checkedSteps.value[index] = !checkedSteps.value[index];
  saveState();
};

// Count completed steps
const completedStepsCount = computed(() => {
  return Object.values(checkedSteps.value).filter(Boolean).length;
});

// Parse instructions string into array of steps
const parsedInstructions = computed(() => {
  if (!props.recipe?.instructions) {
    return [];
  }

  // Split by numbered patterns like "1.", "2.", etc.
  // This regex matches: number followed by period and optional space
  const steps = props.recipe.instructions
    .split(/\s*(?=\d+\.\s)/)
    .filter((step) => step.trim().length > 0);

  // Clean up each step by removing the leading number and period
  return steps.map((step) => {
    // Remove leading number and period (e.g., "1. " or "1.")
    return step.replace(/^\d+\.\s*/, "").trim();
  });
});

// Reset state when recipe changes
watch(
  () => props.recipe?.spoonacularId,
  (newId, oldId) => {
    if (newId !== oldId) {
      isCookingMode.value = false;
      checkedSteps.value = {};
      if (newId) {
        loadSavedState();
      }
    }
  },
);

// Load state on mount
onMounted(() => {
  if (props.recipe?.spoonacularId) {
    loadSavedState();
  }
});
</script>
