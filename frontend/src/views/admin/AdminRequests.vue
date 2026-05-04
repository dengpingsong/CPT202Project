<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '../../utils/api'
import { toast } from '../../utils/ui-feedback'

const loading = ref(true)
const records = ref<any[]>([])
const statusFilter = ref('')

function normalizeStatus(status: string | null | undefined): string {
  return String(status || 'UNKNOWN').toUpperCase()
}

function statusColor(status: string): string {
  const s = normalizeStatus(status)
  if (s === 'PENDING') return '#f6a63d'
  if (s === 'ACCEPTED') return '#2fc5a8'
  if (s === 'REJECTED') return '#c74545'
  if (s === 'WITHDRAWN') return '#6b6b82'
  return '#5a2b98'
}

function formatDate(value: string | null | undefined): string {
  if (!value) return '-'
  const date = new Date(value)
  if (isNaN(date.getTime())) return String(value).replace('T', ' ').slice(0, 16)
  return date.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  })
}

const filteredRecords = computed(() => {
  if (!statusFilter.value) return records.value
  return records.value.filter(r => normalizeStatus(r.requestStatus) === statusFilter.value)
})

async function loadData() {
  loading.value = true
  try {
    const res = await adminApi.listRequestRecords(statusFilter.value || undefined)
    records.value = Array.isArray(res.data) ? res.data : []
  } catch (e: any) {
    toast.error(e.message || 'Failed to load requests')
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
      <h1>Request Records</h1>
    </header>

    <div class="panel">
      <div class="filters-row">
        <label class="filter-field">
          Status
          <select v-model="statusFilter" @change="loadData()">
            <option value="">All Status</option>
            <option value="PENDING">Pending</option>
            <option value="ACCEPTED">Accepted</option>
            <option value="REJECTED">Rejected</option>
            <option value="WITHDRAWN">Withdrawn</option>
          </select>
        </label>
        <button class="clear-btn" @click="statusFilter = ''; loadData()">Refresh</button>
      </div>

      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>Request ID</th>
              <th>Project</th>
              <th>Student</th>
              <th>Status</th>
              <th>Rank</th>
              <th>Submitted</th>
              <th>Decision Comment</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="7" style="text-align: center; color: #888;">Loading...</td>
            </tr>
            <tr v-else-if="filteredRecords.length === 0">
              <td colspan="7" style="text-align: center; color: #888;">No records found</td>
            </tr>
            <tr v-for="r in filteredRecords" :key="r.requestId">
              <td>{{ r.requestId }}</td>
              <td>{{ r.projectTitle || '-' }}</td>
              <td>{{ r.studentName || '-' }}</td>
              <td>
                <span class="status-pill" :style="{ color: statusColor(r.requestStatus), background: statusColor(r.requestStatus) + '18' }">
                  {{ normalizeStatus(r.requestStatus) }}
                </span>
              </td>
              <td>{{ r.preferenceRank || '-' }}</td>
              <td>{{ formatDate(r.submittedAt) }}</td>
              <td>{{ r.decisionComment || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="summary">{{ filteredRecords.length }} record(s)</div>
    </div>
  </div>
</template>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }
.page-header h1 { margin: 0; font-size: 1.8rem; font-weight: 600; color: #1c1b33; }
.panel { background: #fff; border-radius: 28px; box-shadow: 0 20px 60px rgba(21, 16, 45, 0.15); padding: 24px 28px; }
.filters-row { display: flex; gap: 10px; align-items: end; margin-bottom: 16px; }
.filter-field { display: flex; flex-direction: column; gap: 6px; font-size: 0.9rem; color: #6b6b82; }
.filter-field select { padding: 10px 12px; border: 1px solid rgba(90, 43, 152, 0.18); border-radius: 12px; font: inherit; background: #fff; outline: none; min-width: 180px; }
.filter-field select:focus { border-color: #5a2b98; box-shadow: 0 0 0 3px rgba(90, 43, 152, 0.14); }
.clear-btn { padding: 10px 14px; border-radius: 12px; border: 1px solid rgba(90, 43, 152, 0.16); background: linear-gradient(180deg, #fff, #f7f2ff); color: #5a2b98; font-weight: 600; cursor: pointer; font-family: inherit; font-size: 0.9rem; }
.clear-btn:hover { background: linear-gradient(180deg, #fff, #efe4ff); }
.table-wrapper { overflow-x: auto; border-radius: 18px; }
.data-table { width: 100%; border-collapse: collapse; min-width: 800px; font-size: 0.9rem; }
.data-table th { text-align: left; padding: 16px; background: #f8f5ff; font-weight: 600; color: #5a2b98; border-bottom: 2px solid rgba(90, 43, 152, 0.2); }
.data-table td { padding: 16px; border-bottom: 1px solid rgba(156, 156, 178, 0.2); }
.data-table tbody tr:hover { background: rgba(90, 43, 152, 0.03); }
.status-pill { display: inline-block; padding: 4px 10px; border-radius: 999px; font-size: 0.8rem; font-weight: 600; }
.summary { margin-top: 16px; font-size: 0.9rem; color: #6b6b82; }
</style>
