import api from './api'

export const userService = {
  /**
   * Get current user profile
   * @returns {Promise<Object>} User profile data
   */
  async getProfile() {
    const response = await api.get('/v1/users/profile')
    return response.data
  },

  /**
   * Update current user profile
   * @param {Object} profileData - Updated profile data
   * @param {string} profileData.email - Email
   * @param {number} profileData.householdSize - Household size
   * @param {Array} profileData.mealTypes - Meal types
   * @returns {Promise<Object>} Updated user profile
   */
  async updateProfile(profileData) {
    const response = await api.put('/v1/users/profile', profileData)
    return response.data
  },

  /**
   * Get current user's dietary preferences
   * @returns {Promise<Object>} Dietary preferences data
   */
  async getDietaryPreferences() {
    const response = await api.get('/v1/users/dietary-preferences')
    return response.data
  },

  /**
   * Update current user's dietary preferences
   * @param {Object} preferencesData - Updated preferences data
   * @param {Array} preferencesData.restrictions - Dietary restrictions
   * @param {Array} preferencesData.allergies - Allergies
   * @param {Array} preferencesData.preferredCuisines - Preferred cuisines
   * @param {Array} preferencesData.dislikedIngredients - Disliked ingredients
   * @param {string} preferencesData.servingSizePreference - Serving size preference
   * @returns {Promise<Object>} Updated dietary preferences
   */
  async updateDietaryPreferences(preferencesData) {
    const response = await api.put('/v1/users/dietary-preferences', preferencesData)
    return response.data
  },

  /**
   * Get current user's nutritional goals
   * @returns {Promise<Object>} Nutritional goals data
   */
  async getNutritionalGoals() {
    const response = await api.get('/v1/users/nutritional-goals')
    return response.data
  },

  /**
   * Update current user's nutritional goals
   * @param {Object} goalsData - Updated goals data
   * @param {number} goalsData.dailyCalories - Daily calories target
   * @param {number} goalsData.dailyProtein - Daily protein target (grams)
   * @param {number} goalsData.dailyCarbs - Daily carbs target (grams)
   * @param {number} goalsData.dailyFats - Daily fats target (grams)
   * @param {number} goalsData.dailyFiber - Daily fiber target (grams)
   * @param {number} goalsData.dailySodium - Daily sodium target (mg)
   * @param {number} goalsData.dailySugar - Daily sugar target (grams)
   * @param {string} goalsData.goalType - Goal type
   * @returns {Promise<Object>} Updated nutritional goals
   */
  async updateNutritionalGoals(goalsData) {
    const response = await api.put('/v1/users/nutritional-goals', goalsData)
    return response.data
  }
}
