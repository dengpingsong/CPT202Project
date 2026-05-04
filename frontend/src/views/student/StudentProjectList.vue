<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { studentApi } from '../../utils/api'
import { toast } from '../../utils/ui-feedback'

// State
const loading = ref(true)
const projects = ref<any[]>([])
const requests = ref<any[]>([])
const tags = ref<any[]>([])
const categories = ref<any[]>([])
const selectedTagIds = ref<Set<string>>(new Set())

const keyword = ref('')
const categoryId = ref('')
const statusFilter = ref('')

const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)
const totalPages = ref(1)

let keywordTimer: ReturnType<typeof setTimeout> | null = null

// Computed
const visiblePages = computed(() => {
  const tp = totalPages.value
  const cp = currentPage.value
  if (tp <= 5) return Array.from({ length: tp }, (_, i) => i + 1)
  if (cp <= 3) return [1, 2, 3, 4, 5]
  if (cp >= tp - 2) return [tp - 4, tp - 3, tp - 2, tp - 1, tp]
  return [cp - 2, cp - 1, cp, cp + 1, cp + 2]
})

const paginationSummary = computed(() => {
  if (total.value === 0) return ''
  return `Page ${currentPage.value} / ${totalPages.value}, total ${total.value} projects`
})

// Helpers
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

function findStudentRequestStatus(projectId: number | string): string | null {
  const matched = requests.value.find(
    r => String(r.projectId) === String(projectId)
      && ['PENDING', 'ACCEPTED', 'REJECTED'].includes(normalizeStatus(r.requestStatus))
  )
  return matched ? normalizeStatus(matched.requestStatus) : null
}

function getDisplayStatus(project: any): string {
  const reqStatus = findStudentRequestStatus(project.projectId)
  if (reqStatus === 'PENDING') return 'REQUESTED'
  return reqStatus || project.projectStatus
}

function onKeywordInput() {
  if (keywordTimer) clearTimeout(keywordTimer)
  keywordTimer = setTimeout(() => loadProjects(1), 250)
}

function toggleTag(tagId: string) {
  if (selectedTagIds.value.has(tagId)) {
    selectedTagIds.value.delete(tagId)
  } else {
    selectedTagIds.value.add(tagId)
  }
  // Force reactivity
  selectedTagIds.value = new Set(selectedTagIds.value)
  loadProjects(1)
}

function clearTagFilters() {
  selectedTagIds.value = new Set()
  loadProjects(1)
}

function clearAllFilters() {
  keyword.value = ''
  categoryId.value = ''
  statusFilter.value = ''
  selectedTagIds.value = new Set()
  loadProjects(1)
}

