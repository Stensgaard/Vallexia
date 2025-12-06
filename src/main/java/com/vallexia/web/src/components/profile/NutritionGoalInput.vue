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
      
      <div v-if="displayUnit" class="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none">
        <span class="text-gray-500 text-sm">{{ displayUnit }}</span>
      </div>
    </div>
    
    <!-- Error Message -->
    <p v-if="error" class="text-sm text-red-600">{{ error }}</p>
    
    <!-- Hint -->
    <p v-if="hint && !error" class="text-sm text-gray-500">{{ hint }}</p>
    
    <!-- Percentage Display (for macro nutrients) -->
    <div v-if="showPercentage && percentage !== null" class="text-sm text-gray-600">
      {{ settingsStore.formatNumberFn(percentage, 1) }}{{ $t('profile.nutritional.percentageOfCalories') }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useSettingsStore } from '@/stores/settings'

const settingsStore = useSettingsStore()

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
    default: null,
    validator: (value) => value === null || typeof value === 'number'
  },
  macroCalories: {
    type: Number,
    default: null
  },
  macroType: {
    type: String,
    default: null,
    validator: (value) => !value || ['protein', 'carbs', 'fats'].includes(value)
  }
})

const emit = defineEmits(['update:modelValue', 'blur'])

const inputValue = ref(props.modelValue)

watch(() => props.modelValue, (newValue) => {
  inputValue.value = newValue
})

const displayUnit = computed(() => {
  // If unit prop is provided, use it (allows override)
  if (props.unit) {
    return props.unit
  }
  
  // Fallback: determine unit based on measurement system
  // NOTE: This fallback assumes weight-based nutritional values (g/oz).
  // For non-weight values (e.g., calories), the unit prop must be explicitly provided.
  // In practice, all current usages provide the unit prop, so this is rarely used.
  return settingsStore.measurementSystem === 'IMPERIAL' ? 'oz' : 'g'
})

const inputClasses = computed(() => {
  const baseClasses = 'block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm'
  const errorClasses = props.error ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''
  const unitClasses = displayUnit.value ? 'pr-16' : ''
  const disabledClasses = props.disabled ? 'bg-gray-50 text-gray-500' : ''
  
  return `${baseClasses} ${errorClasses} ${unitClasses} ${disabledClasses}`
})

const percentage = computed(() => {
  if (!props.showPercentage || !props.dailyCalories || !inputValue.value) {
    return null
  }
  
  const calories = props.macroCalories ?? (inputValue.value * (props.macroType === 'fats' ? 9 : 4))
  return (calories / props.dailyCalories) * 100
})

const handleInput = () => {
  const numValue = inputValue.value === '' || inputValue.value === null ? null : Number(inputValue.value)
  emit('update:modelValue', numValue)
}

const handleBlur = () => {
  // Validate range
  if (inputValue.value !== '' && inputValue.value !== null) {
    const numValue = Number(inputValue.value)
    if (props.min !== null && numValue < props.min) {
      inputValue.value = props.min
    } else if (props.max !== null && numValue > props.max) {
      inputValue.value = props.max
    } else {
      inputValue.value = numValue
    }
  }
  
  const numValue = inputValue.value === '' || inputValue.value === null ? null : Number(inputValue.value)
  emit('update:modelValue', numValue)
  emit('blur')
}
</script>
