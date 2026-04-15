<template>
  <div class="page-card">
    <div class="toolbar">
      <SearchForm @search="load" @reset="reset">
        <el-form-item>
          <el-input v-model="query.keyword" placeholder="关键词" clearable />
        </el-form-item>
        <el-form-item>
          <el-input v-model="query.location" placeholder="地点" clearable />
        </el-form-item>
      </SearchForm>

      <div>
        <el-button type="primary" @click="$router.push('/item/publish-lost')">发布丢失</el-button>
        <el-button type="success" @click="$router.push('/item/publish-found')">发布拾取</el-button>
      </div>
    </div>

    <CommonTable :data="list" :loading="loading">
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="location" label="地点" />
      <el-table-column prop="happenTime" label="时间" />
      <el-table-column prop="statusDesc" label="状态" />
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button link type="primary" @click="toDetail(scope.row.id)">查看</el-button>
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
import { useRouter } from 'vue-router'
import { getItemPageApi } from '@/api/item'
import { usePagination } from '@/hooks/usePagination'
import CommonTable from '@/components/common/CommonTable.vue'
import CommonPagination from '@/components/common/CommonPagination.vue'
import SearchForm from '@/components/common/SearchForm.vue'

const router = useRouter()
const list = ref<any[]>([])
const loading = ref(false)

const query = reactive({
  keyword: '',
  location: ''
})

const { pageNum, pageSize, total, setPagination } = usePagination()

const load = async () => {
  loading.value = true
  try {
    const res = await getItemPageApi({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: query.keyword,
      location: query.location
    })
    list.value = res.list
    setPagination(res)
  } finally {
    loading.value = false
  }
}

const reset = () => {
  query.keyword = ''
  query.location = ''
  pageNum.value = 1
  load()
}

const changePage = (p: number) => {
  pageNum.value = p
  load()
}

const toDetail = (id: number) => {
  router.push(`/item/detail/${id}`)
}

onMounted(load)
</script>
