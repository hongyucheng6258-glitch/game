import { defineStore } from 'pinia'
import { ref } from 'vue'
import { get, put, del } from '@/api/request'
import type { Message } from '@/types/message'
import type { PageData } from '@/types/common'
import { useSystemStore } from './system'

export const useMessageStore = defineStore('message', () => {
  const messages = ref<Message[]>([])
  const total = ref(0)

  async function fetchMessages(params: any = {}) {
    const res = await get<PageData<Message>>('/messages', params)
    messages.value = res.data.records
    total.value = res.data.total
  }

  async function markAsRead(id: number) {
    await put(`/messages/${id}/read`)
  }

  async function markAllAsRead() {
    await put('/messages/read-all')
  }

  async function deleteMessage(id: number) {
    await del(`/messages/${id}`)
  }

  async function fetchUnreadCount() {
    try {
      const res = await get<{ total: number; system: number }>('/messages/unread-count')
      const systemStore = useSystemStore()
      systemStore.setUnreadCount(res.data.total)
    } catch (error) {
      console.error('获取未读消息数失败:', error)
    }
  }

  return { messages, total, fetchMessages, markAsRead, markAllAsRead, deleteMessage, fetchUnreadCount }
})
