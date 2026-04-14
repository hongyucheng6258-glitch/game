import { defineStore } from 'pinia'
import { ref } from 'vue'
import { get } from '@/api/request'
import type { Service } from '@/types/service'
import type { PageData } from '@/types/common'

export const useServiceStore = defineStore('service', () => {
  const services = ref<Service[]>([])
  const total = ref(0)
  const loading = ref(false)

  async function fetchServices(params: any = {}) {
    loading.value = true
    try {
      const res = await get<PageData<Service>>('/services', params)
      services.value = res.data.records
      total.value = res.data.total
    } finally {
      loading.value = false
    }
  }

  return { services, total, loading, fetchServices }
})
