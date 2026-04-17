<template>
  <div class="page-card">
    <div class="toolbar">
      <SearchForm @search="load" @reset="reset">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="关键词" clearable />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="query.location" placeholder="地点" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.type" placeholder="选择类型" clearable>
            <el-option :value="1" label="丢失" />
            <el-option :value="2" label="拾取" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="AI分类">
          <el-input v-model="query.aiCategory" placeholder="AI分类" clearable />
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
      <el-table-column label="状态">
        <template #default="scope">
          <StatusTag
            :type="itemStatusTagType(scope.row.status)"
            :text="itemStatusText(scope.row.status, scope.row.statusDesc)"
          />
        </template>
      </el-table-column>
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
import StatusTag from '@/components/common/StatusTag.vue'
import { itemStatusTagType, itemStatusText } from '@/utils/item'

const router = useRouter()
const list = ref<any[]>([])
const loading = ref(false)

const query = reactive({
  keyword: '',
  location: '',
  type: '',
  aiCategory: '',
})

const dateRange = ref<string[]>([])

const { pageNum, pageSize, total, setPagination } = usePagination()

const load = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: query.keyword,
      location: query.location,
      type:  query.type || undefined,
      aiCategory: query.aiCategory,
      startTime: dateRange.value[0] ? `${dateRange.value[0]} 00:00:00` : undefined,
      endTime: dateRange.value[1] ? `${dateRange.value[1]} 23:59:59` : undefined
    }
    
    console.log('发送的参数:', params)
    const res = await getItemPageApi(params)
    list.value = res.list
    setPagination(res)
  } finally {
    loading.value = false
  }
}

const reset = () => {
  query.keyword = ''
  query.location = ''
  query.type = ''
  query.aiCategory = ''
  dateRange.value = []
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