<template>
  <div class="app-layout">
    <SidebarNav />

    <main class="main-content">
      <div class="predlozi-wrapper">
        
        <!-- Gornji deo stranice sa naslovom i akcijom -->
        <div class="page-header">
          <div class="header-tekst">
            <h1>Moji predlozi</h1>
            <p class="subtitle">Pregled vaših predloga za nabavku literature</p>
          </div>
          <button class="btn-primary btn-upecatljivo" @click="showForma = !showForma">
            {{ showForma ? '✕ Zatvori formu' : '+ Novi predlog' }}
          </button>
        </div>

        <!-- Forma za novi predlog -->
        <div v-if="showForma" class="forma-kartica animated-fade-in">
          <div class="forma-header">
            <h2>Novi predlog za nabavku</h2>
            <p>Popunite podatke o knjizi koju želite da predložite.</p>
          </div>
          
          <div v-if="formaError" class="alert alert--error">
            <span class="alert-icon">⚠️</span> {{ formaError }}
          </div>
          <div v-if="formaUspeh" class="alert alert--uspeh">
            <span class="alert-icon">✨</span> Predlog je uspešno podnet!
          </div>

          <div class="forma-telo">
            <div class="form-group">
              <label>Naslov knjige</label>
              <input v-model="forma.naslov" type="text" placeholder="Unesite tačan naziv knjige" />
            </div>
            <div class="form-group">
              <label>Autor</label>
              <input v-model="forma.autor" type="text" placeholder="Ime i prezime autora" />
            </div>
          </div>
          
          <div class="forma-akcije">
            <button class="btn-submit" @click="posaljiPredlog" :disabled="loadingForma">
              {{ loadingForma ? 'Slanje...' : 'Pošalji predlog' }}
            </button>
          </div>
        </div>

        <!-- Stanja učitavanja, greške ili prazne liste -->
        <div v-if="loading" class="state-msg">
          <div class="spinner"></div>
          <p>Učitavanje predloga...</p>
        </div>
        <div v-else-if="error" class="state-msg state-msg--error">
          <span class="state-icon">❌</span>
          <p>{{ error }}</p>
        </div>
        <div v-else-if="predlozi.length === 0" class="state-msg state-msg--empty">
          <span class="state-icon">📚</span>
          <p>Trenutno nemate podnetih predloga.</p>
        </div>

        <!-- Glavna tabela sa podacima -->
        <div v-else class="table-wrapper">
          <table class="tabla">
            <thead>
              <tr>
                <th>Naslov knjige</th>
                <th>Autor</th>
                <th>Datum podnošenja</th>
                <th>Status</th>
                <th>Obrazloženje</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in predlozi" :key="p.id">
                <td class="td-naslov">{{ p.naslov }}</td>
                <td class="td-autor">{{ p.autor }}</td>
                <td class="td-datum">{{ p.datumPodnosenja }}</td>
                <td>
                  <span class="status-badge" :class="statusKlasa(p.status)">
                    {{ statusNaziv(p.status) }}
                  </span>
                </td>
                <td class="obrazlozenje-col">
                  {{ p.obrazlozenje || '—' }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>

      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { predlogApi } from '../services/api.js'
import SidebarNav from '../components/Sidebar.vue'

const predlozi = ref([])
const loading = ref(false)
const error = ref('')
const showForma = ref(false)
const loadingForma = ref(false)
const formaError = ref('')
const formaUspeh = ref(false)

const forma = ref({ naslov: '', autor: '' })

async function ucitaj() {
  loading.value = true
  error.value = ''
  try {
    const res = await predlogApi.mojiPredlozi()
    predlozi.value = res.data
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri učitavanju predloga.'
  } finally {
    loading.value = false
  }
}

async function posaljiPredlog() {
  formaError.value = ''
  formaUspeh.value = false

  if (!forma.value.naslov.trim() || !forma.value.autor.trim()) {
    formaError.value = 'Naslov i autor su obavezni.'
    return
  }

  loadingForma.value = true
  try {
    await predlogApi.kreiraj(forma.value)
    formaUspeh.value = true
    forma.value = { naslov: '', autor: '' }
    await ucitaj()
    setTimeout(() => {
      showForma.value = false
      formaUspeh.value = false
    }, 1500)
  } catch (e) {
    formaError.value = e.response?.data?.message || 'Greška pri slanju predloga.'
  } finally {
    loadingForma.value = false
  }
}

function statusKlasa(status) {
  if (status === 'ODOBRENO_BIBLIOTEKAR') return 'status--odobreno'
  if (status === 'ODBIJENO_BIBLIOTEKAR') return 'status--odbijeno'
  return 'status--cekanje'
}

function statusNaziv(status) {
  if (status === 'ODOBRENO_BIBLIOTEKAR') return 'Odobreno'
  if (status === 'ODBIJENO_BIBLIOTEKAR') return 'Odbijeno'
  return 'Na čekanju'
}

onMounted(ucitaj)
</script>

<style scoped>
.predlozi-wrapper { 
  width: 100%; 
  max-width: 100%;
  box-sizing: border-box;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* Poravnato skroz levo po tvojoj želji */
.page-header { 
  margin-bottom: 2rem; 
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  text-align: left;
}
.page-header h1 { 
  margin: 0 0 0.35rem; 
  font-size: 2.25rem; 
  font-weight: 700;
  letter-spacing: -0.02em;
  color: #3f4e37; /* Prilagođena tamno zelena sa slike */
}
.subtitle { 
  color: #556644; 
  font-size: 1rem; 
  margin: 0;
  opacity: 0.85;
}

/* USKLAĐENO: Čisto bela pozadina i jače zaobljene ivice kao baze podataka na slici */
.forma-kartica {
  background: #ffffff !important;
  border: none; 
  border-radius: 20px; /* Mekše zaobljenje sa slike */
  padding: 1.75rem; 
  margin-bottom: 2rem;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.06);
  display: flex; 
  flex-direction: column; 
  gap: 1.5rem;
}
.forma-header h2 { 
  margin: 0 0 0.25rem 0; 
  color: #3f4e37; 
  font-size: 1.35rem; 
  font-weight: 600;
}
.forma-header p {
  margin: 0;
  font-size: 0.9rem;
  color: #556644;
  opacity: 0.8;
}
.forma-telo {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.25rem;
}

.form-group { display: flex; flex-direction: column; gap: 0.5rem; }
.form-group label { 
  font-size: 0.85rem; 
  color: #3f4e37; 
  font-weight: 600; 
  text-transform: uppercase;
  letter-spacing: 0.03em;
}
.form-group input {
  padding: 0.75rem 1rem; 
  border: 1px solid #e2e8f0; 
  border-radius: 10px;
  font-size: 0.95rem; 
  color: #3f4e37; 
  background: #f8fafc;
  font-family: inherit; 
  outline: none; 
  transition: all 0.2s ease;
}
.form-group input:focus { 
  border-color: #7a8f6e; 
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(122, 143, 110, 0.15);
}
.forma-akcije { display: flex; justify-content: flex-end; }

/* USKLAĐENO: Tabela u sopstvenoj beloj "koverti" sa velikim radijusom */
.table-wrapper { 
  background: #ffffff !important;
  border: none; 
  border-radius: 20px; 
  overflow: hidden; 
  width: 100%;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.06);
}
.tabla { width: 100%; border-collapse: collapse; font-size: 0.95rem; text-align: left; }
.tabla thead { background: #f4f6f0; } /* Suptilna zelenkasto-siva za zaglavlje */
.tabla th {
  padding: 1.1rem 1.5rem; 
  font-weight: 600;
  color: #3f4e37; 
  font-size: 0.8rem; 
  text-transform: uppercase; 
  letter-spacing: 0.06em;
  border-bottom: 1px solid #eef0ea;
}
.tabla td { 
  padding: 1.2rem 1.5rem; 
  border-top: 1px solid #f4f6f0; 
  color: #333333; 
  vertical-align: middle;
}
.tabla tbody tr { transition: background-color 0.15s ease; }
.tabla tbody tr:hover { background: #f9faf7; }

.td-naslov { font-weight: 600; color: #3f4e37; }
.td-autor { color: #556644; font-weight: 500; }
.td-datum { color: #666666; font-size: 0.9rem; }
.obrazlozenje-col { 
  font-size: 0.9rem; 
  color: #666666; 
  max-width: 350px; 
  line-height: 1.4;
}

/* Statusni bedževi */
.status-badge { 
  display: inline-flex;
  align-items: center;
  padding: 0.3rem 0.85rem; 
  border-radius: 50px; 
  font-size: 0.8rem; 
  font-weight: 600; 
}
.status--odobreno { background: rgba(34, 197, 94, 0.12); color: #15803d; }
.status--odbijeno { background: rgba(220, 38, 38, 0.12); color: #b91c1c; }
.status--cekanje { background: rgba(234, 179, 8, 0.15); color: #a16207; }

/* Dugmad usklađena sa stilom dugmeta "Pretraži" sa slika */
.btn-primary {
  background: #7a8f6e; /* Maslinasta nijansa sa slika */
  color: #fff; 
  border: none; 
  border-radius: 12px;
  padding: 0.7rem 1.4rem; 
  font-size: 0.9rem; 
  font-weight: 600;
  cursor: pointer; 
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}
.btn-upecatljivo {
  padding: 0.8rem 1.6rem; 
  font-size: 0.95rem;
  box-shadow: 0 4px 12px rgba(122, 143, 110, 0.3); 
}
.btn-upecatljivo:hover:not(:disabled) {
  transform: translateY(-2px); 
  box-shadow: 0 6px 20px rgba(122, 143, 110, 0.4);
  background: #6b7e60;
}
.btn-submit {
  background: #3f4e37;
  color: #ffffff;
  border: none;
  border-radius: 12px;
  padding: 0.75rem 1.5rem;
  font-weight: 600;
  cursor: pointer;
}

.alert { 
  padding: 0.85rem 1.25rem; 
  border-radius: 10px; 
  font-size: 0.9rem; 
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.alert--error { background: rgba(220, 38, 38, 0.08); color: #dc2626; }
.alert--uspeh { background: rgba(34, 197, 94, 0.08); color: #16a34a; }

.state-msg { 
  text-align: center; 
  padding: 4rem 2rem; 
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}
.spinner {
  width: 30px; height: 30px;
  border: 3px solid #e2e8f0;
  border-top-color: #7a8f6e;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.animated-fade-in { animation: fadeIn 0.3s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
</style>