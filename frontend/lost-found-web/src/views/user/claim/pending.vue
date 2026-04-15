<template>
  <div class="page-card">
    <h2>认领申请</h2>

    <CommonTable :data="list">
      <el-table-column prop="itemId" label="物品ID" />
      <el-table-column prop="verificationAnswer" label="验证答案" />
      <el-table-column prop="status" label="状态" />
    </CommonTable>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getPendingClaimApi } from '@/api/claim'
import CommonTable from '@/components/common/CommonTable.vue'
import { BizClaimRequestVO } from '@/types/claim'

const list = ref<BizClaimRequestVO[]>([])

onMounted(async () => {
  list.value = await getPendingClaimApi()
})
</script>
