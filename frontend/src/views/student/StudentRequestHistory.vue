<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { studentApi, api } from '../../utils/api'
import { toast } from '../../utils/ui-feedback'

const loading = ref(true)
const requests = ref<any[]>([])
const historyGroups = ref<Record<string, any[]>>({})

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

function lastUpdate(r: any): string {
  return r.reviewedAt || r.withdrawnAt || r.submittedAt
}

function fallbackHistory(r: any): any[] {
  return [{
    requestId: r.requestId,
    newStatus: normalizeStatus(r.requestStatus),
    remark: r.decisionComment || r.notes || '',
    changedAt: lastUpdate(r),
  }]
}

interface TimelineEntry {
  requestId: number
  projectId: number
  projectTitle: string
  status: string
  submittedAt: string
  preferenceRank: number
  notes: string
  decisionComment: string
  remark: string
  changedByStudentName: string
  changedAt: string
}

const timelineEntries = computed<TimelineEntry[]>(() => {
  const entries: TimelineEntry[] = []
  requests.value.forEach(r => {
    const histories = historyGroups.value[r.requestId] || fallbackHistory(r)
    histories.forEach(h => {
      entries.push({
        requestId: r.requestId,
        projectId: r.projectId,
        projectTitle: r.projectTitle || 'Untitled Project',
        status: h.newStatus || r.requestStatus,
        submittedAt: r.submittedAt,
        preferenceRank: r.preferenceRank,
        notes: r.notes,
        decisionComment: r.decisionComment,
        remark: h.remark,
        changedByStudentName: h.changedByStudentName,
        changedAt: h.changedAt || lastUpdate(r),
      })
    })
  })
  return entries.sort((a, b) => new Date(b.changedAt || 0).getTime() - new Date(a.changedAt || 0).getTime())
})

async function loadHistories(reqs: any[]) {
  const groups: Record<string, any[]> = {}
  for (const r of reqs) {
    try {
      const res = await api.get(`/student/request-history/${r.requestId}`)
      const histories = res.data
      if (Array.isArray(histories) && histories.length > 0) {
        groups[r.requestId] = histories
      }
    } catch {
      // fallback will be used
    }
  }
  historyGroups.value = groups
}

async function init() {
  loading.value = true
  try {
    const reqRes = await studentApi.getRequests()
    requests.value = Array.isArray(reqRes.data) ? reqRes.data : []
    await loadHistories(requests.value)
  } catch (e: any) {
    toast.error(e.message || 'Failed to load request history')
  } finally {
    loading.value = false
  }
}

onMounted(init)
</script>

<template>
  <div class="history-page">
    <header class="page-header">
      <h1>History</h1>
    </header>

    <div class="panel">
      <div v-if="loading" class="timeline-vertical">
        <div class="item">
          <div class="timeline-title">Loading...</div>
          <div class="timeline-desc">Please wait</div>
        </div>
      </div>

      <div v-else-if="timelineEntries.length === 0" class="timeline-vertical">
        <div class="item">
          <div class="timeline-title">No request history</div>
          <div class="timeline-desc">Status changes will appear here after you submit applications.</div>
        </div>
      </div>

      <div v-else class="timeline-vertical">
        <div v-for="entry in timelineEntries" :key="`${entry.requestId}-${entry.changedAt}`" class="item">
          <div class="timeline-date">{{ formatDate(entry.changedAt) }}</div>
          <div class="timeline-title" :style="{ color: statusColor(entry.status) }">
            {{ entry.projectTitle }} · {{ statusText(entry.status) }}
          </div>
          <div class="timeline-desc">
            <div>Submitted: {{ formatDate(entry.submittedAt) }}</div>
            <div>Rank: {{ entry.preferenceRank || '-' }}</div>
            <div v-if="entry.notes">Notes: {{ entry.notes }}</div>
            <div v-if="entry.remark">Status Note: {{ entry.remark }}</div>
            <div v-if="entry.decisionComment">Feedback: {{ entry.decisionComment }}</div>
            <div v-if="entry.changedByStudentName">By: {{ entry.changedByStudentName }}</div>
            <router-link
              :to="`/student/projects/${entry.projectId}`"
              style="color: var(--deep); font-weight: 600; text-decoration: none; display: inline-block; margin-top: 6px;"
            >
              View Project Details
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.history-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  padding: 0;
}

.page-header h1 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 600;
  color: var(--text);
}

.panel {
  background: #fff;
  border: 1px solid rgba(90, 43, 152, 0.16);
  border-radius: 18px;
  padding: 24px 28px;
  box-shadow: 0 12px 30px rgba(90, 43, 152, 0.05);
}

.timeline-vertical {
  position: relative;
  padding-left: 28px;
  margin-top: 10px;
}

.timeline-vertical::before {
  content: "";
  position: absolute;
  left: 7px;
  top: 8px;
  bottom: 24px;
  width: 2px;
  background: #e4e1eb;
}

.timeline-vertical .item {
  position: relative;
  padding-bottom: 28px;
}

.timeline-vertical .item:last-child {
  padding-bottom: 0;
}

.timeline-vertical .item::before {
  content: "";
  position: absolute;
  left: -26.5px;
  top: 2px;
  width: 13px;
  height: 13px;
  border-radius: 50%;
  background: var(--mid);
}

.timeline-date {
  font-size: 0.85rem;
  color: var(--muted);
  margin-bottom: 6px;
}

.timeline-title {
  font-size: 1.1rem;
  font-weight: 700;
  margin-bottom: 6px;
}

.timeline-desc {
  color: #444;
  line-height: 1.6;
  font-size: 0.9rem;
}

.timeline-desc div {
  margin-bottom: 2px;
}
</style>
