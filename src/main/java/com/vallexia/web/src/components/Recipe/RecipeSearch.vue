<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
    <h3 class="text-lg font-semibold mb-4">Search & Filter Recipes</h3>
    
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <!-- Search Query -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Search</label>
        <input
          v-model="localCriteria.query"
          type="text"
          placeholder="Search recipes..."
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          @input="updateCriteria"
        />
      </div>

      <!-- Category -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Category</label>
        <select
          v-model="localCriteria.category"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          @change="updateCriteria"
        >
          <option value="">All Categories</option>
          <option value="BREAKFAST">Breakfast</option>
          <option value="LUNCH">Lunch</option>
          <option value="DINNER">Dinner</option>
          <option value="SNACK">Snack</option>
          <option value="DESSERT">Dessert</option>
          <option value="APPETIZER">Appetizer</option>
          <option value="BEVERAGE">Beverage</option>
          <option value="OTHER">Other</option>
        </select>
      </div>

      <!-- Cuisine Type -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Cuisine</label>
        <select
          v-model="localCriteria.cuisineType"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          @change="updateCriteria"
        >
          <option value="">All Cuisines</option>
          <option value="ITALIAN">Italian</option>
          <option value="MEXICAN">Mexican</option>
          <option value="CHINESE">Chinese</option>
          <option value="JAPANESE">Japanese</option>
          <option value="FRENCH">French</option>
          <option value="INDIAN">Indian</option>
          <option value="AMERICAN">American</option>
          <option value="THAI">Thai</option>
          <option value="MEDITERRANEAN">Mediterranean</option>
          <option value="OTHER">Other</option>
        </select>
      </div>

      <!-- Difficulty Level -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Difficulty</label>
        <select
          v-model="localCriteria.difficultyLevel"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          @change="updateCriteria"
        >
          <option value="">All Levels</option>
          <option value="EASY">Easy</option>
          <option value="MEDIUM">Medium</option>
          <option value="HARD">Hard</option>
          <option value="EXPERT">Expert</option>
        </select>
      </div>

      <!-- Prep Time Range -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Max Prep Time (minutes)</label>
        <input
          v-model.number="localCriteria.maxPrepTime"
          type="number"
          placeholder="e.g. 30"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          @input="updateCriteria"
        />
      </div>

      <!-- Calories Range -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Max Calories</label>
        <input
          v-model.number="localCriteria.maxCalories"
          type="number"
          placeholder="e.g. 500"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          @input="updateCriteria"
        />
      </div>
    </div>

    <div class="flex items-center gap-2 mt-4">
      <button
        @click="applySearch"
        class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
      >
        Search
      </button>
      <button
        @click="clearFilters"
        class="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 transition-colors"
      >
        Clear Filters
      </button>
      <span
        v-if="totalResults > 0"
        class="text-sm text-gray-600 ml-2"
      >
        {{ totalResults }} {{ totalResults === 1 ? 'recipe' : 'recipes' }} found
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  criteria: {
    type: Object,
    default: () => ({
      category: '',
      cuisineType: '',
      difficultyLevel: ''
    })
  },
  totalResults: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['search', 'criteria-changed'])

const localCriteria = ref({
  category: '',
  cuisineType: '',
  difficultyLevel: '',
  ...props.criteria
})

watch(() => props.criteria, (newVal) => {
  localCriteria.value = {
    category: '',
    cuisineType: '',
    difficultyLevel: '',
    ...newVal
  }
}, { deep: true })

const updateCriteria = () => {
  emit('criteria-changed', { ...localCriteria.value })
}

const applySearch = () => {
  emit('search', { ...localCriteria.value })
}

const clearFilters = () => {
  localCriteria.value = {
    category: '',
    cuisineType: '',
    difficultyLevel: ''
  }
  emit('criteria-changed', { ...localCriteria.value })
  emit('search', { ...localCriteria.value })
}
</script>
