<template>
  <div class="space-y-2">
    <label
      v-if="label"
      :for="id"
      class="block text-sm font-medium text-gray-700"
    >
      {{ label }}
      <span v-if="required" class="text-red-500">*</span>
    </label>

    <!-- Input Field -->
    <div
      class="flex flex-wrap gap-2 p-2 border border-gray-300 rounded-md focus-within:ring-2 focus-within:ring-blue-500 focus-within:border-blue-500"
    >
      <!-- Tags -->
      <span
        v-for="(tag, index) in tags"
        :key="index"
        class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800"
      >
        {{ tag }}
        <button
          type="button"
          class="ml-1.5 inline-flex items-center justify-center w-4 h-4 rounded-full text-blue-400 hover:bg-blue-200 hover:text-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
          @click="removeTag(index)"
        >
          <svg class="w-2 h-2" fill="currentColor" viewBox="0 0 20 20">
            <path
              fill-rule="evenodd"
              d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
              clip-rule="evenodd"
            />
          </svg>
        </button>
      </span>

      <!-- Input -->
      <input
        :id="id"
        v-model="inputValue"
        type="text"
        :placeholder="placeholder || $t('common.tagInputPlaceholder')"
        :disabled="disabled"
        class="flex-1 min-w-0 border-0 focus:ring-0 focus:outline-none text-sm"
        @keydown="handleKeydown"
        @blur="addTag"
      />
    </div>

    <!-- Error Message -->
    <p v-if="error" class="text-sm text-red-600">{{ error }}</p>

    <!-- Hint -->
    <p v-if="hint && !error" class="text-sm text-gray-500">{{ hint }}</p>
  </div>
</template>

<script setup>
import { ref, watch } from "vue";

const props = defineProps({
  id: {
    type: String,
    required: true,
  },
  label: {
    type: String,
    default: "",
  },
  placeholder: {
    type: String,
    default: null,
  },
  modelValue: {
    type: Array,
    default: () => [],
  },
  error: {
    type: String,
    default: "",
  },
  hint: {
    type: String,
    default: "",
  },
  required: {
    type: Boolean,
    default: false,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  maxTags: {
    type: Number,
    default: null,
  },
});

const emit = defineEmits(["update:modelValue"]);

const inputValue = ref("");
const tags = ref([...props.modelValue]);

watch(
  () => props.modelValue,
  (newValue) => {
    tags.value = [...newValue];
  },
  { deep: true },
);

const handleKeydown = (event) => {
  if (event.key === "Enter" || event.key === ",") {
    event.preventDefault();
    addTag();
  }
};

const addTag = () => {
  const tag = inputValue.value.trim().toLowerCase();

  if (tag && !tags.value.includes(tag)) {
    if (props.maxTags && tags.value.length >= props.maxTags) {
      return;
    }

    tags.value.push(tag);
    inputValue.value = "";
    emit("update:modelValue", [...tags.value]);
  }
};

const removeTag = (index) => {
  tags.value.splice(index, 1);
  emit("update:modelValue", [...tags.value]);
};
</script>
