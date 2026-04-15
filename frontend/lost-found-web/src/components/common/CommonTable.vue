<template>
  <el-table :data="data" v-loading="loading" border>
    <slot />
  </el-table>
</template>

<script setup lang="ts">
defineProps<{
  data: any[]
  loading?: boolean
}>()
</script>
