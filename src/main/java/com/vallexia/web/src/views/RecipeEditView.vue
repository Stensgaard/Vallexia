<template>
  <div class="space-y-6">
    <div v-if="loadingRecipe" class="text-center py-12">
      <p class="text-gray-500">Loading recipe...</p>
    </div>

    <div v-else-if="recipeStore.error && !recipeStore.currentRecipe" class="bg-red-50 border border-red-200 rounded-lg p-4">
      <p class="text-red-800">{{ recipeStore.error }}</p>
      <button
        @click="router.back()"
        class="mt-2 text-sm text-red-600 hover:text-red-700"
      >
        Go Back
      </button>
    </div>

    <div v-else-if="recipeStore.currentRecipe">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Edit Recipe</h1>
          <p class="text-gray-600">Update your recipe details</p>
        </div>
        <button
          @click="router.back()"
          class="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
        >
          Cancel
        </button>
      </div>

      <RecipeForm
        :recipe="recipeStore.currentRecipe"
        :loading="recipeStore.isLoading"
        @submit="handleSubmit"
        @cancel="router.back()"
      />

      <div v-if="recipeStore.error" class="bg-red-50 border border-red-200 rounded-lg p-4">
        <p class="text-red-800">{{ recipeStore.error }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useRecipeStore } from '@/stores/recipe'
import RecipeForm from '@/components/Recipe/RecipeForm.vue'

const route = useRoute()
const router = useRouter()
const recipeStore = useRecipeStore()

const recipeId = computed(() => Number(route.params.id))
const loadingRecipe = ref(true)

onMounted(async () => {
  try {
    await recipeStore.fetchRecipe(recipeId.value)
  } catch (error) {
    console.error('Failed to load recipe:', error)
  } finally {
    loadingRecipe.value = false
  }
})

const handleSubmit = async (formData) => {
  try {
    await recipeStore.updateRecipe(recipeId.value, formData)
    router.push(`/recipes/${recipeId.value}`)
  } catch (error) {
    console.error('Failed to update recipe:', error)
  }
}
</script>
