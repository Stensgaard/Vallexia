<template>
  <!-- Mobile sidebar overlay -->
  <div v-if="isOpen" class="fixed inset-0 z-40 lg:hidden" @click="$emit('close')">
    <div class="fixed inset-0 bg-gray-600 bg-opacity-75"></div>
  </div>

  <!-- Sidebar -->
  <div :class="[
    'fixed inset-y-0 left-0 z-50 w-64 bg-white shadow-lg transform transition-transform duration-300 ease-in-out lg:translate-x-0 lg:static lg:inset-0',
    isOpen ? 'translate-x-0' : '-translate-x-full'
  ]">
    <div class="flex flex-col h-full">
      <!-- Logo -->
      <div class="flex items-center justify-between h-16 px-4 border-b border-gray-200">
        <div class="flex items-center">
          <svg class="h-8 w-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
          </svg>
          <span class="ml-2 text-xl font-bold text-gray-900">Vallexia</span>
        </div>
        <button @click="$emit('close')" class="lg:hidden p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100">
          <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- Navigation -->
      <nav class="flex-1 px-4 py-4 space-y-2">
        <RouterLink
          v-for="item in navigationItems"
          :key="item.name"
          :to="item.href"
          :class="[
            'group flex items-center px-3 py-2 text-sm font-medium rounded-md transition-colors',
            route.path === item.href || (route.path.startsWith(item.href) && item.href !== '/')
              ? 'bg-blue-50 text-blue-700 border-r-2 border-blue-700'
              : 'text-gray-700 hover:bg-gray-50 hover:text-gray-900'
          ]"
          @click="$emit('close')"
        >
          <component
            :is="item.icon"
            :class="[
              'mr-3 h-5 w-5 flex-shrink-0',
              route.path === item.href || (route.path.startsWith(item.href) && item.href !== '/')
                ? 'text-blue-500'
                : 'text-gray-400 group-hover:text-gray-500'
            ]"
          />
          {{ item.name }}
        </RouterLink>
      </nav>

      <!-- User info -->
      <div class="border-t border-gray-200 p-4">
        <div class="flex items-center">
          <div class="flex-shrink-0">
            <div class="h-8 w-8 rounded-full bg-blue-100 flex items-center justify-center">
              <span class="text-sm font-medium text-blue-600">
                {{ userInitials }}
              </span>
            </div>
          </div>
          <div class="ml-3">
            <p class="text-sm font-medium text-gray-900">{{ userFullName }}</p>
            <p class="text-xs text-gray-500">{{ userEmail }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

defineProps({
  isOpen: {
    type: Boolean,
    default: false
  }
})

defineEmits(['close'])

const route = useRoute()
const authStore = useAuthStore()

const navigationItems = [
  {
    name: 'Dashboard',
    href: '/home',
    icon: 'svg'
  },
  {
    name: 'Recipes',
    href: '/recipes',
    icon: 'svg'
  },
  {
    name: 'Meal Plans',
    href: '/meal-plans',
    icon: 'svg'
  },
  {
    name: 'Grocery Lists',
    href: '/grocery-lists',
    icon: 'svg'
  },
  {
    name: 'Nutrition',
    href: '/nutrition',
    icon: 'svg'
  }
]

const userFullName = computed(() => authStore.user?.username || '')
const userEmail = computed(() => authStore.user?.email || '')
const userInitials = computed(() => {
  if (!userFullName.value) return 'U'
  return userFullName.value
    .split(' ')
    .map(name => name.charAt(0))
    .join('')
    .toUpperCase()
    .slice(0, 2)
})
</script>
