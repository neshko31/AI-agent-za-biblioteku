<template>
  <div class="page-center">
    <div class="card reg-card">
      <h1>Registrujte se</h1>

      <div class="form-group">
        <label>Odaberite biblioteku*</label>
        <select v-model="form.libraryBid">
          <option value="" disabled>— odaberite —</option>
          <option v-for="lib in libraries" :key="lib.bid" :value="lib.bid">
            {{ lib.name }}
          </option>
        </select>
      </div>

      <div class="row2">
        <div class="form-group">
          <label>Ime i Prezime*</label>
          <input v-model="form.fullName" type="text" placeholder="Ime Prezime" />
        </div>
        <div class="form-group">
          <label>Tip članstva*</label>
          <select v-model="form.tipPretplate">
            <option value="" disabled>— odaberite —</option>
            <option value="MESECNA">Mesečna</option>
            <option value="GODISNJA">Godišnja</option>
          </select>
        </div>
      </div>


      <div class="row2">
        <div class="form-group">
          <label>JMBG*</label>
          <input v-model="form.jmbg" type="text" maxlength="13" />
        </div>
        <div class="form-group">
          <label>Lozinka (min 8 znakova)*</label>
          <input v-model="form.password" type="password" />
        </div>
      </div>

      <div class="row3">
        <div class="form-group">
          <label>Email*</label>
          <input v-model="form.email" type="email" />
        </div>
        <div class="form-group">
          <label>Broj telefona*</label>
          <input v-model="form.phone" type="tel" />
        </div>
        <div class="form-group">
          <label>Datum rođenja*</label>
          <input v-model="form.dateOfBirth" type="date" />
        </div>
      </div>

      <div v-if="error">
        <ul v-if="Array.isArray(error)" class="error-msg">
          <li v-for="err in error" :key="err">{{ err }}</li>
        </ul>
        <p v-else class="error-msg">{{ error }}</p>
      </div>

      <button class="btn-primary" @click="handleNext" :disabled="loading">
        {{ loading ? 'Učitavanje…' : 'Nastavite' }}
      </button>

      <p class="login-link">
        Već imate nalog?
        <RouterLink to="/login">Ulogujte se</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stroage/auth.js'
import { authApi, publicApi } from '../services/api.js'

const router    = useRouter()
const authStore = useAuthStore()

const libraries = ref([])
const error     = ref('')
const loading   = ref(false)

const form = ref({
  jmbg: '', fullName: '', email: '', phone: '',
  dateOfBirth: '', password: '', libraryBid: '', tipPretplate: ''
})

onMounted(async () => {
  try {
    const res = await publicApi.getLibraries()
    libraries.value = res.data
  } catch (e) {
    console.error(e.response?.status, e.response?.data)
  }
})


async function handleNext() {
  error.value = ''

  const [firstName, ...rest] = form.value.fullName.trim().split(' ')
  const lastName = rest.join(' ')

  if (!firstName || !lastName) {
    error.value = 'Unesite i ime i prezime'
    return
  }

  loading.value = true
  try {
    const payload = {
      jmbg:          form.value.jmbg,
      firstName,
      lastName,
      email:         form.value.email,
      phone:         form.value.phone,
      dateOfBirth:   form.value.dateOfBirth,
      password:      form.value.password,
      libraryBid:    form.value.libraryBid,
      tipPretplate:  form.value.tipPretplate,
    }
    const res = await authApi.registerStep1(payload)
    authStore.setAuth(res.data)
    authStore.setRegJmbg(res.data.jmbg)
    router.push('/register/step2')
  } catch (e) {
      if (Array.isArray(e.response?.data?.errors)) {
        error.value = e.response.data.errors
      } else {
        error.value = e.response?.data?.message || 'Greška pri registraciji'
      }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.reg-card { width: 100%; max-width: 700px; }
.row2 { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-top: 1rem; }
.row3 { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 1rem; margin-top: 1rem; }
.login-link {
  text-align: center; margin-top: 1rem;
  font-size: 0.9rem; color: var(--text-mid);
}
.login-link a { color: var(--btn-primary); font-weight: 600; text-decoration: none; }
</style>
