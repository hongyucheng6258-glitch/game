import { get, post, put } from './request'
import type { Order } from '@/types/order'
import type { PageData } from '@/types/common'

export function createOrder(data: any) {
  return post<Order>('/orders', data)
}

export function getOrders(params?: any) {
  return get<PageData<Order>>('/orders/my', params)
}

export function getOrderDetail(orderNo: string) {
  return get<Order>(`/orders/${orderNo}`)
}

export function cancelOrder(orderNo: string) {
  return put(`/orders/${orderNo}/cancel`)
}

export function startService(orderNo: string) {
  return put(`/orders/${orderNo}/start`)
}

export function completeService(orderNo: string) {
  return put(`/orders/${orderNo}/complete`)
}

export function confirmOrder(orderNo: string) {
  return put(`/orders/${orderNo}/confirm`)
}

export function payOrder(data: { orderNo: string; paymentMethod: string }) {
  return post('/payments/pay', data)
}

export function checkOrdered(messageId: number) {
  return get<boolean>(`/orders/check-ordered/${messageId}`)
}

export function applyRefund(orderNo: string) {
  return put(`/orders/${orderNo}/refund`)
}
