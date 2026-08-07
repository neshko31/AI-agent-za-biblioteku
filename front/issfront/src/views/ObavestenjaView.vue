<template>
  <div class="app-layout">
    <SidebarNav />
    <main class="main-content">
      <h1 class="page-title">Obaveštenja</h1>

      <p v-if="loading" class="loading-msg">Učitavanje...</p>

      <template v-if="!loading">

        <section v-if="vracanja.length > 0" class="notif-section">
          <h2 class="section-title">Početak naplaćivanja kazne</h2>
          <div v-for="o in vracanja" :key="o.idO" class="notif-card">
            <p class="notif-text">{{ o.tekstO }}</p>
            <p class="notif-date">{{ formatDate(o.datKreiran) }}</p>
            <button class="btn-obrisi" @click="obrisi(o.idO)">Obrišite</button>
          </div>
        </section>

        <section v-if="dostupne.length > 0" class="notif-section">
          <h2 class="section-title">Knjiga dostupna za rezervaciju</h2>
          <div v-for="o in dostupne" :key="o.idO" class="notif-card">
            <p class="notif-text">{{ o.tekstO }}</p>
            <p class="notif-date">{{ formatDate(o.datKreiran) }}</p>
            <button class="btn-obrisi" @click="obrisi(o.idO)">Obrišite</button>
          </div>
        </section>
        <section v-if="produzenja.length > 0" class="notif-section">
          <h2 class="section-title">Produženje pozajmice</h2>
          <div
            v-for="o in produzenja"
            :key="o.idO"
            class="notif-card"
            :class="o.tipO === 'PRODUZENJE_ODOBRENO' ? 'notif-odobreno' : 'notif-odbijeno'"
          >
            <p class="notif-text">{{ o.tekstO }}</p>
            <p class="notif-date">{{ formatDate(o.datKreiran) }}</p>
            <button class="btn-obrisi" @click="obrisi(o.idO)">Obrišite</button>
          </div>
        </section>


        <section v-if="ostala.length > 0" class="notif-section">
          <h2 class="section-title">Istek članarine</h2>
          <div v-for="o in ostala" :key="o.idO" class="notif-card">
            <p class="notif-text">{{ o.tekstO }}</p>
            <p class="notif-date">{{ formatDate(o.datKreiran) }}</p>
            <button class="btn-obrisi" @click="obrisi(o.idO)">Obrišite</button>
          </div>
        </section>

        <div v-if="obavestenja.length === 0" class="empty-state">
          <p>Nemate obaveštenja.</p>
        </div>

        <button v-if="obavestenja.length > 0" class="btn-obrisi-sve" @click="obrisiSva">Obrišite sve</button>
      </template>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import SidebarNav from '../components/Sidebar.vue'
import { pozajmicaApi } from '../services/api.js'
import { useAuthStore } from '../stroage/auth.js'
const authStore = useAuthStore()

const loading = ref(true)
const obavestenja = ref([])

const vracanja = computed(() => obavestenja.value.filter(o => o.tipO === 'VRACANJE'))
const dostupne = computed(() => obavestenja.value.filter(o => o.tipO === 'REZERVACIJA_DOSTUPNA'))

const produzenja = computed(() =>
  obavestenja.value.filter(o =>
    o.tipO === 'PRODUZENJE_ODOBRENO' || o.tipO === 'PRODUZENJE_ODBIJENO'
  )
)
const ostala = computed(() =>
  obavestenja.value.filter(o =>
    o.tipO !== 'VRACANJE' &&
    o.tipO !== 'REZERVACIJA_DOSTUPNA' &&
    o.tipO !== 'PRODUZENJE_ODOBRENO' &&
    o.tipO !== 'PRODUZENJE_ODBIJENO'
  )
)
onMounted(async () => {
  await loadObavestenja()
  authStore.setUnreadCount(0)
  obavestenja.value.forEach(o => o.procitano = true)
})

async function loadObavestenja() {
  loading.value = true
  try {
    const res = await pozajmicaApi.getObavestenja()
    obavestenja.value = res.data || []
  } catch {
    obavestenja.value = []
  } finally {
    loading.value = false
  }
}

async function obrisi(idO) {
  try {
    await pozajmicaApi.deleteObavestenje(idO)
    obavestenja.value = obavestenja.value.filter(o => o.idO !== idO)
  } catch {}
}

async function obrisiSva() {
  for (const o of [...obavestenja.value]) {
    await obrisi(o.idO)
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${String(d.getDate()).padStart(2,'0')}.${String(d.getMonth()+1).padStart(2,'0')}.${d.getFullYear()}.`
}
</script>

<style scoped>
.page-title { margin-bottom: 1.5rem; font-size: 1.8rem; }
.section-title { font-size: 1.05rem; font-weight: 700; margin-bottom: 0.6rem; }
.notif-section { margin-bottom: 1.5rem; }
.loading-msg, .empty-state { color: var(--text-mid); padding: 2rem 0; }

.notif-card {
  background: #f9f0ea;
  border-radius: 10px;
  padding: 0.85rem 1rem;
  margin-bottom: 0.6rem;
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  position: relative;
}
.notif-text { flex: 1; font-size: 0.9rem; color: var(--text-dark); line-height: 1.4; margin: 0; }
.notif-date { font-size: 0.78rem; color: var(--text-mid); white-space: nowrap; padding-top: 0.1rem; }
.btn-obrisi {
  background: transparent; border: 1px solid #c9a090; border-radius: 50px;
  color: #7a5c48; font-size: 0.78rem; padding: 0.3rem 0.85rem; cursor: pointer;
  white-space: nowrap; transition: background 0.15s;
}
.notif-odobreno { background: #eaf3e8; }
.notif-odbijeno { background: #f9eaea; }
.btn-obrisi:hover { background: #f0ded4; }
.btn-obrisi-sve {
  background: transparent; border: 1.5px solid #7a5c48; border-radius: 50px;
  color: #7a5c48; font-size: 0.85rem; padding: 0.5rem 1.5rem; cursor: pointer;
  margin-top: 0.5rem;
}
.btn-obrisi-sve:hover { background: #f0ded4; }
</style>
