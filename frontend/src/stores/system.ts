import { defineStore } from 'pinia'
import { ref } from 'vue'
import { get } from '@/api/request'
import type { ServiceTag } from '@/types/service'

interface SystemSettings {
  siteName: string
  siteDescription: string
  commissionRate: number
  minPrice: number
  maxPrice: number
  autoConfirmDays: number
  minWithdrawAmount: number
  maxWithdrawAmount: number
  dailyWithdrawLimit: number
  withdrawCommissionRate: number
  allowRegister: boolean
  defaultRole: number
}

export const useSystemStore = defineStore('system', () => {
  const tags = ref<ServiceTag[]>([])
  const unreadCount = ref(0)
  const settings = ref<SystemSettings>({
    siteName: '电竞陪玩平台',
    siteDescription: '',
    commissionRate: 10,
    minPrice: 1,
    maxPrice: 9999,
    autoConfirmDays: 7,
    minWithdrawAmount: 10,
    maxWithdrawAmount: 50000,
    dailyWithdrawLimit: 3,
    withdrawCommissionRate: 5,
    allowRegister: true,
    defaultRole: 0,
  })

  async function fetchTags() {
    try {
      const res = await get<ServiceTag[]>('/service-tags')
      tags.value = res.data
    } catch {
      // ignore
    }
  }

  async function fetchSettings() {
    try {
      const res = await get<SystemSettings>('/settings')
      if (res.data) {
        settings.value = res.data
      }
    } catch {
      // ignore
    }
  }

  function setUnreadCount(count: number) {
    unreadCount.value = count
  }

  return { tags, unreadCount, settings, fetchTags, fetchSettings, setUnreadCount }
})
