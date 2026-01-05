<template>
  <div>
    <!-- Results Summary -->
    <div
      v-if="!loading && recipes && recipes.length > 0"
      class="mb-4 flex items-center justify-between"
    >
      <p class="text-sm text-gray-600">
        {{ $t("recipes.list.resultsCount", { count: recipes.length }) }}
      </p>
      <p class="text-sm text-gray-600">
        {{ $t("recipes.list.showingRange", {
          start: startIndex + 1,
          end: endIndex,
          total: recipes.length
        }) }}
      </p>
    </div>

    <div
      v-if="loading"
      class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6"
    >
      <div
        v-for="n in 8"
        :key="n"
        class="bg-white rounded-lg shadow-sm border border-gray-200 animate-pulse"
      >
        <div class="h-48 bg-gray-200"></div>
        <div class="p-4">
          <div class="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
          <div class="h-3 bg-gray-200 rounded w-full mb-2"></div>
          <div class="h-3 bg-gray-200 rounded w-2/3"></div>
        </div>
      </div>
    </div>

    <div
      v-else-if="paginatedRecipes && paginatedRecipes.length > 0"
      class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6"
    >
      <RecipeCard
        v-for="recipe in paginatedRecipes"
        :key="recipe.spoonacularId"
        :recipe="recipe"
        @recipe-clicked="handleRecipeClick"
        @favorite-toggled="handleFavoriteToggle"
      />
    </div>

    <div v-else class="text-center py-12">
      <p class="text-gray-500 text-lg">{{ $t("recipes.list.noRecipes") }}</p>
      <p class="text-gray-400 text-sm mt-2">
        {{ $t("recipes.list.tryAdjusting") }}
      </p>
    </div>

    <!-- Pagination Controls -->
    <div
      v-if="!loading && recipes && recipes.length > 0 && computedTotalPages > 1"
      class="flex items-center justify-center gap-2 mt-8"
    >
      <button
        :disabled="localCurrentPage === 0"
        class="px-4 py-2 border border-gray-300 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
        @click="handlePreviousPage"
      >
        {{ $t("recipes.list.previous") }}
      </button>
      <span class="px-4 py-2 text-sm text-gray-700">
        {{
          $t("recipes.list.page", {
            current: localCurrentPage + 1,
            total: computedTotalPages,
          })
        }}
      </span>
      <button
        :disabled="localCurrentPage >= computedTotalPages - 1"
        class="px-4 py-2 border border-gray-300 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
        @click="handleNextPage"
      >
        {{ $t("recipes.list.next") }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from "vue";
import { useI18n } from "vue-i18n";
import RecipeCard from "./RecipeCard.vue";

useI18n();

const props = defineProps({
  recipes: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
  showPagination: {
    type: Boolean,
    default: true,
  },
  currentPage: {
    type: Number,
    default: 0,
  },
  totalPages: {
    type: Number,
    default: 1,
  },
});

const emit = defineEmits(["recipe-clicked", "favorite-toggled", "page-change"]);

// Client-side pagination state
const localCurrentPage = ref(0);
const pageSize = 20;

// Reset to first page when recipes change (new search)
watch(() => props.recipes, () => {
  localCurrentPage.value = 0;
}, { deep: true });

// Computed total pages based on recipes length
const computedTotalPages = computed(() => {
  if (!props.recipes || props.recipes.length === 0) {
    return 1;
  }
  return Math.ceil(props.recipes.length / pageSize);
});

// Computed paginated recipes
const paginatedRecipes = computed(() => {
  if (!props.recipes || props.recipes.length === 0) {
    return [];
  }
  const start = localCurrentPage.value * pageSize;
  const end = start + pageSize;
  return props.recipes.slice(start, end);
});

// Computed indices for display
const startIndex = computed(() => localCurrentPage.value * pageSize);
const endIndex = computed(() => {
  const end = startIndex.value + pageSize;
  return Math.min(end, props.recipes?.length || 0);
});

// Pagination handlers
const handlePreviousPage = () => {
  if (localCurrentPage.value > 0) {
    localCurrentPage.value--;
    emit("page-change", localCurrentPage.value);
  }
};

const handleNextPage = () => {
  if (localCurrentPage.value < computedTotalPages.value - 1) {
    localCurrentPage.value++;
    emit("page-change", localCurrentPage.value);
  }
};

const handleRecipeClick = (recipe) => {
  emit("recipe-clicked", recipe);
};

const handleFavoriteToggle = (recipeId) => {
  emit("favorite-toggled", recipeId);
};
</script>
