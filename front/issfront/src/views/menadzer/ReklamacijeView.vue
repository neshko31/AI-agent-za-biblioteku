<template>
  <div class="reklamacije-wrapper">
    <!-- Gornje zaglavlje -->
    <div class="page-header">
      <div class="header-tekst">
        <h1>Reklamacije</h1>
        <p class="subtitle">Pregled svih reklamacija i upravljanje ishodima</p>
      </div>
    </div>

    <!-- Stanja učitavanja, greške i prazne liste -->
    <div v-if="loading && reklamacije.length === 0" class="state-msg">
      <div class="spinner"></div>
      <p>Učitavanje reklamacija...</p>
    </div>
    <div v-else-if="error" class="state-msg state-msg--error">
      <p>{{ error }}</p>
    </div>
    <div v-else-if="reklamacije.length === 0" class="state-msg state-msg-prazno">
      <p>Nema evidentiranih reklamacija u sistemu.</p>
    </div>

    <!-- Tabela reklamacija preko celog ekrana -->
    <div v-else class="table-wrapper shadow-premium animated-fade-in">
      <table class="tabla">
        <thead>
          <tr>
            <th>ID</th>
            <th>Dobavljač</th>
            <th>Narudžbina</th>
            <th>Datum podnošenja</th>
            <th>Razlog</th>
            <th>Status</th>
            <th>Datum zatvaranja</th>
            <th class="text-right">Akcije</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in reklamacije" :key="r.id">
            <td>#{{ r.id }}</td>
            <td class="td-bold">{{ r.dobavljacNaziv }}</td>
            <td>
              <RouterLink :to="`/menadzer/narudzbine/${r.narudzbinId}`" class="link-narudzbina">
                #{{ r.narudzbinId }}
              </RouterLink>
            </td>
            <td>{{ r.datumPodnosenja }}</td>
            <td class="td-razlog">{{ r.razlog }}</td>
            <td>
              <span class="status-badge" :class="statusKlasa(r.status)">
                {{ statusNaziv(r.status) }}
              </span>
            </td>
            <td>{{ r.datumZatvaranja || '—' }}</td>
            <td class="text-right">
              <button
                v-if="r.status === 'OTVORENA'"
                class="btn-akcija-tabela btn-zatvori"
                @click="otvoriZatvaranje(r)">
                Zatvori
              </button>
              <span v-else class="zatvorena-oznaka">—</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- MODAL: Zatvaranje reklamacije -->
    <div v-if="reklamacijaZaZatvaranje" class="modal-overlay" @click.self="reklamacijaZaZatvaranje = null">
      <div class="modal animated-scale-up">
        <h2>Zatvaranje reklamacije</h2>
        <p class="modal-opis">
          Izaberite konačan ishod reklamacije #{{ reklamacijaZaZatvaranje.id }} za dobavljača
          <strong>{{ reklamacijaZaZatvaranje.dobavljacNaziv }}</strong>:
        </p>

        <div v-if="modalError" class="alert alert--error">{{ modalError }}</div>

        <div class="ishod-grid">
          <button
            class="btn-ishod btn-resena"
            :class="{ 'btn-ishod--aktivan': izabraniIshod === 'RESENA' }"
            @click="izabraniIshod = 'RESENA'">
            Rešena
          </button>
          <button
            class="btn-ishod btn-odbijena"
            :class="{ 'btn-ishod--aktivan-odbij': izabraniIshod === 'ODBIJENA' }"
            @click="izabraniIshod = 'ODBIJENA'">
            Odbijena od dobavljača
          </button>
        </div>

        <div class="modal-akcije">
          <button class="btn-primary" @click="zatvoriReklamaciju" :disabled="!izabraniIshod || loading">
            {{ loading ? 'Zatvaranje...' : 'Potvrdi ishod' }}
          </button>
          <button class="btn-sekundarni" @click="reklamacijaZaZatvaranje = null">Odustani</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { reklamacijaApi } from '../../services/api.js'

const reklamacije = ref([])
const loading = ref(false)
const error = ref('')
const modalError = ref('')
const reklamacijaZaZatvaranje = ref(null)
const izabraniIshod = ref('')

