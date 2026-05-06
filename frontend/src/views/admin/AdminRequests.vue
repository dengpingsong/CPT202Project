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
  if (s === 'PENDING') return 'var(--orange)'
  if (s === 'ACCEPTED') return 'var(--green)'
  if (s === 'REJECTED') return 'var(--red)'
  if (s === 'WITHDRAWN') return 'var(--muted)'
  return 'var(--deep)'
}

function formatDate(value: string | null | undefined): string {
  if (!value) return '-'
  const date = new Date(value)
  if (isNaN(date.getTime())) return String(value).replace('T', ' ').slice(0, 16)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const filteredRecords = computed(() => {
  if (!statusFilter.value) return records.value
  return records.value.filter(
    (r) => normalizeStatus(r.requestStatus) === statusFilter.value,
  )
})

async function loadData() {
  loading.value = true
  try {
    const res = await adminApi.listRequestRecords(
      statusFilter.value || undefined,
    )
    records.value = Array.isArray(res.data) ? res.data : []
  } catch (e: any) {
    toast.error(e.message || 'Failed to load requests')
    records.value = []
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  statusFilter.value = ''
  void loadData()
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
        <span class="filter-control select-control">
          <select
            v-model="statusFilter"
            aria-label="Request Status"
            @change="loadData()"
          >
            <option value="">All Request Status</option>
            <option value="PENDING">Pending</option>
            <option value="ACCEPTED">Accepted</option>
            <option value="REJECTED">Rejected</option>
            <option value="WITHDRAWN">Withdrawn</option>
          </select>
        </span>
        <button class="clear-btn" @click="resetFilters">
          <i class="bi bi-arrow-counterclockwise"></i>
          Clear
        </button>
      </div>

      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>Request ID</th>
              <th>Project</th>
              <th>Student</th>
              <th>Request Status</th>
              <th>Rank</th>
              <th>Submitted</th>
              <th>Decision Comment</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="7" style="text-align: center; color: #888">
                Loading...
              </td>
            </tr>
            <tr v-else-if="filteredRecords.length === 0">
              <td colspan="7" style="text-align: center; color: #888">
                No records found
              </td>
            </tr>
            <tr v-for="r in filteredRecords" :key="r.requestId">
              <td>{{ r.requestId }}</td>
              <td>{{ r.projectTitle || '-' }}</td>
              <td>{{ r.studentName || '-' }}</td>
              <td>
                <span
                  class="status-pill"
                  :style="{
                    color: statusColor(r.requestStatus),
                    background: statusColor(r.requestStatus) + '18',
                  }"
                >
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
.page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.page-header h1 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 600;
  color: var(--text);
}
.filters-row {
  display: grid;
  grid-template-columns: minmax(170px, 0.9fr) auto;
  gap: 12px;
  align-items: end;
  margin-bottom: 16px;
  max-width: 360px;
}
.filter-control {
  min-height: 44px;
  border: 1.5px solid rgba(90, 43, 152, 0.16);
  border-radius: 12px;
  background: #fff;
  display: flex;
  align-items: center;
  transition:
    border-color 0.15s ease,
    box-shadow 0.15s ease;
}
.filter-control:focus-within {
  border-color: var(--deep);
  box-shadow: 0 0 0 3px rgba(90, 43, 152, 0.08);
}
.filter-control select {
  width: 100%;
  min-width: 0;
  border: 0;
  outline: none;
  background: transparent;
  color: var(--text);
  font: inherit;
  font-size: 0.95rem;
}
.select-control select {
  height: 42px;
  padding: 0 12px;
  cursor: pointer;
}
.clear-btn {
  min-height: 44px;
  padding: 0 16px;
  border-radius: 12px;
  border: 1.5px solid rgba(90, 43, 152, 0.16);
  background: #fff;
  color: var(--deep);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  font-family: inherit;
  font-size: 0.9rem;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
}
.clear-btn:hover {
  border-color: var(--deep);
  background: rgba(90, 43, 152, 0.06);
}
.table-wrapper {
  overflow-x: auto;
  border-radius: 18px;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
  font-size: 0.9rem;
}
.data-table th {
  text-align: left;
  padding: 16px;
  background: #f8f5ff;
  font-weight: 600;
  color: var(--deep);
  border-bottom: 2px solid rgba(90, 43, 152, 0.2);
}
.data-table td {
  padding: 16px;
  border-bottom: 1px solid rgba(156, 156, 178, 0.2);
}
.data-table tbody tr:hover {
  background: rgba(90, 43, 152, 0.03);
}
.status-pill {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 600;
}
.summary {
  margin-top: 16px;
  font-size: 0.9rem;
  color: var(--muted);
}
@media (max-width: 560px) {
  .filters-row {
    grid-template-columns: 1fr;
    max-width: none;
  }
}
</style>
