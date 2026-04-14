import { get, post, put } from './request'
import type { Service, ServiceTag } from '@/types/service'
import type { PageData } from '@/types/common'

export function getServices(params?: any) {
  return get<PageData<Service>>('/services', params)
}

export function getServiceDetail(id: number) {
  return get<Service>(`/services/${id}`)
}

export function createService(data: any) {
  return post<Service>('/services', data)
}

export function updateService(id: number, data: any) {
  return put<Service>(`/services/${id}`, data)
}

export function toggleServiceStatus(id: number) {
  return put(`/services/${id}/status`)
}

export function getRecommendServices() {
  return get<Service[]>('/services/recommend')
}

export function getProviderServices(providerId: number) {
  return get<Service[]>(`/services/provider/${providerId}`)
}

export function getServiceTags() {
  return get<ServiceTag[]>('/service-tags')
}
