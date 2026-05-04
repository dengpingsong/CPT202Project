<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminApi } from '../../utils/api'
import { toast, confirm } from '../../utils/ui-feedback'

const loading = ref(true)
const tags = ref<any[]>([])
const formName = ref('')
const formDescription = ref('')
const editingId = ref<number | null>(null)
const formStatus = ref('')
const formStatusType = ref<'success' | 'error' | ''>('')
const saving = ref(false)

function setFormStatus(msg: string, type: 'success' | 'error' | '') {
  formStatus.value = msg
  formStatusType.value = type
}

function resetForm() {
  formName.value = ''
  formDescription.value = ''
  editingId.value = null
  setFormStatus('', '')
}

function editTag(tag: any) {
  editingId.value = tag.tagId
  formName.value = tag.tagName || ''
  formDescription.value = tag.description || ''
  setFormStatus('Editing mode', '')
}

async function loadTags() {
  loading.value = true
  try {
    const res = await adminApi.listTags()
    tags.value = Array.isArray(res.data) ? res.data : []
  } catch (e: any) {
    toast.error(e.message || 'Failed to load tags')
    tags.value = []
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!formName.value.trim()) {
    setFormStatus('Tag name is required.', 'error')
    return
  }

  saving.value = true
  setFormStatus('Saving...', '')
  try {
    const payload = {
      tagName: formName.value.trim(),
      description: formDescription.value.trim(),
    }
    if (editingId.value !== null) {
      await adminApi.updateTag(editingId.value, payload)
      setFormStatus('Tag updated.', 'success')
      toast.success('Tag updated')
    } else {
      await adminApi.createTag(payload)
      setFormStatus('Tag created.', 'success')
      toast.success('Tag created')
    }
    resetForm()
    await loadTags()
  } catch (e: any) {
    setFormStatus(e.message || 'Failed to save tag', 'error')
  } finally {
    saving.value = false
  }
}

async function handleDelete(tagId: number, name: string) {
  const confirmed = await confirm(`Delete tag "${name}"? This may affect projects using this tag.`)
  if (!confirmed) return
  try {
    await adminApi.deleteTag(tagId)
    toast.success('Tag deleted')
    if (editingId.value === tagId) resetForm()
    await loadTags()
  } catch (e: any) {
    toast.error(e.message || 'Failed to delete tag')
  }
}

onMounted(loadTags)
</script>

<template>
  <div class="page">
    <header class="page-header">
      <h1>Tag Management</h1>
      <span class="hint">Unique names &middot; Many-to-many with projects</span>
    </header>

    <div class="panel">
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Tag Name</th>
              <th>Description</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="4" style="text-align: center; color: #888;">Loading...</td>
            </tr>
            <tr v-else-if="tags.length === 0">
              <td colspan="4" style="text-align: center; color: #888;">No tags yet</td>
            </tr>
            <tr v-for="tag in tags" :key="tag.tagId">
              <td>{{ tag.tagId }}</td>
              <td><strong>{{ tag.tagName }}</strong></td>
              <td>{{ tag.description || '-' }}</td>
              <td>
                <div class="action-btns">
                  <button class="btn-sm btn-edit" @click="editTag(tag)">
                    <i class="bi bi-pencil"></i> Edit
                  </button>
                  <button class="btn-sm btn-delete" @click="handleDelete(tag.tagId, tag.tagName)">
                    <i class="bi bi-trash"></i> Delete
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Add/Edit Form -->
      <div class="add-section">
        <h3>{{ editingId !== null ? 'Edit Tag' : 'Add Tag' }}</h3>
        <form class="inline-form" @submit.prevent="handleSave">
          <div class="form-field">
            <label>Tag Name <span class="required">*</span></label>
            <input v-model="formName" type="text" class="form-control" placeholder="e.g. Python">
          </div>
          <div class="form-field">
            <label>Description</label>
            <input v-model="formDescription" type="text" class="form-control" placeholder="Optional description">
          </div>
          <div class="form-actions">
            <button type="submit" class="btn-primary" :disabled="saving">
              {{ saving ? 'Saving...' : (editingId !== null ? 'Save Changes' : 'Add Tag') }}
            </button>
            <button v-if="editingId !== null" type="button" class="btn-secondary" @click="resetForm">Cancel</button>
          </div>
        </form>
        <div class="form-status" :class="formStatusType">{{ formStatus }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }
.page-header { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; }
.page-header h1 { margin: 0; font-size: 1.8rem; font-weight: 600; color: var(--text); }
.hint { font-size: 0.85rem; color: var(--muted); }
.panel { background: #fff; border-radius: 28px; box-shadow: 0 20px 60px rgba(21, 16, 45, 0.15); padding: 24px 28px; }
.table-wrapper { overflow-x: auto; border-radius: 18px; }
.data-table { width: 100%; border-collapse: collapse; min-width: 500px; font-size: 0.9rem; }
.data-table th { text-align: left; padding: 16px; background: #f8f5ff; font-weight: 600; color: var(--deep); border-bottom: 2px solid rgba(90, 43, 152, 0.2); }
.data-table td { padding: 16px; border-bottom: 1px solid rgba(156, 156, 178, 0.2); }
.data-table tbody tr:hover { background: rgba(90, 43, 152, 0.03); }
.action-btns { display: flex; gap: 8px; }
.btn-sm { padding: 6px 14px; border-radius: 20px; border: 1.5px solid; font-size: 0.8rem; font-weight: 600; cursor: pointer; font-family: inherit; display: inline-flex; align-items: center; gap: 4px; transition: all 0.2s; background: transparent; }
.btn-edit { border-color: var(--deep); color: var(--deep); }
.btn-edit:hover { background: var(--deep); color: #fff; }
.btn-delete { border-color: var(--red); color: var(--red); }
.btn-delete:hover { background: var(--red); color: #fff; }

.add-section { margin-top: 24px; padding-top: 24px; border-top: 1px solid rgba(156, 156, 178, 0.25); }
.add-section h3 { margin: 0 0 16px; font-size: 1.1rem; color: var(--deep); }
.inline-form { display: flex; gap: 16px; align-items: end; flex-wrap: wrap; }
.form-field { display: flex; flex-direction: column; gap: 6px; }
.form-field label { font-weight: 600; color: var(--text); font-size: 0.9rem; }
.required { color: var(--red); }
.form-control { padding: 10px 14px; border: 1.5px solid rgba(90, 43, 152, 0.18); border-radius: 12px; font-size: 0.95rem; font-family: inherit; outline: none; background: #fff; min-width: 200px; }
.form-control:focus { border-color: var(--deep); box-shadow: 0 0 0 3px rgba(90, 43, 152, 0.1); }
.form-actions { display: flex; gap: 10px; }
.form-status { margin-top: 12px; font-size: 0.9rem; min-height: 22px; color: var(--muted); }
.form-status.success { color: #167d68; }
.form-status.error { color: #b02a37; }
.btn-primary { background: var(--deep); border: none; color: white; padding: 10px 24px; border-radius: 40px; font-weight: 600; font-size: 0.95rem; cursor: pointer; font-family: inherit; }
.btn-primary:hover { background: var(--deep); }
.btn-primary:disabled { background: #c4c4e0; cursor: not-allowed; }
.btn-secondary { background: transparent; border: 1.5px solid var(--muted); color: var(--text); padding: 10px 20px; border-radius: 40px; font-weight: 500; cursor: pointer; font-family: inherit; }
.btn-secondary:hover { border-color: var(--deep); color: var(--deep); }
</style>
