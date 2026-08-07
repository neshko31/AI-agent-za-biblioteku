<template>
  <div class="app-layout">
    <SidebarNav />

    <main class="main-content">
      <p v-if="!authorized" class="error-msg">Nemate pristup ovoj stranici.</p>

      <template v-else>
        <div class="detail-top">
          <button class="btn-secondary" @click="goBack">Nazad na sve knjige</button>
        </div>

        <section class="book-detail">
          <div class="detail-cover">
            <img v-if="coverPreviewUrl" :src="coverPreviewUrl" alt="Pregled naslovne" />
            <div v-else class="cover-placeholder"></div>
          </div>

          <div class="detail-info">
            <h1>Dodavanje nove knjige</h1>

            <form class="librarian-form" @submit.prevent="saveBook">
              <div class="form-row">
                <div class="form-group">
                  <label for="book-isbn">ISBN *</label>
                  <input id="book-isbn" v-model="form.isbn" type="text" />
                </div>
                <div class="form-group">
                  <label for="book-title">Naslov *</label>
                  <input id="book-title" v-model="form.naslov" type="text" />
                </div>
                <div class="form-group">
                  <label for="book-author">Autor *</label>
                  <input id="book-author" v-model="form.autor" type="text" />
                </div>
              </div>

              <div class="form-group">
                <label for="book-catalog">Katalog *</label>
                <select
                  id="book-catalog"
                  v-model="form.katId"
                  :disabled="catalogsLoading || !catalogs.length"
                >
                  <option value="">Izaberite katalog</option>
                  <option v-for="catalog in catalogs" :key="catalog.katId" :value="catalog.katId">
                    {{ catalogLabel(catalog) }}
                  </option>
                </select>
                <p v-if="catalogsLoading" class="hint-text">Ucitavanje kataloga...</p>
                <p v-if="catalogError" class="hint-text error-text">{{ catalogError }}</p>
              </div>

              <div class="form-group">
                <label for="book-sinopsis">Opis</label>
                <textarea
                  id="book-sinopsis"
                  v-model="form.sinopsis"
                  class="form-textarea"
                  rows="4"
                ></textarea>
              </div>

              <div class="media-grid">
                <div class="media-card">
                  <div class="media-header">
                    <span class="media-title">Naslovna (JPEG) *</span>
                  </div>
                  <div class="form-group">
                    <label for="cover-file">Izaberite naslovnu</label>
                    <input
                      id="cover-file"
                      type="file"
                      accept="image/jpeg,image/jpg,.jpg,.jpeg"
                      @change="onCoverChange"
                    />
                  </div>
                </div>

                <div class="media-card">
                  <div class="media-header">
                    <span class="media-title">eKnjiga (PDF)</span>
                  </div>
                  <div class="form-group">
                    <label for="eknjiga-file">Dodaj PDF fajl</label>
                    <input
                      id="eknjiga-file"
                      type="file"
                      accept="application/pdf,.pdf"
                      @change="onPdfChange"
                    />
                  </div>
                  <div class="form-group">
                    <label for="eknjiga-pages">Broj strana (opciono)</label>
                    <input
                      id="eknjiga-pages"
                      v-model="form.brojStranaEK"
                      type="number"
                      min="1"
                      :disabled="!pdfFile"
                    />
                  </div>
                </div>

                <div class="media-card">
                  <div class="media-header">
                    <span class="media-title">Audio knjiga (MP3)</span>
                  </div>
                  <div class="form-group">
                    <label for="audioknjiga-file">Dodaj MP3 fajl</label>
                    <input
                      id="audioknjiga-file"
                      type="file"
                      accept="audio/mpeg,audio/mp3,.mp3"
                      @change="onMp3Change"
                    />
                  </div>
                  <div class="form-group">
                    <label for="audioknjiga-duration">Trajanje u sekundama (opciono)</label>
                    <input
                      id="audioknjiga-duration"
                      v-model="form.trajanjeSekundeAK"
                      type="number"
                      min="1"
                      :disabled="!mp3File"
                    />
                  </div>
                </div>
              </div>

              <div class="form-actions">
                <button class="btn-secondary" type="button" :disabled="saving" @click="goBack">
                  Odustani
                </button>
                <button class="btn-secondary" type="submit" :disabled="saving || catalogsLoading">
                  {{ saving ? 'Čuvanje...' : 'Sačuvaj novu knjigu' }}
                </button>
              </div>

              <p v-if="error" class="error-msg">{{ error }}</p>
              <p v-if="success" class="success-msg">{{ success }}</p>
            </form>
          </div>
        </section>
      </template>
    </main>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import SidebarNav from '../components/Sidebar.vue'
import { useAuthStore } from '../stroage/auth.js'
import { katalogApi, knjigaApi } from '../services/api.js'

const authStore = useAuthStore()
const router = useRouter()

const authorized = ref(false)
const catalogs = ref([])
const catalogsLoading = ref(false)
const catalogError = ref('')
const saving = ref(false)
const error = ref('')
const success = ref('')
const coverFile = ref(null)
const coverPreviewUrl = ref('')
const pdfFile = ref(null)
const mp3File = ref(null)

const form = ref({
  isbn: '',
  naslov: '',
  autor: '',
  sinopsis: '',
  katId: '',
  brojStranaEK: '',
  trajanjeSekundeAK: ''
})

const isLibrarian = computed(() => authStore.getRole() === 'BIBLIOTEKAR')

onMounted(() => {
  authorized.value = isLibrarian.value
  if (authorized.value) {
    loadCatalogs()
  }
})

