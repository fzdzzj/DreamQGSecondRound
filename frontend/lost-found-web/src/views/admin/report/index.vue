<template>
  <div class="page-card">
    <h2>举报管理</h2>

    <div class="toolbar">
      <SearchForm @search="load" @reset="reset">
        <el-form-item label="举报状态">
          <el-select v-model="query.status" placeholder="选择状态" clearable>
            <el-option :value="1" label="待处理" />
            <el-option :value="2" label="已处理" />
          </el-select>
        </el-form-item>
        <el-form-item label="举报人ID">
          <el-input v-model="query.reporterId" placeholder="举报人ID" clearable />
        </el-form-item>
        <el-form-item label="物品ID">
          <el-input v-model="query.itemId" placeholder="物品ID" clearable />
        </el-form-item>
        <el-form-item label="举报时间">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </SearchForm>
    </div>

    <CommonTable :data="list" :loading="loading">
      <el-table-column prop="id" label="举报ID" />
      <el-table-column prop="itemId" label="物品ID" />
      <el-table-column prop="reporterId" label="举报人ID" />
      <el-table-column prop="content" label="举报内容" />
      <el-table-column prop="statusDesc" label="状态" />
      <el-table-column prop="createTime" label="举报时间" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small" type="primary" @click="handleReport(scope.row)">
            {{ scope.row.status === 1 ? '处理' : '已处理' }}
          </el-button>
        </template>
      </el-table-column>
    </CommonTable>

    <CommonPagination
      :total="total"
      :pageNum="pageNum"
      :pageSize="pageSize"
      @change="changePage"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { getReportPageApi } from '@/api/admin'
import CommonTable from '@/components/common/CommonTable.vue'
import CommonPagination from '@/components/common/CommonPagination.vue'
import SearchForm from '@/components/common/SearchForm.vue'
import { ReportListVO } from '@/types/report'
import { usePagination } from '@/hooks/usePagination'

const list = ref<ReportListVO[]>([])
const loading = ref(false)

const { pageNum, pageSize, total, setPagination } = usePagination()

const query = reactive({
  status: undefined,
  reporterId: undefined,
  itemId: undefined
})

const dateRange = ref<string[]>([])

const load = async () => {
  loading.value = true
  try {
    const [startTime, endTime] = dateRange.value || []
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: query.status,
      reporterId: query.reporterId,
      itemId: query.itemId,
// 将时间格式从 "YYYY-MM-DD HH:mm:ss" 改为 "YYYY-MM-DDTHH:mm:ss"
startTime: startTime ? startTime.replace(' ', 'T') : undefined,
endTime: endTime ? endTime.replace(' ', 'T') : undefined
    }
    
    console.log('发送的参数:', params)
    const res = await getReportPageApi(params)
    list.value = res.list
    setPagination(res)
  } finally {
    loading.value = false
  }
}

const reset = () => {
  query.status = undefined
  query.reporterId = undefined
  query.itemId = undefined
  dateRange.value = []
  pageNum.value = 1
  load()
}

const changePage = (p: number) => {
  pageNum.value = p
  load()
}

const handleReport = (row: ReportListVO) => {
  // 处理举报的逻辑
  console.log('处理举报:', row)
}

onMounted(load)
</script>