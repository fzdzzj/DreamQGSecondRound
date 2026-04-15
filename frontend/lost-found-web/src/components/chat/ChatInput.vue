<template>
  <div>
    <div class="flex" style="gap: 8px">
      <el-input
        v-model="text"
        type="textarea"
        :rows="3"
        resize="none"
        placeholder="请输入内容"
        @keydown.enter.prevent="handleEnter"
      />
    </div>

    <div style="margin-top: 8px" class="flex-between">
      <ImageUpload @success="handleImageSuccess" />

      <div>
        <el-button @click="emit('stop')" v-if="showStop">停止</el-button>
        <el-button type="primary" @click="sendText">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ImageUpload from '@/components/upload/ImageUpload.vue'

const props = defineProps<{
  showStop?: boolean
}>()

const emit = defineEmits<{
  (e: 'send-text', text: string): void
  (e: 'send-image', imageUrl: string): void
  (e: 'stop'): void
}>()

const text = ref('')

const sendText = () => {
  const value = text.value.trim()
  if (!value) return
  emit('send-text', value)
  text.value = ''
}

const handleImageSuccess = (url: string) => {
  emit('send-image', url)
}

const handleEnter = (event: KeyboardEvent) => {
  if (!event.shiftKey) {
    sendText()
  }
}
</script>
