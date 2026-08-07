<template>
  <div class="app-layout">
    <SidebarNav />

    <main class="main-content">
      <h1 class="page-title">Početna</h1>


      <section v-if="aktivne.length > 0 || aktivneDigitalne.length>0">
        <h2 class="section-title">Pozajmljeno</h2>

        <div class="format-row">

          <div v-if="aktivne.length > 0">
            <p class="format-label">fizička forma</p>
            <div class="book-row">
              <article
                v-for="p in aktivne"
                :key="p.idP"
                class="book-card"
                @click="$router.push(`/knjige/${p.isbn}`)"
              >
                <div class="book-cover">
                  <img v-if="coverUrls[p.isbn]" :src="coverUrls[p.isbn]" alt="" />
                  <div v-else class="cover-placeholder"></div>
                </div>
                <p class="book-title">{{ p.naslovKnjige }}</p>
                <p class="book-author">{{ p.autorKnjige }}</p>
              </article>
            </div>
          </div>

          <div v-if="aktivneDigitalne.length > 0">
            <p class="format-label">digitalne knjige</p>
            <div class="book-row">
              <article
                v-for="p in aktivneDigitalne"
                :key="p.isbn + p._tip"
                class="book-card"
                @click="p._tip === 'eknjiga' ? $router.push(`/knjige/${p.isbn}/citaj`) : $router.push(`/knjige/${p.isbn}/slusaj`)"
              >
                <div class="book-cover">
                  <img v-if="coverUrls[p.isbn]" :src="coverUrls[p.isbn]" alt="" />
                  <div v-else class="cover-placeholder"></div>
                  <span class="digital-badge">{{ p._tip === 'eknjiga' ? '📖' : '🎧' }}</span>
                </div>
                <p class="book-title">{{ p.naslovKnjige }}</p>
                <p class="book-author">{{ p.autorKnjige }}</p>
              </article>
            </div>
          </div>

        </div>
      </section>


      <section class="mt-section">
        <h2 class="section-title">
          Preporučujemo
          <button class="btn-refresh" @click="refreshPreporuke" :disabled="loadingRec">
            Osvežite preporuke
          </button>
        </h2>

        <p v-if="loadingRec" class="loading-msg">Učitavanje preporuka...</p>

        <div v-else class="book-row">
          <article
            v-for="book in preporuke"
            :key="book.isbn"
            class="book-card"
            @click="$router.push(`/knjige/${book.isbn}`)"
          >
            <div class="book-cover">
              <img v-if="coverUrls[book.isbn]" :src="coverUrls[book.isbn]" alt="" />
              <div v-else class="cover-placeholder"></div>
            </div>
            <p class="book-title">{{ book.naslov }}</p>
            <p class="book-author">{{ book.autor }}</p>
          </article>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import SidebarNav from '../components/Sidebar.vue'
import { knjigaApi, pozajmicaApi } from '../services/api.js'

const aktivne = ref([])
const aktivneDigitalne = ref([])
const preporuke = ref([])
const coverUrls = ref({})
const loadingRec = ref(false)

onMounted(async () => {
  await Promise.all([loadPozajmice(), loadPreporuke()])
})

async function loadPozajmice() {
  try {
    const res = await pozajmicaApi.getMoje()
    aktivne.value = res.data.aktivnePozajmice || []
    const eKnjige = (res.data.aktivneEKnjige || []).map(p => ({ ...p, _tip: 'eknjiga' }))
        const audio = (res.data.aktivneAudioKnjige || []).map(p => ({ ...p, _tip: 'audio' }))
        aktivneDigitalne.value = [...eKnjige, ...audio]
        const allIsbns = [
          ...aktivne.value.map(p => p.isbn),
          ...aktivneDigitalne.value.map(p => p.isbn)
        ]
    await loadCovers(allIsbns)
  } catch {
    aktivne.value = []
    aktivneDigitalne.value = []
  }
}

async function loadPreporuke() {
  loadingRec.value = true
  try {
    const res = await knjigaApi.preporucene()
    // take up to 5 books
    const all = res.data || []
    preporuke.value = shuffle(all).slice(0, 5)
    await loadCovers(preporuke.value.map(b => b.isbn))
  } catch {
    preporuke.value = []
  } finally {
    loadingRec.value = false
  }
}

async function refreshPreporuke() {
  await loadPreporuke()
}

async function loadCovers(isbns) {
  for (const isbn of isbns) {
    if (coverUrls.value[isbn] !== undefined) continue
    try {
      const res = await knjigaApi.naslovna(isbn)
      coverUrls.value[isbn] = URL.createObjectURL(res.data)
    } catch {
      coverUrls.value[isbn] = null
    }
  }
}

function shuffle(arr) {
  const a = [...arr]
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]]
  }
  return a
}
</script>

<style scoped>
.page-title  { margin-bottom: 1.5rem; font-size: 1.8rem; }
.section-title {
  font-size: 1.3rem; margin-bottom: 0.8rem;
  display: flex; align-items: center; gap: 1rem; flex-wrap: wrap;
}
.mt-section  { margin-top: 2rem; }
.loading-msg { color: var(--text-mid); }

.format-row  { display: flex; gap: 2.5rem; flex-wrap: wrap; }
.format-label { font-size: 0.8rem; color: var(--text-mid); margin-bottom: 0.5rem; }

.book-row    { display: flex; gap: 1.2rem; flex-wrap: wrap; }

.book-card   { width: 110px; cursor: pointer; transition: transform 0.15s; }
.book-card:hover { transform: translateY(-3px); }

.book-cover  {
  width: 110px; height: 155px;
  border-radius: 10px; overflow: hidden; background: #ddd;
}
.book-cover img    { width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder { width: 100%; height: 100%; background: linear-gradient(135deg, #c8b9ae, #a89080); }

.book-title  { font-size: 0.78rem; font-weight: 600; margin: 0.35rem 0 0; line-height: 1.3; }
.book-author { font-size: 0.72rem; color: var(--text-mid); margin: 0; font-weight: 600; }

.btn-refresh {
  background: #e8d8cd; color: #5e4436; border: none;
  border-radius: 50px; padding: 0.3rem 0.9rem;
  font-size: 0.8rem; cursor: pointer; transition: background 0.2s;
}
.btn-refresh:hover    { background: #d8c4b5; }
.btn-refresh:disabled { opacity: 0.6; cursor: not-allowed; }
</style>