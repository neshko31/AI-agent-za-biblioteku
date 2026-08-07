<template>
  <div class="app-layout">
    <SidebarNav />

    <main class="main-content">
      <div class="books-toolbar">
        <div>
          <h1 class="books-title">Sve knjige</h1>
          <p class="books-sub">Pretražite knjige po nazivu.</p>
        </div>
        <div class="books-actions">
          <button
            v-if="isLibrarian"
            class="btn-secondary"
            @click="openCreateBook"
          >
            Dodaj novu knjigu
          </button>
          <div class="books-search">
            <input
              v-model="query"
              class="search-input"
              type="search"
              placeholder="Unesite naziv knjige"
            />
            <button class="btn-secondary" @click="runSearch">Pretraži</button>
          </div>
        </div>
      </div>

      <p v-if="!authorized" class="error-msg">Nemate pristup ovoj stranici.</p>

      <template v-else>
        <p v-if="loading">Ucitavanje knjiga...</p>
        <p v-if="error" class="error-msg">{{ error }}</p>

        <div v-if="books.length" class="books-grid">
          <article
            v-for="book in books"
            :key="book.isbn"
            class="book-card"
            @click="openBook(book.isbn)"
          >
            <div class="book-cover">
              <img v-if="covers[book.isbn]" :src="covers[book.isbn]" alt="" />
              <div v-else class="cover-placeholder"></div>
            </div>
            <div class="book-meta">
              <h3>{{ book.naslov }}</h3>
              <p>{{ book.autor }}</p>
            </div>
          </article>
        </div>

        <p v-else-if="!loading" class="empty-state">Nema rezultata za prikaz.</p>
      </template>
    </main>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import SidebarNav from '../components/Sidebar.vue'
import { useAuthStore } from '../stroage/auth.js'
import { knjigaApi } from '../services/api.js'

const authStore = useAuthStore()
const router = useRouter()

const authorized = ref(false)
const books = ref([])
const covers = ref({})
const loading = ref(false)
const error = ref('')
const query = ref('')
let searchTimer = null

const isLibrarian = ref(false)

onMounted(() => {
  const role = authStore.getRole()
  authorized.value = role === 'CLAN' || role === 'BIBLIOTEKAR' || role === 'ADMINISTRATOR'
  isLibrarian.value = role === 'BIBLIOTEKAR' || role === 'ADMINISTRATOR'
  if (authorized.value) {
    loadBooks()
  }
})

watch(query, () => {
  if (!authorized.value) return
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    runSearch()
  }, 300)
})

onBeforeUnmount(() => {
  revokeCoverUrls()
})

async function loadBooks(searchValue) {
  loading.value = true
  error.value = ''
  try {
    const res = searchValue
      ? await knjigaApi.pretraga(searchValue)
      : await knjigaApi.sveOsnovno()

    books.value = res.data || []
    await loadCovers()
  } catch (e) {
    error.value = e.response?.data || 'Greška pri učitavanju.'
  } finally {
    loading.value = false
  }
}

async function loadCovers() {
  revokeCoverUrls()
  const nextCovers = {}
  await Promise.all(
    books.value.map(async (book) => {
      try {
        const res = await knjigaApi.naslovna(book.isbn)
        nextCovers[book.isbn] = URL.createObjectURL(res.data)
      } catch {
        nextCovers[book.isbn] = ''
      }
    })
  )
  covers.value = nextCovers
}

function revokeCoverUrls() {
  Object.values(covers.value).forEach((url) => {
    if (url) URL.revokeObjectURL(url)
  })
  covers.value = {}
}

function runSearch() {
  const trimmed = query.value.trim()
  loadBooks(trimmed || null)
}

function openBook(isbn) {
  router.push(`/knjige/${isbn}`)
}

function openCreateBook() {
  router.push('/knjige/nova')
}
</script>

<style scoped>
.books-toolbar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 1.5rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.books-title {
  font-size: 2rem;
  margin: 0;
  text-align: left;
}

.books-sub {
  margin-top: 0.4rem;
  color: var(--text-mid);
}

.books-search {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.books-actions {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  flex-wrap: wrap;
}

.search-input {
  min-width: 240px;
  background: var(--input-bg);
  border: 1.5px solid var(--border);
  border-radius: 999px;
  padding: 0.55rem 1rem;
  font-size: 0.95rem;
}

.books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1.5rem;
}

.book-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: var(--shadow);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  flex-direction: column;
}

.book-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 28px rgba(30, 45, 20, 0.2);
}

.book-cover {
  background: #e3e3e3;
  height: 250px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 80%;
  height: 80%;
  background: linear-gradient(135deg, #c7c7c7, #b0b0b0);
  border-radius: 10px;
}

.book-meta {
  padding: 0.9rem 1rem 1.1rem;
  text-align: left;
}

.book-meta h3 {
  font-size: 1.1rem;
  margin-bottom: 0.2rem;
  color: var(--text-dark);
}

.book-meta p {
  color: var(--text-mid);
  font-size: 0.95rem;
}

.empty-state {
  margin-top: 2rem;
  color: var(--text-mid);
}

@media (max-width: 768px) {
  .books-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .books-search {
    width: 100%;
  }

  .books-actions {
    width: 100%;
  }

  .search-input {
    flex: 1;
  }
}
</style>
