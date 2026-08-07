<template>
  <div class="app-layout">
    <SidebarNav />

    <main class="main-content">
      <p v-if="!authorized" class="error-msg">Nemate pristup ovoj stranici.</p>

      <template v-else>
        <div class="detail-top">
          <button class="btn-secondary" @click="goBack">Nazad na baze podataka</button>
        </div>

        <section class="baza-detail">
          <div class="baza-info">
            <h1>Izmena baze podataka</h1>

            <p v-if="loading">Ucitavanje detalja...</p>
            <p v-if="error" class="error-msg">{{ error }}</p>

            <form v-if="baza" class="baza-form" @submit.prevent="saveChanges">
              <div class="form-row">
                <div class="form-group">
                  <label for="baza-naziv">Naziv *</label>
                  <input id="baza-naziv" v-model="form.naziv" type="text" />
                </div>
                <div class="form-group">
                  <label for="baza-oblast">Oblast *</label>
                  <input id="baza-oblast" v-model="form.oblast" type="text" />
                </div>
                <div class="form-group">
                  <label for="baza-licenca">Licenca *</label>
                  <input id="baza-licenca" v-model="form.licenca" type="text" />
                </div>
              </div>

              <div class="form-group">
                <label for="baza-opis">Opis *</label>
                <textarea
                  id="baza-opis"
                  v-model="form.opis"
                  class="form-textarea"
                  rows="4"
                ></textarea>
              </div>

              <div class="form-group">
                <label for="baza-izdavac">Izdavač</label>
                <p v-if="loadingIzdavaci" class="hint-text">Učitavanje izdavača...</p>
                <p v-else-if="izdavaciError" class="error-msg">{{ izdavaciError }}</p>
                <select
                  v-else
                  id="baza-izdavac"
                  v-model="form.izdavacId"
                >
                  <option :value="null">— bez izdavača —</option>
                  <option
                    v-for="izdavac in izdavaci"
                    :key="izdavac.id"
                    :value="izdavac.id"
                  >
                    {{ izdavac.naziv }}
                  </option>
                </select>
              </div>

              <div class="form-group">
                <label for="baza-zip">Zameni zip fajl (opciono): </label>
                <input id="baza-zip" type="file" accept=".zip" @change="onZipChange" />
              </div>

              <div class="form-actions">
                <button class="btn-secondary" type="submit" :disabled="saving">
                  {{ saving ? 'Čuvanje...' : 'Sačuvaj izmene' }}
                </button>
                <button class="btn-danger" type="button" :disabled="deleting" @click="deleteBaza">
                  Obriši bazu
                </button>
              </div>

              <p v-if="saveSuccess" class="success-msg">{{ saveSuccess }}</p>
            </form>
          </div>
        </section>
      </template>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SidebarNav from '../components/Sidebar.vue'
import { useAuthStore } from '../stroage/auth.js'
import { bazePodatakaApi, izdavaciApi } from '../services/api.js'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const authorized = ref(false)
const loading = ref(false)
const saving = ref(false)
const deleting = ref(false)
const error = ref('')
const saveSuccess = ref('')
const baza = ref(null)
const zipFile = ref(null)

const izdavaci = ref([])
const loadingIzdavaci = ref(false)
const izdavaciError = ref('')

const form = ref({
  naziv: '',
  oblast: '',
  licenca: '',
  opis: '',
  izdavacId: null
})

const isLibrarian = computed(() => authStore.getRole() === 'BIBLIOTEKAR')
const bazaId = computed(() => route.params.id)

onMounted(() => {
  authorized.value = isLibrarian.value
  if (authorized.value) {
    // Učitavamo paralelno detalje baze i listu izdavača
    Promise.all([loadDetails(), loadIzdavaci()])
  }
})

async function loadDetails() {
  loading.value = true
  error.value = ''
  try {
    const res = await bazePodatakaApi.detalji(bazaId.value)
    baza.value = res.data
    form.value.naziv = baza.value.naziv || ''
    form.value.oblast = baza.value.oblast || ''
    form.value.licenca = baza.value.licenca || ''
    form.value.opis = baza.value.opis || ''
    // Pre-selektujemo trenutnog izdavača ako postoji u DTO-u
    form.value.izdavacId = baza.value.izdavacId ?? null
  } catch (e) {
    error.value = e.response?.data || 'Greska pri ucitavanju.'
  } finally {
    loading.value = false
  }
}

