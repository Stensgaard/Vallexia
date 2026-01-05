<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
    <!-- Basic Search -->
    <div class="mb-4">
      <label for="search-query" class="block text-sm font-medium text-gray-700 mb-2">
        {{ $t("recipes.search.title") }}
      </label>
      <div class="flex gap-3">
        <div class="flex-1">
          <input
            id="search-query"
            v-model="localSearchParams.query"
            type="text"
            class="block w-full px-4 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
            :placeholder="$t('recipes.searchPlaceholder')"
            @keyup.enter="handleSearch"
          />
        </div>
        <button
          type="button"
          class="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          :disabled="isLoading"
          @click="handleSearch"
        >
          {{ $t("common.search") }}
        </button>
        <button
          v-if="hasActiveFilters"
          type="button"
          class="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500"
          @click="handleClear"
        >
          {{ $t("common.clear") }}
        </button>
      </div>
    </div>

    <!-- Profile Preferences Info -->
    <div
      v-if="hasProfilePreferences && !hasOverriddenPreferences"
      class="mb-4 p-3 bg-blue-50 border border-blue-200 rounded-md"
    >
      <div class="flex items-start">
        <svg
          class="w-5 h-5 text-blue-500 mr-2 mt-0.5"
          fill="currentColor"
          viewBox="0 0 20 20"
        >
          <path
            fill-rule="evenodd"
            d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"
            clip-rule="evenodd"
          />
        </svg>
        <div class="flex-1 text-sm text-blue-800">
          <p class="font-medium">{{ $t("recipes.search.usingProfilePreferences") }}</p>
          <p class="text-blue-700">{{ $t("recipes.search.overrideHint") }}</p>
        </div>
      </div>
    </div>

    <!-- Reset All to Profile Button -->
    <div v-if="hasOverriddenPreferences" class="mb-4">
      <button
        type="button"
        class="px-4 py-2 text-sm text-blue-600 hover:text-blue-800 border border-blue-300 rounded-md hover:bg-blue-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
        @click="resetAllToProfile"
      >
        {{ $t("recipes.search.resetAllToProfile") }}
      </button>
    </div>

    <!-- Advanced Filters -->
    <div class="space-y-4 border-t border-gray-200 pt-6">
      <!-- Filter Dropdowns Row -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <!-- Diet Restrictions -->
        <div>
          <div class="flex items-center gap-2 mb-2">
            <label class="block text-sm font-medium text-gray-700">
              {{ $t('recipes.search.dietRestrictions') }}
            </label>
            <button
              v-if="hasProfileRestrictions && hasRestrictionsOverride"
              type="button"
              class="text-xs text-blue-600 hover:text-blue-800 whitespace-nowrap"
              @click="resetToProfileRestrictions"
            >
              {{ $t("recipes.search.resetToProfile") }}
            </button>
          </div>
          <SingleSelectDropdown
            id="search-dietary-restrictions"
            v-model="localSearchParams.restriction"
            :options="dietaryRestrictionOptions"
            :placeholder="$t('recipes.search.selectDietRestrictions')"
            @open="handleDropdownOpen('search-dietary-restrictions')"
            @close="handleDropdownClose('search-dietary-restrictions')"
          />
        </div>

        <!-- Cuisine Types -->
        <div>
          <div class="flex items-center gap-2 mb-2">
            <label class="block text-sm font-medium text-gray-700">
              {{ $t('recipes.search.cuisine') }}
            </label>
            <button
              v-if="hasProfileCuisines && hasCuisinesOverride"
              type="button"
              class="text-xs text-blue-600 hover:text-blue-800 whitespace-nowrap"
              @click="resetToProfileCuisines"
            >
              {{ $t("recipes.search.resetToProfile") }}
            </button>
          </div>
          <MultiSelectDropdown
            id="search-cuisines"
            v-model="localSearchParams.preferredCuisines"
            :options="cuisineTypeOptions"
            :placeholder="$t('recipes.search.selectCuisines')"
            @open="handleDropdownOpen('search-cuisines')"
            @close="handleDropdownClose('search-cuisines')"
          />
        </div>

        <!-- Intolerances -->
        <div>
          <div class="flex items-center gap-2 mb-2">
            <label class="block text-sm font-medium text-gray-700">
              {{ $t('recipes.search.intolerances') }}
            </label>
            <button
              v-if="hasProfileAllergies && hasAllergiesOverride"
              type="button"
              class="text-xs text-blue-600 hover:text-blue-800 whitespace-nowrap"
              @click="resetToProfileAllergies"
            >
              {{ $t("recipes.search.resetToProfile") }}
            </button>
          </div>
          <MultiSelectDropdown
            id="search-allergies"
            v-model="localSearchParams.allergies"
            :options="allergyOptions"
            :placeholder="$t('recipes.search.selectIntolerances')"
            @open="handleDropdownOpen('search-allergies')"
            @close="handleDropdownClose('search-allergies')"
          />
        </div>
      </div>

      <!-- Include Ingredients -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">
          {{ $t("recipes.search.includeIngredients") }}
        </label>
        <div class="flex flex-wrap gap-2 mb-2">
          <span
            v-for="(ingredient, index) in localSearchParams.includeIngredients"
            :key="index"
            class="inline-flex items-center px-3 py-1 rounded-full text-sm bg-blue-100 text-blue-800"
          >
            {{ ingredient }}
            <button
              type="button"
              class="ml-2 text-blue-600 hover:text-blue-800"
              @click="removeIncludeIngredient(index)"
            >
              ×
            </button>
          </span>
        </div>
        <div class="flex gap-2">
          <input
            v-model="newIncludeIngredient"
            type="text"
            class="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
            :placeholder="$t('recipes.search.addIngredient')"
            @keyup.enter="addIncludeIngredient"
          />
          <button
            type="button"
            class="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
            @click="addIncludeIngredient"
          >
            {{ $t("common.add") }}
          </button>
        </div>
      </div>

      <!-- Exclude Ingredients -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">
          {{ $t("recipes.search.excludeIngredients") }}
        </label>
        <div class="flex flex-wrap gap-2 mb-2">
          <span
            v-for="(ingredient, index) in localSearchParams.excludeIngredients"
            :key="index"
            class="inline-flex items-center px-3 py-1 rounded-full text-sm bg-red-100 text-red-800"
          >
            {{ ingredient }}
            <button
              type="button"
              class="ml-2 text-red-600 hover:text-red-800"
              @click="removeExcludeIngredient(index)"
            >
              ×
            </button>
          </span>
        </div>
        <div class="flex gap-2">
          <input
            v-model="newExcludeIngredient"
            type="text"
            class="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
            :placeholder="$t('recipes.search.addIngredient')"
            @keyup.enter="addExcludeIngredient"
          />
          <button
            type="button"
            class="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
            @click="addExcludeIngredient"
          >
            {{ $t("common.add") }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import MultiSelectDropdown from "@/components/common/MultiSelectDropdown.vue";
import SingleSelectDropdown from "@/components/common/SingleSelectDropdown.vue";
import { userService } from "@/services/userService";
import {
  getDietaryRestrictions,
  getAllergies,
  getCuisineTypes,
} from "@/utils/localeConfig";

const { t, te } = useI18n();

const props = defineProps({
  searchParams: {
    type: Object,
    default: () => ({}),
  },
  isLoading: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["search", "clear"]);

const profilePreferences = ref({
  restriction: null,
  allergies: [],
  preferredCuisines: [],
});

const localSearchParams = ref({
  query: "",
  restriction: null,
  allergies: [],
  preferredCuisines: [],
  includeIngredients: [],
  excludeIngredients: [],
});

const newIncludeIngredient = ref("");
const newExcludeIngredient = ref("");
const openDropdownId = ref(null);

const dietaryRestrictionOptions = computed(() => {
  try {
    const mapping = {
      VEGETARIAN: "vegetarian",
      VEGAN: "vegan",
      KETOGENIC: "ketogenic",
      GLUTEN_FREE: "gluten free",
      PESCETARIAN: "pescetarian",
      PALEO: "paleo",
    };
    const restrictions = getDietaryRestrictions();
    if (!restrictions || restrictions.length === 0) {
      return [];
    }
    return restrictions.map((restriction) => {
      const key = `constants.dietaryRestrictions.${restriction.code}`;
      return {
        value: restriction.code,
        label: te(key) ? t(key) : restriction.name,
        spoonacularValue: mapping[restriction.code] || restriction.code.toLowerCase().replace(/_/g, " "),
      };
    });
  } catch (error) {
    console.error("Error computing dietary restriction options:", error);
    return [];
  }
});

const allergyOptions = computed(() => {
  try {
    const mapping = {
      DAIRY: "dairy",
      EGG: "egg",
      GLUTEN: "gluten",
      GRAIN: "grain",
      PEANUT: "peanut",
      SEAFOOD: "seafood",
      SESAME: "sesame",
      SHELLFISH: "shellfish",
      SOY: "soy",
      SULFITE: "sulfite",
      TREE_NUT: "tree nut",
      WHEAT: "wheat",
    };
    const allergies = getAllergies();
    if (!allergies || allergies.length === 0) {
      return [];
    }
    return allergies.map((allergy) => {
      const key = `constants.allergies.${allergy.code}`;
      return {
        value: allergy.code,
        label: te(key) ? t(key) : allergy.name,
        spoonacularValue: mapping[allergy.code] || allergy.code.toLowerCase().replace(/_/g, " "),
      };
    });
  } catch (error) {
    console.error("Error computing allergy options:", error);
    return [];
  }
});

const cuisineTypeOptions = computed(() => {
  try {
    const mapping = {
      AFRICAN: "african",
      AMERICAN: "american",
      BRITISH: "british",
      CAJUN: "cajun",
      CARIBBEAN: "caribbean",
      CHINESE: "chinese",
      EASTERN_EUROPEAN: "eastern european",
      FRENCH: "french",
      GERMAN: "german",
      GREEK: "greek",
      INDIAN: "indian",
      IRISH: "irish",
      ITALIAN: "italian",
      JAPANESE: "japanese",
      JEWISH: "jewish",
      KOREAN: "korean",
      LATIN_AMERICAN: "latin american",
      MEDITERRANEAN: "mediterranean",
      MEXICAN: "mexican",
      MIDDLE_EASTERN: "middle eastern",
      NORDIC: "nordic",
      SOUTHERN: "southern",
      SPANISH: "spanish",
      THAI: "thai",
      VIETNAMESE: "vietnamese",
    };
    const cuisines = getCuisineTypes();
    if (!cuisines || cuisines.length === 0) {
      return [];
    }
    return cuisines.map((cuisine) => {
      const key = `constants.cuisineTypes.${cuisine.code}`;
      return {
        value: cuisine.code,
        label: te(key) ? t(key) : cuisine.name,
        spoonacularValue: mapping[cuisine.code] || cuisine.code.toLowerCase().replace(/_/g, " "),
      };
    });
  } catch (error) {
    console.error("Error computing cuisine type options:", error);
    return [];
  }
});

const hasProfilePreferences = computed(() => {
  return (
    profilePreferences.value.restriction != null ||
    profilePreferences.value.allergies?.length > 0 ||
    profilePreferences.value.preferredCuisines?.length > 0
  );
});

const hasProfileRestrictions = computed(() => {
  return profilePreferences.value.restriction != null;
});

const hasProfileAllergies = computed(() => {
  return profilePreferences.value.allergies?.length > 0;
});

const hasProfileCuisines = computed(() => {
  return profilePreferences.value.preferredCuisines?.length > 0;
});

const hasRestrictionsOverride = computed(() => {
  const current = localSearchParams.value.restriction;
  const profile = profilePreferences.value.restriction;
  return current !== profile;
});

const hasAllergiesOverride = computed(() => {
  const current = [...(localSearchParams.value.allergies || [])].sort();
  const profile = [...(profilePreferences.value.allergies || [])].sort();
  return JSON.stringify(current) !== JSON.stringify(profile);
});

const hasCuisinesOverride = computed(() => {
  const current = [...(localSearchParams.value.preferredCuisines || [])].sort();
  const profile = [...(profilePreferences.value.preferredCuisines || [])].sort();
  return JSON.stringify(current) !== JSON.stringify(profile);
});

const hasOverriddenPreferences = computed(() => {
  return hasRestrictionsOverride.value || hasAllergiesOverride.value || hasCuisinesOverride.value;
});

const hasActiveFilters = computed(() => {
  return (
    localSearchParams.value.query ||
    localSearchParams.value.restriction != null ||
    localSearchParams.value.allergies?.length > 0 ||
    localSearchParams.value.preferredCuisines?.length > 0 ||
    localSearchParams.value.includeIngredients?.length > 0 ||
    localSearchParams.value.excludeIngredients?.length > 0
  );
});

const loadProfilePreferences = async () => {
  try {
    const preferences = await userService.getDietaryPreferences();
    profilePreferences.value = {
      restriction: preferences.restriction || null,
      allergies: preferences.allergies || [],
      preferredCuisines: preferences.preferredCuisines || [],
    };

    // Set defaults from profile if no search params provided
    if (!props.searchParams || Object.keys(props.searchParams).length === 0) {
      localSearchParams.value = {
        query: "",
        restriction: profilePreferences.value.restriction,
        allergies: [...profilePreferences.value.allergies],
        preferredCuisines: [...profilePreferences.value.preferredCuisines],
        includeIngredients: [],
        excludeIngredients: [],
      };
    }
  } catch (error) {
    console.error("Failed to load profile preferences:", error);
  }
};

watch(
  () => props.searchParams,
  (newParams) => {
    // Only sync from props if it contains enum codes (restriction, allergies, preferredCuisines)
    // This prevents clearing local state when props contain Spoonacular values (diet, intolerances, cuisine)
    if (newParams && Object.keys(newParams).length > 0) {
      // Check if this is a "clear" operation (empty object) or contains enum codes
      const hasEnumCodes = 
        (typeof newParams.restriction === 'string' || newParams.restriction === null) ||
        Array.isArray(newParams.allergies) ||
        Array.isArray(newParams.preferredCuisines);
      
      // Only update if it's a clear operation or contains enum codes
      // Don't update if it only contains Spoonacular values (from search)
      if (Object.keys(newParams).length === 0 || hasEnumCodes) {
        localSearchParams.value = {
          query: newParams.query || "",
          restriction: newParams.restriction || null,
          allergies: newParams.allergies || [],
          preferredCuisines: newParams.preferredCuisines || [],
          includeIngredients: newParams.includeIngredients || [],
          excludeIngredients: newParams.excludeIngredients || [],
        };
      }
    }
  },
  { deep: true },
);

onMounted(() => {
  loadProfilePreferences();
});

const getSpoonacularValue = (code, type) => {
  let option;
  if (type === "diet") {
    option = dietaryRestrictionOptions.value.find((opt) => opt.value === code);
  } else if (type === "allergy") {
    option = allergyOptions.value.find((opt) => opt.value === code);
  } else if (type === "cuisine") {
    option = cuisineTypeOptions.value.find((opt) => opt.value === code);
  }
  return option?.spoonacularValue || (code ? code.toLowerCase().replace(/_/g, " ") : null);
};

const handleSearch = () => {
  const params = {};

  // Only add query if it's not empty
  if (localSearchParams.value.query && localSearchParams.value.query.trim()) {
    params.query = localSearchParams.value.query.trim();
  }

  // Convert enum code to Spoonacular value
  // Only add if restriction exists and has valid value
  if (localSearchParams.value.restriction) {
    const spoonacularValue = getSpoonacularValue(localSearchParams.value.restriction, "diet");
    if (spoonacularValue && spoonacularValue.trim()) {
      params.diet = spoonacularValue.trim();
    }
  }

  if (
    Array.isArray(localSearchParams.value.allergies) &&
    localSearchParams.value.allergies.length > 0
  ) {
    const intolerances = localSearchParams.value.allergies
      .map((code) => {
        if (!code) return null;
        const value = getSpoonacularValue(code, "allergy");
        return value && value.trim() ? value.trim() : null;
      })
      .filter((val) => val !== null);
    
    if (intolerances.length > 0) {
      params.intolerances = intolerances;
    }
  }

  if (
    Array.isArray(localSearchParams.value.preferredCuisines) &&
    localSearchParams.value.preferredCuisines.length > 0
  ) {
    const cuisines = localSearchParams.value.preferredCuisines
      .map((code) => {
        if (!code) return null;
        const value = getSpoonacularValue(code, "cuisine");
        return value && value.trim() ? value.trim() : null;
      })
      .filter((val) => val !== null);
    
    if (cuisines.length > 0) {
      params.cuisine = cuisines;
    }
  }

  if (
    Array.isArray(localSearchParams.value.includeIngredients) &&
    localSearchParams.value.includeIngredients.length > 0
  ) {
    const ingredients = localSearchParams.value.includeIngredients
      .map((ing) => ing?.trim())
      .filter((ing) => ing && ing.length > 0);
    
    if (ingredients.length > 0) {
      params.includeIngredients = ingredients;
    }
  }

  if (
    Array.isArray(localSearchParams.value.excludeIngredients) &&
    localSearchParams.value.excludeIngredients.length > 0
  ) {
    const ingredients = localSearchParams.value.excludeIngredients
      .map((ing) => ing?.trim())
      .filter((ing) => ing && ing.length > 0);
    
    if (ingredients.length > 0) {
      params.excludeIngredients = ingredients;
    }
  }

  emit("search", params);
};

const handleClear = () => {
  localSearchParams.value = {
    query: "",
    restriction: profilePreferences.value.restriction,
    allergies: [...profilePreferences.value.allergies],
    preferredCuisines: [...profilePreferences.value.preferredCuisines],
    includeIngredients: [],
    excludeIngredients: [],
  };
  newIncludeIngredient.value = "";
  newExcludeIngredient.value = "";
  emit("clear");
};

const resetAllToProfile = () => {
  localSearchParams.value = {
    query: "",
    restriction: profilePreferences.value.restriction || null,
    allergies: [...(profilePreferences.value.allergies || [])],
    preferredCuisines: [...(profilePreferences.value.preferredCuisines || [])],
    includeIngredients: [],
    excludeIngredients: [],
  };
  newIncludeIngredient.value = "";
  newExcludeIngredient.value = "";
};

const resetToProfileRestrictions = () => {
  localSearchParams.value.restriction = profilePreferences.value.restriction || null;
};

const resetToProfileAllergies = () => {
  localSearchParams.value.allergies = [...(profilePreferences.value.allergies || [])];
};

const resetToProfileCuisines = () => {
  localSearchParams.value.preferredCuisines = [
    ...(profilePreferences.value.preferredCuisines || []),
  ];
};

const addIncludeIngredient = () => {
  const ingredient = newIncludeIngredient.value.trim();
  if (ingredient && !localSearchParams.value.includeIngredients.includes(ingredient)) {
    localSearchParams.value.includeIngredients.push(ingredient);
    newIncludeIngredient.value = "";
  }
};

const removeIncludeIngredient = (index) => {
  localSearchParams.value.includeIngredients.splice(index, 1);
};

const addExcludeIngredient = () => {
  const ingredient = newExcludeIngredient.value.trim();
  if (ingredient && !localSearchParams.value.excludeIngredients.includes(ingredient)) {
    localSearchParams.value.excludeIngredients.push(ingredient);
    newExcludeIngredient.value = "";
  }
};

const removeExcludeIngredient = (index) => {
  localSearchParams.value.excludeIngredients.splice(index, 1);
};

const handleDropdownOpen = (id) => {
  if (openDropdownId.value && openDropdownId.value !== id) {
    // Close other dropdowns by dispatching a custom event
    const event = new CustomEvent("close-dropdown", { detail: { id: openDropdownId.value } });
    document.dispatchEvent(event);
  }
  openDropdownId.value = id;
};

const handleDropdownClose = (id) => {
  if (openDropdownId.value === id) {
    openDropdownId.value = null;
  }
};
</script>