onBeforeUnmount(() => {
  revokeCoverPreview()
})

function revokeCoverPreview() {
  if (coverPreviewUrl.value) {
    URL.revokeObjectURL(coverPreviewUrl.value)
    coverPreviewUrl.value = ''
  }
}

function catalogLabel(catalog) {
  return catalog.standard ? `${catalog.katIme} (${catalog.standard})` : catalog.katIme
}

async function loadCatalogs() {
  if (!isLibrarian.value || catalogsLoading.value) return
  catalogsLoading.value = true
  catalogError.value = ''
  try {
    const res = await katalogApi.svi()
    catalogs.value = res.data || []
  } catch (e) {
    catalogError.value = e.response?.data || 'Greska pri ucitavanju kataloga.'
  } finally {
    catalogsLoading.value = false
  }
}

function onCoverChange(event) {
  coverFile.value = event.target.files?.[0] || null
  revokeCoverPreview()
  if (coverFile.value) {
    coverPreviewUrl.value = URL.createObjectURL(coverFile.value)
  }
}

function onPdfChange(event) {
  pdfFile.value = event.target.files?.[0] || null
  if (!pdfFile.value) {
    form.value.brojStranaEK = ''
  }
}

function onMp3Change(event) {
  mp3File.value = event.target.files?.[0] || null
  if (!mp3File.value) {
    form.value.trajanjeSekundeAK = ''
  }
}

function normalizeText(value) {
  const trimmed = (value || '').trim()
  return trimmed.length ? trimmed : null
}

function normalizeInt(value) {
  if (value === '' || value == null) return null
  const parsed = Number(value)
  if (!Number.isFinite(parsed)) return null
  return Math.max(1, Math.floor(parsed))
}

function validateForm() {
  if (!normalizeText(form.value.isbn)) {
    return 'ISBN je obavezan.'
  }
  if (!normalizeText(form.value.naslov)) {
    return 'Naslov je obavezan.'
  }
  if (!normalizeText(form.value.autor)) {
    return 'Autor je obavezan.'
  }
  if (normalizeInt(form.value.katId) == null) {
    return 'Katalog je obavezan.'
  }
  if (!coverFile.value) {
    return 'Naslovna strana je obavezna.'
  }
  return ''
}

async function saveBook() {
  if (!isLibrarian.value) return
  error.value = ''
  success.value = ''

  const validationError = validateForm()
  if (validationError) {
    error.value = validationError
    return
  }

  saving.value = true
  try {
    const payload = {
      isbn: normalizeText(form.value.isbn),
      naslov: normalizeText(form.value.naslov),
      autor: normalizeText(form.value.autor),
      sinopsis: normalizeText(form.value.sinopsis),
      katId: normalizeInt(form.value.katId)
    }

    const brojStrana = normalizeInt(form.value.brojStranaEK)
    if (brojStrana != null && pdfFile.value) {
      payload.brojStranaEK = brojStrana
    }

    const trajanjeSekunde = normalizeInt(form.value.trajanjeSekundeAK)
    if (trajanjeSekunde != null && mp3File.value) {
      payload.trajanjeSekundeAK = trajanjeSekunde
    }

    const formData = new FormData()
    formData.append('podaci', new Blob([JSON.stringify(payload)], { type: 'application/json' }))
    formData.append('naslovna', coverFile.value)
    if (pdfFile.value) formData.append('pdf', pdfFile.value)
    if (mp3File.value) formData.append('mp3', mp3File.value)

    await knjigaApi.dodajKompletna(formData)
    success.value = 'Knjiga je uspesno dodata.'
    router.push('/knjige')
  } catch (e) {
    error.value = e.response?.data || 'Greska pri dodavanju knjige.'
  } finally {
    saving.value = false
  }
}

function goBack() {
  router.push('/knjige')
}
</script>

<style scoped>
.detail-top {
  margin-bottom: 1rem;
}

.book-detail {
  background: white;
  border-radius: 16px;
  padding: 1.5rem;
  display: grid;
  grid-template-columns: 180px 1fr;
  gap: 1.5rem;
  box-shadow: var(--shadow);
}

.detail-cover {
  width: 180px;
  height: 240px;
  background: #e3e3e3;
  border-radius: 12px;
  overflow: hidden;
}

.detail-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #c7c7c7, #b0b0b0);
}

.detail-info h1 {
  margin: 0 0 0.4rem;
  text-align: left;
  font-size: 1.8rem;
}

.helper-text {
  color: var(--text-mid);
  margin-bottom: 1rem;
}

.librarian-form {
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

.form-textarea {
  resize: vertical;
  min-height: 90px;
}

.hint-text {
  font-size: 0.82rem;
  color: var(--text-mid);
}

.error-text {
  color: #8b1a1a;
}

.media-grid {
  display: grid;
  gap: 1rem;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.media-card {
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 0.9rem;
  background: #f5f7ed;
}

.media-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.media-title {
  font-weight: 600;
  color: var(--text-dark);
}

.form-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  margin-top: 0.5rem;
}

.success-msg {
  background: #dff3dc;
  color: #255c1f;
  border-radius: 8px;
  padding: 0.55rem 0.8rem;
  margin-top: 0.5rem;
}

@media (max-width: 1080px) {
  .form-row,
  .media-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 820px) {
  .book-detail {
    grid-template-columns: 1fr;
  }

  .detail-cover {
    width: 160px;
    height: 220px;
  }

  .form-actions {
    justify-content: stretch;
    flex-direction: column;
  }
}
</style>
