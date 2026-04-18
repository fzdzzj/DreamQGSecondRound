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
          <el-button size="small" type="primary" @click="viewItem(scope.row.itemId)">
            查看物品
          </el-button>
          <el-button size="small" type="primary" @click="handleReport(scope.row)" v-if="Number(scope.row.status) === 1" style="margin-left: 8px">
            处理
          </el-button>
          <span v-else style="margin-left: 8px">已处理</span>
        </template>
      </el-table-column>
    </CommonTable>

    <el-dialog v-model="auditDialogVisible" title="处理举报" width="500px">
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="举报ID">
          <span>{{ auditForm.reportId }}</span>
        </el-form-item>
        <el-form-item label="处理结果">
          <el-radio-group v-model="auditForm.action">
            <el-radio :label="1">通过（删除违规内容）</el-radio>
            <el-radio :label="2">不通过（维持原状）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input
            v-model="auditForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入处理备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <CommonPagination
      :total="total"
      :pageNum="pageNum"
      :pageSize="pageSize"
      @change="changePage"
    />
  </div>
</template>

<script setup lang="ts">
import { ElMessageBox } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'
import { getReportPageApi } from '@/api/admin'
import { auditReportApi } from '@/api/report'
import CommonTable from '@/components/common/CommonTable.vue'
import CommonPagination from '@/components/common/CommonPagination.vue'
import SearchForm from '@/components/common/SearchForm.vue'
import { ReportListVO } from '@/types/report'
import { showSuccess, showError } from '@/utils/message'
import { usePagination } from '@/hooks/usePagination'
import { useRouter } from 'vue-router'

const router = useRouter()
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
      status: query.status?.toString(),
      reporterId: query.reporterId?.toString(),
      itemId: query.itemId?.toString(),
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

const auditDialogVisible = ref(false)
const submitting = ref(false)

const auditForm = reactive({
  reportId: 0,
  action: 1, // 默认为通过
  remark: ''
})

const viewItem = (itemId: number) => {
  router.push(`/item/detail/${itemId}`)
}

const handleReport = (row: ReportListVO) => {
  auditForm.reportId = row.id
  auditForm.remark = ''
  auditDialogVisible.value = true
}

const submitAudit = async () => {
  if (!auditForm.remark.trim()) {
    showError('请输入处理备注')
    return
  }

  submitting.value = true
  try {
    const auditData = {
      status: '2', // 设置为已处理
      remark: auditForm.remark + ` (处理结果: ${auditForm.action === 1 ? '通过' : '不通过'})`
    }
    
    await auditReportApi(auditForm.reportId, auditData)
    showSuccess('处理成功')
    auditDialogVisible.value = false
    await load() // 重新加载数据
  } catch (error) {
    console.error('处理举报失败:', error)
    showError('处理失败')
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>