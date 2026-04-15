<template>
  <div :class="['bubble-wrap', roleClass]">
    <div class="bubble">
      <template v-if="messageType === 2 && imageUrl">
        <el-image
          :src="imageUrl"
          fit="cover"
          style="max-width: 220px; max-height: 220px; border-radius: 8px"
          :preview-src-list="[imageUrl]"
        />
      </template>

      <template v-else>
        <span>{{ content }}</span>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  content?: string
  role: string
  messageType?: number
  imageUrl?: string
}>()

const roleClass = computed(() => (props.role === 'user' ? 'user' : 'assistant'))
</script>

<style scoped>
.bubble-wrap {
  display: flex;
  margin: 8px 0;
}
.bubble-wrap.user {
  justify-content: flex-end;
}
.bubble-wrap.assistant {
  justify-content: flex-start;
}
.bubble {
  max-width: 70%;
  padding: 10px 12px;
  border-radius: 10px;
  word-break: break-word;
  background: #fff;
}
.bubble-wrap.user .bubble {
  background: #95ec69;
}
</style>
