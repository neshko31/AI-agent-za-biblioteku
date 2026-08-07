<template>
  <div class="budzet-wrapper">
    <!-- Zaglavlje stranice skroz levo -->
    <div class="page-header">
      <div class="header-tekst">
        <h1>Budžet</h1>
        <p class="subtitle">Pregled i upravljanje budžetom po žanrovima literature</p>
      </div>
    </div>

    <!-- Stanje učitavanja i greške -->
    <div v-if="loading" class="state-msg">
      <div class="spinner"></div>
      <p>Učitavanje budžetskih podataka...</p>
    </div>
    <div v-else-if="error" class="state-msg state-msg--error">
      <span class="state-icon">❌</span>
      <p>{{ error }}</p>
    </div>

    <div v-else class="animated-fade-in">
      <!-- Ukupni pregled (Summary kartice) -->
      <div class="summary-grid">
        <div class="summary-card">
          <span class="summary-label">Ukupan budžet</span>
          <span class="summary-value">{{ formatirajIznos(ukupanBudzet) }}</span>
        </div>
        <div class="summary-card">
          <span class="summary-label">Potrošeno</span>
          <span class="summary-value summary-value--potroseno">{{ formatirajIznos(ukupnoPotroseno) }}</span>
        </div>
        <div class="summary-card">
          <span class="summary-label">Dostupno</span>
          <span class="summary-value summary-value--dostupno">{{ formatirajIznos(ukupnoDostupno) }}</span>
        </div>
      </div>

      <!-- Tabela budžeta po žanrovima -->
      <div class="sekcija">
        <h2 class="sekcija-naslov">Budžet po žanrovima</h2>
        <div class="table-wrapper">
          <table class="tabla">
            <thead>
              <tr>
                <th>Žanr</th>
                <th>Ukupan budžet</th>
                <th>Potrošeno</th>
                <th>Dostupno</th>
                <th>Iskorišćenost</th>
                <th>Rezervisano</th>
                <th class="text-right">Akcije</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="b in budzeti" :key="b.id">
                <td class="td-zanr">{{ b.zanrNaziv }}</td>
                <td class="td-iznos">{{ formatirajIznos(b.ukupanBudzet) }}</td>
                <td class="td-iznos-potroseno">{{ formatirajIznos(b.potroseno) }}</td>
                <td>
                  <span :class="b.dostupno < 5000 ? 'iznos--nizak' : 'iznos--ok'">
                    {{ formatirajIznos(b.dostupno) }}
                  </span>
                </td>
                <td>
                  <div class="progress-bar-wrapper">
                    <div class="progress-bar">
                      <div
                        class="progress-bar-fill"
                        :style="{ width: procenat(b) + '%' }"
                        :class="procenat(b) > 75 ? 'fill--visoko' : 'fill--ok'"
                      ></div>
                    </div>
                    <span class="progress-label" :class="procenat(b) > 80 ? 'text--visoko' : 'text--ok'">{{ procenat(b) }}%</span>
                  </div>
                </td>
                <td>{{ formatirajIznos(b.rezervisano) }}</td>
                <td class="text-right">
                  <button class="btn-akcija btn-izmeni" @click="otvoriPostavljanje(b)">
                    Izmeni
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Kontrolna tabla za upravljanje i preraspodelu -->
      <div class="sekcija forme-grid">

        <!-- Panel 1: Postavi/izmeni budžet -->
        <div class="forma-kartica">
          <h2>{{ editBudzet ? 'Izmeni budžet — ' + editBudzet.zanrNaziv : 'Postavi budžet' }}</h2>
          <p class="panel-opis">Definišite ili ažurirajte maksimalni limit sredstava za izabrani žanr.</p>
          
          <div v-if="postaviError" class="alert alert--error">{{ postaviError }}</div>
          <div v-if="postaviUspeh" class="alert alert--uspeh">Budžet je uspešno ažuriran!</div>

          <div class="form-group">
            <label>Žanr</label>
            <select v-model="postaviForma.zanrId" :disabled="!!editBudzet">
              <option :value="null" disabled>Izaberite žanr</option>
              <option v-for="z in zanrovi" :key="z.id" :value="z.id">{{ z.name }}</option>
            </select>
          </div>

          <div class="form-group">
            <label>Ukupan budžet (RSD)</label>
            <input v-model="postaviForma.ukupanBudzet" type="number" min="0" step="100" placeholder="npr. 50000" />
          </div>

          <div class="forma-akcije">
            <button v-if="editBudzet" class="btn-sekundarni" @click="odustaniOdIzmene">
              Odustani
            </button>
            <button class="btn-primary" @click="sacuvajBudzet" :disabled="loadingPostavi">
              {{ loadingPostavi ? 'Čuvanje...' : 'Sačuvaj izmene' }}
            </button>
          </div>
        </div>

        <!-- Panel 2: Preraspodela -->
        <div class="forma-kartica">
          <h2>Preraspodela sredstava</h2>
          <p class="panel-opis">Brzo prebacite slobodna (dostupna) sredstva sa jednog žanra na drugi.</p>
          
          <div v-if="preraspoError" class="alert alert--error">{{ preraspoError }}</div>
          <div v-if="preraspoUspeh" class="alert alert--uspeh">Preraspodela je uspešno izvršena!</div>

          <div class="form-group">
            <label>Izvorni žanr (odakle uzimate)</label>
            <select v-model="preraspoForma.izvorZanrId">
              <option :value="null" disabled>Izaberite žanr</option>
              <option v-for="b in budzeti" :key="b.zanrId" :value="b.zanrId">
                {{ b.zanrNaziv }} (slobodno: {{ formatirajIznos(b.dostupno) }})
              </option>
            </select>
          </div>

          <div class="form-group">
            <label>Odredišni žanr (kome prebacujete)</label>
            <select v-model="preraspoForma.odredisteZanrId">
              <option :value="null" disabled>Izaberite žanr</option>
              <option
                v-for="b in budzeti"
                :key="b.zanrId"
                :value="b.zanrId"
                :disabled="b.zanrId === preraspoForma.izvorZanrId"
              >
                {{ b.zanrNaziv }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label>Iznos za prenos (RSD)</label>
            <input v-model="preraspoForma.iznos" type="number" min="0.01" step="100" placeholder="npr. 5000" />
          </div>

          <div class="forma-akcije">
            <button class="btn-primary" @click="izvrsiPreraspodelu" :disabled="loadingPreraspo">
              {{ loadingPreraspo ? 'Prenos...' : 'Izvrši prenos novca' }}
            </button>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { budzetApi } from '../../services/api.js'
import { publicApi } from '../../services/api.js'

const budzeti = ref([])
const zanrovi = ref([])
const loading = ref(false)
const error = ref('')

const editBudzet = ref(null)
const postaviError = ref('')
const postaviUspeh = ref(false)
const loadingPostavi = ref(false)

const preraspoError = ref('')
const preraspoUspeh = ref(false)
const loadingPreraspo = ref(false)

const postaviForma = ref({
  zanrId: null,
  ukupanBudzet: '',
  budzetId: null
})
const preraspoForma = ref({ izvorZanrId: null, odredisteZanrId: null, iznos: '' })

const ukupanBudzet = computed(() =>
  budzeti.value.reduce((sum, b) => sum + b.ukupanBudzet, 0)
)
const ukupnoPotroseno = computed(() =>
  budzeti.value.reduce((sum, b) => sum + b.potroseno, 0)
)
const ukupnoDostupno = computed(() =>
  budzeti.value.reduce((sum, b) => sum + b.dostupno, 0)
)

function procenat(b) {
  if (b.ukupanBudzet === 0) return 0
  return Math.round((b.potroseno / b.ukupanBudzet) * 100)
}

function formatirajIznos(iznos) {
  if (iznos === null || iznos === undefined) return '0,00 RSD'
  return Number(iznos).toLocaleString('sr-RS', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }) + ' RSD'
}

