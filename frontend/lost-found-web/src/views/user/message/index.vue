<template>
  <div class="chat-page">
    <div class="chat-session-panel">
      <PrivateSessionList :list="messageStore.conversations" @select="selectConversation" />
    </div>

    <div class="chat-content-panel">
      <div class="chat-messages" ref="msgRef">
        <ChatBubble
          v-for="msg in messageStore.currentMessages"
          :key="msg.id"
          :content="msg.content"
          :imageUrl="msg.imageUrl"
          :messageType="msg.messageType"
          :role="msg.senderId === userId ? 'user' : 'assistant'"
        />
      </div>

      <div class="chat-input-panel">
        <ChatInput @send-text="sendText" @send-image="sendImage" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { useMessageStore } from '@/stores/message'
import { usePrivateChat } from '@/hooks/usePrivateChat'
import {
  getConversationListApi,
  getPrivateMessageHistoryApi,
  markConversationReadApi,
  sendPrivateMessageApi
} from '@/api/message'
import { useUserStore } from '@/stores/user'
import { scrollToBottom } from '@/utils/scroll'
import { showError } from '@/utils/message'

import ChatBubble from '@/components/chat/ChatBubble.vue'
import ChatInput from '@/components/chat/ChatInput.vue'
import PrivateSessionList from '@/components/chat/PrivateSessionList.vue'

const messageStore = useMessageStore()
const userStore = useUserStore()
const { connect } = usePrivateChat()
const userId = userStore.userInfo?.id
const msgRef = ref<HTMLElement>()

onMounted(async () => {
  connect()
  await loadConversations()
})

const loadConversations = async () => {
  const list = await getConversationListApi()
  messageStore.setConversations(list)
}

const selectConversation = async (item: any) => {
  messageStore.setCurrentPeerId(item.peerId)
  const list = await getPrivateMessageHistoryApi(item.peerId)
  messageStore.setCurrentMessages(list)
  await markConversationReadApi(item.peerId)
  await loadConversations()
  await nextTick()
  scrollToBottom(msgRef.value)
}

const sendText = async (text: string) => {
  if (!messageStore.currentPeerId) {
    showError('请先选择会话')
    return
  }

  await sendPrivateMessageApi({
    receiverId: messageStore.currentPeerId,
    messageType: 1,
    content: text,
    clientMsgId: crypto.randomUUID()
  })

  messageStore.appendCurrentMessage({
    id: Date.now(),
    senderId: userId!,
    receiverId: messageStore.currentPeerId,
    content: text,
    messageType: 1,
    status: 1,
    createTime: new Date().toISOString()
  })

  await nextTick()
  scrollToBottom(msgRef.value)
  await loadConversations()
}

const sendImage = async (imageUrl: string) => {
  if (!messageStore.currentPeerId) {
    showError('请先选择会话')
    return
  }

  await sendPrivateMessageApi({
    receiverId: messageStore.currentPeerId,
    messageType: 2,
    imageUrl,
    clientMsgId: crypto.randomUUID()
  })

  messageStore.appendCurrentMessage({
    id: Date.now(),
    senderId: userId!,
    receiverId: messageStore.currentPeerId,
    imageUrl,
    messageType: 2,
    status: 1,
    createTime: new Date().toISOString()
  })

  await nextTick()
  scrollToBottom(msgRef.value)
  await loadConversations()
}
</script>
