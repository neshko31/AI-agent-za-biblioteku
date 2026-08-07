<template>
  <div class="app-layout">
    <SidebarNav />

    <main class="main-content chat-page">
      <p v-if="!authorized" class="error-msg">Nemate pristup ovoj stranici.</p>

      <template v-else>
        <p v-if="healthChecked && !healthOk" class="health-banner">
          AI asistent trenutno nije dostupan. Pokušajte kasnije ili obavestite bibliotekara.
        </p>

        <div class="chat-shell">
          <!-- LEVA KOLONA: dugme + lista sesija -->
          <aside class="session-panel">
            <button class="btn-new-chat" @click="openCompose">Novi čet</button>
            <button class="btn-show-archive" @click="showArchive = !showArchive">
              {{ showArchive ? 'Aktivni četovi' : 'Arhivirani četovi' }}
            </button>

            <p v-if="loadingSessions" class="loading-msg">Učitavanje četova...</p>
            <p v-else-if="sessionsError" class="error-msg">{{ sessionsError }}</p>

            <nav v-else-if="!showArchive" class="session-list">
              <p v-if="!activeSessions.length" class="empty-state">Još nemate nijedan čet.</p>

              <div
                v-for="s in activeSessions"
                :key="s.id"
                class="session-item"
                :class="{ active: s.id === activeSessionId && viewMode === 'conversation' }"
                @click="selectSession(s.id)"
              >
                <span class="session-name" :class="{ 'session-name-pending': s._pending }">
                  {{ sessionLabel(s) }}
                </span>
                <div class="session-right">
                  <span class="agent-pill" :class="agentPillClass(s.tipAgentaCS)">
                    {{ agentLabel(s.tipAgentaCS) }}
                  </span>
                  <button
                    class="btn-archive-session"
                    :disabled="s._pending || archivingSesijaId === s.id || imaGrane(s)"
                    @click.stop="archiveSession(s)"
                    aria-label="Arhiviraj čet"
                    :title="imaGrane(s) ? 'Nije moguće arhivirati čet koji ima grane' : 'Arhiviraj čet'"
                  >
                    {{ archivingSesijaId === s.id ? '…' : '➘' }}
                  </button>
                  <button
                    class="btn-delete-session"
                    :disabled="s._pending || deletingSessionId === s.id || imaGrane(s)"
                    @click.stop="deleteSession(s)"
                    aria-label="Obriši čet"
                    :title="imaGrane(s) ? 'Nije moguće obrisati čet koji ima grane' : 'Obriši čet'"
                  >
                    {{ deletingSessionId === s.id ? '…' : '✕' }}
                  </button>
                </div>
              </div>
            </nav>

            <nav v-else class="session-list archive-list">
              <p class="archive-notice">Arhivirane sesije se automatski brišu 30 dana od arhiviranja.</p>
              <p v-if="!archivedSessions.length" class="empty-state">Nemate arhiviranih četova.</p>
              <div
                v-for="s in archivedSessions"
                :key="s.id"
                class="session-item archived-item"
                :class="{ active: s.id === activeSessionId && viewMode === 'conversation' }"
                @click="selectSession(s.id)"
              >
                <div class="session-name-wrap">
                  <span class="session-name">{{ sessionLabel(s) }}</span>
                  <span class="archive-date">Arhivirano: {{ formatDate(s.datumArhiviranjaCS) }}</span>
                </div>
                <div class="session-right">
                  <span v-if="s.imaGrane" class="branch-pill" title="Ova sesija ima grane (verzije)">grane</span>
                  <button class="btn-unarchive-session" :disabled="unarchivingSesijaId === s.id"
                          @click.stop="unarchiveSession(s)" title="Vrati iz arhive">
                    {{ unarchivingSesijaId === s.id ? '…' : '↩' }}
                  </button>
                  <button class="btn-delete-session" :disabled="deletingSessionId === s.id || imaGrane(s)"
                          @click.stop="deleteSession(s)"
                          :title="imaGrane(s) ? 'Nije moguće obrisati čet koji ima grane' : 'Obriši'">
                    {{ deletingSessionId === s.id ? '…' : '✕' }}
                  </button>
                </div>
              </div>
            </nav>
          </aside>

          <!-- GRANIČNIK IZMEĐU KOLONA -->
          <div class="column-gutter" aria-hidden="true"></div>

          <!-- DESNA KOLONA: aktivni čet ili compose -->
          <section class="conversation-panel">
            <!-- Stanje: kucanje nove poruke / odabir asistenta -->
            <template v-if="viewMode === 'compose'">
              <div class="compose-state">
                <div class="compose-box">
                  <h1 class="compose-title">Novi čet</h1>
                  <p class="compose-subtitle">Izaberite asistenta i započnite razgovor na engleskom jeziku.</p>

                  <p v-if="newSessionError" class="error-msg">{{ newSessionError }}</p>

                  <form class="compose-form" @submit.prevent="createSession">
                    <textarea
                      v-model="newSessionForm.sadrzajPoruke"
                      class="compose-textarea"
                      rows="3"
                      placeholder="Poruka sa pitanjem"
                      :disabled="creatingSession"
                    ></textarea>

                    <template v-if="newSessionForm.tipAgentaCS === 'AGENT_KNJIGE'">
                      <p v-if="composeSlikaError" class="error-msg">{{ composeSlikaError }}</p>
                      <div v-if="composeSlikaPreviewUrl" class="slika-preview-row">
                        <img :src="composeSlikaPreviewUrl" class="slika-preview" alt="Pregled slike za slanje" />
                        <span class="slika-preview-label">Slika će biti priložena uz prvu poruku</span>
                        <button type="button" class="btn-remove-slika" @click="clearComposeSlika" :disabled="creatingSession" aria-label="Ukloni sliku">✕</button>
                      </div>
                      <div class="image-picker-row">
                        <label class="btn-pick-image" :class="{ disabled: creatingSession }">
                          📎 {{ composeSlikaFile ? 'Promeni sliku' : 'Priloži sliku' }}
                          <input
                            ref="composeFileInputEl"
                            type="file"
                            accept="image/png,image/jpeg,image/webp"
                            class="visually-hidden-file-input"
                            @change="onComposeSlikaSelected"
                            :disabled="creatingSession"
                          />
                        </label>
                        <span class="image-picker-hint">JPEG, PNG ili WebP, do 10MB</span>
                      </div>
                    </template>

                    <div class="compose-actions">
                      <select
                        v-model="newSessionForm.tipAgentaCS"
                        class="compose-agent-select"
                        :disabled="creatingSession"
                        @change="onComposeAgentChange"
                      >
                        <option value="AGENT_KNJIGE">Asistent za knjige</option>
                        <option value="AGENT_RECENZIJE">Asistent za recenzije</option>
                      </select>

                      <button
                        type="submit"
                        class="btn-send"
                        :disabled="creatingSession || !newSessionForm.sadrzajPoruke.trim() || healthChecked && !healthOk" 
                        aria-label="Pošalji"
                      >
                        {{ creatingSession ? '…' : '➤' }}
                      </button>
                    </div>
                  </form>
                </div>
              </div>
            </template>

            <!-- Stanje: aktivan postojeći (ili tek pokrenut) čet -->
            <template v-else-if="viewMode === 'conversation' && activeSession">
              <header class="conversation-header">
                <h1 class="conversation-title">{{ sessionLabel(activeSession) }}</h1>
                <span class="agent-pill" :class="agentPillClass(activeSession.tipAgentaCS)">
                  {{ agentLabel(activeSession.tipAgentaCS) }}
                </span>
              </header>

              <p v-if="loadingMessages" class="loading-msg">Učitavanje poruka...</p>
              <p v-if="messagesError" class="error-msg">{{ messagesError }}</p>

              <div ref="messagesEl" class="messages-area">
                <div
                  v-for="m in messages"
                  :key="m.id"
                  class="msg-row"
                  :class="m.tipCP === 'CLAN' ? 'user-row' : 'agent-row'"
                >
                  <div class="msg-bubble" :class="m.tipCP === 'CLAN' ? 'user-bubble' : 'agent-bubble'">
                    <p v-if="m._pending" class="agent-pending">Agent odgovara...</p>

                    <!-- Edit mod za poruku člana -->
                    <div v-else-if="m.tipCP === 'CLAN' && editingMessageId === m.id" class="msg-edit-box">
                      <textarea
                        v-model="editDraft"
                        class="msg-edit-textarea"
                        rows="3"
                        :disabled="editingInFlight"
                      ></textarea>

                      <template v-if="activeSession?.tipAgentaCS === 'AGENT_KNJIGE'">
                        <p v-if="editSlikaError" class="error-msg">{{ editSlikaError }}</p>

                        <!-- Nova slika odabrana za zamenu -->
                        <div v-if="editSlikaPreviewUrl" class="slika-preview-row">
                          <img :src="editSlikaPreviewUrl" class="slika-preview" alt="Nova slika" />
                          <span class="slika-preview-label">Nova slika (zamenjuje postojeću)</span>
                          <button type="button" class="btn-remove-slika" @click="clearNovoOdabranuEditSliku" :disabled="editingInFlight" aria-label="Otkaži novu sliku">✕</button>
                        </div>

                        <!-- Postojeća slika poruke, ako je ima i nije zatraženo brisanje/zamena -->
                        <div v-else-if="editOriginalnaSlikaUrl && !editUkloniSliku" class="slika-preview-row">
                          <img :src="editOriginalnaSlikaUrl" class="slika-preview" alt="Trenutna slika" />
                          <span class="slika-preview-label">Trenutna slika</span>
                        </div>

                        <!-- Slika je zatražena za brisanje -->
                        <p v-else-if="editOriginalnaSlikaUrl && editUkloniSliku" class="slika-preview-removed">
                          Slika će biti uklonjena.
                        </p>

                        <div class="msg-edit-slika-actions">
                          <label
                            class="btn-pick-image"
                            :class="{ disabled: editingInFlight }"
                          >
                            📎 {{ editSlikaFile ? 'Promeni sliku' : 'Priloži sliku' }}
                            <input
                              ref="editFileInputEl"
                              type="file"
                              accept="image/png,image/jpeg,image/webp"
                              class="visually-hidden-file-input"
                              @change="onEditSlikaSelected"
                              :disabled="editingInFlight"
                            />
                          </label>
                          <button
                            v-if="editOriginalnaSlikaUrl"
                            type="button"
                            class="btn-secondary2 btn-toggle-ukloni-slika"
                            @click="toggleUkloniSlikuEdit"
                            :disabled="editingInFlight"
                          >{{ editUkloniSliku ? 'Vrati sliku' : 'Ukloni sliku' }}</button>
                        </div>
                      </template>

                      <p v-if="editError" class="error-msg">{{ editError }}</p>
                      <div class="modal-actions">
                        <button
                          type="button"
                          class="btn-secondary2"
                          @click="cancelEditMessage"
                          :disabled="editingInFlight"
                        >Otkažite</button>
                        <button
                          type="button"
                          class="btn-primary"
                          @click="submitEditMessage(m)"
                          :disabled="editingInFlight || !editDraft.trim()"
                        >{{ editingInFlight ? 'Čuvam...' : 'Sačuvajte' }}</button>
                      </div>
                    </div>

                    <div v-else class="msg-content">
                      <img v-if="m._slikaUrl" :src="m._slikaUrl" class="msg-image" alt="Priložena slika" />
                      <p class="msg-text">{{ m.sadrzajCP }}</p>
                    </div>

                    <details
                      v-if="m.tipCP === 'AI_ASISTENT' && !m._pending && hasIzvori(m)"
                      class="izvori-details"
                    >
                      <summary class="izvori-summary">
                        {{ izvoriNaslov(m) }}
                      </summary>
                      <ul class="izvori-list">
                        <li v-for="(izvor, idx) in izvoriZaPrikaz(m)" :key="idx" class="izvori-item">
                          <template v-if="m.tipAgentaIzvora === 'AGENT_RECENZIJE'">
                            <span class="izvor-naslov">Recenzija {{ izvor.reviewId || izvor.id }}</span>
                            <span v-if="izvor.isbn"> — ISBN: {{ izvor.isbn }}</span>
                            <span v-if="izvor.rating != null"> — ocena: {{ izvor.rating }}/5</span>
                          </template>
                          <template v-else>
                            <span class="izvor-naslov">{{ izvor.naslov }}</span>
                            <span v-if="izvor.autor"> od {{ izvor.autor }}</span>
                          </template>
                          <span v-if="izvor.skor != null" class="izvor-skor">{{ formatSkor(izvor.skor) }}</span>
                        </li>
                      </ul>
                    </details>

                    <div v-if="m.tipCP === 'AI_ASISTENT' && !m._pending" class="rating-row">
                      <button class="btn-rate" :class="{ rated: m._ocena != null }" @click="openRatingPopup(m)" :disabled="activeSession?.arhivirano">
                        {{ m._ocena != null ? `Ocenjeno ★${m._ocena}` : 'Oceni' }}
                      </button>
                    </div>

                    <div
                      v-else-if="m.tipCP === 'CLAN' && !m._pending && editingMessageId !== m.id && canEditMessage(m)"
                      class="rating-row"
                    >
                      <button class="btn-rate" @click="startEditMessage(m)">
                        Izmeni
                      </button>
                    </div>
                  </div>
                </div>
              </div>
              
              <div v-if="activeSession?.arhivirano" class="archived-notice-bar">
                Ova sesija je arhivirana. Vrati je iz arhive da bi nastavio razgovor.
              </div>
              <template v-else>
                <p v-if="slikaError" class="error-msg">{{ slikaError }}</p>
                <div v-if="slikaPreviewUrl" class="slika-preview-row">
                  <img :src="slikaPreviewUrl" class="slika-preview" alt="Pregled slike za slanje" />
                  <button type="button" class="btn-remove-slika" @click="clearSlika" :disabled="sending" aria-label="Ukloni sliku">✕</button>
                </div>
                <form class="message-form" @submit.prevent="sendMessage">
                  <label
                    v-if="activeSession?.tipAgentaCS === 'AGENT_KNJIGE'"
                    class="btn-pick-image btn-pick-image-inline"
                    :class="{ disabled: sending }"
                    title="Priloži sliku"
                    aria-label="Priloži sliku"
                  >
                    📎
                    <input
                      ref="fileInputEl"
                      type="file"
                      accept="image/png,image/jpeg,image/webp"
                      class="visually-hidden-file-input"
                      @change="onSlikaSelected"
                      :disabled="sending"
                    />
                  </label>
                  <input
                    v-model="draft"
                    type="text"
                    class="message-input"
                    placeholder="Poruka sa pitanjem"
                    :disabled="sending"
                  />
                  <button type="submit" class="btn-send" :disabled="sending || !draft.trim()" aria-label="Pošalji">
                    ➤
                  </button>
                </form>
              </template>
            </template>

            <!-- Stanje: ništa odabrano -->
            <div v-else class="no-session-state">
              <p>Izaberite postojeći čet sa leve strane ili pokrenite novi.</p>
            </div>
          </section>
        </div>
      </template>
    </main>

    <!-- POPUP: ocenjivanje poruke -->
    <Teleport to="body">
      <Transition name="modal-pop">
        <div v-if="ratingPopup.open" class="modal-overlay" @click.self="closeRatingPopup">
          <div class="modal-box rating-modal">
            <h2>Ocenite odgovor</h2>
          
            <div class="form-group">
              <label>Broj zvezdica</label>
              <div class="star-picker">
                <button
                  v-for="star in 5"
                  :key="star"
                  type="button"
                  class="star-btn"
                  :class="{ filled: star <= ratingPopup.ocena }"
                  @click="ratingPopup.ocena = star"
                >★</button>
              </div>
            </div>
          
            <div class="form-group">
              <label for="rating-comment">Komentar</label>
              <textarea
                id="rating-comment"
                v-model="ratingPopup.komentar"
                class="form-textarea"
                rows="3"
                placeholder="Opišite zašto ste dali ovu ocenu (opciono)..."
              ></textarea>
            </div>
          
            <p v-if="ratingPopup.error" class="error-msg">{{ ratingPopup.error }}</p>
          
            <div class="modal-actions">
              <button class="btn-secondary2" type="button" @click="closeRatingPopup" :disabled="ratingPopup.saving">
                Otkažite
              </button>
              <button
                class="btn-primary"
                type="button"
                @click="submitRating"
                :disabled="ratingPopup.saving || !ratingPopup.ocena"
              >
                {{ ratingPopup.saving ? 'Čuvam...' : 'Sačuvajte ocenu' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import SidebarNav from '../components/Sidebar.vue'
import { useAuthStore } from '../stroage/auth.js'
import { cetSesijaApi, cetPorukaApi, chatHealthApi } from '../services/api.js'

const authStore = useAuthStore()

const authorized = ref(false)

const healthChecked = ref(false)
const healthOk = ref(true)

const sessions = ref([])

const showArchive = ref(false)          
const archivingSesijaId = ref(null)     
const unarchivingSesijaId = ref(null)

const loadingSessions = ref(false)
const sessionsError = ref('')

// 'compose' | 'conversation'
const viewMode = ref('compose')

const activeSessionId = ref(null)
const messages = ref([])
const loadingMessages = ref(false)
const messagesError = ref('')
const messagesEl = ref(null)

const draft = ref('')
const sending = ref(false)

const slikaFile = ref(null)
const slikaPreviewUrl = ref('')
const fileInputEl = ref(null)
const slikaError = ref('')

const creatingSession = ref(false)
const deletingSessionId = ref(null)
const newSessionError = ref('')
const newSessionForm = ref({
  tipAgentaCS: 'AGENT_KNJIGE',
  sadrzajPoruke: ''
})

const composeSlikaFile = ref(null)
const composeSlikaPreviewUrl = ref('')
const composeFileInputEl = ref(null)
const composeSlikaError = ref('')

const ratingPopup = ref({
  open: false,
  message: null,
  ocena: 0,
  komentar: '',
  saving: false,
  error: ''
})

// ── Editovanje (ažuriranje) poruke člana - kreira novu granu sesije ────
const editingMessageId = ref(null)   // id poruke koja se trenutno edituje (ulazak u edit mod)
const editDraft = ref('')
const editingInFlight = ref(false)
const editError = ref('')

// Slika u edit modu - tri moguća stanja pri slanju, isto kao na backendu:
//   editSlikaFile postavljen      -> slika se zamenjuje novom
//   editUkloniSliku === true      -> slika se briše
//   ni jedno ni drugo              -> originalna slika (editOriginalnaSlikaUrl) ostaje
const editOriginalnaSlikaUrl = ref('')  // originalna slika
const editSlikaFile = ref(null)
const editSlikaPreviewUrl = ref('')
const editFileInputEl = ref(null)
const editUkloniSliku = ref(false)
const editSlikaError = ref('')

const AGENT_LABELS = {
  AGENT_KNJIGE: 'Knjige',
  AGENT_RECENZIJE: 'Recenzije'
}
const agentLabel = (tip) => AGENT_LABELS[tip] || tip || '—'
const agentPillClass = (tip) =>
  tip === 'AGENT_RECENZIJE' ? 'pill-recenzije' : 'pill-knjige'

// Server čuva sliku kao čist base64 string (kolona slikaBase64), bez
// data: prefiksa - ovde ga dodajemo da bi <img> mogao da je prikaže.
// Ne znamo tačan MIME tip sa servera, ali image/jpeg je dovoljno jer
// browseri u praksi ignorišu nepoklapanje MIME-a/sadržaja kod data URL-ova.
function slikaBase64ToDataUrl(slikaBase64) {
  if (!slikaBase64) return null
  if (slikaBase64.startsWith('data:')) return slikaBase64
  return `data:image/jpeg;base64,${slikaBase64}`
}

// ── Izvori (knjige / recenzije) na osnovu kojih je AI agent dao odgovor ──
function izvoriZaPrikaz(m) {
  if (m.tipAgentaIzvora === 'AGENT_RECENZIJE') {
    return m.izvoriRecenzije || []
  }
  return m.izvoriKnjige || []
}

function hasIzvori(m) {
  return izvoriZaPrikaz(m).length > 0
}

function izvoriNaslov(m) {
  return m.tipAgentaIzvora === 'AGENT_RECENZIJE'
    ? 'Recenzije korišćene za odgovor'
    : 'Knjige korišćene za odgovor'
}

function formatSkor(skor) {
  return Number(skor).toFixed(4)
}

const activeSession = computed(() =>
  sessions.value.find((s) => s.id === activeSessionId.value) || null
)

const activeSessions  = computed(() => sessions.value.filter(s => !s.arhivirano && !s._pending))
const archivedSessions = computed(() => sessions.value.filter(s => s.arhivirano))

function sessionLabel(session) {
  return session?.naslovCS || 'Bez naziva'
}

// Korisnik ne može da arhivira ili obriše čet ako ima grane (isto pravilo
// kao na backendu - ovde ga samo unapred provervamo da ne šaljemo poziv
// koji znamo da će vratiti 409).
function imaGrane(session) {
  return !!session?.imaGrane
}

onMounted(async () => {
  const role = authStore.getRole()
  authorized.value = role === 'CLAN'
  if (!authorized.value) return

  await checkHealth()
  await loadSessions()
})

async function checkHealth() {
  try {
    const res = await chatHealthApi.provera()
    healthOk.value = !!res.data?.ollama_available
  } catch {
    healthOk.value = false
  } finally {
    healthChecked.value = true
  }
}

async function loadSessions() {
  loadingSessions.value = true
  sessionsError.value = ''
  try {
    const res = await cetSesijaApi.sve()
    sessions.value = res.data || []
  } catch (e) {
    sessionsError.value = e.response?.status === 403
      ? 'Sesija je istekla. Prijavite se ponovo.'
      : 'Greška pri učitavanju četova.'
    sessions.value = []
  } finally {
    loadingSessions.value = false
  }
}

async function selectSession(id) {
  if (id === activeSessionId.value && viewMode.value === 'conversation') return
  cancelEditMessage()
  activeSessionId.value = id
  viewMode.value = 'conversation'
  await loadMessages(id)
}

async function loadMessages(id) {
  loadingMessages.value = true
  messagesError.value = ''
  messages.value = []
  try {
    const res = await cetSesijaApi.jedna(id)
    const tipAgenta = res.data?.tipAgentaCS
    messages.value = (res.data?.poruke || []).map((p) => ({ ...p, tipAgentaIzvora: tipAgenta, _slikaUrl: slikaBase64ToDataUrl(p.slikaBase64) }))
    await scrollToBottom()
    await loadRatings()
  } catch (e) {
    if (e.response?.status === 404) {
      messagesError.value = 'Ovaj čet ne postoji ili je obrisan.'
    } else {
      messagesError.value = 'Greška pri učitavanju poruka.'
    }
  } finally {
    loadingMessages.value = false
  }
}

async function loadRatings() {
  const agentMessages = messages.value.filter((m) => m.tipCP === 'AI_ASISTENT')
  await Promise.all(
    agentMessages.map(async (m) => {
      try {
        const res = await cetPorukaApi.ocena(m.id)
        m._ocena = res.data?.ocenaCP ?? null
        m._komentar = res.data?.komentarCP ?? ''
      } catch {
        m._ocena = null
        m._komentar = ''
      }
    })
  )
}

function openRatingPopup(message) {
  ratingPopup.value = {
    open: true,
    message,
    ocena: message._ocena ?? 0,
    komentar: message._komentar ?? '',
    saving: false,
    error: ''
  }
}

function closeRatingPopup() {
  if (ratingPopup.value.saving) return
  ratingPopup.value.open = false
}

async function submitRating() {
  const popup = ratingPopup.value
  if (!popup.ocena || !popup.message) return

  popup.saving = true
  popup.error = ''
  try {
    const res = await cetPorukaApi.oceni(popup.message.id, popup.ocena, popup.komentar.trim())
    popup.message._ocena = res.data?.ocenaCP ?? popup.ocena
    popup.message._komentar = res.data?.komentarCP ?? popup.komentar.trim()
    popup.open = false
  } catch {
    popup.error = 'Greška pri čuvanju ocene. Pokušajte ponovo.'
  } finally {
    popup.saving = false
  }
}

async function sendMessage() {
  const sadrzaj = draft.value.trim()
  if (!sadrzaj || !activeSessionId.value) return

  sending.value = true
  draft.value = ''

  const slika = slikaFile.value
  // Lokalni object URL za trenutni prikaz dok čekamo odgovor servera.
  // Čuvamo ga posebno od slikaPreviewUrl da clearSlika() ne oslobodi
  // URL koji upravo prikazujemo u poruci.
  const lokalniPreviewZaPoruku = slikaPreviewUrl.value
  slikaFile.value = null
  slikaPreviewUrl.value = ''
  if (fileInputEl.value) fileInputEl.value.value = ''

  const userMsg = {
    id: `temp-user-${Date.now()}`,
    tipCP: 'CLAN',
    sadrzajCP: sadrzaj,
    _slikaUrl: lokalniPreviewZaPoruku || null
  }
  const pendingAgentMsg = {
    id: `temp-agent-${Date.now()}`,
    tipCP: 'AI_ASISTENT',
    sadrzajCP: '',
    _pending: true
  }
  messages.value.push(userMsg, pendingAgentMsg)
  await scrollToBottom()

  try {
    const res = await cetPorukaApi.nova(activeSessionId.value, sadrzaj, slika)
    const data = res.data || {}
    userMsg.id = data.porukaClana?.id ?? userMsg.id
    // Server je sada izvor istine za sliku (trajno sačuvana kao
    // slikaBase64) - zamenjujemo privremeni object URL data URL-om sa
    // servera, koji ostaje ispravan i posle reload-a/loadMessages.
    if (data.porukaClana?.slikaBase64) {
      if (lokalniPreviewZaPoruku) URL.revokeObjectURL(lokalniPreviewZaPoruku)
      userMsg._slikaUrl = slikaBase64ToDataUrl(data.porukaClana.slikaBase64)
    }
    pendingAgentMsg.id = data.porukaAgenta?.id ?? pendingAgentMsg.id
    pendingAgentMsg.sadrzajCP = data.porukaAgenta?.sadrzajCP ?? ''
    pendingAgentMsg._pending = false
    pendingAgentMsg._ocena = null
    pendingAgentMsg._komentar = ''
  } catch (e) {
    pendingAgentMsg._pending = false
    pendingAgentMsg.sadrzajCP = e.response?.status === 404
      ? 'Ovaj čet više ne postoji.'
      : 'Greška pri dobijanju odgovora. Pokušajte ponovo.'
  } finally {
    sending.value = false
    await scrollToBottom()
  }
}

async function scrollToBottom() {
  await nextTick()
  if (messagesEl.value) {
    messagesEl.value.scrollTop = messagesEl.value.scrollHeight
  }
}

const DOZVOLJENI_TIPOVI_SLIKE = ['image/png', 'image/jpeg', 'image/webp']
const MAX_VELICINA_SLIKE = 10 * 1024 * 1024 // 10MB

function onSlikaSelected(e) {
  const file = e.target.files?.[0]
  if (!file) return

  slikaError.value = ''

  if (!DOZVOLJENI_TIPOVI_SLIKE.includes(file.type)) {
    slikaError.value = 'Dozvoljene su samo JPEG, PNG i WebP slike.'
    e.target.value = ''
    return
  }
  if (file.size > MAX_VELICINA_SLIKE) {
    slikaError.value = 'Slika ne može biti veća od 10MB.'
    e.target.value = ''
    return
  }

  if (slikaPreviewUrl.value) URL.revokeObjectURL(slikaPreviewUrl.value)
  slikaFile.value = file
  slikaPreviewUrl.value = URL.createObjectURL(file)
}

function clearSlika() {
  if (slikaPreviewUrl.value) URL.revokeObjectURL(slikaPreviewUrl.value)
  slikaFile.value = null
  slikaPreviewUrl.value = ''
  slikaError.value = ''
  if (fileInputEl.value) fileInputEl.value.value = ''
}

function onComposeSlikaSelected(e) {
  const file = e.target.files?.[0]
  if (!file) return

  composeSlikaError.value = ''

  if (!DOZVOLJENI_TIPOVI_SLIKE.includes(file.type)) {
    composeSlikaError.value = 'Dozvoljene su samo JPEG, PNG i WebP slike.'
    e.target.value = ''
    return
  }
  if (file.size > MAX_VELICINA_SLIKE) {
    composeSlikaError.value = 'Slika ne može biti veća od 10MB.'
    e.target.value = ''
    return
  }

  if (composeSlikaPreviewUrl.value) URL.revokeObjectURL(composeSlikaPreviewUrl.value)
  composeSlikaFile.value = file
  composeSlikaPreviewUrl.value = URL.createObjectURL(file)
}

function clearComposeSlika() {
  if (composeSlikaPreviewUrl.value) URL.revokeObjectURL(composeSlikaPreviewUrl.value)
  composeSlikaFile.value = null
  composeSlikaPreviewUrl.value = ''
  composeSlikaError.value = ''
  if (composeFileInputEl.value) composeFileInputEl.value.value = ''
}

// Slika je podržana samo za asistenta za knjige - isto pravilo kao na
// backendu (CetSesijaService). Ako korisnik prebaci na recenzije nakon
// što je već odabrao sliku, uklanjamo je da ne dobijemo 400 pri slanju.
function onComposeAgentChange() {
  if (newSessionForm.value.tipAgentaCS !== 'AGENT_KNJIGE') {
    clearComposeSlika()
  }
}

function openCompose() {
  cancelEditMessage()
  newSessionForm.value = { tipAgentaCS: 'AGENT_KNJIGE', sadrzajPoruke: '' }
  newSessionError.value = ''
  clearComposeSlika()
  viewMode.value = 'compose'
}

async function createSession() {
  newSessionError.value = ''
  const sadrzaj = newSessionForm.value.sadrzajPoruke.trim()
  if (!sadrzaj) {
    newSessionError.value = 'Unesite poruku da biste započeli čet.'
    return
  }

  creatingSession.value = true

  const slika = composeSlikaFile.value
  // Lokalni object URL za trenutni prikaz dok čekamo odgovor servera -
  // isti obrazac kao u sendMessage(). Čuvamo ga posebno da clearComposeSlika()
  // (pozvana ispod) ne oslobodi URL koji upravo prikazujemo u poruci.
  const lokalniPreviewZaPoruku = composeSlikaPreviewUrl.value
  composeSlikaFile.value = null
  composeSlikaPreviewUrl.value = ''
  if (composeFileInputEl.value) composeFileInputEl.value.value = ''

  // Optimistički ubacujemo privremenu sesiju u levu listu da korisnik
  // odmah vidi da je čet pokrenut, dok čekamo prawi odgovor sa servera.
  const tempId = `temp-session-${Date.now()}`
  const tipAgenta = newSessionForm.value.tipAgentaCS
  const tempSession = {
    id: tempId,
    naslovCS: 'Novo ćaskanje...',
    tipAgentaCS: tipAgenta,
    _pending: true
  }
  sessions.value = [tempSession, ...sessions.value]
  activeSessionId.value = tempId
  viewMode.value = 'conversation'
  messages.value = [
    { id: `temp-user-${Date.now()}`, tipCP: 'CLAN', sadrzajCP: sadrzaj, _slikaUrl: lokalniPreviewZaPoruku || null },
    { id: `temp-agent-${Date.now()}`, tipCP: 'AI_ASISTENT', sadrzajCP: '', _pending: true }
  ]
  await scrollToBottom()

  try {
    const res = await cetSesijaApi.nova(tipAgenta, sadrzaj, slika)
    const newSession = res.data

    // Zamenjujemo privremenu sesiju pravom (server vraća naslovCS
    // već generisan u istom odgovoru, pa nema potrebe za dodatnim fetch-om).
    const idx = sessions.value.findIndex((s) => s.id === tempId)
    if (idx !== -1 && newSession?.id) {
      sessions.value.splice(idx, 1, newSession)
    } else {
      await loadSessions()
    }

    if (newSession?.id) {
      activeSessionId.value = newSession.id
      // Server je sada izvor istine za sliku (trajno sačuvana kao
      // slikaBase64) - isti obrazac kao u sendMessage().
      if (lokalniPreviewZaPoruku) URL.revokeObjectURL(lokalniPreviewZaPoruku)
      messages.value = (newSession.poruke || []).map((p) => ({
        ...p,
        tipAgentaIzvora: newSession.tipAgentaCS,
        _ocena: null,
        _komentar: '',
        _slikaUrl: slikaBase64ToDataUrl(p.slikaBase64)
      }))
      await scrollToBottom()
    }
  } catch (e) {
    // Uklanjamo privremenu sesiju jer kreiranje nije uspelo, i vraćamo
    // korisnika na compose ekran sa porukom o grešci da ne izgubi tekst.
    sessions.value = sessions.value.filter((s) => s.id !== tempId)
    activeSessionId.value = null
    viewMode.value = 'compose'

    // Vraćamo odabranu sliku na compose ekran da je korisnik ne izgubi
    // zajedno sa porukom, isto kao i tekst (sadrzajPoruke ostaje u formi).
    if (slika) {
      composeSlikaFile.value = slika
      composeSlikaPreviewUrl.value = lokalniPreviewZaPoruku || URL.createObjectURL(slika)
    }

    if (e.response?.status === 400) {
      newSessionError.value = 'Nedostaje sadržaj poruke ili tip asistenta.'
    } else if (e.response?.status === 403) {
      newSessionError.value = 'Nemate dozvolu za ovu akciju. Prijavite se ponovo.'
    } else {
      newSessionError.value = 'Greška pri pokretanju novog četa.'
    }
  } finally {
    creatingSession.value = false
  }
}

async function deleteSession(session) {
  if (session._pending) return
  if (imaGrane(session)) {
    sessionsError.value = 'Nije moguće obrisati čet koji ima grane. Obrišite prvo sve grane.'
    return
  }
  if (!confirm(`Obrisati čet "${sessionLabel(session)}"?`)) return

  deletingSessionId.value = session.id
  try {
    await cetSesijaApi.obrisi(session.id)
    await loadSessions()

    // Ako je obrisana aktivna sesija, vrati na compose ekran
    if (activeSessionId.value === session.id) {
      activeSessionId.value = null
      viewMode.value = 'compose'
      messages.value = []
    }
  } catch (e) {
    if (e.response?.status === 404) {
      sessionsError.value = 'Čet sesija nije pronađena.'
    } else if (e.response?.status === 409) {
      sessionsError.value = 'Nije moguće obrisati čet koji ima grane.'
    } else {
      sessionsError.value = 'Greška pri brisanju. Pokušajte ponovo.'
    }
  } finally {
    deletingSessionId.value = null
  }
}

async function archiveSession(session) {
  if (session._pending) return
  if (imaGrane(session)) {
    sessionsError.value = 'Nije moguće arhivirati čet koji ima grane.'
    return
  }
  if (!confirm(`Arhivirati čet "${sessionLabel(session)}"?`)) return

  archivingSesijaId.value = session.id
  try {
    const res = await cetSesijaApi.arhiviraj(session.id)
    // Ažuriramo lokalnu sesiju sa novim podacima (arhivirano: true, datumArhiviranjaCS)
    const idx = sessions.value.findIndex(s => s.id === session.id)
    if (idx !== -1) sessions.value.splice(idx, 1, res.data)
    // Ako je bila aktivna, osvežimo je
    if (activeSessionId.value === session.id && viewMode.value === 'conversation') {
      await loadMessages(session.id)
    }
  } catch (e) {
    if (e.response?.status === 409) {
      sessionsError.value = 'Sesija je već arhivirana ili ima grane.'
    } else {
      sessionsError.value = 'Greška pri arhiviranju.'
    }
  } finally {
    archivingSesijaId.value = null
  }
}

async function unarchiveSession(session) {
  if (!confirm(`Vratiti čet "${sessionLabel(session)}" iz arhive?`)) return

  unarchivingSesijaId.value = session.id
  try {
    const res = await cetSesijaApi.vrati(session.id)
    const idx = sessions.value.findIndex(s => s.id === session.id)
    if (idx !== -1) sessions.value.splice(idx, 1, res.data)
    if (activeSessionId.value === session.id && viewMode.value === 'conversation') {
      await loadMessages(session.id)
    }
  } catch (e) {
    sessionsError.value = e.response?.status === 409
      ? 'Sesija nije arhivirana.'
      : 'Greška pri vraćanju iz arhive.'
  } finally {
    unarchivingSesijaId.value = null
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString('sr-RS', {
    day: '2-digit', month: '2-digit', year: 'numeric'
  })
}

// ── Editovanje poruke člana ─────────────────────────────────────────
// Dozvoljeno je samo za poruke tipa CLAN, i samo dok sesija nije
// arhivirana (isto pravilo važi i na backendu - frontend ga samo
// proverava unapred radi boljeg UX-a).
function canEditMessage(message) {
  return message?.tipCP === 'CLAN' && !message?._pending && !activeSession.value?.arhivirano
}

function startEditMessage(message) {
  if (!canEditMessage(message)) return
  editingMessageId.value = message.id
  editDraft.value = message.sadrzajCP
  editError.value = ''

  editOriginalnaSlikaUrl.value = message._slikaUrl || ''
  editSlikaFile.value = null
  editSlikaPreviewUrl.value = ''
  editUkloniSliku.value = false
  editSlikaError.value = ''
}

function cancelEditMessage() {
  editingMessageId.value = null
  editDraft.value = ''
  editError.value = ''

  if (editSlikaPreviewUrl.value) URL.revokeObjectURL(editSlikaPreviewUrl.value)
  editOriginalnaSlikaUrl.value = ''
  editSlikaFile.value = null
  editSlikaPreviewUrl.value = ''
  editUkloniSliku.value = false
  editSlikaError.value = ''
  if (editFileInputEl.value) editFileInputEl.value.value = ''
}

function onEditSlikaSelected(e) {
  const file = e.target.files?.[0]
  if (!file) return

  editSlikaError.value = ''

  if (!DOZVOLJENI_TIPOVI_SLIKE.includes(file.type)) {
    editSlikaError.value = 'Dozvoljene su samo JPEG, PNG i WebP slike.'
    e.target.value = ''
    return
  }
  if (file.size > MAX_VELICINA_SLIKE) {
    editSlikaError.value = 'Slika ne može biti veća od 10MB.'
    e.target.value = ''
    return
  }

  if (editSlikaPreviewUrl.value) URL.revokeObjectURL(editSlikaPreviewUrl.value)
  editSlikaFile.value = file
  editSlikaPreviewUrl.value = URL.createObjectURL(file)
  // Nova slika i "ukloni sliku" su međusobno isključivi - biranje nove
  // slike automatski otkazuje prethodno zatraženo brisanje.
  editUkloniSliku.value = false
}

function clearNovoOdabranuEditSliku() {
  if (editSlikaPreviewUrl.value) URL.revokeObjectURL(editSlikaPreviewUrl.value)
  editSlikaFile.value = null
  editSlikaPreviewUrl.value = ''
  if (editFileInputEl.value) editFileInputEl.value.value = ''
}

function toggleUkloniSlikuEdit() {
  // Ako je korisnik upravo odabrao novu sliku, klik na "ukloni sliku"
  // odbacuje tu novu sliku i prelazi na brisanje originalne.
  if (editSlikaFile.value) clearNovoOdabranuEditSliku()
  editUkloniSliku.value = !editUkloniSliku.value
}

async function submitEditMessage(message) {
  const noviSadrzaj = editDraft.value.trim()
  if (!noviSadrzaj) {
    editError.value = 'Poruka mora da ima sadržaj.'
    return
  }

  editingInFlight.value = true
  editError.value = ''
  try {
    // Backend ne menja postojeću poruku/sesiju - kreira NOVU čet sesiju
    // (granu, npr. v2) koja sadrži kopirane poruke do tačke editovanja,
    // izmenjenu poruku i novi odgovor agenta. Vraća CetSesijaDetaljnoDto.
    // Slika ide u jednom od tri stanja: nova (editSlikaFile), brisanje
    // (editUkloniSliku), ili izostavljena (stara slika ostaje na serveru).
    const res = await cetPorukaApi.azuriraj(
      message.id,
      noviSadrzaj,
      editSlikaFile.value,
      editUkloniSliku.value
    )
    const novaSesija = res.data

    // Originalna sesija sada ima grane (imaGrane: true) - ažuriramo
    // je lokalno da se odmah onemoguće arhiviranje/brisanje u UI-ju.
    if (activeSession.value) {
      const idxOriginal = sessions.value.findIndex((s) => s.id === activeSession.value.id)
      if (idxOriginal !== -1) {
        sessions.value.splice(idxOriginal, 1, { ...sessions.value[idxOriginal], imaGrane: true })
      }
    }

    // Dodajemo novu granu u listu sesija i odmah je otvaramo.
    sessions.value = [novaSesija, ...sessions.value]
    activeSessionId.value = novaSesija.id
    viewMode.value = 'conversation'
    messages.value = (novaSesija.poruke || []).map((p) => ({
      ...p,
      _ocena: null,
      _komentar: '',
      _slikaUrl: slikaBase64ToDataUrl(p.slikaBase64)
    }))
    cancelEditMessage()
    await scrollToBottom()
    await loadRatings()
  } catch (e) {
    if (e.response?.status === 404) {
      editError.value = 'Poruka ili sesija ne postoji.'
    } else if (e.response?.status === 409) {
      editError.value = 'Nije moguće editovati poruku u arhiviranoj sesiji.'
    } else if (e.response?.status === 400) {
      editError.value = e.response?.data || 'Nije moguće editovati ovu poruku.'
    } else if (e.response?.status === 502) {
      editError.value = 'Agent trenutno nije dostupan. Pokušajte kasnije.'
    } else {
      editError.value = 'Greška pri ažuriranju poruke. Pokušajte ponovo.'
    }
  } finally {
    editingInFlight.value = false
  }
}

</script>

<style scoped>
.chat-page { display: flex; flex-direction: column; flex: 1; min-height: 0; }

.health-banner {
  background: #fbe6c9;
  color: #7a4f12;
  border-radius: 8px;
  padding: 0.6rem 1rem;
  margin-bottom: 1rem;
  font-size: 0.9rem;
}

.chat-shell {
  display: grid;
  grid-template-columns: 260px 1px 1fr;
  flex: 1;
  min-height: 0;
}

/* ── Graničnik između kolona ─────────────── */
.column-gutter {
  background: #ffffff;
  width: 1px;
  margin: 0 1.5rem;
}

/* ── Leva kolona ──────────────────────────── */
.session-panel {
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding-right: 0.25rem;
}

.btn-new-chat {
  background: var(--btn-primary);
  color: var(--text-light);
  border: none;
  border-radius: 999px;
  padding: 0.7rem 1rem;
  font-weight: 700;
  font-size: 0.95rem;
  cursor: pointer;
  transition: background 0.2s;
  flex-shrink: 0;
}
.btn-new-chat:hover { background: var(--btn-hover); }

.session-list {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  margin-top: 0.9rem;
  overflow-y: auto;
  min-height: 0;
  padding-right: 0.2rem;
}

.session-item {
  background: var(--input-bg);
  border: 2px solid var(--border);
  text-align: left;
  padding: calc(0.75rem - 0.5px) calc(0.9rem - 0.5px);
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-dark);
  cursor: pointer;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.9rem;
  transition: border-color 0.15s;
}
.session-item:hover  { border-color: var(--btn-primary); }
.session-item.active { border-color: var(--btn-primary); }

.session-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  letter-spacing: 0.2px;
}
.session-name-pending { font-style: italic; opacity: 0.75; }

.agent-pill {
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.3px;
  padding: 0.25rem 0.7rem;
  border-radius: 999px;
  flex-shrink: 0;
}
.pill-knjige     { background: #dde8d8; color: #2f5024; }
.pill-recenzije  { background: #dbe6f3; color: #1e4670; }

/* ── Desna kolona ─────────────────────────── */
.conversation-panel {
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding-left: 3.5rem;
}

.conversation-header {
  display: flex;
  align-items: center;
  gap: 0.9rem;
  margin-bottom: 1.2rem;
  flex-shrink: 0;
}
.conversation-title { font-size: 1.8rem; margin: 0; }

.messages-area {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 0.9rem;
  padding-right: 0.3rem;
  margin-bottom: 1rem;
  min-height: 0;
}

.msg-row { display: flex; }
.user-row  { justify-content: flex-end; }
.agent-row { justify-content: flex-start; }

.msg-bubble {
  max-width: 70%;
  border-radius: 14px;
  padding: 0.7rem 1.1rem;
  font-size: 0.95rem;
  line-height: 1.45;
  box-shadow: var(--shadow);
}
.user-bubble  { background: white; border: 1px solid var(--border); color: var(--text-dark); }
.agent-bubble { background: white; border: 1px solid var(--border); color: var(--text-dark); }
.msg-content { display: flex; flex-direction: column; gap: 0.5rem; }
.msg-text { margin: 0; white-space: pre-wrap; }
.msg-image {
  max-width: 220px;
  max-height: 220px;
  border-radius: 10px;
  border: 1px solid var(--border);
  object-fit: cover;
}

.rating-row {
  display: flex;
  align-items: center;
  margin-top: 0.5rem;
  padding-top: 0.4rem;
  border-top: 1px solid var(--border);
}

.btn-rate {
  background: transparent;
  border: 1.5px solid var(--border);
  border-radius: 999px;
  padding: 0.3rem 0.9rem;
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--text-mid);
  cursor: pointer;
  transition: border-color 0.15s, color 0.15s, background 0.15s;
}
.btn-rate:hover { border-color: var(--btn-primary); color: var(--text-dark); }
.btn-rate.rated {
  background: #fbf1dd;
  border-color: #d9a443;
  color: #7a5c1d;
}

.btn-rate:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
}

/* ── Padajući meni: izvori (knjige / recenzije) za odgovor agenta ────── */
.izvori-details {
  margin-top: 0.6rem;
  border: 1.5px solid var(--border);
  border-radius: 10px;
  padding: 0 0.7rem;
  background: var(--card-bg-alt);
}
.izvori-details[open] {
  padding-bottom: 0.6rem;
}

.izvori-summary {
  cursor: pointer;
  list-style: none;
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--text-mid);
  padding: 0.55rem 0;
  user-select: none;
}
.izvori-summary::-webkit-details-marker { display: none; }
.izvori-summary::before {
  content: '▸';
  display: inline-block;
  margin-right: 0.4rem;
  transition: transform 0.15s ease;
}
.izvori-details[open] .izvori-summary::before {
  transform: rotate(90deg);
}
.izvori-summary:hover { color: var(--text-dark); }

