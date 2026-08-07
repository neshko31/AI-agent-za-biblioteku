<template>
  <div class="page-center">
    <div class="card reg-card">
      <h1>Uplata članarine</h1>
      <p class="subtitle">Odaberite način uplate članarine:</p>

      <div class="radio-option" @click="method = 'FIZICKI'">
        <div class="radio-circle" :class="{ active: method === 'FIZICKI' }"></div>
        <span class="radio-label">Fizički – u biblioteci koju ste odabrali</span>
      </div>

      <div class="radio-option" @click="method = 'ONLINE'">
        <div class="radio-circle" :class="{ active: method === 'ONLINE' }"></div>
        <span class="radio-label">Online plaćanje platnom karticom</span>
      </div>

      <transition name="slide">
        <div v-if="method === 'ONLINE'" class="card-form">
          <div class="form-group">
            <label>Broj kartice</label>
            <input v-model="card.broj" type="text" maxlength="19"
                   placeholder="0000 0000 0000 0000" @input="formatCard" />
          </div>
          <div class="row2">
            <div class="form-group">
              <label>Datum važenja</label>
              <input v-model="card.datum" type="text" maxlength="5" placeholder="MM/YY" />
            </div>
            <div class="form-group">
              <label>CVV</label>
              <input v-model="card.cvv" type="text" maxlength="3" placeholder="123" />
            </div>
          </div>
        </div>
      </transition>

      <p v-if="error" class="error-msg">{{ error }}</p>

      <button class="btn-primary" @click="handleFinish" :disabled="loading">
        {{ loading ? 'Učitavanje…' : 'Registracija' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stroage/auth.js'
import { authApi } from '../services/api.js'

const router    = useRouter()
const authStore = useAuthStore()

const method  = ref('FIZICKI')
const card    = ref({ broj: '', datum: '', cvv: '' })
const error   = ref('')
const loading = ref(false)

function formatCard() {
  card.value.broj = card.value.broj.replace(/\D/g, '').replace(/(.{4})/g, '$1 ').trim()
}

async function handleFinish() {
  if (method.value === 'ONLINE') {
    if (!card.value.broj || !card.value.datum || !card.value.cvv) {
      error.value = 'Popunite sve podatke o kartici'
      return
    }
  }

  error.value   = ''
  loading.value = true
  try {
    const payload = {
      nacinUplate:  method.value,
      brojKartice:  card.value.broj || null,
      datumVazenja: card.value.datum || null,
      cvv:          card.value.cvv || null,
    }
    const res = await authApi.registerStep3(authStore.regJmbg, payload)
    authStore.setAuth(res.data)
    router.push('/register/genres')
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri registraciji'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.reg-card { width: 100%; max-width: 600px; }
.subtitle  { text-align: center; color: var(--text-mid); margin-bottom: 1.5rem; font-size: 0.95rem; }
.card-form { margin-top: 1rem; padding: 1rem 0; }
.row2 { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-top: 0.75rem; }

.slide-enter-active, .slide-leave-active { transition: all 0.25s ease; }
.slide-enter-from, .slide-leave-to       { opacity: 0; transform: translateY(-8px); }
</style>
