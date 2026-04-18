<template>
  <div class="page-card">
    <h2>置顶申请管理</h2>

    <div class="toolbar">
      <SearchForm @search="load" @reset="reset">
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable>
            <el-option label="待审核" :value="1" />
            <el-option label="已通过" :value="2" />
            <el-option label="已驳回" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请人ID">
          <el-input v-model="query.applicantId" placeholder="申请人ID" clearable />
        </el-form-item>
        <el-form-item label="物品ID">
          <el-input v-model="query.itemId" placeholder="物品ID" clearable />
        </el-form-item>
      </SearchForm>
    </div>

    <CommonTable :data="list">
      <el-table-column prop="id" label="ID" width="100" />
      <el-table-column prop="itemId" label="物品ID" width="120" />
      <el-table-column prop="applicantId" label="申请人ID" width="100" />
      <el-table-column label="状态" width="120">
        <template #default="scope">
          {{ getStatusText(scope.row.status) }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" min-width="180" />
      <el-table-column label="操作" width="240">
        <template #default="scope">
          <el-button type="primary" @click="openAuditDialog(scope.row, 2)" v-if="Number(scope.row.status) === 1">通过</el-button>
          <el-button type="danger" @click="openAuditDialog(scope.row, 3)" v-if="Number(scope.row.status) === 1">驳回</el-button>
          <span v-else> - </span>
        </template>
      </el-table-column>
    </CommonTable>

    <CommonPagination
      :total="total"
      :pageNum="pageNum"
      :pageSize="pageSize"
      @change="changePage"
    />

    <el-dialog v-model="auditVisible" :title="auditForm.status === 2 ? '通过置顶' : '驳回置顶'" width="520px">
      <el-form label-width="90px">
        <el-form-item label="置顶ID">
          <span>{{ auditForm.id }}</span>
        </el-form-item>
        <el-form-item label="物品ID">
          <span>{{ auditForm.itemId }}</span>
        </el-form-item>
        <el-form-item label="操作备注">
          <el-input
            v-model="auditForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入审核备注"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAudit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { getPinPageApi, auditPinApi } from '@/api/pin'
import { showSuccess, showWarning } from '@/utils/message'
import CommonTable from '@/components/common/CommonTable.vue'
import CommonPagination from '@/components/common/CommonPagination.vue'
import { usePagination } from '@/hooks/usePagination'
import SearchForm from '@/components/common/SearchForm.vue'

const list = ref<any[]>([])
const auditVisible = ref(false)
const submitting = ref(false)

const { pageNum, pageSize, total, setPagination } = usePagination()

const query = reactive({
  status: undefined,
  applicantId: undefined,
  itemId: undefined
})

const auditForm = reactive({
  id: 0,
  itemId: 0,
  status: 2,
  remark: ''
})

const load = async () => {
  const res = await getPinPageApi({
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    status: query.status?.toString(),
    applicantId: query.applicantId?.toString(),
    itemId: query.itemId?.toString()
  })
  list.value = res.list
  setPagination(res)
}

const reset = () => {
  query.status = undefined
  query.applicantId = undefined
  query.itemId = undefined
  pageNum.value = 1
  load()
}

const changePage = (val: number) => {
  pageNum.value = val
  load()
}

const getStatusText = (status: number | string) => {
  const numStatus = Number(status)
  const map: Record<number, string> = {
    1: '待审核',
    2: '已通过',
    3: '已驳回'
  }
  return map[numStatus] || '未知状态'
}

onMounted(load)

const openAuditDialog = (row: any, status: number) => {
  auditForm.id = row.id
  auditForm.itemId = row.itemId
  auditForm.status = status
  auditForm.remark = ''
  auditVisible.value = true
}

const submitAudit = async () => {
  if (!auditForm.remark.trim()) {
    showWarning('请输入操作备注')
    return
  }

  submitting.value = true
  try {
    await auditPinApi({
      requestId: auditForm.id,
      status: auditForm.status,
      remark: auditForm.remark
    })
    showSuccess('审核成功')
    auditVisible.value = false
    await load()
  } finally {
    submitting.value = false
  }
}
</script>