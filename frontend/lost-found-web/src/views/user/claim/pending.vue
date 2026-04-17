<template>
  <div class="page-card">
    <h2>我的物品认领申请</h2>

    <!-- 状态筛选栏 -->
    <div class="query-bar" style="margin-bottom:15px;">
      <el-select v-model="queryStatus" placeholder="请选择状态" style="width:160px;">
        <el-option label="全部" value="" />
        <el-option label="待审批" value="1" />
        <el-option label="已同意" value="2" />
        <el-option label="已拒绝" value="3" />
        <el-option label="需要更多信息" value="4" />
      </el-select>
      <el-button type="primary" @click="load" style="margin-left:10px;">
        查询
      </el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="filteredList" :loading="loading" border style="width:100%">
      <el-table-column prop="id" label="申请ID" width="100" />
      <el-table-column prop="itemId" label="物品ID" width="120" />
      <el-table-column prop="applicantId" label="申请人ID" width="120" />
      
      <el-table-column label="状态" width="120">
        <template #default="scope">
          {{ scope.row.status === '1' ? '待审批' : 
             scope.row.status === '2' ? '已同意' : 
             scope.row.status === '3' ? '已拒绝' : 
             scope.row.status === '4' ? '需要更多信息' : '未知' }}
        </template>
      </el-table-column>

      <el-table-column prop="pickupCode" label="取件码" width="140">
        <template #default="scope">
          {{ scope.row.pickupCode || '未生成' }}
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

    <!-- 审核弹窗 -->
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
import { ref, reactive, onMounted, computed } from 'vue'
import { getPendingClaimRequestsApi, approveClaimRequestApi } from '@/api/claim'
import { showSuccess, showWarning } from '@/utils/message'

// 原始数据
const list = ref([])
const loading = ref(false)

// 状态筛选
const queryStatus = ref('')

// 计算属性：根据状态筛选
const filteredList = computed(() => {
  if (!queryStatus.value) return list.value
  return list.value.filter(item => item.status === queryStatus.value)
})

// 审核弹窗
const auditVisible = ref(false)
const submitting = ref(false)
const auditForm = reactive({
  id: 0,
  itemId: 0,
  status: 2,
  remark: ''
})

// 加载数据
const load = async () => {
  loading.value = true
  try {
    const res = await getPendingClaimRequestsApi(queryStatus.value)
    list.value = res || []
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

onMounted(load)

// 打开审核框
const openAuditDialog = (row, status) => {
  auditForm.id = row.id
  auditForm.itemId = row.itemId
  auditForm.status = status
  auditForm.remark = ''
  auditVisible.value = true
}

// 提交审核
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
    load() // 重新加载
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-card {
  padding: 20px;
}
</style>