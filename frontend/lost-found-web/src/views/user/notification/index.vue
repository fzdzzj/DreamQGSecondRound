<template>
  <div class="page-card">
    <h2>通知</h2>

    <CommonTable :data="list">
      <el-table-column prop="content" label="内容" />
      <el-table-column label="已读状态">
        <template #default="scope">
          {{ isReadText(scope.row.isRead) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button 
            v-if="scope.row.isRead === 0" 
            link type="primary" 
            @click="read(scope.row.id)"
          >
            标记已读
          </el-button>
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
import { READ_STATUS_UNREAD, READ_STATUS_READ } from '@/constants/status'
import { showSuccess, showError } from '@/utils/message'
import { confirmDanger } from '@/utils/comfirm'

const isReadText = (isRead: number): string => {
  return isRead === READ_STATUS_READ ? '已读' : '未读'
}

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
  try {
    await confirmDanger('确定要删除这条通知吗？')
    await deleteNotificationApi(id)
    showSuccess('删除成功')
    await load()
  } catch (error) {
    console.error('删除失败:', error)
  }
}
</script>