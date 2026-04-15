import { onBeforeUnmount, ref } from 'vue'
import { WsClient } from '@/utils/websocket'
import { useMessageStore } from '@/stores/message'
import type { PrivateMessageVO, WsEnvelope, WsPrivateMessagePayload, WsUnreadChangedPayload } from '@/types/message'

export function usePrivateChat() {
  const messageStore = useMessageStore()
  const connected = ref(false)

  const wsClient = new WsClient({
    onOpen() {
      connected.value = true
    },
    onClose() {
      connected.value = false
    },
    onMessage(message: WsEnvelope) {
      if (message.type === 'PRIVATE_MESSAGE') {
        const payload = message.data as WsPrivateMessagePayload
        const msg: PrivateMessageVO = {
          id: payload.id,
          senderId: payload.senderId,
          receiverId: payload.receiverId,
          content: payload.content,
          messageType: payload.messageType,
          imageUrl: payload.imageUrl,
          status: payload.status,
          createTime: payload.createTime
        }

        if (
          messageStore.currentPeerId &&
          (msg.senderId === messageStore.currentPeerId || msg.receiverId === messageStore.currentPeerId)
        ) {
          messageStore.appendCurrentMessage(msg)
        }
      }

      if (message.type === 'UNREAD_CHANGED') {
        const payload = message.data as WsUnreadChangedPayload
        messageStore.updateConversationUnread(
          payload.peerId,
          payload.conversationUnreadCount,
          payload.totalUnreadCount
        )
      }
    }
  })

  const connect = () => wsClient.connect()
  const disconnect = () => wsClient.disconnect()

  onBeforeUnmount(() => disconnect())

  return {
    connected,
    connect,
    disconnect
  }
}
