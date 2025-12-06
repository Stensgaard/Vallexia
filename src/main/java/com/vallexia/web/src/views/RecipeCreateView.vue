<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">{{ $t('recipes.create.title') }}</h1>
        <p class="text-gray-600">{{ $t('recipes.create.description') }}</p>
      </div>
      <button
        @click="router.back()"
        class="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
      >
        {{ $t('common.cancel') }}
      </button>
    </div>

    <RecipeForm
      :loading="recipeStore.isLoading"
      @submit="handleSubmit"
      @cancel="router.back()"
    />
  </div>

  <!-- Toast Notifications -->
  <div class="fixed top-4 right-4 z-50">
    <Toast
      :show="toast.show"
      :type="toast.type"
      :title="toast.title"
      :message="toast.message"
      @dismiss="toast.show = false"
    />
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useRecipeStore } from '@/stores/recipe'
import RecipeForm from '@/components/Recipe/RecipeForm.vue'
import Toast from '@/components/common/Toast.vue'
import { getErrorMessage } from '@/utils/errorUtils'

const { t } = useI18n()

const router = useRouter()
const recipeStore = useRecipeStore()

const toast = reactive({
  show: false,
  type: 'success',
  title: '',
  message: ''
})

const showToast = (type, title, message) => {
  toast.type = type
  toast.title = title
  toast.message = message
  toast.show = true
}

const handleSubmit = async (formData) => {
  try {
    await recipeStore.createRecipe(formData)
    showToast('success', t('common.success'), t('recipes.create.success'))
    router.push(`/recipes/${recipeStore.currentRecipe.id}`)
  } catch (error) {
    const errorMessage = getErrorMessage(error)
    showToast('error', t('common.error'), errorMessage)
  }
}
</script>
