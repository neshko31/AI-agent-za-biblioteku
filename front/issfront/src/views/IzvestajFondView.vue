<template>
  <div class="app-layout">
    <SidebarNav />

    <main class="main-content">
      <h1 class="page-title">Izveštaj o katalogu</h1>
      <p class="page-sub">
        Pregled stanja fonda, cirkulacije naslova i potreba za revizijom
      </p>

      <div class="controls-card">
        <h2 class="card-title">Izaberite period cirkulacije</h2>

        <div class="form-row">
          <div class="form-group">
            <label>Datum od</label>
            <input type="date" v-model="od" :max="datDo || today" />
          </div>
          <div class="form-group">
            <label>Datum do</label>
            <input type="date" v-model="datDo" :min="od" :max="today" />
          </div>
        </div>

        <div class="quick-row">
          <span class="range-label">Brzi izbor:</span>
          <button class="btn-range" @click="setRange(30)">30 dana</button>
          <button class="btn-range" @click="setRange(90)">3 meseca</button>
          <button class="btn-range" @click="setRange(180)">6 meseci</button>
          <button class="btn-range" @click="setCurrentYear()">Ova godina</button>
        </div>

        <p v-if="formErr" class="err-msg">{{ formErr }}</p>

        <div class="btn-row">
          <button class="btn-preview" @click="fetchReport" :disabled="loading">
            <span v-if="loading" class="spinner"></span>
            {{ loading ? 'Učitavanje...' : 'Prikaži pregled' }}
          </button>
          <button
            v-if="pdfBlob"
            class="btn-download"
            @click="downloadPdf"
          >
            ⬇ Preuzmi PDF
          </button>
        </div>
      </div>

      <div v-if="pdfUrl" class="preview-wrapper">
        <div class="preview-header">
          <span class="preview-label">
            Pregled: {{ od }} — {{ datDo }}
          </span>
          <button class="btn-download btn-download--sm" @click="downloadPdf">
            ⬇ Preuzmi PDF
          </button>
        </div>
        <iframe
          :src="pdfUrl"
          class="pdf-frame"
          title="Izveštaj o katalogu"
        ></iframe>
      </div>

      <div v-if="snackMsg" class="snack" :class="snackErr ? 'snack--err' : 'snack--ok'">
        {{ snackMsg }}
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import SidebarNav from '../components/Sidebar.vue'
import { izvestajApi } from '../services/api.js'

const today  = new Date().toISOString().split('T')[0]
const year   = new Date().getFullYear()

const od      = ref(`${year}-01-01`)
const datDo   = ref(today)
const loading = ref(false)
const formErr = ref('')
const snackMsg = ref('')
const snackErr = ref(false)
const pdfBlob  = ref(null)
const pdfUrl   = ref('')

function setRange(days) {
  const d = new Date()
  d.setDate(d.getDate() - days)
  od.value    = d.toISOString().split('T')[0]
  datDo.value = today
  formErr.value = ''
  clearPreview()
}

function setCurrentYear() {
  od.value    = `${year}-01-01`
  datDo.value = today
  formErr.value = ''
  clearPreview()
}

function clearPreview() {
  if (pdfUrl.value) URL.revokeObjectURL(pdfUrl.value)
  pdfUrl.value  = ''
  pdfBlob.value = null
}

async function fetchReport() {
  formErr.value = ''

  if (!od.value || !datDo.value) {
    formErr.value = 'Unesite oba datuma.'
    return
  }
  if (od.value > datDo.value) {
    formErr.value = 'Datum „od" mora biti pre datuma „do".'
    return
  }

  clearPreview()
  loading.value = true

  try {
    const res = await izvestajApi.generisiFond(od.value, datDo.value)
    pdfBlob.value = new Blob([res.data], { type: 'application/pdf' })
    pdfUrl.value  = URL.createObjectURL(pdfBlob.value)
    showSnack('Izveštaj uspešno učitan.', false)
  } catch (e) {
    const msg = e.response?.status === 403
      ? 'Nemate pravo pristupa.'
      : 'Greška pri generisanju izveštaja.'
    showSnack(msg, true)
  } finally {
    loading.value = false
  }
}

function downloadPdf() {
  if (!pdfBlob.value) return
  const a    = document.createElement('a')
  a.href     = URL.createObjectURL(pdfBlob.value)
  a.download = `izvestaj-katalog-${od.value}-${datDo.value}.pdf`
  a.click()
  URL.revokeObjectURL(a.href)
}

