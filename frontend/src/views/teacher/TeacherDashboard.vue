<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { teacherApi } from '../../utils/api'
import { toast, confirm } from '../../utils/ui-feedback'

const router = useRouter()
const loading = ref(true)
const requests = ref<any[]>([])
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

function statusText(status: string): string {
  const map: Record<string, string> = {
    PENDING: 'Pending',
    ACCEPTED: 'Accepted',
    REJECTED: 'Rejected',
    WITHDRAWN: 'Withdrawn',
  }
  return map[normalizeStatus(status)] || normalizeStatus(status)
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

const filteredRequests = computed(() => {
  if (!statusFilter.value) return requests.value
  return requests.value.filter(r => normalizeStatus(r.requestStatus) === statusFilter.value)
})

async function loadRequests() {
  loading.value = true
  try {
    const res = await teacherApi.listRequests()
    requests.value = Array.isArray(res.data) ? res.data : []
  } catch (e: any) {
    toast.error(e.message || 'Failed to load requests')
    requests.value = []
  } finally {
    loading.value = false
  }
}

function viewDetail(requestId: number) {
  router.push(`/teacher/request/${requestId}`)
}

async function handleAccept(requestId: number) {
  const confirmed = await confirm('Are you sure you want to accept this application?')
  if (!confirmed) return
  try {
    await teacherApi.reviewRequest(requestId, 'ACCEPTED')
    toast.success('Application accepted')
    await loadRequests()
  } catch (e: any) {
    toast.error(e.message || 'Failed to accept')
  }
}

async function handleReject(requestId: number) {
  const comment = window.prompt('Enter rejection reason (optional):')
  if (comment === null) return
  try {
    await teacherApi.reviewRequest(requestId, 'REJECTED', comment)
    toast.success('Application rejected')
    await loadRequests()
  } catch (e: any) {
    toast.error(e.message || 'Failed to reject')
  }
}

onMounted(loadRequests)
</script>

<template>
  <div class="dashboard-page">
    <header class="page-header">
      <h1>Review Requests</h1>
    </header>

    <div class="panel">
      <div class="filters-row">
        <label class="filter-field">
          Status
          <select v-model="statusFilter">
            <option value="">All Status</option>
            <option value="PENDING">Pending</option>
            <option value="ACCEPTED">Accepted</option>
            <option value="REJECTED">Rejected</option>
            <option value="WITHDRAWN">Withdrawn</option>
          </select>
        </label>
        <button class="clear-btn" @click="statusFilter = ''">Clear</button>
      </div>

      <div class="table-wrapper">
        <table class="request-table">
          <thead>
            <tr>
              <th>Student</th>
              <th>Project</th>
              <th>Submitted</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="5" style="text-align: center; color: #888;">Loading...</td>
            </tr>
            <tr v-else-if="filteredRequests.length === 0">
              <td colspan="5" style="text-align: center; color: #888;">No requests found</td>
            </tr>
            <tr v-for="r in filteredRequests" :key="r.requestId">
              <td>
                <div class="student-info">
                  <span class="student-name">{{ r.studentName || '-' }}</span>
                  <span class="student-id">{{ r.studentId || '' }}</span>
                </div>
              </td>
              <td>{{ r.projectTitle || '-' }}</td>
              <td>{{ formatDate(r.submittedAt) }}</td>
              <td>
                <span class="status-pill" :style="{ color: statusColor(r.requestStatus), background: statusColor(r.requestStatus) + '18' }">
                  {{ statusText(r.requestStatus) }}
                </span>
              </td>
              <td>
                <div class="action-btns">
                  <button class="btn-sm btn-detail" @click="viewDetail(r.requestId)">
                    <i class="bi bi-eye"></i> Detail
                  </button>
                  <button
                    v-if="normalizeStatus(r.requestStatus) === 'PENDING'"
                    class="btn-sm btn-accept"
                    @click="handleAccept(r.requestId)"
                  >
                    <i class="bi bi-check-lg"></i> Accept
                  </button>
                  <button
                    v-if="normalizeStatus(r.requestStatus) === 'PENDING'"
                    class="btn-sm btn-reject"
                    @click="handleReject(r.requestId)"
                  >
                    <i class="bi bi-x-lg"></i> Reject
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="summary">{{ filteredRequests.length }} request(s)</div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 600;
  color: #1c1b33;
}

.panel {
  background: #fff;
  border-radius: 28px;
  box-shadow: 0 20px 60px rgba(21, 16, 45, 0.15);
  padding: 24px 28px;
}

.filters-row {
  display: flex;
  gap: 10px;
  align-items: end;
  margin-bottom: 16px;
}

.filter-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 0.9rem;
  color: #6b6b82;
}

.filter-field select {
  padding: 10px 12px;
  border: 1px solid rgba(90, 43, 152, 0.18);
  border-radius: 12px;
  font: inherit;
  background: #fff;
  outline: none;
}

.filter-field select:focus {
  border-color: #5a2b98;
  box-shadow: 0 0 0 3px rgba(90, 43, 152, 0.14);
}

.clear-btn {
  padding: 10px 14px;
  border-radius: 12px;
  border: 1px solid rgba(90, 43, 152, 0.16);
  background: linear-gradient(180deg, #fff, #f7f2ff);
  color: #5a2b98;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  font-size: 0.9rem;
}

.clear-btn:hover {
  background: linear-gradient(180deg, #fff, #efe4ff);
}

.table-wrapper {
  overflow-x: auto;
  border-radius: 18px;
}

.request-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 700px;
  font-size: 0.9rem;
}

.request-table th {
  text-align: left;
  padding: 16px;
  background: #f8f5ff;
  font-weight: 600;
  color: #5a2b98;
  border-bottom: 2px solid rgba(90, 43, 152, 0.2);
}

.request-table td {
  padding: 16px;
  border-bottom: 1px solid rgba(156, 156, 178, 0.2);
}

.request-table tbody tr:hover {
  background: rgba(90, 43, 152, 0.03);
}

.student-info {
  display: flex;
  flex-direction: column;
}

.student-name {
  font-weight: 600;
  color: #1c1b33;
}

.student-id {
  font-size: 0.8rem;
  color: #6b6b82;
}

.status-pill {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 600;
}

.action-btns {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.btn-sm {
  padding: 6px 14px;
  border-radius: 20px;
  border: 1.5px solid;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s;
}

.btn-detail {
  border-color: #5a2b98;
  color: #5a2b98;
  background: transparent;
}

.btn-detail:hover {
  background: #5a2b98;
  color: #fff;
}

.btn-accept {
  border-color: #2fc5a8;
  color: #2fc5a8;
  background: transparent;
}

.btn-accept:hover {
  background: #2fc5a8;
  color: #fff;
}

.btn-reject {
  border-color: #c74545;
  color: #c74545;
  background: transparent;
}

.btn-reject:hover {
  background: #c74545;
  color: #fff;
}

.summary {
  margin-top: 16px;
  font-size: 0.9rem;
  color: #6b6b82;
}
</style>
