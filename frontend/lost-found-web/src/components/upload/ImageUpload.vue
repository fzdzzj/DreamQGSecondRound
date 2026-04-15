<template>
  <el-upload
    :show-file-list="false"
    :http-request="customUpload"
    accept="image/*"
  >
    <el-button>发送图片</el-button>
  </el-upload>
</template>

<script setup lang="ts">
import { uploadFileApi } from '@/api/upload'
import { showError, showSuccess } from '@/utils/message'

const emit = defineEmits<{
  (e: 'success', url: string): void
}>()

const customUpload = async (options: any) => {
  try {
    const file = options.file as File
    const url = await uploadFileApi(file)
    emit('success', url)
    showSuccess('上传成功')
  } catch {
    showError('上传失败')
  }
}
</script>
