import { get, post, del } from './request'
import type { PageData } from '@/types/common'
import type { Service } from '@/types/service'

export function addFavorite(serviceId: number) {
  return post(`/favorites/${serviceId}`)
}

export function removeFavorite(serviceId: number) {
  return del(`/favorites/${serviceId}`)
}

export function getFavorites(params?: any) {
  return get<PageData<any>>('/favorites', params)
}

export function checkFavorite(serviceId: number) {
  return get<{ favorited: boolean }>(`/favorites/check/${serviceId}`)
}
