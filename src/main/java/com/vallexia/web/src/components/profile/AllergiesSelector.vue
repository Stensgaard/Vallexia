<template>
  <div class="space-y-3">
    <label class="block text-sm font-medium text-gray-700">
      {{ label }}
      <span v-if="required" class="text-red-500">*</span>
    </label>
    
    <div class="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
      <label
        v-for="(value, key) in options"
        :key="key"
        class="relative flex items-start p-3 border border-red-200 rounded-lg cursor-pointer hover:bg-red-50 focus-within:ring-2 focus-within:ring-red-500"
        :class="{ 'bg-red-50 border-red-300': selectedValues.includes(value) }"
      >
        <div class="flex items-center h-5">
          <input
            :id="`${id}-${key}`"
            v-model="selectedValues"
            :value="value"
            type="checkbox"
            class="h-4 w-4 text-red-600 focus:ring-red-500 border-gray-300 rounded"
            @change="handleChange"
          />
        </div>
        
        <div class="ml-3 text-sm">
          <div class="font-medium text-gray-900 flex items-center">
            <svg class="w-4 h-4 text-red-500 mr-1" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
            </svg>
            {{ getLabel(value) }}
          </div>
        </div>
      </label>
    </div>
    
    <!-- Error Message -->
    <p v-if="error" class="text-sm text-red-600">{{ error }}</p>
    
    <!-- Hint -->
    <p v-if="hint && !error" class="text-sm text-gray-500">{{ hint }}</p>
    
    <!-- Warning -->
    <div v-if="selectedValues.length > 0" class="mt-3 p-3 bg-yellow-50 border border-yellow-200 rounded-md">
      <div class="flex">
        <svg class="w-5 h-5 text-yellow-400 mr-2" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
        </svg>
        <div class="text-sm text-yellow-800">
          <p class="font-medium">Important:</p>
          <p>Selected allergies will be used to filter recipes and ingredients. Please ensure accuracy for your safety.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ALLERGIES, ALLERGIES_LABELS } from '@/utils/constants'

const props = defineProps({
  id: {
    type: String,
    required: true
  },
  label: {
    type: String,
    default: 'Allergies'
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
    default: 'Select any food allergies you have. This helps us avoid dangerous ingredients.'
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

const options = ALLERGIES
const selectedValues = ref([...props.modelValue])

watch(() => props.modelValue, (newValue) => {
  selectedValues.value = [...newValue]
}, { deep: true })

const getLabel = (value) => {
  return ALLERGIES_LABELS[value] || value
}

const handleChange = () => {
  emit('update:modelValue', [...selectedValues.value])
}
</script>
