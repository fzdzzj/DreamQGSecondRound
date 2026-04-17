<template>
  <div class="page-card">
    <h2>举报详情</h2>

    <el-descriptions :column="1" border>
      <el-descriptions-item label="举报ID">
        {{ detail.id }}
      </el-descriptions-item>

      <el-descriptions-item label="物品ID">
        {{ detail.itemId }}
      </el-descriptions-item>

      <el-descriptions-item label="举报人ID">
        {{ detail.reporterId }}
      </el-descriptions-item>

      <el-descriptions-item label="举报原因">
        {{ detail.reason }}
      </el-descriptions-item>

      <el-descriptions-item label="举报详情">
        {{ detail.detail || '-' }}
      </el-descriptions-item>

      <el-descriptions-item label="当前状态">
        {{ detail.statusDesc || detail.status }}
      </el-descriptions-item>

      <el-descriptions-item label="审核备注">
        {{ detail.auditRemark || '-' }}
      </el-descriptions-item>

      <el-descriptions-item label="审核时间">
        {{ detail.auditTime || '-' }}
      </el-descriptions-item>

      <el-descriptions-item label="提交时间">
        {{ detail.createTime || '-' }}
      </el-descriptions-item>
    </el-descriptions>

    <el-divider>审核操作</el-divider>

    <el-form label-width="90px" style="max-width: 700px">
      <el-form-item label="操作备注">
        <el-input
          v-model="remark"
          type="textarea"
          :rows="4"
          placeholder="请输入审核备注"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="submit(2)">
          通过
        </el-button>
        <el-button type="danger" :loading="submitting" @click="submit(3)">
          驳回
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { auditReportApi, getReportDetailApi } from '@/api/report'
import { showSuccess, showWarning } from '@/utils/message'

const route = useRoute()
const router = useRouter()

const detail = ref<any>({})
const remark = ref('')
const submitting = ref(false)

const loadDetail = async () => {
  detail.value = await getReportDetailApi(Number(route.params.id))
}

onMounted(loadDetail)

const submit = async (status: number) => {
  if (!remark.value.trim()) {
    showWarning('请输入操作备注')
    return
  }

  submitting.value = true
  try {
    await auditReportApi(Number(route.params.id), {
      status,
      remark: remark.value
    })
    showSuccess('审核成功')
    await loadDetail()
    router.push('/admin/report')
  } finally {
    submitting.value = false
  }
}
</script>
