<template>
  <el-tag :type="type">{{ text }}</el-tag>
</template>

<script setup lang="ts">
defineProps<{
  type?: '' | 'success' | 'warning' | 'info' | 'danger'
  text: string
}>()
</script>
