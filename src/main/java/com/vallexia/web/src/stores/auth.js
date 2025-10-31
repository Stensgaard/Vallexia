import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService } from '@/services/authService'

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref(null)
  const accessToken = ref(localStorage.getItem('accessToken'))
  const refreshToken = ref(localStorage.getItem('refreshToken'))
  const isLoading = ref(false)
  const error = ref(null)

  // Getters
  const isAuthenticated = computed(() => {
    return !!accessToken.value && !!user.value
  })

  // Actions
  const login = async (credentials) => {
    try {
      isLoading.value = true
      error.value = null

      const response = await authService.login(credentials)
      
      // Store tokens
      accessToken.value = response.accessToken
      refreshToken.value = response.refreshToken
      localStorage.setItem('accessToken', response.accessToken)
      localStorage.setItem('refreshToken', response.refreshToken)
      
      // Store user data
      user.value = {
        id: response.id,
        username: response.username,
        email: response.email,
        householdSize: response.householdSize,
        mealsPerDay: response.mealsPerDay,
        subscriptionStatus: response.subscriptionStatus
      }

      return response
    } catch (err) {
      error.value = err.response?.data?.message || 'Login failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const register = async (userData) => {
    try {
      isLoading.value = true
      error.value = null

      const response = await authService.register(userData)
      
      // Store tokens
      accessToken.value = response.accessToken
      refreshToken.value = response.refreshToken
      localStorage.setItem('accessToken', response.accessToken)
      localStorage.setItem('refreshToken', response.refreshToken)
      
      // Store user data
      user.value = {
        id: response.id,
        username: response.username,
        email: response.email,
        householdSize: response.householdSize,
        mealsPerDay: response.mealsPerDay,
        subscriptionStatus: response.subscriptionStatus
      }

      return response
    } catch (err) {
      error.value = err.response?.data?.message || 'Registration failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const logout = async () => {
    try {
      // Call logout endpoint if authenticated
      if (isAuthenticated.value) {
        await authService.logout()
      }
    } catch (err) {
      console.error('Logout error:', err)
    } finally {
      // Clear local state regardless of API call success
      clearAuthData()
    }
  }

  const refreshAccessToken = async () => {
    try {
      if (!refreshToken.value) {
        throw new Error('No refresh token available')
      }

      const response = await authService.refreshToken(refreshToken.value)
      
      accessToken.value = response.accessToken
      refreshToken.value = response.refreshToken
      localStorage.setItem('accessToken', response.accessToken)
      localStorage.setItem('refreshToken', response.refreshToken)

      return response.accessToken
    } catch (err) {
      // If refresh fails, clear auth data
      clearAuthData()
      throw err
    }
  }

  const clearAuthData = () => {
    user.value = null
    accessToken.value = null
    refreshToken.value = null
    error.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  const clearError = () => {
    error.value = null
  }

  const initializeAuth = () => {
    // Check if we have tokens in localStorage
    const storedAccessToken = localStorage.getItem('accessToken')
    const storedRefreshToken = localStorage.getItem('refreshToken')
    
    if (storedAccessToken && storedRefreshToken) {
      accessToken.value = storedAccessToken
      refreshToken.value = storedRefreshToken
      
      // Try to decode user info from token (basic implementation)
      try {
        const payload = JSON.parse(atob(storedAccessToken.split('.')[1]))
        user.value = {
          id: payload.sub,
          username: payload.sub,
          email: payload.email || '',
          householdSize: payload.householdSize || 1,
          mealsPerDay: payload.mealsPerDay || 3,
          subscriptionStatus: payload.subscriptionStatus || 'FREE'
        }
      } catch (err) {
        console.error('Error decoding token:', err)
        clearAuthData()
      }
    }
  }

  return {
    // State
    user,
    accessToken,
    refreshToken,
    isLoading,
    error,
    
    // Getters
    isAuthenticated,
    
    
    // Actions
    login,
    register,
    logout,
    refreshAccessToken,
    clearAuthData,
    clearError,
    initializeAuth
  }
})
