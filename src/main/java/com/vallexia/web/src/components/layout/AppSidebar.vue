<template>
  <!-- Mobile sidebar overlay -->
  <div
    v-if="isOpen"
    class="fixed inset-0 z-40 lg:hidden"
    @click="$emit('close')"
  >
    <div class="fixed inset-0 bg-gray-600 bg-opacity-75"></div>
  </div>

  <!-- Sidebar -->
  <div
    :class="[
      'fixed inset-y-0 left-0 z-50 w-64 bg-white shadow-lg transform transition-transform duration-300 ease-in-out lg:translate-x-0 lg:sticky lg:top-0 lg:h-screen',
      isOpen ? 'translate-x-0' : '-translate-x-full',
    ]"
  >
    <div class="flex flex-col h-full">
      <!-- Logo -->
      <div
        class="flex items-center justify-between h-16 px-4 border-b border-gray-200"
      >
        <div class="flex items-center">
          <svg
            class="h-8 w-8 text-blue-600"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
            />
          </svg>
          <span class="ml-2 text-xl font-bold text-gray-900">Vallexia</span>
        </div>
        <button
          class="lg:hidden p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100"
          @click="$emit('close')"
        >
          <svg
            class="h-6 w-6"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>
      </div>

      <!-- Navigation -->
      <nav class="flex-1 px-4 py-4 space-y-2 overflow-y-auto">
        <!-- User navigation items -->
        <template v-for="item in userItems" :key="item.name">
          <RouterLink
            v-if="item.href"
            :to="item.href"
            :class="[
              'group flex items-center px-3 py-2 text-sm font-medium rounded-md transition-colors',
              route.path === item.href ||
              (route.path.startsWith(item.href) && item.href !== '/')
                ? 'bg-blue-50 text-blue-700 border-r-2 border-blue-700'
                : 'text-gray-700 hover:bg-gray-50 hover:text-gray-900',
            ]"
            @click="$emit('close')"
          >
            <component
              :is="item.icon"
              :class="[
                'mr-3 h-5 w-5 flex-shrink-0',
                route.path === item.href ||
                (route.path.startsWith(item.href) && item.href !== '/')
                  ? 'text-blue-500'
                  : 'text-gray-400 group-hover:text-gray-500',
              ]"
            />
            {{ item.name }}
          </RouterLink>
          <button
            v-else-if="item.action"
            :class="[
              'group flex items-center w-full px-3 py-2 text-sm font-medium rounded-md transition-colors text-gray-700 hover:bg-gray-50 hover:text-gray-900 cursor-pointer',
            ]"
            @click="
              () => {
                item.action();
                $emit('close');
              }
            "
          >
            <component
              :is="item.icon"
              :class="[
                'mr-3 h-5 w-5 flex-shrink-0 text-gray-400 group-hover:text-gray-500',
              ]"
            />
            {{ item.name }}
          </button>
        </template>

        <!-- Admin tools divider -->
        <div v-if="adminItems.length > 0" class="pt-4 mt-4 border-t border-gray-200">
          <div class="px-3 py-2 text-xs font-semibold text-gray-500 uppercase tracking-wider">
            Admin tools
          </div>
        </div>

        <!-- Admin navigation items -->
        <template v-for="item in adminItems" :key="item.name">
          <RouterLink
            v-if="item.href"
            :to="item.href"
            :class="[
              'group flex items-center px-3 py-2 text-sm font-medium rounded-md transition-colors',
              route.path === item.href ||
              (route.path.startsWith(item.href) && item.href !== '/')
                ? 'bg-blue-50 text-blue-700 border-r-2 border-blue-700'
                : 'text-gray-700 hover:bg-gray-50 hover:text-gray-900',
            ]"
            @click="$emit('close')"
          >
            <component
              :is="item.icon"
              :class="[
                'mr-3 h-5 w-5 flex-shrink-0',
                route.path === item.href ||
                (route.path.startsWith(item.href) && item.href !== '/')
                  ? 'text-blue-500'
                  : 'text-gray-400 group-hover:text-gray-500',
              ]"
            />
            {{ item.name }}
          </RouterLink>
        </template>
      </nav>

      <!-- Logout (separated from admin tools) -->
      <div class="border-t border-gray-200 p-4">
        <button
          :class="[
            'group flex items-center w-full px-3 py-2 text-sm font-medium rounded-md transition-colors text-gray-700 hover:bg-gray-50 hover:text-gray-900 cursor-pointer',
          ]"
          @click="
            () => {
              logoutItem.action();
              $emit('close');
            }
          "
        >
          <component
            :is="logoutItem.icon"
            :class="[
              'mr-3 h-5 w-5 flex-shrink-0 text-gray-400 group-hover:text-gray-500',
            ]"
          />
          {{ logoutItem.name }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, h } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { useAuthStore } from "@/stores/auth";

