import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Landing',
      component: () => import('@/views/LandingView.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: {
        requiresGuest: true,
        title: 'Sign in to your account',
        subtitle: 'Welcome back! Please sign in to continue.'
      }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/auth/RegisterView.vue'),
      meta: {
        requiresGuest: true,
        title: 'Create your account',
        subtitle: 'Join Vallexia and start planning your meals today!'
      }
    },
    {
      path: '/home',
      name: 'Home',
      component: () => import('@/views/HomeView.vue'),
      meta: { requiresAuth: true, layout: 'main' }
    },
    {
      path: '/dashboard',
      redirect: '/home'
    },
    {
      path: '/recipes',
      name: 'Recipes',
      component: () => import('@/views/RecipesView.vue'),
      meta: { requiresAuth: true, layout: 'main' }
    },
    {
      path: '/recipes/:id',
      name: 'RecipeDetail',
      component: () => import('@/views/RecipeDetailView.vue'),
      meta: { requiresAuth: true, layout: 'main' }
    },
    {
      path: '/meal-plans',
      name: 'MealPlans',
      component: () => import('@/views/MealPlansView.vue'),
      meta: { requiresAuth: true, layout: 'main' }
    },
    {
      path: '/grocery-lists',
      name: 'GroceryLists',
      component: () => import('@/views/GroceryListsView.vue'),
      meta: { requiresAuth: true, layout: 'main' }
    },
    {
      path: '/nutrition',
      name: 'Nutrition',
      component: () => import('@/views/NutritionView.vue'),
      meta: { requiresAuth: true, layout: 'main' }
    },
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('@/views/ProfileView.vue'),
      meta: { requiresAuth: true, layout: 'main' }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFoundView.vue')
    }
  ]
})

// Navigation guards
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  // Check auth state directly from raw values to avoid computed property timing issues
  const hasAuth = !!(authStore.accessToken && authStore.user)
  
  // If trying to access a protected route without auth, redirect to homepage
  // This handles token expiration scenarios - user goes to homepage instead of being forced to login
  if (to.meta.requiresAuth && !hasAuth) {
    next('/')
    return
  }
  
  // If authenticated user tries to access guest route, redirect to home
  if (to.meta.requiresGuest && hasAuth) {
    next('/home')
    return
  }
  
  // Allow navigation
  next()
})

export default router
