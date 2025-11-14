<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Create Recipe</h1>
        <p class="text-gray-600">Add a new recipe to your collection</p>
      </div>
      <button
        @click="router.back()"
        class="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
      >
        Cancel
      </button>
    </div>

    <RecipeForm
      :loading="recipeStore.isLoading"
      @submit="handleSubmit"
      @cancel="router.back()"
    />

    <div v-if="recipeStore.error" class="bg-red-50 border border-red-200 rounded-lg p-4">
      <p class="text-red-800">{{ recipeStore.error }}</p>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useRecipeStore } from '@/stores/recipe'
import RecipeForm from '@/components/Recipe/RecipeForm.vue'

const router = useRouter()
const recipeStore = useRecipeStore()

const handleSubmit = async (formData) => {
  try {
    await recipeStore.createRecipe(formData)
    router.push(`/recipes/${recipeStore.currentRecipe.id}`)
  } catch (error) {
    console.error('Failed to create recipe:', error)
  }
}
</script>
