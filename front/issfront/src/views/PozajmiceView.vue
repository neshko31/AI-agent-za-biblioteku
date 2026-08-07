<template>
  <div class="app-layout">
    <SidebarNav />
    <main class="main-content">
      <h1 class="page-title">Pozajmice</h1>

      <p v-if="loading" class="loading-msg">Učitavanje...</p>

      <template v-if="!loading">

        <section v-if="aktivne.length > 0">
          <h2 class="section-title">Aktivne pozajmice - datum vraćanja {{ formatDate(aktivne[0]?.datOcVrac) }}.</h2>
          <div class="book-grid">
            <div
              v-for="p in aktivne"
              :key="p.idP"
              class="book-card"
              @click="openPozajmicaDetail(p)"
            >
              <div class="book-cover">
                <img v-if="coverUrls[p.isbn]" :src="coverUrls[p.isbn]" alt="" />
                <div v-else class="cover-placeholder"></div>
                <span class="format-badge">fizička forma</span>
              </div>
              <div class="book-meta">
                <p class="book-title">{{ p.naslovKnjige }}</p>
                <p class="book-author">{{ p.autorKnjige }}</p>
              </div>
            </div>
          </div>
        </section>

        <!-- DIGITALNE POZAJMICE -->
        <section v-if="aktivneEKnjige.length > 0 || aktivneAudioKnjige.length > 0" class="mt-section">
          <h2 class="section-title">Digitalne pozajmice</h2>
          <div class="book-grid">
            <div
              v-for="p in aktivneEKnjige"
              :key="'ek-' + p.isbn"
              class="book-card"
              @click="$router.push(`/knjige/${p.isbn}/citaj`)"
            >
              <div class="book-cover">
                <img v-if="coverUrls[p.isbn]" :src="coverUrls[p.isbn]" alt="" />
                <div v-else class="cover-placeholder"></div>
                <span class="format-badge"> e-knjiga</span>
              </div>
              <div class="book-meta">
                <p class="book-title">{{ p.naslovKnjige }}</p>
                <p class="book-author">{{ p.autorKnjige }}</p>
                <p class="book-date">dostupno do: {{ formatDate(p.datOcVrac) }}</p>
              </div>
            </div>
            <div
              v-for="p in aktivneAudioKnjige"
              :key="'au-' + p.isbn"
              class="book-card"
              @click="$router.push(`/knjige/${p.isbn}/slusaj`)"
            >
              <div class="book-cover">
                <img v-if="coverUrls[p.isbn]" :src="coverUrls[p.isbn]" alt="" />
                <div v-else class="cover-placeholder"></div>
                <span class="format-badge"> audio</span>
              </div>
              <div class="book-meta">
                <p class="book-title">{{ p.naslovKnjige }}</p>
                <p class="book-author">{{ p.autorKnjige }}</p>
                <p class="book-date">dostupno do: {{ formatDate(p.datOcVrac) }}</p>
              </div>
            </div>
          </div>
        </section>


        <section v-if="rezervacije.length > 0" class="mt-section">
          <h2 class="section-title">Rezervacije</h2>
          <div class="book-grid">
            <div
              v-for="r in rezervacije"
              :key="r.idR"
              class="book-card"
            >
              <div class="book-cover">
                <img v-if="coverUrls[r.isbn]" :src="coverUrls[r.isbn]" alt="" />
                <div v-else class="cover-placeholder"></div>
              </div>
              <div class="book-meta">
                <p class="book-title">{{ r.naslovKnjige }}</p>
                <p class="book-author">{{ r.autorKnjige }}</p>
                <p class="book-date" v-if="r.datIspR">
                  dostupno za preuzimanje<br>{{ formatDate(r.datIspR) }}
                </p>
                <p class="book-date available" v-if="r.datObavR"> dostupno odmah</p>
                <button
                  v-if="r.datObavR"
                  class="btn-borrow"
                  @click="borrowFromReservation(r)"
                  :disabled="borrowingRez === r.idR"
                >
                  Pozajmite
                </button>
              </div>
            </div>
          </div>
        </section>


        <div v-if="aktivne.length === 0 && aktivneEKnjige.length === 0 && aktivneAudioKnjige.length === 0 && rezervacije.length === 0" class="empty-state">
          <p>Nemate aktivnih pozajmica ni rezervacija.</p>
          <button class="btn-secondary" @click="$router.push('/knjige')">Pregledajte knjige</button>
        </div>
      </template>


      <div v-if="selectedPozajmica" class="modal-overlay" @click.self="closeDetail">
        <div class="modal-box detail-modal">
          <h2>{{ selectedPozajmica.naslovKnjige }}</h2>
          <p class="modal-author">{{ selectedPozajmica.autorKnjige }}</p>

          <div class="format-icons">
            <div class="format-icon active"><span>📚</span><small>fizička</small></div>
            <div class="format-icon"><span>📖</span><small>elektronska</small></div>
            <div class="format-icon"><span>🎧</span><small>audio</small></div>
          </div>

          <div class="modal-dates">
            <p>Datum pozajmice: <strong>{{ formatDate(selectedPozajmica.datPoz) }}</strong></p>
            <p>Rok vraćanja: <strong>{{ formatDate(selectedPozajmica.datOcVrac) }}</strong></p>
          </div>

          <div class="modal-actions">
            <button class="btn-borrow" @click="requestExtension(selectedPozajmica)" :disabled="extending">
              Produžite pozajmicu
            </button>
            <button class="btn-danger-outline" @click="reportLost(selectedPozajmica)" :disabled="reportingLost">
              Prijavite izgubljenu knjigu
            </button>
          </div>

          <p v-if="detailMsg" :class="detailMsgError ? 'error-msg' : 'success-msg'">{{ detailMsg }}</p>

          <button class="btn-close" @click="closeDetail">✕</button>
        </div>
      </div>


      <div v-if="showFeedbackModal" class="modal-overlay" @click.self="closeFeedback">
        <div class="modal-box">
          <h2>{{ feedbackTitle }}</h2>
          <p>{{ feedbackMsg }}</p>
          <button class="btn-secondary" @click="closeFeedback">Nazad na Početnu</button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import SidebarNav from '../components/Sidebar.vue'
