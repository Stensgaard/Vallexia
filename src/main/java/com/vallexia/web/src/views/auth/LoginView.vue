<template>
  <AuthLayout>
    <form @submit.prevent="handleSubmit" class="space-y-6">
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
        label="Username or Email"
        placeholder="Enter your username or email"
        :error="errors.usernameOrEmail"
        :required="true"
        :disabled="authStore.isLoading"
      />

      <!-- Password Field -->
      <FormInput
        id="password"
        v-model="form.password"
        type="password"
        label="Password"
        placeholder="Enter your password"
        :error="errors.password"
        :required="true"
        :disabled="authStore.isLoading"
      />

      <!-- Remember Me -->
      <div class="flex items-center justify-between">
        <div class="flex items-center">
          <input
            id="rememberMe"
            v-model="form.rememberMe"
            type="checkbox"
            class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
            :disabled="authStore.isLoading"
          />
          <label for="rememberMe" class="ml-2 block text-sm text-gray-900">
            Remember me
          </label>
        </div>

        <div class="text-sm">
          <a href="#" class="font-medium text-blue-600 hover:text-blue-500">
            Forgot your password?
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
          <LoadingSpinner v-if="authStore.isLoading" size="small" color="white" />
          <span v-else>Sign in</span>
        </button>
      </div>
  </form>
  </AuthLayout>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AuthLayout from '@/views/auth/AuthLayout.vue'
import FormInput from '@/components/common/FormInput.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import ErrorMessage from '@/components/common/ErrorMessage.vue'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  usernameOrEmail: '',
  password: '',
  rememberMe: false
})

const errors = reactive({
  usernameOrEmail: '',
  password: ''
})

const isFormValid = computed(() => {
  return form.usernameOrEmail.trim() && form.password.trim()
})

const validateForm = () => {
  // Clear previous errors
  errors.usernameOrEmail = ''
  errors.password = ''

  let isValid = true

  if (!form.usernameOrEmail.trim()) {
    errors.usernameOrEmail = 'Username or email is required'
    isValid = false
  }

  if (!form.password.trim()) {
    errors.password = 'Password is required'
    isValid = false
  }

  return isValid
}

const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  try {
    await authStore.login(form)
    router.push('/dashboard')
  } catch (error) {
    // Error is handled by the store and displayed in the template
  }
}

onMounted(() => {
  // Clear any previous errors when component mounts
  authStore.clearError()
})
</script>
