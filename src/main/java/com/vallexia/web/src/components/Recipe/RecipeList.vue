<template>
  <div>
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
      v-else-if="recipes && recipes.length > 0"
      class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6"
    >
      <RecipeCard
        v-for="recipe in recipes"
        :key="recipe.id"
        :recipe="recipe"
        @recipe-clicked="handleRecipeClick"
        @favorite-toggled="handleFavoriteToggle"
      />
    </div>
    
    <div
      v-else
      class="text-center py-12"
    >
      <p class="text-gray-500 text-lg">{{ $t('recipes.list.noRecipes') }}</p>
      <p class="text-gray-400 text-sm mt-2">
        {{ $t('recipes.list.tryAdjusting') }}
      </p>
    </div>
    
    <div
      v-if="showPagination && totalPages > 1"
      class="flex items-center justify-center gap-2 mt-8"
    >
      <button
        @click="$emit('page-change', currentPage - 1)"
        :disabled="currentPage === 0"
        class="px-4 py-2 border border-gray-300 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
      >
        {{ $t('recipes.list.previous') }}
      </button>
      <span class="px-4 py-2 text-sm text-gray-700">
        {{ $t('recipes.list.page', { current: currentPage + 1, total: totalPages }) }}
      </span>
      <button
        @click="$emit('page-change', currentPage + 1)"
        :disabled="currentPage >= totalPages - 1"
        class="px-4 py-2 border border-gray-300 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
      >
        {{ $t('recipes.list.next') }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import RecipeCard from './RecipeCard.vue'

const { t } = useI18n()

const props = defineProps({
  recipes: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  showPagination: {
    type: Boolean,
    default: true
  },
  currentPage: {
    type: Number,
    default: 0
  },
  totalPages: {
    type: Number,
    default: 1
  }
})

const emit = defineEmits(['recipe-clicked', 'favorite-toggled', 'page-change'])

const handleRecipeClick = (recipe) => {
  emit('recipe-clicked', recipe)
}

const handleFavoriteToggle = (recipeId) => {
  emit('favorite-toggled', recipeId)
}
</script>
