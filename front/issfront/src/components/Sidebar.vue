<template>
  <aside class="sidebar">
    <RouterLink to="/profile" class="sidebar-profile">
      <div class="sidebar-avatar">👤</div>
      <div class="sidebar-name">
        {{ authStore.user?.firstName }}<br />{{ authStore.user?.lastName }}
      </div>
    </RouterLink>
    <nav class="sidebar-nav">
      <RouterLink class="nav-item" to="/home">
        <span class="nav-icon"></span> Početna
      </RouterLink>
      <RouterLink class="nav-item" to="/knjige">
        <span class="nav-icon"></span> Sve knjige
      </RouterLink>
      <RouterLink class="nav-item" to="/pozajmice">
        <span class="nav-icon"></span> Pozajmice
      </RouterLink>
      <RouterLink class="nav-item" to="/dugovanja">
        <span class="nav-icon"></span> Dugovanja
      </RouterLink>
      <RouterLink class="nav-item notif-item" to="/obavestenja">
        <span class="nav-icon"></span> Obaveštenja
        <span v-if="unreadCount > 0" class="notif-dot"></span>
      </RouterLink>
      <RouterLink v-if="role === 'ADMINISTRATOR' || role === 'BIBLIOTEKAR'" class="nav-item" to="/katalog">
        <span class="nav-icon"></span> Katalog
      </RouterLink>
      <RouterLink class="nav-item" to="/baze-podataka">
        <span class="nav-icon"></span> Elektronske baze podataka
      </RouterLink>

      <!-- Samo za bibliotekara i administratora -->
      <RouterLink v-if="role === 'ADMINISTRATOR' || role === 'BIBLIOTEKAR'" class="nav-item" to="/zahtevi">
        <span class="nav-icon"></span> Međubibliotečki zahtevi
      </RouterLink>

      <!-- Samo za clana -->
      <RouterLink v-if="role === 'CLAN'" class="nav-item" to="/moji-predlozi">
        <span class="nav-icon"></span> Moji predlozi naslova
      </RouterLink>
      <RouterLink v-if="role === 'CLAN'" class="nav-item" to="/notifikacije">
        <span class="nav-icon"></span> Praćenje statusa predloga
      </RouterLink>
      <RouterLink v-if="role === 'CLAN'" class="nav-item" to="/asistent">
        <span class="nav-icon"></span> AI Asistent
      </RouterLink>

      <!-- Samo za bibliotekara -->
      <RouterLink v-if="role === 'BIBLIOTEKAR'" class="nav-item" to="/predlozi-na-cekanju">
        <span class="nav-icon"></span> Predlozi naslova
      </RouterLink>
      <RouterLink v-if="role === 'BIBLIOTEKAR'" class="nav-item" to="/produzenja-na-cekanju">
        Zahtevi za produženje
      </RouterLink>
      <RouterLink v-if="role === 'BIBLIOTEKAR'" class="nav-item" to="/vracanje-knjiga">
        Vraćanje knjiga
      </RouterLink>
      <RouterLink v-if="role === 'BIBLIOTEKAR' || role === 'MENADZER' || role === 'ADMINISTRATOR'" class="nav-item" to="/izvestaj">
              <span class="nav-icon"></span> Izvestaj o aktivnostima
      </RouterLink>
      <RouterLink v-if="role === 'BIBLIOTEKAR' || role === 'MENADZER' || role === 'ADMINISTRATOR'" class="nav-item" to="/izvestaj-katalog">
              <span class="nav-icon"></span> Izvestaj o katalogu
      </RouterLink>
      <RouterLink v-if="role === 'BIBLIOTEKAR' || role === 'MENADZER' || role === 'ADMINISTRATOR'" class="nav-item" to="/izvestaj-ai-agent">
              <span class="nav-icon"></span> Izveštaj o upotrebi AI agenta
      </RouterLink>
    </nav>

    <button class="logout-btn" @click="handleLogout">Odjavi se</button>
  </aside>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stroage/auth.js'
import { pozajmicaApi } from '../services/api.js'

const router    = useRouter()
const authStore = useAuthStore()
const unreadCount = computed(() => authStore.unreadCount)
onMounted(async () => {
  if (authStore.isLoggedIn && authStore.getRole() === 'CLAN') {
    try {
      const res = await pozajmicaApi.getObavestenja()
      const count = (res.data || []).filter(o => !o.procitano).length
            authStore.setUnreadCount(count)
    } catch {}
  }
})
const role = authStore.getRole()

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.logout-btn {
  margin: 0 1rem 1.5rem;
  background: transparent;
  border: 1px solid rgba(255,255,255,0.4);
  color: rgba(255,255,255,0.8);
  border-radius: 50px;
  padding: 0.4rem 1rem;
  cursor: pointer;
  font-size: 0.85rem;
  transition: background 0.2s;
}
.logout-btn:hover { background: rgba(255,255,255,0.1); }
.notif-item {
  position: relative;
}

.notif-dot {
  width: 8px;
  height: 8px;
  background: red;
  border-radius: 50%;
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
}
</style>
