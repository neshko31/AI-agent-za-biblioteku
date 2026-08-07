<template>
  <div class="forma-wrapper">
    <div class="page-header">
      <h1>Dobavljači</h1>
      <p class="subtitle">Unos novog dobavljača</p>
    </div>

    <div class="forma-kartica">
      <!-- Greška -->
      <div v-if="error" class="alert alert--error">{{ error }}</div>
      <!-- Uspeh -->
      <div v-if="uspeh" class="alert alert--uspeh">Dobavljač je uspešno dodat!</div>

      <div class="form-group">
        <label>Unesite naziv dobavljača</label>
        <input v-model="forma.naziv" type="text" placeholder="Naziv dobavljača" />
      </div>

      <div class="form-group">
        <label>Izaberite tip dobavljača</label>
        <select v-model="forma.tipDobavljaca">
          <option value="" disabled>Tip dobavljača</option>
          <option value="00">Ni knjižara ni izdavač</option>
          <option value="01">Knjižara</option>
          <option value="10">Izdavač</option>
          <option value="11">Knjižara i izdavač</option>
        </select>
      </div>

      <!-- URL se prikazuje samo ako je knjižara -->
      <div class="form-group" v-if="jeKnjizara">
        <label>Unesite URL online prodavnice</label>
        <input v-model="forma.urlOnlineProdavnice" type="text" placeholder="https://..." />
      </div>

      <div class="form-group">
        <label>Unesite PIB dobavljača</label>
        <input v-model="forma.pib" type="text" placeholder="PIB (samo cifre)" />
      </div>

      <div class="form-group">
        <label>Unesite email dobavljača</label>
        <input v-model="forma.email" type="email" placeholder="email@primer.com" />
      </div>

      <div class="form-group">
        <label>Unesite telefon dobavljača</label>
        <input v-model="forma.tel" type="text" placeholder="011/555-666" />
      </div>

      <div class="forma-akcije">
        <button class="btn-sekundarni" @click="router.back()">Odustani</button>
        <button class="btn-primary" @click="sacuvaj" :disabled="loading">
          {{ loading ? 'Čuvanje...' : 'Sačuvaj' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { dobavljacApi } from '../../services/api.js'

const router = useRouter()

const forma = ref({
  naziv: '',
  tipDobavljaca: '',
  urlOnlineProdavnice: '',
  pib: '',
  email: '',
  tel: ''
})

const loading = ref(false)
const error = ref('')
const uspeh = ref(false)

// Prikazuje URL polje samo ako je tip 01 ili 11
const jeKnjizara = computed(() =>
  forma.value.tipDobavljaca === '01' || forma.value.tipDobavljaca === '11'
)

async function sacuvaj() {
  error.value = ''
  uspeh.value = false

  // Osnovna validacija
  if (!forma.value.naziv || !forma.value.tipDobavljaca || !forma.value.pib ||
      !forma.value.email || !forma.value.tel) {
    error.value = 'Sva polja su obavezna.'
    return
  }

  if (jeKnjizara.value && !forma.value.urlOnlineProdavnice) {
    error.value = 'URL online prodavnice je obavezan za knjižaru.'
    return
  }

  loading.value = true
  try {
    await dobavljacApi.kreiraj(forma.value)
    uspeh.value = true
    // Nakon 1.5s idi na listu dobavljača
    setTimeout(() => router.push('/menadzer/dobavljaci'), 1500)
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri dodavanju dobavljača.'
  } finally {
    loading.value = false
  }
}
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

.form-group input,
.form-group select {
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

.form-group input:focus,
.form-group select:focus {
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
</style>