import { pozajmicaApi, knjigaApi } from '../services/api.js'

const router = useRouter()
const loading = ref(true)
const aktivne = ref([])
const aktivneEKnjige = ref([])
const aktivneAudioKnjige = ref([])
const rezervacije = ref([])
const coverUrls = ref({})
const selectedPozajmica = ref(null)
const extending = ref(false)
const reportingLost = ref(false)
const detailMsg = ref('')
const detailMsgError = ref(false)
const showFeedbackModal = ref(false)
const feedbackTitle = ref('')
const feedbackMsg = ref('')
const borrowingRez = ref(null)

onMounted(async () => {
  await loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await pozajmicaApi.getMoje()
    aktivne.value = res.data.aktivnePozajmice || []
    aktivneEKnjige.value = res.data.aktivneEKnjige || []
    aktivneAudioKnjige.value = res.data.aktivneAudioKnjige || []
    rezervacije.value = res.data.aktivneRezervacije || []
    await loadAllCovers()
  } catch {
    aktivne.value = []
    aktivneEKnjige.value = []
    aktivneAudioKnjige.value = []
    rezervacije.value = []
  } finally {
    loading.value = false
  }
}

async function loadAllCovers() {
  const isbnSet = new Set([
    ...aktivne.value.map(p => p.isbn),
    ...aktivneEKnjige.value.map(p => p.isbn),
    ...aktivneAudioKnjige.value.map(p => p.isbn),
    ...rezervacije.value.map(r => r.isbn)
  ])
  for (const isbn of isbnSet) {
    try {
      const res = await knjigaApi.naslovna(isbn)
      coverUrls.value[isbn] = URL.createObjectURL(res.data)
    } catch {
      coverUrls.value[isbn] = null
    }
  }
}

function openPozajmicaDetail(p) {
  selectedPozajmica.value = p
  detailMsg.value = ''
  detailMsgError.value = false
}

function closeDetail() {
  selectedPozajmica.value = null
}

async function requestExtension(p) {
  extending.value = true
  detailMsg.value = ''
  try {
    const res = await pozajmicaApi.produzenje(p.idP)
    detailMsg.value = res.data.message
    detailMsgError.value = false
  } catch (e) {
    detailMsg.value = e.response?.data?.message || 'Greška pri produženju.'
    detailMsgError.value = true
  } finally {
    extending.value = false
  }
}

async function reportLost(p) {
  if (!window.confirm('Da li ste sigurni da želite da prijavite izgubljenu knjigu?')) return
  reportingLost.value = true
  detailMsg.value = ''
  try {
    const res = await pozajmicaApi.izgubljena(p.idP)
    detailMsg.value = res.data.message
    detailMsgError.value = false
    await loadData()
    closeDetail()
  } catch (e) {
    detailMsg.value = e.response?.data?.message || 'Greška pri prijavi.'
    detailMsgError.value = true
  } finally {
    reportingLost.value = false
  }
}

