<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { teacherApi } from '../../utils/api'
import { toast, confirm } from '../../utils/ui-feedback'

const router = useRouter()

const loading = ref(true)
const projects = ref<any[]>([])
const categories = ref<any[]>([])
const tags = ref<any[]>([])
const statusFilter = ref('')
const projectStatus = ref('')

// Edit panel
const showEditPanel = ref(false)
const editPanelStatus = ref('')
const editPanelStatusType = ref<'success' | 'error' | ''>('')
const saving = ref(false)

// Edit form fields
const editProjectId = ref('')
const editTitle = ref('')
const editMaxStudents = ref(1)
const editCategoryId = ref('')
const editTopicArea = ref('')
const editDescription = ref('')
const editRequiredSkills = ref('')
const editProjectStatus = ref('AVAILABLE')
const editStatusRemark = ref('')
const editSelectedTagIds = ref<Set<number>>(new Set())
const originalProjectStatus = ref('')
const originalTagIds = ref<number[]>([])

// Status confirm modal
const showStatusConfirm = ref(false)
const statusConfirmMessage = ref('')
let pendingConfirmAction: (() => Promise<void>) | null = null

function normalizeStatus(status: string | null | undefined): string {
  const s = String(status || 'UNKNOWN').toUpperCase()
  return s === 'ARCHIVED' ? 'CLOSED' : s
}

function statusText(status: string): string {
  const map: Record<string, string> = {
    AVAILABLE: 'Available',
    REQUESTED: 'Requested',
    AGREED: 'Agreed',
    CLOSED: 'Closed',
  }
  return map[normalizeStatus(status)] || normalizeStatus(status)
}

function statusClass(status: string): string {
  const s = normalizeStatus(status)
  if (s === 'AVAILABLE') return 'status-available'
  if (s === 'REQUESTED') return 'status-requested'
  if (s === 'AGREED') return 'status-agreed'
  if (s === 'CLOSED') return 'status-unavailable'
  return 'status-requested'
}

function setEditStatus(msg: string, type: 'success' | 'error' | '') {
  editPanelStatus.value = msg
  editPanelStatusType.value = type
}

async function loadProjects() {
  loading.value = true
  try {
    const res = await teacherApi.listProjects(statusFilter.value || undefined)
    projects.value = Array.isArray(res.data) ? res.data : []
    projectStatus.value = `${projects.value.length} project(s)`
  } catch (e: any) {
    toast.error(e.message || 'Failed to load projects')
    projects.value = []
  } finally {
    loading.value = false
  }
}

async function loadReferenceData() {
  try {
    const [catRes, tagRes] = await Promise.all([
      teacherApi.listCategories(),
      teacherApi.listTags(),
    ])
    categories.value = Array.isArray(catRes.data) ? catRes.data : []
    tags.value = Array.isArray(tagRes.data) ? tagRes.data : []
  } catch {
    categories.value = []
    tags.value = []
  }
}

async function openProjectEditor(projectId: number, focusArea?: string) {
  showEditPanel.value = true
  setEditStatus('Loading project details...', '')

  try {
    const [projRes, tagsRes] = await Promise.all([
      teacherApi.getProject(projectId),
      teacherApi.listProjectTags(projectId),
    ])
    const project = projRes.data
    const projectTags = Array.isArray(tagsRes.data) ? tagsRes.data : []

    editProjectId.value = String(project.projectId)
    editTitle.value = project.title || ''
    editMaxStudents.value = Number(project.maxStudents || 1)
    editTopicArea.value = project.topicArea || ''
    editDescription.value = project.description || ''
    editRequiredSkills.value = project.requiredSkills || ''
    editCategoryId.value = String(project.categoryId || '')

    const normalized = normalizeStatus(project.projectStatus)
    editProjectStatus.value = normalized === 'REQUESTED' ? 'AVAILABLE' : normalized
    originalProjectStatus.value = editProjectStatus.value
    editStatusRemark.value = ''

    const selectedIds = projectTags.map((t: any) => t.tagId)
    editSelectedTagIds.value = new Set(selectedIds)
    originalTagIds.value = selectedIds.slice()

    setEditStatus('Project details loaded.', 'success')
  } catch (e: any) {
    setEditStatus(e.message || 'Failed to load project details', 'error')
  }
}

function closeEditPanel() {
  showEditPanel.value = false
  editProjectId.value = ''
  setEditStatus('', '')
}

