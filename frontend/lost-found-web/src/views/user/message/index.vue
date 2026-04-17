<template>
  <div class="chat-page">
    <div class="chat-session-panel">
      <div style="padding:12px;border-bottom:1px solid #ebeef5">
        <el-input v-model="manualPeerId" placeholder="输入对方用户ID" />
        <el-button
          type="primary"
          style="width:100%;margin-top:8px"
          @click="startConversation"
        >
          开始聊天
        </el-button>
      </div>

      <PrivateSessionList
        :list="messageStore.conversations"
        @select="selectConversation"
      />
    </div>

    <div class="chat-content-panel">
      <div class="flex-between header-bar">
        <span v-if="messageStore.currentPeerId">
          当前会话：{{ messageStore.currentPeerId }}
        </span>
        <span v-else>请选择会话</span>

        <div v-if="messageStore.currentPeerId">
          <el-button
            type="warning"
            plain
            @click="clearCurrentConversation"
          >
            清空会话
          </el-button>

          <el-button
            type="danger"
            plain
            @click="removeConversation"
          >
            删除会话
          </el-button>
        </div>
      </div>

      <div class="chat-messages" ref="msgRef">
        <ChatBubble
          v-for="msg in messageStore.currentMessages"
          :key="msg.id"
          :content="msg.content"
          :imageUrl="msg.imageUrl"
          :messageType="msg.messageType"
          :role="msg.senderId === userId ? 'user' : 'assistant'"
          :showDelete="msg.senderId === userId"
          @delete="removeMessage(msg.id)"
        />
      </div>

      <div class="chat-input-panel">
        <ChatInput
          @send-text="sendText"
          @send-image="sendImage"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { useMessageStore } from '@/stores/message'
import { usePrivateChat } from '@/hooks/usePrivateChat'
import {
  clearConversationApi,
  deleteConversationApi,
  deletePrivateMessageApi,
  getConversationListApi,
  getPrivateMessageHistoryApi,
  markConversationReadApi,
  sendPrivateMessageApi
} from '@/api/message'
import { useUserStore } from '@/stores/user'
import { scrollToBottom } from '@/utils/scroll'
import { showSuccess, showWarning } from '@/utils/message'

import ChatBubble from '@/components/chat/ChatBubble.vue'
import ChatInput from '@/components/chat/ChatInput.vue'
import PrivateSessionList from '@/components/chat/PrivateSessionList.vue'

const messageStore = useMessageStore()
const userStore = useUserStore()
const { connect } = usePrivateChat()

const msgRef = ref<HTMLElement>()
const manualPeerId = ref('')
const userId = userStore.userInfo?.id

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
  messageStore.setCurrentMessages([...list].reverse())
  await markConversationReadApi(item.peerId)
  await nextTick()
  scrollToBottom(msgRef.value)
}

const startConversation = async () => {
  const peerId = Number(manualPeerId.value)
  if (!peerId) return showWarning('请输入用户ID')

  messageStore.setCurrentPeerId(peerId)
  messageStore.setCurrentMessages([])
}

const sendText = async (text: string) => {
  if (!messageStore.currentPeerId) {
    return showWarning('请先选择会话')
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
    status: 1
  })

  await nextTick()
  scrollToBottom(msgRef.value)
  await loadConversations()
}

const sendImage = async (url: string) => {
  if (!messageStore.currentPeerId) {
    return showWarning('请先选择会话')
  }

  await sendPrivateMessageApi({
    receiverId: messageStore.currentPeerId,
    messageType: 2,
    imageUrl: url,
    clientMsgId: crypto.randomUUID()
  })

  await loadConversations()
}

const removeMessage = async (id: number) => {
  await ElMessageBox.confirm('确认删除这条消息？', '提示')

  await deletePrivateMessageApi(id)

  messageStore.setCurrentMessages(
    messageStore.currentMessages.filter(item => item.id !== id)
  )

  showSuccess('删除成功')
}

const removeConversation = async () => {
  if (!messageStore.currentPeerId) return

  await ElMessageBox.confirm('确认删除整个会话？', '提示')

  await deleteConversationApi(messageStore.currentPeerId)

  messageStore.setCurrentPeerId(null)
  messageStore.setCurrentMessages([])

  await loadConversations()

  showSuccess('会话已删除')
}

const clearCurrentConversation = async () => {
  if (!messageStore.currentPeerId) return

  await ElMessageBox.confirm('确认清空当前会话？', '提示')

  await clearConversationApi(messageStore.currentPeerId)

  messageStore.setCurrentMessages([])

  showSuccess('已清空')
}
</script>
