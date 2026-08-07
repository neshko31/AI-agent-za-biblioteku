<template>
  <div class="forma-wrapper">
    <div class="page-header">
      <h1>Dobavljači</h1>
      <p class="subtitle">Izmena dobavljača</p>
    </div>

    <div v-if="loadingPodataka" class="state-msg">Učitavanje...</div>

    <div v-else class="forma-kartica">
      <div v-if="error" class="alert alert--error">{{ error }}</div>
      <div v-if="uspeh" class="alert alert--uspeh">Dobavljač je uspešno izmenjen!</div>

      <div class="form-group">
        <label>Naziv dobavljača</label>
        <input v-model="forma.naziv" type="text" placeholder="Naziv dobavljača" />
      </div>

      <div class="form-group">
        <label>PIB dobavljača</label>
        <input v-model="forma.pib" type="text" placeholder="PIB (samo cifre)" />
      </div>

      <div class="form-group">
        <label>Email dobavljača</label>
        <input v-model="forma.email" type="email" placeholder="email@primer.com" />
      </div>

      <div class="form-group">
        <label>Telefon dobavljača</label>
        <input v-model="forma.tel" type="text" placeholder="011/555-666" />
      </div>

      <!-- URL samo ako je knjižara -->
      <div class="form-group" v-if="jeKnjizara">
        <label>URL online prodavnice</label>
        <input v-model="forma.urlOnlineProdavnice" type="text" placeholder="https://..." />
      </div>

      <div class="forma-akcije">
        <button class="btn-sekundarni" @click="router.back()">Odustani</button>
        <button class="btn-primary" @click="sacuvaj" :disabled="loading">
          {{ loading ? 'Čuvanje...' : 'Sačuvaj izmene' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { dobavljacApi } from '../../services/api.js'

const router = useRouter()
const route = useRoute()
const id = route.params.id

const forma = ref({
  naziv: '',
  pib: '',
  email: '',
  tel: '',
  urlOnlineProdavnice: ''
})

const tipDobavljaca = ref('')
const loadingPodataka = ref(false)
const loading = ref(false)
const error = ref('')
const uspeh = ref(false)

const jeKnjizara = computed(() =>
  tipDobavljaca.value === '01' || tipDobavljaca.value === '11'
)

async function ucitaj() {
  loadingPodataka.value = true
  try {
    const res = await dobavljacApi.jedan(id)
    const d = res.data
    forma.value.naziv = d.naziv
    forma.value.pib = d.pib
    forma.value.email = d.email
    forma.value.tel = d.tel
    forma.value.urlOnlineProdavnice = d.urlOnlineProdavnice || ''
    tipDobavljaca.value = d.tipDobavljaca || ''
  } catch (e) {
    error.value = 'Greška pri učitavanju podataka.'
  } finally {
    loadingPodataka.value = false
  }
}

async function sacuvaj() {
  error.value = ''
  uspeh.value = false
  loading.value = true
  try {
    await dobavljacApi.izmeni(id, forma.value)
    uspeh.value = true
    setTimeout(() => router.push(`/menadzer/dobavljaci/${id}`), 1500)
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri izmeni dobavljača.'
  } finally {
    loading.value = false
  }
}

onMounted(ucitaj)
</script>

<style scoped>
.forma-wrapper {
  width: 100%;
}

.page-header {
  margin-bottom: 2rem;
}

.page-header h1 {
  margin: 0 0 0.25rem;
  font-size: 2rem;
  color: var(--text-h);
}

.subtitle {
  color: var(--text);
  font-size: 0.95rem;
  margin: 0;
}

.forma-kartica {
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.form-group label {
  font-size: 0.9rem;
  color: var(--text);
  font-weight: 500;
}

.form-group input {
  padding: 0.65rem 1rem;
  border: 1px solid var(--border);
  border-radius: 8px;
  font-size: 0.95rem;
  color: var(--text-h);
  background: var(--bg);
  font-family: inherit;
  transition: border-color 0.15s;
  outline: none;
}

.form-group input:focus {
  border-color: var(--accent);
}

.forma-akcije {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-top: 0.5rem;
}

.btn-primary {
  background: var(--accent);
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 0.65rem 2rem;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: opacity 0.15s;
}
.btn-primary:hover:not(:disabled) { opacity: 0.85; }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }

.btn-sekundarni {
  background: transparent;
  color: var(--text);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 0.65rem 2rem;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.15s;
}
.btn-sekundarni:hover { background: var(--accent-bg); }

.alert {
  padding: 0.75rem 1rem;
  border-radius: 8px;
  font-size: 0.9rem;
}
.alert--error {
  background: rgba(220, 38, 38, 0.1);
  color: #dc2626;
}
.alert--uspeh {
  background: rgba(34, 197, 94, 0.1);
  color: #16a34a;
}

.state-msg {
  text-align: center;
  padding: 3rem;
  color: var(--text);
}
</style>
