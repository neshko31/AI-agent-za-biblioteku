<template>
  <div class="forma-wrapper">
    <div class="page-header">
      <h1>Ugovori</h1>
      <p class="subtitle">Kreiranje novog ugovora</p>
    </div>

    <div class="forma-kartica">
      <div v-if="error" class="alert alert--error">{{ error }}</div>
      <div v-if="uspeh" class="alert alert--uspeh">Ugovor je uspešno kreiran!</div>

      <div class="form-group">
        <label>Dobavljač</label>
        <input :value="nazivDobavljaca" type="text" disabled class="input-disabled" />
      </div>

      <div class="form-group">
        <label>Datum potpisa</label>
        <input v-model="forma.datumPotpisa" type="date" />
      </div>

      <div class="form-group">
        <label>Datum početka važenja</label>
        <input v-model="forma.datumPocetka" type="date" />
      </div>

      <div class="form-group">
        <label>Datum isteka</label>
        <input v-model="forma.datumIsteka" type="date" />
      </div>

      <div class="form-group">
        <label>Popust (%)</label>
        <input v-model="forma.popust" type="number" min="0" max="100" step="0.1" placeholder="npr. 10.5" />
      </div>

      <div class="form-group">
        <label>Rok isporuke (u danima)</label>
        <input v-model="forma.rokIsporuke" type="number" min="1" placeholder="npr. 7" />
      </div>

      <div class="forma-akcije">
        <button class="btn-sekundarni" @click="router.back()">Odustani</button>
        <button class="btn-primary" @click="sacuvaj" :disabled="loading">
          {{ loading ? 'Kreiranje...' : 'Sačuvaj' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { dobavljacApi, ugovorApi } from '../../services/api.js'

const router = useRouter()
const route = useRoute()
const id = route.params.id

const nazivDobavljaca = ref('')
const loading = ref(false)
const error = ref('')
const uspeh = ref(false)

const forma = ref({
  dobavljacId: Number(id),
  datumPotpisa: '',
  datumPocetka: '',
  datumIsteka: '',
  popust: '',
  rokIsporuke: ''
})

async function ucitajDobavljaca() {
  try {
    const res = await dobavljacApi.jedan(id)
    nazivDobavljaca.value = res.data.naziv
  } catch (e) {
    error.value = 'Greška pri učitavanju dobavljača.'
  }
}

async function sacuvaj() {
  error.value = ''
  uspeh.value = false

  if (!forma.value.datumPotpisa || !forma.value.datumPocetka ||
      !forma.value.datumIsteka || forma.value.popust === '' ||
      !forma.value.rokIsporuke) {
    error.value = 'Sva polja su obavezna.'
    return
  }

  if (forma.value.datumIsteka <= forma.value.datumPocetka) {
    error.value = 'Datum isteka mora biti posle datuma početka.'
    return
  }

  loading.value = true
  try {
    await ugovorApi.kreiraj({
      ...forma.value,
      popust: Number(forma.value.popust),
      rokIsporuke: Number(forma.value.rokIsporuke)
    })
    uspeh.value = true
    setTimeout(() => router.push(`/menadzer/dobavljaci/${id}`), 1500)
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri kreiranju ugovora.'
  } finally {
    loading.value = false
  }
}

onMounted(ucitajDobavljaca)
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

.input-disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: var(--code-bg) !important;
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
</style>
