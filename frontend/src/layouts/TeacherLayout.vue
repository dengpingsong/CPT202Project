<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { clearAuth } from '../utils/api'

const router = useRouter()
const collapsed = ref(false)

function logout() {
  clearAuth()
  router.push('/login')
}
</script>

<template>
  <div class="layout" :class="{ collapsed }">
    <aside class="sidebar">
      <div class="toggle-wrap">
        <button class="toggle" @click="collapsed = !collapsed" aria-label="Toggle menu">
          <span></span>
        </button>
      </div>
      <nav class="nav-group">
        <router-link to="/teacher/projects" active-class="active">
          <i class="bi bi-kanban nav-icon"></i>
          <span class="nav-label">My Projects</span>
        </router-link>
        <router-link to="/teacher/dashboard" active-class="active">
          <i class="bi bi-file-earmark-check nav-icon"></i>
          <span class="nav-label">Review Requests</span>
        </router-link>
        <router-link to="/teacher/history" active-class="active">
          <i class="bi bi-clock-history nav-icon"></i>
          <span class="nav-label">History</span>
        </router-link>
        <router-link to="/teacher/notifications" active-class="active">
          <i class="bi bi-chat-dots nav-icon"></i>
          <span class="nav-label">Notifications</span>
        </router-link>
        <router-link to="/teacher/settings" active-class="active">
          <i class="bi bi-person-gear nav-icon"></i>
          <span class="nav-label">Settings</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <button class="logout-btn" @click="logout">
          <i class="bi bi-box-arrow-right nav-icon"></i>
          <span class="nav-label">Logout</span>
        </button>
      </div>
    </aside>

    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
@import url('https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css');

.layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  min-height: 100vh;
  transition: grid-template-columns 0.3s ease;
}

.layout.collapsed {
  grid-template-columns: 80px 1fr;
}

.sidebar {
  background: linear-gradient(180deg, #3d1566, #631e97);
  padding: 24px 16px;
  color: white;
  display: flex;
  flex-direction: column;
  gap: 20px;
  position: relative;
  overflow: hidden;
}

.sidebar::after {
  content: "";
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at top, rgba(255, 255, 255, 0.2), transparent 60%);
  pointer-events: none;
}

.toggle-wrap {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  margin-bottom: 4px;
}

.toggle {
  width: 100%;
  height: 100%;
  border-radius: 16px;
  border: none;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.3s ease;
  position: relative;
}

.toggle span,
.toggle span::before,
.toggle span::after {
  display: block;
  width: 22px;
  height: 2px;
  background: white;
  transition: all 0.3s ease;
  position: absolute;
}

.toggle span { background: transparent; }
.toggle span::before { content: ""; top: 0; transform: rotate(45deg); }
.toggle span::after { content: ""; top: 0; transform: rotate(-45deg); }

.layout.collapsed .toggle span { background: white; }
.layout.collapsed .toggle span::before { top: -8px; transform: none; }
.layout.collapsed .toggle span::after { top: 8px; transform: none; }

.nav-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 8px;
}

.nav-group a {
  border: none;
  background: transparent;
  color: white;
  font-size: 0.95rem;
  padding: 10px 14px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s, padding 0.2s;
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: flex-start;
  text-decoration: none;
}

.nav-group a.active,
.nav-group a:hover {
  background: rgba(255, 255, 255, 0.2);
}

.nav-icon {
  font-size: 1.2rem;
  width: 28px;
  text-align: center;
}

.nav-label {
  transition: opacity 0.3s ease;
  white-space: nowrap;
}

.layout.collapsed .nav-group a {
  justify-content: center;
  padding: 12px;
}

.layout.collapsed .nav-label {
  opacity: 0;
  width: 0;
  overflow: hidden;
}

.sidebar-footer {
  margin-top: auto;
}

.logout-btn {
  width: 100%;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.95rem;
  padding: 10px 14px;
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: flex-start;
  font-family: inherit;
  transition: background 0.2s, color 0.2s;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  color: white;
}

.layout.collapsed .logout-btn {
  justify-content: center;
  padding: 12px;
}

.main {
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 32px;
  background: #f4f3f7;
  min-height: 100vh;
}

@media (max-width: 960px) {
  .layout { grid-template-columns: 1fr; }
  .sidebar { flex-direction: row; gap: 8px; padding: 12px; }
  .sidebar::after { display: none; }
  .layout.collapsed { grid-template-columns: 1fr; }
  .sidebar-footer { margin-top: 0; }
}
</style>
