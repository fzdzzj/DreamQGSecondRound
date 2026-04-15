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
      Authorization: `Bearer ${localStorage.getItem('ACCESS_TOKEN')}`
    },
    signal: controller.signal
  })

  const reader = res.body!.getReader()
  const decoder = new TextDecoder()

  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })

    let lines = buffer.split('\n')
    buffer = lines.pop() || ''

    for (const line of lines) {
      if (!line.startsWith('data:')) continue

      const data = line.replace('data:', '').trim()

      if (data === '[DONE]') {
        handlers.onDone()
        return
      }

      handlers.onChunk(data)
    }
  }

  return controller
}
