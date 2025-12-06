<template>
  <div class="space-y-6">
    <div v-if="loadingRecipe" class="text-center py-12">
      <p class="text-gray-500">{{ $t("recipes.detail.loading") }}</p>
    </div>

    <div
      v-else-if="recipeStore.error && !recipeStore.currentRecipe"
      class="bg-red-50 border border-red-200 rounded-lg p-4"
    >
      <p class="text-red-800">{{ recipeStore.error }}</p>
      <button
        class="mt-2 text-sm text-red-600 hover:text-red-700"
        @click="router.back()"
      >
        {{ $t("recipes.view.goBack") }}
      </button>
    </div>

    <div v-else-if="recipeStore.currentRecipe">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">
            {{ $t("recipes.edit.title") }}
          </h1>
          <p class="text-gray-600">{{ $t("recipes.edit.description") }}</p>
        </div>
        <button
          class="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
          @click="router.back()"
        >
          {{ $t("common.cancel") }}
        </button>
      </div>

      <RecipeForm
        :recipe="recipeStore.currentRecipe"
        :loading="recipeStore.isLoading"
        @submit="handleSubmit"
        @cancel="router.back()"
      />
    </div>
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
import { ref, reactive, onMounted, computed } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute, useRouter } from "vue-router";
import { useRecipeStore } from "@/stores/recipe";
import RecipeForm from "@/components/Recipe/RecipeForm.vue";
import Toast from "@/components/common/Toast.vue";
import { getErrorMessage } from "@/utils/errorUtils";

const { t } = useI18n();

const route = useRoute();
const router = useRouter();
const recipeStore = useRecipeStore();

const recipeId = computed(() => Number(route.params.id));
const loadingRecipe = ref(true);

const toast = reactive({
  show: false,
  type: "success",
  title: "",
  message: "",
});

const showToast = (type, title, message) => {
  toast.type = type;
  toast.title = title;
  toast.message = message;
  toast.show = true;
};

onMounted(async () => {
  try {
    await recipeStore.fetchRecipe(recipeId.value);
  } catch (error) {
    const errorMessage = getErrorMessage(error);
    showToast("error", t("common.error"), errorMessage);
  } finally {
    loadingRecipe.value = false;
  }
});

const handleSubmit = async (formData) => {
  try {
    await recipeStore.updateRecipe(recipeId.value, formData);
    showToast("success", t("common.success"), t("recipes.edit.success"));
    router.push(`/recipes/${recipeId.value}`);
  } catch (error) {
    const errorMessage = getErrorMessage(error);
    showToast("error", t("common.error"), errorMessage);
  }
};
</script>
