<template>
  <div class="page-card">
    <div class="page-title">AI 统计报告</div>

    <el-form :inline="true" class="mb-16">
      <el-form-item label="统计日期">
        <el-date-picker
          v-model="query.statDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="选择日期"
        />
      </el-form-item>

      <el-form-item label="统计类型">
        <el-select v-model="query.statType" style="width:140px">
          <el-option label="日统计" value="1" />
          <el-option label="周统计" value="2" />
          <el-option label="月统计" value="3" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="16" v-loading="loading">
      <el-col
        :span="12"
        v-for="(item,index) in list"
        :key="index"
        class="mb-16"
      >
        <el-card shadow="hover">
          <template #header>
            <div class="flex-between">
              <span>{{ item.modelName }}</span>
              <span class="time">{{ item.createTime }}</span>
            </div>
          </template>

          <div class="summary">
            {{ item.aiSummary }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && list.length === 0" description="暂无统计数据" />

    <div class="mt-16 flex-end">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="query.pageSize"
        :current-page="query.pageNum"
        @current-change="changePage"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import {
  getAdminAiStatisticsApi,
  type AdminAiStatisticsVO
} from '@/api/admin-ai'

const loading = ref(false)
const total = ref(0)
const list = ref<AdminAiStatisticsVO[]>([])

const query = reactive({
  statDate: '',
  statType: '1',
  pageNum: 1,
  pageSize: 10
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAdminAiStatisticsApi(query)

    const page = res.data || res

    list.value = page.list || []
    total.value = page.total || 0
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.statDate = ''
  query.statType = '1'
  query.pageNum = 1
  loadData()
}

const changePage = (page: number) => {
  query.pageNum = page
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-title{
  font-size:20px;
  font-weight:600;
  margin-bottom:16px;
}
.flex-between{
  display:flex;
  justify-content:space-between;
  align-items:center;
}
.flex-end{
  display:flex;
  justify-content:flex-end;
}
.mb-16{
  margin-bottom:16px;
}
.mt-16{
  margin-top:16px;
}
.summary{
  white-space:pre-wrap;
  line-height:1.8;
  color:#333;
}
.time{
  color:#999;
  font-size:13px;
}
</style>
