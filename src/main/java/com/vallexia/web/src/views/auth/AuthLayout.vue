<template>
  <div
    class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8"
  >
    <div class="max-w-md w-full space-y-8">
      <!-- Header -->
      <div class="text-center">
        <div
          class="mx-auto h-12 w-12 flex items-center justify-center rounded-full bg-blue-600"
        >
          <svg
            class="h-8 w-8 text-white"
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
        </div>
        <h2 class="mt-6 text-3xl font-extrabold text-gray-900">
          {{ headerTitleTranslated }}
        </h2>
        <p class="mt-2 text-sm text-gray-600">
          {{ headerSubtitleTranslated }}
        </p>
      </div>

      <!-- Form Container -->
      <div class="bg-white py-8 px-6 shadow-lg rounded-lg">
        <slot />
      </div>

      <!-- Footer Links -->
      <div class="text-center space-y-2">
        <slot name="footer">
          <p class="text-sm text-gray-600">
            <RouterLink
              to="/"
              class="font-medium text-blue-600 hover:text-blue-500"
            >
              ← {{ $t("common.back") }} {{ $t("home.title") }}
            </RouterLink>
          </p>
          <p class="text-sm text-gray-600">
            <RouterLink
              v-if="showLoginLink"
              to="/login"
              class="font-medium text-blue-600 hover:text-blue-500"
            >
              {{ $t("auth.register.alreadyHaveAccount") }}
              {{ $t("auth.register.signIn") }}
            </RouterLink>
            <RouterLink
              v-if="showRegisterLink"
              to="/register"
              class="font-medium text-blue-600 hover:text-blue-500"
            >
              {{ $t("auth.login.noAccount") }} {{ $t("auth.login.signUp") }}
            </RouterLink>
          </p>
        </slot>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute, RouterLink } from "vue-router";

const { t } = useI18n();
const route = useRoute();

const props = defineProps({
  title: {
    type: String,
    default: "",
  },
  subtitle: {
    type: String,
    default: "",
  },
});

const headerTitle = computed(() => {
  return props.title || route.meta.title || "";
});

const headerSubtitle = computed(() => {
  return props.subtitle || route.meta.subtitle || "";
});

const headerTitleTranslated = computed(() => {
  const title = headerTitle.value;
  if (!title) return "";
  // If it's a translation key (looks like a key), translate it
  if (title.includes("Title") || title.includes("Subtitle")) {
    const translated = t(`auth.meta.${title}`);
    // If translation exists (not the same as the key), return it
    return translated !== `auth.meta.${title}` ? translated : title;
  }
  // Otherwise return as-is (for custom titles passed as props)
  return title;
});

const headerSubtitleTranslated = computed(() => {
  const subtitle = headerSubtitle.value;
  if (!subtitle) return "";
  // If it's a translation key (looks like a key), translate it
  if (subtitle.includes("Title") || subtitle.includes("Subtitle")) {
    const translated = t(`auth.meta.${subtitle}`);
    // If translation exists (not the same as the key), return it
    return translated !== `auth.meta.${subtitle}` ? translated : subtitle;
  }
  // Otherwise return as-is (for custom subtitles passed as props)
  return subtitle;
});

const showLoginLink = computed(() => {
  return route.name === "Register";
});

const showRegisterLink = computed(() => {
  return route.name === "Login";
});
</script>
