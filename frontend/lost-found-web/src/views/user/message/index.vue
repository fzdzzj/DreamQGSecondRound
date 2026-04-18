<template>
  <div class="chat-page">
    <div class="chat-session-panel">
      <div style="padding: 12px; border-bottom: 1px solid #ebeef5">
        <el-input
          v-model="manualPeerId"
          placeholder="輸入對方用戶ID"
          clearable
        />
        <el-button
          type="primary"
          style="width: 100%; margin-top: 8px"
          @click="startConversation"
        >
          開始聊天
        </el-button>
      </div>

      <PrivateSessionList :list="messageStore.conversations" @select="selectConversation" />
    </div>

    <div class="chat-content-panel">
      <div style="padding: 12px; border-bottom: 1px solid #ebeef5">
        <span v-if="messageStore.currentPeerId">
          當前會話用戶ID：{{ messageStore.currentPeerId }}
        </span>
        <span v-else>
          請先從左側選擇會話，或輸入用戶ID開始聊天
        </span>
        <div v-if="messageStore.currentPeerId">
          <el-button type="warning" plain @click="clearCurrentConversation">
            清空會話
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

      <div class="chat-messages" ref="msgRef" @scroll="handleScroll">
        <div v-if="messageStore.loadingHistory" class="flex-center" style="padding: 8px; color: #909399">
          正在加載更早消息...
        </div>

        <div
          v-else-if="!messageStore.hasMoreHistory && messageStore.currentMessages.length > 0"
          class="flex-center"
          style="padding: 8px; color: #c0c4cc"
        >
          沒有更多消息了
        </div>

        <ChatBubble
          v-for="msg in messageStore.currentMessages"
          :key="msg.id"
          :content="msg.content"
          :imageUrl="msg.imageUrl"
          :messageType="msg.messageType"
          :role="msg.senderId === userId ? 'user' : 'assistant'"
          :showDelete="true"
           @delete="removeMessage(msg.id)"
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
  sendPrivateMessageApi,
  deletePrivateMessageApi,
  deleteConversationApi
} from '@/api/message'
import { useUserStore } from '@/stores/user'
import { scrollToBottom } from '@/utils/scroll'
import { showError, showSuccess, showWarning } from '@/utils/message'
import { clearConversationApi } from '@/api/message'
import { ElMessageBox } from 'element-plus'
import ChatBubble from '@/components/chat/ChatBubble.vue'
import ChatInput from '@/components/chat/ChatInput.vue'
import PrivateSessionList from '@/components/chat/PrivateSessionList.vue'
import { uploadFileApi } from '@/api/upload'
import { useLoading } from '@/hooks/useLoading'

const PAGE_SIZE = 20

const messageStore = useMessageStore()
const userStore = useUserStore()
const { connect } = usePrivateChat()

const userId = userStore.userInfo?.id
const msgRef = ref<HTMLElement>()
const manualPeerId = ref('')

onMounted(async () => {
  connect()
  await loadConversations()
})

const normalizeHistoryOrder = (list: any[]) => {
  // 後端 SQL 現在大概率是 ORDER BY id DESC
  // 聊天窗口要正序顯示，因此前端反轉
  return [...list].reverse()
}

const loadConversations = async () => {
  const list = await getConversationListApi()
  messageStore.setConversations(list)
}

const loadLatestMessages = async (peerId: number) => {
  messageStore.resetHistoryState()

  const list = await getPrivateMessageHistoryApi(peerId, undefined, PAGE_SIZE)
  const normalized = normalizeHistoryOrder(list)
  messageStore.setCurrentMessages(normalized)
  messageStore.setHasMoreHistory(list.length >= PAGE_SIZE)
}
const clearCurrentConversation = async () => {
  if (!messageStore.currentPeerId) {
    showWarning('當前沒有可清空的會話')
    return
  }

  await ElMessageBox.confirm('確定清空當前會話嗎？清空後僅隱藏歷史消息，不影響對方消息。', '提示', {
    type: 'warning',
    confirmButtonText: '確定',
    cancelButtonText: '取消'
  })

  await clearConversationApi(messageStore.currentPeerId)
  messageStore.setCurrentMessages([])
  messageStore.resetHistoryState()
  messageStore.setHasMoreHistory(false)

  showSuccess('已清空當前會話')
  await loadConversations()
}
const loadOlderMessages = async () => {
  if (!messageStore.currentPeerId) return
  if (messageStore.loadingHistory) return
  if (!messageStore.hasMoreHistory) return
  if (!messageStore.currentMessages.length) return

  const firstMessage = messageStore.currentMessages[0]
  if (!firstMessage?.id) return

  const container = msgRef.value
  const oldScrollHeight = container?.scrollHeight || 0

  messageStore.setLoadingHistory(true)
  try {
    const olderList = await getPrivateMessageHistoryApi(
      messageStore.currentPeerId,
      firstMessage.id,
      PAGE_SIZE
    )

    if (!olderList.length) {
      messageStore.setHasMoreHistory(false)
      return
    }

    const normalized = normalizeHistoryOrder(olderList)
    messageStore.prependCurrentMessages(normalized)

    if (olderList.length < PAGE_SIZE) {
      messageStore.setHasMoreHistory(false)
    }

    await nextTick()

    if (container) {
      const newScrollHeight = container.scrollHeight
      container.scrollTop = newScrollHeight - oldScrollHeight
    }
  } finally {
    messageStore.setLoadingHistory(false)
  }
}

const selectConversation = async (item: any) => {
  messageStore.setCurrentPeerId(item.peerId)
  await loadLatestMessages(item.peerId)
  await markConversationReadApi(item.peerId)
  await loadConversations()
  await nextTick()
  scrollToBottom(msgRef.value)
}

const startConversation = async () => {
  const peerId = Number(manualPeerId.value)
  if (!peerId) {
    showWarning('請輸入有效的用戶ID')
    return
  }
  if (peerId === userId) {
    showWarning('不能和自己聊天')
    return
  }

  messageStore.setCurrentPeerId(peerId)
  messageStore.setCurrentMessages([])
  messageStore.resetHistoryState()

  try {
    await loadLatestMessages(peerId)
  } catch {
    messageStore.setCurrentMessages([])
  }

  showSuccess('已切換到該會話')
  await nextTick()
  scrollToBottom(msgRef.value)
}

const sendText = async (text: string) => {
  if (!messageStore.currentPeerId) {
    showError('請先選擇會話或輸入用戶ID開始聊天')
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
const sendImage = async (imageUrl: string) => {
  if (!messageStore.currentPeerId) {
    showError('請先選擇會話或輸入用戶ID開始聊天')
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

const handleScroll = async (event: Event) => {
  const target = event.target as HTMLElement
  if (target.scrollTop <= 20) {
    await loadOlderMessages()
  }
}
</script>