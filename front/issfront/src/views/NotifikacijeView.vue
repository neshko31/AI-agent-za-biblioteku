<template>
  <div class="app-layout">
    <SidebarNav />

    <main class="main-content">
      <div class="notifikacije-wrapper">
        
        <!-- Zaglavlje stranice postavljeno skroz levo -->
        <div class="page-header">
          <div class="header-tekst">
            <h1>Obaveštenja</h1>
            <p class="subtitle">Vaš lični centar za praćenje statusa predloga</p>
          </div>
        </div>

        <!-- Stanja učitavanja, greške ili prazne liste -->
        <div v-if="loading" class="state-msg">
          <div class="spinner"></div>
          <p>Učitavanje obaveštenja...</p>
        </div>
        <div v-else-if="error" class="state-msg state-msg--error">
          <span class="state-icon">❌</span>
          <p>{{ error }}</p>
        </div>
        <div v-else-if="notifikacije.length === 0" class="state-msg state-msg--empty">
          <span class="state-icon">🔔</span>
          <p>Nemate novih obaveštenja.</p>
        </div>

        <!-- Lista obaveštenja -->
        <div v-else class="lista animated-fade-in">
          <div
            v-for="n in notifikacije"
            :key="n.id"
            class="notifikacija-kartica"
            :class="{ 'notifikacija--neprocitana': !n.procitana }"
            @click="oznаciKaoProcitanu(n)"
          >
            <div class="notifikacija-header">
              <div class="header-levo">
                <span v-if="!n.procitana" class="pulsirajuci-krug"></span>
                <span v-if="!n.procitana" class="badge-novo">Novo</span>
              </div>
              <span class="datum">
                <span class="sat-ikonica">🕒</span> {{ formatirajDatum(n.datum) }}
              </span>
            </div>
            <p class="poruka">{{ n.poruka }}</p>
          </div>
        </div>

      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { notifikacijaApi } from '../services/api.js'
import SidebarNav from '../components/Sidebar.vue'

const notifikacije = ref([])
const loading = ref(false)
const error = ref('')

async function ucitaj() {
  loading.value = true
  error.value = ''
  try {
    const res = await notifikacijaApi.mojeNotifikacije()
    notifikacije.value = res.data
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri učitavanju obaveštenja.'
  } finally {
    loading.value = false
  }
}

async function oznаciKaoProcitanu(n) {
  if (n.procitana) return
  try {
    await notifikacijaApi.oznаciKaoProcitanu(n.id)
    n.procitana = true
  } catch (e) {
    // tiho
  }
}

function formatirajDatum(datum) {
  if (!datum) return ''
  const d = new Date(datum)
  return d.toLocaleDateString('sr-RS', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

onMounted(ucitaj)
</script>

<style scoped>
.notifikacije-wrapper { 
  width: 100%; 
  max-width: 100%;
  box-sizing: border-box;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* Izmenjen header za levo poravnanje */
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
  color: #3f4e37; /* Isti zeleni naslov */
}
.subtitle { 
  color: #556644; 
  font-size: 1rem; 
  margin: 0;
  opacity: 0.85;
}

.lista { 
  display: flex; 
  flex-direction: column; 
  gap: 1.25rem; /* Malo veći razmak u stilu kartica sa slike */
  width: 100%;
}

/* USKLAĐENO: Svaka pojedinačna kartica je bela i zaobljena kao na slici */
.notifikacija-kartica {
  background: #ffffff !important;
  border: none;
  border-radius: 20px; /* Identičan visoki radijus sa slike */
  padding: 1.35rem 1.75rem; 
  cursor: pointer;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.05); /* Nežna senka kao kod baza podataka */
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.notifikacija-kartica:hover { 
  transform: translateY(-3px); /* Elegantan skok na hover */
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

/* Specifičan stil za nepročitanu - koristi blagu nijansu iz palete aplikacije */
.notifikacija--neprocitana {
  background: #fbfcf9 !important; /* Suptilni zelenkasti odsjaj bele boje */
}
.notifikacija--neprocitana::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 5px;
  background: #7a8f6e; /* Glavna boja sa slike za fokus */
}

.notifikacija-header {
  display: flex; 
  justify-content: space-between;
  align-items: center; 
  margin-bottom: 0.6rem;
}
.header-levo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.badge-novo {
  background: #7a8f6e; 
  color: #fff;
  font-size: 0.75rem; 
  font-weight: 700;
  padding: 0.2rem 0.6rem; 
  border-radius: 50px;
}
.pulsirajuci-krug {
  width: 8px;
  height: 8px;
  background-color: #7a8f6e;
  border-radius: 50%;
  display: inline-block;
}

.datum { 
  font-size: 0.85rem; 
  color: #666666; 
  display: flex;
  align-items: center;
  gap: 0.35rem;
}

.poruka { 
  color: #333333; 
  font-size: 1rem; 
  margin: 0; 
  line-height: 1.5;
}
.notifikacija--neprocitana .poruka {
  font-weight: 600;
  color: #3f4e37;
}

/* Stanja i Loader */
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
}
.spinner {
  width: 30px; height: 30px;
  border: 3px solid #e2e8f0;
  border-top-color: #7a8f6e;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.animated-fade-in { animation: fadeIn 0.35s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
</style>