<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminApi } from '../../utils/api'
import { toast } from '../../utils/ui-feedback'

const loading = ref(true)
const records = ref<any[]>([])

function formatDate(value: string | null | undefined): string {
  if (!value) return '-'
  const date = new Date(value)
  if (isNaN(date.getTime())) return String(value).replace('T', ' ').slice(0, 16)
  return date.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  })
}

async function loadData() {
  loading.value = true
  try {
    const res = await adminApi.listRequestHistoryRecords()
    records.value = Array.isArray(res.data) ? res.data : []
  } catch (e: any) {
    toast.error(e.message || 'Failed to load history')
    records.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page">
    <header class="page-header">
      <h1>Request History</h1>
    </header>

    <div class="panel">
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>History ID</th>
              <th>Request ID</th>
              <th>Old Status</th>
              <th>New Status</th>
              <th>Changed By</th>
              <th>Remark</th>
              <th>Changed At</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="7" style="text-align: center; color: #888;">Loading...</td>
            </tr>
            <tr v-else-if="records.length === 0">
              <td colspan="7" style="text-align: center; color: #888;">No history records</td>
            </tr>
            <tr v-for="r in records" :key="r.historyId || r.id">
              <td>{{ r.historyId || r.id || '-' }}</td>
              <td>{{ r.requestId || '-' }}</td>
              <td>{{ r.oldStatus || '-' }}</td>
              <td>{{ r.newStatus || '-' }}</td>
              <td>{{ r.changedByStudentName || r.changedBy || '-' }}</td>
              <td>{{ r.remark || '-' }}</td>
              <td>{{ formatDate(r.changedAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="summary">{{ records.length }} record(s)</div>
    </div>
  </div>
</template>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }
.page-header h1 { margin: 0; font-size: 1.8rem; font-weight: 600; color: #1c1b33; }
.panel { background: #fff; border-radius: 28px; box-shadow: 0 20px 60px rgba(21, 16, 45, 0.15); padding: 24px 28px; }
.table-wrapper { overflow-x: auto; border-radius: 18px; }
.data-table { width: 100%; border-collapse: collapse; min-width: 700px; font-size: 0.9rem; }
.data-table th { text-align: left; padding: 16px; background: #f8f5ff; font-weight: 600; color: #5a2b98; border-bottom: 2px solid rgba(90, 43, 152, 0.2); }
.data-table td { padding: 16px; border-bottom: 1px solid rgba(156, 156, 178, 0.2); }
.data-table tbody tr:hover { background: rgba(90, 43, 152, 0.03); }
.summary { margin-top: 16px; font-size: 0.9rem; color: #6b6b82; }
</style>
