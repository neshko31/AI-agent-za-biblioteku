<template>
  <div class="app-layout">
    <SidebarNav />

    <main class="main-content">
      <div class="page-toolbar">
        <div class="toolbar-left">
          <RouterLink to="/katalog" class="back-btn">← Katalozi</RouterLink>
          <div>
            <h1 class="page-title">{{ katalog?.katIme || 'Katalog' }}</h1>
            <p class="page-sub">{{ katalog?.standard }} · {{ katalog?.biblioteka?.name }}</p>
          </div>
        </div>
      </div>

      <p v-if="loading" class="state-msg">Učitavanje knjiga...</p>
      <p v-if="error" class="error-msg">{{ error }}</p>

      <template v-if="!loading && katalog">
        <p v-if="!books.length" class="state-msg">Ovaj katalog nema knjiga.</p>

        <div v-else class="books-grid">
          <RouterLink
            v-for="book in books"
            :key="book.isbn"
            :to="book.deleted ? '' : `/knjige/${book.isbn}`"
            custom
            v-slot="{ navigate }"
          >
          <div
            class="book-card"
            :class="{ 'book-deleted': book.deleted }"
            @click="!book.deleted && navigate()"
            :style="!book.deleted ? 'cursor: pointer' : 'cursor: default'"
          >
            <div class="book-cover">
              <img
                v-if="book.putanjaNaslovna"
                :src="book.putanjaNaslovna"
                :alt="book.naslov"
                class="cover-img"
                @error="e => e.target.style.display='none'"
              />
              <div v-else class="cover-placeholder">📖</div>
            </div>

            <div class="book-body">
              <div class="book-badges">
                <span v-if="book.audioKnjiga" class="badge badge-audio">🎧 Audio</span>
                <span v-if="book.fizickaKnjiga" class="badge badge-physical">📦 Fizička</span>
                <span v-if="book.deleted" class="badge badge-deleted">Obrisana</span>
              </div>
              <h3 class="book-title">{{ book.naslov }}</h3>
              <p class="book-autor">{{ book.autor }}</p>
              <p class="book-sinopsis">{{ book.sinopsis }}</p>
              <p class="book-isbn">ISBN: {{ book.isbn }}</p>
            </div>
          </div>
          </RouterLink>
        </div>
      </template>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import SidebarNav from '../components/Sidebar.vue'
import { useAuthStore } from '../stroage/auth.js'

const auth = useAuthStore()
const route = useRoute()

const katalog = ref(null)
const loading = ref(false)
const error = ref('')

const books = computed(() => katalog.value?.books ?? [])

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await axios.get(`http://localhost:8080/api/katalog/${route.params.id}`, {
      headers: { Authorization: `Bearer ${auth.token}` }
    })
    katalog.value = res.data
  } catch (e) {
    error.value = e.response?.data?.message || e.response?.data || 'Greška pri učitavanju kataloga.'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page-toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1.5rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.toolbar-left {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.back-btn {
  font-size: 0.85rem;
  color: var(--text-mid, #666);
  text-decoration: none;
  opacity: 0.7;
  transition: opacity 0.2s;
}
.back-btn:hover { opacity: 1; }

.page-title {
  font-size: 2rem;
  margin: 0;
  text-align: left;
}

.page-sub {
  margin-top: 0.3rem;
  font-size: 0.9rem;
  color: var(--text-mid, #888);
}

.state-msg {
  margin-top: 2rem;
  color: var(--text-mid, #888);
}

.error-msg {
  color: #e53e3e;
  font-size: 0.9rem;
  margin-top: 1rem;
}

/* Grid */
.books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.25rem;
}

.book-card {
  background: white;
  border-radius: 16px;
  box-shadow: var(--shadow, 0 4px 12px rgba(0,0,0,0.08));
  display: flex;
  gap: 1rem;
  padding: 1.1rem;
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
}

.book-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(30,45,20,0.13);
}

/* Deleted books — grayed out */
.book-deleted {
  opacity: 0.45;
  filter: grayscale(60%);
}
.book-deleted:hover {
  transform: none;
  box-shadow: var(--shadow, 0 4px 12px rgba(0,0,0,0.08));
}

/* Cover */
.book-cover {
  flex-shrink: 0;
  width: 60px;
  height: 84px;
  border-radius: 6px;
  overflow: hidden;
  background: #f0ede8;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  font-size: 1.8rem;
}

/* Body */
.book-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.book-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
  margin-bottom: 0.2rem;
}

.badge {
  font-size: 0.7rem;
  padding: 2px 8px;
  border-radius: 999px;
  font-weight: 500;
}

.badge-audio    { background: #e9f5ff; color: #2b6cb0; }
.badge-physical { background: #f0fff4; color: #276749; }
.badge-deleted  { background: #fff5f5; color: #c53030; }

.book-title {
  font-size: 0.95rem;
  font-weight: 600;
  margin: 0;
  color: var(--text-dark, #111);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-autor {
  font-size: 0.82rem;
  color: var(--text-mid, #666);
  margin: 0;
}

.book-sinopsis {
  font-size: 0.8rem;
  color: var(--text, #888);
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.book-isbn {
  font-size: 0.75rem;
  color: var(--text-mid, #aaa);
  margin: 0;
  margin-top: auto;
}
</style>
