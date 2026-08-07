<template>
  <div class="app-layout">
    <SidebarNav />
    <main class="main-content">
      <h1 class="page-title">Zahtevi za produženje pozajmice</h1>

      <p v-if="loading" class="loading-msg">Učitavanje...</p>

      <div v-if="!loading && zahtevi.length === 0" class="empty-state">
        <p>Nema zahteva koji čekaju obradu.</p>
      </div>

      <div v-if="!loading && zahtevi.length > 0" class="zahtevi-lista">
        <div v-for="z in zahtevi" :key="z.idPP" class="zahtev-card">
          <div class="zahtev-info">
            <p class="knjiga-naslov">{{ z.naslovKnjige }}</p>
            <p class="knjiga-autor">{{ z.autorKnjige }}</p>
            <p class="meta">Član: <strong>{{ z.imeClan }}</strong> ({{ z.jmbgClan }})</p>
            <p class="meta">Trenutni rok: <strong>{{ formatDate(z.stariDatVrac) }}</strong></p>
            <p class="meta">Zahtev kreiran: {{ formatDate(z.datKrePP) }}</p>
            <p class="meta novi-rok">Novi rok ako se odobri: <strong>{{ noviRok(z.stariDatVrac) }}</strong></p>
          </div>

          <div class="zahtev-akcije">
            <button class="btn-odobri" @click="obradi(z.idPP, true, '')"> Odobri</button>
            <div class="odbij-section">
              <input
                v-model="razlozi[z.idPP]"
                type="text"
                placeholder="Razlog odbijanja (opciono)"
                class="input-razlog"
              />
              <button class="btn-odbij" @click="obradi(z.idPP, false, razlozi[z.idPP] || '')"> Odbij</button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive } from 'vue'
import SidebarNav from '../components/Sidebar.vue'
import { pozajmicaApi } from '../services/api.js'

const loading = ref(true)
const zahtevi = ref([])
const razlozi = reactive({})

onMounted(async () => {
  await load()
})

async function load() {
  loading.value = true
  try {
    const res = await pozajmicaApi.produzenjaNaCekanju()
    zahtevi.value = res.data || []
  } catch {
    zahtevi.value = []
  } finally {
    loading.value = false
  }
}

async function obradi(idPP, approve, razlog) {
  try {
    await pozajmicaApi.obradiProduzenje(idPP, approve, razlog)
    zahtevi.value = zahtevi.value.filter(z => z.idPP !== idPP)
  } catch (e) {
    alert('Greška pri obradi zahteva.')
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${String(d.getDate()).padStart(2,'0')}.${String(d.getMonth()+1).padStart(2,'0')}.${d.getFullYear()}.`
}

function noviRok(stariDatVrac) {
  if (!stariDatVrac) return ''
  const d = new Date(stariDatVrac)
  d.setDate(d.getDate() + 14)
  return `${String(d.getDate()).padStart(2,'0')}.${String(d.getMonth()+1).padStart(2,'0')}.${d.getFullYear()}.`
}
</script>

<style scoped>
.page-title { margin-bottom: 1.5rem; font-size: 1.8rem; }
.loading-msg, .empty-state { color: var(--text-mid); padding: 2rem 0; }

.zahtevi-lista { display: flex; flex-direction: column; gap: 1rem; }

.zahtev-card {
  background: #f9f0ea;
  border-radius: 12px;
  padding: 1.2rem 1.4rem;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 2rem;
  flex-wrap: wrap;
}

.knjiga-naslov { font-weight: 700; font-size: 1rem; margin: 0 0 0.2rem; }
.knjiga-autor { color: var(--text-mid); font-size: 0.88rem; margin: 0 0 0.5rem; }
.meta { font-size: 0.87rem; margin: 0.15rem 0; color: var(--text-dark); }
.novi-rok { color: #5a7a4a; }

.zahtev-akcije {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  min-width: 220px;
}

.btn-odobri {
  background: #5a7a4a; color: white; border: none;
  border-radius: 8px; padding: 0.55rem 1.2rem;
  font-size: 0.9rem; cursor: pointer; font-weight: 600;
}
.btn-odobri:hover { background: #4a6a3a; }

.odbij-section { display: flex; gap: 0.5rem; }

.input-razlog {
  flex: 1; border: 1px solid #c9a090; border-radius: 8px;
  padding: 0.45rem 0.7rem; font-size: 0.85rem;
}

.btn-odbij {
  background: transparent; border: 1.5px solid #c9a090;
  color: #7a5c48; border-radius: 8px;
  padding: 0.45rem 0.9rem; font-size: 0.88rem;
  cursor: pointer; white-space: nowrap;
}
.btn-odbij:hover { background: #f0ded4; }
</style>