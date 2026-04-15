import { defineStore } from 'pinia'
import type { ConversationVO, PrivateMessageVO } from '@/types/message'

interface MessageState {
  conversations: ConversationVO[]
  currentPeerId: number | null
  currentMessages: PrivateMessageVO[]
  totalUnreadCount: number
}

export const useMessageStore = defineStore('message', {
  state: (): MessageState => ({
    conversations: [],
    currentPeerId: null,
    currentMessages: [],
    totalUnreadCount: 0
  }),
  actions: {
    setConversations(list: ConversationVO[]) {
      this.conversations = list
    },
    setCurrentPeerId(peerId: number | null) {
      this.currentPeerId = peerId
    },
    setCurrentMessages(list: PrivateMessageVO[]) {
      this.currentMessages = list
    },
    appendCurrentMessage(message: PrivateMessageVO) {
      this.currentMessages.push(message)
    },
    setTotalUnreadCount(count: number) {
      this.totalUnreadCount = count
    },
    updateConversationUnread(peerId: number, conversationUnreadCount: number, totalUnreadCount: number) {
      this.totalUnreadCount = totalUnreadCount
      const target = this.conversations.find((item) => item.peerId === peerId)
      if (target) {
        target.unreadCount = conversationUnreadCount
      }
    }
  }
})
