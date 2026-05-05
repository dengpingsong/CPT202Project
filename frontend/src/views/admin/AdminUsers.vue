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
        <span class="filter-control select-control">
          <select v-model="roleFilter" aria-label="Role" @change="loadUsers()">
            <option value="">All Roles</option>
            <option value="ADMIN">Admin</option>
            <option value="TEACHER">Teacher</option>
            <option value="STUDENT">Student</option>
          </select>
        </span>
        <span class="filter-control select-control">
          <select v-model="statusFilter" aria-label="Status" @change="loadUsers()">
            <option value="">All Status</option>
            <option value="ACTIVE">Active</option>
            <option value="DISABLED">Disabled</option>
          </select>
        </span>
        <button class="clear-btn" @click="roleFilter = ''; statusFilter = ''; loadUsers()">
          <i class="bi bi-arrow-counterclockwise"></i>
          Clear
        </button>
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
.filters-row { display: grid; grid-template-columns: minmax(190px, 1fr) minmax(170px, 0.9fr) auto; gap: 12px; align-items: end; margin-bottom: 16px; }
.filter-control { min-height: 44px; border: 1.5px solid rgba(90, 43, 152, 0.16); border-radius: 12px; background: #fff; display: flex; align-items: center; transition: border-color 0.15s ease, box-shadow 0.15s ease; }
.filter-control:focus-within { border-color: var(--deep); box-shadow: 0 0 0 3px rgba(90, 43, 152, 0.08); }
.filter-control select { width: 100%; min-width: 0; border: 0; outline: none; background: transparent; color: var(--text); font: inherit; font-size: 0.95rem; }
.select-control select { height: 42px; padding: 0 12px; cursor: pointer; }
.clear-btn { min-height: 44px; padding: 0 16px; border-radius: 12px; border: 1.5px solid rgba(90, 43, 152, 0.16); background: #fff; color: var(--deep); display: inline-flex; align-items: center; justify-content: center; gap: 7px; font-family: inherit; font-size: 0.9rem; font-weight: 700; cursor: pointer; white-space: nowrap; }
.clear-btn:hover { border-color: var(--deep); background: rgba(90, 43, 152, 0.06); }
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
@media (max-width: 760px) {
  .filters-row { grid-template-columns: 1fr; }
}
</style>
