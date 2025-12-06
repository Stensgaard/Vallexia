<template>
  <div class="flex items-center justify-center">
    <div :class="spinnerClasses" :style="spinnerStyle"></div>
    <span v-if="text" class="ml-2 text-gray-600">{{ text }}</span>
  </div>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  size: {
    type: String,
    default: "medium",
    validator: (value) => ["small", "medium", "large"].includes(value),
  },
  color: {
    type: String,
    default: "blue",
    validator: (value) => ["blue", "white", "gray"].includes(value),
  },
  text: {
    type: String,
    default: "",
  },
});

const spinnerClasses = computed(() => {
  const sizeClasses = {
    small: "w-4 h-4",
    medium: "w-6 h-6",
    large: "w-8 h-8",
  };

  const colorClasses = {
    blue: "border-gray-300 border-t-blue-600",
    white: "border-gray-200 border-t-white",
    gray: "border-gray-200 border-t-gray-600",
  };

  return `spinner ${sizeClasses[props.size]} ${colorClasses[props.color]}`;
});

const spinnerStyle = computed(() => {
  return {
    animationDuration: "1s",
  };
});
</script>
