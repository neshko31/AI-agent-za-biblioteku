<template>
  <div class="komentar-kartica" :style="{ marginLeft: dubina * 20 + 'px' }">
    <div class="komentar-header">
      <span class="komentar-autor">{{ komentar.autorIme }}</span>
      <span class="komentar-datum">
        {{ new Date(komentar.datumKreiranjaK).toLocaleDateString('sr') }}
      </span>
    </div>
    <p class="komentar-tekst">{{ komentar.tekstK }}</p>
    <div class="komentar-akcije">
      <button
        class="btn-lajk"
        :class="{ aktivan: komentar.lajkovaoTrenutniKorisnik }"
        type="button"
        @click="$emit('lajkuj', komentar)"
      >
        {{ komentar.lajkovaoTrenutniKorisnik ? '❤️' : '🤍' }} {{ komentar.brojLajkova }}
      </button>
      <button
        v-if="dubina < 7"
        class="btn-odgovor-link"
        type="button"
        @click="$emit('odgovori', komentar)"
      >
        Odgovori
      </button>
    </div>

    <!-- Rekurzivno gnežđenje odgovora -->
    <KomentarStablo
      v-for="o in komentar.odgovori"
      :key="o.id"
      :komentar="o"
      :dubina="dubina + 1"
      @odgovori="$emit('odgovori', $event)"
      @lajkuj="$emit('lajkuj', $event)"
    />
  </div>
</template>

<script setup>
defineProps({
  komentar: { type: Object, required: true },
  dubina:   { type: Number, default: 0 }
})

defineEmits(['odgovori', 'lajkuj'])
</script>

<style scoped>
.komentar-kartica {
  background: #f9f5f0;
  border-radius: 10px;
  padding: 0.85rem 1rem;
  margin-top: 0.5rem;
}

/* Vizuelno razlikovanje po dubini — svaki nivo malo tamniji */
.komentar-kartica + .komentar-kartica,
.komentar-kartica .komentar-kartica {
  border-left: 3px solid var(--border);
  background: #f2ece5;
}

.komentar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.35rem;
}
.komentar-autor { font-weight: 600; font-size: 0.9rem; color: var(--text-dark); }
.komentar-datum { font-size: 0.78rem; color: var(--text-mid); }
.komentar-tekst { font-size: 0.95rem; color: var(--text-dark); line-height: 1.5; margin-bottom: 0.5rem; }

.komentar-akcije { display: flex; gap: 0.75rem; align-items: center; }
.btn-lajk {
  background: none;
  border: 1px solid var(--border);
  border-radius: 50px;
  padding: 0.25rem 0.75rem;
  font-size: 0.85rem;
  cursor: pointer;
  color: var(--text-mid);
  transition: all 0.15s;
}
.btn-lajk:hover, .btn-lajk.aktivan {
  background: #fce4e4;
  border-color: #e07070;
  color: #8b1a1a;
}
.btn-odgovor-link {
  background: none;
  border: none;
  color: var(--text-mid);
  font-size: 0.85rem;
  cursor: pointer;
  text-decoration: underline;
  padding: 0;
}
.btn-odgovor-link:hover { color: var(--btn-primary); }
</style>