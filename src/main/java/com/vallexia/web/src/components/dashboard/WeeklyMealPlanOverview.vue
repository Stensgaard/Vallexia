<template>
  <div
    class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 flex flex-col h-full"
  >
    <div class="flex items-center justify-between mb-4">
      <h3 class="text-lg font-semibold text-gray-900">
        {{ t("dashboard.weeklyMealPlan.title") }}
      </h3>
      <RouterLink
        to="/meal-plans"
        class="text-sm text-blue-600 hover:text-blue-700 font-medium"
      >
        {{ t("dashboard.weeklyMealPlan.viewAll") }} →
      </RouterLink>
    </div>

    <!-- Week navigation -->
    <div class="flex items-center justify-between mb-4">
      <button
        :aria-label="t('dashboard.weeklyMealPlan.previousWeek')"
        class="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-md"
        @click="previousWeek"
      >
        <svg
          class="h-5 w-5"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M15 19l-7-7 7-7"
          />
        </svg>
      </button>
      <h4 class="text-sm font-medium text-gray-900">{{ weekRange }}</h4>
      <button
        :aria-label="t('dashboard.weeklyMealPlan.nextWeek')"
        class="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-md"
        @click="nextWeek"
      >
        <svg
          class="h-5 w-5"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M9 5l7 7-7 7"
          />
        </svg>
      </button>
    </div>

    <!-- Calendar grid -->
    <div class="flex-1 flex flex-col">
      <div class="grid grid-cols-7 gap-2 flex-1">
        <div v-for="day in weekDays" :key="day.date" class="text-center">
          <div class="text-xs font-medium text-gray-500 mb-2">
            {{ day.name }}
          </div>
          <div class="text-sm font-semibold text-gray-900 mb-2">
            {{ day.day }}
          </div>

          <!-- Meal slots -->
          <div class="space-y-1">
            <div
              v-for="mealType in MEAL_TYPES"
              :key="`${day.date}-${mealType}`"
              class="h-8 bg-gray-50 rounded border border-gray-200 flex items-center justify-center cursor-pointer hover:bg-gray-100 transition-colors"
              @click="openMealSelector(day.date, mealType)"
            >
              <span
                v-if="getMealForDay(day.date, mealType)"
                class="text-xs text-gray-700 truncate px-1"
              >
                {{ getMealForDay(day.date, mealType) }}
              </span>
              <svg
                v-else
                class="h-4 w-4 text-gray-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M12 6v6m0 0v6m0-6h6m-6 0H6"
                />
              </svg>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick actions -->
    <div class="mt-4 flex space-x-2">
      <button
        :aria-label="t('dashboard.weeklyMealPlan.generatePlan')"
        class="flex-1 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
        @click="generateWeeklyPlan"
      >
        {{ t("dashboard.weeklyMealPlan.generatePlan") }}
      </button>
      <button
        :aria-label="t('dashboard.weeklyMealPlan.clear')"
        class="px-4 py-2 border border-gray-300 text-gray-700 rounded-md text-sm font-medium hover:bg-gray-50 transition-colors"
        @click="clearWeek"
      >
        {{ t("dashboard.weeklyMealPlan.clear") }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import { RouterLink } from "vue-router";
import { useSettingsStore } from "@/stores/settings";
import { userService } from "@/services/userService";

const { t } = useI18n();
const settingsStore = useSettingsStore();

const userMealTypes = ref([]);

// Fetch user meal types from profile
const loadUserMealTypes = async () => {
  const profile = await userService.getProfile();
  // Convert uppercase enum values (BREAKFAST, LUNCH, etc.) to lowercase
  userMealTypes.value = (profile.mealTypes || []).map((type) =>
    type.toLowerCase(),
  );
};

// Computed property for meal types - use user's meal types
const MEAL_TYPES = computed(() => {
  return userMealTypes.value;
});

const emit = defineEmits(["meal-select", "generate-plan", "clear-week"]);

const currentWeekStart = ref(new Date());

// Calculate current week start based on user's first day of week setting
const getWeekStart = (date) => {
  const d = new Date(date);
  const day = d.getDay();
  const firstDay = settingsStore.firstDayOfWeek === "SUNDAY" ? 0 : 1;

  // Create a new date to avoid mutation
  const result = new Date(d);

  if (firstDay === 1) {
    // Monday is first day
    const diff = result.getDate() - day + (day === 0 ? -6 : 1);
    result.setDate(diff);
  } else {
    // Sunday is first day
    const diff = result.getDate() - day;
    result.setDate(diff);
  }

  return result;
};

// Initialize with current week
currentWeekStart.value = getWeekStart(new Date());

const weekDays = computed(() => {
  const days = [];
  const start = new Date(currentWeekStart.value);

  for (let i = 0; i < 7; i++) {
    const date = new Date(start);
    date.setDate(start.getDate() + i);

    days.push({
      date: date.toISOString().split("T")[0],
      name: date.toLocaleDateString(settingsStore.locale, { weekday: "short" }),
      day: date.getDate(),
    });
  }

  return days;
});

const weekRange = computed(() => {
  const start = weekDays.value[0];
  const end = weekDays.value[6];
  // Use settings store to format dates properly
  const startDate = new Date(start.date);
  const endDate = new Date(end.date);
  return `${settingsStore.formatDateFn(startDate)} - ${settingsStore.formatDateFn(endDate)}`;
});

// TODO: replace with actual meal data from store/API
const mealPlans = ref({
  "2024-01-15": {
    breakfast: "Oatmeal",
    lunch: "Salad",
    dinner: "Pasta",
  },
  "2024-01-16": {
    breakfast: "Toast",
    lunch: "Sandwich",
    dinner: "Chicken",
  },
});

const getMealForDay = (date, mealType) => {
  return mealPlans.value[date]?.[mealType] || null;
};

const previousWeek = () => {
  const newDate = new Date(currentWeekStart.value);
  newDate.setDate(newDate.getDate() - 7);
  currentWeekStart.value = getWeekStart(newDate);
};

const nextWeek = () => {
  const newDate = new Date(currentWeekStart.value);
  newDate.setDate(newDate.getDate() + 7);
  currentWeekStart.value = getWeekStart(newDate);
};

const openMealSelector = (date, mealType) => {
  emit("meal-select", { date, mealType });
};

const generateWeeklyPlan = () => {
  emit("generate-plan", { weekStart: currentWeekStart.value });
};

const clearWeek = () => {
  emit("clear-week", { weekStart: currentWeekStart.value });
};

// Load user meal types on component mount
onMounted(() => {
  loadUserMealTypes();
});
</script>
