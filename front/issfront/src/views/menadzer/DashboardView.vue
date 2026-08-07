<template>
  <div class="dashboard">
    <div class="page-header">
      <h1>Dobrodošli, {{ user?.firstName }} {{ user?.lastName }}</h1>
    </div>

    <div class="info-grid">
      <div class="info-card">
        <span class="info-label">Email</span>
        <span class="info-value">{{ user?.email }}</span>
      </div>
      <div class="info-card">
        <span class="info-label">Uloga</span>
        <span class="info-value role-badge">{{ user?.role }}</span>
      </div>
      <div class="info-card">
        <span class="info-label">JMBG</span>
        <span class="info-value">{{ user?.jmbg }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '../../stroage/auth.js'

const auth = useAuthStore()
const user = auth.user
</script>

<style scoped>
.dashboard {
  width: 100%;
}

.page-header {
  margin-bottom: 2rem;
}

.page-header h1 {
  margin: 0 0 0.25rem;
  font-size: 2rem;
  color: var(--text-h);
}

.subtitle {
  color: var(--text);
  font-size: 0.95rem;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.info-card {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  padding: 1.25rem 1.5rem;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: var(--bg);
}

.info-label {
  font-size: 0.8rem;
  color: var(--text);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.info-value {
  font-size: 1rem;
  color: var(--text-h);
  font-weight: 500;
}

.role-badge {
  color: var(--accent);
}
</style>