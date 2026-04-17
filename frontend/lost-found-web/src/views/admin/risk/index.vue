<template>
  <div class="page-card">
    <h2>风险事件管理</h2>

    <el-form :inline="true" style="margin-bottom:16px">
      <el-form-item label="状态">
        <el-select v-model="filters.status" placeholder="全部">
          <el-option label="未处理" :value="1" />
          <el-option label="处理中" :value="2" />
          <el-option label="已处理" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">查询</el-button>
      </el-form-item>
    </el-form>

    <CommonTable :data="list">
      <el-table-column prop="id" label="事件ID" width="100" />
      <el-table-column prop="itemId" label="物品ID" width="120" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="statusDesc" label="状态" width="120" />
      <el-table-column prop="createTime" label="创建时间" min-width="180" />
      <el-table-column prop="updateTime" label="更新时间" min-width="180" />
    </CommonTable>

    <el-pagination
      style="margin-top: 16px; text-align: right"
      background
      layout="prev, pager, next, jumper"
      :current-page.sync="pageNum"
      :page-size="pageSize"
      :total="total"
      @current-change="handlePageChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getRiskPageApi, RiskQueryParams } from '@/api/risk'
import CommonTable from '@/components/common/CommonTable.vue'

const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const list = ref([])
const filters = reactive({
  handleStatus: '',
  riskType: ''
})

const loadRiskPage = async () => {
  const res = await getRiskPageApi({
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    handleStatus: filters.handleStatus || undefined,
    riskType: filters.riskType || undefined
  })

  list.value = res.data.list
  total.value = res.data.total
}


onMounted(loadRiskPage)

const handlePageChange = (val: number) => {
  pageNum.value = val
  loadRiskPage()
}
</script>
