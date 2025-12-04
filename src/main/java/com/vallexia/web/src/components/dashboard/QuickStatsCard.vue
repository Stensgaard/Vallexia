<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
    <div class="flex items-center justify-between mb-4">
      <h3 class="text-lg font-semibold text-gray-900">{{ title }}</h3>
      <div class="flex items-center">
        <component :is="icon" class="h-5 w-5 text-gray-400 mr-2" />
        <span class="text-sm text-gray-500">{{ subtitle }}</span>
      </div>
    </div>
    
    <div class="text-3xl font-bold text-gray-900 mb-2">{{ value }}</div>
    
    <div v-if="change !== null" class="flex items-center">
      <svg
        :class="[
          'h-4 w-4 mr-1',
          change >= 0 ? 'text-green-500' : 'text-red-500'
        ]"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          v-if="change >= 0"
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M7 17l9.2-9.2M17 17V7H7"
        />
        <path
          v-else
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M17 7l-9.2 9.2M7 7v10h10"
        />
      </svg>
      <span
        :class="[
          'text-sm font-medium',
          change >= 0 ? 'text-green-600' : 'text-red-600'
        ]"
      >
        {{ Math.abs(change) }}%
      </span>
      <span class="text-sm text-gray-500 ml-1">{{ t('dashboard.stats.fromLastWeek') }}</span>
    </div>
    
    <div v-if="actionText" class="mt-4">
      <button
        @click="$emit('action')"
        :aria-label="actionText"
        class="text-sm text-blue-600 hover:text-blue-700 font-medium"
      >
        {{ actionText }} →
      </button>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

defineProps({
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  },
  value: {
    type: [String, Number],
    required: true
  },
  change: {
    type: Number,
    default: null
  },
  icon: {
    type: String,
    default: 'svg'
  },
  actionText: {
    type: String,
    default: ''
  }
})

defineEmits(['action'])
</script>
