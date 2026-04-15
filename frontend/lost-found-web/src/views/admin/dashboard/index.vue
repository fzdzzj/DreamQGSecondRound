<template>
  <div class="page-card">
    <h2>统计面板</h2>
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card>发布数：{{ data.publishCount || 0 }}</el-card>
      </el-col>
      <el-col :span="8">
        <el-card>找回数：{{ data.foundCount || 0 }}</el-card>
      </el-col>
      <el-col :span="8">
        <el-card>活跃用户：{{ data.activeUserCount || 0 }}</el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getAdminStatisticsApi } from '@/api/admin'

const data = ref<any>({})

onMounted(async () => {
  data.value = await getAdminStatisticsApi()
})
</script>