defineProps({
  isOpen: {
    type: Boolean,
    default: false,
  },
});

defineEmits(["close"]);

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const { t } = useI18n();

const handleLogout = async () => {
  await authStore.logout();
  router.push("/");
};

// Define icon components using Vue's h() function
const DashboardIcon = (props) =>
  h(
    "svg",
    {
      fill: "none",
      stroke: "currentColor",
      viewBox: "0 0 24 24",
      ...props,
    },
    [
      h("path", {
        "stroke-linecap": "round",
        "stroke-linejoin": "round",
        "stroke-width": "2",
        d: "M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6",
      }),
    ],
  );

const RecipeIcon = (props) =>
  h(
    "svg",
    {
      fill: "none",
      stroke: "currentColor",
      viewBox: "0 0 24 24",
      ...props,
    },
    [
      h("path", {
        "stroke-linecap": "round",
        "stroke-linejoin": "round",
        "stroke-width": "2",
        d: "M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253",
      }),
    ],
  );

const MealPlanIcon = (props) =>
  h(
    "svg",
    {
      fill: "none",
      stroke: "currentColor",
      viewBox: "0 0 24 24",
      ...props,
    },
    [
      h("path", {
        "stroke-linecap": "round",
        "stroke-linejoin": "round",
        "stroke-width": "2",
        d: "M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z",
      }),
    ],
  );

const GroceryListIcon = (props) =>
  h(
    "svg",
    {
      fill: "none",
      stroke: "currentColor",
      viewBox: "0 0 24 24",
      ...props,
    },
    [
      h("path", {
        "stroke-linecap": "round",
        "stroke-linejoin": "round",
        "stroke-width": "2",
        d: "M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2",
      }),
    ],
  );

const NutritionIcon = (props) =>
  h(
    "svg",
    {
      fill: "none",
      stroke: "currentColor",
      viewBox: "0 0 24 24",
      ...props,
    },
    [
      h("path", {
        "stroke-linecap": "round",
        "stroke-linejoin": "round",
        "stroke-width": "2",
        d: "M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z",
      }),
    ],
  );

const ProfileIcon = (props) =>
  h(
    "svg",
    {
      fill: "none",
      stroke: "currentColor",
      viewBox: "0 0 24 24",
      ...props,
    },
    [
      h("path", {
        "stroke-linecap": "round",
        "stroke-linejoin": "round",
        "stroke-width": "2",
        d: "M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z",
      }),
    ],
  );

const LogoutIcon = (props) =>
  h(
    "svg",
    {
      fill: "none",
      stroke: "currentColor",
      viewBox: "0 0 24 24",
      ...props,
    },
    [
      h("path", {
        "stroke-linecap": "round",
        "stroke-linejoin": "round",
        "stroke-width": "2",
        d: "M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1",
      }),
    ],
  );

const AdminIcon = (props) =>
  h(
    "svg",
    {
      fill: "none",
      stroke: "currentColor",
      viewBox: "0 0 24 24",
      ...props,
    },
    [
      h("path", {
        "stroke-linecap": "round",
        "stroke-linejoin": "round",
        "stroke-width": "2",
        d: "M9 12l2 2 4-4m5 2a9 9 0 11-18 0 9 9 0 0118 0z",
      }),
    ],
  );

// Split navigation items into user, admin, and logout
const userItems = computed(() => [
  {
    name: t("layout.navigation.dashboard"),
    href: "/home",
    icon: DashboardIcon,
  },
  {
    name: t("layout.navigation.recipes"),
    href: "/recipes",
    icon: RecipeIcon,
  },
  {
    name: t("layout.navigation.mealPlans"),
    href: "/meal-plans",
    icon: MealPlanIcon,
  },
  {
    name: t("layout.navigation.groceryLists"),
    href: "/grocery-lists",
    icon: GroceryListIcon,
  },
  {
    name: t("layout.navigation.nutrition"),
    href: "/nutrition",
    icon: NutritionIcon,
  },
  {
    name: t("layout.navigation.profile"),
    href: "/profile",
    icon: ProfileIcon,
  },
]);

const adminItems = computed(() =>
  authStore.isAdmin
    ? [
        {
          name: "Ingredient curation",
          href: "/admin/ingredients",
          icon: AdminIcon,
        },
        {
          name: "Stores",
          href: "/admin/stores",
          icon: AdminIcon,
        },
        {
          name: "Offer filters",
          href: "/admin/offer-filters",
          icon: AdminIcon,
        },
      ]
    : []
);

const logoutItem = computed(() => ({
  name: t("layout.navigation.logout"),
  action: handleLogout,
  icon: LogoutIcon,
}));
</script>
