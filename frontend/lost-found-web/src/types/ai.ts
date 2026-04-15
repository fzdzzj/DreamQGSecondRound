export interface MessageVO {
  role: 'user' | 'assistant'
  content: string
}

export interface AiSessionItem {
  chatId: string
}

export interface AiStreamCallbacks {
  onChunk: (chunk: string) => void
  onDone?: () => void
  onError?: (message: string) => void
}

export interface MessageVO {
  role: 'user' | 'assistant'
  content: string
}