function showSnack(msg, isErr) {
  snackMsg.value = msg
  snackErr.value = isErr
  setTimeout(() => { snackMsg.value = '' }, 4000)
}

onUnmounted(() => {
  if (pdfUrl.value) URL.revokeObjectURL(pdfUrl.value)
})
</script>

<style scoped>
.page-title { margin-bottom: 0.3rem; font-size: 1.8rem; }
.page-sub   { color: var(--text-mid); font-size: 0.92rem; margin-bottom: 2rem; }

/* ── Controls card ─────────────────────────────────────────────── */
.controls-card {
  background: white;
  border-radius: 14px;
  padding: 1.6rem 2rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.07);
  margin-bottom: 1.5rem;
}

.card-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-dark);
  margin-bottom: 1.2rem;
}

.form-row {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.form-group label {
  font-size: 0.85rem;
  color: var(--text-mid);
  font-weight: 600;
}

.form-group input[type="date"] {
  padding: 0.5rem 0.7rem;
  border: 1.5px solid var(--border, #ddd);
  border-radius: 8px;
  font-size: 0.9rem;
  color: var(--text-dark);
  outline: none;
  cursor: pointer;
}

.form-group input[type="date"]:focus {
  border-color: var(--btn-primary);
}

.quick-row {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  flex-wrap: wrap;
  margin-bottom: 1.2rem;
}

.range-label {
  font-size: 0.82rem;
  color: var(--text-mid);
}

.btn-range {
  background: #f0f4ea;
  border: 1px solid var(--border);
  border-radius: 50px;
  padding: 0.3rem 0.85rem;
  font-size: 0.8rem;
  color: var(--btn-primary);
  cursor: pointer;
  transition: background 0.15s;
}
.btn-range:hover { background: #dde8cc; }

.btn-row {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.btn-preview {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  background: var(--btn-primary);
  color: white;
  border: none;
  border-radius: 50px;
  padding: 0.65rem 1.8rem;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-preview:hover    { background: var(--btn-hover); }
.btn-preview:disabled { opacity: 0.6; cursor: not-allowed; }

.btn-download {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: white;
  color: var(--btn-primary);
  border: 2px solid var(--btn-primary);
  border-radius: 50px;
  padding: 0.6rem 1.6rem;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-download:hover { background: var(--btn-primary); color: white; }

.btn-download--sm {
  padding: 0.4rem 1.1rem;
  font-size: 0.85rem;
}

.spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255,255,255,0.4);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  display: inline-block;
}
@keyframes spin { to { transform: rotate(360deg); } }

.err-msg {
  color: #7a1e1e;
  font-size: 0.85rem;
  margin-bottom: 1rem;
}

/* ── Info cards ─────────────────────────────────────────────────── */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.info-card {
  background: white;
  border-radius: 14px;
  padding: 1.4rem 1.5rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.07);
  display: flex;
  gap: 1rem;
  align-items: flex-start;
  border-left: 4px solid var(--btn-primary);
}

.info-card--warn {
  border-left-color: #a07020;
}

.info-icon {
  font-size: 1.6rem;
  line-height: 1;
  flex-shrink: 0;
}

.info-heading {
  font-weight: 700;
  font-size: 0.95rem;
  color: var(--text-dark);
  margin-bottom: 0.4rem;
}

.info-body {
  font-size: 0.85rem;
  color: var(--text-mid);
  line-height: 1.5;
}

/* ── PDF Preview ─────────────────────────────────────────────────── */
.preview-wrapper {
  background: white;
  border-radius: 14px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.07);
  margin-bottom: 1.5rem;
  display: flex;
  flex-direction: column;
  min-height: 1500px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.9rem 1.5rem;
  background: #f5f8f0;
  border-bottom: 1px solid var(--border);
  border-radius: 14px 14px 0 0;
  flex-shrink: 0;
}

.preview-label {
  font-size: 0.88rem;
  font-weight: 600;
  color: var(--text-mid);
}

.pdf-frame {
  width: 100%;
  flex: 1;
  min-height: 1440px;
  border: none;
  display: block;
  border-radius: 0 0 14px 14px;
}

/* ── Snackbar ─────────────────────────────────────────────────────── */
.snack {
  position: fixed;
  bottom: 2rem;
  left: 50%;
  transform: translateX(-50%);
  padding: 0.7rem 1.5rem;
  border-radius: 8px;
  font-size: 0.9rem;
  z-index: 9999;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}
.snack--ok  { background: #d8f1dd; color: #1d5a26; }
.snack--err { background: #f8d7d7; color: #7a1e1e; }
</style>