function toggleEditTag(tagId: number) {
  const s = new Set(editSelectedTagIds.value)
  if (s.has(tagId)) s.delete(tagId)
  else s.add(tagId)
  editSelectedTagIds.value = s
}

function validateEditForm(): string {
  if (!editCategoryId.value) return 'Please select a category.'
  if (!editTitle.value.trim()) return 'Title is required.'
  if (!editDescription.value.trim()) return 'Description is required.'
  if (editMaxStudents.value < 1) return 'Max students must be at least 1.'
  return ''
}

function areTagSelectionsEqual(a: number[], b: number[]): boolean {
  return JSON.stringify([...a].sort()) === JSON.stringify([...b].sort())
}

async function executeSave() {
  const projectId = editProjectId.value
  if (!projectId) return

  saving.value = true
  setEditStatus('Saving...', '')
  try {
    // 1. Update project info
    await teacherApi.updateProject(projectId, {
      categoryId: Number(editCategoryId.value),
      title: editTitle.value.trim(),
      description: editDescription.value.trim(),
      requiredSkills: editRequiredSkills.value.trim(),
      topicArea: editTopicArea.value.trim(),
      maxStudents: editMaxStudents.value,
    })

    // 2. Update status if changed
    if (originalProjectStatus.value !== editProjectStatus.value || editStatusRemark.value.trim()) {
      await teacherApi.changeProjectStatus(projectId, editProjectStatus.value, editStatusRemark.value.trim())
    }

    // 3. Update tags if changed
    const currentTagIds = [...editSelectedTagIds.value]
    if (!areTagSelectionsEqual(currentTagIds, originalTagIds.value)) {
      await teacherApi.bindProjectTags(projectId, currentTagIds)
    }

    setEditStatus('Changes saved successfully.', 'success')
    toast.success('Project updated')
    await loadProjects()
    await openProjectEditor(Number(projectId))
  } catch (e: any) {
    setEditStatus(e.message || 'Failed to save changes', 'error')
  } finally {
    saving.value = false
  }
}

async function saveAllChanges() {
  const err = validateEditForm()
  if (err) {
    setEditStatus(err, 'error')
    return
  }

  // Check if status is changing to AGREED/CLOSED
  if (originalProjectStatus.value !== editProjectStatus.value && ['AGREED', 'CLOSED'].includes(editProjectStatus.value)) {
    const label = editProjectStatus.value === 'AGREED' ? 'Agreed' : 'Closed'
    statusConfirmMessage.value = `Are you sure you want to change the status to "${label}"? This will take effect immediately.`
    pendingConfirmAction = executeSave
    showStatusConfirm.value = true
    return
  }

  await executeSave()
}

function confirmStatusAction() {
  const action = pendingConfirmAction
  showStatusConfirm.value = false
  pendingConfirmAction = null
  if (action) action()
}

function viewRequests() {
  router.push('/teacher/dashboard')
}

onMounted(async () => {
  await loadReferenceData()
  await loadProjects()
})
</script>

