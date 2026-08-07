<template>
  <div class="app-layout">
    <SidebarNav />

    <main class="main-content">
      <div class="page-toolbar">
        <div>
          <h1 class="page-title">Katalozi</h1>
          <p class="page-sub">Izaberite katalog da biste videli knjige.</p>
        </div>
        <RouterLink to="/katalog/novi" class="btn-add" title="Novi katalog">+</RouterLink>
      </div>

      <p v-if="!authorized" class="error-msg">Nemate pristup ovoj stranici.</p>

      <template v-else>
        <p v-if="loading">Učitavanje kataloga...</p>
        <p v-if="error" class="error-msg">{{ error }}</p>

        <div v-if="catalogs.length" class="catalog-grid">
          <div
            v-for="kat in catalogs"
            :key="getKatId(kat)"
            class="catalog-card"
            @click="openCatalog(getKatId(kat))"
          >
            <div class="catalog-icon">📚</div>
            <div class="catalog-info">
              <h3>{{ kat.katIme }}</h3>
              <p>{{ kat.standard }}</p>
            </div>
            <button
              class="edit-btn"
              title="Izmeni katalog"
              @click.stop="openEdit(kat)"
            >✏️</button>
            <button
              class="delete-btn"
              title="Obriši katalog"
              @click.stop="confirmDelete(kat)"
            >🗑</button>
          </div>
        </div>

        <p v-else-if="!loading" class="empty-state">Nema kataloga za prikaz.</p>
      </template>
    </main>

    <!-- Confirm delete dialog -->
    <div v-if="pendingDelete" class="overlay" @click.self="pendingDelete = null">
      <div class="dialog">
        <p>Obrisati katalog <strong>{{ pendingDelete.katIme }}</strong>?</p>
        <div class="dialog-actions">
          <button class="btn-secondary" @click="pendingDelete = null">Odustani</button>
          <button class="btn-danger" @click="doDelete">Obriši</button>
        </div>
      </div>
    </div>

    <!-- Edit dialog -->
    <div v-if="pendingEdit" class="overlay" @click.self="pendingEdit = null">
      <div class="dialog">
        <h3 class="dialog-title">Izmeni katalog</h3>
        <div class="form-group">
          <label>Naziv</label>
          <input v-model="editForm.naziv" type="text" class="form-input" placeholder="Naziv kataloga" />
        </div>
        <div class="form-group">
          <label>Standard</label>
          <input v-model="editForm.standard" type="text" class="form-input" placeholder="Standard" />
        </div>
        <p v-if="editError" class="error-msg">{{ editError }}</p>
        <div class="dialog-actions">
          <button class="btn-secondary" @click="pendingEdit = null">Odustani</button>
          <button class="btn-secondary" @click="doEdit">Sačuvaj</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import SidebarNav from '../components/Sidebar.vue'
import { useAuthStore } from '../stroage/auth.js'

const auth = useAuthStore()
const router = useRouter()

const authorized = ref(false)
const catalogs = ref([])
const loading = ref(false)
const error = ref('')
const pendingDelete = ref(null)
const pendingEdit = ref(null)
const editForm = ref({ naziv: '', standard: '' })
const editError = ref('')

onMounted(() => {
  authorized.value = auth.getRole() === 'BIBLIOTEKAR' || auth.getRole() === 'ADMINISTRATOR'
  error.value = ''
  if (authorized.value) loadCatalogs()
})

async function loadCatalogs() {
  loading.value = true
  error.value = ''
  try {
    const res = await axios.get('http://localhost:8080/api/katalog/all', {
      headers: { Authorization: `Bearer ${auth.token}` }
    })
    catalogs.value = (res.data || []).filter(k => !k.deleted)
  } catch (e) {
    error.value = e.response?.data || 'Greška pri učitavanju.'
  } finally {
    loading.value = false
  }
}

function getKatId(kat) {
  return kat.katId
}

function openCatalog(id) {
  router.push(`/katalog/${id}`)
}

function confirmDelete(kat) {
  pendingDelete.value = kat
}

function openEdit(kat) {
  pendingEdit.value = kat
  editForm.value = { naziv: kat.katIme, standard: kat.standard }
  editError.value = ''
}

async function doDelete() {
  const kat = pendingDelete.value
  pendingDelete.value = null
  try {
    const id = getKatId(kat)
    await axios.put(`http://localhost:8080/api/katalog/delete/${id}`, {}, {
      headers: { Authorization: `Bearer ${auth.token}` }
    })
    catalogs.value = catalogs.value.filter(k => getKatId(k) !== id)
  } catch (e) {
    error.value = e.response?.data || 'Greška pri brisanju.'
  }
}