async function ucitaj() {
  loading.value = true
  error.value = ''
  try {
    const [budzetRes, zanrRes] = await Promise.all([
      budzetApi.getSviBudzeti(),
      publicApi.getGenres()
    ])
    budzeti.value = budzetRes.data
    zanrovi.value = zanrRes.data
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri učitavanju budžeta.'
  } finally {
    loading.value = false
  }
}

function otvoriPostavljanje(b) {
  console.log(b)
  editBudzet.value = b
  postaviForma.value.zanrId = b.zanrId
  postaviForma.value.ukupanBudzet = b.ukupanBudzet
  postaviForma.value.budzetId = b.budzetId
  postaviError.value = ''
  postaviUspeh.value = false
}

function odustaniOdIzmene() {
  editBudzet.value = null
  postaviForma.value = { zanrId: null, ukupanBudzet: '', budzetId: null }
  postaviError.value = ''
}

async function sacuvajBudzet() {
  postaviError.value = ''
  postaviUspeh.value = false

  if (!postaviForma.value.zanrId) {
    postaviError.value = 'Žanr je obavezan.'
    return
  }
  if (!postaviForma.value.ukupanBudzet || postaviForma.value.ukupanBudzet < 0) {
    postaviError.value = 'Unesite validan iznos.'
    return
  }

  loadingPostavi.value = true
  try {
    await budzetApi.postaviBudzet({
        zanrId: postaviForma.value.zanrId,
        ukupanBudzet: Number(postaviForma.value.ukupanBudzet),
        budzetId: postaviForma.value.budzetId
        })
    postaviUspeh.value = true
    editBudzet.value = null
    postaviForma.value = {  zanrId: null, ukupanBudzet: '' }
    await ucitaj()
    setTimeout(() => { postaviUspeh.value = false }, 2000)
  } catch (e) {
    postaviError.value = e.response?.data?.message || 'Greška pri čuvanju budžeta.'
  } finally {
    loadingPostavi.value = false
  }
}

