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
        <el-select v-model="form.status">
          <el-option label="开放中" value="1" />
          <el-option label="已匹配" value="2" />
          <el-option label="已关闭" value="3" />
          <el-option label="已举报" value="4" />
          <el-option label="已删除" value="5" />
        </el-select>
      </el-form-item>

      <el-form-item label="图片">
        <ImageUpload @success="handleUploadSuccess" />
        <div style="margin-top: 12px; display: flex; gap: 8px; flex-wrap: wrap">
          <div v-for="(url, index) in form.imageUrls" :key="url" style="position: relative">
            <el-image :src="url" style="width: 80px; height: 80px" />
            <el-button 
              type="danger" 
              size="small" 
              circle 
              style="position: absolute; top: -8px; right: -8px" 
              @click="handleDeleteImage(index)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </el-form-item>

      <el-form-item>
        <el-button @click="router.back()">返回</el-button>
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
import { itemStatusText } from '@/utils/item'
import { Delete } from '@element-plus/icons-vue'

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

const handleDeleteImage = (index: number) => {
  form.imageUrls.splice(index, 1)
}

const save = async () => {
  await updateItemApi(itemId, form)
  showSuccess('保存成功')
  router.push('/item/my')
}
</script>