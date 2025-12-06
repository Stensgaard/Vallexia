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

      <!-- Username/Email Field -->
      <FormInput
        id="usernameOrEmail"
        v-model="form.usernameOrEmail"
        type="text"
        :label="$t('auth.login.usernameOrEmail')"
        :placeholder="$t('auth.login.usernameOrEmailPlaceholder')"
        :error="errors.usernameOrEmail"
        :required="true"
        :disabled="authStore.isLoading"
      />

      <!-- Password Field -->
      <FormInput
        id="password"
        v-model="form.password"
        type="password"
        :label="$t('auth.login.password')"
        :placeholder="$t('auth.login.passwordPlaceholder')"
        :error="errors.password"
        :required="true"
        :disabled="authStore.isLoading"
      />

      <!-- Forgot Password -->
      <div class="flex items-center justify-end">
        <div class="text-sm">
          <a href="#" class="font-medium text-blue-600 hover:text-blue-500">
            {{ $t("auth.login.forgotPassword") }}
          </a>
        </div>
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
          <span v-else>{{ $t("auth.login.signIn") }}</span>
        </button>
      </div>
    </form>
  </AuthLayout>
</template>

<script setup>
import { reactive, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { useAuthStore } from "@/stores/auth";
import AuthLayout from "@/views/auth/AuthLayout.vue";
import FormInput from "@/components/common/FormInput.vue";
import LoadingSpinner from "@/components/common/LoadingSpinner.vue";
import ErrorMessage from "@/components/common/ErrorMessage.vue";

const { t } = useI18n();

const router = useRouter();
const authStore = useAuthStore();

const form = reactive({
  usernameOrEmail: "",
  password: "",
});

const errors = reactive({
  usernameOrEmail: "",
  password: "",
});

const isFormValid = computed(() => {
  return form.usernameOrEmail.trim() && form.password.trim();
});

const validateForm = () => {
  // Clear previous errors
  errors.usernameOrEmail = "";
  errors.password = "";

  let isValid = true;

  if (!form.usernameOrEmail.trim()) {
    errors.usernameOrEmail = t("auth.validation.usernameOrEmailRequired");
    isValid = false;
  }

  if (!form.password.trim()) {
    errors.password = t("auth.validation.passwordRequired");
    isValid = false;
  }

  return isValid;
};

const handleSubmit = async () => {
  if (!validateForm()) {
    return;
  }

  try {
    await authStore.login(form);
    router.push("/dashboard");
  } catch (error) {
    // Error is handled by the store and displayed in the template
  }
};

onMounted(() => {
  // Clear any previous errors when component mounts
  authStore.clearError();
});
</script>
