<template>
  <div class="page-card">
    <h2>操作日志</h2>

    <el-form :inline="true" style="margin-bottom:16px">
      <el-form-item label="用户ID">
        <el-input v-model="query.userId" placeholder="用户ID" clearable />
      </el-form-item>
      <el-form-item label="操作类型">
        <el-select v-model="query.operationType" placeholder="全部" clearable>
          <el-option label="认领" value="1" />
          <el-option label="编辑" value="2" />
          <el-option label="发布" value="3" />
          <el-option label="管理员" value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <CommonTable :data="list" :loading="loading">
<el-table-column prop="id" label="ID" width="80" />
<el-table-column prop="userId" label="用户" width="120" /> <!-- 修改为 userId -->
<el-table-column prop="actionTypeDesc" label="操作内容" /> <!-- 修改为 actionTypeDesc -->
<el-table-column prop="createTime" label="时间" min-width="180" />
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
import { ref, reactive, onMounted } from 'vue'
import CommonTable from '@/components/common/CommonTable.vue'
import CommonPagination from '@/components/common/CommonPagination.vue'
import { getOperationLogPageApi } from '@/api/log'
import { usePagination } from '@/hooks/usePagination'

const list = ref<any[]>([])
const loading = ref(false)

const query = reactive({
  userId: '',
  operationType: ''
})
const dateRange = ref<string[]>([])

const { pageNum, pageSize, total, setPagination } = usePagination()

const load = async () => {
  loading.value = true
  try {
    const [startTime, endTime] = dateRange.value || []
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      userId: query.userId || undefined,
      operationType: query.operationType || undefined,
      startTime: startTime || undefined,
      endTime: endTime || undefined
    }
    
    console.log('发送的参数:', params)
    const res = await getOperationLogPageApi(params)
    
    list.value = res.list
    setPagination(res)
  } finally {
    loading.value = false
  }
}

const reset = () => {
  query.userId = ''
  query.operationType = ''
  dateRange.value = []
  pageNum.value = 1
  load()
}

const changePage = (p: number) => {
  pageNum.value = p
  load()
}

onMounted(load)
</script>