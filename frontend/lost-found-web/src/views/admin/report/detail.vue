<template>
  <div class="page-card">
    <h2>举报详情</h2>
    <div>物品ID：{{ detail.itemId }}</div>
    <div>举报人：{{ detail.reporterId }}</div>
    <div>原因：{{ detail.reason }}</div>
    <div>详情：{{ detail.detail }}</div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getReportDetailApi } from '@/api/report'

const route = useRoute()
const detail = ref<any>({})

onMounted(async () => {
  detail.value = await getReportDetailApi(Number(route.params.id))
})
</script>
