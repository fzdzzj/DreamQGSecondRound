<template>
  <div class="page-card">
    <h2>编辑物品</h2>

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

      <el-form-item label="状态">
        <el-input-number v-model="form.status" :min="1" :max="5" />
      </el-form-item>

      <el-form-item label="图片">
        <ImageUpload @success="handleUploadSuccess" />
        <div style="margin-top: 12px; display: flex; gap: 8px; flex-wrap: wrap">
          <el-image v-for="url in form.imageUrls" :key="url" :src="url" style="width: 80px; height: 80px" />
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
import { useRoute, useRouter } from 'vue-router'
import { getItemDetailApi, updateItemApi } from '@/api/item'
import ImageUpload from '@/components/upload/ImageUpload.vue'
import { showSuccess } from '@/utils/message'

const route = useRoute()
const router = useRouter()
const itemId = Number(route.params.id)

const form = reactive<any>({
  id: undefined,
  title: '',
  description: '',
  location: '',
  happenTime: '',
  contactMethod: '',
  status: 1,
  imageUrls: []
})

onMounted(async () => {
  const detail = await getItemDetailApi(itemId)
  Object.assign(form, detail)
})

const handleUploadSuccess = (url: string) => {
  form.imageUrls.push(url)
}

const save = async () => {
  await updateItemApi(itemId, form)
  showSuccess('保存成功')
  router.push('/item/my')
}
</script>
