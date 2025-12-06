import api from "./api";

/**
 * Recipe service for managing recipe operations
 */
export const recipeService = {
  /**
   * Get all public recipes with pagination
   * @param {number} page - Page number (0-indexed)
   * @param {number} size - Page size
   * @param {Object} filters - Optional filters
   * @returns {Promise<Object>} Page of recipes
   */
  async getAllRecipes(page = 0, size = 20, filters = {}) {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      ...filters,
    });
    const response = await api.get(`/v1/recipes?${params}`);
    return response.data;
  },

  /**
   * Get recipe by ID
   * @param {number} id - Recipe ID
   * @returns {Promise<Object>} Recipe DTO
   */
  async getRecipeById(id) {
    const response = await api.get(`/v1/recipes/${id}`);
    return response.data;
  },

  /**
   * Create a new recipe (ADMIN ONLY)
   * @param {Object} recipeData - Recipe creation data
   * @returns {Promise<Object>} Created recipe DTO
   * @throws {Error} If user doesn't have admin role (403 Forbidden)
   */
  async createRecipe(recipeData) {
    try {
      const response = await api.post("/v1/recipes", recipeData);
      return response.data;
    } catch (error) {
      if (error.response?.status === 403) {
        throw new Error("Admin role required to create recipes");
      }
      throw error;
    }
  },

  /**
   * Update an existing recipe (ADMIN ONLY)
   * @param {number} id - Recipe ID
   * @param {Object} recipeData - Recipe update data
   * @returns {Promise<Object>} Updated recipe DTO
   * @throws {Error} If user doesn't have admin role (403 Forbidden)
   */
  async updateRecipe(id, recipeData) {
    try {
      const response = await api.put(`/v1/recipes/${id}`, recipeData);
      return response.data;
    } catch (error) {
      if (error.response?.status === 403) {
        throw new Error("Admin role required to update recipes");
      }
      throw error;
    }
  },

  /**
   * Delete a recipe (ADMIN ONLY)
   * @param {number} id - Recipe ID
   * @returns {Promise<void>}
   * @throws {Error} If user doesn't have admin role (403 Forbidden)
   */
  async deleteRecipe(id) {
    try {
      await api.delete(`/v1/recipes/${id}`);
    } catch (error) {
      if (error.response?.status === 403) {
        throw new Error("Admin role required to delete recipes");
      }
      throw error;
    }
  },

  /**
   * Search recipes with advanced criteria
   * @param {Object} criteria - Search criteria
   * @param {number} page - Page number
   * @param {number} size - Page size
   * @returns {Promise<Object>} Search response with paginated results
   */
  async searchRecipes(criteria, page = 0, size = 20) {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      ...Object.fromEntries(
        Object.entries(criteria).filter(([_, v]) => v != null && v !== ""),
      ),
    });
    const response = await api.get(`/v1/recipes/search?${params}`);
    return response.data;
  },

  /**
   * Scale recipe to different number of servings
   * @param {number} id - Recipe ID
   * @param {number} servings - Target number of servings
   * @returns {Promise<Object>} Scaled recipe DTO
   */
  async scaleRecipe(id, servings) {
    const params = new URLSearchParams({
      servings: servings.toString(),
    });
    const response = await api.get(`/v1/recipes/${id}/scale?${params}`);
    return response.data;
  },

  /**
   * Add recipe to favorites
   * @param {number} id - Recipe ID
   * @returns {Promise<void>}
   */
  async addFavorite(id) {
    await api.post(`/v1/recipes/${id}/favorite`);
  },

  /**
   * Remove recipe from favorites
   * @param {number} id - Recipe ID
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
