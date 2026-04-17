<template>
  <div class="page-card">
    <h2>风险事件管理</h2>

    <!-- 查询表单 -->
    <el-form :inline="true" style="margin-bottom:16px">
      <el-form-item label="处理状态">
        <el-select v-model="filters.handleStatus" placeholder="全部">
          <el-option label="全部" value="" />
          <el-option label="未处理" value="1" />
          <el-option label="处理中" value="2" />
          <el-option label="已处理" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">查询</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-table :data="list" border width="100%">
      <el-table-column prop="id" label="事件ID" width="100" />
      <el-table-column prop="relatedItemId" label="物品ID" width="120" />
      <el-table-column prop="title" label="风险标题" min-width="200" />
      <el-table-column prop="location" label="地点" width="120" />

      <!-- 状态文字 -->
      <el-table-column label="处理状态" width="120">
        <template #default="scope">
          {{ 
            scope.row.handleStatus === '1' ? '未处理' :
            scope.row.handleStatus === '2' ? '处理中' :
            scope.row.handleStatus === '3' ? '已处理' : '未知'
          }}
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="创建时间" min-width="180" />
      <el-table-column prop="updateTime" label="更新时间" min-width="180" />
    </el-table>

    <!-- 分页 -->
    <el-pagination
      style="margin-top:16px; text-align:right"
      background
      layout="prev, pager, next, jumper"
      v-model:current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      @current-change="loadData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getRiskPageApi } from '@/api/risk'

const list = ref([])  // 数据列表
const total = ref(0)  // 数据总数
const pageNum = ref(1)  // 当前页
const pageSize = ref(10)  // 每页条数

// 查询条件
const filters = reactive({
  handleStatus: ''
})

const loadData = async () => {
  try {
    const res = await getRiskPageApi({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      handleStatus: filters.handleStatus || undefined
    })

    // 检查返回的正确结构
    if (res && res.list) {
      console.log("返回的数据列表：", res.list)  // 查看 list 数据
      list.value = res.list
      total.value = res.total
    } else {
      console.error("接口返回的数据不符合预期", res)
    }
  } catch (err) {
    console.error("加载失败", err)
  }
}



// 组件加载时调用
onMounted(loadData)
</script>

<style scoped>
.page-card {
  padding: 20px;
}
</style>
