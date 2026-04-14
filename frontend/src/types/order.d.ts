export interface Order {
  id: number
  orderNo: string
  userId: number
  userName?: string
  providerId: number
  providerName?: string
  serviceId: number
  serviceTitle?: string
  totalAmount: number
  status: number
  requirements: string | null
  startTime: string | null
  endTime: string | null
  paymentTime: string | null
  paymentMethod?: string
  createdAt: string
}

export interface OrderCreateRequest {
  serviceId: number
  gameAccountId?: number
  requirements?: string
  negotiatedPrice?: number
  priceNegotiationMessageId?: number
}
