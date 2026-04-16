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
    this.closedManually = false
    const token = encodeURIComponent(getAccessToken())
    const url = `${import.meta.env.VITE_WS_BASE}/ws/private-chat?token=${token}`

    this.socket = new WebSocket(url)

    this.socket.onopen = () => {
      this.options.onOpen?.()
      this.startHeartbeat()
    }

    this.socket.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data) as WsEnvelope
        this.options.onMessage?.(message)
      } catch {
        // ignore
      }
    }

    this.socket.onerror = () => {
      this.options.onError?.()
    }

    this.socket.onclose = () => {
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
