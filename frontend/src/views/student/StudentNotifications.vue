<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { studentApi } from '../../utils/api'
import { toast } from '../../utils/ui-feedback'

const loading = ref(true)
const notifications = ref<any[]>([])

function formatDate(value: string | null | undefined): string {
  if (!value) return '-'
  const date = new Date(value)
  if (isNaN(date.getTime())) return String(value).slice(0, 16)
  return date.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  })
}

function getIcon(status: string): string {
  const s = String(status || '').toUpperCase()
  if (s === 'ACCEPTED') return 'bi-check-circle'
  if (s === 'REJECTED') return 'bi-x-circle'
  if (s === 'WITHDRAWN') return 'bi-arrow-counterclockwise'
  return 'bi-bell'
}

function getIconColor(status: string): string {
  const s = String(status || '').toUpperCase()
  if (s === 'ACCEPTED') return 'var(--green)'
  if (s === 'REJECTED') return 'var(--red)'
  if (s === 'WITHDRAWN') return 'var(--muted)'
  return 'var(--deep)'
}

async function init() {
  loading.value = true
  try {
    const reqRes = await studentApi.getRequests()
    const reqs = Array.isArray(reqRes.data) ? reqRes.data : []
    // Build notifications from requests that have been reviewed
    notifications.value = reqs
      .filter(r => r.reviewedAt || r.withdrawnAt)
      .sort((a, b) => new Date(b.reviewedAt || b.withdrawnAt || 0).getTime() - new Date(a.reviewedAt || a.withdrawnAt || 0).getTime())
  } catch (e: any) {
    toast.error(e.message || 'Failed to load notifications')
  } finally {
    loading.value = false
  }
}

onMounted(init)
</script>

<template>
  <div class="notifications-page">
    <header class="page-header">
      <h1>Notifications</h1>
    </header>

    <div v-if="loading" class="panel" style="text-align: center; padding: 40px; color: var(--muted);">
      Loading...
    </div>

    <div v-else-if="notifications.length === 0" class="panel" style="text-align: center; padding: 40px; color: var(--muted);">
      No notifications yet.
    </div>

    <div v-else class="notification-list">
      <div v-for="n in notifications" :key="n.requestId" class="notification-item" :class="{ unread: !n.readAt }">
        <div class="notification-icon">
          <i :class="`bi ${getIcon(n.requestStatus)}`" :style="{ color: getIconColor(n.requestStatus) }"></i>
        </div>
        <div>
          <div class="notification-title">
            {{ n.projectTitle || 'Untitled Project' }}
            <span v-if="!n.readAt" class="unread-dot"></span>
          </div>
          <div class="notification-message">
            Status: {{ n.requestStatus }}
            <span v-if="n.decisionComment"> — {{ n.decisionComment }}</span>
          </div>
          <div class="notification-meta">
            <span>{{ formatDate(n.reviewedAt || n.withdrawnAt) }}</span>
            <span>Rank: {{ n.preferenceRank || '-' }}</span>
          </div>
          <router-link :to="`/student/projects/${n.projectId}`" class="detail-link">View Project</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.notifications-page {
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

.panel {
  background: #fff;
  border-radius: 28px;
  padding: 24px 28px;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.notification-item {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 16px;
  align-items: flex-start;
  padding: 18px 20px;
  border-radius: 22px;
  border: 1px solid rgba(90, 43, 152, 0.1);
  background: #fcfbff;
  transition: transform 0.2s, box-shadow 0.2s;
}

.notification-item:hover {
  transform: translateY(-1px);
}

.notification-item.unread {
  background: linear-gradient(180deg, rgba(90, 43, 152, 0.05), rgba(36, 179, 255, 0.03));
}

.notification-icon {
  width: 46px;
  height: 46px;
  border-radius: 16px;
  background: rgba(90, 43, 152, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
}

.notification-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 1rem;
  font-weight: 700;
  color: var(--text);
}

.notification-message {
  color: var(--text);
  line-height: 1.65;
  font-size: 0.9rem;
}

.notification-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-top: 10px;
  color: var(--muted);
  font-size: 0.84rem;
}

.unread-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #ff6b6b;
  display: inline-block;
}

.detail-link {
  color: var(--deep);
  font-weight: 600;
  text-decoration: none;
  font-size: 0.85rem;
  margin-top: 8px;
  display: inline-block;
}

.detail-link:hover { text-decoration: underline; }
</style>
