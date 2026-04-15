<template>
  <div>
    <div
      v-for="item in list"
      :key="item.peerId"
      @click="$emit('select', item)"
      class="item"
    >
      <div class="flex-between">
        <span>{{ item.peerNickname }}</span>
        <el-badge :value="item.unreadCount" />
      </div>

      <div style="font-size: 12px; color: #909399; margin-top: 4px">
        <template v-if="item.lastMessageType === 2">[图片]</template>
        <template v-else>{{ item.lastMessage || '-' }}</template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ConversationVO } from '@/types/message'

defineProps<{
  list: ConversationVO[]
}>()
</script>

<style scoped>
.item {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}
.item:hover {
  background: #f7f7f7;
}
</style>
