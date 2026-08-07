<template>
  <div class="page-center">
    <div class="card reg-card">
      <h1>Odaberite kategoriju</h1>

      <div class="step2-body">
        <div class="category-list">
          <div
            v-for="kat in kategorije"
            :key="kat.idkc"
            class="radio-option"
            @click="selected = kat.idkc"
          >
            <div class="radio-circle" :class="{ active: selected === kat.idkc }"></div>
            <span class="radio-label">{{ labelOf(kat.tipKC) }}</span>
          </div>
        </div>

        <div class="doc-panel">
          <p class="doc-title">Neophodna dokumentacija za odabranu kategoriju:</p>
          <div class="doc-box">
            <span class="doc-text">{{ docText }}</span>
          </div>
          <div class="upload-section">
              <label class="upload-btn">
                Dodaj dokument
                <input type="file" @change="handleFile" hidden />
              </label>

              <p v-if="fileName" class="file-name">
                {{ fileName }}
              </p>
          </div>
        </div>
      </div>

      <p v-if="error" class="error-msg">{{ error }}</p>

      <button class="btn-primary" @click="handleNext" :disabled="loading || !selected">
        {{ loading ? 'Učitavanje…' : 'Nastavite' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stroage/auth.js'
import { authApi, publicApi } from '../services/api.js'

const router    = useRouter()
const authStore = useAuthStore()

const kategorije = ref([])
const selected   = ref(null)
const error      = ref('')
const loading    = ref(false)
const fileName = ref('')
const file = ref(null)

onMounted(async () => {
  const res = await publicApi.getKategorije()
  kategorije.value = res.data
  if (res.data.length) selected.value = res.data[0].idkc
})

const LABELS = {
  REGULARNA: 'Regularna - 600.00', DECIJA: 'Dečija - 300.00',
  STUDENTSKA: 'Studentska - 400.00', PENZIONERSKA: 'Penzionerska - 350.00', PORODICNA: 'Porodična - 900.00'
}
const DOCS = {
  REGULARNA:    'Za izabranu kategoriju nije potrebno priložiti nikakvu dokumentaciju',
  DECIJA:       'Potrebno je priložiti kopiju rodnog lista',
  STUDENTSKA:   'Potrebno je priložiti važeću studentsku legitimaciju',
  PENZIONERSKA: 'Potrebno je priložiti kopiju penzionerske kartice',
  PORODICNA:    'Potrebno je priložiti izvod iz matične knjige venčanih i rodni listi dece',
}
function handleFile(e) {
  const f = e.target.files[0]
  if (f) {
    file.value = f
    fileName.value = f.name
  }
}

const labelOf = (tip) => LABELS[tip] || tip

const docText = computed(() => {
  const kat = kategorije.value.find(k => k.idkc === selected.value)
  return kat ? (DOCS[kat.tipKC] || '') : ''
})

async function handleNext() {
  error.value = ''
  const kat = kategorije.value.find(k => k.idkc === selected.value)
  if (kat && kat.tipKC !== 'REGULARNA' && !file.value) {
      error.value = 'Morate priložiti dokument za izabranu kategoriju'
      return
    }
  loading.value = true
  try {
    await authApi.registerStep2(authStore.regJmbg, { kategorijaClanaId: selected.value })
    router.push('/register/step3')
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.reg-card { width: 100%; max-width: 700px; }
.step2-body {
  display: grid;
  grid-template-columns: 1fr 1.4fr;
  gap: 2rem;
  align-items: start;
}
.doc-title { font-size: 0.9rem; color: var(--text-mid); margin-bottom: 0.5rem; }
.doc-box {
  background: white;
  border-radius: 8px;
  padding: 1rem;
  min-height: 180px;
  font-size: 0.9rem;
  color: var(--text-mid);
}

.upload-section {
  margin-top: 1rem;
}

.upload-btn {
  display: inline-block;
  background: var(--btn-primary);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
}

.upload-btn:hover {
  opacity: 0.9;
}

.file-name {
  margin-top: 0.5rem;
  font-size: 0.85rem;
  color: var(--text-mid);
}
</style>
