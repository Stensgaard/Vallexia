import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { watch } from 'vue'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'
import { useSettingsStore } from './stores/settings'

// Import global styles
import './assets/css/main.css'

// Import i18n configuration
import { i18n, supportedLocales } from './i18n'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(i18n)

// Watch for auth state changes and redirect if on protected route when auth is lost
// This handles the case where auth is cleared while user is already on a protected route
// Must be set up after Pinia is initialized
const authStore = useAuthStore()
const settingsStore = useSettingsStore()

// Function to load settings and update locale
const loadUserSettingsAndUpdateLocale = async () => {
  try {
    await settingsStore.loadSettings()
    // Set locale from user settings, overriding any temporary locale changes
    if (settingsStore.settings?.language) {
      const userLanguage = settingsStore.settings.language
      // Validate against dynamically discovered supported locales
      if (supportedLocales.includes(userLanguage)) {
        i18n.global.locale.value = userLanguage
      }
    }
  } catch (error) {
    // Silently fail - settings will be loaded when user visits profile page
  }
}

// Watch for authentication state changes
watch(
  () => !!(authStore.accessToken && authStore.user),
  async (isAuthenticated, wasAuthenticated) => {
    // If user just logged in (changed from false to true), load their settings
    if (!wasAuthenticated && isAuthenticated) {
      await loadUserSettingsAndUpdateLocale()
    }
    
    // Only redirect if auth was lost (changed from true to false), not on initial load
    if (wasAuthenticated && !isAuthenticated && router.currentRoute.value.meta?.requiresAuth) {
      // Auth was lost while on a protected route - redirect to homepage
      router.replace('/')
    }
  }
)

// Watch for language changes in settings store
watch(
  () => settingsStore.settings?.language,
  (newLanguage) => {
    // Validate against dynamically discovered supported locales
    if (newLanguage && supportedLocales.includes(newLanguage)) {
      i18n.global.locale.value = newLanguage
    }
  }
)

// Watch for locale changes and update document title and html lang attribute
watch(
  () => i18n.global.locale.value,
  (newLocale) => {
    // Update document title
    document.title = i18n.global.t('common.appTitle')
    // Update html lang attribute
    document.documentElement.lang = newLocale
  },
  { immediate: true }
)

// Initialize auth state from localStorage and validate with backend
authStore.initializeAuth().then(() => {
  // Load user settings if authenticated
  if (authStore.isAuthenticated) {
    loadUserSettingsAndUpdateLocale()
  }
  
  app.mount('#app')
}).catch(() => {
  app.mount('#app')
})
