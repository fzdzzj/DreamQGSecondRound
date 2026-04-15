<template>
  <div class="page-card">
    <h2>我的举报</h2>

    <CommonTable :data="list">
      <el-table-column prop="itemId" label="物品ID" />
      <el-table-column prop="statusDesc" label="状态" />
    </CommonTable>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getReportPageApi } from '@/api/report'
import CommonTable from '@/components/common/CommonTable.vue'
import { ReportListVO } from '@/types/report'

const list = ref<ReportListVO[]>([])

onMounted(async () => {
  const res = await getReportPageApi({ pageNum: 1, pageSize: 10 })
  list.value = res.list
})
</script>
