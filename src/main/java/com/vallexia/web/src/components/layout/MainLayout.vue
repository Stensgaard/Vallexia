<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Sidebar -->
    <AppSidebar :is-open="sidebarOpen" @close="sidebarOpen = false" />
    
    <!-- Main content area -->
    <div class="lg:pl-64">
      <!-- Top bar -->
      <AppTopBar @toggle-sidebar="sidebarOpen = !sidebarOpen" />
      
      <!-- Page content -->
      <main class="py-6">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <RouterView />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { RouterView } from 'vue-router'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AppTopBar from '@/components/layout/AppTopBar.vue'

const sidebarOpen = ref(false)
</script>
