<template>
  <div class="app-layout">
    <SidebarNav />

    <main class="main-content">
      <div class="page-toolbar">
        <div class="toolbar-left">
          <RouterLink :to="backTarget" class="back-btn">← Nazad</RouterLink>
          <div>
            <h1 class="page-title">OCR skeniranje</h1>
            <p v-if="bookTitle" class="page-sub">{{ bookTitle }}</p>
          </div>
        </div>
      </div>

      <p v-if="resolving" class="state-msg">Učitavanje podataka o knjizi...</p>

      <p v-else-if="resolveError" class="error-msg">{{ resolveError }}</p>

      <section v-else class="ocr-card">
        <label
          class="dropzone"
          :class="{ 'dropzone--active': dragOver, 'dropzone--filled': !!file }"
          @dragover.prevent="dragOver = true"
          @dragleave.prevent="dragOver = false"
          @drop.prevent="onDrop"
        >
          <input
            type="file"
            accept="image/*,.pdf,application/pdf"
            class="dropzone-input"
            @change="onFileChange"
          />

          <template v-if="!file">
            <span class="dropzone-icon">🖼️</span>
            <p class="dropzone-text">Otpremite ili prevucite sliku stranice (ili PDF) ovde</p>
          </template>

          <template v-else>
            <img v-if="previewUrl" :src="previewUrl" alt="" class="dropzone-preview" />
            <span v-else class="dropzone-icon">📄</span>
            <p class="dropzone-text">{{ file.name }}</p>
            <button type="button" class="btn-clear" @click.prevent="clearFile">Ukloni</button>
          </template>
        </label>

        <button class="btn-primary" type="button" :disabled="!file || submitting" @click="submit">
          {{ submitting ? 'Obrada u toku...' : 'Pokreni OCR' }}
        </button>

        <p v-if="submitError" class="error-msg">{{ submitError }}</p>
        <p v-if="successMsg" class="success-msg">{{ successMsg }}</p>

        <div v-if="resultText !== null" class="result-box">
          <h3>Prepoznat tekst</h3>
          <textarea readonly rows="10" :value="resultText"></textarea>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import SidebarNav from '../components/Sidebar.vue'
import { searchApi } from '../services/api.js'

const route = useRoute()

const isbn = computed(() => route.query.isbn || '')
const backTarget = computed(() => (isbn.value ? `/knjige/${isbn.value}` : '/knjige'))

const resolving = ref(false)
const resolveError = ref('')
const recordId = ref('')
const bookTitle = ref('')

const file = ref(null)
const previewUrl = ref('')
const dragOver = ref(false)

const submitting = ref(false)
const submitError = ref('')
const successMsg = ref('')
const resultText = ref(null)

onMounted(async () => {
  if (route.query.recordId) {
    recordId.value = route.query.recordId
    return
  }
  if (!isbn.value) {
    resolveError.value = 'OCR stranica se otvara sa detalja knjige.'
    return
  }
  resolving.value = true
  try {
    const res = await searchApi.poIsbn(isbn.value)
    recordId.value = res.data.recordId
    bookTitle.value = res.data.title
  } catch (e) {
    resolveError.value = e.response?.status === 404
      ? 'Knjiga nije pronađena u pretraživom indeksu.'
      : (e.response?.data || 'Greška pri učitavanju knjige.')
  } finally {
    resolving.value = false
  }
})

onBeforeUnmount(() => {
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
})

function setFile(f) {
  if (!f) return
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  file.value = f
  previewUrl.value = f.type.startsWith('image/') ? URL.createObjectURL(f) : ''
  submitError.value = ''
  successMsg.value = ''
  resultText.value = null
}

function onFileChange(e) {
  setFile(e.target.files?.[0] || null)
}

function onDrop(e) {
  dragOver.value = false
  setFile(e.dataTransfer.files?.[0] || null)
}

function clearFile() {
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  file.value = null
  previewUrl.value = ''
}

async function submit() {
  if (!file.value || !recordId.value) return
  submitting.value = true
  submitError.value = ''
  successMsg.value = ''
  resultText.value = null
  try {
    const res = await searchApi.pokreniOcr(recordId.value, file.value, true)
    resultText.value = res.data.textExcerpt || ''
    successMsg.value = 'OCR uspešno završen.'
  } catch (e) {
    submitError.value = e.response?.data || 'Greška prilikom OCR obrade.'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-toolbar { display: flex; align-items: flex-start; justify-content: space-between; gap: 1.5rem; margin-bottom: 2rem; flex-wrap: wrap; }
.toolbar-left { display: flex; flex-direction: column; gap: 0.5rem; }
.back-btn { font-size: 0.85rem; color: var(--text-mid); text-decoration: none; opacity: 0.75; transition: opacity 0.2s; }
.back-btn:hover { opacity: 1; }
.page-title { font-size: 2rem; margin: 0; text-align: left; }
.page-sub { margin-top: 0.3rem; font-size: 0.9rem; color: var(--text-mid); }

.state-msg { color: var(--text-mid); padding: 1rem 0; }

.ocr-card {
  background: var(--card-bg);
  border-radius: 18px;
  box-shadow: var(--shadow);
  padding: 2rem;
  max-width: 560px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.25rem;
}

.dropzone {
  width: 100%;
  min-height: 220px;
  border: 2px dashed var(--border);
  border-radius: 14px;
  background: var(--input-bg);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.6rem;
  padding: 1.5rem;
  cursor: pointer;
  text-align: center;
  transition: border-color 0.2s, background 0.2s;
}
.dropzone--active { border-color: var(--btn-primary); background: var(--card-bg-alt); }
.dropzone--filled { border-style: solid; }
.dropzone-input { display: none; }
.dropzone-icon { font-size: 2.5rem; }
.dropzone-text { color: var(--text-mid); font-size: 0.92rem; word-break: break-all; }
.dropzone-preview { max-width: 100%; max-height: 180px; border-radius: 8px; object-fit: contain; }

.btn-clear {
  background: transparent;
  border: 1.5px solid var(--border);
  color: var(--text-mid);
  border-radius: 50px;
  padding: 0.3rem 1rem;
  font-size: 0.8rem;
  cursor: pointer;
}
.btn-clear:hover { background: rgba(0,0,0,0.05); }

.btn-primary { margin: 0; width: 100%; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }

.success-msg {
  color: #1d5a26;
  background: #d8f1dd;
  border-radius: 6px;
  padding: 0.5rem 0.75rem;
  font-size: 0.9rem;
  text-align: center;
  width: 100%;
}

.result-box { width: 100%; text-align: left; }
.result-box h3 { margin-bottom: 0.5rem; }
.result-box textarea {
  width: 100%;
  background: var(--input-bg);
  border: 1.5px solid var(--border);
  border-radius: 8px;
  padding: 0.75rem;
  font-size: 0.88rem;
  color: var(--text-dark);
  resize: vertical;
  font-family: var(--mono, monospace);
}
</style>
