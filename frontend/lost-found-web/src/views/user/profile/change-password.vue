<template>
  <div class="page-card">
    <h2>修改密码</h2>

    <el-form :model="form" label-width="120px" style="max-width: 480px">
      <el-form-item label="邮箱">
        <el-input v-model="form.email" placeholder="请输入注册邮箱" />
      </el-form-item>

      <el-form-item label="验证码">
        <div style="display: flex; gap: 8px; width: 100%">
          <el-input v-model="form.code" placeholder="请输入验证码" />
          <el-button :disabled="countdown > 0" @click="sendCode">
            {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
          </el-button>
        </div>
      </el-form-item>

      <el-form-item label="新密码">
        <el-input v-model="form.newPassword" type="password" show-password />
      </el-form-item>

      <el-form-item label="确认密码">
        <el-input v-model="form.confirmPassword" type="password" show-password />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="loading" @click="submit">
          修改密码
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { changePasswordApi } from '@/api/auth'
import { sendEmailCodeApi } from '@/api/email'
import { showSuccess, showWarning } from '@/utils/message'
import { changePasswordByCodeApi } from '@/api/user'
const loading = ref(false)
const countdown = ref(0)
let timer: number | null = null

const form = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const sendCode = async () => {
  if (!form.email) {
    showWarning('请输入邮箱')
    return
  }

  await sendEmailCodeApi({
    email: form.email,
    type: '3'
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

const submit = async () => {
  if (!form.email) return showWarning('请输入邮箱')
  if (!form.code) return showWarning('请输入验证码')
  if (!form.newPassword) return showWarning('请输入新密码')
  if (form.newPassword !== form.confirmPassword) {
    return showWarning('两次密码不一致')
  }

  loading.value = true
  try {
    await changePasswordByCodeApi(form)
    showSuccess('修改成功，请重新登录')
  } finally {
    loading.value = false
  }
}
</script>
