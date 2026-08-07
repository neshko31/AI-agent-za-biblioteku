<template>
  <div class="app-layout">
    <SidebarNav />

    <main class="main-content">
      <div class="db-toolbar">
        <div>
          <h1 class="db-title">Dostupne baze podataka</h1>
          <p class="db-sub">Pretražite baze podataka po nazivu.</p>
        </div>
        <div class="db-actions">
          <button v-if="isLibrarian" class="btn-secondary" @click="openCreate">
            Dodaj novu bazu podataka
          </button>
          <div class="db-search">
            <input
              v-model="query"
              class="search-input"
              type="search"
              placeholder="Unesite naziv baze"
            />
            <button class="btn-secondary" @click="runSearch">Pretraži</button>
          </div>
        </div>
      </div>

      <p v-if="!authorized" class="error-msg">Nemate pristup ovoj stranici.</p>

      <template v-else>
        <p v-if="loading">Učitavanje baza...</p>
        <p v-if="error" class="error-msg">{{ error }}</p>

        <div v-if="baze.length" class="db-list">
          <article v-for="baza in baze" :key="baza.id" class="db-card">
            <h3>{{ baza.naziv }}</h3>
            <p class="db-meta"><strong>Oblast istrazivanja:</strong> {{ baza.oblast }}</p>
            <p class="db-meta"><strong>Licenca:</strong> {{ baza.licenca }}</p>
            <p class="db-desc"><strong>Opis:</strong> {{ baza.opis }}</p>

            <div class="db-actions-row">
              <button
                v-if="isClan"
                class="btn-secondary"
                :disabled="downloadingId === baza.id"
                @click="downloadBaza(baza)"
              >
                {{ downloadingId === baza.id ? 'Preuzimanje...' : 'Preuzmi' }}
              </button>
              <button v-if="isLibrarian" class="btn-secondary" @click="openEdit(baza.id)">
                Izmeni podatke o elektronskoj bazi
              </button>
            </div>
          </article>
        </div>

        <p v-else-if="!loading" class="empty-state">Nema rezultata za prikaz.</p>
      </template>
    </main>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import SidebarNav from '../components/Sidebar.vue'
import { useAuthStore } from '../stroage/auth.js'
import { bazePodatakaApi } from '../services/api.js'

const authStore = useAuthStore()
const router = useRouter()

const authorized = ref(false)
const baze = ref([])
const loading = ref(false)
const error = ref('')
const query = ref('')
const downloadingId = ref(null)
let searchTimer = null

const isLibrarian = computed(() => authStore.getRole() === 'BIBLIOTEKAR')
const isClan = computed(() => authStore.getRole() === 'CLAN')

onMounted(() => {
  const role = authStore.getRole()
  authorized.value = role === 'CLAN' || role === 'BIBLIOTEKAR' || role === 'ADMINISTRATOR'
  if (authorized.value) {
    loadBaze()
  }
})

onBeforeUnmount(() => {
  if (searchTimer) clearTimeout(searchTimer)
})

watch(query, () => {
  if (!authorized.value) return
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    runSearch()
  }, 300)
})

async function loadBaze(searchValue) {
  loading.value = true
  error.value = ''
  try {
    const res = searchValue
      ? await bazePodatakaApi.pretraga(searchValue)
      : await bazePodatakaApi.sveOsnovno()
    baze.value = res.data || []
  } catch (e) {
    error.value = e.response?.data || 'Greska pri ucitavanju.'
  } finally {
    loading.value = false
  }
}

function runSearch() {
  const trimmed = query.value.trim()
  loadBaze(trimmed || null)
}

function openCreate() {
  router.push('/baze-podataka/nova')
}

function openEdit(id) {
  router.push(`/baze-podataka/${id}/izmena`)
}

function buildDownloadName(baza) {
  const base = (baza?.naziv || 'baza').trim().replace(/\s+/g, '_')
  return `${base}.zip`
}

async function downloadBaza(baza) {
  if (!isClan.value) return
  downloadingId.value = baza.id
  error.value = ''
  try {
    const res = await bazePodatakaApi.preuzmi(baza.id)
    const url = URL.createObjectURL(res.data)
    const link = document.createElement('a')
    link.href = url
    link.download = buildDownloadName(baza)
    document.body.appendChild(link)
    link.click()
    link.remove()
    setTimeout(() => URL.revokeObjectURL(url), 0)
  } catch (e) {
    error.value = e.response?.data || 'Greska pri preuzimanju.'
  } finally {
    downloadingId.value = null
  }
}
</script>

<style scoped>
.db-toolbar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 1.5rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.db-title {
  font-size: 2rem;
  margin: 0;
  text-align: left;
}

.db-sub {
  margin-top: 0.4rem;
  color: var(--text-mid);
}

.db-actions {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  flex-wrap: wrap;
}

.db-search {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.search-input {
  min-width: 240px;
  background: var(--input-bg);
  border: 1.5px solid var(--border);
  border-radius: 999px;
  padding: 0.55rem 1rem;
  font-size: 0.95rem;
}

.db-list {
  display: grid;
  gap: 1.5rem;
}

.db-card {
  background: white;
  border-radius: 16px;
  box-shadow: var(--shadow);
  padding: 1.3rem 1.5rem;
  text-align: left;
}

.db-card h3 {
  margin: 0 0 0.5rem;
  font-size: 1.25rem;
  color: var(--text-dark);
}

.db-meta {
  margin-bottom: 0.35rem;
  color: var(--text-mid);
}

.db-desc {
  margin: 0.6rem 0 0.9rem;
  color: var(--text-mid);
  line-height: 1.5;
}

.db-actions-row {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.empty-state {
  margin-top: 2rem;
  color: var(--text-mid);
}

@media (max-width: 768px) {
  .db-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .db-search {
    width: 100%;
  }

  .db-actions {
    width: 100%;
  }

  .search-input {
    flex: 1;
  }
}
</style>
