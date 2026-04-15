<template>
  <div class="page-card">
    <h2>我的置顶申请</h2>

    <CommonTable :data="list">
      <el-table-column prop="itemId" label="物品ID" />
      <el-table-column prop="statusDesc" label="状态" />
    </CommonTable>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getMyPinListApi } from '@/api/pin'
import CommonTable from '@/components/common/CommonTable.vue'
import { PinRequestStatVO } from '@/types/pin'

const list = ref<PinRequestStatVO[]>([])

onMounted(async () => {
  list.value = await getMyPinListApi()
})
</script>