async function izvrsiPreraspodelu() {
  preraspoError.value = ''
  preraspoUspeh.value = false

  if (!preraspoForma.value.izvorZanrId || !preraspoForma.value.odredisteZanrId) {
    preraspoError.value = 'Oba žanra su obavezna.'
    return
  }
  if (preraspoForma.value.izvorZanrId === preraspoForma.value.odredisteZanrId) {
    preraspoError.value = 'Izvorni i odredišni žanr ne mogu biti isti.'
    return
  }
  if (!preraspoForma.value.iznos || preraspoForma.value.iznos <= 0) {
    preraspoError.value = 'Unesite validan iznos.'
    return
  }

  loadingPreraspo.value = true
  try {
    await budzetApi.prerasporedi({
      izvorZanrId: preraspoForma.value.izvorZanrId,
      odredisteZanrId: preraspoForma.value.odredisteZanrId,
      iznos: Number(preraspoForma.value.iznos)
    })
    preraspoUspeh.value = true
    preraspoForma.value = { izvorZanrId: null, odredisteZanrId: null, iznos: '' }
    await ucitaj()
    setTimeout(() => { preraspoUspeh.value = false }, 2000)
  } catch (e) {
    preraspoError.value = e.response?.data?.message || 'Greška pri preraspodeli.'
  } finally {
    loadingPreraspo.value = false
  }
}

onMounted(ucitaj)
</script>

