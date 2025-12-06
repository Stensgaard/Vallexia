import { createI18n } from "vue-i18n";
import { localeService } from "@/services/localeService";

/**
 * Formats a language code into a display name by capitalizing the first letter.
 * @param {string} code - The language code (e.g., 'en', 'da')
 * @returns {string} Formatted language name with first letter capitalized
 */
const formatLanguageName = (code) =>
  code?.charAt(0).toUpperCase() + code?.slice(1) || "";

// Dynamically import all locale files from the locales directory
// This automatically discovers all available translation files
const localeModules = import.meta.glob("./locales/*.json", { eager: true });

// Extract locale codes from file paths and build messages object
const messages = {};
const availableLocaleCodes = [];
Object.keys(localeModules).forEach((path) => {
  // Extract locale code from path (e.g., './locales/en.json' -> 'en')
  const localeMatch = path.match(/\.\/locales\/(.+)\.json$/);
  if (localeMatch) {
    const localeCode = localeMatch[1];
    messages[localeCode] = localeModules[path].default || localeModules[path];
    availableLocaleCodes.push(localeCode);
  }
});

const fallbackLanguages = availableLocaleCodes.map((code) => ({
  code,
  name: formatLanguageName(code),
}));

export const SUPPORTED_LANGUAGES = [...fallbackLanguages];
let supportedLanguageCodes = new Set(
  fallbackLanguages.map((lang) => lang.code),
);

const normalizeLanguages = (languages) => {
  if (!Array.isArray(languages) || languages.length === 0) {
    return fallbackLanguages;
  }

  return languages
    .map((lang) => {
      // Extract and normalize code
      const code =
        typeof lang === "string" ? lang : lang.code || lang.locale || "";
      if (!code || typeof code !== "string") {
        return null;
      }

      const normalizedCode = code.toLowerCase().trim();
      if (!normalizedCode) {
        return null;
      }

      return {
        code: normalizedCode,
        name:
          (typeof lang === "object" && lang.name) ||
          formatLanguageName(normalizedCode),
      };
    })
    .filter((lang) => lang && messages[lang.code]);
};

const updateSupportedLanguages = (languages) => {
  const normalized = normalizeLanguages(languages);
  SUPPORTED_LANGUAGES.splice(0, SUPPORTED_LANGUAGES.length, ...normalized);
  supportedLanguageCodes = new Set(normalized.map((lang) => lang.code));
};

const loadSupportedLanguages = async () => {
  try {
    const locales = await localeService.getSupportedLocales();
    updateSupportedLanguages(locales);
  } catch (error) {
    // Log warning in development mode, keep fallback languages if backend fetch fails
    if (import.meta.env.DEV) {
      console.warn("Failed to load supported languages from backend:", error);
    }
  }

  return SUPPORTED_LANGUAGES;
};

// Configure i18n
export const i18n = createI18n({
  legacy: false,
  locale: "en",
  fallbackLocale: "en",
  messages,
});

const supportedLanguagesPromise = loadSupportedLanguages();

export const ensureSupportedLanguagesLoaded = () => supportedLanguagesPromise;
export const getSupportedLanguageCodes = () =>
  Array.from(supportedLanguageCodes);
export const isSupportedLanguage = (code) => {
  if (!code) {
    return false;
  }

  return supportedLanguageCodes.has(code);
};
export const getDefaultLanguage = () => SUPPORTED_LANGUAGES[0]?.code || "en";
