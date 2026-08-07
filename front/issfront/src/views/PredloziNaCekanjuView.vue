<template>
  <div class="app-layout">
    <SidebarNav />

    <main class="main-content">
      <div class="predlozi-wrapper">
        
        <!-- Zaglavlje stranice skroz levo -->
        <div class="page-header">
          <div class="header-tekst">
            <h1>Predlozi naslova</h1>
            <p class="subtitle">Predlozi na čekanju</p>
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
          <span class="state-icon">📥</span>
          <p>Nema predloga na čekanju.</p>
        </div>

        <!-- Glavna bela tabela sa zaobljenim ivicama -->
        <div v-else class="table-wrapper animated-fade-in">
          <table class="tabla">
            <thead>
              <tr>
                <th>Korisnik</th>
                <th>Naslov</th>
                <th>Autor</th>
                <th>Datum</th>
                <th class="text-center">Akcije</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in predlozi" :key="p.id">
                <td class="td-korisnik">👤 {{ p.korisnikIme }} {{ p.korisnikPrezime }}</td>
                <td class="td-naslov">{{ p.naslov }}</td>
                <td class="td-autor">{{ p.autor }}</td>
                <td class="td-datum">{{ p.datumPodnosenja }}</td>
                <td class="akcije">
                  <button class="btn-akcija btn-odobri" @click="otvoriOdobravanje(p)">
                    ✓ Odobri
                  </button>
                  <button class="btn-akcija btn-odbij" @click="otvoriOdbijanje(p)">
                    ✕ Odbij
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Modal odobravanje -->
        <div v-if="predlogZaOdobravanje" class="modal-overlay" @click.self="predlogZaOdobravanje = null">
          <div class="modal animated-scale-up">
            <h2>Odobravanje predloga</h2>
            <p>Odobravate predlog za knjigu <strong class="istaknuto">{{ predlogZaOdobravanje.naslov }}</strong>.</p>

            <div class="form-group">
              <label>Žanr knjige</label>
              <select v-model="izabraniZanrId">
                <option :value="null" disabled>Izaberite žanr</option>
                <option v-for="z in zanrovi" :key="z.id" :value="z.id">
                  {{ z.name }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>Okvirna cena (RSD)</label>
              <input v-model="okvirnaCena" type="number" min="0" step="0.01" placeholder="npr. 1500" />
            </div>

            <div v-if="modalError" class="alert alert--error">
              <span class="alert-icon">⚠️</span> {{ modalError }}
            </div>

            <div class="modal-akcije">
              <button class="btn-primary" @click="obradiPredlog('ODOBRENO_BIBLIOTEKAR')">Da, odobri</button>
              <button class="btn-sekundarni" @click="predlogZaOdobravanje = null">Odustani</button>
            </div>
          </div>
        </div>

        <!-- Modal odbijanje -->
        <div v-if="predlogZaOdbijanje" class="modal-overlay" @click.self="zatvoriOdbijanje">
          <div class="modal animated-scale-up">
            <h2>Odbijanje predloga</h2>
            <p>Unesite razlog odbijanja za <strong class="istaknuto">{{ predlogZaOdbijanje.naslov }}</strong>:</p>
            
            <div class="form-group">
              <textarea
                v-model="obrazlozenje"
                placeholder="Napišite obrazloženje za korisnika (obavezno)..."
                rows="4"
              ></textarea>
            </div>
            
            <div v-if="modalError" class="alert alert--error">
              <span class="alert-icon">⚠️</span> {{ modalError }}
            </div>
            
            <div class="modal-akcije">
              <button class="btn-primary btn-primary--danger" @click="obradiPredlog('ODBIJENO_BIBLIOTEKAR')">Odbij predlog</button>
              <button class="btn-sekundarni" @click="zatvoriOdbijanje">Odustani</button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { predlogApi } from '../services/api.js'
import { publicApi } from '../services/api.js'
import SidebarNav from '../components/Sidebar.vue'

const predlozi = ref([])
const loading = ref(false)
const error = ref('')
const modalError = ref('')
const predlogZaOdobravanje = ref(null)
const predlogZaOdbijanje = ref(null)
const obrazlozenje = ref('')

const zanrovi = ref([])
const izabraniZanrId = ref(null)
const okvirnaCena = ref('')

async function ucitaj() {
  loading.value = true
  error.value = ''
  try {
    const res = await predlogApi.predloziNaCekanju()
    predlozi.value = res.data
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri učitavanju predloga.'
  } finally {
    loading.value = false
  }
}

async function ucitajZanrove() {
  try {
    const res = await publicApi.getGenres()
    zanrovi.value = res.data
  } catch (e) {
  }
}

function otvoriOdobravanje(p) {
  predlogZaOdobravanje.value = p
  modalError.value = ''
  izabraniZanrId.value = null
  okvirnaCena.value = ''
}

function otvoriOdbijanje(p) {
  predlogZaOdbijanje.value = p
  obrazlozenje.value = ''
  modalError.value = ''
}

function zatvoriOdbijanje() {
  predlogZaOdbijanje.value = null
  obrazlozenje.value = ''
  modalError.value = ''
}

async function obradiPredlog(status) {
  modalError.value = ''

  const predlog = status === 'ODOBRENO_BIBLIOTEKAR' ? predlogZaOdobravanje.value : predlogZaOdbijanje.value

  if (status === 'ODBIJENO_BIBLIOTEKAR' && !obrazlozenje.value.trim()) {
    modalError.value = 'Razlog odbijanja je obavezan.'
    return
  }

  if (status === 'ODOBRENO_BIBLIOTEKAR') {
    if (!izabraniZanrId.value) {
      modalError.value = 'Žanr je obavezan.'
      return
    }
    if (!okvirnaCena.value || okvirnaCena.value <= 0) {
      modalError.value = 'Okvirna cena je obavezna.'
      return
    }
  }

  try {
    await predlogApi.obradiPredlog(predlog.id, {
      status,
      obrazlozenje: status === 'ODBIJENO_BIBLIOTEKAR' ? obrazlozenje.value : null,
      zanrId: status === 'ODOBRENO_BIBLIOTEKAR' ? izabraniZanrId.value : null,
      okvirnaCena: status === 'ODOBRENO_BIBLIOTEKAR' ? Number(okvirnaCena.value) : null
    })
    predlogZaOdobravanje.value = null
    predlogZaOdbijanje.value = null
    await ucitaj()
  } catch (e) {
    modalError.value = e.response?.data?.message || 'Greška pri obradi predloga.'
  }
}

onMounted(() => {
  ucitaj()
  ucitajZanrove()
})

</script>

<style scoped>
.predlozi-wrapper { 
  width: 100%; 
  max-width: 100%;
  box-sizing: border-box;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.page-header { 
  margin-bottom: 2rem; 
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-align: left;
}
.page-header h1 { 
  margin: 0 0 0.35rem; 
  font-size: 2.25rem; 
  font-weight: 700;
  letter-spacing: -0.02em;
  color: #3f4e37;
}
.subtitle { 
  color: #556644; 
  font-size: 1rem; 
  margin: 0;
  opacity: 0.85;
}


.table-wrapper { 
  background: #ffffff !important;
  border: none; 
  border-radius: 20px; 
  overflow: hidden; 
  width: 100%;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.06);
}

.tabla { width: 100%; border-collapse: collapse; font-size: 0.95rem; text-align: left; }
.tabla thead { background: #f4f6f0; }
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
  padding: 1.1rem 1.5rem; 
  border-top: 1px solid #f4f6f0; 
  color: #333333; 
  vertical-align: middle;
}
.tabla tbody tr { transition: background-color 0.15s ease; }
.tabla tbody tr:hover { background: #f9faf7; }

.td-korisnik { font-weight: 500; color: #556644; }
.td-naslov { font-weight: 600; color: #3f4e37; }
.td-autor { color: #666666; }
.td-datum { color: #666666; font-size: 0.9rem; }
.text-center { text-align: center; }

.akcije { 
  display: flex; 
  gap: 0.65rem; 
  align-items: center; 
}

.btn-akcija {
  padding: 0.45rem 1rem; 
  border-radius: 50px; 
  font-size: 0.825rem;
  font-weight: 600; 
  border: none; 
  cursor: pointer; 
  font-family: inherit; 
  transition: all 0.2s ease;
}
.btn-odobri { 
  background: rgba(34, 197, 94, 0.1); 
  color: #15803d; 
}
.btn-odobri:hover { 
  background: #15803d; 
  color: #ffffff; 
  transform: translateY(-1px);
}
.btn-odbij { 
  background: rgba(220, 38, 38, 0.08); 
  color: #b91c1c; 
}
.btn-odbij:hover { 
  background: #b91c1c; 
  color: #ffffff; 
  transform: translateY(-1px);
}

.modal-overlay {
  position: fixed; 
  inset: 0; 
  background: rgba(63, 78, 55, 0.4); 
  backdrop-filter: blur(4px); 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  z-index: 100;
}
.modal {
  background: #ffffff !important; 
  border: none;
  border-radius: 20px; 
  padding: 2.25rem; 
  max-width: 480px; 
  width: 90%;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
}
.modal h2 { 
  margin: 0 0 0.5rem; 
  color: #3f4e37; 
  font-size: 1.5rem;
  font-weight: 700;
}
.modal p { 
  color: #556644; 
  margin-bottom: 1.5rem; 
  font-size: 1rem;
  line-height: 1.5;
}
.istaknuto {
  color: #3f4e37;
  font-weight: 600;
}

.form-group textarea {
  width: 100%; 
  padding: 0.85rem 1rem; 
  border: 1px solid #e2e8f0;
  border-radius: 12px; 
  font-size: 0.95rem; 
  color: #333333;
  background: #f8fafc; 
  font-family: inherit; 
  resize: vertical;
  outline: none; 
  box-sizing: border-box;
  transition: all 0.2s ease;
}
.form-group textarea:focus { 
  border-color: #7a8f6e; 
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(122, 143, 110, 0.15);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  margin-bottom: 1rem;
}
.form-group label {
  font-size: 0.9rem;
  color: #556644;
  font-weight: 500;
}
.form-group select,
.form-group input {
  padding: 0.65rem 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  font-size: 0.95rem;
  color: #333;
  background: #f8fafc;
  font-family: inherit;
  outline: none;
  transition: all 0.2s ease;
}
.form-group select:focus,
.form-group input:focus {
  border-color: #7a8f6e;
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(122, 143, 110, 0.15);
}

/* Dugmad u modalu */
.modal-akcije { 
  display: flex; 
  justify-content: flex-end;
  gap: 0.75rem; 
  margin-top: 1.5rem; 
}

.btn-primary {
  background: #7a8f6e;
  color: #fff; 
  border: none; 
  border-radius: 12px;
  padding: 0.7rem 1.4rem; 
  font-size: 0.9rem; 
  font-weight: 600;
  cursor: pointer; 
  font-family: inherit; 
  transition: all 0.2s ease;
}
.btn-primary:hover { 
  background: #6b7e60; 
  transform: translateY(-1px);
}
.btn-primary--danger {
  background: #b91c1c;
}
.btn-primary--danger:hover {
  background: #991b1b;
}

.btn-sekundarni {
  background: transparent; 
  color: #556644; 
  border: 1px solid #e2e8f0;
  border-radius: 12px; 
  padding: 0.7rem 1.4rem; 
  font-size: 0.9rem;
  font-weight: 500; 
  cursor: pointer; 
  font-family: inherit; 
  transition: all 0.2s ease;
}
.btn-sekundarni:hover { 
  background: #f4f6f0; 
  border-color: #cbd5e1;
}

.alert { 
  padding: 0.85rem 1.25rem; 
  border-radius: 10px; 
  font-size: 0.9rem; 
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1rem;
}
.alert--error { background: rgba(220, 38, 38, 0.08); color: #dc2626; }

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
  width: 100%;
  box-sizing: border-box;
}
.state-msg p { margin: 0; font-size: 1.05rem; font-weight: 500; color: #556644; }
.state-icon { font-size: 2.5rem; }
.state-msg--error { color: #dc2626; }

.spinner {
  width: 30px; 
  height: 30px;
  border: 3px solid #e2e8f0;
  border-top-color: #7a8f6e;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.animated-fade-in { animation: fadeIn 0.35s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }

.animated-scale-up { animation: scaleUp 0.2s cubic-bezier(0.34, 1.56, 0.64, 1); }
@keyframes scaleUp { from { transform: scale(0.95); opacity: 0; } to { transform: scale(1); opacity: 1; } }
</style>