<style scoped>
/* Osnovni raspored */
.budzet-wrapper { 
  width: 100%; 
  max-width: 100%;
  box-sizing: border-box;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* Page Header sa slika */
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

/* Elegantne summary kartice u belom kontejneru sa zaobljenjem 20px */
.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.25rem;
  margin-bottom: 2.5rem;
}
.summary-card {
  background: #ffffff;
  border: none;
  border-radius: 20px;
  padding: 1.4rem 1.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.05);
}
.summary-label {
  font-size: 0.8rem;
  color: #556644;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  font-weight: 600;
}
.summary-value {
  font-size: 1.55rem;
  font-weight: 700;
  color: #3f4e37;
  letter-spacing: -0.01em;
}
.summary-value--potroseno { color: #b91c1c; }
.summary-value--dostupno { color: #15803d; }

/* Naslovi sekcija */
.sekcija { margin-bottom: 2.5rem; }
.sekcija-naslov { margin: 0 0 1.2rem; color: #3f4e37; font-size: 1.35rem; font-weight: 700; }

/* Bela Tabela ekrana sa zaobljenim ivicama (20px) */
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

.td-zanr { font-weight: 600; color: #3f4e37; }
.td-iznos { color: #444444; }
.td-iznos-potroseno { color: #666666; }

.iznos--nizak { color: #b91c1c; font-weight: 600; background: rgba(220, 38, 38, 0.08); padding: 0.25rem 0.65rem; border-radius: 50px; font-size: 0.85rem; }
.iznos--ok { color: #15803d; font-weight: 600; background: rgba(34, 197, 94, 0.08); padding: 0.25rem 0.65rem; border-radius: 50px; font-size: 0.85rem; }

/* Modernizovani Progress Bar unutar tabele */
.progress-bar-wrapper { display: flex; align-items: center; gap: 0.75rem; }
.progress-bar {
  flex: 1; height: 8px; background: #eef0ea;
  border-radius: 99px; overflow: hidden;
}
.progress-bar-fill { height: 100%; border-radius: 99px; transition: width 0.4s ease; }
.fill--ok { background: #7a8f6e; } /* Maslinasto zelena sa slika */
.fill--visoko { background: #b91c1c; }
.progress-label { font-size: 0.825rem; font-weight: 600; min-width: 35px; }
.text--ok { color: #556644; }
.text--visoko { color: #b91c1c; }

/* Grid raspored donjih formi */
.forme-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.75rem;
}

/* Kartice formi (Bela pozadina + 20px zaobljenje) */
.forma-kartica {
  background: #ffffff;
  border: none; 
  border-radius: 20px;
  padding: 1.85rem; 
  display: flex; 
  flex-direction: column; 
  gap: 1.2rem;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.06);
}
.forma-kartica h2 { margin: 0; color: #3f4e37; font-size: 1.3rem; font-weight: 700; }
.panel-opis { margin: -0.75rem 0 0.25rem 0; font-size: 0.875rem; color: #666666; line-height: 1.4; }

/* Polja za unos (Input i Select) sa slika */
.form-group { display: flex; flex-direction: column; gap: 0.45rem; }
.form-group label { font-size: 0.875rem; color: #556644; font-weight: 600; }
.form-group input,
.form-group select {
  padding: 0.75rem 1rem; 
  border: 1px solid #e2e8f0; 
  border-radius: 12px;
  font-size: 0.95rem; 
  color: #333333; 
  background: #f8fafc;
  font-family: inherit; 
  outline: none; 
  box-sizing: border-box;
  transition: all 0.2s ease;
}
.form-group input:focus,
.form-group select:focus { 
  border-color: #7a8f6e; 
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(122, 143, 110, 0.15);
}

.forma-akcije { display: flex; gap: 0.75rem; justify-content: flex-end; margin-top: 0.5rem; }

/* Glavna i sporedna dugmad */
.btn-primary {
  background: #7a8f6e; 
  color: #fff; 
  border: none; 
  border-radius: 12px;
  padding: 0.75rem 1.5rem; 
  font-size: 0.9rem; 
  font-weight: 600;
  cursor: pointer; 
  font-family: inherit; 
  transition: all 0.2s ease;
}
.btn-primary:hover:not(:disabled) { background: #6b7e60; transform: translateY(-1px); }
.btn-primary:disabled { opacity: 0.4; cursor: not-allowed; }

.btn-sekundarni {
  background: transparent; 
  color: #556644; 
  border: 1px solid #e2e8f0;
  border-radius: 12px; 
  padding: 0.75rem 1.5rem; 
  font-size: 0.9rem;
  font-weight: 500; 
  cursor: pointer; 
  font-family: inherit; 
  transition: all 0.2s ease;
}
.btn-sekundarni:hover { background: #f4f6f0; border-color: #cbd5e1; }

/* Akciona dugmad u tabeli (Pill oblik) */
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
.btn-izmeni { background: rgba(122, 143, 110, 0.12); color: #3f4e37; }
.btn-izmeni:hover { background: #7a8f6e; color: #ffffff; transform: translateY(-1px); }
.text-right { text-align: right; }

/* Stanja na ekranu */
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
.state-msg--error { color: #b91c1c; }

/* Spinner */
.spinner {
  width: 30px; 
  height: 30px;
  border: 3px solid #e2e8f0;
  border-top-color: #7a8f6e;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Notifikacije / Alerte */
.alert { padding: 0.85rem 1.25rem; border-radius: 12px; font-size: 0.9rem; font-weight: 500; }
.alert--error { background: rgba(220, 38, 38, 0.08); color: #b91c1c; }
.alert--uspeh { background: rgba(34, 197, 94, 0.08); color: #15803d; }

/* CSS Animacija pojavljivanja */
.animated-fade-in { animation: fadeIn 0.35s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
</style>    