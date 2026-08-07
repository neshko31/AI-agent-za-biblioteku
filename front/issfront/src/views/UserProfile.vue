<template>
  <div class="app-layout">
    <SidebarNav />

    <main class="main-content">
      <template v-if="loading">
        <p>Učitavanje profila…</p>
      </template>

      <template v-else-if="profile">
        <h1 style="text-align:left;margin-bottom:0.2rem">Moj nalog</h1>
        <h2>Osnovni podaci</h2>

        <div class="profile-grid">

          <div class="profile-fields">
            <div class="field-row">
              <span class="field-label">Ime:</span>
              <input
                v-model="editForm.firstName"
                :disabled="!editing"
                class="field-input"
                :class="{ editable: editing }"
              />
            </div>
            <div class="field-row">
              <span class="field-label">Prezime:</span>
              <input
                v-model="editForm.lastName"
                :disabled="!editing"
                class="field-input"
                :class="{ editable: editing }"
              />
            </div>
            <div class="field-row">
              <span class="field-label">Email:</span>
              <input
                v-model="editForm.email"
                :disabled="!editing"
                class="field-input"
                :class="{ editable: editing }"
              />
            </div>
            <div class="field-row">
              <span class="field-label">Broj telefona:</span>
              <input
                v-model="editForm.phone"
                :disabled="!editing"
                class="field-input"
                :class="{ editable: editing }"
              />
            </div>
            <div class="field-row">
              <span class="field-label">Lozinka:</span>
              <template v-if="!editing">
                <button class="btn-small" @click="showPwChange = !showPwChange">
                  Promenite lozinku
                </button>
              </template>
            </div>


            <transition name="slide">
              <div v-if="showPwChange && !editing" class="pw-change">
                <input v-model="pwForm.current" type="password" placeholder="Trenutna lozinka" />
                <input v-model="pwForm.new" type="password" placeholder="Nova lozinka" />
                <button class="btn-small" @click="changePassword">Sačuvaj lozinku</button>
              </div>
            </transition>

            <div class="action-row">
              <button class="btn-primary" style="margin:0;padding:0.55rem 1.5rem;font-size:0.95rem"
                      @click="toggleEdit">
                {{ editing ? 'Otkažite' : 'Izmenite' }}
              </button>
              <button v-if="editing" class="btn-secondary" @click="saveProfile">
                Sačuvajte
              </button>
            </div>

            <p v-if="saveMsg" class="success-msg">{{ saveMsg }}</p>
            <p v-if="saveErr" class="error-msg">{{ saveErr }}</p>

            <div class="meta-section">
              <p><strong>Biblioteka:</strong> {{ profile.libraryName || '…' }}</p>
              <p>
                <strong>Članarina:</strong>
                {{ profile.tipPretplate === 'GODISNJA' ? 'godišnja' : 'mesečna' }}
              </p>
              <div v-if="profile.datUplate && profile.datIsteka">
                Važenje članarine od
                <strong>{{ fmt(profile.datUplate) }}</strong> do
                <strong>{{ fmt(profile.datIsteka) }}</strong>

                <p class="card-note">
                  Produženje članarine moguće je isključivo plaćanjem karticom.
                </p>
                <select v-model="selectedTipPretplate" class="field-input editable" style="margin-left:1rem;width:150px">
                    <option value="MESECNA">mesečna</option>
                    <option value="GODISNJA">godišnja</option>
                  </select>
                  <div class="card-form">
                    <input v-model="card.number" placeholder="Broj kartice" />

                    <div class="card-row">
                      <input v-model="card.expiry" placeholder="MM/YY" />
                      <input v-model="card.cvv" placeholder="CVV" />
                    </div>
                  </div>

                <button class="btn-small" style="margin-left:1rem"
                        @click="renewMembership">Produžite članstvo</button>

            </div>
          </div>
          </div>

          <div class="digital-card-wrap">
            <div class="digital-card">
              <div class="dc-name">{{ profile.firstName }} {{ profile.lastName }}</div>
              <div class="dc-lib">{{ profile.libraryName }}</div>
              <div class="dc-jmbg">JMBG: {{ profile.jmbg }}</div>
              <div class="dc-kat">{{ labelOf(profile.kategorijaClana) }}</div>
              <div class="dc-valid" v-if="profile.datIsteka">
                Važi do: {{ fmt(profile.datIsteka) }}
              </div>
              <img v-if="qrCodeUrl" :src="qrCodeUrl" class="qr-code" />
              <div class="dc-badge" :class="isActive ? 'active' : 'inactive'">
                {{ isActive ? 'AKTIVNA' : 'NEAKTIVNA' }}
              </div>
            </div>
            <p class="dc-label">digitalna kartica</p>
          </div>
        </div>


        <div class="genres-section">
          <h2 style="display:inline">Omiljeni žanrovi</h2>
          <RouterLink to="/genres/edit" class="btn-small" style="margin-left:1rem;text-decoration:none">
            Izmenite
          </RouterLink>
          <p class="genres-list">
            {{ profile.favouriteGenres?.join(', ') || '—' }}
          </p>
        </div>
      </template>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import SidebarNav from '../components/Sidebar.vue'
