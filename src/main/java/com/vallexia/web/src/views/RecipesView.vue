<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Recipes</h1>
        <p class="text-gray-600">Browse and explore the recipe collection</p>
      </div>
    </div>

    <!-- Search and filters -->
    <RecipeSearch
      :criteria="recipeStore.searchCriteria"
      :total-results="recipeStore.pagination.totalElements"
      @search="handleSearch"
      @criteria-changed="handleCriteriaChanged"
    />

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
    <div v-if="recipeStore.error" class="bg-red-50 border border-red-200 rounded-lg p-4">
      <p class="text-red-800">{{ recipeStore.error }}</p>
      <button
        @click="recipeStore.clearError()"
        class="mt-2 text-sm text-red-600 hover:text-red-700"
      >
        Dismiss
      </button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useRecipeStore } from '@/stores/recipe'
import RecipeList from '@/components/Recipe/RecipeList.vue'
import RecipeSearch from '@/components/Recipe/RecipeSearch.vue'

const router = useRouter()
const recipeStore = useRecipeStore()

onMounted(async () => {
  // Perform initial search with default criteria (all categories, cuisines, and difficulty levels)
  await recipeStore.searchRecipes(recipeStore.searchCriteria, 0, 20)
})

const handleSearch = async (criteria) => {
  await recipeStore.searchRecipes(criteria, 0, 20)
}

const handleCriteriaChanged = (criteria) => {
  // Store criteria for later use
  recipeStore.searchCriteria = criteria
}

const handleRecipeClick = (recipe) => {
  router.push(`/recipes/${recipe.id}`)
}

const handleFavoriteToggle = async (recipeId) => {
  await recipeStore.toggleFavorite(recipeId)
}

const handlePageChange = async (page) => {
  // Always use searchRecipes since we default to "all" criteria
  await recipeStore.searchRecipes(recipeStore.searchCriteria, page, 20)
}
</script>
