export interface Service {
  id: number
  providerId: number
  providerName?: string
  providerAvatar?: string
  gameType: string
  serviceType: number
  title: string
  description: string
  price: number
  duration: number
  status: number
  rating: number
  reviewCount: number
  salesCount: number
  tags?: string[]
  createdAt: string
  activityId?: number
  activityTitle?: string
  activityPrice?: number
  activityDiscountRate?: number
  activityType?: number
}

export interface ServiceTag {
  id: number
  name: string
  useCount?: number
  createdAt?: string
  updatedAt?: string
}
