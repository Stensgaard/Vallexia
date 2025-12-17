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
