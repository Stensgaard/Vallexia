<template>
  <form @submit.prevent="handleSubmit" class="space-y-6">
    <!-- Basic Information -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h3 class="text-lg font-semibold mb-4">Basic Information</h3>
      
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">
            Recipe Name <span class="text-red-500">*</span>
          </label>
          <input
            v-model="formData.name"
            type="text"
            required
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Description</label>
          <textarea
            v-model="formData.description"
            rows="3"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          ></textarea>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Category <span class="text-red-500">*</span></label>
            <select
              v-model="formData.category"
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">Select Category</option>
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

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Cuisine Type</label>
            <select
              v-model="formData.cuisineType"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">Select Cuisine</option>
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
        </div>
      </div>
    </div>

    <!-- Timing & Servings -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h3 class="text-lg font-semibold mb-4">Timing & Servings</h3>
      
      <div class="grid grid-cols-3 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Prep Time (min)</label>
          <input
            v-model.number="formData.prepTimeMinutes"
            type="number"
            min="0"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Cook Time (min)</label>
          <input
            v-model.number="formData.cookTimeMinutes"
            type="number"
            min="0"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">
            Servings <span class="text-red-500">*</span>
          </label>
          <input
            v-model.number="formData.servings"
            type="number"
            min="1"
            required
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </div>

      <div class="mt-4">
        <label class="block text-sm font-medium text-gray-700 mb-1">Difficulty Level</label>
        <select
          v-model="formData.difficultyLevel"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="">Select Difficulty</option>
          <option value="EASY">Easy</option>
          <option value="MEDIUM">Medium</option>
          <option value="HARD">Hard</option>
          <option value="EXPERT">Expert</option>
        </select>
      </div>
    </div>

    <!-- Ingredients -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-semibold">Ingredients</h3>
        <button
          type="button"
          @click="addIngredient"
          class="px-3 py-1 text-sm bg-blue-600 text-white rounded-md hover:bg-blue-700"
        >
          Add Ingredient
        </button>
      </div>
      
      <div class="space-y-3">
        <div
          v-for="(ingredient, index) in formData.ingredients"
          :key="index"
          class="grid grid-cols-12 gap-2 items-start"
        >
          <div class="col-span-5">
            <input
              v-model="ingredient.name"
              type="text"
              placeholder="Ingredient name"
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div class="col-span-3">
            <input
              v-model.number="ingredient.quantity"
              type="number"
              step="0.1"
              min="0"
              placeholder="Quantity"
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div class="col-span-3">
            <input
              v-model="ingredient.unit"
              type="text"
              placeholder="Unit (e.g., cup, tsp)"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div class="col-span-1">
            <button
              type="button"
              @click="removeIngredient(index)"
              class="w-full p-2 text-red-600 hover:bg-red-50 rounded-md"
            >
              ×
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Instructions -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h3 class="text-lg font-semibold mb-4">
        Instructions <span class="text-red-500">*</span>
      </h3>
      <textarea
        v-model="formData.instructions"
        rows="10"
        required
        placeholder="Enter step-by-step instructions..."
        class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
      ></textarea>
    </div>

    <!-- Nutritional Info -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h3 class="text-lg font-semibold mb-4">Nutritional Information</h3>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Calories</label>
          <input
            v-model.number="formData.nutritionalInfo.calories"
            type="number"
            step="0.1"
            min="0"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Protein (g)</label>
          <input
            v-model.number="formData.nutritionalInfo.protein"
            type="number"
            step="0.1"
            min="0"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Carbs (g)</label>
          <input
            v-model.number="formData.nutritionalInfo.carbs"
            type="number"
            step="0.1"
            min="0"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Fats (g)</label>
          <input
            v-model.number="formData.nutritionalInfo.fats"
            type="number"
            step="0.1"
            min="0"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </div>
    </div>

    <!-- Image URL -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <label class="block text-sm font-medium text-gray-700 mb-1">Image URL</label>
      <input
        v-model="formData.imageUrl"
        type="url"
        placeholder="https://example.com/image.jpg"
        class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
    </div>

    <!-- Public/Private -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <label class="flex items-center">
        <input
          v-model="formData.isPublic"
          type="checkbox"
          class="mr-2"
        />
        <span class="text-sm font-medium text-gray-700">Make this recipe public</span>
      </label>
    </div>

    <!-- Actions -->
    <div class="flex justify-end gap-4">
      <button
        type="button"
        @click="$emit('cancel')"
        class="px-6 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
      >
        Cancel
      </button>
      <button
        type="submit"
        :disabled="loading"
        class="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {{ loading ? 'Saving...' : (recipeId ? 'Update Recipe' : 'Create Recipe') }}
      </button>
    </div>
  </form>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  recipe: {
    type: Object,
    default: null
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['submit', 'cancel'])

