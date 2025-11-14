import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { watch } from 'vue'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'

// Import global styles
import './assets/css/main.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)

// Watch for auth state changes and redirect if on protected route when auth is lost
// This handles the case where auth is cleared while user is already on a protected route
// Must be set up after Pinia is initialized
const authStore = useAuthStore()
watch(
  () => !!(authStore.accessToken && authStore.user),
  (isAuthenticated, wasAuthenticated) => {
    // Only redirect if auth was lost (changed from true to false), not on initial load
    if (wasAuthenticated && !isAuthenticated && router.currentRoute.value.meta?.requiresAuth) {
      // Auth was lost while on a protected route - redirect to homepage
      router.replace('/')
    }
  }
)

// Initialize auth state from localStorage and validate with backend
authStore.initializeAuth().then(() => {
  app.mount('#app')
}).catch((err) => {
  console.error('Failed to initialize auth:', err)
  app.mount('#app')
})
