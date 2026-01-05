import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { recipeService } from "@/services/recipeService";
import { getErrorMessage } from "@/utils/errorUtils";

export const useRecipeStore = defineStore("recipe", () => {
  // State
  const recipes = ref([]);
  const currentRecipe = ref(null);
  const favorites = ref([]);
  const isLoading = ref(false);
  const error = ref(null);
  const pagination = ref({
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
  });

  // Getters
  const filteredRecipes = computed(() => {
    return recipes.value;
  });

  const recipeById = computed(() => {
    return (id) => recipes.value.find((r) => r.spoonacularId === id);
  });

  const isFavorite = computed(() => {
    return (id) => favorites.value.some((f) => f.spoonacularId === id);
  });

  // Actions
  const fetchRecipes = async (page = 0, size = 20, searchParams = {}) => {
    try {
      isLoading.value = true;
      error.value = null;

      const response = await recipeService.searchRecipes(searchParams, page, size);

      recipes.value = response.content || [];
      pagination.value = {
        page: response.number || page,
        size: response.size || size,
        totalElements: response.totalElements || 0,
        totalPages: response.totalPages || 0,
      };

      return response;
    } catch (err) {
      error.value = getErrorMessage(err);
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  // Fetch all recipes in a single API call
  const fetchAllRecipes = async (searchParams = {}) => {
    try {
      isLoading.value = true;
      error.value = null;

      // Fetch all results with a large page size (1000 should cover most cases)
      const response = await recipeService.searchRecipes(searchParams, 0, 1000);

      recipes.value = response.content || [];
      
      // Update pagination to reflect all results are loaded
      pagination.value = {
        page: 0,
        size: response.content?.length || 0,
        totalElements: response.totalElements || response.content?.length || 0,
        totalPages: 1, // All results on one "page"
      };

      return response;
    } catch (err) {
      error.value = getErrorMessage(err);
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const fetchRecipe = async (id) => {
    try {
      isLoading.value = true;
      error.value = null;

      const recipe = await recipeService.getRecipeById(id);
      currentRecipe.value = recipe;

      // Update recipes list if not present
      const index = recipes.value.findIndex((r) => r.spoonacularId === id);
      if (index >= 0) {
        recipes.value[index] = recipe;
      } else {
        recipes.value.push(recipe);
      }

      return recipe;
    } catch (err) {
      error.value = getErrorMessage(err);
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const toggleFavorite = async (id) => {
    try {
      isLoading.value = true;
      error.value = null;

      const isFav = isFavorite.value(id);

      if (isFav) {
        await recipeService.removeFavorite(id);
        favorites.value = favorites.value.filter((f) => f.spoonacularId !== id);

        // Update recipe in list
        const recipe = recipes.value.find((r) => r.spoonacularId === id);
        if (recipe) {
          recipe.isFavorite = false;
        }
        if (currentRecipe.value?.spoonacularId === id) {
          currentRecipe.value.isFavorite = false;
        }
      } else {
        await recipeService.addFavorite(id);

        // Add to favorites if we have the recipe
        const recipe =
          recipes.value.find((r) => r.spoonacularId === id) || currentRecipe.value;
        if (recipe && recipe.spoonacularId === id) {
          favorites.value.push({ ...recipe, isFavorite: true });
          recipe.isFavorite = true;
          if (currentRecipe.value?.spoonacularId === id) {
            currentRecipe.value.isFavorite = true;
          }
        }
      }
    } catch (err) {
      error.value = getErrorMessage(err);
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const fetchFavorites = async (page = 0, size = 20) => {
    try {
      isLoading.value = true;
      error.value = null;

      const response = await recipeService.getFavorites(page, size);

      favorites.value = response.content || [];
      pagination.value = {
        page: response.number || page,
        size: response.size || size,
        totalElements: response.totalElements || 0,
        totalPages: response.totalPages || 0,
      };

      return response;
    } catch (err) {
      error.value = getErrorMessage(err);
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const clearError = () => {
    error.value = null;
  };

  const clearCurrentRecipe = () => {
    currentRecipe.value = null;
  };

  const clearRecipes = () => {
    recipes.value = [];
    currentRecipe.value = null;
  };

  return {
    // State
    recipes,
    currentRecipe,
    favorites,
    isLoading,
    error,
    pagination,

    // Getters
    filteredRecipes,
    recipeById,
    isFavorite,

    // Actions
    fetchRecipes,
    fetchAllRecipes,
    fetchRecipe,
    toggleFavorite,
    fetchFavorites,
    clearError,
    clearCurrentRecipe,
    clearRecipes,
  };
});
