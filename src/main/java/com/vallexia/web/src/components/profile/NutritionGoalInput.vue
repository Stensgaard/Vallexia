<template>
  <div class="space-y-1">
    <label :for="id" class="block text-sm font-medium text-gray-700">
      {{ label }}
      <span v-if="required" class="text-red-500">*</span>
    </label>
    
    <div class="relative">
      <input
        :id="id"
        v-model.number="inputValue"
        type="number"
        :min="min"
        :max="max"
        :step="step"
        :placeholder="placeholder"
        :disabled="disabled"
        :class="inputClasses"
        @input="handleInput"
        @blur="handleBlur"
      />
      
      <div v-if="unit" class="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none">
        <span class="text-gray-500 text-sm">{{ unit }}</span>
      </div>
    </div>
    
    <!-- Error Message -->
    <p v-if="error" class="text-sm text-red-600">{{ error }}</p>
    
    <!-- Hint -->
    <p v-if="hint && !error" class="text-sm text-gray-500">{{ hint }}</p>
    
    <!-- Percentage Display (for macro nutrients) -->
    <div v-if="showPercentage && percentage !== null" class="text-sm text-gray-600">
      {{ percentage.toFixed(1) }}% of daily calories
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  id: {
    type: String,
    required: true
  },
  label: {
    type: String,
    required: true
  },
  modelValue: {
    type: Number,
    default: null
  },
  unit: {
    type: String,
    default: ''
  },
  min: {
    type: Number,
    default: 0
  },
  max: {
    type: Number,
    default: null
  },
  step: {
    type: Number,
    default: 1
  },
  placeholder: {
    type: String,
    default: ''
  },
  error: {
    type: String,
    default: ''
  },
  hint: {
    type: String,
    default: ''
  },
  required: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  showPercentage: {
    type: Boolean,
    default: false
  },
  dailyCalories: {
    type: Number,
    default: null
  },
  caloriesPerGram: {
    type: Number,
    default: 4 // Default for protein/carbs, fats would be 9
  }
})

const emit = defineEmits(['update:modelValue'])

const inputValue = ref(props.modelValue)

watch(() => props.modelValue, (newValue) => {
  inputValue.value = newValue
})

const inputClasses = computed(() => {
  const baseClasses = 'block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm'
  const errorClasses = props.error ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''
  const unitClasses = props.unit ? 'pr-16' : ''
  const disabledClasses = props.disabled ? 'bg-gray-50 text-gray-500' : ''
  
  return `${baseClasses} ${errorClasses} ${unitClasses} ${disabledClasses}`
})

const percentage = computed(() => {
  if (!props.showPercentage || !props.dailyCalories || !inputValue.value) {
    return null
  }
  
  const calories = inputValue.value * props.caloriesPerGram
  return (calories / props.dailyCalories) * 100
})

const handleInput = () => {
  emit('update:modelValue', inputValue.value)
}

const handleBlur = () => {
  // Validate range
  if (props.min !== null && inputValue.value < props.min) {
    inputValue.value = props.min
  }
  if (props.max !== null && inputValue.value > props.max) {
    inputValue.value = props.max
  }
  
  emit('update:modelValue', inputValue.value)
}
</script>

