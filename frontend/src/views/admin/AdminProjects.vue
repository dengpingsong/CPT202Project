<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminApi } from '../../utils/api'
import { toast } from '../../utils/ui-feedback'

const loading = ref(true)
const projects = ref<any[]>([])
const showDetail = ref(false)
const selectedProject = ref<any>(null)

function normalizeStatus(status: string | null | undefined): string {
  const s = String(status || 'UNKNOWN').toUpperCase()
  return s === 'ARCHIVED' ? 'CLOSED' : s
}

function statusClass(status: string): string {
  const s = normalizeStatus(status)
  if (s === 'AVAILABLE') return 'status-available'
  if (s === 'REQUESTED') return 'status-requested'
  if (s === 'AGREED') return 'status-agreed'
  if (s === 'CLOSED') return 'status-unavailable'
  return 'status-unavailable'
}

function formatDate(value: string | null | undefined): string {
  if (!value) return '-'
  const date = new Date(value)
  if (isNaN(date.getTime())) return String(value).split('T')[0]
  return date.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  })
}

async function loadProjects() {
  loading.value = true
  try {
    const res = await adminApi.listProjects()
    projects.value = Array.isArray(res.data) ? res.data : []
  } catch (e: any) {
    toast.error(e.message || 'Failed to load projects')
    projects.value = []
  } finally {
    loading.value = false
  }
}

function openDetail(project: any) {
  selectedProject.value = project
  showDetail.value = true
}

onMounted(loadProjects)
</script>

