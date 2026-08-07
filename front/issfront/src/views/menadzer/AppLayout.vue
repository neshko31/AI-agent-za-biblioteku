<template>
  <div class="layout">
    <!-- SIDEBAR -->
    <aside class="sidebar">
      <div class="sidebar-logo">
        <span class="logo-fallback">📚</span>
      </div>

      <nav class="sidebar-nav">
        <RouterLink to="/menadzer/dashboard" class="nav-item" active-class="nav-item--active">
          <span class="nav-icon">🏠</span>
          <span class="nav-label">Početna</span>
        </RouterLink>

        <RouterLink to="/menadzer/dobavljaci" class="nav-item" active-class="nav-item--active">
          <span class="nav-icon">📋</span>
          <span class="nav-label">Dobavljači</span>
        </RouterLink>

        <RouterLink to="/menadzer/predlozi" class="nav-item" active-class="nav-item--active">
          <span class="nav-icon">📑</span>
          <span class="nav-label">Korisnički predlozi</span>
        </RouterLink>

        <RouterLink to="/menadzer/sistemske-preporuke" class="nav-item" active-class="nav-item--active">
          <span class="nav-icon">📈</span>
          <span class="nav-label">Sistemske preporuke</span>
        </RouterLink>

        <RouterLink to="/menadzer/budzet" class="nav-item" active-class="nav-item--active">
          <span class="nav-icon">💰</span>
          <span class="nav-label">Budžet</span>
        </RouterLink>

        <RouterLink to="/menadzer/narudzbine" class="nav-item" active-class="nav-item--active">
          <span class="nav-icon">📦</span>
          <span class="nav-label">Narudžbine</span>
        </RouterLink>
      
        <RouterLink to="/menadzer/reklamacije" class="nav-item" active-class="nav-item--active">
          <span class="nav-icon">⚠️</span>
          <span class="nav-label">Reklamacije</span>
        </RouterLink>

        <RouterLink to="/menadzer/izvestaj" class="nav-item" active-class="nav-item--active">
          <span class="nav-icon">📄</span>
          <span class="nav-label">Izvestaji</span>
        </RouterLink>

      </nav>

      <button class="nav-item nav-item--logout" @click="handleLogout">
        <span class="nav-icon">↩</span>
        <span class="nav-label">Odjavi se</span>
      </button>
    </aside>

    <!-- MAIN CONTENT -->
    <main class="main-content">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { useAuthStore } from '../../stroage/auth.js'

const auth = useAuthStore()
const router = useRouter()

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100svh;
  background: var(--bg);
}

/* ── SIDEBAR ── */
.sidebar {
  width: 180px;
  min-height: 100svh;
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  padding: 1rem 0;
  position: sticky;
  top: 0;
  flex-shrink: 0;
}

.sidebar-logo {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 1rem;
  margin-bottom: 1.5rem;
}

.logo-img {
  width: 60px;
  height: 60px;
  object-fit: contain;
}

.logo-fallback {
  font-size: 2.5rem;
}

/* ── NAV ITEMS ── */
.sidebar-nav {
  display: flex;
  flex-direction: column;
  flex: 1;
  gap: 0.25rem;
  padding: 0 0.5rem;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.3rem;
  padding: 0.75rem 0.5rem;
  border-radius: 10px;
  text-decoration: none;
  color: var(--text);
  background: transparent;
  border: none;
  cursor: pointer;
  font-family: inherit;
  font-size: inherit;
  transition: background 0.18s, color 0.18s;
  width: 100%;
}

.nav-item:hover {
  background: var(--accent-bg);
  color: var(--accent);
}

.nav-item--active {
  background: var(--accent-bg);
  color: var(--accent);
  border-left: 3px solid var(--accent);
}

.nav-icon {
  font-size: 1.4rem;
}

.nav-label {
  font-size: 0.75rem;
  font-weight: 500;
}

/* ── LOGOUT ── */
.nav-item--logout {
  margin: 0.5rem;
  color: var(--text);
}

.nav-item--logout:hover {
  background: rgba(220, 50, 50, 0.1);
  color: #dc3232;
}

/* ── MAIN ── */
.main-content {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
}
</style>