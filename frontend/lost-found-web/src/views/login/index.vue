<template>
  <div class="flex-center" style="height: 100vh">
    <el-card style="width: 420px">
      <h2 style="text-align: center; margin-bottom: 20px">登录</h2>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="密码登录" name="password" />
        <el-tab-pane label="验证码登录" name="code" />
      </el-tabs>

      <el-form :model="form" label-position="top">
        <el-form-item label="账号">
          <el-input v-model="form.identifier" placeholder="用户名 / 邮箱 / 手机号" />
        </el-form-item>

        <template v-if="activeTab === 'password'">
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
        </template>

        <template v-else>
          <el-form-item label="验证码">
            <div class="flex" style="gap: 8px; width: 100%">
              <el-input v-model="form.code" placeholder="请输入邮箱验证码" />
              <el-button :disabled="countdown > 0" @click="sendCode">
                {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
              </el-button>
            </div>
          </el-form-item>
        </template>

        <el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="login">
            登录
          </el-button>
        </el-form-item>

        <div class="flex-between">
          <span></span>
          <el-button link type="primary" @click="goRegister">去注册</el-button>
          <el-button link type="primary" @click="$router.push('/profile/change-password')">忘记密码</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { loginApi } from '@/api/auth'
import { sendEmailCodeApi } from '@/api/email'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { showSuccess, showWarning } from '@/utils/message'
import { LoginType } from '@/types/auth'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const activeTab = ref<'password' | 'code'>('password')
const countdown = ref(0)
let timer: number | null = null

const form = reactive({
  identifier: '',
  password: '',
  code: ''
})

const PASSWORD_LOGIN_TYPE = 'PASSWORD'
const EMAIL_CODE_LOGIN_TYPE = 'EMAIL_CODE'

const login = async () => {
  loading.value = true
  try {
    const payload =
      activeTab.value === 'password'
        ? {
            identifier: form.identifier,
            password: form.password,
            loginType: '1' as LoginType
          }
        : {
            identifier: form.identifier,
            code: form.code,
            loginType: '2' as LoginType
          }

    const res = await loginApi(payload)
    userStore.setLogin(res)
    await userStore.fetchProfile()
    router.push('/home')
  } catch (error) {
    // 错误已经在响应拦截器中处理，这里不需要额外处理
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}


const sendCode = async () => {
  if (!form.identifier) {
    showWarning('请先输入邮箱')
    return
  }

  await sendEmailCodeApi({
    email: form.identifier,
    type: '2' // 改为数字字符串
  })

  showSuccess('验证码已发送')
  countdown.value = 60
  timer && clearInterval(timer)
  timer = window.setInterval(() => {
    countdown.value--
    if (countdown.value <= 0 && timer) {
      clearInterval(timer)
      timer = null
    }
  }, 1000)
}

const goRegister = () => {
  router.push('/register')
}
</script>