import { useAuthStore } from '../stroage/auth.js'
import { userApi, authApi } from '../services/api.js'
import QRCode from 'qrcode'
const authStore = useAuthStore()

const profile     = ref(null)
const loading     = ref(true)
const qrCodeUrl = ref('')
const editing     = ref(false)
const showPwChange = ref(false)
const saveMsg     = ref('')
const saveErr     = ref('')
const selectedTipPretplate = ref('MESECNA')
const isActive = computed(() => {
  if (!profile.value?.datIsteka) return false

  const danas = new Date()
  const istek = new Date(profile.value.datIsteka)

  return istek >= danas
})
const card = reactive({
  number: '',
  expiry: '',
  cvv: ''
})

const editForm = reactive({ firstName: '', lastName: '', email: '', phone: '', newPassword: '' })
const pwForm   = reactive({ current: '', new: '' })
onMounted(async () => {
  const value = authStore.user.jmbg + '-' + Date.now()
  qrCodeUrl.value = await QRCode.toDataURL(value)
})
onMounted(async () => {
  try {
    const res = await userApi.getMe()
    profile.value = res.data
    Object.assign(editForm, {
      firstName: res.data.firstName,
      lastName:  res.data.lastName,
      email:     res.data.email,
      phone:     res.data.phone,
    })
  } finally {
    loading.value = false
  }
})

function toggleEdit() {
  if (editing.value) {
    Object.assign(editForm, {
      firstName: profile.value.firstName,
      lastName:  profile.value.lastName,
      email:     profile.value.email,
      phone:     profile.value.phone,
      newPassword: '',
    })
  }
  editing.value = !editing.value
  saveMsg.value = ''
  saveErr.value = ''
}

async function saveProfile() {
  saveErr.value = ''
  try {
    const payload = {
      firstName:   editForm.firstName,
      lastName:    editForm.lastName,
      email:       editForm.email,
      phone:       editForm.phone

    }
    const res = await userApi.updateProfile(authStore.user.jmbg, payload)
    profile.value = res.data
    editing.value = false
    saveMsg.value = 'Profil je uspešno sačuvan!'
    setTimeout(() => saveMsg.value = '', 3000)
  } catch (e) {
    saveErr.value = e.response?.data?.message || 'Greška pri čuvanju'
  }
}

async function changePassword() {
  try {
    await userApi.updateProfile(authStore.user.jmbg, { newPassword: pwForm.new })
    showPwChange.value = false
    pwForm.current = ''
    pwForm.new = ''
    saveMsg.value = 'Lozinka je promenjena!'
    setTimeout(() => saveMsg.value = '', 3000)
  } catch (e) {
    saveErr.value = e.response?.data?.message || 'Greška'
  }
}
async function renewMembership() {
  saveErr.value = ''

  if (!card.number || !card.expiry || !card.cvv) {
    saveErr.value = 'Unesite sve podatke kartice'
    return
  }

  try {
    await authApi.renewMembership(
      authStore.user.jmbg,
      'ONLINE',
      selectedTipPretplate.value
    )

    const res = await userApi.getMe()
    profile.value = res.data

    saveMsg.value = 'Članstvo je produženo!'

    card.number = ''
    card.expiry = ''
    card.cvv = ''

    setTimeout(() => saveMsg.value = '', 3000)
  } catch (e) {
    saveErr.value = 'Greška pri produžetku'
  }
}

