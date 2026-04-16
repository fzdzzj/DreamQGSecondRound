<template>
  <div class="page-card">
    <h2>我的物品</h2>

    <CommonTable :data="list">
      <el-table-column prop="title" label="标题" />
      <el-table-column label="状态">
        <template #default="scope">
          <StatusTag
            :type="itemStatusTagType(scope.row.status)"
            :text="itemStatusText(scope.row.status, scope.row.statusDesc)"
          />
        </template>
      </el-table-column>

      <el-table-column label="操作" width="480">
        <template #default="scope">
          <el-button link type="primary" @click="view(scope.row.id)">查看</el-button>
          <el-button link type="primary" @click="edit(scope.row.id)">编辑</el-button>

          <el-button
            link
            type="success"
            v-if="scope.row.status !== 1"
            @click="changeStatus(scope.row, 1)"
          >
            开启
          </el-button>

          <el-button
            link
            type="warning"
            v-if="scope.row.status !== 2"
            @click="changeStatus(scope.row, 2)"
          >
            已匹配
          </el-button>

          <el-button
            link
            type="info"
            v-if="scope.row.status !== 3"
            @click="changeStatus(scope.row, 3)"
          >
            关闭
          </el-button>

          <el-button link type="danger" @click="remove(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </CommonTable>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { deleteItemApi, getItemDetailApi, getMyItemPageApi, updateItemApi } from '@/api/item'
import CommonTable from '@/components/common/CommonTable.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { itemStatusTagType, itemStatusText } from '@/utils/item'
import { showSuccess } from '@/utils/message'

const router = useRouter()
const list = ref<any[]>([])

const load = async () => {
  const res = await getMyItemPageApi({ pageNum: 1, pageSize: 20 })
  list.value = res.list
}

onMounted(load)

const view = (id: number) => {
  router.push(`/item/detail/${id}`)
}

const edit = (id: number) => {
  router.push(`/item/edit/${id}`)
}

const changeStatus = async (row: any, status: number) => {
  const detail = await getItemDetailApi(row.id)

  await updateItemApi(row.id, {
    title: detail.title,
    description: detail.description,
    location: detail.location,
    happenTime: detail.happenTime,
    contactMethod: detail.contactMethod??'',
    imageUrls: detail.imageUrls || [],
    status
  })

  showSuccess('状态更新成功')
  await load()
}

const remove = async (id: number) => {
  await deleteItemApi(id)
  showSuccess('删除成功')
  await load()
}
</script>