const recipeId = ref(props.recipe?.id || null)

const formData = ref({
  name: props.recipe?.name || '',
  description: props.recipe?.description || '',
  instructions: props.recipe?.instructions || '',
  prepTimeMinutes: props.recipe?.prepTimeMinutes || null,
  cookTimeMinutes: props.recipe?.cookTimeMinutes || null,
  servings: props.recipe?.servings || 1,
  difficultyLevel: props.recipe?.difficultyLevel || '',
  category: props.recipe?.category || '',
  cuisineType: props.recipe?.cuisineType || '',
  imageUrl: props.recipe?.imageUrl || '',
  isPublic: props.recipe?.isPublic !== undefined ? props.recipe.isPublic : true,
  ingredients: props.recipe?.ingredients?.length > 0
    ? props.recipe.ingredients.map(ing => ({
        name: ing.name || '',
        quantity: ing.quantity || 0,
        unit: ing.unit || '',
        displayOrder: ing.displayOrder || 0
      }))
    : [],
  nutritionalInfo: {
    calories: props.recipe?.nutritionalInfo?.calories || null,
    protein: props.recipe?.nutritionalInfo?.protein || null,
    carbs: props.recipe?.nutritionalInfo?.carbs || null,
    fats: props.recipe?.nutritionalInfo?.fats || null
  }
})

watch(() => props.recipe, (newRecipe) => {
  if (newRecipe) {
    recipeId.value = newRecipe.id
    formData.value = {
      name: newRecipe.name || '',
      description: newRecipe.description || '',
      instructions: newRecipe.instructions || '',
      prepTimeMinutes: newRecipe.prepTimeMinutes || null,
      cookTimeMinutes: newRecipe.cookTimeMinutes || null,
      servings: newRecipe.servings || 1,
      difficultyLevel: newRecipe.difficultyLevel || '',
      category: newRecipe.category || '',
      cuisineType: newRecipe.cuisineType || '',
      imageUrl: newRecipe.imageUrl || '',
      isPublic: newRecipe.isPublic !== undefined ? newRecipe.isPublic : true,
      ingredients: newRecipe.ingredients?.length > 0
        ? newRecipe.ingredients.map(ing => ({
            name: ing.name || '',
            quantity: ing.quantity || 0,
            unit: ing.unit || '',
            displayOrder: ing.displayOrder || 0
          }))
        : [],
      nutritionalInfo: {
        calories: newRecipe.nutritionalInfo?.calories || null,
        protein: newRecipe.nutritionalInfo?.protein || null,
        carbs: newRecipe.nutritionalInfo?.carbs || null,
        fats: newRecipe.nutritionalInfo?.fats || null
      }
    }
  }
}, { deep: true })

const addIngredient = () => {
  formData.value.ingredients.push({
    name: '',
    quantity: 0,
    unit: '',
    displayOrder: formData.value.ingredients.length
  })
}

const removeIngredient = (index) => {
  formData.value.ingredients.splice(index, 1)
  // Update display orders
  formData.value.ingredients.forEach((ing, idx) => {
    ing.displayOrder = idx
  })
}

const handleSubmit = () => {
  // Clean up empty ingredients
  const cleanedIngredients = formData.value.ingredients.filter(
    ing => ing.name && ing.name.trim() !== ''
  )

  const submitData = {
    ...formData.value,
    ingredients: cleanedIngredients.length > 0 ? cleanedIngredients : null,
    nutritionalInfo: Object.values(formData.value.nutritionalInfo).some(v => v != null)
      ? formData.value.nutritionalInfo
      : null
  }

  emit('submit', submitData)
}
</script>
