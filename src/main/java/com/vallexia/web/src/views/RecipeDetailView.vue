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
        @favorite-toggled="handleFavoriteToggle"
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
import { reactive, onMounted, computed } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute, useRouter } from "vue-router";
import { useRecipeStore } from "@/stores/recipe";
import RecipeDetail from "@/components/Recipe/RecipeDetail.vue";
import Toast from "@/components/common/Toast.vue";

const { t } = useI18n();

const route = useRoute();
const router = useRouter();
const recipeStore = useRecipeStore();

const recipeId = computed(() => Number(route.params.id));

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
  await recipeStore.fetchRecipe(recipeId.value);
});

const handleFavoriteToggle = async (recipeId) => {
  await recipeStore.toggleFavorite(recipeId);
};
</script>
