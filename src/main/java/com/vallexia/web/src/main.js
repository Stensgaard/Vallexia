import { createApp, watch } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import router from "./router";
import { useAuthStore } from "./stores/auth";
import { useSettingsStore } from "./stores/settings";

// Import global styles
import "./assets/css/main.css";

// Import i18n configuration and helpers
import {
  i18n,
  ensureSupportedLanguagesLoaded,
  getDefaultLanguage,
  isSupportedLanguage,
} from "./i18n";
import { ensureLocaleConfigLoaded } from "./utils/localeConfig";

const showConfigErrorScreen = (message) => {
  const root = document.getElementById("app");
  if (!root) {
    return;
  }

  // Use English text for error screen (appears before i18n is initialized)
  const title = "Unable to load configuration";
  const defaultMessage = "Please check your connection and try again.";
  const retryText = "Retry";

  // Create safe HTML structure using innerHTML for static content
  root.innerHTML = `
    <div class="min-h-screen flex flex-col items-center justify-center bg-gray-50 px-4">
      <div class="max-w-md w-full bg-white shadow rounded-lg p-6 text-center">
        <h1 class="text-xl font-semibold text-gray-900 mb-2">${title}</h1>
        <p class="text-gray-600 mb-4" id="error-message"></p>
        <button
          id="retry-locale-config"
          class="btn btn-primary w-full"
        >
          ${retryText}
        </button>
      </div>
    </div>
  `;

  const errorMessageEl = document.getElementById("error-message");
  if (errorMessageEl) {
    errorMessageEl.textContent = message || defaultMessage;
  }

  const retryBtn = document.getElementById("retry-locale-config");
  if (retryBtn) {
    retryBtn.addEventListener("click", () => {
      root.innerHTML = "";
      bootstrap();
    });
  }
};

const loadLocaleDependencies = async () => {
  await Promise.all([
    ensureSupportedLanguagesLoaded(),
    ensureLocaleConfigLoaded(),
  ]);
};

const bootstrap = async () => {
  try {
    await loadLocaleDependencies();
  } catch (error) {
    if (import.meta.env.DEV) {
      // eslint-disable-next-line no-console
      console.error("Failed to load locale configuration", error);
    }
    showConfigErrorScreen(
      error?.message || "Failed to load locale configuration.",
    );
    return;
  }

  const app = createApp(App);
  const pinia = createPinia();

  app.use(pinia);
  app.use(router);
  app.use(i18n);

  const authStore = useAuthStore();
  const settingsStore = useSettingsStore();

  const applyLocale = (localeCode) => {
    if (isSupportedLanguage(localeCode)) {
      i18n.global.locale.value = localeCode;
    }
  };

  // Function to load settings and update locale
  const loadUserSettingsAndUpdateLocale = async () => {
    try {
      await settingsStore.loadSettings();
      // Set locale from user settings, overriding any temporary locale changes
      if (settingsStore.settings?.language) {
        applyLocale(settingsStore.settings.language);
      } else {
        applyLocale(getDefaultLanguage());
      }
    } catch (error) {
      // Log warning for debugging while still allowing app to continue
      if (import.meta.env.DEV) {
        // eslint-disable-next-line no-console
        console.warn("Failed to load user settings on initialization:", error);
      }
      // Apply default language as fallback
      applyLocale(getDefaultLanguage());
    }
  };

  // Watch for authentication state changes
  watch(
    () => !!(authStore.accessToken && authStore.user),
    async (isAuthenticated, wasAuthenticated) => {
      // If user just logged in (changed from false to true), load their settings
      if (!wasAuthenticated && isAuthenticated) {
        await loadUserSettingsAndUpdateLocale();
      }

      // Only redirect if auth was lost (changed from true to false), not on initial load
      if (
        wasAuthenticated &&
        !isAuthenticated &&
        router.currentRoute.value.meta?.requiresAuth
      ) {
        // Auth was lost while on a protected route - redirect to homepage
        router.replace("/");
      }
    },
  );

  // Watch for language changes in settings store
  watch(
    () => settingsStore.settings?.language,
    (newLanguage) => {
      if (newLanguage) {
        applyLocale(newLanguage);
      }
    },
  );

  try {
    await authStore.initializeAuth();
    // Load user settings if authenticated
    if (authStore.isAuthenticated) {
      await loadUserSettingsAndUpdateLocale();
    }
  } catch (error) {
    // Log error for debugging but don't block app mounting
    if (import.meta.env.DEV) {
      // eslint-disable-next-line no-console
      console.error("Auth initialization error:", error);
    }
  } finally {
    app.mount("#app");
  }
};

bootstrap();
