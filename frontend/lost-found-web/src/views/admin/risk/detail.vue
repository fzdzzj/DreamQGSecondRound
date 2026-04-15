<template>
  <div class="page-card">
    <h2>风险详情</h2>

    <div>标题：{{ detail.title }}</div>
    <div>内容：{{ detail.content }}</div>

    <el-button type="primary" @click="handle(2)">处理完成</el-button>
    <el-button type="danger" @click="handle(3)">忽略</el-button>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getRiskDetailApi, handleRiskApi } from '@/api/risk'

const route = useRoute()
const detail = ref<any>({})

onMounted(async () => {
  detail.value = await getRiskDetailApi(Number(route.params.id))
})

const handle = async (status: number) => {
  await handleRiskApi(Number(route.params.id), {
    handleStatus: status,
    handleRemark: ''
  })
}
</script>