.izvori-list {
  margin: 0.2rem 0 0;
  padding-left: 1.1rem;
  display: flex;
  flex-direction: column;
  gap: 0.45rem;
}

.izvori-item {
  font-size: 0.85rem;
  color: var(--text-dark);
  line-height: 1.4;
}

.izvor-naslov { font-weight: 700; }

.izvor-skor {
  display: inline-block;
  margin-left: 0.5rem;
  background: #e3f3e3;
  color: #2d6a2d;
  border-radius: 6px;
  padding: 0.05rem 0.45rem;
  font-size: 0.78rem;
  font-family: 'Courier New', monospace;
}

/* ── Editovanje poruke (kreira granu) ────────────────────── */
.msg-edit-box {
  display: flex;
  flex-direction: column;
  gap: 0rem;
}
.msg-edit-textarea {
  width: 100%;
  border: 1.5px solid var(--border);
  border-radius: 8px;
  padding: 0.5rem 0.7rem;
  font-size: 0.95rem;
  font-family: inherit;
  color: var(--text-dark);
  resize: vertical;
}
.msg-edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 0rem;
}
.msg-edit-actions .btn-secondary2,
.msg-edit-actions .btn-small {
  margin: 0;
}

.message-form {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: var(--input-bg);
  border: 1.5px solid var(--border);
  border-radius: 999px;
  padding: 0.5rem;
  flex-shrink: 0;
}
.message-form .message-input {
  padding-left: 0.4rem;
}
.message-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 0.95rem;
  outline: none;
  color: var(--text-dark);
}
.btn-send {
  background: var(--btn-primary, #7a5c48);
  color: white;
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.btn-send:hover    { opacity: 0.9; }
.btn-send:disabled { opacity: 0.5; cursor: not-allowed; }

/* ── Biranje slike: native <input type="file"> je vizuelno nemoguće
   doterati preko CSS-a (browser "Choose file" dugme ne prati temu), pa
   ga sakrivamo i koristimo <label> kao trigger - klik na label fokusira
   i otvara dijalog vezanog inputa bez ikakvog JS-a za to. ────────────── */
.visually-hidden-file-input {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.btn-pick-image {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  flex-shrink: 0;
  background: var(--card-bg-alt, transparent);
  border: 1.5px solid var(--border);
  border-radius: 999px;
  padding: 0.45rem 0.9rem;
  font-size: 0.85rem;
  color: var(--text-mid);
  cursor: pointer;
  user-select: none;
  transition: border-color 0.15s, color 0.15s, background 0.15s;
}
.btn-pick-image:hover {
  border-color: var(--btn-primary, #7a5c48);
  color: var(--text-dark);
}
.btn-pick-image.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
}

/* Varijanta dugmeta koja se uklapa u zaokruženu traku za slanje poruke
   (pored teksta i dugmeta za slanje) - samo ikonica, bez teksta. */
.btn-pick-image-inline {
  padding: 0.5rem;
  width: 40px;
  height: 40px;
  justify-content: center;
  border-radius: 50%;
  font-size: 1.05rem;
}

.image-picker-row {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  flex-wrap: wrap;
}
.image-picker-hint {
  font-size: 0.78rem;
  color: var(--text-mid);
}

.slika-preview-row {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin-bottom: 0.5rem;
  flex-shrink: 0;
}
.slika-preview {
  width: 56px;
  height: 56px;
  object-fit: cover;
  border-radius: 8px;
  border: 1.5px solid var(--border);
}
.btn-remove-slika {
  background: transparent;
  border: 1.5px solid var(--border);
  border-radius: 50%;
  width: 24px;
  height: 24px;
  font-size: 0.75rem;
  color: var(--text-mid);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.btn-remove-slika:hover { color: #c0392b; border-color: #c0392b; }
.btn-remove-slika:disabled { opacity: 0.4; cursor: not-allowed; }

.slika-preview-label {
  font-size: 0.78rem;
  color: var(--text-mid);
}
.slika-preview-removed {
  font-size: 0.82rem;
  color: #c0392b;
  margin: 0 0 0.5rem 0;
}
.msg-edit-slika-actions {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin-bottom: 0.6rem;
  flex-wrap: wrap;
}
.btn-toggle-ukloni-slika {
  font-size: 0.78rem;
  padding: 0.3rem 0.7rem;
}

.no-session-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-mid);
  text-align: center;
}

/* ── Compose stanje (zamena za stari modal) ── */
.compose-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.compose-box {
  width: 100%;
  max-width: 540px;
  text-align: center;
}

.compose-title { font-size: 1.9rem; margin-bottom: 0.4rem; }
.compose-subtitle { color: var(--text-mid); margin-bottom: 1.4rem; }

.compose-form {
  background: var(--input-bg);
  border: 1.5px solid var(--border);
  border-radius: 18px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  text-align: left;
}

.compose-textarea {
  border: none;
  background: transparent;
  font-size: 1rem;
  color: var(--text-dark);
  resize: vertical;
  outline: none;
  font-family: inherit;
}

.compose-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.compose-agent-select {
  background: var(--card-bg-alt);
  border: 1.5px solid var(--border);
  border-radius: 999px;
  padding: 0.5rem 0.9rem;
  font-size: 0.9rem;
  color: var(--text-dark);
}

/* ── Popup za ocenjivanje ─────────────────── */
.rating-modal { text-align: left; }
.rating-modal h2 { text-align: center; margin-bottom: 1.2rem; }

.star-picker { display: flex; gap: 0.25rem; }
.star-btn {
  background: transparent;
  border: none;
  font-size: 1.6rem;
  cursor: pointer;
  color: #c9c2bb;
  padding: 0.1rem;
  line-height: 1;
}
.star-btn.filled { color: #d9a443; }

.form-group { margin-top: 1rem; display: flex; flex-direction: column; gap: 0.4rem; }
.form-group label { font-size: 0.85rem; font-weight: 600; color: var(--text-mid); }

.form-textarea {
  background: var(--input-bg);
  border: 1.5px solid var(--border);
  border-radius: 8px;
  padding: 0.6rem 0.75rem;
  font-size: 0.95rem;
  color: var(--text-dark);
  resize: vertical;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1.4rem;
}

@media (max-width: 768px) {
  .chat-shell { grid-template-columns: 1fr; }
  .column-gutter { display: none; }
  .session-panel { order: 2; }
  .conversation-panel { order: 1; }
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-box {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.25);
  padding: 24px;
  max-width: 420px;
  width: 90%;
}

.modal-pop-enter-active,
.modal-pop-leave-active {
  transition: opacity 0.18s ease;
}
.modal-pop-enter-active .modal-box,
.modal-pop-leave-active .modal-box {
  transition: transform 0.18s ease, opacity 0.18s ease;
}
.modal-pop-enter-from,
.modal-pop-leave-to {
  opacity: 0;
}
.modal-pop-enter-from .modal-box,
.modal-pop-leave-to .modal-box {
  transform: scale(0.92) translateY(8px);
  opacity: 0;
}

.btn-secondary2 {
  background: var(--radio-inactive);
  color: var(--text-dark);
  border: none;
  border-radius: 50px;
  padding: 0.65rem 1.8rem;
  font-size: 1.15rem;
  cursor: pointer;
  transition: background 0.2s;
  margin: 2rem auto 0;
  font-weight: bold;
}
.btn-secondary2:hover { background: #8fa870; }

.session-right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-shrink: 0;
}

.btn-delete-session {
  background: transparent;
  border: none;
  color: var(--text-mid);
  cursor: pointer;
  padding: 0.2rem 0.45rem;
  border-radius: 4px;
  font-size: 0.82rem;
  font-weight: 700;
  transition: opacity 0.15s, color 0.15s, background 0.15s;
  color: #c0392b;
}
.session-item:hover .btn-delete-session { opacity: 1; }
.btn-delete-session:hover { color: #c0392b; background: #fdecea; }
.btn-delete-session:disabled { opacity: 0.4; cursor: not-allowed; }

.btn-show-archive {
  background: var(--btn-secondary);
  color: var(--text-light);
  border: none;
  border-radius: 999px;
  padding: 0.7rem 1rem;
  font-weight: 700;
  font-size: 0.95rem;
  cursor: pointer;
  flex-shrink: 0;
  margin-top: 0.8%;
  transition: background 0.2s;
}
.btn-show-archive:hover { background: var(--btn-secondary-hover); }

.archive-notice {
  font-size: 0.8rem;
  background: #fef3cd;
  color: #7a5c1d;
  border-radius: 6px;
  padding: 0.5rem 0.75rem;
  margin-bottom: 0.5rem;
}

.archived-item { opacity: 0.85; }

.session-name-wrap {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.archive-date {
  font-size: 0.72rem;
  color: var(--text-mid);
  font-weight: 400;
}

.btn-unarchive-session {
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 0.2rem 0.45rem;
  border-radius: 4px;
  font-size: 0.9rem;
  color: var(--btn-primary);
  transition: background 0.15s;
}
.btn-unarchive-session:hover { background: #dde8d8; }

.btn-archive-session {
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 0.2rem 0.45rem;
  border-radius: 4px;
  font-size: 0.82rem;
  color: var(--text-mid);
  transition: background 0.15s;
}
.btn-archive-session:hover { background: #fef3cd; }
.btn-archive-session:disabled { opacity: 0.4; cursor: not-allowed; background: transparent; }

.archived-notice-bar {
  background: #fef3cd;
  color: #7a5c1d;
  border-radius: 8px;
  padding: 0.7rem 1.2rem;
  font-size: 0.92rem;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}
</style>