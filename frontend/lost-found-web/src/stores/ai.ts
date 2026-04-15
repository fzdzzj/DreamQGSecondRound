import { defineStore } from 'pinia'
import type { MessageVO } from '@/types/ai'

interface AiState {
  currentChatId: string
  sessions: string[]
  messages: MessageVO[]
  streaming: boolean
}

export const useAiStore = defineStore('ai', {
  state: (): AiState => ({
    currentChatId: '',
    sessions: [],
    messages: [],
    streaming: false
  }),
  actions: {
    setCurrentChatId(chatId: string) {
      this.currentChatId = chatId
    },
    setSessions(list: string[]) {
      this.sessions = list
    },
    setMessages(list: MessageVO[]) {
      this.messages = list
    },
    appendUserMessage(content: string) {
      this.messages.push({ role: 'user', content })
    },
    appendAssistantMessage(content: string) {
      this.messages.push({ role: 'assistant', content })
    },
    replaceLastAssistantMessage(content: string) {
      const last = this.messages[this.messages.length - 1]
      if (last && last.role === 'assistant') {
        last.content = content
      } else {
        this.appendAssistantMessage(content)
      }
    },
    setStreaming(status: boolean) {
      this.streaming = status
    },
    clearMessages() {
      this.messages = []
    }
  }
})
