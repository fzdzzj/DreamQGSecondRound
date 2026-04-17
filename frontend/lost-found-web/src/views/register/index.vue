<template>
  <div class="flex-center" style="height: 100vh">
    <el-card style="width: 460px">
      <h2 style="text-align: center; margin-bottom: 20px">注册</h2>

      <el-form :model="form" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>

        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>

        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>

        <el-form-item label="确认密码">
          <el-input v-model="form.passwordConfirm" type="password" show-password />
        </el-form-item>

        <el-form-item label="邮箱验证码">
          <div class="flex" style="gap: 8px; width: 100%">
            <el-input v-model="form.code" />
            <el-button :disabled="countdown > 0 || sending" @click="sendCode">
              {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="register">
            注册
          </el-button>
        </el-form-item>

        <div class="flex-between">
          <span></span>
          <el-button link type="primary" @click="goLogin">去登录</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { registerApi } from '@/api/auth'
import { sendEmailCodeApi } from '@/api/email'
import { showSuccess, showWarning } from '@/utils/message'

const router = useRouter()
const loading = ref(false)
const sending = ref(false)
const countdown = ref(0)
let timer: number | null = null

const form = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  password: '',
  passwordConfirm: '',
  code: ''
})

const register = async () => {
  if (!form.username.trim()) {
    showWarning('请输入用户名')
    return
  }
  if (!form.nickname.trim()) {
    showWarning('请输入昵称')
    return
  }
  if (!form.email.trim()) {
    showWarning('请输入邮箱')
    return
  }
  if (!form.password) {
    showWarning('请输入密码')
    return
  }
  if (form.password !== form.passwordConfirm) {
    showWarning('两次输入的密码不一致')
    return
  }
  if (!form.code.trim()) {
    showWarning('请输入邮箱验证码')
    return
  }

  loading.value = true
  try {
    await registerApi({
      username: form.username,
      email: form.email,
      phone: form.phone,
      password: form.password,
      passwordConfirm: form.passwordConfirm,
      nickname: form.nickname,
      code: form.code
    })
    showSuccess('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}

const sendCode = async () => {
  if (!form.email.trim()) {
    showWarning('请先输入邮箱')
    return
  }

  sending.value = true
  try {
    await sendEmailCodeApi({
      email: form.email,
      type: '1'
    })
    showSuccess('验证码已发送')
    countdown.value = 60

    if (timer) {
      clearInterval(timer)
      timer = null
    }

    timer = window.setInterval(() => {
      countdown.value--
      if (countdown.value <= 0 && timer) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)
  } finally {
    sending.value = false
  }
}

const goLogin = () => {
  router.push('/login')
}
</script>
