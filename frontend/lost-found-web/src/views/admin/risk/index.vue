<template>
  <div class="page-card">
    <h2>风险事件</h2>

    <CommonTable :data="list">
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="riskLevel" label="级别" />
      <el-table-column prop="handleStatus" label="处理状态" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button @click="toDetail(scope.row.id)">详情</el-button>
        </template>
      </el-table-column>
    </CommonTable>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getRiskPageApi } from '@/api/risk'
import { BizRiskEvent } from '@/types/risk'
import CommonTable from '@/components/common/CommonTable.vue'

const list = ref<BizRiskEvent[]>([])

onMounted(async () => {
  const res = await getRiskPageApi({ pageNum: 1, pageSize: 10 })
  list.value = res.list
})

const toDetail = (id: number) => {
  location.href = `/admin/risk/${id}`
}
</script>
