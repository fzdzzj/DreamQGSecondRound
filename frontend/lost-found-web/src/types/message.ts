export interface PrivateMessageSendDTO {
  receiverId: number
  messageType: number
  content?: string
  imageUrl?: string
  clientMsgId?: string
}

export interface PrivateMessageVO {
  id: number
  senderId: number
  receiverId: number
  content?: string
  messageType: number
  imageUrl?: string
  status: number
  clientMsgId?: string
  createTime?: string
}

export interface ConversationVO {
  peerId: number
  peerNickname: string
  peerAvatar?: string
  lastMessage?: string
  lastMessageType?: number
  lastImageUrl?: string
  lastMessageTime?: string
  unreadCount: number
}

export interface WsEnvelope<T = unknown> {
  type: string
  data: T
  timestamp: string
}

export interface WsUnreadChangedPayload {
  totalUnreadCount: number
  conversationUnreadCount: number
  peerId: number
}

export interface WsPrivateMessagePayload {
  id: number
  senderId: number
  receiverId: number
  content?: string
  messageType: number
  imageUrl?: string
  status: number
  createTime?: string
}
