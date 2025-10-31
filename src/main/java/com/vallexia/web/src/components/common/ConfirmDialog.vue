<template>
  <Transition name="fade">
    <div v-if="show" class="fixed inset-0 z-50 overflow-y-auto" @click.self="$emit('cancel')">
      <div class="flex items-center justify-center min-h-screen px-4 pt-4 pb-20 text-center sm:block sm:p-0">
        <!-- Background overlay -->
        <div class="fixed inset-0 transition-opacity bg-gray-500 bg-opacity-75" aria-hidden="true"></div>
        
        <!-- Center the modal -->
        <span class="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>
        
        <!-- Modal panel -->
        <div class="inline-block w-full max-w-md p-6 my-8 overflow-hidden text-left align-middle transition-all transform bg-white shadow-xl rounded-lg">
          <div class="flex items-center justify-center w-12 h-12 mx-auto mb-4 bg-red-100 rounded-full">
            <svg class="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
          </div>
          
          <div class="text-center">
            <h3 class="text-lg font-medium text-gray-900 mb-2">
              {{ title }}
            </h3>
            <p class="text-sm text-gray-500 mb-6">
              {{ message }}
            </p>
            
            <div class="flex flex-col sm:flex-row gap-3 sm:justify-center">
              <button
                type="button"
                class="btn btn-outline order-2 sm:order-1"
                @click="$emit('cancel')"
              >
                {{ cancelText }}
              </button>
              <button
                type="button"
                :class="confirmButtonClasses"
                @click="$emit('confirm')"
              >
                {{ confirmText }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: 'Confirm Action'
  },
  message: {
    type: String,
    required: true
  },
  confirmText: {
    type: String,
    default: 'Confirm'
  },
  cancelText: {
    type: String,
    default: 'Cancel'
  },
  type: {
    type: String,
    default: 'danger',
    validator: (value) => ['danger', 'warning', 'info'].includes(value)
  }
})

const emit = defineEmits(['confirm', 'cancel'])

const confirmButtonClasses = computed(() => {
  const typeClasses = {
    danger: 'btn btn-danger order-1 sm:order-2',
    warning: 'btn btn-warning order-1 sm:order-2',
    info: 'btn btn-primary order-1 sm:order-2'
  }
  
  return typeClasses[props.type]
})
</script>








