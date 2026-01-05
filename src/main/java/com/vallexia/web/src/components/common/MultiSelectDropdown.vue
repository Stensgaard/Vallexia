<template>
  <div class="relative">
    <label v-if="label" :for="id" class="block text-sm font-medium text-gray-700 mb-2">
      {{ label }}
    </label>
    <div class="relative">
      <button
        :id="id"
        ref="buttonRef"
        type="button"
        class="relative w-full bg-white border border-gray-300 rounded-md pl-3 pr-10 py-2 text-left cursor-pointer focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
        :class="{
          'border-blue-300 bg-blue-50': selectedValues.length > 0,
        }"
        @click.stop="toggleDropdown"
      >
        <span class="block truncate">
          <span v-if="selectedValues.length === 0" class="text-gray-500">
            {{ placeholder }}
          </span>
          <span v-else-if="selectedValues.length === 1" class="text-gray-900">
            {{ getSelectedLabel(selectedValues[0]) }}
          </span>
          <span v-else class="text-gray-900">
            {{ selectedValues.length }} {{ t("common.selected") || "selected" }}
          </span>
        </span>
        <span
          class="absolute inset-y-0 right-0 flex items-center pr-2 pointer-events-none"
        >
          <svg
            class="h-5 w-5 text-gray-400 transition-transform"
            :class="{ 'rotate-180': isOpen }"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M19 9l-7 7-7-7"
            />
          </svg>
        </span>
      </button>

      <transition
        enter-active-class="transition ease-out duration-100"
        enter-from-class="transform opacity-0 scale-95"
        enter-to-class="transform opacity-100 scale-100"
        leave-active-class="transition ease-in duration-75"
        leave-from-class="transform opacity-100 scale-100"
        leave-to-class="transform opacity-0 scale-95"
      >
        <div
          v-if="isOpen && sortedOptions && sortedOptions.length > 0"
          ref="dropdownRef"
          class="absolute z-50 mt-1 w-full bg-white shadow-lg max-h-60 rounded-md py-1 text-base ring-1 ring-black ring-opacity-5 overflow-auto focus:outline-none sm:text-sm"
        >
          <div
            v-for="option in sortedOptions"
            :key="option.value"
            class="cursor-pointer select-none relative py-2 pl-3 pr-9 hover:bg-gray-50"
            :class="{
              'bg-blue-50': selectedValues.includes(option.value),
            }"
            @click.stop="toggleOption(option.value)"
          >
            <div class="flex items-center">
              <input
                :checked="selectedValues.includes(option.value)"
                type="checkbox"
                class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                @click.stop
                @change="toggleOption(option.value)"
              />
              <span
                class="ml-3 block truncate"
                :class="{
                  'font-medium text-gray-900': selectedValues.includes(option.value),
                  'font-normal text-gray-700': !selectedValues.includes(option.value),
                }"
              >
                {{ option.label }}
              </span>
            </div>
            <span
              v-if="selectedValues.includes(option.value)"
              class="absolute inset-y-0 right-0 flex items-center pr-4"
            >
              <svg
                class="h-5 w-5 text-blue-600"
                fill="currentColor"
                viewBox="0 0 20 20"
              >
                <path
                  fill-rule="evenodd"
                  d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                  clip-rule="evenodd"
                />
              </svg>
            </span>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed, onMounted, onBeforeUnmount } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const props = defineProps({
  id: {
    type: String,
    required: true,
  },
  label: {
    type: String,
    default: "",
  },
  modelValue: {
    type: Array,
    default: () => [],
  },
  options: {
    type: Array,
    required: true,
    validator: (value) => {
      return value.every(
        (opt) => typeof opt === "object" && opt.value && opt.label,
      );
    },
  },
  placeholder: {
    type: String,
    default: "Select options...",
  },
});

const emit = defineEmits(["update:modelValue", "open", "close"]);

const isOpen = ref(false);
const selectedValues = ref([...props.modelValue]);
const dropdownRef = ref(null);
const buttonRef = ref(null);

watch(
  () => props.modelValue,
  (newValue) => {
    selectedValues.value = [...newValue];
  },
  { deep: true },
);

const toggleDropdown = () => {
  if (isOpen.value) {
    closeDropdown();
  } else {
    openDropdown();
  }
};

const openDropdown = () => {
  isOpen.value = true;
  emit("open");
};

const closeDropdown = () => {
  isOpen.value = false;
  emit("close");
};

const toggleOption = (value) => {
  const index = selectedValues.value.indexOf(value);
  if (index > -1) {
    selectedValues.value.splice(index, 1);
  } else {
    selectedValues.value.push(value);
  }
  emit("update:modelValue", [...selectedValues.value]);
};

const getSelectedLabel = (value) => {
  const option = props.options.find((opt) => opt.value === value);
  return option ? option.label : value;
};

const sortedOptions = computed(() => {
  if (!props.options || props.options.length === 0) {
    return [];
  }
  
  const selected = [];
  const unselected = [];
  
  props.options.forEach((option) => {
    if (selectedValues.value.includes(option.value)) {
      selected.push(option);
    } else {
      unselected.push(option);
    }
  });
  
  return [...selected, ...unselected];
});

const handleClickOutside = (event) => {
  if (!isOpen.value) {
    return;
  }
  
  if (!dropdownRef.value || !buttonRef.value) {
    return;
  }
  
  if (
    !dropdownRef.value.contains(event.target) &&
    !buttonRef.value.contains(event.target)
  ) {
    closeDropdown();
  }
};

const handleEscape = (e) => {
  if (e.key === "Escape" && isOpen.value) {
    closeDropdown();
  }
};

const handleCloseEvent = (event) => {
  if (event.detail?.id === props.id) {
    closeDropdown();
  }
};

onMounted(() => {
  document.addEventListener("click", handleClickOutside);
  document.addEventListener("keydown", handleEscape);
  document.addEventListener("close-dropdown", handleCloseEvent);
});

onBeforeUnmount(() => {
  isOpen.value = false;
  document.removeEventListener("click", handleClickOutside);
  document.removeEventListener("keydown", handleEscape);
  document.removeEventListener("close-dropdown", handleCloseEvent);
});
</script>
