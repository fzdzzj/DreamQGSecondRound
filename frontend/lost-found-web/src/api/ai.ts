import request from './request'
import type { MessageVO } from '@/types/ai'
import { AI_CHAT_TYPE } from '@/constants/app'

export function getAiChatIdsApi(type = AI_CHAT_TYPE) {
  return request.get<unknown, string[]>('/ai/history/chatIds', {
    params: { type }
  })
}

export function getAiChatHistoryApi(chatId: string, type = AI_CHAT_TYPE) {
  return request.get<unknown, MessageVO[]>(`/ai/history/${chatId}`, {
    params: { type }
  })
}

export function getAiChatStreamUrl(prompt: string, chatId: string) {
  return `http://localhost:8080/ai/chat?prompt=${encodeURIComponent(prompt)}&chatId=${encodeURIComponent(chatId)}`
}

export function regenerateItemAiApi(itemId: number) {
  return request.post(`/ai/item/${itemId}/regenerate`)
}
