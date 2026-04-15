<template>
  <div class="flex-center" style="height: 100vh">
    <el-card style="width: 380px">
      <h2 style="text-align: center; margin-bottom: 20px">登录</h2>

      <el-form :model="form" label-position="top">
        <el-form-item label="账号">
          <el-input v-model="form.identifier" placeholder="用户名 / 邮箱 / 手机号" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="login">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { loginApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const form = reactive({
  identifier: '',
  password: '',
  loginType: 'PASSWORD' as const
})

const login = async () => {
  loading.value = true
  try {
    const res = await loginApi(form)
    userStore.setLogin(res)
    await userStore.fetchProfile()
    router.push('/home')
  } finally {
    loading.value = false
  }
}
</script>
