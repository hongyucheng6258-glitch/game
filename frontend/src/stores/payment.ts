import { defineStore } from 'pinia'
import { ref } from 'vue'
import { get } from '@/api/request'
import type { PaymentRecord } from '@/types/payment'
import type { PageData } from '@/types/common'

export const usePaymentStore = defineStore('payment', () => {
  const records = ref<PaymentRecord[]>([])
  const total = ref(0)

  async function fetchRecords(params: any = {}) {
    const res = await get<PageData<PaymentRecord>>('/payment-records', params)
    records.value = res.data.records
    total.value = res.data.total
  }

  return { records, total, fetchRecords }
})
