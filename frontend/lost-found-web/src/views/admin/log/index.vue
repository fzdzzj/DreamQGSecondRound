<template>
  <div class="page-card">
    <h2>操作日志</h2>

    <el-form :inline="true" style="margin-bottom:16px">
      <el-form-item label="操作人">
        <el-input v-model="query.userName" placeholder="操作人" clearable />
      </el-form-item>
      <el-form-item label="对象类型">
        <el-input v-model="query.targetType" placeholder="对象类型" clearable />
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <CommonTable :data="list" :loading="loading">
<el-table-column prop="id" label="ID" width="80" />
<el-table-column prop="userId" label="操作人" width="120" /> <!-- 修改为 userId -->
<el-table-column prop="actionTypeDesc" label="操作内容" /> <!-- 修改为 actionTypeDesc -->
<el-table-column prop="actionType" label="对象类型" width="120" /> <!-- 修改为 actionType -->
<el-table-column prop="id" label="对象ID" width="100" /> <!-- 如果没有 targetId，可暂时用 id 占位 -->
<el-table-column prop="actionTypeDesc" label="备注" /> <!-- 使用 actionTypeDesc 作为备注 -->
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
  userName: '',
  targetType: ''
})
const dateRange = ref<string[]>([])

const { pageNum, pageSize, total, setPagination } = usePagination()

const load = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      userName: query.userName,
      targetType: query.targetType,
      startTime: dateRange.value[0] ? `${dateRange.value[0]} 00:00:00` : undefined,
      endTime: dateRange.value[1] ? `${dateRange.value[1]} 23:59:59` : undefined
    }
    
    const res = await getOperationLogPageApi(params)
    
    // 调试建议：先打印 res 查看实际结构
    // console.log('API Res:', res)

    // 方案 A：如果拦截器已解包（res 直接是 { list, total... }）
    if (res && res.list) {
      list.value = res.list
      setPagination(res) // setPagination 通常需要 { total, pageNum, pageSize }
    } 
    // 方案 B：如果拦截器未解包（res 是 { code, data: { list... }, ... }）
    else if (res && res.data && res.data.list) {
      list.value = res.data.list
      setPagination(res.data)
    } 
    else {
      console.warn('Unexpected response structure:', res)
      list.value = []
    }
    
  } catch (error) {
    console.error('Load logs failed:', error)
    list.value = []
  } finally {
    loading.value = false
  }
}

const reset = () => {
  query.userName = ''
  query.targetType = ''
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