async function ucitaj() {
  loading.value = true
  error.value = ''
  try {
    const res = await reklamacijaApi.getSve()
    reklamacije.value = res.data
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri učitavanju reklamacija.'
  } finally {
    loading.value = false
  }
}

function otvoriZatvaranje(r) {
  reklamacijaZaZatvaranje.value = r
  izabraniIshod.value = ''
  modalError.value = ''
}

async function zatvoriReklamaciju() {
  modalError.value = ''
  if (!izabraniIshod.value) {
    modalError.value = 'Izaberite ishod reklamacije.'
    return
  }

  loading.value = true
  try {
    await reklamacijaApi.zatvori(reklamacijaZaZatvaranje.value.id, {
      status: izabraniIshod.value
    })
    reklamacijaZaZatvaranje.value = null
    await ucitaj()
  } catch (e) {
    modalError.value = e.response?.data?.message || 'Greška pri zatvaranju reklamacije.'
  } finally {
    loading.value = false
  }
}

function statusKlasa(status) {
  if (status === 'OTVORENA') return 'status--otvorena'
  if (status === 'RESENA') return 'status--resena'
  if (status === 'ODBIJENA') return 'status--odbijena'
  return ''
}

function statusNaziv(status) {
  if (status === 'OTVORENA') return '• Otvorena'
  if (status === 'RESENA') return '• Rešena'
  if (status === 'ODBIJENA') return '• Odbijena'
  return status
}

onMounted(ucitaj)
</script>

