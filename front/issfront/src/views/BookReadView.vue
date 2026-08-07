<template>
  <div class="app-layout">
    <SidebarNav />

    <main class="main-content">
      <p v-if="!authorized" class="error-msg">Nemate pristup ovoj stranici.</p>

      <template v-else>
        <p v-if="loading">Ucitavanje e-knjige...</p>
        <p v-if="showError" class="error-msg">{{ error }}</p>

        <div v-if="book" class="read-header">
          <div>
            <h1>{{ book.naslov }}</h1>
            <p class="read-author">{{ book.autor }}</p>
          </div>
          <div class="read-actions">
            <div class="page-status">
              Strana {{ currentPage }} / {{ totalPages || '—' }}
            </div>
            <button class="btn-secondary" :disabled="currentPage <= 1" @click="prevPage">
              Prethodna
            </button>
            <button class="btn-secondary" :disabled="currentPage >= totalPages" @click="nextPage">
              Sledeca
            </button>
            <button class="btn-secondary" @click="closeAndSave">Sacuvaj i zatvori</button>
          </div>
        </div>

        <div class="pdf-frame" v-if="pdfReady">
          <canvas ref="canvasRef"></canvas>
        </div>
      </template>
    </main>
  </div>
</template>

<script setup>
import { computed, markRaw, nextTick, onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import SidebarNav from '../components/Sidebar.vue'
import { useAuthStore } from '../stroage/auth.js'
import { knjigaApi } from '../services/api.js'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const isbn = computed(() => route.params.isbn)
const authorized = ref(false)
const loading = ref(false)
const error = ref('')
const book = ref(null)
const pdfDoc = shallowRef(null)
const totalPages = ref(0)
const currentPage = ref(1)
const canvasRef = ref(null)
const pdfReady = computed(() => !!pdfDoc.value && totalPages.value > 0)
const showError = computed(() => !!error.value && !pdfReady.value)
let renderTask = null
let lastSavedPage = null
let lastSavedFinished = false
let saveTimer = null
let pdfjsLib = null
let workerSrc = null

onMounted(() => {
  authorized.value = authStore.getRole() === 'CLAN'
  if (authorized.value) {
    loadReader()
  }
})

onBeforeUnmount(() => {
  if (renderTask) renderTask.cancel()
  if (saveTimer) clearTimeout(saveTimer)
})

onBeforeRouteLeave(async () => {
  if (authorized.value) {
    await saveProgress()
  }
})

async function loadReader() {
  loading.value = true
  error.value = ''
  try {
    const [detailRes, progressRes, pdfRes] = await Promise.all([
      knjigaApi.detalji(isbn.value),
      knjigaApi.citanjeProgress(isbn.value),
      knjigaApi.pdf(isbn.value)
    ])
    book.value = detailRes.data
    await ensurePdfjs()
    const data = await pdfRes.data.arrayBuffer()
    pdfDoc.value = markRaw(await pdfjsLib.getDocument({ data, isEvalSupported: false }).promise)
    totalPages.value = pdfDoc.value.numPages
    const savedPage = progressRes.data?.trenutnaStranica || 1
    currentPage.value = clampPage(savedPage)
    await nextTick()
    await renderPage(currentPage.value)
  } catch (e) {
    error.value = e.response?.data || 'Greska pri ucitavanju.'
  } finally {
    loading.value = false
  }
}

async function ensurePdfjs() {
  if (pdfjsLib) return
  pdfjsLib = await import('pdfjs-dist/build/pdf')
  workerSrc = (await import('pdfjs-dist/build/pdf.worker?url')).default
  pdfjsLib.GlobalWorkerOptions.workerSrc = workerSrc
}

function clampPage(page) {
  if (!totalPages.value) return Math.max(1, Number(page) || 1)
  return Math.min(Math.max(1, Number(page) || 1), totalPages.value)
}

async function renderPage(pageNumber) {
  if (!pdfDoc.value || !canvasRef.value) return
  if (renderTask) renderTask.cancel()

  const page = await pdfDoc.value.getPage(pageNumber)
  const viewport = page.getViewport({ scale: 1.35 })
  const canvas = canvasRef.value
  const context = canvas.getContext('2d')

  canvas.height = viewport.height
  canvas.width = viewport.width
  renderTask = page.render({ canvasContext: context, viewport })
  try {
    await renderTask.promise
  } catch (err) {
    if (err?.name !== 'RenderingCancelledException') {
      throw err
    }
  }
}

watch(
  () => canvasRef.value,
  (canvas) => {
    if (canvas && pdfDoc.value && totalPages.value) {
      renderPage(currentPage.value)
    }
  }
)

function scheduleSave() {
  if (saveTimer) clearTimeout(saveTimer)
  saveTimer = setTimeout(() => {
    saveProgress()
  }, 600)
}

function nextPage() {
  if (currentPage.value >= totalPages.value) return
  currentPage.value += 1
  renderPage(currentPage.value)
  scheduleSave()
}

function prevPage() {
  if (currentPage.value <= 1) return
  currentPage.value -= 1
  renderPage(currentPage.value)
  scheduleSave()
}

async function saveProgress() {
  const page = clampPage(currentPage.value)
  const zavrseno = totalPages.value > 0 && page >= totalPages.value
  if (lastSavedPage === page && lastSavedFinished === zavrseno) return
  try {
    await knjigaApi.sacuvajCitanje(isbn.value, { trenutnaStranica: page, zavrseno })
    lastSavedPage = page
    lastSavedFinished = zavrseno
  } catch {
    lastSavedPage = page
    lastSavedFinished = zavrseno
  }
}

async function closeAndSave() {
  await saveProgress()
  router.push(`/knjige/${isbn.value}`)
}
</script>

<style scoped>
.read-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1.5rem;
  flex-wrap: wrap;
  margin-bottom: 1.5rem;
}

.read-header h1 {
  margin: 0;
  text-align: left;
  font-size: 1.8rem;
}

.read-author {
  color: var(--text-mid);
  margin-top: 0.3rem;
}

.read-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.page-status {
  font-size: 0.9rem;
  color: var(--text-mid);
}

.pdf-frame {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: var(--shadow);
  padding: 1rem;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.pdf-frame canvas {
  max-width: 100%;
  height: auto;
}
</style>
