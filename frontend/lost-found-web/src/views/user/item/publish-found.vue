<template>
  <div class="page-card">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px">
      <h2>发布拾取物品</h2>
      <el-button @click="router.back()">返回</el-button>
    </div>

    <el-form :model="form" label-width="100px">
      <el-form-item label="标题">
        <el-input v-model="form.title" />
      </el-form-item>

      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="4" />
      </el-form-item>

      <el-form-item label="地点">
        <el-input v-model="form.location" />
      </el-form-item>

      <el-form-item label="时间">
        <el-date-picker v-model="form.happenTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
      </el-form-item>

      <el-form-item label="联系方式">
        <el-input v-model="form.contactMethod" />
      </el-form-item>

      <el-form-item label="图片">
        <ImageUpload @success="handleUploadSuccess" />
        <div style="margin-top: 12px; display: flex; gap: 8px; flex-wrap: wrap">
          <el-image v-for="url in form.imageUrls" :key="url" :src="url" style="width: 80px; height: 80px" />
        </div>
      </el-form-item>

      <el-form-item>
        <el-button type="success" @click="submit">提交</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { publishFoundItemApi } from '@/api/item'
import ImageUpload from '@/components/upload/ImageUpload.vue'
import { showSuccess } from '@/utils/message'

const router = useRouter()

const form = reactive({
  title: '',
  description: '',
  location: '',
  happenTime: '',
  contactMethod: '',
  imageUrls: [] as string[]
})

const handleUploadSuccess = (url: string) => {
  form.imageUrls.push(url)
}

const submit = async () => {
  await publishFoundItemApi(form)
  showSuccess('发布成功')
  router.push('/item/my')
}
</script>