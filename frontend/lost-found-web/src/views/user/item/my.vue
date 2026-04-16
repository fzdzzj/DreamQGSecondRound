<template>
  <div class="page-card">
    <h2>我的物品</h2>

    <div class="toolbar">
      <SearchForm @search="load" @reset="reset">
        <el-form-item label="搜索">
          <el-input v-model="query.keyword" placeholder="关键词/地点" clearable />
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
import { deleteItemApi, getItemDetailApi, getMyItemPageApi, updateItemApi } from '@/api/item'
import CommonTable from '@/components/common/CommonTable.vue'
import CommonPagination from '@/components/common/CommonPagination.vue'
import SearchForm from '@/components/common/SearchForm.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { itemStatusTagType, itemStatusText } from '@/utils/item'
import { showSuccess } from '@/utils/message'
import { usePagination } from '@/hooks/usePagination'

const router = useRouter()
const list = ref<any[]>([])
const loading = ref(false)

const query = reactive({
  keyword: '',
  type: undefined,
  aiCategory: ''
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
      type: query.type,
      aiCategory: query.aiCategory,
      startTime: dateRange.value[0] ? `${dateRange.value[0]}T00:00:00` : undefined,
      endTime: dateRange.value[1] ? `${dateRange.value[1]}T23:59:59` : undefined
    }
    
    const res = await getMyItemPageApi(params)
    list.value = res.list
    setPagination(res)
  } finally {
    loading.value = false
  }
}

const reset = () => {
  query.keyword = ''
  query.type = undefined
  query.aiCategory = ''
  dateRange.value = []
  pageNum.value = 1
  load()
}

const changePage = (p: number) => {
  pageNum.value = p
  load()
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