<style scoped>
/* Prilagođeno punoj širini ekrana */
.reklamacije-wrapper { 
  width: 100%; 
  max-width: 100%; 
  box-sizing: border-box;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* Page Header - Smanjene dimenzije naslova */
.page-header {
  display: flex; 
  justify-content: space-between;
  align-items: center; 
  margin-bottom: 1.5rem;
}
.page-header h1 { margin: 0 0 0.25rem; font-size: 1.85rem; font-weight: 700; color: #3f4e37; letter-spacing: -0.02em; }
.subtitle { color: #556644; font-size: 0.9rem; margin: 0; opacity: 0.85; }

/* Tabela stavki proširena na 100% sa 20px zaobljenjem */
.table-wrapper { 
  background: #ffffff; 
  border: none; 
  border-radius: 20px; 
  overflow: hidden; 
  width: 100%;
}
.tabla { width: 100%; border-collapse: collapse; font-size: 0.875rem; }
.tabla thead { background: #f4f6f0; }
.tabla th {
  padding: 0.95rem 1.25rem; 
  text-align: left; 
  font-weight: 600;
  color: #3f4e37; 
  font-size: 0.75rem; 
  text-transform: uppercase; 
  letter-spacing: 0.06em;
  border-bottom: 1px solid #eef0ea;
}
.tabla td { padding: 0.95rem 1.25rem; border-top: 1px solid #f4f6f0; color: #333333; vertical-align: middle; }
.tabla tbody tr { transition: background-color 0.15s; }
.tabla tbody tr:hover { background: #f9faf7; }

/* Tipografski detalji unutar tabele */
.td-bold { font-weight: 600; color: #3f4e37; }
.td-razlog { max-width: 260px; font-size: 0.825rem; color: #555555; line-height: 1.4; }
.link-narudzbina { color: #7a8f6e; text-decoration: none; font-weight: 600; }
.link-narudzbina:hover { text-decoration: underline; color: #3f4e37; }
.zatvorena-oznaka { color: #94a3b8; font-size: 0.85rem; }

/* Statusni Bedževi (Pill izgled) */
.status-badge { padding: 0.3rem 0.75rem; border-radius: 50px; font-size: 0.75rem; font-weight: 700; letter-spacing: 0.02em; display: inline-block; }
.status--otvorena { background: rgba(234, 179, 8, 0.08); color: #b45309; }
.status--resena { background: rgba(34, 197, 94, 0.08); color: #15803d; }
.status--odbijena { background: rgba(220, 38, 38, 0.08); color: #b91c1c; }

/* Akciona dugmad u tabeli (Pill izgled) */
.btn-akcija-tabela { 
  padding: 0.35rem 0.85rem; border-radius: 50px; font-size: 0.75rem; 
  font-weight: 600; border: none; cursor: pointer; transition: all 0.15s; 
}
.btn-zatvori { background: rgba(122, 143, 110, 0.12); color: #3f4e37; }
.btn-zatvori:hover { background: #7a8f6e; color: #ffffff; }

/* Primarna i sekundarna dugmad u modalima */
.btn-primary {
  background: #7a8f6e; color: #fff; border: none; border-radius: 10px;
  padding: 0.6rem 1.25rem; font-size: 0.85rem; font-weight: 600;
  cursor: pointer; font-family: inherit; transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(122, 143, 110, 0.2);
}
.btn-primary:hover:not(:disabled) { background: #6b7e60; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(122, 143, 110, 0.3); }
.btn-primary:disabled { opacity: 0.45; cursor: not-allowed; }

.btn-sekundarni {
  background: transparent; color: #556644; border: 1px solid #e2e8f0;
  border-radius: 10px; padding: 0.6rem 1.25rem; font-size: 0.85rem;
  font-weight: 500; cursor: pointer; font-family: inherit; transition: all 0.2s ease;
}
.btn-sekundarni:hover { background: #f4f6f0; border-color: #cbd5e1; }

/* Modali (20px zaobljenje) */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(25, 33, 21, 0.4);
  backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 100;
}
.modal {
  background: #ffffff; border: none; border-radius: 20px;
  padding: 2rem; max-width: 450px; width: 90%;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
}
.modal h2 { margin: 0 0 0.75rem; color: #3f4e37; font-size: 1.3rem; font-weight: 700; letter-spacing: -0.01em; }
.modal-opis { color: #555555; font-size: 0.875rem; margin-bottom: 1.25rem; line-height: 1.5; }

/* Mreža za izbor ishoda reklamacije unutar modala */
.ishod-grid { display: flex; gap: 0.75rem; margin-bottom: 1.5rem; }
.btn-ishod {
  flex: 1; padding: 0.7rem 0.95rem; border-radius: 10px; font-size: 0.85rem;
  font-weight: 600; cursor: pointer; font-family: inherit;
  transition: all 0.15s; border: 2px solid transparent; text-align: center;
}
.btn-resena { background: rgba(34, 197, 94, 0.06); color: #15803d; }
.btn-resena:hover { background: rgba(34, 197, 94, 0.12); }
.btn-odbijena { background: rgba(220, 38, 38, 0.06); color: #b91c1c; }
.btn-odbijena:hover { background: rgba(220, 38, 38, 0.12); }

/* Aktivna stanja selektora ishoda */
.btn-ishod--aktivan { border-color: #16a34a !important; background: rgba(34, 197, 94, 0.12) !important; }
.btn-ishod--aktivan-odbij { border-color: #b91c1c !important; background: rgba(220, 38, 38, 0.12) !important; }

.modal-akcije { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1.5rem; }

/* Obaveštenja / Alerte */
.alert { padding: 0.75rem 1rem; border-radius: 10px; font-size: 0.85rem; margin-bottom: 1rem; font-weight: 500; }
.alert--error { background: rgba(220, 38, 38, 0.08); color: #b91c1c; }

/* Globalna stanja i poruke */
.state-msg { 
  text-align: center; padding: 3rem 1.5rem; background: #ffffff; border-radius: 20px;
  display: flex; flex-direction: column; align-items: center; gap: 0.75rem; color: #556644; font-weight: 500; font-size: 0.9rem;
}
.state-msg-prazno { background: rgba(255, 255, 255, 0.5); border: 2px dashed #eef0ea; box-shadow: none; }
.state-msg--error p { color: #b91c1c; }
.text-right { text-align: right; }

/* Spinner i senke */
.spinner { width: 30px; height: 30px; border: 3px solid #eef0ea; border-top-color: #7a8f6e; border-radius: 50%; animation: spin 0.85s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.shadow-premium { box-shadow: 0 4px 18px rgba(0, 0, 0, 0.06); }

/* Animacije */
.animated-fade-in { animation: fadeIn 0.35s ease-out; }
.animated-scale-up { animation: scaleUp 0.25s cubic-bezier(0.34, 1.56, 0.64, 1); }
@keyframes fadeIn { from { opacity: 0; transform: translateY(6px); } to { opacity: 1; transform: translateY(0); } }
@keyframes scaleUp { from { transform: scale(0.97); opacity: 0; } to { transform: scale(1); opacity: 1; } }
</style>