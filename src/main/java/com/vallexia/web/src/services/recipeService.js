import api from "./api";

/**
 * Recipe service for managing recipe operations with Spoonacular API
 */
export const recipeService = {
  /**
   * Search recipes with filters
   * @param {Object} searchParams - Search parameters
   * @param {string} searchParams.query - Search query
   * @param {string[]} searchParams.includeIngredients - Ingredients to include
   * @param {string[]} searchParams.excludeIngredients - Ingredients to exclude
   * @param {string} searchParams.diet - Diet type
   * @param {string[]} searchParams.intolerances - Intolerances
   * @param {string[]} searchParams.cuisine - Cuisines to include
   * @param {string[]} searchParams.excludeCuisine - Cuisines to exclude
   * @param {number} page - Page number (0-indexed)
   * @param {number} size - Page size
   * @returns {Promise<Object>} Page of recipes
   */
  async searchRecipes(searchParams = {}, page = 0, size = 20) {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    
    if (searchParams.query) {
      params.append("query", searchParams.query);
    }
    if (searchParams.includeIngredients) {
      searchParams.includeIngredients.forEach(ing => params.append("includeIngredients", ing));
    }
    if (searchParams.excludeIngredients) {
      searchParams.excludeIngredients.forEach(ing => params.append("excludeIngredients", ing));
    }
    if (searchParams.diet) {
      params.append("diet", searchParams.diet);
    }
    if (searchParams.intolerances) {
      searchParams.intolerances.forEach(int => params.append("intolerances", int));
    }
    if (searchParams.cuisine) {
      searchParams.cuisine.forEach(c => params.append("cuisine", c));
    }
    if (searchParams.excludeCuisine) {
      searchParams.excludeCuisine.forEach(c => params.append("excludeCuisine", c));
    }
    
    const response = await api.get(`/v1/recipes/search?${params}`);
    return response.data;
  },

  /**
   * Get recipe by Spoonacular ID
   * @param {number} id - Spoonacular Recipe ID
   * @returns {Promise<Object>} Recipe DTO
   */
  async getRecipeById(id) {
    const response = await api.get(`/v1/recipes/${id}`);
    return response.data;
  },

  /**
   * Add recipe to favorites
   * @param {number} id - Spoonacular Recipe ID
   * @returns {Promise<void>}
   */
  async addFavorite(id) {
    await api.post(`/v1/recipes/${id}/favorite`);
  },

  /**
   * Remove recipe from favorites
   * @param {number} id - Spoonacular Recipe ID
   * @returns {Promise<void>}
   */
  async removeFavorite(id) {
    await api.delete(`/v1/recipes/${id}/favorite`);
  },

  /**
   * Get user's favorite recipes
   * @param {number} page - Page number
   * @param {number} size - Page size
   * @returns {Promise<Object>} Page of favorite recipes
   */
  async getFavorites(page = 0, size = 20) {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    const response = await api.get(`/v1/recipes/favorites?${params}`);
    return response.data;
  },
};
