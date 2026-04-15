<template>
  <div class="chat-page">
    <div class="chat-session-panel">
      <div style="padding: 12px; border-bottom: 1px solid #ebeef5">
        <el-button type="primary" style="width: 100%" @click="newSession">新建会话</el-button>
      </div>
      <AiSessionList :list="sessions" @select="selectSession" />
    </div>

    <div class="chat-content-panel">
      <div class="chat-messages" ref="msgRef">
        <ChatBubble
          v-for="(msg, index) in aiStore.messages"
          :key="index"
          :content="msg.content"
          :role="msg.role"
          :messageType="1"
        />
      </div>

      <div class="chat-input-panel">
        <ChatInput
          :showStop="aiStore.streaming"
          @send-text="sendText"
          @stop="stop"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { useAiStore } from '@/stores/ai'
import { useSseChat } from '@/hooks/useSseChat'
import { getAiChatHistoryApi, getAiChatIdsApi } from '@/api/ai'
import { scrollToBottom } from '@/utils/scroll'
import ChatBubble from '@/components/chat/ChatBubble.vue'
import ChatInput from '@/components/chat/ChatInput.vue'
import AiSessionList from '@/components/chat/AiSessionList.vue'

const aiStore = useAiStore()
const { startChat, stopChat } = useSseChat()
const sessions = ref<string[]>([])
const msgRef = ref<HTMLElement>()

onMounted(async () => {
  await loadSessions()
  if (sessions.value.length) {
    await selectSession(sessions.value[0])
  }
})

const loadSessions = async () => {
  sessions.value = await getAiChatIdsApi()
}

const newSession = () => {
  const chatId = crypto.randomUUID()
  aiStore.setCurrentChatId(chatId)
  aiStore.clearMessages()
  if (!sessions.value.includes(chatId)) {
    sessions.value.unshift(chatId)
  }
}

const selectSession = async (chatId: string) => {
  aiStore.setCurrentChatId(chatId)
  const history = await getAiChatHistoryApi(chatId)
  aiStore.setMessages(history)
  await nextTick()
  scrollToBottom(msgRef.value)
}

const sendText = async (text: string) => {
  if (!aiStore.currentChatId) {
    newSession()
  }

  await startChat(text, aiStore.currentChatId)

  await nextTick()
  scrollToBottom(msgRef.value)

  if (!sessions.value.includes(aiStore.currentChatId)) {
    sessions.value.unshift(aiStore.currentChatId)
  }
}

const stop = () => {
  stopChat()
}
</script>
