<template>
  <div class="page-card">
    <h2>置顶详情</h2>
    <div>物品ID：{{ detail.itemId }}</div>
    <div>申请人：{{ detail.applicantId }}</div>
    <div>理由：{{ detail.reason }}</div>
    <div>状态：{{ detail.statusDesc }}</div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getPinDetailApi } from '@/api/pin'

const route = useRoute()
const detail = ref<any>({})

onMounted(async () => {
  detail.value = await getPinDetailApi(Number(route.params.id))
})
</script>
