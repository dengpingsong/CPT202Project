<script setup lang="ts">
</script>

<template>
  <div class="admin-layout">
    <nav class="sidebar">
      <router-link to="/admin/projects">Projects</router-link>
      <router-link to="/admin/users">Users</router-link>
      <router-link to="/admin/requests">Requests</router-link>
      <router-link to="/admin/request-history">Request History</router-link>
      <router-link to="/admin/categories">Categories</router-link>
      <router-link to="/admin/tags">Tags</router-link>
      <router-link to="/admin/settings">Settings</router-link>
    </nav>
    <main class="content">
      <router-view />
    </main>
  </div>
</template>
