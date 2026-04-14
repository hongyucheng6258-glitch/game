import { get, post, put } from './request'
import type { Review } from '@/types/review'
import type { PageData } from '@/types/common'

export function createReview(data: any) {
  return post<Review>('/reviews', data)
}

export function getServiceReviews(serviceId: number, params?: any) {
  return get<PageData<Review>>(`/reviews/service/${serviceId}`, params)
}

export function getProviderReviews(providerId: number, params?: any) {
  return get<PageData<Review>>(`/reviews/provider/${providerId}`, params)
}

export function replyReview(id: number, reply: string) {
  return put(`/reviews/${id}/reply`, { reply })
}

export function getReviewStats(providerId: number) {
  return get<any>(`/reviews/stats/${providerId}`)
}
