<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item v-for="item in list" :key="item">
      {{ item }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { computed } from 'vue'

const route = useRoute()

const list = computed(() => {
  return (route.meta.title as string)?.split('/') || ['页面']
})
</script>
