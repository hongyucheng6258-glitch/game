import { get, post } from './request'
import type { PaymentRecord } from '@/types/payment'
import type { PageData } from '@/types/common'

export function payOrder(data: { orderNo: string; paymentMethod: string }) {
  return post('/payments/pay', data)
}

export function recharge(data: { amount: number; paymentMethod: string }) {
  return post('/payments/recharge', data)
}

export function getPaymentRecords(params?: any) {
  return get<PageData<PaymentRecord>>('/payment-records', params)
}

export function getPaymentStatistics() {
  return get<any>('/payments/statistics')
}
