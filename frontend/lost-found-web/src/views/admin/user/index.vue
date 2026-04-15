<template>
  <div class="page-card">
    <h2>用户管理</h2>

    <CommonTable :data="list">
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="roleDesc" label="角色" />
      <el-table-column prop="statusDesc" label="状态" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small" @click="disable(scope.row.id)">禁用</el-button>
          <el-button size="small" type="success" @click="enable(scope.row.id)">启用</el-button>
        </template>
      </el-table-column>
    </CommonTable>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { disableAdminUserApi, enableAdminUserApi, getAdminUserPageApi } from '@/api/admin'
import CommonTable from '@/components/common/CommonTable.vue'
import { SysUserStatVO } from '@/types/user'
const list = ref<SysUserStatVO[]>([])

const load = async () => {
  const res = await getAdminUserPageApi({ pageNum: 1, pageSize: 10 })
  list.value = res.list
}

onMounted(load)

const disable = async (id: number) => {
  await disableAdminUserApi(id)
  await load()
}

const enable = async (id: number) => {
  await enableAdminUserApi(id)
  await load()
}
</script>
