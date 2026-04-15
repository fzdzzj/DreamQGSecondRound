<template>
  <div class="page-card">
    <h2>我的物品</h2>

    <CommonTable :data="list">
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="statusDesc" label="状态" />
      <el-table-column label="操作" width="260">
        <template #default="scope">
          <el-button link type="primary" @click="edit(scope.row.id)">编辑</el-button>
          <el-button link type="warning" @click="close(scope.row.id)">关闭</el-button>
          <el-button link type="danger" @click="remove(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </CommonTable>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { closeItemApi, deleteItemApi, getMyItemPageApi } from '@/api/item'
import CommonTable from '@/components/common/CommonTable.vue'
import { showSuccess } from '@/utils/message'
import {BizItemStatVO } from '@/types/item'
const router = useRouter()
const list = ref<BizItemStatVO[]>([])

const load = async () => {
  const res = await getMyItemPageApi({ pageNum: 1, pageSize: 20 })
  list.value = res.list
}

onMounted(load)

const edit = (id: number) => {
  router.push(`/item/edit/${id}`)
}

const close = async (id: number) => {
  await closeItemApi(id)
  showSuccess('关闭成功')
  await load()
}

const remove = async (id: number) => {
  await deleteItemApi(id)
  showSuccess('删除成功')
  await load()
}
</script>
