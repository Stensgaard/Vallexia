import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'

// Import global styles
import './assets/css/main.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)

// Initialize auth state from localStorage and validate with backend
const authStore = useAuthStore()
authStore.initializeAuth().then(() => {
  app.mount('#app')
}).catch((err) => {
  console.error('Failed to initialize auth:', err)
  app.mount('#app')
})
