<template>
  <div class="page-card">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px">
      <h2>发布丢失物品</h2>
      <el-button @click="router.back()">返回</el-button>
    </div>

    <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" />
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="4" />
      </el-form-item>

      <el-form-item label="地点" prop="location">
        <el-input v-model="form.location" />
      </el-form-item>

      <el-form-item label="时间" prop="happenTime">
        <el-date-picker v-model="form.happenTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
      </el-form-item>

      <el-form-item label="联系方式" prop="contactMethod">
        <el-input v-model="form.contactMethod" />
      </el-form-item>

      <el-form-item label="图片">
        <ImageUpload @success="handleUploadSuccess" />
        <div style="margin-top: 12px; display: flex; gap: 8px; flex-wrap: wrap">
          <el-image v-for="url in form.imageUrls" :key="url" :src="url" style="width: 80px; height: 80px" />
        </div>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submit">提交</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { publishLostItemApi } from '@/api/item'
import ImageUpload from '@/components/upload/ImageUpload.vue'
import { showSuccess, showError } from '@/utils/message'
import dayjs from 'dayjs'

const router = useRouter()
const formRef = ref()

const form = reactive({
  title: '',
  description: '',
  location: '',
  happenTime: '',
  contactMethod: '',
  imageUrls: [] as string[]
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }],
  location: [{ required: true, message: '请输入地点', trigger: 'blur' }],
  happenTime: [{ required: true, message: '请选择时间', trigger: 'change' }],
  contactMethod: [{ required: true, message: '请输入联系方式', trigger: 'blur' }]
}

const handleUploadSuccess = (url: string) => {
  form.imageUrls.push(url)
}

const submit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // 确保happenTime不为空
    if (!form.happenTime) {
      showError('请选择时间')
      return
    }
    
    // 转换时间格式（如果需要）
    const submitData = {
      ...form,
      // 可以根据后端需要调整时间格式
      happenTime: form.happenTime
    }
    
    await publishLostItemApi(submitData)
    showSuccess('发布成功')
    router.push('/item/my')
  } catch (error) {
    console.error('提交失败:', error)
  }
}
</script>