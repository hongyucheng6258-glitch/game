export interface Review {
  id: number
  orderId: number
  userId: number
  userName?: string
  userAvatar?: string
  providerId: number
  serviceId: number
  rating: number
  content: string | null
  reply: string | null
  isAnonymous: boolean
  createdAt: string
}
