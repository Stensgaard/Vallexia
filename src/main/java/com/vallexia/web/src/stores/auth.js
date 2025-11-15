import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService } from '@/services/authService'
import { getErrorMessage } from '@/utils/errorUtils'
import { SUBSCRIPTION_STATUS } from '@/utils/constants'

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref(null)
  const accessToken = ref(localStorage.getItem('accessToken'))
  const refreshToken = ref(localStorage.getItem('refreshToken'))
  const isLoading = ref(false)
  const error = ref(null)
  const isInitialized = ref(false)

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
        subscriptionStatus: response.subscriptionStatus,
        householdSize: response.householdSize || 1
      }

      return response
    } catch (err) {
      error.value = getErrorMessage(err)
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
        subscriptionStatus: response.subscriptionStatus,
        householdSize: response.householdSize || 1
      }

      return response
    } catch (err) {
      error.value = getErrorMessage(err)
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
      // Logout errors are non-critical, silently fail
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

  // Track initialization promise to avoid multiple concurrent calls
  let initializationPromise = null

  const initializeAuth = async () => {
    // If already initialized, return immediately
    if (isInitialized.value) {
      return
    }

    // If initialization is in progress, wait for it
    if (initializationPromise) {
      return initializationPromise
    }

    // Start initialization
    initializationPromise = (async () => {
      try {
        // Check if we have tokens in localStorage
        const storedAccessToken = localStorage.getItem('accessToken')
        const storedRefreshToken = localStorage.getItem('refreshToken')
        
        if (!storedAccessToken || !storedRefreshToken) {
          // No tokens, ensure clean state
          clearAuthData()
          isInitialized.value = true
          return
        }
        
        // Set tokens in store
        accessToken.value = storedAccessToken
        refreshToken.value = storedRefreshToken
        
        // Validate token by fetching user profile from backend
        try {
          const { userService } = await import('@/services/userService')
          const profile = await userService.getProfile()
          
          // Token is valid, update user state with actual profile data
          user.value = {
            id: profile.id,
            username: profile.username,
            email: profile.email,
            subscriptionStatus: profile.subscriptionStatus || SUBSCRIPTION_STATUS.FREE,
            householdSize: profile.householdSize || 1
          }
        } catch (err) {
          // Token is invalid (401) or user doesn't exist (404)
          // Clear auth data - router guard will handle navigation
          clearAuthData()
        }
      } finally {
        // Mark as initialized regardless of success or failure
        isInitialized.value = true
        initializationPromise = null
      }
    })()

    return initializationPromise
  }

  return {
    // State
    user,
    accessToken,
    refreshToken,
    isLoading,
    error,
    isInitialized,
    
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
