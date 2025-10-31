<template>
  <div class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-md">
      <div class="text-center">
        <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 12h6m-6-4h6m2 5.291A7.962 7.962 0 0112 15c-2.34 0-4.29-1.009-5.824-2.709M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
        </svg>
        <h1 class="mt-6 text-4xl font-bold text-gray-900">404</h1>
        <h2 class="mt-2 text-2xl font-semibold text-gray-700">Page Not Found</h2>
        <p class="mt-2 text-sm text-gray-600">
          Sorry, we couldn't find the page you're looking for.
        </p>
        <div class="mt-6">
          <RouterLink :to="homeRoute" class="btn btn-primary">
            {{ homeButtonText }}
          </RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const homeRoute = computed(() => {
  return authStore.isAuthenticated ? '/home' : '/'
})

const homeButtonText = computed(() => {
  return authStore.isAuthenticated ? 'Go to Dashboard' : 'Go to Home'
})
</script>
