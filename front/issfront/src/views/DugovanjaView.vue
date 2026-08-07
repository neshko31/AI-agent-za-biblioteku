<template>
  <div class="app-layout">
    <SidebarNav />
    <main class="main-content">
      <h1 class="page-title">Dugovanja</h1>

      <p v-if="loading" class="loading-msg">Učitavanje...</p>

      <template v-if="!loading">
        <div v-if="kazne.length === 0" class="empty-state">
          <p>Nemate evidentiranih dugovanja.</p>
        </div>

        <template v-else>

          <section v-if="prekoracene.length > 0">
            <h2 class="section-heading">Nevraćena knjiga</h2>
            <p class="section-sub">Kazna se obračunava kao 100 din po danu kašnjenja</p>
            <div v-for="k in prekoracene" :key="k.idK" class="dugovanje-card">
              <div class="book-cover-wrap">
                <img v-if="coverUrls[k.isbn]" :src="coverUrls[k.isbn]" class="book-cover" alt="" />
                <div v-else class="book-cover cover-placeholder"></div>
                <p class="cover-title">{{ k.naslovKnjige }}</p>
                <p class="cover-author">{{ k.autorKnjige }}</p>
              </div>
              <div class="info-box">
                <h3>Informacije</h3>
                <p>Naziv knjige: {{ k.naslovKnjige }}</p>
                <p>Datum uzimanja: {{ formatDate(k.datPoz) }}</p>
                <p>Datum očekivanog vraćanja: {{ formatDate(k.datOcVrac) }}</p>
                <p>Iznos kazne: <strong>{{ k.iznosK }} dinara</strong></p>

                <p v-if="k.placena" class="paid-badge">✓ Plaćeno ({{ k.nacinPlacanja === 'ONLINE' ? 'online' : 'uživo' }})</p>
                <div v-else class="pay-actions">
                  <button
                    class="btn-pay"
                    @click="togglePanel(k.idK)"
                    :disabled="paying === k.idK"
                  >
                    {{ activePanelId === k.idK ? 'Otkažite' : 'Platite online' }}
                  </button>
                </div>

                <!-- Payment panel -->
                <transition name="slide">
                  <div v-if="activePanelId === k.idK && !k.placena" class="card-form">
                    <p class="card-note">Unesite podatke kartice za plaćanje</p>
                    <input v-model="card.number" placeholder="Broj kartice" maxlength="19" />
                    <div class="card-row">
                      <input v-model="card.expiry" placeholder="MM/GG" maxlength="5" />
                      <input v-model="card.cvv" placeholder="CVV" maxlength="3" type="password" />
                    </div>
                    <p v-if="payError" class="pay-error">{{ payError }}</p>
                    <button
                      class="btn-pay btn-confirm"
                      @click="plati(k)"
                      :disabled="paying === k.idK"
                    >
                      {{ paying === k.idK ? 'Obrada...' : 'Potvrdite plaćanje' }}
                    </button>
                  </div>
                </transition>
              </div>
            </div>
          </section>

          <section v-if="izgubljene.length > 0" :class="{ 'mt-section': prekoracene.length > 0 }">
            <h2 class="section-heading">Izgubljena knjiga</h2>
            <div v-for="k in izgubljene" :key="k.idK" class="dugovanje-card">
              <div class="book-cover-wrap">
                <img v-if="coverUrls[k.isbn]" :src="coverUrls[k.isbn]" class="book-cover" alt="" />
                <div v-else class="book-cover cover-placeholder"></div>
                <p class="cover-title">{{ k.naslovKnjige }}</p>
                <p class="cover-author">{{ k.autorKnjige }}</p>
              </div>
              <div class="info-box">
                <h3>Informacije</h3>
                <p>Naziv knjige: {{ k.naslovKnjige }}</p>
                <p>Datum uzimanja: {{ formatDate(k.datPoz) }}</p>
                <p>Iznos kazne: <strong>{{ k.iznosK }} dinara</strong></p>

                <p v-if="k.placena" class="paid-badge">✓ Plaćeno ({{ k.nacinPlacanja === 'ONLINE' ? 'online' : 'uživo' }})</p>
                <div v-else class="pay-actions">
                  <button
                    class="btn-pay"
                    @click="togglePanel(k.idK)"
                    :disabled="paying === k.idK"
                  >
                    {{ activePanelId === k.idK ? 'Otkažite' : 'Platite online' }}
                  </button>
                </div>

                <!-- Payment panel -->
                <transition name="slide">
                  <div v-if="activePanelId === k.idK && !k.placena" class="card-form">
                    <p class="card-note">Unesite podatke kartice za plaćanje</p>
                    <input v-model="card.number" placeholder="Broj kartice" maxlength="19" />
                    <div class="card-row">
                      <input v-model="card.expiry" placeholder="MM/GG" maxlength="5" />
                      <input v-model="card.cvv" placeholder="CVV" maxlength="3" type="password" />
                    </div>
                    <p v-if="payError" class="pay-error">{{ payError }}</p>
                    <button
                      class="btn-pay btn-confirm"
                      @click="plati(k)"
                      :disabled="paying === k.idK"
                    >
                      {{ paying === k.idK ? 'Obrada...' : 'Potvrdite plaćanje' }}
                    </button>
                  </div>
                </transition>
              </div>
            </div>
          </section>

        </template>
      </template>

      <!-- Feedback snackbar -->
      <div v-if="snackMsg" class="snack" :class="snackErr ? 'snack--err' : 'snack--ok'">
        {{ snackMsg }}
      </div>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref, computed, reactive } from 'vue'
import SidebarNav from '../components/Sidebar.vue'
import { kaznaApi, knjigaApi } from '../services/api.js'

