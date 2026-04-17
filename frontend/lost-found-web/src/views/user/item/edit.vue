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
        <el-date-picker v-model="form.happenTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
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

             <el-button
          type="primary"
          link
          :loading="generating"
          @click="handleRegenerateDescription"
          class="regenerate-btn"
        >
          <el-icon><MagicStick /></el-icon>
          AI重新生成
        </el-button>
        <el-button @click="router.back()">返回</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getItemDetailApi, updateItemApi } from '@/api/item'
import ImageUpload from '@/components/upload/ImageUpload.vue'
import { showSuccess, showError, showWarning } from '@/utils/message'
import { itemStatusText } from '@/utils/item'
import { Delete } from '@element-plus/icons-vue'
import { regenerateItemAiApi } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { Ref } from 'vue'
const route = useRoute()
const router = useRouter()
const itemId = Number(route.params.id)

const form = reactive<any>({
  id: undefined,
  title: '',
  description: '',
  location: '',
  happenTime: null,
  contactMethod: '',
  status: '1',
  imageUrls: []
})

onMounted(async () => {
  const detail = await getItemDetailApi(itemId)
  // 确保status字段是字符串类型
  if (detail.status !== undefined) {
    detail.status = String(detail.status)
  }
  Object.assign(form, detail)
})

const handleUploadSuccess = (url: string) => {
  form.imageUrls.push(url)
}

const handleDeleteImage = (index: number) => {
  form.imageUrls.splice(index, 1)
}
const generating = ref(false)
const handleRegenerateDescription = async () => {
  if (!form.id) {
    ElMessage.warning('请先保存物品以生成ID')
    return
  }

  generating.value = true
  try {
    // 调用API获取新生成的描述
    const newDescription = await regenerateItemAiApi(form.id)
    
    // 更新表单中的描述字段
    form.description = newDescription
    
    ElMessage.success('AI描述生成成功')
  } catch (error) {
    console.error(error)
    ElMessage.error('AI描述生成失败，请稍后重试')
  } finally {
    generating.value = false
  }
}
const save = async () => {
  // 表单验证
  if (!form.title) {
    return showWarning('请输入标题')
  }
  if (!form.description) {
    return showWarning('请输入描述')
  }
  if (!form.location) {
    return showWarning('请输入地点')
  }
  if (!form.happenTime) {
    return showWarning('请选择时间')
  }
  if (!form.contactMethod) {
    return showWarning('请输入联系方式')
  }
  if (!form.status) {
    return showWarning('请选择状态')
  }

  try {
    // 确保数据类型正确，只发送后端需要的字段
    const submitData = {
      title: form.title,
      description: form.description,
      location: form.location,
      happenTime: form.happenTime,
      contactMethod: form.contactMethod,
      status: form.status,
      imageUrls: form.imageUrls || []
    }
    
    console.log('提交数据类型:', {
      title: typeof submitData.title,
      description: typeof submitData.description,
      location: typeof submitData.location,
      happenTime: typeof submitData.happenTime,
      contactMethod: typeof submitData.contactMethod,
      status: typeof submitData.status,
      imageUrls: Array.isArray(submitData.imageUrls)
    })
    console.log('提交数据:', submitData)
    
    await updateItemApi(itemId, submitData)
    showSuccess('保存成功')
    router.push('/item/my')
  } catch (error: any) {
    console.error('保存失败:', error)
    console.error('错误响应:', error.response)
    showError('保存失败，请检查输入格式')
  }
}
</script>