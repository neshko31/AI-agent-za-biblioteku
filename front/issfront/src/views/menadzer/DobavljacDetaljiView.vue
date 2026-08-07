<template>
  <div class="detalji-wrapper">

    <!-- ZAGLAVLJE -->
    <div class="page-header">
      <div>
        <h1>Dobavljači</h1>
        <p class="subtitle">Detaljan pregled dobavljača</p>
      </div>
      <button class="btn-sekundarni" @click="router.push('/menadzer/dobavljaci')">
        ← Nazad
      </button>
    </div>

    <div v-if="loading" class="state-msg">Učitavanje...</div>
    <div v-else-if="error" class="state-msg state-msg--error">{{ error }}</div>

    <div v-else>
      <!-- INFO KARTICA -->
      <div class="kartica">
        <div class="kartica-header">
          <div>
            <h2>{{ dobavljac.naziv }}</h2>
            <span class="tip-badge">{{ tipNaziv }}</span>
          </div>
          <div class="header-akcije">
            <span class="status-badge" :class="statusKlasa">{{ dobavljac.status }}</span>
            <button 
                class="btn-akcija btn-izmena"
                @click="router.push(`/menadzer/dobavljaci/${id}/izmena`)"
                :disabled="dobavljac.status === 'NEAKTIVAN'">
                Izmeni
            </button>
            <button class="btn-akcija btn-brisi" @click="showModal = true"
              :disabled="dobavljac.status === 'NEAKTIVAN'">
              Obriši
            </button>
          </div>
        </div>

        <div class="info-grid">
          <div class="info-row">
            <span class="info-label">Email</span>
            <span class="info-value">{{ dobavljac.email }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">Telefon</span>
            <span class="info-value">{{ dobavljac.tel }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">PIB</span>
            <span class="info-value">{{ dobavljac.pib }}</span>
          </div>
          <div class="info-row" v-if="dobavljac.urlOnlineProdavnice">
            <span class="info-label">URL prodavnice</span>
            <a :href="dobavljac.urlOnlineProdavnice" target="_blank" class="info-link">
              {{ dobavljac.urlOnlineProdavnice }}
            </a>
          </div>
        </div>
      </div>

      <!-- UGOVORI -->
      <div class="sekcija-header">
        <h2>Ugovori</h2>
        <button
          class="btn-primary"
          @click="router.push(`/menadzer/dobavljaci/${id}/ugovor`)"
          :disabled="dobavljac.status === 'NEAKTIVAN' || imaAktivanUgovor">
          + Novi ugovor
        </button>
      </div>

      <div v-if="loadingUgovora" class="state-msg">Učitavanje ugovora...</div>

      <div v-else-if="ugovori.length === 0" class="state-msg">
        Ovaj dobavljač nema ugovora.
      </div>

      <div v-else class="table-wrapper">
        <table class="tabla">
          <thead>
            <tr>
              <th>Datum potpisa</th>
              <th>Početak</th>
              <th>Istek</th>
              <th>Popust</th>
              <th>Rok isporuke</th>
              <th>Status</th>
              <th>Akcije</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in ugovori" :key="u.id">
              <td>{{ u.datumPotpisa }}</td>
              <td>{{ u.datumPocetka }}</td>
              <td>{{ u.datumIsteka }}</td>
              <td>{{ u.popust }}%</td>
              <td>{{ u.rokIsporuke }} dan(a)</td>
              <td>
                <span class="status-badge" :class="ugovorStatusKlasa(u.status)">
                  {{ u.status }}
                </span>
              </td>
              <td>
                <button
                  class="btn-akcija btn-brisi"
                  @click="potvrdиRaskid(u)"
                  :disabled="u.status !== 'AKTIVAN'">
                  Raskini
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal brisanje dobavljača -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <h2>Brisanje dobavljača</h2>
        <p>Da li ste sigurni da želite da obrišete <strong>{{ dobavljac.naziv }}</strong>?</p>
        <div class="modal-akcije">
          <button class="btn-primary" @click="obrisiDobavljaca">Da</button>
          <button class="btn-sekundarni" @click="showModal = false">Ne</button>
        </div>
      </div>
    </div>

    <!-- Modal raskid ugovora -->
    <div v-if="ugovorZaRaskid" class="modal-overlay" @click.self="ugovorZaRaskid = null">
      <div class="modal">
        <h2>Raskidanje ugovora</h2>
        <p>Da li ste sigurni da želite da raskinete ugovor od <strong>{{ ugovorZaRaskid.datumPotpisa }}</strong>?</p>
        <div class="modal-akcije">
          <button class="btn-primary" @click="raskinиUgovor">Da</button>
          <button class="btn-sekundarni" @click="ugovorZaRaskid = null">Ne</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { dobavljacApi, ugovorApi } from '../../services/api.js'

const router = useRouter()
const route = useRoute()
const id = route.params.id

const dobavljac = ref({})
const ugovori = ref([])
const loading = ref(false)
const loadingUgovora = ref(false)
const error = ref('')
const showModal = ref(false)
const ugovorZaRaskid = ref(null)

const imaAktivanUgovor = computed(() =>
  ugovori.value.some(u => u.status?.toUpperCase() === 'AKTIVAN')
)
const tipNaziv = computed(() => {
  const t = dobavljac.value.tipDobavljaca
  if (t === '11') return 'Knjižara i izdavač'
  if (t === '01') return 'Knjižara'
  if (t === '10') return 'Izdavač'
  return 'Dobavljač'
})

const statusKlasa = computed(() =>
  dobavljac.value.status === 'AKTIVAN' ? 'status--aktivan' : 'status--neaktivan'
)

function ugovorStatusKlasa(status) {
  if (status === 'AKTIVAN') return 'status--aktivan'
  if (status === 'RASKINUT') return 'status--neaktivan'
  return 'status--istekao'
}

async function ucitaj() {
  loading.value = true
  try {
    const res = await dobavljacApi.jedan(id)
    dobavljac.value = res.data
  } catch (e) {
    console.log(e)
    console.log(e.response)

    error.value =
        e.response?.data?.message ||
        `Greška: ${e.response?.status}` ||
        'Greška pri učitavanju dobavljača.'
  } finally {
    loading.value = false
  }
  ucitajUgovore()
}

async function ucitajUgovore() {
  loadingUgovora.value = true
  try {
    const res = await ugovorApi.sviZaDobavljaca(id)
    ugovori.value = res.data
  } catch (e) {
    // dobavljač možda nema ugovore
  } finally {
    loadingUgovora.value = false
  }
}

async function obrisiDobavljaca() {
  try {
    await dobavljacApi.obrisi(id)
    showModal.value = false
    router.push('/menadzer/dobavljaci')
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri brisanju.'
    showModal.value = false
  }
}

function potvrdиRaskid(u) {
  ugovorZaRaskid.value = u
}

async function raskinиUgovor() {
  try {
    await ugovorApi.raskini(ugovorZaRaskid.value.id)
    ugovorZaRaskid.value = null
    await ucitajUgovore()
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri raskidanju ugovora.'
    ugovorZaRaskid.value = null
  }
}

onMounted(ucitaj)
</script>

<style scoped>
.detalji-wrapper {
  width: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
}

.page-header h1 {
  margin: 0 0 0.25rem;
  font-size: 2rem;
  color: var(--text-h);
}

.subtitle {
  color: var(--text);
  font-size: 0.95rem;
  margin: 0;
}

/* ── KARTICA ── */
.kartica {
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 1.5rem;
  margin-bottom: 2rem;
}

.kartica-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.kartica-header h2 {
  margin: 0 0 0.4rem;
  color: var(--text-h);
}

.tip-badge {
  font-size: 0.8rem;
  color: var(--accent);
  background: var(--accent-bg);
  padding: 0.2rem 0.7rem;
  border-radius: 20px;
}

.header-akcije {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.info-row {
  display: flex;
  gap: 1rem;
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--border);
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  width: 140px;
  flex-shrink: 0;
  font-size: 0.85rem;
  color: var(--text);
  font-weight: 500;
}

.info-value {
  color: var(--text-h);
}

.info-link {
  color: var(--accent);
  text-decoration: none;
}
.info-link:hover { text-decoration: underline; }

/* ── SEKCIJA UGOVORI ── */
.sekcija-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.sekcija-header h2 {
  margin: 0;
  color: var(--text-h);
}

/* ── TABELA ── */
.table-wrapper {
  border: 1px solid var(--border);
  border-radius: 12px;
  overflow: hidden;
}

.tabla {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}

.tabla thead { background: var(--accent-bg); }

.tabla th {
  padding: 0.75rem 1rem;
  text-align: left;
  font-weight: 600;
  color: var(--accent);
  font-size: 0.78rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.tabla td {
  padding: 0.75rem 1rem;
  border-top: 1px solid var(--border);
  color: var(--text-h);
}

.tabla tbody tr:hover { background: var(--accent-bg); }

/* ── STATUS ── */
.status-badge {
  padding: 0.2rem 0.7rem;
  border-radius: 20px;
  font-size: 0.78rem;
  font-weight: 600;
}
.status--aktivan { background: rgba(34,197,94,0.1); color: #16a34a; }
.status--neaktivan { background: rgba(220,38,38,0.1); color: #dc2626; }
.status--istekao { background: rgba(234,179,8,0.1); color: #b45309; }

/* ── DUGMAD ── */
.btn-primary {
  background: var(--accent);
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 0.6rem 1.2rem;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: opacity 0.15s;
}
.btn-primary:hover:not(:disabled) { opacity: 0.85; }
.btn-primary:disabled { opacity: 0.4; cursor: not-allowed; }

.btn-sekundarni {
  background: transparent;
  color: var(--text);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 0.6rem 1.2rem;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.15s;
}
.btn-sekundarni:hover { background: var(--accent-bg); }

.btn-akcija {
  padding: 0.3rem 0.8rem;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 500;
  border: none;
  cursor: pointer;
  text-decoration: none;
  font-family: inherit;
  transition: opacity 0.15s;
  display: inline-block;
}
.btn-akcija:hover:not(:disabled) { opacity: 0.8; }
.btn-akcija:disabled { opacity: 0.4; cursor: not-allowed; }

.btn-izmena { background: rgba(234,179,8,0.1); color: #b45309; }
.btn-brisi { background: rgba(220,38,38,0.1); color: #dc2626; }

/* ── STATE ── */
.state-msg {
  text-align: center;
  padding: 3rem;
  color: var(--text);
}
.state-msg--error { color: #dc2626; }

/* ── MODAL ── */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal {
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 2rem;
  max-width: 420px;
  width: 90%;
}

.modal h2 { margin: 0 0 0.75rem; color: var(--text-h); }
.modal p { color: var(--text); margin-bottom: 1.5rem; }
.modal-akcije { display: flex; gap: 0.75rem; }
</style>
