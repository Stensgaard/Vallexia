<template>
  <div class="min-h-screen bg-gray-50 flex flex-col lg:flex-row">
    <!-- Sidebar -->
    <AppSidebar :is-open="sidebarOpen" @close="sidebarOpen = false" />

    <!-- Main content area -->
    <div class="flex-1 flex flex-col min-w-0">
      <!-- Top bar -->
      <AppTopBar @toggle-sidebar="sidebarOpen = !sidebarOpen" />

      <!-- Page content -->
      <main class="flex-1 py-6">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <slot />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import AppSidebar from "@/components/layout/AppSidebar.vue";
import AppTopBar from "@/components/layout/AppTopBar.vue";

const sidebarOpen = ref(false);
</script>
