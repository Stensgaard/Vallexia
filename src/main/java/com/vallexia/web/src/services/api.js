import axios from 'axios'
import { getActivePinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'

// Create axios instance
const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Flag to track if refresh is in progress
let isRefreshing = false
// Queue to store failed requests during refresh
let failedQueue = []

// Process queued requests after successful refresh
const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })
  
  failedQueue = []
}

const getAuthStore = () => {
  try {
    const activePinia = getActivePinia()
    if (!activePinia) {
      return null
    }
    return useAuthStore(activePinia)
  } catch (error) {
    // Pinia not yet initialized
    return null
  }
}

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const authStore = getAuthStore()
    if (authStore?.accessToken) {
      config.headers.Authorization = `Bearer ${authStore.accessToken}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor to handle token refresh
api.interceptors.response.use(
  (response) => {
    return response
  },
  async (error) => {
    const originalRequest = error.config
    
    // Check if this is a 401 error and not already retried
    if (error.response?.status === 401 && !originalRequest._retry) {
      // Check if this is the refresh request itself (using custom flag or URL check)
      // If so, don't try to refresh - just clear tokens
      const isRefreshRequest = originalRequest._skipAuthRefresh || 
                               originalRequest.url?.includes('/auth/refresh') || 
                               originalRequest.url?.includes('/v1/auth/refresh')
      
      if (isRefreshRequest) {
        // Refresh token itself failed - clear auth data
        // Router guard will handle navigation
        const authStore = getAuthStore()
        authStore?.clearAuthData()
        return Promise.reject(error)
      }
      
      // If refresh is already in progress, queue this request
      if (isRefreshing) {
        originalRequest._retry = true
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            return api(originalRequest)
          })
          .catch((err) => {
            return Promise.reject(err)
          })
      }
      
      originalRequest._retry = true
      isRefreshing = true
      
      try {
        const authStore = getAuthStore()
        if (!authStore) {
          isRefreshing = false
          processQueue(error, null)
          return Promise.reject(error)
        }
        const newToken = await authStore.refreshAccessToken()
        
        // Process queued requests with new token
        processQueue(null, newToken)
        isRefreshing = false
        
        // Retry the original request
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return api(originalRequest)
      } catch (refreshError) {
        // Refresh failed - clear auth data
        // Router guard will handle navigation
        isRefreshing = false
        processQueue(refreshError, null)
        
        const authStore = getAuthStore()
        authStore?.clearAuthData()
        return Promise.reject(refreshError)
      }
    }
    
    return Promise.reject(error)
  }
)

export default api
