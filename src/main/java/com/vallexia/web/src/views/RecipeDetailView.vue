<template>
  <div class="space-y-6">
    <div v-if="recipeStore.isLoading" class="text-center py-12">
      <p class="text-gray-500">{{ $t("recipes.detail.loading") }}</p>
    </div>

    <div
      v-else-if="recipeStore.error"
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
      <!-- Back button and actions -->
      <div class="flex items-center justify-between mb-4">
        <button
          class="flex items-center text-gray-600 hover:text-gray-900"
          @click="router.back()"
        >
          <svg
            class="w-5 h-5 mr-2"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M15 19l-7-7 7-7"
            />
          </svg>
          {{ $t("recipes.view.backToRecipes") }}
        </button>
      </div>

      <!-- Recipe detail -->
      <RecipeDetail
        :recipe="recipeStore.currentRecipe"
        :target-servings="targetServings"
        :scaling="scaling"
        @favorite-toggled="handleFavoriteToggle"
        @scale-recipe="handleScale"
        @update-target-servings="handleUpdateTargetServings"
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
import { useAuthStore } from "@/stores/auth";
import RecipeDetail from "@/components/Recipe/RecipeDetail.vue";
import Toast from "@/components/common/Toast.vue";
import { getErrorMessage } from "@/utils/errorUtils";

const { t } = useI18n();

const route = useRoute();
const router = useRouter();
const recipeStore = useRecipeStore();

const recipeId = computed(() => Number(route.params.id));
const targetServings = ref(null);
const scaling = ref(false);

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

// Edit/Delete functionality removed - only admins can manage recipes

onMounted(async () => {
  targetServings.value = recipeStore.currentRecipe?.servings || 4;
  await recipeStore.fetchRecipe(recipeId.value);
  if (recipeStore.currentRecipe?.servings) {
    targetServings.value = recipeStore.currentRecipe.servings;
  }
});

const handleFavoriteToggle = async (recipeId) => {
  await recipeStore.toggleFavorite(recipeId);
};

const handleUpdateTargetServings = (value) => {
  targetServings.value = value;
};

const handleScale = async (newServings) => {
  if (!newServings || newServings < 1) return;

  try {
    scaling.value = true;
    const scaledRecipe = await recipeStore.scaleRecipe(
      recipeId.value,
      newServings,
    );
    // Update current recipe with scaled data
    recipeStore.currentRecipe = scaledRecipe;
    showToast("success", t("common.success"), t("recipes.detail.scaling"));
  } catch (error) {
    const errorMessage = getErrorMessage(error);
    showToast("error", t("common.error"), errorMessage);
  } finally {
    scaling.value = false;
  }
};
</script>
