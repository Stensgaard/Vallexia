import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { authService } from "@/services/authService";
import { getErrorMessage } from "@/utils/errorUtils";
import { getJwtRoles } from "@/utils/jwt";
import {
  getSubscriptionStatuses,
  createEnumFromList,
  getDefaultSubscriptionStatusCode,
} from "@/utils/localeConfig";

export const useAuthStore = defineStore("auth", () => {
  const subscriptionStatuses = getSubscriptionStatuses();
  const subscriptionStatusEnum = createEnumFromList(subscriptionStatuses);
  const defaultSubscriptionStatus =
    getDefaultSubscriptionStatusCode() || "FREE";

  const sanitizeSubscriptionStatus = (status) => {
    if (status && subscriptionStatusEnum[status]) {
      return status;
    }
    return defaultSubscriptionStatus;
  };

  // State
  const user = ref(null);
  const accessToken = ref(localStorage.getItem("accessToken"));
  const refreshToken = ref(localStorage.getItem("refreshToken"));
  const isLoading = ref(false);
  const error = ref(null);

  // Getters
  const isAuthenticated = computed(() => {
    return !!accessToken.value && !!user.value;
  });

  const roles = computed(() => {
    return accessToken.value ? getJwtRoles(accessToken.value) : [];
  });

  const isAdmin = computed(() => {
    return roles.value.includes("ROLE_ADMIN");
  });

  // Actions
  const login = async (credentials) => {
    try {
      isLoading.value = true;
      error.value = null;

      const response = await authService.login(credentials);

      // Store tokens
      accessToken.value = response.accessToken;
      refreshToken.value = response.refreshToken;
      localStorage.setItem("accessToken", response.accessToken);
      localStorage.setItem("refreshToken", response.refreshToken);

      // Store user data
      user.value = {
        id: response.id,
        username: response.username,
        email: response.email,
        subscriptionStatus: sanitizeSubscriptionStatus(
          response.subscriptionStatus,
        ),
        householdSize: response.householdSize || 1,
      };

      return response;
    } catch (err) {
      error.value = getErrorMessage(err);
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const register = async (userData) => {
    try {
      isLoading.value = true;
      error.value = null;

      const response = await authService.register(userData);

      // Store tokens
      accessToken.value = response.accessToken;
      refreshToken.value = response.refreshToken;
      localStorage.setItem("accessToken", response.accessToken);
      localStorage.setItem("refreshToken", response.refreshToken);

      // Store user data
      user.value = {
        id: response.id,
        username: response.username,
        email: response.email,
        subscriptionStatus: sanitizeSubscriptionStatus(
          response.subscriptionStatus,
        ),
        householdSize: response.householdSize || 1,
      };

      return response;
    } catch (err) {
      error.value = getErrorMessage(err);
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const logout = async () => {
    // Call logout endpoint if authenticated
    if (isAuthenticated.value) {
      await authService.logout();
    }
    // Clear local state regardless of API call success
    clearAuthData();
  };

  const refreshAccessToken = async () => {
    try {
      if (!refreshToken.value) {
        throw new Error("No refresh token available");
      }

      const response = await authService.refreshToken(refreshToken.value);

      accessToken.value = response.accessToken;
      refreshToken.value = response.refreshToken;
      localStorage.setItem("accessToken", response.accessToken);
      localStorage.setItem("refreshToken", response.refreshToken);

      return response.accessToken;
    } catch (err) {
      // If refresh fails, clear auth data
      clearAuthData();
      throw err;
    }
  };

  const clearAuthData = () => {
    user.value = null;
    accessToken.value = null;
    refreshToken.value = null;
    error.value = null;
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
  };

  const clearError = () => {
    error.value = null;
  };

  const initializeAuth = async () => {
    // Check if we have tokens in localStorage
    const storedAccessToken = localStorage.getItem("accessToken");
    const storedRefreshToken = localStorage.getItem("refreshToken");

    if (!storedAccessToken || !storedRefreshToken) {
      // No tokens, ensure clean state
      clearAuthData();
      return;
    }

    // Set tokens in store
    accessToken.value = storedAccessToken;
    refreshToken.value = storedRefreshToken;

    // Validate token by fetching user profile from backend
    try {
      const { userService } = await import("@/services/userService");
      const profile = await userService.getProfile();

      // Token is valid, update user state with actual profile data
      user.value = {
        id: profile.id,
        username: profile.username,
        email: profile.email,
        subscriptionStatus: sanitizeSubscriptionStatus(
          profile.subscriptionStatus,
        ),
        householdSize: profile.householdSize || 1,
      };
    } catch (error_) {
      // Token is invalid (401) or user doesn't exist (404)
      // Clear auth data - router guard will handle navigation
      clearAuthData();
      throw error_;
    }
  };

  return {
    // State
    user,
    accessToken,
    refreshToken,
    isLoading,
    error,

    // Getters
    isAuthenticated,
    roles,
    isAdmin,

    // Actions
    login,
    register,
    logout,
    refreshAccessToken,
    clearAuthData,
    clearError,
    initializeAuth,
  };
});
