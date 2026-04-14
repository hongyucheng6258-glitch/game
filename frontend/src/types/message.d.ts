export interface Message {
  id: number
  senderId: number
  receiverId: number
  conversationId: number | null
  type: number
  content: string
  isRead: number
  createdAt: string
  relatedId: number | null
  relatedType: string | null
}

export interface PriceRequestData {
  serviceId: number
  serviceTitle: string
  originalPrice: number
  requestedPrice: number
  remark?: string
}

export interface PriceResponseData {
  serviceId: number
  serviceTitle: string
  negotiatedPrice: number
  requestMessageId: number
  remark?: string
}

export interface Conversation {
  id: number
  user1Id: number
  user2Id: number
  lastMessage: string | null
  lastMessageTime: string | null
  updatedAt: string
  otherUserName?: string
  otherUserAvatar?: string
  unreadCount?: number
}

export const MESSAGE_TYPE_SYSTEM = 0
export const MESSAGE_TYPE_CHAT = 1
export const MESSAGE_TYPE_PRICE_REQUEST = 2
export const MESSAGE_TYPE_PRICE_RESPONSE = 3
