import { createI18n } from 'vue-i18n'

// Dynamically import all locale files from the locales directory
// This automatically discovers all available translation files
const localeModules = import.meta.glob('./locales/*.json', { eager: true })

// Extract locale codes from file paths and build messages object
const messages = {}
Object.keys(localeModules).forEach((path) => {
  // Extract locale code from path (e.g., './locales/en.json' -> 'en')
  const localeMatch = path.match(/\.\/locales\/(.+)\.json$/)
  if (localeMatch) {
    const localeCode = localeMatch[1]
    messages[localeCode] = localeModules[path].default || localeModules[path]
  }
})

// Get supported locale codes for validation
export const supportedLocales = Object.keys(messages)

// Export SUPPORTED_LANGUAGES in the format expected by components
// This uses translation keys for language names
export const SUPPORTED_LANGUAGES = supportedLocales.map(code => ({
  code,
  // Name will be resolved via translation key: constants.languages.{code}
  name: code.charAt(0).toUpperCase() + code.slice(1) // Fallback, should use $t() in components
}))

// Configure i18n
export const i18n = createI18n({
  legacy: false,
  locale: 'en',
  fallbackLocale: 'en',
  messages
})
