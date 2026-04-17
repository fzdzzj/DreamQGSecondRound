<template>
  <div class="page-card">
    <h2>我的物品待审批认领</h2>

    <CommonTable :data="list" :loading="loading">
      <el-table-column prop="id" label="申请ID" width="100" />
      <el-table-column prop="itemId" label="物品ID" width="120" />
      <el-table-column prop="requesterName" label="申请人" width="120" />
      <el-table-column prop="statusDesc" label="状态" width="120" />
      <el-table-column prop="createTime" label="申请时间" min-width="180" />
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === 1"
            type="primary"
            @click="openAuditDialog(scope.row, 2)"
          >
            同意
          </el-button>
          <el-button
            v-if="scope.row.status === 1"
            type="danger"
            @click="openAuditDialog(scope.row, 3)"
          >
            拒绝
          </el-button>
        </template>
      </el-table-column>
    </CommonTable>

    <el-dialog v-model="auditVisible" :title="auditForm.status === 2 ? '同意认领' : '拒绝认领'" width="520px">
      <el-form label-width="90px">
        <el-form-item label="申请ID">
          <span>{{ auditForm.id }}</span>
        </el-form-item>
        <el-form-item label="物品ID">
          <span>{{ auditForm.itemId }}</span>
        </el-form-item>
        <el-form-item label="操作备注">
          <el-input v-model="auditForm.remark" type="textarea" :rows="4" placeholder="请输入审核备注" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAudit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getPendingClaimRequestsApi, approveClaimRequestApi } from '@/api/claim'
import CommonTable from '@/components/common/CommonTable.vue'
import { showSuccess, showWarning } from '@/utils/message'

const list = ref<any[]>([])
const loading = ref(false)

const auditVisible = ref(false)
const submitting = ref(false)
const auditForm = reactive({
  id: 0,
  itemId: 0,
  status: 2,
  remark: ''
})

const load = async () => {
  loading.value = true
  try {
    const res = await getPendingClaimRequestsApi()
    list.value = res.data
  } finally {
    loading.value = false
  }
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
    await approveClaimRequestApi(auditForm.id, {
      status: auditForm.status,
      remark: auditForm.remark
    })
    showSuccess('操作成功')
    auditVisible.value = false
    await load()
  } finally {
    submitting.value = false
  }
}
</script>
