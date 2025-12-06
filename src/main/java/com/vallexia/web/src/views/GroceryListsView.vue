<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">
          {{ $t("groceryLists.title") }}
        </h1>
        <p class="text-gray-600">{{ $t("groceryLists.description") }}</p>
      </div>
      <button
        class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md font-medium"
      >
        {{ $t("groceryLists.create") }}
      </button>
    </div>

    <!-- Quick generate from meal plan -->
    <div
      class="bg-gradient-to-r from-green-50 to-blue-50 rounded-lg p-6 border border-green-200"
    >
      <div class="flex items-center justify-between">
        <div>
          <h3 class="text-lg font-semibold text-gray-900 mb-2">
            {{ $t("groceryLists.generateFromPlan") }}
          </h3>
          <p class="text-gray-600">
            {{ $t("groceryLists.generateFromPlanDescription") }}
          </p>
        </div>
        <button
          class="bg-green-600 hover:bg-green-700 text-white px-6 py-3 rounded-md font-medium"
        >
          {{ $t("groceryLists.generateList") }}
        </button>
      </div>
    </div>

    <!-- Active lists -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="list in groceryLists"
        :key="list.id"
        class="bg-white rounded-lg shadow-sm border border-gray-200 p-6"
      >
        <div class="flex items-center justify-between mb-4">
          <h3 class="font-semibold text-gray-900">{{ list.name }}</h3>
          <span class="text-xs text-gray-500">{{ list.date }}</span>
        </div>

        <div class="space-y-2 mb-4">
          <div
            v-for="item in list.items.slice(0, 3)"
            :key="item.id"
            class="flex items-center justify-between text-sm"
          >
            <span class="text-gray-700">{{ item.name }}</span>
            <span class="text-gray-500">{{ item.quantity }}</span>
          </div>
          <div v-if="list.items.length > 3" class="text-xs text-gray-500">
            +{{ list.items.length - 3 }} {{ $t("groceryLists.moreItems") }}
          </div>
        </div>

        <div class="flex items-center justify-between">
          <span class="text-sm font-medium text-gray-900"
            >{{ list.items.length }} {{ $t("groceryLists.items") }}</span
          >
          <div class="flex space-x-2">
            <button
              class="text-blue-600 hover:text-blue-700 text-sm font-medium"
            >
              {{ $t("groceryLists.edit") }}
            </button>
            <button
              class="text-green-600 hover:text-green-700 text-sm font-medium"
            >
              {{ $t("groceryLists.shop") }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Shopping optimization -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h3 class="text-lg font-semibold text-gray-900 mb-4">
        {{ $t("groceryLists.shoppingOptimization") }}
      </h3>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <h4 class="font-medium text-gray-900 mb-2">
            {{ $t("groceryLists.storeLayout") }}
          </h4>
          <p class="text-sm text-gray-600 mb-4">
            {{ $t("groceryLists.storeLayoutDescription") }}
          </p>
          <button
            class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md font-medium"
          >
            {{ $t("groceryLists.optimizeRoute") }}
          </button>
        </div>
        <div>
          <h4 class="font-medium text-gray-900 mb-2">
            {{ $t("groceryLists.priceTracking") }}
          </h4>
          <p class="text-sm text-gray-600 mb-4">
            {{ $t("groceryLists.priceTrackingDescription") }}
          </p>
          <button
            class="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-md font-medium"
          >
            {{ $t("groceryLists.trackPrices") }}
          </button>
        </div>
      </div>
    </div>

    <!-- Coming soon message -->
    <div class="bg-blue-50 border border-blue-200 rounded-lg p-6 text-center">
      <svg
        class="mx-auto h-12 w-12 text-blue-400 mb-4"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"
        />
      </svg>
      <h3 class="text-lg font-semibold text-gray-900 mb-2">
        {{ $t("groceryLists.comingSoon") }}
      </h3>
      <p class="text-gray-600">
        {{ $t("groceryLists.comingSoonDescription") }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from "vue-i18n";

const { t } = useI18n();

// Mock data for grocery lists
const groceryLists = [
  {
    id: 1,
    name: "This Week's Shopping",
    date: "Jan 15, 2024",
    items: [
      { id: 1, name: "Chicken Breast", quantity: "2 lbs" },
      { id: 2, name: "Salmon Fillet", quantity: "1 lb" },
      { id: 3, name: "Mixed Greens", quantity: "1 bag" },
      { id: 4, name: "Quinoa", quantity: "1 bag" },
      { id: 5, name: "Olive Oil", quantity: "1 bottle" },
    ],
  },
  {
    id: 2,
    name: "Pantry Essentials",
    date: "Jan 10, 2024",
    items: [
      { id: 1, name: "Rice", quantity: "5 lbs" },
      { id: 2, name: "Pasta", quantity: "3 boxes" },
      { id: 3, name: "Canned Tomatoes", quantity: "6 cans" },
    ],
  },
  {
    id: 3,
    name: "Breakfast Items",
    date: "Jan 8, 2024",
    items: [
      { id: 1, name: "Oats", quantity: "1 bag" },
      { id: 2, name: "Greek Yogurt", quantity: "2 containers" },
      { id: 3, name: "Berries", quantity: "2 packages" },
      { id: 4, name: "Honey", quantity: "1 jar" },
    ],
  },
];
</script>
