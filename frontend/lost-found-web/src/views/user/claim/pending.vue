<template>
  <div class="page-card">
    <h2>我的物品待审批认领</h2>

    <!-- 关键修复：把 CommonTable 换成原生 el-table -->
    <el-table :data="list" :loading="loading" border style="width:100%">
      <el-table-column prop="id" label="申请ID" width="100" />
      <el-table-column prop="itemId" label="物品ID" width="120" />
      <el-table-column prop="applicantId" label="申请人ID" width="120" />
      
      <el-table-column label="状态" width="120">
        <template #default="scope">
          {{ scope.row.status === '1' ? '待审批' : 
             scope.row.status === '2' ? '已同意' : '已拒绝' }}
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="申请时间" min-width="180" />
      
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === '1'"
            type="primary"
            @click="openAuditDialog(scope.row, 2)"
          >
            同意
          </el-button>
          <el-button
            v-if="scope.row.status === '1'"
            type="danger"
            @click="openAuditDialog(scope.row, 3)"
          >
            拒绝
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="auditVisible" :title="auditForm.status === 2 ? '同意认领' : '拒绝认领'" width="520px">
      <el-form label-width="90px">
        <el-form-item label="申请ID">
          <span>{{ auditForm.id }}</span>
        </el-form-item>
        <el-form-item label="物品ID">
          <span>{{ auditForm.itemId }}</span>
        </el-form-item>
        <el-form-item label="操作备注">
          <el-input v-model="auditForm.remark" type="textarea" :rows="4" placeholder="请输入操作备注" />
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
import { ref, reactive, onMounted, markRaw } from 'vue'
import { getPendingClaimRequestsApi, approveClaimRequestApi } from '@/api/claim'
import { showSuccess, showWarning } from '@/utils/message'

// 核心修复：初始化必须是空数组，不能是 any[]
const list = ref([])  
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
    console.log("原始接口返回：", res)
    
    // ✅ 核心修复：强行覆盖数组，触发 Vue 视图更新
    list.value = [...res]  // 这里是关键！！！
    
    console.log("最终表格数据：", list.value)
  } catch (err) {
    console.error("加载失败", err)
  } finally {
    loading.value = false
  }
}

onMounted(load)

const openAuditDialog = (row, status) => {
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
    load()
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-card { padding: 20px; }
</style>