// Data loading
async function loadProjects(page: number) {
  loading.value = true
  try {
    const res = await studentApi.getProjects(page, pageSize.value, {
      keyword: keyword.value.trim(),
      categoryId: categoryId.value,
      status: statusFilter.value,
      tagIds: Array.from(selectedTagIds.value),
    })
    const data = res.data
    projects.value = Array.isArray(data?.records) ? data.records : []
    total.value = Number(data?.total || projects.value.length)
    totalPages.value = Math.max(1, Number(data?.totalPages || Math.ceil(total.value / pageSize.value) || 1))
    currentPage.value = Math.max(1, Number(data?.pageNum || page))
  } catch (e: any) {
    toast.error(e.message || 'Failed to load projects')
    projects.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function loadTags() {
  try {
    const res = await studentApi.listProjectTags()
    tags.value = Array.isArray(res.data) ? res.data : []
  } catch {
    tags.value = []
  }
}

async function loadCategories() {
  try {
    const res = await studentApi.listProjectCategories()
    categories.value = Array.isArray(res.data) ? res.data : []
  } catch {
    categories.value = []
  }
}

async function init() {
  try {
    const reqRes = await studentApi.getRequests()
    requests.value = Array.isArray(reqRes.data) ? reqRes.data : []
  } catch {
    requests.value = []
  }
  await Promise.all([loadTags(), loadCategories()])
  await loadProjects(1)
}

onMounted(init)
</script>

<template>
  <div class="project-list-page">
    <div class="page-header">
      <h1>Available Projects</h1>
    </div>

    <div class="panel-card">
      <p style="color: var(--muted); margin-bottom: 10px;">Click View to see project details</p>

      <!-- Filters -->
      <div class="filters-row">
        <label class="filter-field">
          Keyword
          <input
            v-model="keyword"
            type="search"
            placeholder="Project name, area, or skill"
            @input="onKeywordInput"
          >
        </label>
        <label class="filter-field">
          Category
          <select v-model="categoryId" @change="loadProjects(1)">
            <option value="">All Categories</option>
            <option v-for="cat in categories" :key="cat.categoryId" :value="cat.categoryId">
              {{ cat.categoryName }}
            </option>
          </select>
        </label>
        <label class="filter-field">
          Status
          <select v-model="statusFilter" @change="loadProjects(1)">
            <option value="">All Status</option>
            <option value="AVAILABLE">Available</option>
            <option value="REQUESTED">Requested</option>
            <option value="AGREED">Agreed</option>
            <option value="CLOSED">Closed</option>
          </select>
        </label>
        <button class="clear-btn" @click="clearAllFilters">Clear Filters</button>
      </div>

      <!-- Tag Filters -->
      <div class="tag-filter-bar">
        <span class="tag-label">Tags:</span>
        <template v-if="tags.length > 0">
          <button
            v-for="tag in tags"
            :key="tag.tagId"
            class="tag-btn"
            :class="{ active: selectedTagIds.has(String(tag.tagId)) }"
            @click="toggleTag(String(tag.tagId))"
          >
            {{ tag.tagName }}
          </button>
          <button
            class="tag-btn"
            :disabled="selectedTagIds.size === 0"
            @click="clearTagFilters"
          >Clear</button>
        </template>
        <span v-else class="tag-label">No tags available</span>
      </div>

      <!-- Table -->
      <div class="table-wrapper">
        <table class="project-table">
          <thead>
            <tr>
              <th>Project Code</th>
              <th>Project Title</th>
              <th>Supervisor</th>
              <th>Category</th>
              <th>Topic Area</th>
              <th>Quota</th>
              <th>Status</th>
              <th>Details</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="8" style="text-align: center; color: #888;">Loading...</td>
            </tr>
            <tr v-else-if="projects.length === 0">
              <td colspan="8" style="text-align: center; color: #888;">No projects available</td>
            </tr>
            <tr v-for="project in projects" :key="project.projectId">
              <td><strong>{{ project.projectId }}</strong></td>
              <td>{{ project.title }}</td>
              <td>{{ project.teacherName || '-' }}</td>
              <td>{{ project.categoryName || '-' }}</td>
              <td>{{ project.topicArea || '-' }}</td>
              <td>{{ project.currentAgreedCount || 0 }}/{{ project.maxStudents || 0 }}</td>
              <td>
                <span class="status-pill" :class="statusClass(getDisplayStatus(project))">
                  {{ getDisplayStatus(project) }}
                </span>
              </td>
              <td>
                <router-link class="view-btn" :to="`/student/projects/${project.projectId}`">
                  View
                </router-link>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="project-count">Total: {{ total }} projects</div>

      <!-- Pagination -->
      <div v-if="total > 0" class="pagination-bar">
        <button
          class="pagination-btn pagination-nav"
          :disabled="currentPage <= 1"
          @click="loadProjects(currentPage - 1)"
        >
          <span>Prev</span>
        </button>
        <div class="pagination-pages">
          <button
            v-for="page in visiblePages"
            :key="page"
            class="pagination-number"
            :class="{ active: page === currentPage }"
            @click="loadProjects(page)"
          >
            {{ page }}
          </button>
        </div>
        <button
          class="pagination-btn pagination-nav"
          :disabled="currentPage >= totalPages"
          @click="loadProjects(currentPage + 1)"
        >
          <span>Next</span>
        </button>
      </div>
      <div v-if="total > 0" class="pagination-summary">{{ paginationSummary }}</div>
    </div>
  </div>
</template>

<style scoped>
.project-list-page {
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

.panel-card {
  background: #fff;
  border-radius: 28px;
  padding: 24px 28px;
}

.filters-row {
  display: grid;
  grid-template-columns: minmax(180px, 1.5fr) minmax(150px, 1fr) minmax(150px, 1fr) auto;
  gap: 10px;
  align-items: end;
  margin-bottom: 14px;
}

.filter-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 0.9rem;
  color: var(--muted);
}

.filter-field input,
.filter-field select {
  padding: 10px 12px;
  border: 1px solid rgba(90, 43, 152, 0.18);
  border-radius: 12px;
  font: inherit;
  background: #fff;
  outline: none;
}

.filter-field input:focus,
.filter-field select:focus {
  border-color: var(--deep);
  box-shadow: 0 0 0 3px rgba(90, 43, 152, 0.14);
}

.clear-btn {
  padding: 10px 14px;
  border-radius: 12px;
  border: 1px solid rgba(90, 43, 152, 0.16);
  background: linear-gradient(180deg, #fff, #f7f2ff);
  color: var(--deep);
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  font-size: 0.9rem;
}

.clear-btn:hover {
  background: linear-gradient(180deg, #fff, #efe4ff);
}

.tag-filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 16px;
}

.tag-label {
  color: var(--muted);
  font-size: 0.9rem;
}

.tag-btn {
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid rgba(90, 43, 152, 0.16);
  background: #fff;
  color: var(--deep);
  font-size: 0.85rem;
  cursor: pointer;
  font-family: inherit;
  font-weight: 500;
  transition: all 0.2s;
}

.tag-btn:hover {
  background: #f7f2ff;
}

.tag-btn.active {
  background: var(--deep);
  color: #fff;
  border-color: var(--deep);
}

.tag-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.table-wrapper {
  overflow-x: auto;
  border-radius: 18px;
}

.project-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
  font-size: 0.9rem;
}

.project-table th {
  text-align: left;
  padding: 16px;
  background: #f8f5ff;
  font-weight: 600;
  color: var(--deep);
  border-bottom: 2px solid rgba(90, 43, 152, 0.2);
}

.project-table td {
  padding: 16px;
  border-bottom: 1px solid rgba(156, 156, 178, 0.2);
}

.project-table tbody tr:hover {
  background: rgba(90, 43, 152, 0.03);
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
.status-rejected { background: rgba(199, 69, 69, 0.12); color: var(--red); }
.status-unavailable { background: rgba(156, 156, 178, 0.2); color: rgba(28, 27, 51, 0.65); }

.view-btn {
  background: #fff;
  border: 1.5px solid var(--deep);
  color: var(--deep);
  font-weight: 600;
  padding: 8px 18px;
  border-radius: 40px;
  font-size: 0.8rem;
  cursor: pointer;
  text-decoration: none;
  display: inline-block;
  transition: all 0.2s;
}

.view-btn:hover {
  background: var(--deep);
  color: #fff;
}

.project-count {
  margin-top: 16px;
  font-size: 0.9rem;
  color: var(--muted);
}

/* Pagination */
.pagination-bar {
  margin-top: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  padding: 14px 18px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(90, 43, 152, 0.12), rgba(123, 79, 189, 0.08));
  border: 1px solid rgba(90, 43, 152, 0.14);
}

.pagination-pages {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.pagination-btn,
.pagination-number {
  min-width: 50px;
  height: 46px;
  padding: 0 18px;
  border-radius: 14px;
  border: 1px solid rgba(90, 43, 152, 0.16);
  background: linear-gradient(180deg, #fff, #f7f2ff);
  color: var(--deep);
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
}

.pagination-btn:hover,
.pagination-number:hover {
  background: linear-gradient(180deg, #fff, #efe4ff);
  transform: translateY(-1px);
}

.pagination-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
  transform: none;
}

.pagination-number.active {
  background: linear-gradient(135deg, var(--deep), var(--mid));
  color: #fff;
  border-color: var(--deep);
}

.pagination-nav {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, var(--deep), var(--mid));
  color: #fff;
}

.pagination-nav:hover {
  background: linear-gradient(135deg, var(--deep), #6d43b0);
  color: #fff;
}

.pagination-summary {
  margin-top: 10px;
  color: #6c5a8f;
  font-size: 0.9rem;
  text-align: center;
}

@media (max-width: 960px) {
  .filters-row {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
