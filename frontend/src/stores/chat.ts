import { defineStore } from 'pinia'
import { ref } from 'vue'
import { get, post } from '@/api/request'
import type { Conversation, Message } from '@/types/message'
import type { PageData } from '@/types/common'

export const useChatStore = defineStore('chat', () => {
  const conversations = ref<Conversation[]>([])
  const currentMessages = ref<Message[]>([])
  const currentChatUserId = ref<number | null>(null)

  async function fetchConversations() {
    const res = await get<Conversation[]>('/chat/conversations')
    conversations.value = res.data
  }

  async function fetchChatHistory(userId: number, page = 1, size = 50) {
    currentChatUserId.value = userId
    const res = await get<PageData<Message>>(`/chat/${userId}`, { page, size })
    currentMessages.value = res.data.records
  }

  async function sendMessage(receiverId: number, content: string) {
    const res = await post<Message>('/chat/send', { receiverId, content })
    currentMessages.value.push(res.data)
    return res.data
  }

  function addMessage(message: Message) {
    currentMessages.value.push(message)
  }

  return { conversations, currentMessages, currentChatUserId, fetchConversations, fetchChatHistory, sendMessage, addMessage }
})
