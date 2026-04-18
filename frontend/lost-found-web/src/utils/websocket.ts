import { getAccessToken } from './token'
import type { WsEnvelope } from '@/types/message'

export interface WsClientOptions {
  onOpen?: () => void
  onClose?: () => void
  onError?: () => void
  onMessage?: <T>(message: WsEnvelope<T>) => void
}

export class WsClient {
  private socket: WebSocket | null = null
  private heartbeatTimer: number | null = null
  private reconnectTimer: number | null = null
  private closedManually = false
  private readonly options: WsClientOptions

  constructor(options: WsClientOptions = {}) {
    this.options = options
  }

  connect(): void {
    if (
      this.socket &&
      (this.socket.readyState === WebSocket.OPEN ||
        this.socket.readyState === WebSocket.CONNECTING)
    ) {
      console.log('WebSocket 已连接或正在连接，跳过重复连接')
      return
    }

    this.closedManually = false

    const rawToken = getAccessToken()
    console.log('WebSocket token=', rawToken)

    if (!rawToken) {
      console.warn('WebSocket 连接失败：token为空')
      return
    }

    const token = encodeURIComponent(rawToken)
    const url = `${import.meta.env.VITE_WS_BASE}/ws/private-chat?token=${token}`

    console.log('WebSocket 连接地址=', url)

    this.socket = new WebSocket(url)

    this.socket.onopen = () => {
      console.log('WebSocket 连接成功')
      this.options.onOpen?.()
      this.startHeartbeat()
    }

    this.socket.onmessage = (event) => {
      console.log('WebSocket 收到原始消息=', event.data)
      try {
        const message = JSON.parse(event.data) as WsEnvelope
        this.options.onMessage?.(message)
      } catch (e) {
        console.error('WebSocket 消息解析失败', e)
      }
    }

    this.socket.onerror = (event) => {
      console.error('WebSocket 连接异常', event)
      this.options.onError?.()
    }

    this.socket.onclose = (event) => {
      console.warn('WebSocket 连接关闭', {
        code: event.code,
        reason: event.reason,
        wasClean: event.wasClean
      })

      this.stopHeartbeat()
      this.options.onClose?.()

      if (!this.closedManually) {
        this.reconnectTimer = window.setTimeout(() => this.connect(), 3000)
      }
    }
  }

  send(payload: unknown): void {
    if (this.socket?.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify(payload))
    } else {
      console.warn('WebSocket 发送失败，连接未打开', payload)
    }
  }

  disconnect(): void {
    this.closedManually = true
    this.stopHeartbeat()

    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }

    this.socket?.close()
    this.socket = null
  }

  private startHeartbeat(): void {
    this.stopHeartbeat()
    this.heartbeatTimer = window.setInterval(() => {
      this.send({ type: 'PING', data: 'ping' })
    }, 15000)
  }

  private stopHeartbeat(): void {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }
}