async function borrowFromReservation(r) {
  borrowingRez.value = r.idR
  try {
    const res = await pozajmicaApi.izRezervacije(r.idR)
    feedbackTitle.value = 'Pozajmica'
    feedbackMsg.value = res.data.message
    showFeedbackModal.value = true
    await loadData()
  } catch (e) {
    feedbackTitle.value = 'Pozajmica'
    feedbackMsg.value = e.response?.data?.message || 'Greška pri pozajmici.'
    showFeedbackModal.value = true
  } finally {
    borrowingRez.value = null
  }
}

function closeFeedback() {
  showFeedbackModal.value = false
  router.push('/')
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${String(d.getDate()).padStart(2,'0')}.${String(d.getMonth()+1).padStart(2,'0')}.${d.getFullYear()}.`
}
</script>

<style scoped>
.page-title { margin-bottom: 1.5rem; font-size: 1.8rem; }
.section-title { font-size: 1.1rem; margin-bottom: 1rem; color: var(--text-dark); }
.mt-section { margin-top: 2rem; }
.loading-msg { color: var(--text-mid); }
.empty-state { text-align: center; padding: 3rem 0; color: var(--text-mid); }
.empty-state button { margin-top: 1rem; }

.book-grid { display: flex; flex-wrap: wrap; gap: 1.2rem; }
.book-card {
  width: 120px; cursor: pointer; transition: transform 0.15s;
}
.book-card:hover { transform: translateY(-3px); }
.book-cover { width: 120px; height: 170px; border-radius: 10px; overflow: hidden; background: #ddd; position: relative; }
.book-cover img { width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder { width: 100%; height: 100%; background: linear-gradient(135deg, #c8b9ae, #a89080); }
.format-badge {
  position: absolute; bottom: 0; left: 0; right: 0;
  background: rgba(0,0,0,0.5); color: #fff; font-size: 0.65rem;
  text-align: center; padding: 0.2rem 0;
}
.book-meta { padding: 0.4rem 0; }
.book-title { font-size: 0.8rem; font-weight: 600; margin: 0; line-height: 1.3; }
.book-author { font-size: 0.75rem; color: var(--text-mid); margin: 0; }
.book-date { font-size: 0.72rem; color: var(--text-mid); margin-top: 0.3rem; }
.book-date.available { color: #1d5a26; font-weight: 600; }

.btn-borrow {
  background: #7a5c48; color: #fff; border: none; border-radius: 50px;
  padding: 0.4rem 1rem; font-size: 0.82rem; cursor: pointer; margin-top: 0.4rem;
}
.btn-borrow:hover { background: #5e4436; }
.btn-borrow:disabled { opacity: 0.6; cursor: not-allowed; }


.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.45); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-box { background: #f9f0ea; border-radius: 16px; padding: 2rem 2.5rem; max-width: 460px; width: 90%; text-align: center; box-shadow: 0 8px 30px rgba(0,0,0,0.2); position: relative; }
.modal-box h2 { margin-bottom: 0.5rem; }
.modal-author { color: var(--text-mid); margin-bottom: 1rem; }
.detail-modal { max-width: 420px; }
.format-icons { display: flex; gap: 0.8rem; justify-content: center; margin-bottom: 1rem; }
.format-icon { display: flex; flex-direction: column; align-items: center; gap: 0.2rem; padding: 0.5rem 0.8rem; border-radius: 10px; background: #f0ebe6; opacity: 0.4; font-size: 1.4rem; }
.format-icon.active { opacity: 1; background: #e8d8cd; }
.format-icon small { font-size: 0.65rem; color: var(--text-mid); }
.modal-dates { text-align: left; margin-bottom: 1.2rem; }
.modal-dates p { margin: 0.3rem 0; font-size: 0.9rem; color: var(--text-mid); }
.modal-dates strong { color: var(--text-dark); }
.modal-actions { display: flex; flex-direction: column; gap: 0.6rem; }
.btn-danger-outline { background: transparent; color: #962f2f; border: 1px solid #c66a6a; border-radius: 50px; padding: 0.5rem 1.4rem; font-size: 0.9rem; cursor: pointer; }
.btn-danger-outline:hover { background: #f8d7d7; }
.btn-danger-outline:disabled { opacity: 0.7; cursor: not-allowed; }
.btn-close { position: absolute; top: 0.8rem; right: 1rem; background: none; border: none; font-size: 1.2rem; cursor: pointer; color: var(--text-mid); }
.success-msg { color: #1d5a26; background: #d8f1dd; border-radius: 6px; padding: 0.5rem; font-size: 0.88rem; margin-top: 0.8rem; }
.error-msg { color: #7a1e1e; background: #f8d7d7; border-radius: 6px; padding: 0.5rem; font-size: 0.88rem; margin-top: 0.8rem; }
</style>
