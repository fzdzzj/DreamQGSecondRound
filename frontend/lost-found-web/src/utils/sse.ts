export async function createSseStream(
  url: string,
  handlers: {
    onChunk: (text: string) => void
    onDone: () => void
    onError: (msg: string) => void
  }
) {
  const controller = new AbortController()

  const res = await fetch(url, {
    method: 'GET',
    headers: {
      Authorization: `Bearer ${localStorage.getItem('ACCESS_TOKEN') || ''}`
    },
    signal: controller.signal
  })

  if (!res.ok || !res.body) {
    handlers.onError('SSE连接失败')
    return controller
  }

  const reader = res.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })

    // 一个完整 SSE 事件以 \n\n 结尾
    const blocks = buffer.split('\n\n')
    buffer = blocks.pop() || ''

    for (const block of blocks) {
      // 跳过空块
      if (!block.trim()) continue

      const lines = block.split('\n')
      let eventName = 'message'
      let data = ''

      for (const line of lines) {
        // 跳过空行
        if (!line.trim()) continue

        // 处理错误的SSE格式：移除所有"data:"前缀
        let processedLine = line.trim()
        while (processedLine.startsWith('data:')) {
          processedLine = processedLine.replace('data:', '').trim()
        }

        if (processedLine.startsWith('event:')) {
          eventName = processedLine.replace('event:', '').trim()
        } else {
          // 剩下的部分都是数据
          data += processedLine
        }
      }

      if (eventName === 'message' && data) {
        handlers.onChunk(data)
      } else if (eventName === 'done') {
        handlers.onDone()
        return controller
      } else if (eventName === 'error') {
        handlers.onError(data || '流式响应异常')
        return controller
      }
    }
  }

  return controller
}