<template>
  <div class="projects-page">
    <header class="page-header">
      <h1>My Projects</h1>
      <div style="flex-grow: 1;"></div>
      <button class="btn-primary" @click="router.push('/teacher/create-project')">
        <i class="bi bi-plus-lg"></i> Create Project
      </button>
    </header>

    <div class="panel">
      <div class="status-line">{{ projectStatus }}</div>

      <div class="filters-row">
        <label class="filter-field">
          Status
          <select v-model="statusFilter" @change="loadProjects()">
            <option value="">All Status</option>
            <option value="AVAILABLE">Available</option>
            <option value="REQUESTED">Requested</option>
            <option value="AGREED">Agreed</option>
            <option value="CLOSED">Closed</option>
          </select>
        </label>
      </div>

      <div v-if="loading" style="color: var(--muted); text-align: center; padding: 24px;">Loading...</div>

      <div v-else-if="projects.length === 0" style="color: var(--muted); text-align: center; padding: 24px;">
        No projects found. Create a new project to get started.
      </div>

      <div v-else class="project-grid">
        <div v-for="p in projects" :key="p.projectId" class="outline-card">
          <h4>{{ p.title || 'Untitled Project' }}</h4>
          <p class="meta">
            {{ [p.categoryName, p.topicArea, p.requiredSkills].filter(Boolean).join(' / ') || 'No category' }}
          </p>
          <p class="meta">ID: {{ p.projectId }} | Quota: {{ p.currentAgreedCount || 0 }}/{{ p.maxStudents || 0 }}</p>
          <div style="flex-grow: 1;">
            <span class="status-pill" :class="statusClass(p.projectStatus)">{{ statusText(p.projectStatus) }}</span>
          </div>
          <div class="card-actions">
            <button class="btn-card" @click="viewRequests">Requests</button>
            <button class="btn-card" @click="openProjectEditor(p.projectId, 'edit')">Edit</button>
            <button class="btn-card" @click="openProjectEditor(p.projectId, 'status')">Status</button>
            <button class="btn-card" @click="openProjectEditor(p.projectId, 'tags')">Tags</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit Panel -->
    <div v-if="showEditPanel" class="panel edit-panel">
      <div class="edit-header">
        <h3>Edit Project</h3>
        <button class="btn-close-panel" @click="closeEditPanel">Close</button>
      </div>
      <div class="edit-status" :class="editPanelStatusType">{{ editPanelStatus }}</div>

      <!-- Project Info -->
      <form @submit.prevent>
        <div class="form-grid-2">
          <div class="form-group">
            <label>Title</label>
            <input v-model="editTitle" type="text" class="form-control">
          </div>
          <div class="form-group">
            <label>Max Students</label>
            <input v-model.number="editMaxStudents" type="number" min="1" class="form-control">
          </div>
        </div>
        <div class="form-grid-2">
          <div class="form-group">
            <label>Category</label>
            <select v-model="editCategoryId" class="form-control">
              <option value="">Select category</option>
              <option v-for="cat in categories" :key="cat.categoryId" :value="String(cat.categoryId)">
                {{ cat.categoryName }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>Topic Area</label>
            <input v-model="editTopicArea" type="text" class="form-control">
          </div>
        </div>
        <div class="form-group">
          <label>Description</label>
          <textarea v-model="editDescription" rows="4" class="form-control"></textarea>
        </div>
        <div class="form-group">
          <label>Required Skills</label>
          <textarea v-model="editRequiredSkills" rows="3" class="form-control"></textarea>
        </div>
      </form>

      <hr class="divider">

      <!-- Status -->
      <form @submit.prevent>
        <div class="form-grid-2">
          <div class="form-group">
            <label>Project Status</label>
            <select v-model="editProjectStatus" class="form-control">
              <option value="AVAILABLE">Available</option>
              <option value="AGREED">Agreed</option>
              <option value="CLOSED">Closed</option>
            </select>
          </div>
          <div class="form-group">
            <label>Status Remark</label>
            <input v-model="editStatusRemark" type="text" class="form-control" placeholder="Optional">
          </div>
        </div>
      </form>

      <hr class="divider">

      <!-- Tags -->
      <div class="form-group">
        <label>Project Tags</label>
        <div class="tags-container">
          <label v-for="tag in tags" :key="tag.tagId" class="tag-checkbox">
            <input
              type="checkbox"
              :checked="editSelectedTagIds.has(tag.tagId)"
              @change="toggleEditTag(tag.tagId)"
            >
            <span>{{ tag.tagName }}</span>
          </label>
          <span v-if="tags.length === 0" style="color: var(--muted); font-size: 0.9rem;">No tags available</span>
        </div>
      </div>

      <div style="margin-top: 20px;">
        <button class="btn-primary" :disabled="saving" @click="saveAllChanges">
          {{ saving ? 'Saving...' : 'Save All Changes' }}
        </button>
      </div>
    </div>

    <!-- Status Confirm Modal -->
    <Teleport to="body">
      <div v-if="showStatusConfirm" class="modal-overlay" @click.self="showStatusConfirm = false">
        <div class="modal-dialog">
          <div class="modal-header">
            <h2>Confirm Status Change</h2>
            <button class="icon-button" @click="showStatusConfirm = false">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
          <p style="margin: 0 0 18px; color: var(--text); line-height: 1.6;">{{ statusConfirmMessage }}</p>
          <div class="modal-actions">
            <button class="btn-secondary" @click="showStatusConfirm = false">Cancel</button>
            <button class="btn-primary" @click="confirmStatusAction">Confirm</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.projects-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
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
  box-shadow: 0 20px 60px rgba(21, 16, 45, 0.15);
  padding: 24px 28px;
}

.status-line {
  font-size: 0.9rem;
  color: var(--muted);
  margin-bottom: 12px;
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
  color: var(--muted);
}

.filter-field select {
  padding: 10px 12px;
  border: 1px solid rgba(90, 43, 152, 0.18);
  border-radius: 12px;
  font: inherit;
  background: #fff;
  outline: none;
  min-width: 180px;
}

