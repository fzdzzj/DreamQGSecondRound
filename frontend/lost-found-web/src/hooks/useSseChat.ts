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

    let assistantText = '' // 用来累积 AI 回复的文本

    // 创建 SSE 流并处理数据
    abortController.value = await createSseStream(getAiChatStreamUrl(prompt, chatId), {
      onChunk(chunk) {
        assistantText += chunk  // 将每个块累加到 assistantText 中
        aiStore.replaceLastAssistantMessage(assistantText)  // 每次接收到新数据块后更新消息
      },
      onDone() {
        aiStore.setStreaming(false)  // 完成后停止流式标识
      },
      onError(message) {
        aiStore.setStreaming(false)  // 错误后停止流式标识
        aiStore.replaceLastAssistantMessage(`错误：${message}`)  // 显示错误信息
      }
    })
  }

  const stopChat = () => {
    abortController.value?.abort()  // 停止 SSE 流
    aiStore.setStreaming(false)  // 停止流式标识
  }

  return {
    startChat,
    stopChat
  }
}
