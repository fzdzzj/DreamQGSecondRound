import request from './request'
import type { ConversationVO, PrivateMessageSendDTO, PrivateMessageVO } from '@/types/message'

export function sendPrivateMessageApi(data: PrivateMessageSendDTO) {
  return request.post('/message', data)
}

export function getPrivateMessageHistoryApi(peerId: number, lastMessageId?: number, pageSize = 20) {
  return request.get<unknown, PrivateMessageVO[]>(`/message/history/cursor/${peerId}`, {
    params: { lastMessageId, pageSize }
  })
}

export function markConversationReadApi(peerId: number) {
  return request.put(`/message/${peerId}/read`)
}

export function getConversationListApi() {
  return request.get<unknown, ConversationVO[]>('/message/conversations')
}

export function deletePrivateMessageApi(messageId: number) {
  return request.delete(`/message/${messageId}`)
}

export function deleteConversationApi(peerId: number) {
  return request.delete(`/message/conversation/${peerId}`)
}

export function clearConversationApi(peerId: number) {
  return request.put(`/message/conversation/${peerId}/clear`)
}

export function getPrivateUnreadCountApi() {
  return request.get<unknown, number>('/message/unread/count')
}
