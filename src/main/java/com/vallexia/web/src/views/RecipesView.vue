<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Recipes</h1>
        <p class="text-gray-600">Manage your recipe collection</p>
      </div>
      <button class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md font-medium">
        Add Recipe
      </button>
    </div>

    <!-- Search and filters -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div class="flex flex-col sm:flex-row gap-4">
        <div class="flex-1">
          <input
            type="text"
            placeholder="Search recipes..."
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          />
        </div>
        <select class="px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500">
          <option>All Categories</option>
          <option>Breakfast</option>
          <option>Lunch</option>
          <option>Dinner</option>
          <option>Snacks</option>
        </select>
        <select class="px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500">
          <option>All Dietary</option>
          <option>Vegetarian</option>
          <option>Vegan</option>
          <option>Gluten-Free</option>
          <option>Keto</option>
        </select>
      </div>
    </div>

    <!-- Recipe grid placeholder -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="i in 6"
        :key="i"
        class="bg-white rounded-lg shadow-sm border border-gray-200 p-6"
      >
        <div class="aspect-w-16 aspect-h-9 bg-gray-200 rounded-lg mb-4"></div>
        <h3 class="font-semibold text-gray-900 mb-2">Recipe {{ i }}</h3>
        <p class="text-sm text-gray-600 mb-3">This is a placeholder recipe description.</p>
        <div class="flex items-center justify-between text-sm text-gray-500">
          <span>30 min</span>
          <span>4 servings</span>
        </div>
      </div>
    </div>

    <!-- Coming soon message -->
    <div class="bg-blue-50 border border-blue-200 rounded-lg p-6 text-center">
      <svg class="mx-auto h-12 w-12 text-blue-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
      </svg>
      <h3 class="text-lg font-semibold text-gray-900 mb-2">Recipe Management Coming Soon</h3>
      <p class="text-gray-600">
        We're working on bringing you a comprehensive recipe management system with search, categorization, and nutritional analysis.
      </p>
    </div>
  </div>
</template>

<script setup>
// Recipe management functionality will be implemented here
</script>
