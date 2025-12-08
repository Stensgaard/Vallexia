<template>
  <AuthLayout>
    <form class="space-y-6" @submit.prevent="handleSubmit">
      <!-- Global Error Message -->
      <ErrorMessage
        v-if="authStore.error"
        :show="true"
        type="error"
        :message="authStore.error"
        :dismissible="true"
        @dismiss="authStore.clearError"
      />

      <!-- Username Field -->
      <FormInput
        id="username"
        v-model="form.username"
        type="text"
        :label="$t('auth.register.username')"
        :placeholder="$t('auth.register.usernamePlaceholder')"
        :error="errors.username"
        :required="true"
        :disabled="authStore.isLoading"
        :hint="$t('auth.register.usernameHint')"
      />

      <!-- Email Field -->
      <FormInput
        id="email"
        v-model="form.email"
        type="email"
        :label="$t('auth.register.email')"
        :placeholder="$t('auth.register.emailPlaceholder')"
        :error="errors.email"
        :required="true"
        :disabled="authStore.isLoading"
      />

      <!-- Country Field -->
      <div>
        <label for="country" class="form-label">
          {{ $t("auth.register.country") }}
          <span class="text-red-500">*</span>
        </label>
        <select
          id="country"
          v-model="form.country"
          :disabled="authStore.isLoading"
          :class="['form-input', errors.country ? 'form-input-error' : '']"
          required
        >
          <option value="">{{ $t("auth.register.countryPlaceholder") }}</option>
          <option
            v-for="country in countryOptions"
            :key="country.code"
            :value="country.code"
          >
            {{ country.name }}
          </option>
        </select>
        <div v-if="errors.country" class="form-error">
          {{ errors.country }}
        </div>
      </div>

      <!-- Password Field -->
      <FormInput
        id="password"
        v-model="form.password"
        type="password"
        :label="$t('auth.register.password')"
        :placeholder="$t('auth.register.passwordPlaceholder')"
        :error="errors.password"
        :required="true"
        :disabled="authStore.isLoading"
        :hint="$t('auth.register.passwordHint')"
      />

      <!-- Confirm Password Field -->
      <FormInput
        id="confirmPassword"
        v-model="form.confirmPassword"
        type="password"
        :label="$t('auth.register.confirmPassword')"
        :placeholder="$t('auth.register.confirmPasswordPlaceholder')"
        :error="errors.confirmPassword"
        :required="true"
        :disabled="authStore.isLoading"
      />

      <!-- Terms and Conditions -->
      <div class="flex items-center">
        <input
          id="acceptTerms"
          v-model="form.acceptTerms"
          type="checkbox"
          class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
          :disabled="authStore.isLoading"
        />
        <label for="acceptTerms" class="ml-2 block text-sm text-gray-900">
          {{ $t("auth.register.acceptTerms") }}
          <a href="#" class="text-blue-600 hover:text-blue-500">{{
            $t("auth.register.termsOfService")
          }}</a>
          {{ $t("auth.register.and") }}
          <a href="#" class="text-blue-600 hover:text-blue-500">{{
            $t("auth.register.privacyPolicy")
          }}</a>
        </label>
      </div>
      <div v-if="errors.acceptTerms" class="text-sm text-red-600">
        {{ errors.acceptTerms }}
      </div>

      <!-- Submit Button -->
      <div>
        <button
          type="submit"
          :disabled="authStore.isLoading || !isFormValid"
          class="btn btn-primary w-full"
        >
          <LoadingSpinner
            v-if="authStore.isLoading"
            size="small"
            color="white"
          />
          <span v-else>{{ $t("auth.register.createAccount") }}</span>
        </button>
      </div>
    </form>
  </AuthLayout>
</template>

<script setup>
import { reactive, computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { useAuthStore } from "@/stores/auth";
import { getCountries, ensureLocaleConfigLoaded } from "@/utils/localeConfig";
import AuthLayout from "@/views/auth/AuthLayout.vue";
import FormInput from "@/components/common/FormInput.vue";
import LoadingSpinner from "@/components/common/LoadingSpinner.vue";
import ErrorMessage from "@/components/common/ErrorMessage.vue";

const { t } = useI18n();

const router = useRouter();
const authStore = useAuthStore();

const countryOptions = ref([]);

const form = reactive({
  username: "",
  email: "",
  country: "",
  password: "",
  confirmPassword: "",
  acceptTerms: false,
});

const errors = reactive({
  username: "",
  email: "",
  country: "",
  password: "",
  confirmPassword: "",
  acceptTerms: "",
});

const isFormValid = computed(() => {
  return (
    form.username.trim() &&
    form.email.trim() &&
    form.country.trim() &&
    form.password.trim() &&
    form.confirmPassword.trim() &&
    form.acceptTerms
  );
});

const validateForm = () => {
  // Clear previous errors
  Object.keys(errors).forEach((key) => {
    errors[key] = "";
  });

  let isValid = true;

  // Username validation
  if (!form.username.trim()) {
    errors.username = t("auth.validation.usernameRequired");
    isValid = false;
  } else if (form.username.length < 3 || form.username.length > 20) {
    errors.username = t("auth.validation.usernameLength");
    isValid = false;
  } else if (!/^[a-zA-Z0-9]+$/.test(form.username)) {
    errors.username = t("auth.validation.usernameFormat");
    isValid = false;
  }

  // Email validation
  if (!form.email.trim()) {
    errors.email = t("auth.validation.emailRequired");
    isValid = false;
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = t("auth.validation.emailInvalid");
    isValid = false;
  }

  // Country validation
  const countryValue = form.country.trim();
  if (countryValue) {
    // Validate country is in available options
    const validCountry = countryOptions.value.some(
      (country) => country.code === countryValue,
    );
    if (!validCountry) {
      errors.country = t("auth.validation.countryInvalid");
      isValid = false;
    }
  } else {
    errors.country = t("auth.validation.countryRequired");
    isValid = false;
  }

  // Password validation
  if (!form.password.trim()) {
    errors.password = t("auth.validation.passwordRequired");
    isValid = false;
  } else if (form.password.length < 8) {
    errors.password = t("auth.validation.passwordLength");
    isValid = false;
  } else if (
    !/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])/.test(form.password)
  ) {
    errors.password = t("auth.validation.passwordComplexity");
    isValid = false;
  }

  // Confirm password validation
  if (!form.confirmPassword.trim()) {
    errors.confirmPassword = t("auth.validation.confirmPasswordRequired");
    isValid = false;
  } else if (form.password !== form.confirmPassword) {
    errors.confirmPassword = t("auth.validation.passwordsDoNotMatch");
    isValid = false;
  }

  // Terms acceptance validation
  if (!form.acceptTerms) {
    errors.acceptTerms = t("auth.validation.termsRequired");
    isValid = false;
  }

  return isValid;
};

const handleSubmit = async () => {
  if (!validateForm()) {
    return;
  }

  try {
    await authStore.register(form);
    router.push("/dashboard");
  } catch (error_) {
    // Error is handled by the store and displayed in the template; rethrow for upstream handlers
    throw error_;
  }
};

onMounted(async () => {
  // Clear any previous errors when component mounts
  authStore.clearError();

  // Load locale config to get countries
  try {
    await ensureLocaleConfigLoaded();
    countryOptions.value = getCountries();
  } catch (error) {
    if (import.meta.env.DEV) {
      // eslint-disable-next-line no-console
      console.error("Failed to load locale config:", error);
    }
  }
});
</script>
