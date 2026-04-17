<template>
  <div class="page-card">
    <h2>置顶申请管理</h2>

    <CommonTable :data="list">
      <el-table-column prop="id" label="ID" width="100" />
      <el-table-column prop="itemId" label="物品ID" width="120" />
      <el-table-column prop="userId" label="申请人ID" width="100" />
      <el-table-column prop="statusDesc" label="状态" width="120" />
      <el-table-column prop="createTime" label="提交时间" min-width="180" />
      <el-table-column label="操作" width="240">
        <template #default="scope">
          <el-button type="primary" @click="openAuditDialog(scope.row, 2)">通过</el-button>
          <el-button type="danger" @click="openAuditDialog(scope.row, 3)">驳回</el-button>
        </template>
      </el-table-column>
    </CommonTable>

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
import { ref, reactive, onMounted } from 'vue'
import { getPinPageApi, auditPinApi } from '@/api/pin'
import { showSuccess, showWarning } from '@/utils/message'
import CommonTable from '@/components/common/CommonTable.vue'

const list = ref<any[]>([])
const auditVisible = ref(false)
const submitting = ref(false)

const auditForm = reactive({
  id: 0,
  itemId: 0,
  status: 2,
  remark: ''
})

const load = async () => {
  const res = await getPinPageApi({ pageNum: 1, pageSize: 10 })
  list.value = res.list
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