const loading     = ref(true)
const kazne       = ref([])
const coverUrls   = ref({})
const paying      = ref(null)
const snackMsg    = ref('')
const snackErr    = ref(false)
const activePanelId = ref(null)
const payError    = ref('')

const card = reactive({ number: '', expiry: '', cvv: '' })

const prekoracene = computed(() => kazne.value.filter(k => !k.izgubljena && !k.placena))
const izgubljene  = computed(() => kazne.value.filter(k =>  k.izgubljena && !k.placena))

onMounted(async () => {
  await loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await kaznaApi.moje()
    kazne.value = res.data || []
    await loadCovers()
  } catch {
    kazne.value = []
  } finally {
    loading.value = false
  }
}

async function loadCovers() {
  const isbnSet = new Set(kazne.value.map(k => k.isbn))
  for (const isbn of isbnSet) {
    try {
      const res = await knjigaApi.naslovna(isbn)
      coverUrls.value[isbn] = URL.createObjectURL(res.data)
    } catch {
      coverUrls.value[isbn] = null
    }
  }
}

function togglePanel(idK) {
  if (activePanelId.value === idK) {
    activePanelId.value = null
  } else {
    activePanelId.value = idK
    card.number  = ''
    card.expiry  = ''
    card.cvv     = ''
    payError.value = ''
  }
}

async function plati(k) {
  payError.value = ''

  if (!card.number.trim() || !card.expiry.trim() || !card.cvv.trim()) {
    payError.value = 'Popunite sva polja kartice.'
    return
  }
  if (card.cvv.length < 3) {
    payError.value = 'CVV mora imati 3 cifre.'
    return
  }

  paying.value = k.idK
  try {
    await kaznaApi.plati(k.idK, 'ONLINE')
    activePanelId.value = null
    card.number = ''
    card.expiry = ''
    card.cvv    = ''
    showSnack('Kazna uspešno plaćena.', false)
    await loadData()
  } catch (e) {
    payError.value = e.response?.data || 'Greška pri plaćanju.'
  } finally {
    paying.value = null
  }
}

function showSnack(msg, isErr) {
  snackMsg.value = msg
  snackErr.value = isErr
  setTimeout(() => { snackMsg.value = '' }, 3500)
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${String(d.getDate()).padStart(2,'0')}. ${String(d.getMonth()+1).padStart(2,'0')}. ${d.getFullYear()}.`
}
</script>

<style scoped>
.page-title   { margin-bottom: 1.5rem; font-size: 1.8rem; }
.loading-msg  { color: var(--text-mid); }
.empty-state  { text-align: center; padding: 3rem 0; color: var(--text-mid); }
.mt-section   { margin-top: 2rem; }

.section-heading { font-size: 1.3rem; margin-bottom: 0.3rem; color: var(--text-dark); }
.section-sub     { font-size: 0.85rem; color: var(--text-mid); margin-bottom: 1rem; }

.dugovanje-card {
  display: flex;
  gap: 1.5rem;
  align-items: flex-start;
  margin-bottom: 1.5rem;
  background: transparent;
}

.book-cover-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 110px;
  flex-shrink: 0;
}

.book-cover {
  width: 110px;
  height: 155px;
  border-radius: 8px;
  object-fit: cover;
}

.cover-placeholder {
  background: linear-gradient(135deg, #c8b9ae, #a89080);
}

.cover-title  { font-size: 0.75rem; font-weight: 600; text-align: center; margin: 0.3rem 0 0; line-height: 1.2; }
.cover-author { font-size: 0.7rem;  color: var(--text-mid); text-align: center; margin: 0; }

.info-box { flex: 1; }
.info-box h3 { font-size: 1.1rem; margin-bottom: 0.6rem; color: var(--text-dark); }
.info-box p  { font-size: 0.88rem; color: var(--text-mid); margin: 0.25rem 0; }
.info-box strong { color: var(--text-dark); }

.pay-actions {
  display: flex;
  gap: 0.6rem;
  margin-top: 0.8rem;
  flex-wrap: wrap;
}

.btn-pay {
  background: #7a5c48;
  color: #fff;
  border: none;
  border-radius: 50px;
  padding: 0.45rem 1.2rem;
  font-size: 0.85rem;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-pay:hover    { background: #5e4436; }
.btn-pay:disabled { opacity: 0.6; cursor: not-allowed; }

.btn-confirm {
  margin-top: 0.5rem;
  border-radius: 6px;
  width: 100%;
}

/* Payment panel */
.card-form {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  margin-top: 0.9rem;
  background: #faf8f6;
  border: 1.5px solid var(--border, #ddd);
  border-radius: 10px;
  padding: 1rem 1.1rem;
  max-width: 320px;
}

.card-form input {
  padding: 0.45rem 0.6rem;
  border: 1.5px solid var(--border, #ddd);
  border-radius: 6px;
  font-size: 0.85rem;
  background: white;
  outline: none;
}

.card-form input:focus {
  border-color: #7a5c48;
}

.card-row {
  display: flex;
  gap: 0.5rem;
}

.card-row input { flex: 1; }

.card-note {
  font-size: 0.82rem;
  color: var(--text-mid, #777);
  font-style: italic;
  margin: 0 0 0.2rem !important;
}

.pay-error {
  color: #7a1e1e;
  font-size: 0.82rem;
  margin: 0 !important;
}

.paid-badge {
  color: #1d5a26;
  font-weight: 600;
  margin-top: 0.6rem !important;
}

/* Slide animation */
.slide-enter-active, .slide-leave-active { transition: all 0.2s ease; }
.slide-enter-from, .slide-leave-to       { opacity: 0; transform: translateY(-6px); }

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