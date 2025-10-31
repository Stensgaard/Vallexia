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

// Initialize auth state from localStorage before mounting
const authStore = useAuthStore()
authStore.initializeAuth()

app.mount('#app')
