<template>
  <div class="narudzbine-wrapper">
    <div class="page-header">
      <div class="header-tekst">
        <h1>Narudžbine</h1>
        <p class="subtitle">Pregled i upravljanje svim narudžbinama literature</p>
      </div>
      <RouterLink to="/menadzer/narudzbine/nova" class="btn-primary animate-hover">
        ➕ Nova narudžbina
      </RouterLink>
    </div>

    <div v-if="loading" class="state-msg">
      <div class="spinner"></div>
      <p>Učitavanje narudžbina...</p>
    </div>
    <div v-else-if="error" class="state-msg state-msg--error">
      <span class="state-icon">❌</span>
      <p>{{ error }}</p>
    </div>
    <div v-else-if="narudzbine.length === 0" class="state-msg">
      <span class="state-icon">📦</span>
      <p>Trenutno nema evidentiranih narudžbina.</p>
    </div>

    <div v-else class="animated-fade-in table-wrapper">
      <table class="tabla">
        <thead>
          <tr>
            <th>ID</th>
            <th>Dobavljač</th>
            <th>Datum kreiranja</th>
            <th>Očekivana isporuka</th>
            <th>Ukupna cena</th>
            <th>Status</th>
            <th class="text-right">Akcije</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="n in narudzbine" :key="n.id">
            <td class="td-id">#{{ n.id }}</td>
            <td class="td-dobavljac"> {{ n.dobavljacNaziv }}</td>
            <td class="td-datum">{{ n.datumKreiranja }}</td>
            <td class="td-datum">{{ n.datumOcekivaneIsporuke }}</td>
            <td class="td-cena">{{ formatirajIznos(n.ukupnaCena) }}</td>
            <td>
              <span class="status-badge" :class="statusKlasa(n.status)">
                {{ statusNaziv(n.status) }}
              </span>
            </td>
            <td class="text-right">
              <RouterLink :to="`/menadzer/narudzbine/${n.id}`" class="btn-akcija btn-detalji">
                🔍 Detalji
              </RouterLink>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { narudzbinApi } from '../../services/api.js'

const narudzbine = ref([])
const loading = ref(false)
const error = ref('')

async function ucitaj() {
  loading.value = true
  error.value = ''
  try {
    const res = await narudzbinApi.getSve()
    narudzbine.value = res.data
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri učitavanju narudžbina.'
  } finally {
    loading.value = false
  }
}

function formatirajIznos(iznos) {
  if (!iznos) return '0,00 RSD'
  return Number(iznos).toLocaleString('sr-RS', { 
    minimumFractionDigits: 2,
    maximumFractionDigits: 2 
  }) + ' RSD'
}

function statusKlasa(status) {
  if (status === 'KREIRANA') return 'status--kreirana'
  if (status === 'ISPORUCENA') return 'status--isporucena'
  if (status === 'REKLAMIRANA') return 'status--reklamirana'
  return 'status--otkazana'
}

function statusNaziv(status) {
  if (status === 'KREIRANA') return '• Kreirana'
  if (status === 'ISPORUCENA') return '• Isporučena'
  if (status === 'REKLAMIRANA') return '• Reklamirana'
  return status
}

onMounted(ucitaj)
</script>

<style scoped>
/* Osnovni raspored i fontovi */
.narudzbine-wrapper { 
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
  margin-bottom: 2rem;
  width: 100%;
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

/* Tipografija u tabeli */
.td-id { color: #888888; font-weight: 500; font-family: monospace; font-size: 0.95rem; }
.td-dobavljac { font-weight: 600; color: #3f4e37; }
.td-datum { color: #555555; }
.td-cena { font-weight: 600; color: #3f4e37; }

/* Moderni statusni bedževi (Pill oblik) */
.status-badge { 
  padding: 0.35rem 0.85rem; 
  border-radius: 50px; 
  font-size: 0.825rem; 
  font-weight: 700; 
  display: inline-block;
  letter-spacing: 0.02em;
}
.status--kreirana { background: rgba(234, 179, 8, 0.08); color: #b45309; }
.status--isporucena { background: rgba(34, 197, 94, 0.08); color: #15803d; }
.status--reklamirana { background: rgba(220, 38, 38, 0.08); color: #b91c1c; }
.status--otkazana { background: rgba(107, 114, 128, 0.08); color: #4b5563; }

/* Glavno dugme (Zeleno) */
.btn-primary {
  background: #7a8f6e; 
  color: #fff; 
  border: none; 
  border-radius: 12px;
  padding: 0.75rem 1.5rem; 
  font-size: 0.9rem; 
  font-weight: 600;
  cursor: pointer; 
  text-decoration: none;
  font-family: inherit; 
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(122, 143, 110, 0.2);
}
.btn-primary:hover { background: #6b7e60; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(122, 143, 110, 0.3); }

/* Akciono dugme u tabeli (Pill oblik) */
.btn-akcija {
  padding: 0.45rem 1.1rem; 
  border-radius: 50px; 
  font-size: 0.825rem;
  font-weight: 600; 
  border: none; 
  cursor: pointer; 
  text-decoration: none;
  font-family: inherit; 
  transition: all 0.2s ease;
  display: inline-block;
}
.btn-detalji { background: rgba(122, 143, 110, 0.12); color: #3f4e37; }
.btn-detalji:hover { background: #7a8f6e; color: #ffffff; transform: translateY(-1px); }
.text-right { text-align: right; }

/* Stanja na ekranu (Loading, Empty, Error) */
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
.state-msg--error p { color: #b91c1c; }

/* Prelepi Spinner */
.spinner {
  width: 35px; 
  height: 35px;
  border: 3.5px solid #eef0ea;
  border-top-color: #7a8f6e;
  border-radius: 50%;
  animation: spin 0.85s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* CSS Animacija pojavljivanja elemenata */
.animated-fade-in { animation: fadeIn 0.35s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
</style>