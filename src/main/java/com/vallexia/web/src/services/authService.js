import api from './api'

export const authService = {
  /**
   * Login user with credentials
   * @param {Object} credentials - Login credentials
   * @param {string} credentials.usernameOrEmail - Username or email
   * @param {string} credentials.password - Password
   * @returns {Promise<Object>} JWT response with tokens and user info
   */
  async login(credentials) {
    const response = await api.post('/v1/auth/login', credentials)
    return response.data
  },

  /**
   * Register new user
   * @param {Object} userData - User registration data
   * @param {string} userData.username - Username
   * @param {string} userData.email - Email
   * @param {string} userData.country - Country code (ISO 3166-1 alpha-2)
   * @param {string} userData.password - Password
   * @param {string} userData.confirmPassword - Password confirmation
   * @returns {Promise<Object>} JWT response with tokens and user info
   */
  async register(userData) {
    const response = await api.post('/v1/auth/register', userData)
    return response.data
  },

  /**
   * Refresh access token
   * @param {string} refreshToken - Refresh token
   * @returns {Promise<Object>} New JWT response
   */
  async refreshToken(refreshToken) {
    // Mark this request to skip refresh retry in interceptor
    const response = await api.post('/v1/auth/refresh', { refreshToken }, {
      _skipAuthRefresh: true
    })
    return response.data
  },

  /**
   * Logout user
   * @returns {Promise<Object>} Logout response
   */
  async logout() {
    const response = await api.post('/v1/auth/logout')
    return response.data
  }
}
