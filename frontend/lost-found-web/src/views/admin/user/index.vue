<template>
  <div class="page-card">
    <h2>用户管理</h2>

    <div class="toolbar">
      <SearchForm @search="load" @reset="reset">
        <el-form-item label="用户ID">
          <el-input v-model="query.id" placeholder="用户ID" clearable />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="query.username" placeholder="用户名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="query.role" placeholder="选择角色" clearable>
            <el-option :value="'1'" label="普通用户" />
            <el-option :value="'2'" label="管理员" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="选择状态" clearable>
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
          </el-select>
        </el-form-item>
        <el-form-item label="最后登录时间">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </SearchForm>
    </div>

    <CommonTable :data="list" :loading="loading">
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

    <CommonPagination
      :total="total"
      :pageNum="pageNum"
      :pageSize="pageSize"
      @change="changePage"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { disableAdminUserApi, enableAdminUserApi, getAdminUserPageApi } from '@/api/admin'
import CommonTable from '@/components/common/CommonTable.vue'
import CommonPagination from '@/components/common/CommonPagination.vue'
import SearchForm from '@/components/common/SearchForm.vue'
import { SysUserStatVO } from '@/types/user'
import { usePagination } from '@/hooks/usePagination'

const list = ref<SysUserStatVO[]>([])
const loading = ref(false)

const { pageNum, pageSize, total, setPagination } = usePagination()

const query = reactive({
  id: undefined,
  username: '',
  role: undefined,
  status: undefined
})

const dateRange = ref<string[]>([])

const load = async () => {
  loading.value = true
  try {
    const [startTime, endTime] = dateRange.value || []
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      id: query.id?.toString(),
      username: query.username,
      role: query.role,
      status: query.status,
      startTime: startTime || undefined,
      endTime: endTime || undefined
    }
    
    console.log('发送的参数:', params)
    const res = await getAdminUserPageApi(params)
    list.value = res.list
    setPagination(res)
  } finally {
    loading.value = false
  }
}

const reset = () => {
  query.id = undefined
  query.username = ''
  query.role = undefined
  query.status = undefined
  dateRange.value = []
  pageNum.value = 1
  load()
}

const changePage = (p: number) => {
  pageNum.value = p
  load()
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