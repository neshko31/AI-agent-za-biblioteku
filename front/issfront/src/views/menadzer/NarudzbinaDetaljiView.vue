<template>
  <div class="detalji-wrapper">
    <!-- Gornje zaglavlje sa dugmetom za nazad -->
    <div class="page-header">
      <div class="header-tekst">
        <h1>Narudžbine</h1>
        <p class="subtitle">Pregled detalja, stavki i upravljanje statusom narudžbine</p>
      </div>
      <button class="btn-sekundarni" @click="router.push('/menadzer/narudzbine')">Nazad na listu</button>
    </div>

    <!-- Stanje učitavanja i greške -->
    <div v-if="loading" class="state-msg">
      <div class="spinner"></div>
      <p>Učitavanje detalja narudžbine...</p>
    </div>
    <div v-else-if="error" class="state-msg state-msg--error">
      <p>{{ error }}</p>
    </div>

    <div v-else class="animated-fade-in">
      <!-- Glavna kartica sa detaljima narudžbine -->
      <div class="kartica shadow-premium">
        <div class="kartica-header">
          <div class="naslov-grupa">
            <h2>Narudžbina #{{ narudzbina.id }}</h2>
            <span class="status-badge" :class="statusKlasa(narudzbina.status)">
              {{ statusNaziv(narudzbina.status) }}
            </span>
          </div>
          <div class="header-akcije">
            <button
              v-if="narudzbina.status === 'KREIRANA'"
              class="btn-akcija btn-potvrdi"
              @click="potvrdi"
              :disabled="!narudzbina.stavke || narudzbina.stavke.length === 0">
              Potvrdi narudžbinu
            </button>
            <button
              v-if="narudzbina.status === 'POTVRDJENA'"
              class="btn-akcija btn-isporuka"
              @click="showIsporukaModal = true">
              Evidentiraj isporuku
            </button>
            <button
              v-if="narudzbina.status === 'ISPORUCENA'"
              class="btn-akcija btn-reklamacija"
              @click="showReklamacijaModal = true">
              Pokreni reklamaciju
            </button>
          </div>
        </div>

        <!-- Pregledna tabela/mreža informacija -->
        <div class="info-grid">
          <div class="info-row">
            <span class="info-label">Dobavljač</span>
            <span class="info-value td-bold">{{ narudzbina.dobavljacNaziv }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">Ugovoreni popust</span>
            <span class="info-value ugovor-popust">{{ narudzbina.popust }}%</span>
          </div>
          <div class="info-row">
            <span class="info-label">Datum kreiranja</span>
            <span class="info-value">{{ narudzbina.datumKreiranja }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">Očekivana isporuka</span>
            <span class="info-value">{{ narudzbina.datumOcekivaneIsporuke }}</span>
          </div>
          <div class="info-row" v-if="narudzbina.datumStvarneIsporuke">
            <span class="info-label">Stvarna isporuka</span>
            <span class="info-value stvarna-isporuka-date">{{ narudzbina.datumStvarneIsporuke }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">Ukupna vrednost</span>
            <span class="info-value info-value--accent">{{ formatirajIznos(narudzbina.ukupnaCena) }}</span>
          </div>
          <div class="info-row" v-if="narudzbina.napomena">
            <span class="info-label">Napomena menadžera</span>
            <span class="info-value napomena-text">{{ narudzbina.napomena }}</span>
          </div>
        </div>
      </div>

      <!-- Pod-sekcija za Stavke -->
      <div class="sekcija-header">
        <h2>Stavke narudžbine</h2>
        <button
          v-if="narudzbina.status === 'KREIRANA'"
          class="btn-primary"
          @click="showDodajStavkuModal = true">
          Dodaj stavku
        </button>
      </div>

      <!-- Prazno stanje za stavke -->
      <div v-if="!narudzbina.stavke || narudzbina.stavke.length === 0" class="state-msg-stavke">
        Nema dodatih stavki. Koristite dugme iznad da dodate knjige u ovu narudžbinu.
      </div>

      <!-- Tabela stavki preko celog ekrana -->
      <div v-else class="table-wrapper shadow-premium">
        <table class="tabla">
          <thead>
            <tr>
              <th>Knjiga / Publikacija</th>
              <th>Autor</th>
              <th>Količina</th>
              <th>Cena po kom.</th>
              <th>Ukupna cena</th>
              <th v-if="narudzbina.status === 'KREIRANA'" class="text-right">Akcije</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in narudzbina.stavke" :key="s.id">
              <td class="td-bold">{{ s.naslov }}</td>
              <td>{{ s.autor }}</td>
              <td class="td-kolicina">{{ s.kolicina }}</td>
              <td>{{ formatirajIznos(s.cenaPoKomadu) }}</td>
              <td class="td-bold text-skupno">{{ formatirajIznos(s.ukupnaCenaStavke) }}</td>
              <td v-if="narudzbina.status === 'KREIRANA'" class="text-right">
                <button class="btn-akcija-tabela btn-otkazi" @click="ukloniStavku(s.id)">
                  Ukloni
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- MODAL: Dodaj stavku -->
    <div v-if="showDodajStavkuModal" class="modal-overlay" @click.self="showDodajStavkuModal = false">
      <div class="modal animated-scale-up">
        <h2>Dodaj stavku u narudžbinu</h2>
        <div v-if="stavkaError" class="alert alert--error">{{ stavkaError }}</div>

        <div class="form-group">
          <label>Knjiga</label>
          <select v-model="stavkaForma.isbn">
            <option :value="null" disabled>Izaberite knjigu sa liste</option>
            <option v-for="k in knjige" :key="k.predlogId ?? k.isbn" :value="k">
              {{ k.naslov }} — {{ k.autor }}
            </option>
          </select>
        </div>
        <div class="form-group">
          <label>Količina (komada)</label>
          <input v-model="stavkaForma.kolicina" type="number" min="1" placeholder="Unesite količinu, npr. 5" />
        </div>
        <div class="modal-akcije">
          <button class="btn-primary" @click="dodajStavku" :disabled="loadingStavka">
            {{ loadingStavka ? 'Dodavanje...' : 'Potvrdi i dodaj' }}
          </button>
          <button class="btn-sekundarni" @click="showDodajStavkuModal = false">Odustani</button>
        </div>
      </div>
    </div>

    <!-- MODAL: Evidentiraj isporuku -->
    <div v-if="showIsporukaModal" class="modal-overlay" @click.self="showIsporukaModal = false">
      <div class="modal animated-scale-up">
        <h2>Evidentiranje realizacije isporuke</h2>
        <div v-if="isporukaError" class="alert alert--error">{{ isporukaError }}</div>
        <div class="form-group">
          <label>Datum stvarne isporuke od strane dobavljača</label>
          <input v-model="datumIsporuke" type="date" />
        </div>
        <div class="modal-akcije">
          <button class="btn-primary" @click="evidentirajIsporuku" :disabled="loading">Zavedi isporuku</button>
          <button class="btn-sekundarni" @click="showIsporukaModal = false">Zatvori</button>
        </div>
      </div>
    </div>

    <!-- MODAL: Kreiranje reklamacije -->
    <div v-if="showReklamacijaModal" class="modal-overlay" @click.self="showReklamacijaModal = false">
      <div class="modal animated-scale-up">
        <h2>Zapisnik o reklamaciji isporuke</h2>
        <div v-if="reklamacijaError" class="alert alert--error">{{ reklamacijaError }}</div>
        <div class="form-group">
          <label>Zvanični razlog reklamacije</label>
          <textarea v-model="reklamacijaForma.razlog" rows="3" placeholder="Opišite oštećenje, manjak ili drugi problem sa pošiljkom..."></textarea>
        </div>
        <div class="form-group">
          <label>Dodatne interne napomene (opciono)</label>
          <textarea v-model="reklamacijaForma.napomena" rows="2" placeholder="Dodatni detalji..."></textarea>
        </div>
        <div class="modal-akcije">
          <button class="btn-reklamacija-submit" @click="kreirajReklamaciju">Kreiraj reklamaciju</button>
          <button class="btn-sekundarni" @click="showReklamacijaModal = false">Odustani</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { narudzbinApi, reklamacijaApi, predlogApi, sistemskePreporukeApi } from '../../services/api.js'

const router = useRouter()
const route = useRoute()
const id = route.params.id

const narudzbina = ref({})
const knjige = ref([])
const loading = ref(false)
const error = ref('')

const showDodajStavkuModal = ref(false)
const showIsporukaModal = ref(false)
const showReklamacijaModal = ref(false)

const stavkaForma = ref({ isbn: null, kolicina: '', okvirnaCena: '' })
const stavkaError = ref('')
const loadingStavka = ref(false)

const datumIsporuke = ref('')
const isporukaError = ref('')

const reklamacijaForma = ref({ razlog: '', napomena: '' })
const reklamacijaError = ref('')

async function ucitaj() {
  loading.value = true
  error.value = ''
  try {
    const res = await narudzbinApi.getJedna(id)
    narudzbina.value = res.data
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri učitavanju narudžbine.'
  } finally {
    loading.value = false
  }
}

async function ucitajKnjige() {
  try {
    const [predloziRes, preporukeRes] = await Promise.all([
      predlogApi.zaNarudzbinu(),
      sistemskePreporukeApi.zaNarudzbinu()
    ])
    knjige.value = [
      ...predloziRes.data,
      ...preporukeRes.data
    ]
  } catch (e) {
    console.log('Greška pri učitavanju knjiga:', e)
  }
}

async function dodajStavku() {
  stavkaError.value = ''
  if (!stavkaForma.value.isbn || !stavkaForma.value.kolicina) {
    stavkaError.value = 'Sva polja su obavezna.'
    return
  }

  loadingStavka.value = true
  try {
    let slanjeIsbn = null
    let slanjePreporukaId = null
    let slanjePredlogId = null

    if (typeof stavkaForma.value.isbn === 'object' && stavkaForma.value.isbn !== null) {
      const selektovano = stavkaForma.value.isbn
      if (selektovano.predlogId !== null && selektovano.predlogId !== undefined) {
        slanjePredlogId = selektovano.predlogId
      } else {
        slanjeIsbn = selektovano.isbn
        slanjePreporukaId = selektovano.id 
      }
    } else {
      slanjeIsbn = stavkaForma.value.isbn
    }

    const res = await narudzbinApi.dodajStavku(id, {
      isbn: slanjeIsbn, 
      kolicina: Number(stavkaForma.value.kolicina),
      preporukaId: slanjePreporukaId,
      predlogId: slanjePredlogId
    })
    
    narudzbina.value = res.data
    showDodajStavkuModal.value = false
    stavkaForma.value = { isbn: null, kolicina: '', okvirnaCena: '' }
  } catch (e) {
    stavkaError.value = e.response?.data?.message || 'Greška pri dodavanju stavke.'
  } finally {
    loadingStavka.value = false
  }
}

async function ukloniStavku(stavkaId) {
  if (loading.value) return
  try {
    const res = await narudzbinApi.ukloniStavku(id, stavkaId)
    narudzbina.value = res.data
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri uklanjanju stavke.'
  }
}

async function potvrdi() {
  try {
    const res = await narudzbinApi.potvrdi(id)
    narudzbina.value = res.data
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri potvrđivanju narudžbine.'
  }
}

async function evidentirajIsporuku() {
  isporukaError.value = ''
  if (!datumIsporuke.value) {
    isporukaError.value = 'Datum isporuke je obavezan.'
    return
  }
  try {
    const res = await narudzbinApi.evidentirajIsporuku(id, {
      datumStvarneIsporuke: datumIsporuke.value
    })
    narudzbina.value = res.data
    showIsporukaModal.value = false
  } catch (e) {
    isporukaError.value = e.response?.data?.message || 'Greška pri evidentiranju isporuke.'
  }
}

async function kreirajReklamaciju() {
  reklamacijaError.value = ''
  if (!reklamacijaForma.value.razlog.trim()) {
    reklamacijaError.value = 'Razlog je obavezan.'
    return
  }
  try {
    await reklamacijaApi.kreiraj(id, reklamacijaForma.value)
    await ucitaj()
    showReklamacijaModal.value = false
    reklamacijaForma.value = { razlog: '', napomena: '' }
  } catch (e) {
    reklamacijaError.value = e.response?.data?.message || 'Greška pri kreiranju reklamacije.'
  }
}

function statusKlasa(status) {
  if (status === 'KREIRANA') return 'status--kreirana'
  if (status === 'ISPORUCENA') return 'status--isporucena'
  if (status === 'REKLAMIRANA') return 'status--reklamirana'
  if (status === 'POTVRDJENA') return 'status--potvrdjena'
  return 'status--otkazana'
}

function statusNaziv(status) {
  if (status === 'KREIRANA') return '• Kreirana'
  if (status === 'ISPORUCENA') return '• Isporučena'
  if (status === 'REKLAMIRANA') return '• Reklamirana'
  if (status === 'OTKAZANA') return '• Otkazana'
  if (status === 'POTVRDJENA') return '• Potvrđena'
  return status
}

function formatirajIznos(iznos) {
  if (!iznos) return '0,00 RSD'
  return Number(iznos).toLocaleString('sr-RS', { minimumFractionDigits: 2 }) + ' RSD'
}

onMounted(() => {
  ucitaj()
  ucitajKnjige()
})
</script>

<style scoped>
/* Promenjeno na full-width prikaz ekrana */
.detalji-wrapper { 
  width: 100%; 
  max-width: 100%; 
  box-sizing: border-box;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* Page Header */
.page-header {
  display: flex; 
  justify-content: space-between;
  align-items: center; 
  margin-bottom: 1.5rem;
}
/* Smanjene dimenzije fontova */
.page-header h1 { margin: 0 0 0.25rem; font-size: 1.85rem; font-weight: 700; color: #3f4e37; letter-spacing: -0.02em; }
.subtitle { color: #556644; font-size: 0.9rem; margin: 0; opacity: 0.85; }

/* Glavna kartica */
.kartica { 
  background: #ffffff; 
  border: none; 
  border-radius: 20px; 
  padding: 1.5rem; 
  margin-bottom: 2rem; 
}
.kartica-header {
  display: flex; 
  justify-content: space-between; 
  align-items: center;
  margin-bottom: 1.5rem; 
  flex-wrap: wrap; 
  gap: 1rem;
}
.naslov-grupa { display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap; }
.kartica-header h2 { margin: 0; color: #3f4e37; font-size: 1.4rem; font-weight: 700; }

.header-akcije { display: flex; gap: 0.5rem; flex-wrap: wrap; align-items: center; }

/* Kompaktnija mreža informacija i sitniji tekst */
.info-grid { display: flex; flex-direction: column; gap: 0.15rem; }
.info-row { 
  display: flex; 
  align-items: center;
  gap: 1.5rem; 
  padding: 0.75rem 0.5rem; 
  border-bottom: 1px solid #f4f6f0; 
}
.info-row:last-child { border-bottom: none; }
.info-label { width: 160px; flex-shrink: 0; font-size: 0.8rem; color: #666666; font-weight: 600; text-transform: uppercase; letter-spacing: 0.03em; }
.info-value { color: #333333; font-size: 0.9rem; }
.info-value--accent { color: #3f4e37; font-weight: 700; font-size: 1.05rem; }
.ugovor-popust { color: #15803d; font-weight: 700; }
.stvarna-isporuka-date { color: #16a34a; font-weight: 600; }
.napomena-text { font-style: italic; color: #555555; }

/* Sekcija stavki */
.sekcija-header {
  display: flex; 
  justify-content: space-between;
  align-items: center; 
  margin-bottom: 1rem;
}
.sekcija-header h2 { margin: 0; color: #3f4e37; font-size: 1.3rem; font-weight: 700; }

/* Tabela stavki preko cele širine */
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
.td-bold { font-weight: 600; color: #3f4e37; }
.td-kolicina { font-weight: 700; color: #556644; font-family: monospace; font-size: 0.9rem; }
.text-skupno { color: #3f4e37; }

/* Statusni Bedževi */
.status-badge { padding: 0.3rem 0.75rem; border-radius: 50px; font-size: 0.75rem; font-weight: 700; letter-spacing: 0.02em; }
.status--kreirana { background: rgba(234, 179, 8, 0.08); color: #b45309; }
.status--isporucena { background: rgba(34, 197, 94, 0.08); color: #15803d; }
.status--reklamirana { background: rgba(220, 38, 38, 0.08); color: #b91c1c; }
.status--potvrdjena { background: rgba(122, 143, 110, 0.15); color: #3f4e37; }
.status--otkazana { background: rgba(107, 114, 128, 0.08); color: #4b5563; }

/* Dugmad */
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

.btn-akcija {
  padding: 0.5rem 1.1rem; border-radius: 50px; font-size: 0.8rem;
  font-weight: 600; border: none; cursor: pointer; font-family: inherit; transition: all 0.2s ease;
}
.btn-akcija:hover:not(:disabled) { transform: translateY(-1px); opacity: 0.9; }
.btn-akcija:disabled { opacity: 0.4; cursor: not-allowed; transform: none; }
.btn-potvrdi { background: #16a34a; color: #ffffff; box-shadow: 0 2px 6px rgba(22, 163, 74, 0.2); }
.btn-isporuka { background: #7a8f6e; color: #ffffff; box-shadow: 0 2px 6px rgba(122, 143, 110, 0.2); }
.btn-reklamacija { background: rgba(220, 38, 38, 0.08); color: #b91c1c; border: 1px solid rgba(220, 38, 38, 0.15); }
.btn-reklamacija-submit { background: #b91c1c; color: #ffffff; border-radius: 10px; padding: 0.65rem 1.25rem; font-size: 0.875rem; font-weight: 600; border: none; cursor: pointer; font-family: inherit; transition: all 0.2s; }
.btn-reklamacija-submit:hover { background: #991b1b; }

.btn-akcija-tabela { padding: 0.35rem 0.85rem; border-radius: 50px; font-size: 0.75rem; font-weight: 600; border: none; cursor: pointer; transition: all 0.15s; }
.btn-otkazi { background: rgba(220, 38, 38, 0.08); color: #b91c1c; }
.btn-otkazi:hover { background: #b91c1c; color: #ffffff; }

/* Modali */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(25, 33, 21, 0.4);
  backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 100;
}
.modal {
  background: #ffffff; border: none; border-radius: 20px;
  padding: 2rem; max-width: 450px; width: 90%;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
}
.modal h2 { margin: 0 0 1rem; color: #3f4e37; font-size: 1.3rem; font-weight: 700; letter-spacing: -0.01em; }

.form-group { display: flex; flex-direction: column; gap: 0.4rem; margin-bottom: 1rem; }
.form-group label { font-size: 0.8rem; color: #556644; font-weight: 600; }
.form-group input,
.form-group select,
.form-group textarea {
  padding: 0.65rem 0.85rem; border: 1px solid #e2e8f0; border-radius: 10px;
  font-size: 0.9rem; color: #333333; background: #f8fafc;
  font-family: inherit; outline: none; transition: all 0.15s; box-sizing: border-box; width: 100%;
}
.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus { 
  border-color: #7a8f6e; background: #ffffff; box-shadow: 0 0 0 3px rgba(122, 143, 110, 0.15); 
}
.form-group textarea { resize: vertical; }
.modal-akcije { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1.5rem; }

.alert { padding: 0.75rem 1rem; border-radius: 10px; font-size: 0.85rem; margin-bottom: 1rem; font-weight: 500; }
.alert--error { background: rgba(220, 38, 38, 0.08); color: #b91c1c; }

/* Stanja */
.state-msg, .state-msg-stavke { 
  text-align: center; padding: 3rem 1.5rem; background: #ffffff; border-radius: 20px;
  display: flex; flex-direction: column; align-items: center; gap: 0.75rem; color: #556644; font-weight: 500; font-size: 0.9rem;
}
.state-msg-stavke { padding: 2.5rem; background: rgba(255, 255, 255, 0.5); border: 2px dashed #eef0ea; box-shadow: none; }
.state-msg--error p { color: #b91c1c; }
.text-right { text-align: right; }

.spinner { width: 30px; height: 30px; border: 3px solid #eef0ea; border-top-color: #7a8f6e; border-radius: 50%; animation: spin 0.85s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.shadow-premium { box-shadow: 0 4px 18px rgba(0, 0, 0, 0.06); }

.animated-fade-in { animation: fadeIn 0.35s ease-out; }
.animated-scale-up { animation: scaleUp 0.25s cubic-bezier(0.34, 1.56, 0.64, 1); }
@keyframes fadeIn { from { opacity: 0; transform: translateY(6px); } to { opacity: 1; transform: translateY(0); } }
@keyframes scaleUp { from { transform: scale(0.97); opacity: 0; } to { transform: scale(1); opacity: 1; } }
</style>