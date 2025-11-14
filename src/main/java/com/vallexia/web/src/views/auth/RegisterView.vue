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

      <!-- Username Field -->
      <FormInput
        id="username"
        v-model="form.username"
        type="text"
        label="Username"
        placeholder="Choose a username"
        :error="errors.username"
        :required="true"
        :disabled="authStore.isLoading"
        hint="3-20 characters, letters and numbers only"
      />

      <!-- Email Field -->
      <FormInput
        id="email"
        v-model="form.email"
        type="email"
        label="Email Address"
        placeholder="Enter your email address"
        :error="errors.email"
        :required="true"
        :disabled="authStore.isLoading"
      />

      <!-- Password Field -->
      <FormInput
        id="password"
        v-model="form.password"
        type="password"
        label="Password"
        placeholder="Create a strong password"
        :error="errors.password"
        :required="true"
        :disabled="authStore.isLoading"
        hint="At least 8 characters with uppercase, lowercase, number, and special character"
      />

      <!-- Confirm Password Field -->
      <FormInput
        id="confirmPassword"
        v-model="form.confirmPassword"
        type="password"
        label="Confirm Password"
        placeholder="Confirm your password"
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
          I agree to the
          <a href="#" class="text-blue-600 hover:text-blue-500">Terms of Service</a>
          and
          <a href="#" class="text-blue-600 hover:text-blue-500">Privacy Policy</a>
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
          <LoadingSpinner v-if="authStore.isLoading" size="small" color="white" />
          <span v-else>Create Account</span>
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
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  acceptTerms: false
})

const errors = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  acceptTerms: ''
})

const isFormValid = computed(() => {
  return form.username.trim() &&
         form.email.trim() &&
         form.password.trim() &&
         form.confirmPassword.trim() &&
         form.acceptTerms
})

const validateForm = () => {
  // Clear previous errors
  Object.keys(errors).forEach(key => {
    errors[key] = ''
  })

  let isValid = true

  // Username validation
  if (!form.username.trim()) {
    errors.username = 'Username is required'
    isValid = false
  } else if (form.username.length < 3 || form.username.length > 20) {
    errors.username = 'Username must be between 3 and 20 characters'
    isValid = false
  } else if (!/^[a-zA-Z0-9]+$/.test(form.username)) {
    errors.username = 'Username can only contain letters and numbers'
    isValid = false
  }

  // Email validation
  if (!form.email.trim()) {
    errors.email = 'Email is required'
    isValid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = 'Please enter a valid email address'
    isValid = false
  }

  // Password validation
  if (!form.password.trim()) {
    errors.password = 'Password is required'
    isValid = false
  } else if (form.password.length < 8) {
    errors.password = 'Password must be at least 8 characters'
    isValid = false
  } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])/.test(form.password)) {
    errors.password = 'Password must contain uppercase, lowercase, number, and special character'
    isValid = false
  }

  // Confirm password validation
  if (!form.confirmPassword.trim()) {
    errors.confirmPassword = 'Please confirm your password'
    isValid = false
  } else if (form.password !== form.confirmPassword) {
    errors.confirmPassword = 'Passwords do not match'
    isValid = false
  }

  // Terms acceptance validation
  if (!form.acceptTerms) {
    errors.acceptTerms = 'You must accept the terms and conditions'
    isValid = false
  }

  return isValid
}

const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  try {
    await authStore.register(form)
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
