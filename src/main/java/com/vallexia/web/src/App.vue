<template>
  <div id="app" class="min-h-screen bg-gray-50">
    <!-- Check if route requires main layout -->
    <MainLayout v-if="requiresMainLayout">
      <RouterView />
    </MainLayout>
    <!-- Default layout for auth and landing pages -->
    <RouterView v-else />
  </div>
</template>

<script setup>
import { computed } from "vue";
import { RouterView, useRoute } from "vue-router";
import MainLayout from "@/components/layout/MainLayout.vue";

const route = useRoute();

const requiresMainLayout = computed(() => {
  return route.meta.layout === "main";
});
</script>
