<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { studentApi, api } from '../../utils/api'
import { toast, confirm } from '../../utils/ui-feedback'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const project = ref<any>(null)
const requests = ref<any[]>([])
const preferenceRank = ref(1)
const notes = ref('')
const submitting = ref(false)

const projectId = computed(() => route.params.id as string)

const activeRequest = computed(() => {
  return requests.value.find(
    r => String(r.projectId) === String(projectId.value)
      && ['PENDING', 'ACCEPTED', 'REJECTED'].includes(normalizeStatus(r.requestStatus))
  )
})

const hasAccepted = computed(() => {
  return requests.value.some(r => normalizeStatus(r.requestStatus) === 'ACCEPTED')
})

const displayStatus = computed(() => {
  if (!project.value) return 'UNKNOWN'
  if (activeRequest.value) {
    const s = normalizeStatus(activeRequest.value.requestStatus)
    return s === 'PENDING' ? 'REQUESTED' : s
  }
  return normalizeStatus(project.value.projectStatus)
})

const buttonState = computed(() => {
  if (!project.value) return { disabled: true, text: 'Loading...', hint: '', canWithdraw: false }

  const existing = activeRequest.value
  const existingStatus = normalizeStatus(existing?.requestStatus)

  if (existingStatus === 'REJECTED') {
    return { disabled: true, text: 'Rejected', hint: 'This application was rejected. You cannot re-apply.', canWithdraw: false }
  }
  if (existing) {
    return {
      disabled: true,
      text: existingStatus === 'ACCEPTED' ? 'Accepted' : 'Already Applied',
      hint: 'You already have an active application for this project.',
      canWithdraw: existingStatus === 'PENDING',
      requestId: existing.requestId,
    }
  }
  if (hasAccepted.value) {
    return { disabled: true, text: 'Already Accepted', hint: 'You have an accepted project. Cannot apply to more.', canWithdraw: false }
  }
  if (normalizeStatus(project.value.projectStatus) !== 'AVAILABLE') {
    return { disabled: true, text: 'Not Available', hint: 'This project is not accepting applications.', canWithdraw: false }
  }
  const current = Number(project.value.currentAgreedCount || 0)
  const max = Number(project.value.maxStudents || 0)
  if (max > 0 && current >= max) {
    return { disabled: true, text: 'Full', hint: 'This project has reached its maximum capacity.', canWithdraw: false }
  }
  return { disabled: false, text: 'Apply', hint: '', canWithdraw: false }
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

function statusText(status: string): string {
  const map: Record<string, string> = {
    AVAILABLE: 'Available',
    REQUESTED: 'Requested',
    ACCEPTED: 'Accepted',
    REJECTED: 'Rejected',
    AGREED: 'Agreed',
    CLOSED: 'Closed',
    ARCHIVED: 'Closed',
  }
  return map[normalizeStatus(status)] || status || 'Unknown'
}

function formatDate(v: string | null | undefined): string {
  if (!v) return '-'
  try { return new Date(v).toLocaleString() } catch { return String(v) }
}

function hasDuplicateRank(rank: number): boolean {
  return requests.value.some(
    r => ['PENDING', 'ACCEPTED'].includes(normalizeStatus(r.requestStatus))
      && Number(r.preferenceRank) === rank
  )
}

async function handleApply() {
  if (buttonState.value.disabled || !project.value) return
  if (hasDuplicateRank(preferenceRank.value)) {
    toast.error('This preference rank is already used. Choose another.')
    return
  }
  submitting.value = true
  try {
    await api.post('/student/requests', {
      projectId: project.value.projectId,
      preferenceRank: preferenceRank.value,
      notes: notes.value.trim(),
    })
    toast.success('Application submitted successfully')
    router.push('/student/projects')
  } catch (e: any) {
    toast.error(e.message || 'Failed to submit application')
  } finally {
    submitting.value = false
  }
}

async function handleWithdraw() {
  if (!buttonState.value.requestId) return
  const confirmed = await confirm('Are you sure you want to withdraw this application?')
  if (!confirmed) return
  submitting.value = true
  try {
    await studentApi.withdrawRequest(buttonState.value.requestId)
    toast.success('Application withdrawn')
    await loadData()
  } catch (e: any) {
    toast.error(e.message || 'Failed to withdraw')
  } finally {
    submitting.value = false
  }
}

async function loadData() {
  loading.value = true
  try {
    const [projRes, reqRes] = await Promise.all([
      api.get(`/student/projects/${projectId.value}`),
      studentApi.getRequests(),
    ])
    project.value = projRes.data
    requests.value = Array.isArray(reqRes.data) ? reqRes.data : []
  } catch (e: any) {
    toast.error(e.message || 'Failed to load project details')
    project.value = null
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="detail-page">
    <div class="page-header">
      <router-link class="back-link" to="/student/projects">
        <i class="bi bi-arrow-left"></i> Back to Projects
      </router-link>
      <h1>Project Details</h1>
    </div>

    <div v-if="loading" class="panel-card" style="text-align: center; padding: 40px; color: #6b6b82;">
      Loading...
    </div>

    <div v-else-if="!project" class="panel-card" style="text-align: center; padding: 40px;">
      Project not found.
      <router-link to="/student/projects">Back to list</router-link>
    </div>

    <template v-else>
      <div class="panel-card">
        <div class="detail-header">
          <h2>{{ project.title }}</h2>
          <span class="surf-badge">SURF-{{ project.projectId }}</span>
        </div>

        <div class="meta-grid">
          <div class="meta-item">
            <span class="meta-label">Project ID</span>
            <span class="meta-value">{{ project.projectId }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Teacher</span>
            <span class="meta-value">{{ project.teacherName || '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Category</span>
            <span class="meta-value">{{ project.categoryName || '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Topic Area</span>
            <span class="meta-value">{{ project.topicArea || '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Required Skills</span>
            <span class="meta-value">{{ project.requiredSkills || '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Quota</span>
            <span class="meta-value">{{ project.currentAgreedCount || 0 }}/{{ project.maxStudents || 0 }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Status</span>
            <span class="meta-value">
              <span class="status-pill" :class="statusClass(displayStatus)">{{ statusText(displayStatus) }}</span>
            </span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Publish Date</span>
            <span class="meta-value">{{ formatDate(project.publishDate) }}</span>
          </div>
        </div>

        <div class="detail-block">
          <h3>Description</h3>
          <div class="detail-text">{{ project.description || '-' }}</div>
        </div>

        <div v-if="!buttonState.disabled" class="detail-block">
          <h3>Application Notes</h3>
          <label class="field-label">Preference Rank</label>
          <select v-model.number="preferenceRank" class="rank-select">
            <option :value="1">1st Choice</option>
            <option :value="2">2nd Choice</option>
            <option :value="3">3rd Choice</option>
            <option :value="4">4th Choice</option>
            <option :value="5">5th Choice</option>
          </select>
          <label class="field-label">Additional Notes</label>
          <textarea
            v-model="notes"
            rows="4"
            class="notes-textarea"
            placeholder="Describe your interest, experience, or reason for applying"
          ></textarea>
        </div>

        <div class="action-buttons">
          <button
            class="btn-primary"
            :disabled="buttonState.disabled || submitting"
            @click="handleApply"
          >
            {{ submitting ? 'Submitting...' : buttonState.text }}
          </button>
          <button
            v-if="buttonState.canWithdraw"
            class="btn-withdraw"
            :disabled="submitting"
            @click="handleWithdraw"
          >
            {{ submitting ? 'Withdrawing...' : 'Withdraw' }}
          </button>
          <router-link class="btn-secondary" to="/student/projects">Back</router-link>
        </div>
        <div v-if="buttonState.hint" class="footnote">{{ buttonState.hint }}</div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.detail-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 600;
  color: #1c1b33;
}

.back-link {
  color: #5a2b98;
  text-decoration: none;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 30px;
  background: rgba(90, 43, 152, 0.1);
  transition: background 0.2s;
}

.back-link:hover { background: rgba(90, 43, 152, 0.2); text-decoration: none; }

.panel-card {
  background: #fff;
  border-radius: 28px;
  box-shadow: 0 20px 60px rgba(21, 16, 45, 0.15);
  padding: 24px 28px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.detail-header h2 {
  margin: 0;
  font-size: 1.6rem;
  color: #1c1b33;
}

.surf-badge {
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 0.85rem;
  font-weight: 600;
  background: rgba(36, 179, 255, 0.15);
  color: #24b3ff;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 16px 28px;
  background: #f9f5ff;
  border-radius: 20px;
  padding: 18px 20px;
  margin-bottom: 28px;
}

.meta-item {
  display: flex;
  flex-direction: column;
}

.meta-label {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: #6b6b82;
  font-weight: 600;
}

.meta-value {
  font-weight: 600;
  color: #1c1b33;
  margin-top: 2px;
}

.detail-block {
  margin-bottom: 28px;
}

.detail-block h3 {
  font-weight: 600;
  font-size: 1.2rem;
  margin-bottom: 12px;
  color: #5a2b98;
  border-left: 5px solid #5a2b98;
  padding-left: 18px;
}

.detail-text {
  background: #faf9ff;
  padding: 18px 22px;
  border-radius: 18px;
  line-height: 1.7;
  color: #1c1b33;
  border: 1px solid rgba(90, 43, 152, 0.15);
}

.field-label {
  display: block;
  font-weight: 600;
  margin-bottom: 8px;
  color: #1c1b33;
  font-size: 0.9rem;
}

.rank-select {
  width: 100%;
  max-width: 220px;
  padding: 10px 12px;
  border: 1.5px solid rgba(90, 43, 152, 0.18);
  border-radius: 12px;
  font-family: inherit;
  margin-bottom: 16px;
  background: #fff;
  outline: none;
}

.rank-select:focus {
  border-color: #5a2b98;
  box-shadow: 0 0 0 3px rgba(90, 43, 152, 0.14);
}

.notes-textarea {
  width: 100%;
  max-width: 720px;
  padding: 12px 14px;
  border: 1.5px solid rgba(90, 43, 152, 0.18);
  border-radius: 16px;
  font-family: inherit;
  resize: vertical;
  outline: none;
}

.notes-textarea:focus {
  border-color: #5a2b98;
  box-shadow: 0 0 0 3px rgba(90, 43, 152, 0.14);
}

.status-pill {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status-available { background: rgba(47, 197, 168, 0.12); color: #2fc5a8; }
.status-requested { background: rgba(246, 166, 61, 0.12); color: #f6a63d; }
.status-agreed { background: rgba(36, 179, 255, 0.15); color: #24b3ff; }
.status-rejected { background: rgba(199, 69, 69, 0.12); color: #c74545; }
.status-unavailable { background: rgba(156, 156, 178, 0.2); color: rgba(28, 27, 51, 0.65); }

.action-buttons {
  display: flex;
  gap: 16px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid rgba(156, 156, 178, 0.25);
}

.btn-primary {
  background: #5a2b98;
  color: white;
  border: none;
  padding: 12px 28px;
  border-radius: 40px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
}

.btn-primary:disabled {
  background: #c4c4e0;
  cursor: not-allowed;
}

.btn-secondary {
  background: transparent;
  border: 1.5px solid #6b6b82;
  color: #1c1b33;
  padding: 12px 24px;
  border-radius: 40px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
}

.btn-secondary:hover { background: #f4f3f7; text-decoration: none; }

.btn-withdraw {
  background: transparent;
  border: 1.5px solid #f56c6c;
  color: #f56c6c;
  padding: 12px 24px;
  border-radius: 40px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
}

.btn-withdraw:hover {
  background: #f56c6c;
  color: white;
}

.btn-withdraw:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.footnote {
  margin-top: 16px;
  color: #6b6b82;
  font-size: 0.8rem;
}
</style>
