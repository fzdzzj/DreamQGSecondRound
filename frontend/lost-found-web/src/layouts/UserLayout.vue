<template>
  <div class="app-layout">
    <AppHeader />

    <div class="flex">
      <AppSidebar />

      <div class="app-main">
        <AppBreadcrumb />
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import AppHeader from './components/AppHeader.vue'
import AppSidebar from './components/AppSidebar.vue'
import AppBreadcrumb from './components/AppBreadcrumb.vue'
</script>
