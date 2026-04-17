<template>
  <div :class="['bubble-wrap', roleClass]">
    <div class="bubble-box">
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

      <div class="actions" v-if="showDelete">
        <el-button link type="danger" size="small" @click="$emit('delete')">
          删除
        </el-button>
      </div>
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
  showDelete?: boolean
}>()

defineEmits<{
  (e: 'delete'): void
}>()

const roleClass = computed(() => (props.role === 'user' ? 'user' : 'assistant'))
</script>

<style scoped>
.bubble-wrap {
  display: flex;
  margin: 10px 0;
}

.bubble-wrap.user {
  justify-content: flex-end;
}

.bubble-wrap.assistant {
  justify-content: flex-start;
}

.bubble-box {
  max-width: 70%;
}

.bubble {
  padding: 10px 12px;
  border-radius: 10px;
  background: #fff;
  word-break: break-word;
}

.user .bubble {
  background: #95ec69;
}

.actions {
  margin-top: 4px;
  font-size: 12px;
}
</style>
