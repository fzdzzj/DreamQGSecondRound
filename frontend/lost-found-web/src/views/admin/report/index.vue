<template>
  <div class="page-card">
    <h2>举报管理</h2>

    <CommonTable :data="list">
      <el-table-column prop="itemId" label="物品ID" />
      <el-table-column prop="reporterId" label="举报人" />
      <el-table-column prop="statusDesc" label="状态" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button @click="toDetail(scope.row.id)">详情</el-button>
          <el-button type="primary" @click="audit(scope.row.id, 2)">通过</el-button>
          <el-button type="danger" @click="audit(scope.row.id, 3)">驳回</el-button>
        </template>
      </el-table-column>
    </CommonTable>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { auditReportApi, getReportPageApi } from '@/api/report'
import { ReportListVO } from '@/types/report'
import CommonTable from '@/components/common/CommonTable.vue'

const list = ref<ReportListVO[]>([])

const load = async () => {
  const res = await getReportPageApi({ pageNum: 1, pageSize: 10 })
  list.value = res.list
}

onMounted(load)

const audit = async (id: number, status: number) => {
  await auditReportApi(id, { status, remark: '' })
  await load()
}

const toDetail = (id: number) => {
  location.href = `/admin/report/${id}`
}
</script>
