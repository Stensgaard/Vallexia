<template>
  <form @submit.prevent="handleSubmit" class="space-y-6">
    <!-- Basic Information -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h3 class="text-lg font-semibold mb-4">{{ $t('recipes.form.basicInfo') }}</h3>
      
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">
            {{ $t('recipes.form.name') }} <span class="text-red-500">*</span>
          </label>
          <input
            v-model="formData.name"
            type="text"
            required
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">{{ $t('recipes.form.description') }}</label>
          <textarea
            v-model="formData.description"
            rows="3"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          ></textarea>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">{{ $t('recipes.form.category') }} <span class="text-red-500">*</span></label>
            <select
              v-model="formData.category"
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">{{ $t('recipes.form.categoryPlaceholder') }}</option>
              <option
                v-for="category in Object.values(RECIPE_CATEGORIES)"
                :key="category"
                :value="category"
              >
                {{ $t(`recipes.categories.${category}`) }}
              </option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">{{ $t('recipes.form.cuisineType') }}</label>
            <select
              v-model="formData.cuisineType"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">{{ $t('recipes.form.cuisineTypePlaceholder') }}</option>
              <option
                v-for="cuisineType in Object.values(CUISINE_TYPES)"
                :key="cuisineType"
                :value="cuisineType"
              >
                {{ $t(`constants.cuisineTypes.${cuisineType}`) }}
              </option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <!-- Timing & Servings -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h3 class="text-lg font-semibold mb-4">{{ $t('recipes.form.timingServings') }}</h3>
      
      <div class="grid grid-cols-3 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">{{ $t('recipes.form.prepTime') }}</label>
          <input
            v-model.number="formData.prepTimeMinutes"
            type="number"
            min="0"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">{{ $t('recipes.form.cookTime') }}</label>
          <input
            v-model.number="formData.cookTimeMinutes"
            type="number"
            min="0"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">
            {{ $t('recipes.form.servings') }} <span class="text-red-500">*</span>
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
        <label class="block text-sm font-medium text-gray-700 mb-1">{{ $t('recipes.form.difficultyLevel') }}</label>
        <select
          v-model="formData.difficultyLevel"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="">{{ $t('recipes.form.difficultyPlaceholder') }}</option>
          <option
            v-for="difficulty in Object.values(DIFFICULTY_LEVELS)"
            :key="difficulty"
            :value="difficulty"
          >
            {{ $t(`recipes.difficulty.${difficulty}`) }}
          </option>
        </select>
      </div>
    </div>

    <!-- Ingredients -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-semibold">{{ $t('recipes.form.ingredients') }}</h3>
        <button
          type="button"
          @click="addIngredient"
          class="px-3 py-1 text-sm bg-blue-600 text-white rounded-md hover:bg-blue-700"
        >
          {{ $t('recipes.form.addIngredient') }}
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
              :placeholder="$t('recipes.form.ingredientNamePlaceholder')"
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
              :placeholder="$t('recipes.form.quantity')"
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div class="col-span-3">
            <input
              v-model="ingredient.unit"
              type="text"
              :placeholder="$t('recipes.form.unitPlaceholder')"
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
        {{ $t('recipes.form.instructions') }} <span class="text-red-500">*</span>
      </h3>
      <textarea
        v-model="formData.instructions"
        rows="10"
        required
        :placeholder="$t('recipes.form.instructionsPlaceholder')"
        class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
      ></textarea>
    </div>

    <!-- Nutritional Info -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h3 class="text-lg font-semibold mb-4">{{ $t('recipes.form.nutritionalInfo') }}</h3>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">{{ $t('recipes.form.calories') }}</label>
          <input
            v-model.number="formData.nutritionalInfo.calories"
            type="number"
            step="0.1"
            min="0"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">{{ $t('recipes.form.protein') }} ({{ nutritionalUnit }})</label>
          <input
            v-model.number="formData.nutritionalInfo.protein"
            type="number"
            step="0.1"
            min="0"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">{{ $t('recipes.form.carbs') }} ({{ nutritionalUnit }})</label>
          <input
            v-model.number="formData.nutritionalInfo.carbs"
            type="number"
            step="0.1"
            min="0"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">{{ $t('recipes.form.fats') }} ({{ nutritionalUnit }})</label>
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
      <label class="block text-sm font-medium text-gray-700 mb-1">{{ $t('recipes.form.imageUrl') }}</label>
      <input
        v-model="formData.imageUrl"
        type="url"
        :placeholder="$t('recipes.form.imageUrlPlaceholder')"
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
        <span class="text-sm font-medium text-gray-700">{{ $t('recipes.form.makePublic') }}</span>
      </label>
    </div>

    <!-- Actions -->
    <div class="flex justify-end gap-4">
      <button
        type="button"
        @click="$emit('cancel')"
        class="px-6 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
      >
        {{ $t('common.cancel') }}
      </button>
      <button
        type="submit"
        :disabled="loading"
        class="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {{ loading ? $t('recipes.form.saving') : (recipeId ? $t('recipes.form.updateRecipe') : $t('recipes.form.createRecipe')) }}
      </button>
    </div>
  </form>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSettingsStore } from '@/stores/settings'
import { MEAL_TYPES, CUISINE_TYPES, RECIPE_CATEGORIES, DIFFICULTY_LEVELS } from '@/utils/constants'

const { t } = useI18n()
const settingsStore = useSettingsStore()

const nutritionalUnit = computed(() => {
  return settingsStore.measurementSystem === 'IMPERIAL' ? 'oz' : 'g'
})

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
