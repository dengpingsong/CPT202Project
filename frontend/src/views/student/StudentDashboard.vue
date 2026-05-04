<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { studentApi, getCurrentUser } from '../../utils/api'
import { toast, confirm } from '../../utils/ui-feedback'

const router = useRouter()

const loading = ref(true)
const requests = ref<any[]>([])
const projects = ref<any[]>([])

const user = getCurrentUser()
const displayName = computed(() => user.fullName || user.username || 'Student')

const pending = computed(() => requests.value.filter(r => normalizeStatus(r.requestStatus) === 'PENDING').length)
const accepted = computed(() => requests.value.filter(r => normalizeStatus(r.requestStatus) === 'ACCEPTED').length)
const rejected = computed(() => requests.value.filter(r => normalizeStatus(r.requestStatus) === 'REJECTED').length)
const withdrawn = computed(() => requests.value.filter(r => normalizeStatus(r.requestStatus) === 'WITHDRAWN').length)
const activeCount = computed(() => pending.value + accepted.value)

const statusSummary = computed(() => {
  const parts: string[] = []
  if (pending.value > 0) parts.push(`${pending.value} pending`)
  if (accepted.value > 0) parts.push(`${accepted.value} accepted`)
  if (rejected.value > 0) parts.push(`${rejected.value} rejected`)
  if (withdrawn.value > 0) parts.push(`${withdrawn.value} withdrawn`)
  return parts.length ? parts.join('  ') : '0 request'
})

const statusRule = computed(() => {
  if (accepted.value > 0) return `You have ${accepted.value} accepted project(s), new applications may be restricted`
  if (pending.value > 0 || rejected.value > 0) return `${pending.value} pending, ${rejected.value} rejected`
  return 'No pending or accepted applications'
})

const requestCountRule = computed(() => {
  if (accepted.value > 0) return 'If a project is closed or cancelled, status will update automatically'
  if (pending.value > 0 || rejected.value > 0) return 'You can continue browsing projects and re-arrange your preferences'
  return 'Start by browsing recommended projects or searching'
})

const withdrawnProjectIds = computed(() => {
  return new Set(
    requests.value
      .filter(r => normalizeStatus(r.requestStatus) === 'WITHDRAWN')
      .map(r => String(r.projectId))
  )
})

const recommendedProjects = computed(() => {
  return projects.value
    .filter(p => ['AVAILABLE', 'REQUESTED'].includes(normalizeStatus(p.projectStatus)))
    .filter(p => !withdrawnProjectIds.value.has(String(p.projectId)))
    .slice(0, 3)
})

const recentRequests = computed(() => {
  return [...requests.value]
    .sort((a, b) => {
      const rankDiff = Number(a.preferenceRank || 999) - Number(b.preferenceRank || 999)
      if (rankDiff !== 0) return rankDiff
      return new Date(b.submittedAt || 0).getTime() - new Date(a.submittedAt || 0).getTime()
    })
    .slice(0, 3)
})

function normalizeStatus(status: string | null | undefined): string {
  return String(status || 'UNKNOWN').toUpperCase()
}

function statusClass(status: string): string {
  const s = normalizeStatus(status)
  if (s === 'AVAILABLE') return 'status-available'
  if (s === 'PENDING' || s === 'REQUESTED') return 'status-requested'
  if (s === 'ACCEPTED' || s === 'AGREED') return 'status-agreed'
  if (s === 'REJECTED') return 'status-rejected'
  return 'status-unavailable'
}

