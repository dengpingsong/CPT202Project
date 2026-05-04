<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminApi } from '../../utils/api'
import { toast } from '../../utils/ui-feedback'

const loading = ref(true)
const users = ref<any[]>([])
const roleFilter = ref('')
const statusFilter = ref('')

function statusColor(status: string): string {
  const s = String(status || '').toUpperCase()
  return s === 'ACTIVE' ? 'var(--green)' : 'var(--red)'
}

async function loadUsers() {
  loading.value = true
  try {
    const res = await adminApi.listUsers(roleFilter.value || undefined, statusFilter.value || undefined)
    users.value = Array.isArray(res.data) ? res.data : []
  } catch (e: any) {
    toast.error(e.message || 'Failed to load users')
    users.value = []
  } finally {
    loading.value = false
  }
}

async function toggleStatus(userId: number, currentStatus: string) {
  const newStatus = currentStatus.toUpperCase() === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  try {
    await adminApi.updateUserStatus(userId, newStatus)
    toast.success(`User ${newStatus === 'ACTIVE' ? 'enabled' : 'disabled'}`)
    await loadUsers()
  } catch (e: any) {
    toast.error(e.message || 'Failed to update user status')
  }
}

onMounted(loadUsers)
</script>

<template>
  <div class="page">
    <header class="page-header">
      <h1>User Management</h1>
    </header>

    <div class="panel">
      <div class="filters-row">
        <label class="filter-field">
          Role
          <select v-model="roleFilter" @change="loadUsers()">
            <option value="">All Roles</option>
            <option value="ADMIN">Admin</option>
            <option value="TEACHER">Teacher</option>
            <option value="STUDENT">Student</option>
          </select>
        </label>
        <label class="filter-field">
          Status
          <select v-model="statusFilter" @change="loadUsers()">
            <option value="">All Status</option>
            <option value="ACTIVE">Active</option>
            <option value="DISABLED">Disabled</option>
          </select>
        </label>
        <button class="clear-btn" @click="roleFilter = ''; statusFilter = ''; loadUsers()">Refresh</button>
      </div>

      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Username</th>
              <th>Full Name</th>
              <th>Email</th>
              <th>Role</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="7" style="text-align: center; color: #888;">Loading...</td>
            </tr>
            <tr v-else-if="users.length === 0">
              <td colspan="7" style="text-align: center; color: #888;">No users found</td>
            </tr>
            <tr v-for="u in users" :key="u.userId">
              <td>{{ u.userId }}</td>
              <td>{{ u.username || '-' }}</td>
              <td>{{ u.fullName || '-' }}</td>
              <td>{{ u.email || '-' }}</td>
              <td>{{ u.role || '-' }}</td>
              <td>
                <span class="status-chip" :style="{ color: statusColor(u.accountStatus), background: statusColor(u.accountStatus) + '18' }">
                  {{ u.accountStatus || '-' }}
                </span>
              </td>
              <td>
                <button
                  class="btn-sm"
                  :class="String(u.accountStatus).toUpperCase() === 'ACTIVE' ? 'btn-disable' : 'btn-enable'"
                  @click="toggleStatus(u.userId, u.accountStatus)"
                >
                  {{ String(u.accountStatus).toUpperCase() === 'ACTIVE' ? 'Disable' : 'Enable' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="summary">{{ users.length }} user(s)</div>
    </div>
  </div>
</template>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }
.page-header h1 { margin: 0; font-size: 1.8rem; font-weight: 600; color: var(--text); }
.filters-row { display: flex; gap: 10px; align-items: end; margin-bottom: 16px; flex-wrap: wrap; }
.filter-field { display: flex; flex-direction: column; gap: 6px; font-size: 0.9rem; color: var(--muted); }
.filter-field select { padding: 10px 12px; border: 1px solid rgba(90, 43, 152, 0.18); border-radius: 12px; font: inherit; background: #fff; outline: none; min-width: 160px; }
.filter-field select:focus { border-color: var(--deep); box-shadow: 0 0 0 3px rgba(90, 43, 152, 0.14); }
.clear-btn { padding: 10px 14px; border-radius: 12px; border: 1px solid rgba(90, 43, 152, 0.16); background: linear-gradient(180deg, #fff, #f7f2ff); color: var(--deep); font-weight: 600; cursor: pointer; font-family: inherit; font-size: 0.9rem; }
.clear-btn:hover { background: linear-gradient(180deg, #fff, #efe4ff); }
.table-wrapper { overflow-x: auto; border-radius: 18px; }
.data-table { width: 100%; border-collapse: collapse; min-width: 700px; font-size: 0.9rem; }
.data-table th { text-align: left; padding: 16px; background: #f8f5ff; font-weight: 600; color: var(--deep); border-bottom: 2px solid rgba(90, 43, 152, 0.2); }
.data-table td { padding: 16px; border-bottom: 1px solid rgba(156, 156, 178, 0.2); }
.data-table tbody tr:hover { background: rgba(90, 43, 152, 0.03); }
.status-chip { display: inline-block; padding: 4px 10px; border-radius: 999px; font-size: 0.8rem; font-weight: 600; }
.btn-sm { padding: 6px 14px; border-radius: 20px; border: 1.5px solid; font-size: 0.8rem; font-weight: 600; cursor: pointer; font-family: inherit; transition: all 0.2s; background: transparent; }
.btn-disable { border-color: var(--red); color: var(--red); }
.btn-disable:hover { background: var(--red); color: #fff; }
.btn-enable { border-color: var(--green); color: var(--green); }
.btn-enable:hover { background: var(--green); color: #fff; }
.summary { margin-top: 16px; font-size: 0.9rem; color: var(--muted); }
</style>
