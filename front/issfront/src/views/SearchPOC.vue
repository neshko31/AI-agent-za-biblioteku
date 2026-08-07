<template>
  <div style="padding:1rem;border:4px dashed red;background:#fffae0;">
    <h3> FULLTEXT SEARCH POC </h3>

    <input
      v-model="query"
      @keyup.enter="doSearch"
      placeholder="search term..."
      style="padding:4px;width:250px;"
    />
    <button @click="doSearch" style="margin-left:6px;">go</button>

    <p v-if="loading">searching...</p>
    <p v-if="error" style="color:red;">ERROR: {{ error }}</p>

    <pre v-if="results" style="background:#eee;padding:8px;max-height:400px;overflow:auto;">{{ results }}</pre>
  </div>
</template>

<script setup>
import { ref } from 'vue'
// adjust this import path if your alias setup differs
import { searchApi } from '../services/api'

const query = ref('')
const results = ref(null)
const loading = ref(false)
const error = ref('')

async function doSearch() {
  if (!query.value) return
  loading.value = true
  error.value = ''
  results.value = null
  try {
    const res = await searchApi.fulltext(query.value)
    results.value = res.data
  } catch (e) {
    error.value = e.response?.data?.message || e.message || 'unknown error'
  } finally {
    loading.value = false
  }
}
</script>
