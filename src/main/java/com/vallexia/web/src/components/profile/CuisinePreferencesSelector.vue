<template>
  <div class="space-y-3">
    <label class="block text-sm font-medium text-gray-700">
      {{ label }}
      <span v-if="required" class="text-red-500">*</span>
    </label>

    <div class="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4">
      <label
        v-for="option in translatedOptions"
        :key="option.code"
        class="relative flex flex-col items-center p-3 border border-gray-200 rounded-lg cursor-pointer hover:bg-gray-50 focus-within:ring-2 focus-within:ring-blue-500"
        :class="{
          'bg-blue-50 border-blue-300': selectedValues.includes(option.code),
        }"
      >
        <div class="flex items-center h-5 mb-2">
          <input
            :id="`${id}-${option.code}`"
            v-model="selectedValues"
            :value="option.code"
            type="checkbox"
            class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
            @change="handleChange"
          />
        </div>

        <div class="text-center">
          <div class="text-sm font-medium text-gray-900">
            {{ option.name || option.code }}
          </div>
        </div>
      </label>
    </div>

    <!-- Error Message -->
    <p v-if="error" class="text-sm text-red-600">{{ error }}</p>

    <!-- Hint -->
    <p v-if="!error" class="text-sm text-gray-500">
      {{ hint || $t("profile.dietary.cuisinesHint") }}
    </p>
  </div>
</template>

<script setup>
import { ref, watch, computed } from "vue";
import { useI18n } from "vue-i18n";
import { getCuisineTypes } from "@/utils/localeConfig";

const { t, te } = useI18n();

const props = defineProps({
  id: {
    type: String,
    required: true,
  },
  label: {
    type: String,
    default: "Preferred Cuisines",
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
});

const emit = defineEmits(["update:modelValue"]);

const selectedValues = ref([...props.modelValue]);

const translatedOptions = computed(() => {
  return getCuisineTypes().map((option) => {
    const key = `constants.cuisineTypes.${option.code}`;
    return {
      code: option.code,
      name: te(key) ? t(key) : option.name,
    };
  });
});

watch(
  () => props.modelValue,
  (newValue) => {
    selectedValues.value = [...newValue];
  },
  { deep: true },
);

const handleChange = () => {
  emit("update:modelValue", [...selectedValues.value]);
};
</script>
