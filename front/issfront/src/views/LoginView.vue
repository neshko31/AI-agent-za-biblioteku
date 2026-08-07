<template>
  <div class="page-center">
    <div class="card login-card">
      <h1>Ulogujte se</h1>

      <div class="form-group">
        <label>Email</label>
        <input v-model="form.email" type="email" autocomplete="email" />
      </div>

      <div class="form-group" style="margin-top: 1rem">
        <label>Lozinka</label>
        <input v-model="form.password" type="password" autocomplete="current-password" />
      </div>

      <p v-if="error" class="error-msg">{{ error }}</p>

      <button class="btn-primary" @click="handleLogin" :disabled="loading">
        {{ loading ? 'Učitavanje…' : 'Logovanje' }}
      </button>

      <p class="register-link">
        Nemate nalog?
        <RouterLink to="/register">Registrujte se</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stroage/auth.js'
import { authApi } from '../services/api.js'

const router = useRouter()
const auth   = useAuthStore()

const form    = ref({ email: '', password: '' })
const error   = ref('')
const loading = ref(false)

async function handleLogin() {

  error.value = ''
  loading.value = true

  try {
    const res = await authApi.login(form.value)

    auth.setAuth(res.data)
    const role = res.data.role
    if (role === 'MENADZER') {
      router.push('/menadzer/dashboard')
    } else {
      router.push('/profile')
    }

  } catch (e) {
    error.value = e.response?.data?.message || 'Login failed'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-card { width: 100%; max-width: 460px; }
.register-link {
  text-align: center;
  margin-top: 1.2rem;
  font-size: 0.9rem;
  color: var(--text-mid);
}
.register-link a { color: var(--btn-primary); font-weight: 600; text-decoration: none; }
.register-link a:hover { text-decoration: underline; }
</style>