async function doEdit() {
  editError.value = ''
  const kat = pendingEdit.value
  const id = getKatId(kat)
  try {
    await axios.put(`http://localhost:8080/api/katalog/${id}`, editForm.value, {
      headers: { Authorization: `Bearer ${auth.token}` }
    })
    const target = catalogs.value.find(k => getKatId(k) === id)
    if (target) {
      target.katIme = editForm.value.naziv
      target.standard = editForm.value.standard
    }
    pendingEdit.value = null
  } catch (e) {
    editError.value = e.response?.data || 'Greška pri izmeni.'
  }
}
</script>

<style scoped>
.page-toolbar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 1.5rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.page-title {
  font-size: 2rem;
  margin: 0;
  text-align: left;
}

.page-sub {
  margin-top: 0.4rem;
  color: var(--text-mid);
}

.btn-add {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.4rem;
  height: 2.4rem;
  border-radius: 999px;
  background: var(--accent);
  color: white;
  font-size: 1.5rem;
  line-height: 1;
  text-decoration: none;
  transition: opacity 0.2s;
  flex-shrink: 0;
}

.btn-add:hover { opacity: 0.85; }

/* Grid */
.catalog-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 1.25rem;
}

.catalog-card {
  background: white;
  border-radius: 16px;
  box-shadow: var(--shadow);
  padding: 1.25rem 1.25rem 1.25rem 1.1rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
}

.catalog-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 28px rgba(30, 45, 20, 0.15);
}

.catalog-icon {
  font-size: 1.8rem;
  flex-shrink: 0;
}

.catalog-info {
  flex: 1;
  text-align: left;
  min-width: 0;
}

.catalog-info h3 {
  font-size: 1rem;
  margin: 0 0 0.2rem;
  color: var(--text-dark);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.catalog-info p {
  font-size: 0.85rem;
  color: var(--text-mid);
  margin: 0;
}

.edit-btn,
.delete-btn {
  background: transparent;
  border: none;
  font-size: 1rem;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
  padding: 0.3rem;
  border-radius: 6px;
  flex-shrink: 0;
}

.catalog-card:hover .edit-btn,
.catalog-card:hover .delete-btn { opacity: 1; }

.edit-btn:hover { background: rgba(49, 130, 206, 0.1); }
.delete-btn:hover { background: rgba(229, 62, 62, 0.1); }

/* Confirm / Edit dialog */
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.dialog {
  background: white;
  border-radius: 16px;
  padding: 1.75rem 2rem;
  box-shadow: var(--shadow);
  max-width: 360px;
  width: 90%;
  text-align: center;
}

.dialog-title {
  font-size: 1.1rem;
  margin: 0 0 1.25rem;
  color: var(--text-dark);
}

.dialog p {
  font-size: 1rem;
  margin-bottom: 1.5rem;
  color: var(--text-dark);
}

.form-group {
  display: flex;
  flex-direction: column;
  text-align: left;
  gap: 0.35rem;
  margin-bottom: 1rem;
}

.form-group label {
  font-size: 0.85rem;
  color: var(--text-mid);
  font-weight: 500;
}

.form-input {
  border: 1.5px solid var(--border, #ddd);
  border-radius: 8px;
  padding: 0.5rem 0.75rem;
  font-size: 0.95rem;
  outline: none;
  transition: border-color 0.2s;
}

.form-input:focus { border-color: var(--accent); }

.dialog-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: center;
  margin-top: 1.25rem;
}

.btn-secondary {
  background: transparent;
  border: 1.5px solid var(--border);
  border-radius: 999px;
  padding: 0.5rem 1.25rem;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-secondary:hover { background: var(--accent-bg); }

.btn-primary {
  background: #38a169;
  color: white;
  border: 1.5px solid transparent;
  border-radius: 999px;
  padding: 0.5rem 1.25rem;
  font-size: 0.9rem;
  cursor: pointer;
  transition: opacity 0.2s;
}

.btn-primary:hover { opacity: 0.85; }

.btn-danger {
  background: #e53e3e;
  color: white;
  border: none;
  border-radius: 999px;
  padding: 0.5rem 1.25rem;
  font-size: 0.9rem;
  cursor: pointer;
  transition: opacity 0.2s;
}

.btn-danger:hover { opacity: 0.85; }

.error-msg { color: #e53e3e; font-size: 0.9rem; }
.empty-state { margin-top: 2rem; color: var(--text-mid); }
</style>
