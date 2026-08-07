<template>
  <div class="app-layout">
    <SidebarNav />

    <main class="main-content">
      <div class="page-toolbar">
        <div>
          <h1 class="page-title">Novi katalog</h1>
          <p class="page-sub">Popunite polja da biste dodali novi katalog.</p>
        </div>
      </div>

      <p v-if="!authorized" class="error-msg">Nemate pristup ovoj stranici.</p>

      <template v-else>
        <div class="form-card">
          <div class="form-fields">
            <div class="field-group">
              <label class="field-label">Naziv</label>
              <input
                v-model="naziv"
                type="text"
                placeholder="Naziv kataloga"
                class="field-input"
              />
            </div>

            <div class="field-group">
              <label class="field-label">Standard</label>
              <input
                v-model="standard"
                type="text"
                placeholder="npr. MARC"
                class="field-input"
              />
            </div>

            <p v-if="error" class="error-msg">{{ error }}</p>
            <p v-if="success" class="success-msg">{{ success }}</p>

            <button class="btn-primary" @click="submit">
              <span class="btn-icon">+</span> Dodaj katalog
            </button>
          </div>
        </div>
      </template>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useAuthStore } from '../stroage/auth.js'
import SidebarNav from '../components/Sidebar.vue'

const auth = useAuthStore()
const naziv = ref('')
const standard = ref('')
const error = ref('')
const success = ref('')
const authorized = ref(false)

onMounted(() => {
  // Role check — expand this later for role-based sidebar variants
  authorized.value = auth.getRole() === 'BIBLIOTEKAR'
})

async function submit() {
  error.value = ''
  success.value = ''
  if (!naziv.value || !standard.value) {
    error.value = 'Sva polja su obavezna.'
    return
  }
  try {
    await axios.post('http://localhost:8080/api/katalog/novi', {
      naziv: naziv.value,
      standard: standard.value
    }, {
      headers: { Authorization: `Bearer ${auth.token}` }
    })
    success.value = 'Katalog uspešno dodat.'
    naziv.value = ''
    standard.value = ''
  } catch (e) {
    error.value = e.response?.data || 'Došlo je do greške.'
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

.form-card {
  background: white;
  border-radius: 16px;
  box-shadow: var(--shadow);
  padding: 2rem;
  max-width: 480px;
}

.form-fields {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.field-label {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--text-dark);
}

.field-input {
  width: 100%;
  padding: 0.55rem 1rem;
  border: 1.5px solid var(--border);
  border-radius: 999px;
  background: var(--input-bg);
  color: var(--text-h);
  font-size: 0.95rem;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

.field-input:focus {
  outline: none;
  border-color: var(--accent);
}

.btn-primary {
  align-self: stretch;
  background: #green;
  color: #ffffff;
  border: none;
  border-radius: 999px;
  padding: 0.85rem 1.5rem;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s, transform 0.1s, box-shadow 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  letter-spacing: 0.3px;
  margin-top: 0.5rem;
}

.btn-primary:hover {
  background: #5a18a8;
  box-shadow: 0 6px 20px rgba(106, 31, 191, 0.55);
  transform: translateY(-1px);
}

.btn-primary:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(106, 31, 191, 0.4);
}

.btn-icon {
  font-size: 1.3rem;
  line-height: 1;
  font-weight: 400;
}

.error-msg {
  color: #e53e3e;
  font-size: 0.9rem;
}

.success-msg {
  color: #38a169;
  font-size: 0.9rem;
}
</style>