.filter-field select:focus {
  border-color: var(--deep);
  box-shadow: 0 0 0 3px rgba(90, 43, 152, 0.14);
}

.project-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.outline-card {
  border: 1.5px solid rgba(90, 43, 152, 0.15);
  border-radius: 20px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.outline-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(21, 16, 45, 0.08);
}

.outline-card h4 {
  margin: 0;
  font-size: 1.1rem;
  color: var(--text);
}

.outline-card .meta {
  margin: 0;
  font-size: 0.85rem;
  color: var(--muted);
  line-height: 1.5;
}

.card-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.btn-card {
  padding: 8px 12px;
  border-radius: 12px;
  border: 1px solid rgba(90, 43, 152, 0.16);
  background: #fff;
  color: var(--deep);
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}

.btn-card:hover {
  background: #f7f2ff;
  border-color: var(--deep);
}

.status-pill {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status-available { background: rgba(47, 197, 168, 0.12); color: var(--green); }
.status-requested { background: rgba(246, 166, 61, 0.12); color: var(--orange); }
.status-agreed { background: rgba(36, 179, 255, 0.15); color: var(--accent); }
.status-unavailable { background: rgba(156, 156, 178, 0.2); color: rgba(28, 27, 51, 0.65); }

/* Edit Panel */
.edit-panel {
  margin-top: 4px;
  border: 2px solid rgba(90, 43, 152, 0.12);
}

.edit-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.edit-header h3 {
  margin: 0;
  flex: 1;
  font-size: 1.2rem;
  color: var(--text);
}

.btn-close-panel {
  padding: 8px 14px;
  border-radius: 12px;
  border: 1px solid rgba(90, 43, 152, 0.16);
  background: #fff;
  color: var(--deep);
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  font-size: 0.85rem;
}

.btn-close-panel:hover {
  background: #f7f2ff;
}

.edit-status {
  font-size: 0.9rem;
  margin-bottom: 16px;
  min-height: 22px;
  color: var(--muted);
}

.edit-status.success { color: #167d68; }
.edit-status.error { color: #b02a37; }

.form-grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}

.form-group label {
  font-weight: 600;
  color: var(--text);
  font-size: 0.9rem;
}

.form-control {
  padding: 10px 14px;
  border: 1.5px solid rgba(90, 43, 152, 0.18);
  border-radius: 12px;
  font-size: 0.95rem;
  font-family: inherit;
  outline: none;
  background: #fff;
}

.form-control:focus {
  border-color: var(--deep);
  box-shadow: 0 0 0 3px rgba(90, 43, 152, 0.1);
}

.divider {
  border: none;
  border-top: 1px solid rgba(156, 156, 178, 0.25);
  margin: 24px 0;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-checkbox {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 12px;
  border: 1px solid rgba(90, 43, 152, 0.18);
  border-radius: 999px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.2s;
}

.tag-checkbox:has(input:checked) {
  background: var(--deep);
  color: #fff;
  border-color: var(--deep);
}

.tag-checkbox input {
  display: none;
}

.btn-primary {
  background: var(--deep);
  border: none;
  color: white;
  padding: 12px 28px;
  border-radius: 40px;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  font-family: inherit;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn-primary:hover { background: var(--deep); }
.btn-primary:disabled { background: #c4c4e0; cursor: not-allowed; }

.btn-secondary {
  background: transparent;
  border: 1.5px solid var(--muted);
  color: var(--text);
  padding: 12px 24px;
  border-radius: 40px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
}

.btn-secondary:hover { border-color: var(--deep); color: var(--deep); }

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(28, 27, 51, 0.45);
  z-index: 20;
}

.modal-dialog {
  width: min(460px, 100%);
  background: #fff;
  border-radius: 24px;
  border: 1px solid rgba(28, 27, 51, 0.08);
  box-shadow: 0 24px 80px rgba(21, 16, 45, 0.22);
  padding: 28px;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.modal-header h2 {
  margin: 0;
  color: var(--text);
  font-size: 1.35rem;
  font-weight: 600;
}

.icon-button {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: 1px solid rgba(90, 43, 152, 0.16);
  background: rgba(90, 43, 152, 0.06);
  color: var(--deep);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .form-grid-2 {
    grid-template-columns: 1fr;
  }
  .project-grid {
    grid-template-columns: 1fr;
  }
}
</style>
