import { get, post, put } from './request'
import type { Conversation, Message } from '@/types/message'
import type { PageData } from '@/types/common'

export function getConversations() {
  return get<Conversation[]>('/chat/conversations')
}

export function getChatHistory(userId: number, params?: any) {
  return get<PageData<Message>>(`/chat/${userId}`, params)
}

export function sendMessage(data: { receiverId: number; content: string; type?: number }) {
  return post<Message>('/chat/send', data)
}

export function sendPriceRequest(data: { serviceId: number; requestedPrice: number; remark?: string }) {
  return post<Message>('/chat/price-request', data)
}

export function sendPriceResponse(data: { serviceId: number; negotiatedPrice: number; requestMessageId: number; remark?: string }) {
  return post<Message>('/chat/price-response', data)
}

export function markConversationAsRead(otherUserId: number) {
  return put(`/chat/${otherUserId}/read`)
}
