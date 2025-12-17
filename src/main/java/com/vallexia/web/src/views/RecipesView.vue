<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">
          {{ $t("recipes.title") }}
        </h1>
        <p class="text-gray-600">{{ $t("recipes.description") }}</p>
      </div>
    </div>

    <!-- Recipe list -->
    <RecipeList
      :recipes="recipeStore.filteredRecipes"
      :loading="recipeStore.isLoading"
      :current-page="recipeStore.pagination.page"
      :total-pages="recipeStore.pagination.totalPages"
      @recipe-clicked="handleRecipeClick"
      @favorite-toggled="handleFavoriteToggle"
      @page-change="handlePageChange"
    />

    <!-- Error message -->
    <div
      v-if="recipeStore.error"
      class="bg-red-50 border border-red-200 rounded-lg p-4"
    >
      <p class="text-red-800">{{ recipeStore.error }}</p>
      <button
        class="mt-2 text-sm text-red-600 hover:text-red-700"
        @click="recipeStore.clearError()"
      >
        {{ $t("common.dismiss") }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from "vue";
import { useRouter } from "vue-router";
import { useRecipeStore } from "@/stores/recipe";
import RecipeList from "@/components/Recipe/RecipeList.vue";

const router = useRouter();
const recipeStore = useRecipeStore();

onMounted(async () => {
  await recipeStore.fetchRecipes(0, 20);
});

const handleRecipeClick = (recipe) => {
  router.push(`/recipes/${recipe.id}`);
};

const handleFavoriteToggle = async (recipeId) => {
  await recipeStore.toggleFavorite(recipeId);
};

const handlePageChange = async (page) => {
  await recipeStore.fetchRecipes(page, 20);
};
</script>
