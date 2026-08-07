<template>
  <div class="app-layout">
    <SidebarNav />
    <main class="main-content">
      <h1 class="page-title">Vraćanje knjiga</h1>


      <p v-if="loading" class="status-msg">Učitavanje...</p>
      <p v-if="!loading && filtrirane.length === 0" class="status-msg">
        {{ pretraga ? 'Nema rezultata za pretragu.' : 'Nema aktivnih pozajmica.' }}
      </p>

      <div v-if="!loading && filtrirane.length > 0" class="tabla-wrapper">
        <table class="pozajmice-tabla">
          <thead>
            <tr>
              <th>Knjiga</th>
              <th>Autor</th>
              <th>Član</th>
              <th>Datum pozajmice</th>
              <th>Rok vraćanja</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="p in filtrirane"
              :key="p.idP"
              :class="{ 'red-overdue': isOverdue(p.datOcVrac) }"
            >
              <td class="cell-naslov">{{ p.naslovKnjige }}</td>
              <td>{{ p.autorKnjige }}</td>
              <td>
                <span class="ime-clan">{{ p.imeClan }}</span>
                <span class="jmbg-clan">{{ p.jmbgClan }}</span>
              </td>
              <td>{{ formatDate(p.datPoz) }}</td>
              <td :class="{ 'datum-prekoracen': isOverdue(p.datOcVrac) }">
                {{ formatDate(p.datOcVrac) }}
                <span v-if="isOverdue(p.datOcVrac)" class="badge-kasni">Kasni</span>
              </td>
              <td>
                <span class="badge-aktivna">Aktivna</span>
              </td>
              <td>
                <button
                  class="btn-vrati"
                  :disabled="vracanje[p.idP]"
                  @click="vratiKnjigu(p)"
                >
                  {{ vracanje[p.idP] ? '...' : 'Vrati knjigu' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref, computed, reactive } from 'vue'
import SidebarNav from '../components/Sidebar.vue'
import { pozajmicaApi } from '../services/api.js'

const loading = ref(true)
const pozajmice = ref([])
const pretraga = ref('')
const vracanje = reactive({})

onMounted(async () => {
  await load()
})

async function load() {
  loading.value = true
  try {
    const res = await pozajmicaApi.sveAktivne()
    pozajmice.value = res.data || []
  } catch {
    pozajmice.value = []
  } finally {
    loading.value = false
  }
}

const filtrirane = computed(() => {
  const q = pretraga.value.toLowerCase().trim()
  if (!q) return pozajmice.value
  return pozajmice.value.filter(p =>
    p.naslovKnjige?.toLowerCase().includes(q) ||
    p.autorKnjige?.toLowerCase().includes(q) ||
    p.imeClan?.toLowerCase().includes(q) ||
    p.jmbgClan?.includes(q)
  )
})

async function vratiKnjigu(p) {
  vracanje[p.idP] = true
  try {
    await pozajmicaApi.vratiKnjigu(p.idP)
    pozajmice.value = pozajmice.value.filter(x => x.idP !== p.idP)
  } catch {
    alert('Greška pri vraćanju knjige.')
    vracanje[p.idP] = false
  }
}

function isOverdue(datOcVrac) {
  return datOcVrac && new Date(datOcVrac) < new Date(new Date().toDateString())
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${String(d.getDate()).padStart(2,'0')}.${String(d.getMonth()+1).padStart(2,'0')}.${d.getFullYear()}.`
}
</script>

<style scoped>
.page-title { margin-bottom: 1.2rem; font-size: 1.8rem; }
.status-msg { color: var(--text-mid); padding: 2rem 0; }


.tabla-wrapper { overflow-x: auto; }

.pozajmice-tabla {
  width: 100%; border-collapse: collapse; font-size: 0.9rem;
}
.pozajmice-tabla th {
  text-align: left; padding: 0.7rem 1rem;
  background: #f5ede8; font-weight: 600;
  border-bottom: 2px solid #e0c9be;
}
.pozajmice-tabla td {
  padding: 0.7rem 1rem;
  border-bottom: 1px solid #ede4df;
  vertical-align: middle;
}
.pozajmice-tabla tr:hover td { background: #fdf7f4; }

.red-overdue td { background: #fff2f0; }
.red-overdue:hover td { background: #ffe8e5; }

.cell-naslov { font-weight: 600; }

.ime-clan { display: block; font-weight: 500; }
.jmbg-clan { display: block; font-size: 0.8rem; color: var(--text-mid); }

.datum-prekoracen { color: #c0392b; font-weight: 600; }

.badge-kasni {
  display: inline-block; margin-left: 0.4rem;
  background: #c0392b; color: white;
  font-size: 0.72rem; font-weight: 700;
  padding: 0.1rem 0.45rem; border-radius: 4px;
}
.badge-aktivna {
  background: #eaf3e8; color: #4a7a3a;
  font-size: 0.78rem; font-weight: 600;
  padding: 0.2rem 0.55rem; border-radius: 6px;
}

.btn-vrati {
  background: #7a5c48; color: white; border: none;
  border-radius: 8px; padding: 0.45rem 1rem;
  font-size: 0.87rem; font-weight: 600;
  cursor: pointer; white-space: nowrap;
}
.btn-vrati:hover:not(:disabled) { background: #5c3d2a; }
.btn-vrati:disabled { opacity: 0.5; cursor: default; }
</style>