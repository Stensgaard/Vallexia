<template>
  <div v-if="recipe" class="max-w-4xl mx-auto">
    <!-- Header -->
    <div class="mb-6">
      <div class="flex items-start justify-between mb-4">
        <div class="flex-1">
          <h1 class="text-3xl font-bold text-gray-900 mb-2">{{ recipe.name }}</h1>
          <div class="flex items-center gap-4 text-sm text-gray-600">
            <span v-if="recipe.creatorUsername">By {{ recipe.creatorUsername }}</span>
            <span v-if="recipe.category">{{ recipe.category }}</span>
            <span v-if="recipe.cuisineType">{{ recipe.cuisineType }}</span>
          </div>
        </div>
        <button
          @click="$emit('favorite-toggled', recipe.id)"
          class="p-2 rounded-full hover:bg-gray-100 transition-colors"
          :class="{ 'text-red-500': recipe.isFavorite, 'text-gray-400': !recipe.isFavorite }"
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
      
      <div v-if="recipe.description" class="text-gray-700 mb-4">
        {{ recipe.description }}
      </div>
    </div>

    <!-- Image -->
    <div v-if="recipe.imageUrl" class="mb-6 rounded-lg overflow-hidden">
      <img :src="recipe.imageUrl" :alt="recipe.name" class="w-full h-96 object-cover" />
    </div>

    <!-- Meta Info -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
      <div v-if="recipe.prepTimeMinutes" class="bg-gray-50 p-4 rounded-lg">
        <div class="text-sm text-gray-600">Prep Time</div>
        <div class="text-xl font-semibold">{{ recipe.prepTimeMinutes }} min</div>
      </div>
      <div v-if="recipe.cookTimeMinutes" class="bg-gray-50 p-4 rounded-lg">
        <div class="text-sm text-gray-600">Cook Time</div>
        <div class="text-xl font-semibold">{{ recipe.cookTimeMinutes }} min</div>
      </div>
      <div v-if="recipe.servings" class="bg-gray-50 p-4 rounded-lg">
        <div class="text-sm text-gray-600">Servings</div>
        <div class="text-xl font-semibold">{{ recipe.servings }}</div>
      </div>
      <div v-if="recipe.difficultyLevel" class="bg-gray-50 p-4 rounded-lg">
        <div class="text-sm text-gray-600">Difficulty</div>
        <div class="text-xl font-semibold">{{ recipe.difficultyLevel }}</div>
      </div>
    </div>

    <!-- Ingredients -->
    <div v-if="recipe.ingredients && recipe.ingredients.length > 0" class="mb-6">
      <h2 class="text-2xl font-semibold mb-4">Ingredients</h2>
      <ul class="space-y-2">
        <li
          v-for="(ingredient, index) in recipe.ingredients"
          :key="index"
          class="flex items-start gap-3 p-3 bg-gray-50 rounded-lg"
        >
          <span class="flex-shrink-0 w-6 h-6 rounded-full bg-blue-500 text-white flex items-center justify-center text-sm font-medium">
            {{ index + 1 }}
          </span>
          <div class="flex-1">
            <span class="font-medium">{{ ingredient.name }}</span>
            <span v-if="ingredient.quantity" class="text-gray-700 ml-2">
              {{ ingredient.quantity }} {{ ingredient.unit || '' }}
            </span>
            <p v-if="ingredient.notes" class="text-sm text-gray-600 mt-1">{{ ingredient.notes }}</p>
          </div>
        </li>
      </ul>
    </div>

    <!-- Instructions -->
    <div v-if="recipe.instructions" class="mb-6">
      <h2 class="text-2xl font-semibold mb-4">Instructions</h2>
      <div class="prose max-w-none">
        <p class="whitespace-pre-line text-gray-700">{{ recipe.instructions }}</p>
      </div>
    </div>

    <!-- Nutritional Info -->
    <div v-if="recipe.nutritionalInfo" class="mb-6">
      <h2 class="text-2xl font-semibold mb-4">Nutritional Information</h2>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div v-if="recipe.nutritionalInfo.calories" class="bg-blue-50 p-4 rounded-lg">
          <div class="text-sm text-blue-600">Calories</div>
          <div class="text-xl font-semibold">{{ Math.round(recipe.nutritionalInfo.calories) }}</div>
        </div>
        <div v-if="recipe.nutritionalInfo.protein" class="bg-green-50 p-4 rounded-lg">
          <div class="text-sm text-green-600">Protein</div>
          <div class="text-xl font-semibold">{{ Math.round(recipe.nutritionalInfo.protein) }}g</div>
        </div>
        <div v-if="recipe.nutritionalInfo.carbs" class="bg-purple-50 p-4 rounded-lg">
          <div class="text-sm text-purple-600">Carbs</div>
          <div class="text-xl font-semibold">{{ Math.round(recipe.nutritionalInfo.carbs) }}g</div>
        </div>
        <div v-if="recipe.nutritionalInfo.fats" class="bg-orange-50 p-4 rounded-lg">
          <div class="text-sm text-orange-600">Fats</div>
          <div class="text-xl font-semibold">{{ Math.round(recipe.nutritionalInfo.fats) }}g</div>
        </div>
      </div>
    </div>

    <!-- Tags -->
    <div v-if="recipe.tags && recipe.tags.length > 0" class="flex flex-wrap gap-2">
      <span
        v-for="tag in recipe.tags"
        :key="tag"
        class="px-3 py-1 text-sm bg-gray-100 text-gray-700 rounded-full"
      >
        {{ tag }}
      </span>
    </div>
  </div>
  
  <div v-else class="text-center py-12">
    <p class="text-gray-500">Loading recipe...</p>
  </div>
</template>

<script setup>
defineProps({
  recipe: {
    type: Object,
    default: null
  }
})

defineEmits(['favorite-toggled'])
</script>
