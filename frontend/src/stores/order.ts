import { defineStore } from 'pinia'
import { ref } from 'vue'
import { get } from '@/api/request'
import type { Order } from '@/types/order'
import type { PageData } from '@/types/common'

export const useOrderStore = defineStore('order', () => {
  const orders = ref<Order[]>([])
  const total = ref(0)

  async function fetchOrders(params: any = {}) {
    const res = await get<PageData<Order>>('/orders', params)
    orders.value = res.data.records
    total.value = res.data.total
  }

  return { orders, total, fetchOrders }
})
