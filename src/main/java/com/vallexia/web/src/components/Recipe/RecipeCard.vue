<template>
  <div
    class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden hover:shadow-md transition-shadow cursor-pointer"
    @click="$emit('recipe-clicked', recipe)"
  >
    <div
      v-if="recipe.imageUrl"
      class="relative h-48 w-full overflow-hidden bg-gray-200"
    >
      <img
        :src="recipe.imageUrl"
        :alt="recipe.name"
        class="w-full h-full object-cover"
      />
    </div>
    <div
      v-else
      class="h-48 w-full bg-gradient-to-br from-blue-50 to-purple-50 flex items-center justify-center"
    >
      <span class="text-4xl">🍳</span>
    </div>

    <div class="p-4">
      <div class="flex items-start justify-between mb-2">
        <h3 class="text-lg font-semibold text-gray-900 truncate flex-1">
          {{ recipe.name }}
        </h3>
        <button
          class="ml-2 p-1 rounded-full hover:bg-gray-100 transition-colors"
          :class="{
            'text-red-500': recipe.isFavorite,
            'text-gray-400': !recipe.isFavorite,
          }"
          @click.stop="$emit('favorite-toggled', recipe.spoonacularId)"
        >
          <svg
            class="w-5 h-5"
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

      <div
        v-if="recipe.description"
        class="text-sm text-gray-600 line-clamp-2 mb-3 recipe-card-description"
        v-html="sanitizedDescription"
      ></div>

      <div class="flex flex-wrap gap-2 mb-3">
        <span
          v-if="recipe.category"
          class="px-2 py-1 text-xs font-medium bg-blue-100 text-blue-800 rounded"
        >
          {{ $t(`recipes.categories.${recipe.category}`) }}
        </span>
        <span
          v-if="recipe.cuisineType"
          class="px-2 py-1 text-xs font-medium bg-purple-100 text-purple-800 rounded"
        >
          {{ $t(`constants.cuisineTypes.${recipe.cuisineType}`) }}
        </span>
      </div>

      <div class="flex items-center justify-between text-sm text-gray-500">
        <div class="flex items-center gap-4">
          <span v-if="recipe.prepTimeMinutes" class="flex items-center">
            <svg
              class="w-4 h-4 mr-1"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
            {{ recipe.prepTimeMinutes }}m
          </span>
          <span v-if="recipe.servings" class="flex items-center">
            <svg
              class="w-4 h-4 mr-1"
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
            {{ recipe.servings }}
          </span>
        </div>
        <span
          v-if="recipe.nutritionalInfo?.calories"
          class="text-xs font-medium text-gray-700"
        >
          {{ formatNumber(recipe.nutritionalInfo.calories, 0) }} kcal
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { useSettingsStore } from "@/stores/settings";
import { sanitizeRecipeDescription } from "@/utils/sanitizeUtils";

useI18n();
const settingsStore = useSettingsStore();

const formatNumber = (number, decimals = 0) => {
  return settingsStore.formatNumberFn(number, decimals);
};

const props = defineProps({
  recipe: {
    type: Object,
    required: true,
  },
});

defineEmits(["recipe-clicked", "favorite-toggled"]);

// Sanitize recipe description HTML
const sanitizedDescription = computed(() => {
  if (!props.recipe?.description) {
    return "";
  }
  return sanitizeRecipeDescription(props.recipe.description);
});
</script>

<style scoped>
.recipe-card-description :deep(a) {
  color: rgb(37, 99, 235);
  text-decoration: underline;
}

.recipe-card-description :deep(a:hover) {
  color: rgb(30, 64, 175);
}

.recipe-card-description :deep(b),
.recipe-card-description :deep(strong) {
  font-weight: 600;
}

.recipe-card-description :deep(i),
.recipe-card-description :deep(em) {
  font-style: italic;
}
</style>
