<template>
  <div class="forma-wrapper">
    <!-- Zaglavlje stranice skroz levo -->
    <div class="page-header">
      <div class="header-tekst">
        <h1>Narudžbine</h1>
        <p class="subtitle">Kreiranje nove narudžbine i provera ugovornih uslova</p>
      </div>
    </div>

    <!-- Glavna forma smeštena u elegantnu belu karticu sa 20px zaobljenjem -->
    <div class="forma-kartica animated-fade-in">
      
      <!-- Obaveštenja o uspehu ili greškama -->
      <div v-if="error" class="alert alert--error">{{ error }}</div>
      <div v-if="uspeh" class="alert alert--uspeh">Narudžbina je uspešno kreirana!</div>

      <!-- Izbor dobavljača -->
      <div class="form-group">
        <label>Dobavljač</label>
        <select v-model="forma.dobavljacId" @change="ucitajUgovor">
          <option :value="null" disabled>Izaberite dobavljača</option>
          <option v-for="d in dobavljaci" :key="d.id" :value="d.id">
            {{ d.naziv }}
          </option>
        </select>
      </div>

      <!-- Stanje učitavanja ugovora -->
      <div v-if="loadingUgovor" class="ugovor-loading">
        <div class="spinner-small"></div>
        <span>Provera aktivnog ugovora...</span>
      </div>

      <!-- Informacije o aktivnom ugovoru (Ulepšan info-panel) -->
      <div v-if="aktivniUgovor" class="ugovor-info">
        <h3>Aktivni ugovor sa dobavljačem</h3>
        <div class="ugovor-grid">
          <div class="ugovor-card">
            <span class="ugovor-label">Popust</span>
            <span class="ugovor-value ugovor-value--popust">{{ aktivniUgovor.popust }}%</span>
          </div>
          <div class="ugovor-card">
            <span class="ugovor-label">Rok isporuke</span>
            <span class="ugovor-value">{{ aktivniUgovor.rokIsporuke }} dan(a)</span>
          </div>
          <div class="ugovor-card">
            <span class="ugovor-label">Važi do</span>
            <span class="ugovor-value ugovor-value--datum">{{ aktivniUgovor.datumIsteka }}</span>
          </div>
        </div>
      </div>

      <!-- Greška ukoliko nema aktivnog ugovora -->
      <div v-if="forma.dobavljacId && !aktivniUgovor && !loadingUgovor" class="alert alert--error">
        Ovaj dobavljač nema aktivan komercijalni ugovor. Kreiranje narudžbine nije moguće.
      </div>

      <!-- Polje za napomenu -->
      <div class="form-group">
        <label>Napomena (opciono)</label>
        <textarea v-model="forma.napomena" rows="4" placeholder="Unesite specifične zahteve isporuke ili interne napomene..."></textarea>
      </div>

      <!-- Akcije na dnu forme -->
      <div class="forma-akcije">
        <button class="btn-sekundarni" @click="router.back()">
          Odustani
        </button>
        <button
          class="btn-primary"
          @click="sacuvaj"
          :disabled="loading || !aktivniUgovor"
        >
          {{ loading ? 'Kreiranje...' : 'Kreiraj narudžbinu' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { narudzbinApi, dobavljacApi, ugovorApi } from '../../services/api.js'

const router = useRouter()

const dobavljaci = ref([])
const aktivniUgovor = ref(null)
const loadingUgovor = ref(false)
const loading = ref(false)
const error = ref('')
const uspeh = ref(false)

const forma = ref({
  dobavljacId: null,
  ugovorId: null,
  napomena: ''
})

async function ucitajDobavljace() {
  try {
    const res = await dobavljacApi.svi()
    dobavljaci.value = res.data.filter(d => d.status === 'AKTIVAN')
  } catch (e) {
    error.value = 'Greška pri učitavanju dobavljača.'
  }
}

async function ucitajUgovor() {
  aktivniUgovor.value = null
  forma.value.ugovorId = null
  if (!forma.value.dobavljacId) return

  loadingUgovor.value = true
  try {
    const res = await ugovorApi.sviZaDobavljaca(forma.value.dobavljacId)
    const aktivni = res.data.find(u => u.status === 'AKTIVAN')
    if (aktivni) {
      aktivniUgovor.value = aktivni
      forma.value.ugovorId = aktivni.id
    }
  } catch (e) {
    // tiho
  } finally {
    loadingUgovor.value = false
  }
}

async function sacuvaj() {
  error.value = ''
  uspeh.value = false

  if (!forma.value.dobavljacId || !forma.value.ugovorId) {
    error.value = 'Izaberite dobavljača sa aktivnim ugovorom.'
    return
  }

  loading.value = true
  try {
    const res = await narudzbinApi.kreiraj({
      dobavljacId: forma.value.dobavljacId,
      ugovorId: forma.value.ugovorId,
      napomena: forma.value.napomena
    })
    uspeh.value = true
    setTimeout(() => router.push(`/menadzer/narudzbine/${res.data.id}`), 1500)
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri kreiranju narudžbine.'
  } finally {
    loading.value = false
  }
}

onMounted(ucitajDobavljace)
</script>

<style scoped>
/* Osnovni raspored i centriranje forme */
.forma-wrapper { 
  width: 100%; 
  max-width: 650px; 
  margin: 0 auto;
  box-sizing: border-box;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* Page Header */
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

/* Elegantna bela kartica za formu (20px zaobljenje) */
.forma-kartica {
  background: #ffffff;
  border: none;
  border-radius: 20px;
  padding: 2.25rem; 
  display: flex; 
  flex-direction: column; 
  gap: 1.5rem;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.06);
}

/* Polja forme (Form groups i kontrole) */
.form-group { display: flex; flex-direction: column; gap: 0.5rem; }
.form-group label { font-size: 0.875rem; color: #556644; font-weight: 600; }
.form-group select,
.form-group textarea {
  padding: 0.8rem 1rem; 
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
.form-group select:focus,
.form-group textarea:focus { 
  border-color: #7a8f6e; 
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(122, 143, 110, 0.15);
}
.form-group textarea { resize: vertical; min-height: 100px; }

/* Modernizovani info-panel za ugovor */
.ugovor-info {
  background: #f4f6f0; 
  border: 1px solid #eef0ea;
  border-radius: 16px; 
  padding: 1.25rem 1.5rem;
}
.ugovor-info h3 { 
  margin: 0 0 1rem 0; 
  color: #3f4e37; 
  font-size: 0.9rem; 
  font-weight: 700;
  text-transform: uppercase; 
  letter-spacing: 0.06em; 
}
.ugovor-grid { 
  display: grid; 
  grid-template-columns: repeat(3, 1fr); 
  gap: 1rem; 
}
.ugovor-card {
  background: #ffffff;
  padding: 0.75rem 1rem;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.02);
}
.ugovor-label { font-size: 0.75rem; color: #666666; font-weight: 500; }
.ugovor-value { font-weight: 700; color: #3f4e37; font-size: 1rem; }
.ugovor-value--popust { color: #15803d; }
.ugovor-value--datum { color: #556644; }

/* Loader unutar ugovornog panela */
.ugovor-loading {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  font-size: 0.9rem;
  color: #556644;
  padding: 0.5rem;
}
.spinner-small {
  width: 16px;
  height: 16px;
  border: 2px solid #e2e8f0;
  border-top-color: #7a8f6e;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

/* Akcije i dugmad */
.forma-akcije { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 0.5rem; }

.btn-primary {
  background: #7a8f6e; 
  color: #fff; 
  border: none; 
  border-radius: 12px;
  padding: 0.75rem 1.75rem; 
  font-size: 0.95rem; 
  font-weight: 600;
  cursor: pointer; 
  font-family: inherit; 
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(122, 143, 110, 0.2);
}
.btn-primary:hover:not(:disabled) { background: #6b7e60; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(122, 143, 110, 0.3); }
.btn-primary:disabled { opacity: 0.45; cursor: not-allowed; transform: none !important; box-shadow: none !important; }

.btn-sekundarni {
  background: transparent; 
  color: #556644; 
  border: 1px solid #e2e8f0;
  border-radius: 12px; 
  padding: 0.75rem 1.75rem; 
  font-size: 0.95rem;
  font-weight: 500; 
  cursor: pointer; 
  font-family: inherit; 
  transition: all 0.2s ease;
}
.btn-sekundarni:hover { background: #f4f6f0; border-color: #cbd5e1; }

/* Obaveštenja / Alerte */
.alert { padding: 0.9rem 1.25rem; border-radius: 12px; font-size: 0.9rem; font-weight: 500; line-height: 1.4; }
.alert--error { background: rgba(220, 38, 38, 0.08); color: #b91c1c; }
.alert--uspeh { background: rgba(34, 197, 94, 0.08); color: #15803d; }

/* Animacija i pomoćne klase */
@keyframes spin { to { transform: rotate(360deg); } }
.animated-fade-in { animation: fadeIn 0.35s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
</style>