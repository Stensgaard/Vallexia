import js from '@eslint/js';
import vue from 'eslint-plugin-vue';
import prettier from '@vue/eslint-config-prettier';
import globals from 'globals';

export default [
  // Base ESLint recommended rules
  js.configs.recommended,
  
  // Vue plugin configuration
  ...vue.configs['flat/recommended'],
  
  // Prettier config (must be last to override other configs)
  prettier,
  
  // Custom configuration
  {
    files: ['**/*.{js,mjs,cjs,ts,vue}'],
    languageOptions: {
      ecmaVersion: 2022,
      sourceType: 'module',
      globals: {
        ...globals.browser,
        ...globals.node,
      },
    },
    rules: {
      // Allow unused variables that start with underscore
      'no-unused-vars': [
        'error',
        {
          argsIgnorePattern: '^_',
          varsIgnorePattern: '^_',
          caughtErrorsIgnorePattern: '^_',
        },
      ],
      // Allow console in development
      'no-console': 'warn',
      // Allow Object.prototype methods
      'no-prototype-builtins': 'off',
      // Vue specific rules
      'vue/multi-word-component-names': 'off',
      'vue/no-v-html': 'warn',
    },
  },
  {
    // Ignore patterns
    ignores: [
      'node_modules/**',
      'dist/**',
      'build/**',
      '*.config.js',
      'public/**',
    ],
  },
];
