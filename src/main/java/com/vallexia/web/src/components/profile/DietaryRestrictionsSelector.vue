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
        class="relative flex items-start p-3 border border-gray-200 rounded-lg cursor-pointer hover:bg-gray-50 focus-within:ring-2 focus-within:ring-blue-500"
        :class="{ 'bg-blue-50 border-blue-300': selectedValues.includes(value) }"
      >
        <div class="flex items-center h-5">
          <input
            :id="`${id}-${key}`"
            v-model="selectedValues"
            :value="value"
            type="checkbox"
            class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
            @change="handleChange"
          />
        </div>
        
        <div class="ml-3 text-sm">
          <div class="font-medium text-gray-900">{{ getLabel(value) }}</div>
          <div v-if="getDescription(value)" class="text-gray-500">{{ getDescription(value) }}</div>
        </div>
      </label>
    </div>
    
    <!-- Error Message -->
    <p v-if="error" class="text-sm text-red-600">{{ error }}</p>
    
    <!-- Hint -->
    <p v-if="!error" class="text-sm text-gray-500">{{ hint || $t('profile.dietary.restrictionsHint') }}</p>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { DIETARY_RESTRICTIONS } from '@/utils/constants'

const { t } = useI18n()

const props = defineProps({
  id: {
    type: String,
    required: true
  },
  label: {
    type: String,
    default: 'Dietary Restrictions'
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
    default: ''
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

const options = DIETARY_RESTRICTIONS
const selectedValues = ref([...props.modelValue])

watch(() => props.modelValue, (newValue) => {
  selectedValues.value = [...newValue]
}, { deep: true })

const getLabel = (value) => {
  return t(`constants.dietaryRestrictions.${value}`) || value
}

const getDescription = (value) => {
  if (!value) return ''
  return t(`constants.dietaryRestrictions.descriptions.${value}`) || ''
}

const handleChange = () => {
  emit('update:modelValue', [...selectedValues.value])
}
</script>