async function loadIzdavaci() {
  loadingIzdavaci.value = true
  izdavaciError.value = ''
  try {
    const res = await izdavaciApi.ispisiSve()
    izdavaci.value = res.data
  } catch (e) {
    izdavaciError.value = 'Greska pri ucitavanju izdavaca.'
  } finally {
    loadingIzdavaci.value = false
  }
}

function onZipChange(event) {
  zipFile.value = event.target.files?.[0] || null
}

function normalizeText(value) {
  const trimmed = (value || '').trim()
  return trimmed.length ? trimmed : null
}

function validateForm() {
  if (!normalizeText(form.value.naziv)) return 'Naziv je obavezan.'
  if (!normalizeText(form.value.oblast)) return 'Oblast je obavezna.'
  if (!normalizeText(form.value.licenca)) return 'Licenca je obavezna.'
  if (!normalizeText(form.value.opis)) return 'Opis je obavezan.'
  return ''
}

async function saveChanges() {
  if (!isLibrarian.value || !baza.value) return
  error.value = ''
  saveSuccess.value = ''

  const validationError = validateForm()
  if (validationError) {
    error.value = validationError
    return
  }

  saving.value = true
  try {
    const payload = {
      naziv: normalizeText(form.value.naziv),
      oblast: normalizeText(form.value.oblast),
      licenca: normalizeText(form.value.licenca),
      opis: normalizeText(form.value.opis),
      izdavacId: form.value.izdavacId ?? null
    }

    const formData = new FormData()
    formData.append('podaci', new Blob([JSON.stringify(payload)], { type: 'application/json' }))
    if (zipFile.value) {
      formData.append('zip', zipFile.value)
    }

    await bazePodatakaApi.azuriraj(bazaId.value, formData)
    saveSuccess.value = 'Baza podataka je uspesno azurirana.'
    await loadDetails()
  } catch (e) {
    error.value = e.response?.data || 'Greska pri azuriranju.'
  } finally {
    saving.value = false
  }
}

async function deleteBaza() {
  if (!isLibrarian.value || !baza.value) return
  const confirmed = window.confirm('Da li ste sigurni da zelite da obrisete bazu?')
  if (!confirmed) return
  deleting.value = true
  error.value = ''
  try {
    await bazePodatakaApi.obrisi(bazaId.value)
    router.push('/baze-podataka')
  } catch (e) {
    error.value = e.response?.data || 'Greska pri brisanju.'
  } finally {
    deleting.value = false
  }
}

function goBack() {
  router.push('/baze-podataka')
}
</script>

<style scoped>
.detail-top {
  margin-bottom: 1rem;
}

.baza-detail {
  background: white;
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: var(--shadow);
}

.baza-info h1 {
  margin: 0 0 0.6rem;
  text-align: left;
  font-size: 1.8rem;
}

.baza-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.9rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.form-group label {
  font-size: 0.86rem;
  color: var(--text-mid);
  text-align: left;
}

.form-group input,
.form-group select,
.form-textarea {
  background: var(--input-bg);
  border: 1.5px solid var(--border);
  border-radius: 8px;
  padding: 0.55rem 0.8rem;
  font-size: 0.95rem;
  width: 100%;
}

.form-group select {
  cursor: pointer;
  appearance: auto;
}

.form-textarea {
  resize: vertical;
  min-height: 90px;
}

.form-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  margin-top: 0.5rem;
}

.btn-danger {
  background: #b23b3b;
  color: #fff;
  border: none;
  border-radius: 50px;
  padding: 0.65rem 1.6rem;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-danger:hover {
  background: #962f2f;
}

.btn-danger:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.success-msg {
  background: #dff3dc;
  color: #255c1f;
  border-radius: 8px;
  padding: 0.55rem 0.8rem;
  margin-top: 0.5rem;
}

@media (max-width: 1080px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 820px) {
  .form-actions {
    justify-content: stretch;
    flex-direction: column;
  }
}
</style>