<template>
  <div class="space-y-3">
    <label class="block text-sm font-medium text-gray-700">
      {{ label }}
      <span v-if="required" class="text-red-500">*</span>
    </label>
    
    <div class="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4">
      <label
        v-for="(value, key) in options"
        :key="key"
        class="relative flex flex-col items-center p-3 border border-gray-200 rounded-lg cursor-pointer hover:bg-gray-50 focus-within:ring-2 focus-within:ring-blue-500"
        :class="{ 'bg-blue-50 border-blue-300': selectedValues.includes(value) }"
      >
        <div class="flex items-center h-5 mb-2">
          <input
            :id="`${id}-${key}`"
            v-model="selectedValues"
            :value="value"
            type="checkbox"
            class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
            @change="handleChange"
          />
        </div>
        
        <div class="text-center">
          <div class="text-2xl mb-1">{{ getFlag(value) }}</div>
          <div class="text-sm font-medium text-gray-900">{{ getLabel(value) }}</div>
        </div>
      </label>
    </div>
    
    <!-- Error Message -->
    <p v-if="error" class="text-sm text-red-600">{{ error }}</p>
    
    <!-- Hint -->
    <p v-if="hint && !error" class="text-sm text-gray-500">{{ hint }}</p>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { CUISINE_TYPES, CUISINE_TYPES_LABELS } from '@/utils/constants'

const props = defineProps({
  id: {
    type: String,
    required: true
  },
  label: {
    type: String,
    default: 'Preferred Cuisines'
  },
  modelValue: {
    type: Array,
    default: () => []
  },
  error: {
    type: String,
    default: ''
  },
  hint: {
    type: String,
    default: 'Select cuisines you enjoy to get personalized recipe recommendations.'
  },
  required: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const options = CUISINE_TYPES
const selectedValues = ref([...props.modelValue])

watch(() => props.modelValue, (newValue) => {
  selectedValues.value = [...newValue]
}, { deep: true })

const getLabel = (value) => {
  return CUISINE_TYPES_LABELS[value] || value
}

const getFlag = (value) => {
  const flags = {
    [CUISINE_TYPES.ITALIAN]: '🇮🇹',
    [CUISINE_TYPES.MEXICAN]: '🇲🇽',
    [CUISINE_TYPES.CHINESE]: '🇨🇳',
    [CUISINE_TYPES.JAPANESE]: '🇯🇵',
    [CUISINE_TYPES.INDIAN]: '🇮🇳',
    [CUISINE_TYPES.THAI]: '🇹🇭',
    [CUISINE_TYPES.MEDITERRANEAN]: '🌊',
    [CUISINE_TYPES.AMERICAN]: '🇺🇸',
    [CUISINE_TYPES.FRENCH]: '🇫🇷',
    [CUISINE_TYPES.GREEK]: '🇬🇷',
    [CUISINE_TYPES.KOREAN]: '🇰🇷',
    [CUISINE_TYPES.VIETNAMESE]: '🇻🇳'
  }
  return flags[value] || '🍽️'
}

const handleChange = () => {
  emit('update:modelValue', [...selectedValues.value])
}
</script>

