<template>
  <el-pagination
    background
    layout="total, prev, pager, next"
    :total="total"
    :page-size="pageSize"
    :current-page="pageNum"
    @current-change="$emit('change', $event)"
  />
</template>

<script setup lang="ts">
defineProps<{
  total: number
  pageSize: number
  pageNum: number
}>()
</script>