function formatDate(value: string | null | undefined): string {
  if (!value) return '-'
  const date = new Date(value)
  if (isNaN(date.getTime())) return String(value).slice(0, 10)
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

function getProjectRequestStatus(projectId: number | string): string | null {
  const matched = requests.value.find(
    r => String(r.projectId) === String(projectId) && ['PENDING', 'REQUESTED'].includes(normalizeStatus(r.requestStatus))
  )
  return matched ? normalizeStatus(matched.requestStatus) : null
}

async function withdrawRequest(requestId: number | string) {
  const confirmed = await confirm('Are you sure you want to withdraw this application?')
  if (!confirmed) return
  try {
    await studentApi.withdrawRequest(requestId)
    toast.success('Application withdrawn')
    await loadData()
  } catch (e: any) {
    toast.error(e.message || 'Failed to withdraw')
  }
}

async function loadData() {
  loading.value = true
  try {
    const [reqData, projData] = await Promise.all([
      studentApi.getRequests(),
      studentApi.getProjects(1, 12),
    ])
    requests.value = Array.isArray(reqData.data) ? reqData.data : []
    projects.value = Array.isArray(projData.data?.records) ? projData.data.records : []
  } catch (e: any) {
    toast.error(e.message || 'Failed to load dashboard data')
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="dashboard">
    <!-- Hero -->
    <section class="hero">
      <div>
        <h1>Welcome, {{ displayName }}</h1>
        <p class="hero-info">
          This is your student workspace. You can view current applications, search for projects published by supervisors, or check past request records.
        </p>
        <span class="badge">Active applications: {{ loading ? 'Loading...' : activeCount }}</span>
      </div>
      <button class="cta" @click="router.push('/student/projects')">
        Quick Apply
        <small>Prioritize projects matching your skills</small>
      </button>
    </section>

    <!-- Stats -->
    <section class="stats">
      <div class="panel">
        <h3>Current Status</h3>
        <strong>{{ loading ? 'Loading...' : statusSummary }}</strong>
        <p class="rule">{{ statusRule }}</p>
      </div>
      <div class="panel">
        <h3>Accepted Projects</h3>
        <strong>{{ accepted }}</strong>
        <p class="rule">If already accepted, new application buttons will be disabled</p>
      </div>
      <div class="panel">
        <h3>Total Applications</h3>
        <strong>{{ requests.length }}</strong>
        <p class="rule">{{ requestCountRule }}</p>
      </div>
    </section>

    <!-- Recommended Projects -->
    <section>
      <h2>Recommended Projects</h2>
      <div class="grid">
        <template v-if="recommendedProjects.length > 0">
          <article v-for="project in recommendedProjects" :key="project.projectId" class="card">
            <span class="status-pill" :class="statusClass(getProjectRequestStatus(project.projectId) || project.projectStatus)">
              {{ getProjectRequestStatus(project.projectId) || project.projectStatus }}
            </span>
            <h4>{{ project.title || 'Untitled Project' }}</h4>
            <p class="meta">Category: {{ project.categoryName || '-' }} | Quota: {{ project.currentAgreedCount || 0 }}/{{ project.maxStudents || 0 }}</p>
            <p class="meta">Supervisor: {{ project.teacherName || '-' }} | Area: {{ project.topicArea || '-' }}</p>
            <button
              :class="{ secondary: !getProjectRequestStatus(project.projectId) }"
              @click="router.push(`/student/projects/${project.projectId}`)"
            >
              {{ getProjectRequestStatus(project.projectId) ? 'View Application' : 'View Details' }}
            </button>
          </article>
        </template>
        <article v-else class="card">
          <h4>No recommended projects</h4>
          <p class="meta">No projects available at the moment</p>
          <button class="secondary" @click="router.push('/student/projects')">Browse Projects</button>
        </article>
      </div>
    </section>

    <!-- Recent Requests -->
    <section class="timeline">
      <h3>Recent Applications</h3>
      <template v-if="recentRequests.length > 0">
        <div v-for="item in recentRequests" :key="item.requestId" class="item">
          <div style="display: flex; flex-direction: column;">
            <strong style="margin-bottom: 6px;">{{ item.projectTitle || 'Untitled Project' }}</strong>
            <div class="meta-row">
              <span>Submitted: {{ formatDate(item.submittedAt) }}</span>
              <span>Updated: {{ formatDate(item.reviewedAt || item.withdrawnAt || item.submittedAt) }}</span>
              <span>Rank: {{ item.preferenceRank || '-' }}</span>
            </div>
            <p class="meta" style="margin-top: 8px;">Notes: {{ item.notes || '-' }}</p>
            <p v-if="item.decisionComment" class="meta" style="margin-top: 4px;">Feedback: {{ item.decisionComment }}</p>
            <div v-if="normalizeStatus(item.requestStatus) === 'PENDING'" style="margin-top: 10px;">
              <button class="withdraw-btn" @click="withdrawRequest(item.requestId)">Withdraw</button>
            </div>
          </div>
          <span class="status" :class="statusClass(item.requestStatus)">
            {{ normalizeStatus(item.requestStatus) }}
          </span>
        </div>
      </template>
      <div v-else class="item">
        <div style="display: flex; flex-direction: column;">
          <strong style="margin-bottom: 6px;">No applications yet</strong>
          <p class="meta" style="margin-top: 8px;">Your applications will appear here once submitted</p>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.hero, .panel, .card, .timeline {
  background: var(--panel, #fff);
  border-radius: 28px;
  padding: 24px 28px;
}

.hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 32px;
}

.hero h1 { margin: 0; font-size: 1.9rem; }
.hero-info { max-width: 520px; color: var(--muted); line-height: 1.6; }

.badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 0.85rem;
  background: rgba(36, 179, 255, 0.15);
  color: var(--accent);
}

.cta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  border: none;
  border-radius: 18px;
  padding: 14px 24px;
  background: linear-gradient(135deg, var(--accent), #0c76e3);
  color: white;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 12px 30px rgba(36, 179, 255, 0.4);
  font-family: inherit;
}

.cta small {
  font-weight: 400;
  opacity: 0.85;
  font-size: 0.8rem;
}

.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 16px;
}

.stats .panel { padding: 20px 24px; }

.panel h3 {
  margin: 0 0 12px;
  font-size: 0.95rem;
  color: var(--muted);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.panel strong { font-size: 2rem; display: block; margin-bottom: 6px; }
.panel .rule { font-size: 0.8rem; color: var(--muted); }

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 24px;
  margin-top: 16px;
}

.card {
  padding: 22px;
  min-height: 210px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card h4 { margin: 0; font-size: 1.2rem; }
.card .meta { font-size: 0.85rem; color: var(--muted); line-height: 1.5; }

.status-pill {
  align-self: flex-start;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status-available { background: rgba(47, 197, 168, 0.12); color: var(--green); }
.status-requested { background: rgba(246, 166, 61, 0.12); color: var(--orange); }
.status-agreed { background: rgba(36, 179, 255, 0.15); color: var(--accent); }
.status-rejected { background: rgba(199, 69, 69, 0.12); color: var(--red); }
.status-unavailable { background: rgba(156, 156, 178, 0.2); color: rgba(28, 27, 51, 0.65); }

.card button {
  margin-top: auto;
  border: none;
  border-radius: 12px;
  padding: 12px 18px;
  background: var(--deep);
  color: white;
  cursor: pointer;
  font-weight: 600;
  font-family: inherit;
  transition: transform 0.2s ease;
}

.card button.secondary {
  background: #e8e8f8;
  color: #3c2e7b;
}

.card button:active { transform: translateY(1px); }

h2 { margin: 0 0 8px; font-size: 1.4rem; color: var(--text); }
h3 { margin: 0 0 16px; font-size: 1.1rem; color: var(--text); }

.timeline .item {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid rgba(156, 156, 178, 0.25);
}

.timeline .item:last-child { border-bottom: none; }

.timeline .meta-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  font-size: 0.85rem;
  color: var(--muted);
}

.timeline .status {
  font-weight: 600;
  align-self: center;
  text-transform: uppercase;
  font-size: 0.85rem;
}

.timeline .meta { font-size: 0.85rem; color: var(--muted); }

.withdraw-btn {
  padding: 8px 14px;
  border-radius: 999px;
  border: 1.5px solid #f56c6c;
  color: #f56c6c;
  background: transparent;
  cursor: pointer;
  font-weight: 600;
  font-family: inherit;
  font-size: 0.85rem;
}

.withdraw-btn:hover {
  background: #f56c6c;
  color: white;
}

@media (max-width: 960px) {
  .hero { flex-direction: column; align-items: flex-start; }
}
</style>
