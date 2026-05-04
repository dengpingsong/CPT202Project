<script setup lang="ts">
</script>

<template>
  <div class="teacher-layout">
    <nav class="sidebar">
      <router-link to="/teacher/dashboard">Dashboard</router-link>
      <router-link to="/teacher/projects">Projects</router-link>
      <router-link to="/teacher/create-project">Create Project</router-link>
      <router-link to="/teacher/history">History</router-link>
      <router-link to="/teacher/notifications">Notifications</router-link>
      <router-link to="/teacher/settings">Settings</router-link>
    </nav>
    <main class="content">
      <router-view />
    </main>
  </div>
</template>
