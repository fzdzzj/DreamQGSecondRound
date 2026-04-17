<template>
  <div class="page-card">
    <h2>物品管理</h2>

    <CommonTable :data="list">
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="statusDesc" label="状态" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button type="danger" @click="remove(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </CommonTable>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { adminDeleteItemApi } from '@/api/admin'
import { getItemPageApi } from '@/api/item'
import { BizItemStatVO } from '@/types/item'
import CommonTable from '@/components/common/CommonTable.vue'

const list = ref<BizItemStatVO[]>([])

const load = async () => {
  const params = {
    pageNum: 1,
    pageSize: 10,
    type: undefined,
    startTime: undefined,
    endTime: undefined
  }
  
  console.log('发送的参数:', params)
  const res = await getItemPageApi(params)
  list.value = res.list
}

onMounted(load)

const remove = async (id: number) => {
  await adminDeleteItemApi(id)
  await load()
}
</script>