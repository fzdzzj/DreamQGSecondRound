<template>
  <div class="page-card">
    <h2>统计面板</h2>
    
    <div class="toolbar">
      <el-form inline>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="loadData"
          />
        </el-form-item>
      </el-form>
    </div>
    
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
const dateRange = ref<[string, string]>([])

// 初始化默认日期范围（开始时间为当前时间减去7天，结束时间为当前时间）
const initDateRange = () => {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 7)
  
  // 格式化时间为后端期望的格式：YYYY-MM-DD HH:mm:ss
  const formatDate = (date: Date) => {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  }
  
  dateRange.value = [
    formatDate(start),
    formatDate(end)
  ]
}

const loadData = async () => {
  const [startTime, endTime] = dateRange.value || []
  const params = {
    startTime: startTime || undefined,
    endTime: endTime || undefined
  }
  
  data.value = await getAdminStatisticsApi(params)
}

onMounted(() => {
  initDateRange()
  loadData()
})
</script>