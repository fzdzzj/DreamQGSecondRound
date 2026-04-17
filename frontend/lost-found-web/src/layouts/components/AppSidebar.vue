<template>
  <div class="sidebar">
    <el-menu router>

      <!-- 用户端 -->
      <template v-if="!isAdmin">
        <el-menu-item index="/home">首页</el-menu-item>
        <el-menu-item index="/item/list">物品广场</el-menu-item>
        <el-menu-item index="/item/my">我的物品</el-menu-item>
        <el-menu-item index="/ai">AI助手</el-menu-item>
        <el-menu-item index="/message">私信</el-menu-item>
        <el-menu-item index="/notification">通知</el-menu-item>
        <el-menu-item index="/profile">个人中心</el-menu-item>
        <el-menu-item index="/claim/pending">待处理物品管理</el-menu-item>
      </template>

      <!-- 管理端 -->
      <template v-else>
        <el-menu-item index="/admin/dashboard">统计</el-menu-item>
        <el-menu-item index="/admin/user">用户管理</el-menu-item>
        <el-menu-item index="/admin/report">举报管理</el-menu-item>
        <el-menu-item index="/admin/pin">置顶审核</el-menu-item>
        <el-menu-item index="/admin/risk">风险事件</el-menu-item>
        <el-menu-item index="/admin/item">物品管理</el-menu-item>
        <el-menu-item index="/admin/ai-statistics">AI统计</el-menu-item>
        <el-menu-item index="/admin/log">操作日志</el-menu-item>
      </template>

    </el-menu>
  </div>
</template>

<script setup lang="ts">
defineProps({
  isAdmin: Boolean
})
</script>

<style scoped>
.sidebar {
  width: 220px;
  background: #2d3a4b;
  min-height: calc(100vh - 60px);
}
</style>
