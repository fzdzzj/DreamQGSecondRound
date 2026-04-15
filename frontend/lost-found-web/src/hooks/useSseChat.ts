import { ref } from 'vue'
import { createSseStream } from '@/utils/sse'
import { useAiStore } from '@/stores/ai'
import { getAiChatStreamUrl } from '@/api/ai'

export function useSseChat() {
  const aiStore = useAiStore()
  const abortController = ref<AbortController | null>(null)

  const startChat = async (prompt: string, chatId: string) => {
    aiStore.appendUserMessage(prompt)
    aiStore.setStreaming(true)

    let assistantText = ''

    abortController.value = await createSseStream(getAiChatStreamUrl(prompt, chatId), {
      onChunk(chunk) {
        assistantText += chunk
        aiStore.replaceLastAssistantMessage(assistantText)
      },
      onDone() {
        aiStore.setStreaming(false)
      },
      onError(message) {
        aiStore.setStreaming(false)
        aiStore.replaceLastAssistantMessage(`错误：${message}`)
      }
    })
  }

  const stopChat = () => {
    abortController.value?.abort()
    aiStore.setStreaming(false)
  }

  return {
    startChat,
    stopChat
  }
}
