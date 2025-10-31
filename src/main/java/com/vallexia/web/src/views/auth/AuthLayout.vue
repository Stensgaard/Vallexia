<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <!-- Header -->
      <div class="text-center">
        <div class="mx-auto h-12 w-12 flex items-center justify-center rounded-full bg-blue-600">
          <svg class="h-8 w-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
          </svg>
        </div>
        <h2 class="mt-6 text-3xl font-extrabold text-gray-900">
          {{ headerTitle }}
        </h2>
        <p class="mt-2 text-sm text-gray-600">
          {{ headerSubtitle }}
        </p>
      </div>

      <!-- Form Container -->
      <div class="bg-white py-8 px-6 shadow-lg rounded-lg">
        <RouterView />
      </div>

      <!-- Footer Links -->
      <div class="text-center">
        <slot name="footer">
          <p class="text-sm text-gray-600">
            <RouterLink v-if="showLoginLink" to="/auth/login" class="font-medium text-blue-600 hover:text-blue-500">
              Already have an account? Sign in
            </RouterLink>
            <RouterLink v-if="showRegisterLink" to="/auth/register" class="font-medium text-blue-600 hover:text-blue-500">
              Don't have an account? Sign up
            </RouterLink>
          </p>
        </slot>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, RouterView, RouterLink } from 'vue-router'

const route = useRoute()

const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  subtitle: {
    type: String,
    default: ''
  }
})

const headerTitle = computed(() => {
  return props.title || route.meta.title || ''
})

const headerSubtitle = computed(() => {
  return props.subtitle || route.meta.subtitle || ''
})

const showLoginLink = computed(() => {
  return route.name === 'Register'
})

const showRegisterLink = computed(() => {
  return route.name === 'Login'
})
</script>