const KAT_LABELS = {
  REGULARNA: 'Regularna', DECIJA: 'Dečija',
  STUDENTSKA: 'Studentska', PENZIONERSKA: 'Penzionerska', PORODICNA: 'Porodična'
}
const labelOf = (tip) => KAT_LABELS[tip] || tip || '—'
const fmt = (d) => d ? new Date(d).toLocaleDateString('sr-RS') : ''
</script>

<style scoped>
.profile-grid {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 2rem;
  margin-top: 1rem;
}
.field-row {
  display: flex; align-items: center;
  gap: 1rem; margin-bottom: 0.7rem;
}
.field-label { width: 130px; font-size: 0.95rem; flex-shrink: 0; }
.field-input {
  background: transparent;
  border: none;
  font-size: 0.9rem;
  color: var(--text-mid);
  outline: none;
  width: 180px;
  padding: 0.25rem 0.5rem;
  border-radius: 5px;
}
.field-input.editable {
  background: white;
  border: 1.5px solid var(--border);
}
.action-row { display: flex; gap: 1rem; margin: 1.2rem 0 0.5rem; }

.meta-section { margin-top: 1.2rem; line-height: 2; font-size: 0.9rem; }

.digital-card {
  background: white;
  border-radius: 14px;
  padding: 1.2rem 1.4rem;
  box-shadow: 0 3px 12px rgba(0,0,0,0.12);
  min-height: 200px;
  display: flex; flex-direction: column; gap: 0.4rem;
}
.dc-header { font-size: 0.8rem; font-weight: 700; color: var(--btn-primary); }
.dc-name   { font-size: 1.1rem; font-weight: 600; }
.dc-lib    { font-size: 0.8rem; color: var(--text-mid); }
.dc-jmbg   { font-size: 0.75rem; color: #888; font-family: monospace; }
.dc-kat    { font-size: 0.85rem; color: var(--text-mid); }
.dc-valid  { font-size: 0.8rem; color: var(--text-mid); }
.dc-badge  {
  display: inline-block; padding: 0.2rem 0.7rem;
  border-radius: 50px; font-size: 0.75rem; font-weight: 700; margin-top: auto;
}
.dc-badge.active   { background: #c8f0c8; color: #1a5e1a; }
.dc-badge.inactive { background: #f8d7d7; color: #8b1a1a; }
.dc-label { text-align: right; font-size: 0.8rem; color: var(--text-mid); margin-top: 0.4rem; }

.genres-section { margin-top: 2rem; }
.genres-list { margin-top: 0.6rem; font-size: 0.95rem; color: var(--text-mid); }

.pw-change {
  display: flex; flex-direction: column; gap: 0.5rem;
  margin-bottom: 0.7rem;
  background: white; padding: 0.8rem; border-radius: 8px;
}
.pw-change input {
  width: 100%; padding: 0.4rem 0.6rem;
  border: 1.5px solid var(--border); border-radius: 5px;
}

.success-msg {
  color: #1a5e1a; background: #d4f0d4;
  border-radius: 6px; padding: 0.4rem 0.75rem;
  font-size: 0.85rem; margin-top: 0.5rem;
}
.card-note {
  margin-top: 0.5rem;
  font-size: 0.85rem;
  color: #555;
  font-style: italic;
}
.card-form {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  margin-top: 0.5rem;
}

.card-form input {
  padding: 0.45rem 0.6rem;
  border: 1.5px solid var(--border);
  border-radius: 6px;
  font-size: 0.85rem;
}

.card-row {
  display: flex;
  gap: 0.5rem;
}

.card-row input {
  flex: 1;
}

.qr-code {
  margin-top: 0.5rem;
  width: 100px;
  height: 100px;
}
.slide-enter-active, .slide-leave-active { transition: all 0.2s ease; }
.slide-enter-from, .slide-leave-to       { opacity: 0; transform: translateY(-6px); }
</style>
