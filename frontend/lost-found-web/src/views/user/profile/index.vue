<template>
  <div class="page-card">
    <h2>个人中心</h2>

    <el-form :model="form" label-width="100px">
      <el-form-item label="昵称">
        <el-input v-model="form.nickname" />
      </el-form-item>

      <el-form-item label="邮箱">
        <el-input v-model="form.email" />
      </el-form-item>

      <el-form-item label="手机号">
        <el-input v-model="form.phone" />
      </el-form-item>

      <el-form-item label="头像">
        <ImageUpload @success="handleAvatarSuccess" />
        <div style="margin-top: 12px" v-if="form.avatar">
          <el-image :src="form.avatar" style="width: 100px; height: 100px" />
        </div>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="save">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { getPersonalInfoApi, updatePersonalInfoApi } from '@/api/user'
import ImageUpload from '@/components/upload/ImageUpload.vue'
import { showSuccess } from '@/utils/message'

const form = reactive<any>({
  nickname: '',
  email: '',
  phone: '',
  avatar: ''
})

onMounted(async () => {
  const data = await getPersonalInfoApi()
  Object.assign(form, data)
})

const handleAvatarSuccess = (url: string) => {
  form.avatar = url
}

const save = async () => {
  await updatePersonalInfoApi(form)
  showSuccess('保存成功')
}
</script>
