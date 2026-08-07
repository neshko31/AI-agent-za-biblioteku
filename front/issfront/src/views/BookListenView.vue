<template>
  <div class="app-layout">
    <SidebarNav />

    <main class="main-content">
      <p v-if="!authorized" class="error-msg">Nemate pristup ovoj stranici.</p>

      <template v-else>
        <p v-if="loading">Ucitavanje audio knjige...</p>
        <p v-if="error" class="error-msg">{{ error }}</p>

        <div v-if="book" class="listen-card">
          <div class="listen-info">
            <h1>{{ book.naslov }}</h1>
            <p class="listen-author">{{ book.autor }}</p>
          </div>

          <audio
            v-if="audioUrl"
            ref="audioRef"
            class="audio-player"
            controls
            @timeupdate="updateTime"
            @loadedmetadata="applySeek"
            @ended="handleEnded"
          >
            <source :src="audioUrl" />
          </audio>

          <div class="listen-actions">
            <span class="time-info">Trenutno: {{ formatSeconds(currentSecond) }}</span>
            <button class="btn-secondary" @click="closeAndSave">Sacuvaj i zatvori</button>
          </div>
        </div>
      </template>
    </main>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import SidebarNav from '../components/Sidebar.vue'
import { useAuthStore } from '../stroage/auth.js'
import { knjigaApi } from '../services/api.js'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const isbn = computed(() => route.params.isbn)
const authorized = ref(false)
const loading = ref(false)
const error = ref('')
const book = ref(null)
const audioUrl = ref('')
const audioRef = ref(null)
const currentSecond = ref(0)
const pendingSeek = ref(null)
let lastSavedSecond = null
let lastSavedFinished = false

onMounted(() => {
  authorized.value = authStore.getRole() === 'CLAN'
  if (authorized.value) {
    loadPlayer()
  }
})

onBeforeUnmount(() => {
  if (audioUrl.value) URL.revokeObjectURL(audioUrl.value)
})

onBeforeRouteLeave(async () => {
  if (authorized.value) {
    await saveProgress()
  }
})

async function loadPlayer() {
  loading.value = true
  error.value = ''
  try {
    const [detailRes, progressRes, audioRes] = await Promise.all([
      knjigaApi.detalji(isbn.value),
      knjigaApi.slusanjeProgress(isbn.value),
      knjigaApi.audio(isbn.value)
    ])
    book.value = detailRes.data
    currentSecond.value = progressRes.data?.trenutnaSekunda || 0
    pendingSeek.value = currentSecond.value
    audioUrl.value = URL.createObjectURL(audioRes.data)
  } catch (e) {
    error.value = e.response?.data || 'Greska pri ucitavanju.'
  } finally {
    loading.value = false
  }
}

function updateTime() {
  if (!audioRef.value) return
  currentSecond.value = Math.floor(audioRef.value.currentTime || 0)
}

function applySeek() {
  if (audioRef.value && pendingSeek.value != null) {
    audioRef.value.currentTime = pendingSeek.value
    pendingSeek.value = null
  }
}

async function saveProgress(zavrseno = false) {
  const seconds = Math.max(0, Math.floor(audioRef.value?.currentTime || currentSecond.value || 0))
  if (lastSavedSecond === seconds && lastSavedFinished === zavrseno) return
  try {
    await knjigaApi.sacuvajSlusanje(isbn.value, { trenutnaSekunda: seconds, zavrseno })
    lastSavedSecond = seconds
    lastSavedFinished = zavrseno
  } catch {
    lastSavedSecond = seconds
    lastSavedFinished = zavrseno
  }
}

async function closeAndSave() {
  await saveProgress(false)
  router.push(`/knjige/${isbn.value}`)
}

async function handleEnded() {
  await saveProgress(true)
}

function formatSeconds(total) {
  const minutes = Math.floor(total / 60)
  const seconds = total % 60
  return `${minutes}:${seconds.toString().padStart(2, '0')}`
}
</script>

<style scoped>
.listen-card {
  background: white;
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: var(--shadow);
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.listen-info h1 {
  margin: 0;
  text-align: left;
  font-size: 1.8rem;
}

.listen-author {
  color: var(--text-mid);
  margin-top: 0.3rem;
}

.audio-player {
  width: 100%;
}

.listen-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.time-info {
  color: var(--text-mid);
}
</style>
