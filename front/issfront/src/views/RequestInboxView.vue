<template>
  <div class="app-layout">
    <SidebarNav />

    <main class="main-content">
      <div class="page-header">
        <div class="header-tekst">
          <h1>Međubibliotečki zahtevi</h1>
          <p class="subtitle">Pristigli i poslati zahtevi vaše biblioteke</p>
        </div>
        <button class="btn-primary" type="button" @click="openNewModal">+ Novi zahtev</button>
      </div>

      <div class="tabs">
        <button
          class="tab-btn"
          :class="{ 'tab-btn--active': activeTab === 'incoming' }"
          @click="activeTab = 'incoming'"
        >
          Pristigli <span class="tab-count">{{ incoming.length }}</span>
        </button>
        <button
          class="tab-btn"
          :class="{ 'tab-btn--active': activeTab === 'outgoing' }"
          @click="activeTab = 'outgoing'"
        >
          Poslati <span class="tab-count">{{ outgoing.length }}</span>
        </button>
      </div>

      <div v-if="loading" class="state-msg">
        <div class="spinner"></div>
        <p>Učitavanje zahteva...</p>
      </div>
      <div v-else-if="error" class="state-msg state-msg--error">
        <span class="state-icon">❌</span>
        <p>{{ error }}</p>
      </div>
      <div v-else-if="list.length === 0" class="state-msg state-msg--empty">
        <span class="state-icon">📥</span>
        <p>{{ activeTab === 'incoming' ? 'Nema pristiglih zahteva.' : 'Nema poslatih zahteva.' }}</p>
      </div>

      <div v-else class="request-list animated-fade-in">
        <div
          v-for="r in list"
          :key="r.id"
          class="request-card"
          :class="{ 'request-card--active': selected?.id === r.id }"
          @click="openDetail(r)"
        >
          <div class="request-main">
            <span class="library-name">
              {{ activeTab === 'incoming' ? libraryName(r.senderLibraryId) : libraryName(r.receiverLibraryId) }}
            </span>
            <p class="request-desc">{{ r.description }}</p>
          </div>
          <div class="request-meta">
            <span class="status-pill" :class="statusClass(r.status)">{{ statusLabel(r.status) }}</span>
            <span class="request-date">{{ formatDate(r.createdAt) }}</span>
          </div>
        </div>
      </div>

      <!-- Side panel za detalje zahteva -->
      <div v-if="selected" class="panel-overlay" @click.self="closeDetail">
        <aside class="side-panel animated-slide-in">
          <div class="side-panel-header">
            <h2>Detalji zahteva</h2>
            <button class="btn-close" type="button" @click="closeDetail">✕</button>
          </div>

          <div class="side-panel-body">
            <span class="status-pill" :class="statusClass(selected.status)">{{ statusLabel(selected.status) }}</span>

            <div class="detail-row">
              <span class="detail-label">Od biblioteke</span>
              <span class="detail-value">{{ libraryName(selected.senderLibraryId) }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Ka biblioteci</span>
              <span class="detail-value">{{ libraryName(selected.receiverLibraryId) }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Opis</span>
              <span class="detail-value">{{ selected.description }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Kreiran</span>
              <span class="detail-value">{{ formatDate(selected.createdAt) }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Ažuriran</span>
              <span class="detail-value">{{ formatDate(selected.updatedAt) }}</span>
            </div>
            <div v-if="selected.respondedByLibrarianId" class="detail-row">
              <span class="detail-label">Obradio</span>
              <span class="detail-value">{{ selected.respondedByLibrarianId }}</span>
            </div>

            <p v-if="actionError" class="error-msg">{{ actionError }}</p>

            <div class="side-panel-actions">
              <template v-if="isIncoming(selected) && selected.status === 'PENDING'">
                <button class="btn-akcija btn-odobri" :disabled="actionLoading" @click="accept(selected)">
                  ✓ Prihvati
                </button>
                <button class="btn-akcija btn-odbij" :disabled="actionLoading" @click="deny(selected)">
                  ✕ Odbij
                </button>
              </template>

              <button
                v-if="isAdmin && selected.status !== 'CANCELLED' && selected.status !== 'EXPIRED'"
                class="btn-akcija btn-otkazi"
                :disabled="actionLoading"
                @click="cancel(selected)"
              >
                Otkaži zahtev
              </button>
            </div>
          </div>
        </aside>
      </div>

      <!-- Modal za novi zahtev -->
      <div v-if="showNewModal" class="modal-overlay" @click.self="closeNewModal">
        <div class="modal animated-scale-up">
          <h2>Novi zahtev</h2>
          <p>Pošaljite zahtev drugoj biblioteci.</p>

          <div class="form-group">
            <label>Biblioteka</label>
            <select v-model="newForm.receiverLibraryId">
              <option value="" disabled>Izaberite biblioteku</option>
              <option v-for="l in otherLibraries" :key="l.bid" :value="l.bid">{{ l.name }}</option>
            </select>
          </div>

          <div class="form-group">
            <label>Opis zahteva</label>
            <textarea
              v-model="newForm.description"
              rows="4"
              maxlength="300"
              placeholder="Opišite šta tražite (5-300 karaktera)..."
            ></textarea>
            <span class="char-count">{{ newForm.description.length }}/300</span>
          </div>

          <p v-if="newError" class="error-msg">{{ newError }}</p>

          <div class="modal-akcije">
            <button class="btn-primary" :disabled="creating" @click="submitNewRequest">
              {{ creating ? 'Slanje...' : 'Pošalji zahtev' }}
            </button>
            <button class="btn-sekundarni" @click="closeNewModal">Odustani</button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import SidebarNav from '../components/Sidebar.vue'
import { useAuthStore } from '../stroage/auth.js'
import { requestApi, publicApi } from '../services/api.js'

const authStore = useAuthStore()
const myBid = authStore.getBid()
const myJmbg = authStore.user?.jmbg
const isAdmin = computed(() => authStore.isAdmin())

const activeTab = ref('incoming')
const incoming = ref([])
const outgoing = ref([])
const loading = ref(false)
const error = ref('')

const libraries = ref([])
const libraryMap = computed(() => {
  const m = {}
  libraries.value.forEach(l => { m[l.bid] = l.name })
  return m
})
const otherLibraries = computed(() => libraries.value.filter(l => l.bid !== myBid))

const list = computed(() => (activeTab.value === 'incoming' ? incoming.value : outgoing.value))

const selected = ref(null)
const actionError = ref('')
const actionLoading = ref(false)

const showNewModal = ref(false)
const newForm = ref({ receiverLibraryId: '', description: '' })
const newError = ref('')
const creating = ref(false)

const STATUS_LABELS = {
  PENDING: 'Na čekanju',
  ACCEPTED: 'Prihvaćen',
  DENIED: 'Odbijen',
  CANCELLED: 'Otkazan',
  EXPIRED: 'Istekao',
}

function statusLabel(s) { return STATUS_LABELS[s] || s }
function statusClass(s) { return 'status-' + (s ? s.toLowerCase() : 'unknown') }
function libraryName(id) { return libraryMap.value[id] || id }
function isIncoming(r) { return r.receiverLibraryId === myBid }

function formatDate(s) {
  if (!s) return ''
  const d = new Date(s)
  return `${String(d.getDate()).padStart(2, '0')}.${String(d.getMonth() + 1).padStart(2, '0')}.${d.getFullYear()}. ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

async function loadRequests() {
  loading.value = true
  error.value = ''
  try {
    const [incRes, outRes] = await Promise.all([
      requestApi.incoming(myBid),
      requestApi.outgoing(myBid),
    ])
    incoming.value = incRes.data || []
    outgoing.value = outRes.data || []
  } catch (e) {
    error.value = e.response?.data?.error || 'Greška pri učitavanju zahteva.'
  } finally {
    loading.value = false
  }
}

async function loadLibraries() {
  try {
    const res = await publicApi.getLibraries()
    libraries.value = res.data || []
  } catch {
    libraries.value = []
  }
}

function openDetail(r) {
  selected.value = r
  actionError.value = ''
}
function closeDetail() {
  selected.value = null
  actionError.value = ''
}

function replaceInLists(updated) {
  incoming.value = incoming.value.map(r => (r.id === updated.id ? updated : r))
  outgoing.value = outgoing.value.map(r => (r.id === updated.id ? updated : r))
  if (selected.value?.id === updated.id) selected.value = updated
}

async function accept(req) {
  actionLoading.value = true
  actionError.value = ''
  try {
    const res = await requestApi.azuriraj(req.id, { status: 'ACCEPTED', respondedByLibrarianId: myJmbg })
    replaceInLists(res.data)
  } catch (e) {
    actionError.value = e.response?.data?.error || 'Greška pri prihvatanju zahteva.'
  } finally {
    actionLoading.value = false
  }
}

async function deny(req) {
  actionLoading.value = true
  actionError.value = ''
  try {
    const res = await requestApi.azuriraj(req.id, { status: 'DENIED', respondedByLibrarianId: myJmbg })
    replaceInLists(res.data)
  } catch (e) {
    actionError.value = e.response?.data?.error || 'Greška pri odbijanju zahteva.'
  } finally {
    actionLoading.value = false
  }
}

async function cancel(req) {
  if (!window.confirm('Da li ste sigurni da želite da otkažete ovaj zahtev?')) return
  actionLoading.value = true
  actionError.value = ''
  try {
    const res = await requestApi.otkazi(req.id)
    replaceInLists(res.data)
  } catch (e) {
    actionError.value = e.response?.data?.error || 'Greška pri otkazivanju zahteva.'
  } finally {
    actionLoading.value = false
  }
}

function openNewModal() {
  newForm.value = { receiverLibraryId: '', description: '' }
  newError.value = ''
  showNewModal.value = true
}
function closeNewModal() {
  showNewModal.value = false
}

async function submitNewRequest() {
  newError.value = ''
  if (!newForm.value.receiverLibraryId) {
    newError.value = 'Izaberite biblioteku kojoj šaljete zahtev.'
    return
  }
  const desc = newForm.value.description.trim()
  if (desc.length < 5 || desc.length > 300) {
    newError.value = 'Opis mora imati između 5 i 300 karaktera.'
    return
  }
  creating.value = true
  try {
    await requestApi.kreiraj({
      senderId: myJmbg,
      senderLibraryId: myBid,
      receiverLibraryId: newForm.value.receiverLibraryId,
      description: desc,
    })
    showNewModal.value = false
    activeTab.value = 'outgoing'
    await loadRequests()
  } catch (e) {
    newError.value = e.response?.data?.error || 'Greška pri slanju zahteva.'
  } finally {
    creating.value = false
  }
}

onMounted(() => {
  loadRequests()
  loadLibraries()
})
</script>

<style scoped>
.page-header {
  margin-bottom: 1.5rem;
  width: 100%;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}
.header-tekst h1 { margin: 0 0 0.35rem; font-size: 2rem; text-align: left; }
.subtitle { color: var(--text-mid); font-size: 0.95rem; margin: 0; }

.tabs { display: flex; gap: 0.5rem; margin-bottom: 1.5rem; }
.tab-btn {
  background: var(--card-bg-alt);
  border: 1.5px solid var(--border);
  border-radius: 50px;
  padding: 0.5rem 1.3rem;
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-mid);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.tab-btn--active { background: var(--btn-primary); color: var(--text-light); border-color: var(--btn-primary); }
.tab-count {
  background: rgba(255,255,255,0.35);
  border-radius: 50px;
  padding: 0.05rem 0.5rem;
  font-size: 0.78rem;
}

.request-list { display: flex; flex-direction: column; gap: 0.75rem; }
.request-card {
  background: var(--input-bg);
  border-radius: 14px;
  box-shadow: var(--shadow);
  padding: 1rem 1.25rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  cursor: pointer;
  transition: transform 0.15s;
}
.request-card:hover { transform: translateY(-1px); }
.request-card--active { outline: 2px solid var(--btn-primary); }
.request-main { min-width: 0; flex: 1; }
.library-name { font-weight: 700; color: var(--text-dark); display: block; margin-bottom: 0.2rem; }
.request-desc {
  color: var(--text-mid);
  font-size: 0.88rem;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.request-meta { display: flex; flex-direction: column; align-items: flex-end; gap: 0.4rem; flex-shrink: 0; }
.request-date { font-size: 0.78rem; color: var(--text-mid); white-space: nowrap; }

.status-pill { padding: 0.2rem 0.7rem; border-radius: 999px; font-size: 0.78rem; font-weight: 600; white-space: nowrap; }
.status-pending { background: #fff3cd; color: #8a6300; }
.status-accepted { background: #d8f1dd; color: #1d5a26; }
.status-denied { background: #f8d7d7; color: #8b1a1a; }
.status-cancelled { background: #e6e6e6; color: #555; }
.status-expired { background: #ecdff3; color: #5b3a73; }

/* Side panel */
.panel-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.35); z-index: 200;
  display: flex; justify-content: flex-end;
}
.side-panel {
  width: 380px; max-width: 90vw; height: 100%;
  background: var(--card-bg-alt);
  box-shadow: -8px 0 24px rgba(0,0,0,0.18);
  padding: 1.75rem; display: flex; flex-direction: column; gap: 1.25rem;
  overflow-y: auto;
}
.side-panel-header { display: flex; align-items: center; justify-content: space-between; }
.side-panel-header h2 { margin: 0; font-size: 1.25rem; }
.btn-close { background: transparent; border: none; font-size: 1.1rem; cursor: pointer; color: var(--text-mid); }
.side-panel-body { display: flex; flex-direction: column; gap: 0.9rem; }

.detail-row { display: flex; flex-direction: column; gap: 0.15rem; }
.detail-label { font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-mid); }
.detail-value { color: var(--text-dark); font-size: 0.95rem; word-break: break-word; }

.side-panel-actions { display: flex; flex-direction: column; gap: 0.6rem; margin-top: 0.5rem; }
.btn-akcija {
  padding: 0.55rem 1rem; border-radius: 50px; font-size: 0.88rem; font-weight: 600;
  border: none; cursor: pointer; transition: all 0.2s ease;
}
.btn-akcija:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-odobri { background: rgba(34,197,94,0.12); color: #15803d; }
.btn-odobri:hover { background: #15803d; color: #fff; }
.btn-odbij { background: rgba(220,38,38,0.08); color: #b91c1c; }
.btn-odbij:hover { background: #b91c1c; color: #fff; }
.btn-otkazi { background: rgba(0,0,0,0.06); color: #444; }
.btn-otkazi:hover { background: #444; color: #fff; }

/* Modal */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4); backdrop-filter: blur(2px);
  display: flex; align-items: center; justify-content: center; z-index: 300;
}
.modal { background: var(--input-bg); border-radius: 18px; padding: 2rem; max-width: 440px; width: 90%; box-shadow: var(--shadow); }
.modal h2 { margin: 0 0 0.4rem; font-size: 1.3rem; text-align: left; }
.modal p { color: var(--text-mid); margin-bottom: 1rem; font-size: 0.92rem; }
.form-group { display: flex; flex-direction: column; gap: 0.3rem; margin-bottom: 1rem; position: relative; }
.form-group label { font-size: 0.85rem; color: var(--text-mid); }
.form-group textarea {
  background: var(--input-bg);
  border: 1.5px solid var(--border);
  border-radius: 6px;
  padding: 0.6rem 0.75rem;
  font-size: 0.92rem;
  color: var(--text-dark);
  font-family: inherit;
  resize: vertical;
  outline: none;
  width: 100%;
  transition: border-color 0.2s;
}
.form-group textarea:focus { border-color: var(--btn-primary); }
.char-count { position: absolute; right: 0; bottom: -1.1rem; font-size: 0.72rem; color: var(--text-mid); }
.modal-akcije { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1rem; }
.btn-primary { margin: 0; }
.btn-sekundarni {
  background: transparent; color: var(--text-mid); border: 1px solid var(--border);
  border-radius: 50px; padding: 0.65rem 1.4rem; font-size: 0.9rem; cursor: pointer;
}
.btn-sekundarni:hover { background: rgba(0,0,0,0.05); }

.state-msg {
  text-align: center; padding: 3rem 2rem; background: var(--input-bg); border-radius: 18px;
  box-shadow: var(--shadow); display: flex; flex-direction: column; align-items: center; gap: 1rem;
}
.state-msg p { margin: 0; color: var(--text-mid); }
.state-icon { font-size: 2.2rem; }
.state-msg--error { color: #b91c1c; }
.spinner {
  width: 28px; height: 28px; border: 3px solid var(--border); border-top-color: var(--btn-primary);
  border-radius: 50%; animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.animated-fade-in { animation: fadeIn 0.3s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(6px); } to { opacity: 1; transform: translateY(0); } }
.animated-slide-in { animation: slideIn 0.22s ease-out; }
@keyframes slideIn { from { transform: translateX(24px); opacity: 0; } to { transform: translateX(0); opacity: 1; } }
.animated-scale-up { animation: scaleUp 0.2s cubic-bezier(0.34, 1.56, 0.64, 1); }
@keyframes scaleUp { from { transform: scale(0.95); opacity: 0; } to { transform: scale(1); opacity: 1; } }
</style>
