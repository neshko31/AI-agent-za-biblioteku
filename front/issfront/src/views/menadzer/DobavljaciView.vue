<template>
  <div class="dobavljaci">
    <div class="page-header">
      <div>
        <h1>Dobavljači</h1>
        <p class="subtitle"> Pregled svih dobavljača</p>
      </div>
      <RouterLink to="/menadzer/dobavljaci/novi" class="btn-primary">
        + Novi dobavljač
      </RouterLink>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="state-msg">Učitavanje...</div>

    <!-- Greška -->
    <div v-else-if="error" class="state-msg state-msg--error">{{ error }}</div>

    <!-- Prazna lista -->
    <div v-else-if="dobavljaci.length === 0" class="state-msg">
      Nema dobavljača u sistemu.
    </div>

    <!-- Tabela -->
    <div v-else class="table-wrapper">
      <table class="tabla">
        <thead>
          <tr>
            <th>Naziv</th>
            <th>Telefon</th>
            <th>Status</th>
            <!--<th>Id</th>-->
            <th>Akcije</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="d in dobavljaci" :key="d.id">
            <td>{{ d.naziv }}</td>
            <td>{{ d.tel }}</td>
            <td>
              <span class="status-badge" :class="statusKlasa(d.status)">
                {{ d.status }}
              </span>
            </td>
            <!--<td>{{d.id}}</td>-->
            <td class="akcije">
              <RouterLink :to="`/menadzer/dobavljaci/${d.id}`" class="btn-akcija btn-detalji">
                Detalji
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
import { dobavljacApi } from '../../services/api.js'

const dobavljaci = ref([])
const loading = ref(false)
const error = ref('')
const dobavljacZaBrisanje = ref(null)

async function ucitaj() {
  loading.value = true
  error.value = ''
  try {
    const res = await dobavljacApi.svi()
    dobavljaci.value = res.data
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri učitavanju dobavljača.'
  } finally {
    loading.value = false
  }
}

function potvrdiBrisanje(d) {
  dobavljacZaBrisanje.value = d
}

async function obrisi() {
  try {
    await dobavljacApi.obrisi(dobavljacZaBrisanje.value.id)
    dobavljacZaBrisanje.value = null
    await ucitaj()
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri brisanju dobavljača.'
    dobavljacZaBrisanje.value = null
  }
}

function statusKlasa(status) {
  if (status === 'AKTIVAN') return 'status--aktivan'
  if (status === 'NEAKTIVAN') return 'status--neaktivan'
  return ''
}

onMounted(ucitaj)
</script>

<style scoped>
.dobavljaci {
  width: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
}

.page-header h1 {
  margin: 0 0 0.25rem;
  font-size: 2rem;
  color: var(--text-h);
}

.subtitle {
  color: var(--text);
  font-size: 0.95rem;
  margin: 0;
}

.table-wrapper {
  border: 1px solid var(--border);
  border-radius: 12px;
  overflow: hidden;
}

.tabla {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.95rem;
}

.tabla thead {
  background: var(--accent-bg);
}

.tabla th {
  padding: 0.85rem 1.2rem;
  text-align: left;
  font-weight: 600;
  color: var(--accent);
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.tabla td {
  padding: 0.85rem 1.2rem;
  border-top: 1px solid var(--border);
  color: var(--text-h);
}

.tabla tbody tr:hover {
  background: var(--accent-bg);
}

.status-badge {
  padding: 0.2rem 0.7rem;
  border-radius: 20px;
  font-size: 0.78rem;
  font-weight: 600;
}

.status--aktivan {
  background: rgba(34, 197, 94, 0.1);
  color: #16a34a;
}

.status--neaktivan {
  background: rgba(220, 38, 38, 0.1);
  color: #dc2626;
}

.akcije {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.btn-akcija {
  padding: 0.3rem 0.8rem;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 500;
  border: none;
  cursor: pointer;
  text-decoration: none;
  font-family: inherit;
  transition: opacity 0.15s;
}

.btn-akcija:hover { opacity: 0.8; }

.btn-detalji {
  background: var(--accent-bg);
  color: var(--accent);
}


.btn-primary {
  background: var(--accent);
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 0.6rem 1.2rem;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  text-decoration: none;
  font-family: inherit;
  transition: opacity 0.15s;
}
.btn-primary:hover { opacity: 0.85; }

.btn-sekundarni {
  background: transparent;
  color: var(--text);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 0.6rem 1.2rem;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.15s;
}
.btn-sekundarni:hover { background: var(--accent-bg); }

.state-msg {
  text-align: center;
  padding: 3rem;
  color: var(--text);
}
.state-msg--error { color: #dc2626; }

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal {
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 2rem;
  max-width: 420px;
  width: 90%;
}

.modal h2 {
  margin: 0 0 0.75rem;
  color: var(--text-h);
}

.modal p {
  color: var(--text);
  margin-bottom: 1.5rem;
}

.modal-akcije {
  display: flex;
  gap: 0.75rem;
}
</style>