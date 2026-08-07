<template>
  <div class="app-layout">
    <SidebarNav />
    <main class="main-content">
      <h1 class="page-title">Izveštaj o upotrebi AI agenta</h1>
      <p class="page-sub">
        Pregled upotrebe AI agenta od strane članova biblioteke.
      </p>

      <div class="report-card">
        <h2 class="card-title">Izveštaji o upotrebi AI agenta i njegovih funkcionalnosti od strane članova biblioteke za odabrani period</h2>

        <div class="form-row">
          <div class="form-group">
            <label>Datum od:</label>
            <input type="date" v-model="od" :max="datDo || today" />
          </div>
          <div class="form-group">
            <label>Datum do:</label>
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
            <button class="btn-generate" @click="generisi" :disabled="loading">
              <span v-if="loading" class="spinner"></span>
              {{ loading ? 'Generisanje...' : 'Generišite PDF izveštaj' }}
            </button>
        </div>
      </div>

      <div v-if="snackMsg" class="snack" :class="snackErr ? 'snack--err' : 'snack--ok'">
        {{ snackMsg }}
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import SidebarNav from '../components/Sidebar.vue'
import { izvestajApi } from '../services/api.js'

const today  = new Date().toISOString().split('T')[0]
const od     = ref('')
const datDo  = ref(today)
const loading = ref(false)
const formErr = ref('')
const snackMsg = ref('')
const snackErr = ref(false)

function setRange(days) {
  const d = new Date()
  d.setDate(d.getDate() - days)
  od.value   = d.toISOString().split('T')[0]
  datDo.value = today
  formErr.value = ''
}

function setCurrentYear() {
  const year = new Date().getFullYear()
  od.value    = `${year}-01-01`
  datDo.value = today
  formErr.value = ''
}

async function generisi() {
  formErr.value = ''

  if (!od.value || !datDo.value) {
    formErr.value = 'Unesite oba datuma.'
    return
  }
  if (od.value > datDo.value) {
    formErr.value = 'Datum „od" mora biti pre datuma „do".'
    return
  }

  loading.value = true
  try {
    const res = await izvestajApi.generisiAIAgentIzvestaj(od.value, datDo.value)

    // Preuzimanje PDF-a
    const blob = new Blob([res.data], { type: 'application/pdf' })
    const url  = URL.createObjectURL(blob)
    const a    = document.createElement('a')
    a.href     = url

    a.download = `izvestaj-ai-agent-${od.value}-${datDo.value}.pdf`
    a.click()
    URL.revokeObjectURL(url)

    showSnack('Izveštaj uspešno generisan i preuzet.', false)
  } catch (e) {
    const msg = e.response?.status === 403
      ? 'Nemate pravo pristupa.'
      : 'Greška pri generisanju izveštaja.'
    showSnack(msg, true)
  } finally {
    loading.value = false
  }
}

function showSnack(msg, isErr) {
  snackMsg.value = msg
  snackErr.value = isErr
  setTimeout(() => { snackMsg.value = '' }, 4000)
}
</script>

<style scoped>
.page-title { margin-bottom: 0.3rem; font-size: 1.8rem; }
.page-sub   { color: var(--text-mid); font-size: 0.92rem; margin-bottom: 2rem; }

.report-card, .preview-card {
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
  margin-bottom: 1.2rem;
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
  border-color: #7a5c48;
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

.btn-generate {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  background: #4a6741;
  color: white;
  border: none;
  border-radius: 50px;
  padding: 0.65rem 1.8rem;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-generate:hover    { background: #4a6741; }
.btn-generate:disabled { opacity: 0.6; cursor: not-allowed; }

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
  color: #4a6741;
  font-size: 0.85rem;
  margin-bottom: 1rem;
}


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