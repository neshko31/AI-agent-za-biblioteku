import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token    = ref(localStorage.getItem('token') || null)
  const user     = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const regJmbg  = ref(localStorage.getItem('regJmbg') || null)  // persists across steps

const unreadCount= ref(0)
  const isLoggedIn = computed(() => !!token.value)

  function setAuth(authResponse) {
    token.value = authResponse.token
    user.value  = {
      jmbg:      authResponse.jmbg,
      email:     authResponse.email,
      role:      authResponse.role,
      firstName: authResponse.firstName,
      lastName:  authResponse.lastName,
      bid:       authResponse.bid,
    }
    localStorage.setItem('token', token.value)
    localStorage.setItem('user',  JSON.stringify(user.value))
  }

  function setRegJmbg(jmbg) {
    regJmbg.value = jmbg
    localStorage.setItem('regJmbg', jmbg)
  }

  function logout() {
    token.value = null
    user.value  = null
    unreadCount.value= 0
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('regJmbg')
  }
function setUnreadCount(count) {
    unreadCount.value = count
  }
  function getRole() {
    return user.value ? user.value.role : null
  }

  function getBid() {
    return user.value ? user.value.bid : null
  }

  function isAdmin() {
    return getRole() === 'ADMINISTRATOR'
  }

  function isLibrarianOrAdmin() {
    return getRole() === 'BIBLIOTEKAR' || getRole() === 'ADMINISTRATOR'
  }

  return { token, user, regJmbg, isLoggedIn, setAuth, setRegJmbg, logout, getRole, getBid, isAdmin, isLibrarianOrAdmin, unreadCount, setUnreadCount }

})
