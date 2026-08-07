<template>
  <div class="predlozi-wrapper">
    
    <div class="page-header">
      <div class="header-tekst">
        <h1>Predlozi naslova</h1>
        <p class="subtitle">Arhiva odobrenih predloga za nabavku literature</p>
      </div>
    </div>

    <div v-if="loading" class="state-msg">
      <div class="spinner"></div>
      <p>Učitavanje odobrenih predloga...</p>
    </div>
    <div v-else-if="error" class="state-msg state-msg--error">
      <span class="state-icon">❌</span>
      <p>{{ error }}</p>
    </div>
    <div v-else-if="predlozi.length === 0" class="state-msg state-msg--empty">
      <span class="state-icon">📚</span>
      <p>Nema odobrenih predloga.</p>
    </div>

    <div v-else class="table-wrapper animated-fade-in">
      <table class="tabla">
        <thead>
          <tr>
            <th>Korisnik</th>
            <th>Naslov knjige</th>
            <th>Autor</th>
            <th>Datum podnošenja</th>
            <th>Zanr</th>
            <th>Okvirna cena</th>
            <th>Akcije</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in predlozi" :key="p.id">
            <td class="td-korisnik">👤 {{ p.korisnikIme }} {{ p.korisnikPrezime }}</td>
            <td class="td-naslov">{{ p.naslov }}</td>
            <td class="td-autor">{{ p.autor }}</td>
            <td class="td-datum">📅 {{ p.datumPodnosenja }}</td>
            <td>{{ p.zanrNaziv }}</td>
            <td>
              {{ p.okvirnaCena ? p.okvirnaCena.toFixed(2) + ' RSD' : '-' }}
            </td>

            <td class="akcije">
              <button
                  class="btn-odobri"
                  @click="obradiPredlog(p.id, true)"
              >
                  ✓ Odobri
              </button>

              <button
                  class="btn-odbij"
                  @click="obradiPredlog(p.id, false)"
              >
                  ✕ Odbij
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { predlogApi } from '../../services/api.js'

const predlozi = ref([])
const loading = ref(false)
const error = ref('')

async function ucitaj() {
  loading.value = true
  error.value = ''
  try {
    const res = await predlogApi.odobreniPredlozi()
    predlozi.value = res.data
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri učitavanju predloga.'
  } finally {
    loading.value = false
  }
}

async function obradiPredlog(id, odobren) {

  try {

    await predlogApi.obradiPredlogMenadzer(id, odobren)

    predlozi.value = predlozi.value.filter(
        p => p.id !== id
    )

  } catch (e) {

    alert(
      e.response?.data?.message ||
      'Greška prilikom obrade predloga.'
    )

  }
}

onMounted(ucitaj)
</script>

<style scoped>
/* Prilagođeno punoj širini u stilu ostalih ekrana */
.predlozi-wrapper { 
  width: 100%; 
  max-width: 100%;
  box-sizing: border-box;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* Pozicioniranje i boje zaglavlja usklađeni sa slikama */
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
  color: #3f4e37; /* Prepoznatljiva tamno zelena */
}
.subtitle { 
  color: #556644; 
  font-size: 1rem; 
  margin: 0;
  opacity: 0.85;
}

.akcije {
  display: flex;
  gap: 0.75rem;
}

.btn-odobri {
  border: none;
  background: #22c55e;
  color: white;
  padding: 0.55rem 1rem;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 600;
  transition: 0.2s;
}

.btn-odobri:hover {
  transform: translateY(-1px);
  opacity: 0.9;
}

.btn-odbij {
  border: none;
  background: #ef4444;
  color: white;
  padding: 0.55rem 1rem;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 600;
  transition: 0.2s;
}

.btn-odbij:hover {
  transform: translateY(-1px);
  opacity: 0.9;
}

/* USKLAĐENO: Čisto bela pozadina i mekano zaobljene ivice (20px) */
.table-wrapper { 
  background: #ffffff !important;
  border: none; 
  border-radius: 20px; 
  overflow: hidden; 
  width: 100%;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.06);
}

.tabla { width: 100%; border-collapse: collapse; font-size: 0.95rem; text-align: left; }
.tabla thead { background: #f4f6f0; } /* Suptilni zelenkasti ton za zaglavlje */
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

/* Tipografija elemenata unutar redova */
.td-korisnik { font-weight: 500; color: #556644; }
.td-naslov { font-weight: 600; color: #3f4e37; }
.td-autor { color: #666666; }
.td-datum { color: #666666; font-size: 0.9rem; white-space: nowrap; }
.text-right { text-align: right; }

/* Elegantan fiksni zeleni bedž za status pošto su ovo samo odobreni predlozi */
.status-badge-odobreno {
  display: inline-flex;
  align-items: center;
  padding: 0.35rem 0.9rem; 
  border-radius: 50px; 
  font-size: 0.8rem; 
  font-weight: 600; 
  background: rgba(34, 197, 94, 0.1); 
  color: #15803d;
}

/* Stanja ekrana (Loading, Error, Prazno) */
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

/* Animirani spinner za učitavanje */
.spinner {
  width: 30px; 
  height: 30px;
  border: 3px solid #e2e8f0;
  border-top-color: #7a8f6e; /* Maslinasta boja pretraživača sa slika */
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Glatka CSS animacija za pojavljivanje tabele */
.animated-fade-in { animation: fadeIn 0.35s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
</style>