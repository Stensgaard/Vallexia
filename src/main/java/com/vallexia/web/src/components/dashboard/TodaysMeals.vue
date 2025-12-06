<template>
  <div
    class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 flex flex-col h-full"
  >
    <div class="flex items-center justify-between mb-4">
      <h3 class="text-lg font-semibold text-gray-900">
        {{ t("dashboard.todaysMeals.title") }}
      </h3>
      <span class="text-sm text-gray-500">{{ todayDate }}</span>
    </div>

    <div class="flex-1 space-y-4">
      <div
        v-for="meal in todaysMeals"
        :key="meal.type"
        class="flex items-center justify-between p-4 bg-gray-50 rounded-lg"
      >
        <div class="flex items-center">
          <div
            class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center mr-3"
          >
            <component :is="meal.icon" class="h-5 w-5 text-blue-600" />
          </div>
          <div>
            <h4 class="font-medium text-gray-900">{{ meal.name }}</h4>
            <p class="text-sm text-gray-500">{{ meal.description }}</p>
          </div>
        </div>

        <div class="text-right">
          <div v-if="meal.nutrition" class="text-sm text-gray-600">
            <div>{{ formatNumber(meal.nutrition.calories, 0) }} cal</div>
            <div class="text-xs text-gray-500">
              {{ formatNutritionalValue(meal.nutrition.protein) }} protein
            </div>
          </div>
          <button
            v-else
            :aria-label="
              t('dashboard.todaysMeals.addMeal', { mealType: meal.type })
            "
            class="text-blue-600 hover:text-blue-700 text-sm font-medium"
            @click="addMeal(meal.type)"
          >
            {{ t("dashboard.todaysMeals.addMeal") }}
          </button>
        </div>
      </div>
    </div>

    <!-- Daily nutrition summary -->
    <div v-if="dailyNutrition" class="mt-6 pt-4 border-t border-gray-200">
      <h4 class="text-sm font-medium text-gray-900 mb-3">
        {{ t("dashboard.todaysMeals.dailyNutrition") }}
      </h4>
      <div class="grid grid-cols-4 gap-4">
        <div
          v-for="item in nutritionSummaryItems"
          :key="item.key"
          class="text-center"
        >
          <div class="text-lg font-semibold text-gray-900">
            {{ item.value }}
          </div>
          <div class="text-xs text-gray-500">{{ item.label }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { useI18n } from "vue-i18n";
import { useSettingsStore } from "@/stores/settings";
import { useFormattedValue } from "@/composables/useFormattedValue";

const { t } = useI18n();
const settingsStore = useSettingsStore();

// Use composable for formatted values with proper Vue reactivity
const { formatNutritionalValue } = useFormattedValue();

const formatNumber = (number, decimals = 0) => {
  return settingsStore.formatNumberFn(number, decimals);
};

const todayDate = computed(() => {
  return new Date().toLocaleDateString(settingsStore.locale, {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  });
});

// TODO: Replace with actual data from store/API
const todaysMeals = ref([
  {
    type: "breakfast",
    name: "Oatmeal with Berries",
    description: "Steel-cut oats with fresh blueberries and honey",
    nutrition: {
      calories: 320,
      protein: 12,
    },
    icon: "svg",
  },
  {
    type: "lunch",
    name: "Mediterranean Salad",
    description: "Mixed greens with tomatoes, olives, and feta",
    nutrition: {
      calories: 280,
      protein: 15,
    },
    icon: "svg",
  },
  {
    type: "dinner",
    name: "",
    description: "",
    nutrition: null,
    icon: "svg",
  },
]);

const dailyNutrition = computed(() => {
  const meals = todaysMeals.value.filter((meal) => meal.nutrition);
  if (meals.length === 0) return null;

  return meals.reduce(
    (total, meal) => ({
      calories: total.calories + meal.nutrition.calories,
      protein: total.protein + meal.nutrition.protein,
      carbs: total.carbs + (meal.nutrition.carbs || 0),
      fat: total.fat + (meal.nutrition.fat || 0),
    }),
    { calories: 0, protein: 0, carbs: 0, fat: 0 },
  );
});

const nutritionSummaryItems = computed(() => {
  if (!dailyNutrition.value) return [];

  return [
    {
      key: "calories",
      value: formatNumber(dailyNutrition.value.calories, 0),
      label: t("dashboard.todaysMeals.calories"),
    },
    {
      key: "protein",
      value: formatNutritionalValue(dailyNutrition.value.protein),
      label: t("dashboard.todaysMeals.protein"),
    },
    {
      key: "carbs",
      value: formatNutritionalValue(dailyNutrition.value.carbs),
      label: t("dashboard.todaysMeals.carbs"),
    },
    {
      key: "fat",
      value: formatNutritionalValue(dailyNutrition.value.fat),
      label: t("dashboard.todaysMeals.fat"),
    },
  ];
});

const emit = defineEmits(["add-meal"]);

const addMeal = (mealType) => {
  emit("add-meal", mealType);
};
</script>