<template>
  <div class="page">
    <header class="page-header">
      <h1>Project Overview</h1>
      <span class="badge">{{ projects.length }}</span>
    </header>

    <div class="panel">
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Project Name</th>
              <th>Teacher</th>
              <th>Category</th>
              <th>Tags</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="6" style="text-align: center; color: #888;">Loading...</td>
            </tr>
            <tr v-else-if="projects.length === 0">
              <td colspan="6" style="text-align: center; color: #888;">No projects found</td>
            </tr>
            <tr v-for="p in projects" :key="p.projectId">
              <td><strong>{{ p.projectId }}</strong></td>
              <td>{{ p.title || '-' }}</td>
              <td>{{ p.teacherName || '-' }}</td>
              <td>{{ p.categoryName || '-' }}</td>
              <td>
                <span v-if="p.topicArea" class="tag-pill">{{ p.topicArea }}</span>
                <span v-else style="color: #6b6b82;">-</span>
              </td>
              <td>
                <button class="btn-sm btn-detail" @click="openDetail(p)">
                  <i class="bi bi-eye"></i> Detail
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="summary">{{ projects.length }} project(s)</div>
    </div>

    <!-- Detail Modal -->
    <Teleport to="body">
      <div v-if="showDetail" class="modal-overlay" @click.self="showDetail = false">
        <div class="modal-dialog">
          <div class="modal-header">
            <h2>Project Details</h2>
            <button class="icon-button" @click="showDetail = false">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
          <div v-if="selectedProject" class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">Project ID</span>
              <span class="detail-value">{{ selectedProject.projectId }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Teacher</span>
              <span class="detail-value">{{ selectedProject.teacherName || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Category</span>
              <span class="detail-value">{{ selectedProject.categoryName || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Topic Area</span>
              <span class="detail-value">{{ selectedProject.topicArea || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Required Skills</span>
              <span class="detail-value">{{ selectedProject.requiredSkills || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Quota</span>
              <span class="detail-value">{{ selectedProject.currentAgreedCount || 0 }}/{{ selectedProject.maxStudents || 0 }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Status</span>
              <span class="detail-value">
                <span class="status-pill" :class="statusClass(selectedProject.projectStatus)">
                  {{ normalizeStatus(selectedProject.projectStatus) }}
                </span>
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Publish Date</span>
              <span class="detail-value">{{ formatDate(selectedProject.publishDate) }}</span>
            </div>
          </div>
          <div v-if="selectedProject?.description" class="detail-block">
            <h3>Description</h3>
            <div class="detail-text">{{ selectedProject.description }}</div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }
.page-header { display: flex; align-items: center; gap: 12px; }
.page-header h1 { margin: 0; font-size: 1.8rem; font-weight: 600; color: #1c1b33; }
.badge { background: #5a2b98; color: #fff; padding: 4px 12px; border-radius: 999px; font-size: 0.85rem; font-weight: 600; }
.panel { background: #fff; border-radius: 28px; box-shadow: 0 20px 60px rgba(21, 16, 45, 0.15); padding: 24px 28px; }
.table-wrapper { overflow-x: auto; border-radius: 18px; }
.data-table { width: 100%; border-collapse: collapse; min-width: 800px; font-size: 0.9rem; }
.data-table th { text-align: left; padding: 16px; background: #f8f5ff; font-weight: 600; color: #5a2b98; border-bottom: 2px solid rgba(90, 43, 152, 0.2); }
.data-table td { padding: 16px; border-bottom: 1px solid rgba(156, 156, 178, 0.2); }
.data-table tbody tr:hover { background: rgba(90, 43, 152, 0.03); }
.tag-pill { display: inline-block; padding: 3px 10px; border-radius: 999px; font-size: 0.8rem; background: rgba(90, 43, 152, 0.08); color: #5a2b98; }
.status-pill { display: inline-block; padding: 4px 10px; border-radius: 999px; font-size: 0.8rem; font-weight: 600; }
.status-available { background: rgba(47, 197, 168, 0.12); color: #2fc5a8; }
.status-requested { background: rgba(246, 166, 61, 0.12); color: #f6a63d; }
.status-agreed { background: rgba(36, 179, 255, 0.15); color: #24b3ff; }
.status-unavailable { background: rgba(156, 156, 178, 0.2); color: rgba(28, 27, 51, 0.65); }
.btn-sm { padding: 6px 14px; border-radius: 20px; border: 1.5px solid #5a2b98; font-size: 0.8rem; font-weight: 600; cursor: pointer; font-family: inherit; background: transparent; color: #5a2b98; display: inline-flex; align-items: center; gap: 4px; transition: all 0.2s; }
.btn-sm:hover { background: #5a2b98; color: #fff; }
.summary { margin-top: 16px; font-size: 0.9rem; color: #6b6b82; }

/* Modal */
.modal-overlay { position: fixed; inset: 0; display: flex; align-items: center; justify-content: center; padding: 24px; background: rgba(28, 27, 51, 0.45); z-index: 20; }
.modal-dialog { width: min(600px, 100%); background: #fff; border-radius: 24px; border: 1px solid rgba(28, 27, 51, 0.08); box-shadow: 0 24px 80px rgba(21, 16, 45, 0.22); padding: 28px; max-height: 80vh; overflow-y: auto; }
.modal-header { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 20px; }
.modal-header h2 { margin: 0; color: #1c1b33; font-size: 1.35rem; font-weight: 600; }
.icon-button { width: 38px; height: 38px; border-radius: 50%; border: 1px solid rgba(90, 43, 152, 0.16); background: rgba(90, 43, 152, 0.06); color: #5a2b98; cursor: pointer; display: inline-flex; align-items: center; justify-content: center; font-size: 1.1rem; }
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; background: #f9f5ff; border-radius: 20px; padding: 18px 20px; }
.detail-item { display: flex; flex-direction: column; }
.detail-label { font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.5px; color: #6b6b82; font-weight: 600; }
.detail-value { font-weight: 600; color: #1c1b33; margin-top: 2px; }
.detail-block { margin-top: 20px; }
.detail-block h3 { font-weight: 600; font-size: 1rem; margin-bottom: 10px; color: #5a2b98; border-left: 4px solid #5a2b98; padding-left: 14px; }
.detail-text { background: #faf9ff; padding: 16px 20px; border-radius: 18px; line-height: 1.7; color: #1c1b33; border: 1px solid rgba(90, 43, 152, 0.15); }
</style>
