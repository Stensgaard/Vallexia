<template>
  <div class="space-y-6">
    <div v-if="recipeStore.isLoading" class="text-center py-12">
      <p class="text-gray-500">Loading recipe...</p>
    </div>

    <div v-else-if="recipeStore.error" class="bg-red-50 border border-red-200 rounded-lg p-4">
      <p class="text-red-800">{{ recipeStore.error }}</p>
      <button
        @click="router.back()"
        class="mt-2 text-sm text-red-600 hover:text-red-700"
      >
        Go Back
      </button>
    </div>

    <div v-else-if="recipeStore.currentRecipe">
      <!-- Back button and actions -->
      <div class="flex items-center justify-between mb-4">
        <button
          @click="router.back()"
          class="flex items-center text-gray-600 hover:text-gray-900"
        >
          <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
          Back to Recipes
        </button>
        
      </div>

      <!-- Recipe detail -->
      <RecipeDetail
        :recipe="recipeStore.currentRecipe"
        @favorite-toggled="handleFavoriteToggle"
      />

      <!-- Scaling section -->
      <div v-if="recipeStore.currentRecipe.servings" class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <h3 class="text-lg font-semibold mb-4">Scale Recipe</h3>
        <div class="flex items-center gap-4">
          <label class="text-sm font-medium text-gray-700">Servings:</label>
          <input
            v-model.number="targetServings"
            type="number"
            min="1"
            class="w-24 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <button
            @click="handleScale"
            :disabled="scaling || targetServings === recipeStore.currentRecipe.servings"
            class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Scale
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useRecipeStore } from '@/stores/recipe'
import { useAuthStore } from '@/stores/auth'
import RecipeDetail from '@/components/Recipe/RecipeDetail.vue'

const route = useRoute()
const router = useRouter()
const recipeStore = useRecipeStore()
const authStore = useAuthStore()

const recipeId = computed(() => Number(route.params.id))
const targetServings = ref(null)
const scaling = ref(false)

// Edit/Delete functionality removed - only admins can manage recipes

onMounted(async () => {
  targetServings.value = recipeStore.currentRecipe?.servings || 4
  await recipeStore.fetchRecipe(recipeId.value)
  if (recipeStore.currentRecipe?.servings) {
    targetServings.value = recipeStore.currentRecipe.servings
  }
})

const handleFavoriteToggle = async (recipeId) => {
  await recipeStore.toggleFavorite(recipeId)
}

const handleScale = async () => {
  if (!targetServings.value || targetServings.value < 1) return
  
  try {
    scaling.value = true
    const scaledRecipe = await recipeStore.scaleRecipe(recipeId.value, targetServings.value)
    // Update current recipe with scaled data
    recipeStore.currentRecipe = scaledRecipe
  } catch (error) {
    console.error('Failed to scale recipe:', error)
  } finally {
    scaling.value = false
  }
}
</script>
