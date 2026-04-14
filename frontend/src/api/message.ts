import { get, put, del } from './request'
import type { Message } from '@/types/message'
import type { PageData } from '@/types/common'

export function getMessages(params?: any) {
  return get<PageData<Message>>('/messages', params)
}

export function markAsRead(id: number) {
  return put(`/messages/${id}/read`)
}

export function markAllAsRead() {
  return put('/messages/read-all')
}

export function getUnreadCount() {
  return get<number>('/messages/unread-count')
}

export function deleteMessage(id: number) {
  return del(`/messages/${id}`)
}
