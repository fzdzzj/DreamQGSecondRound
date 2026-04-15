<template>
  <div class="page-card">
    <h2>通知</h2>

    <CommonTable :data="list">
      <el-table-column prop="content" label="内容" />
      <el-table-column prop="isRead" label="已读状态" />
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button link type="primary" @click="read(scope.row.id)">标记已读</el-button>
          <el-button link type="danger" @click="remove(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </CommonTable>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { deleteNotificationApi, getNotificationPageApi, markNotificationReadApi } from '@/api/notification'
import CommonTable from '@/components/common/CommonTable.vue'
import { NotificationVO } from '@/types/notification'

const list = ref<NotificationVO[]>([])

const load = async () => {
  const res = await getNotificationPageApi()
  list.value = res.list
}

onMounted(load)

const read = async (id: number) => {
  await markNotificationReadApi(id)
  await load()
}

const remove = async (id: number) => {
  await deleteNotificationApi(id)
  await load()
}
</script>
