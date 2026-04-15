<template>
  <div class="header flex-between">
    <div class="title">失物招领系统</div>

    <div class="flex-center">
      <span style="margin-right: 12px">{{ userStore.userInfo?.nickname }}</span>
      <el-button type="danger" size="small" @click="logout">退出</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const logout = async () => {
  await userStore.doLogout()
  router.push('/login')
}
</script>

<style scoped>
.header {
  height: 60px;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}
.title {
  font-size: 18px;
  font-weight: bold;
}
</style>
