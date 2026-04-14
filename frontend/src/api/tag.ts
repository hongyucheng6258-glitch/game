import { get } from './request'
import type { ServiceTag } from '@/types/service'

export function getTags() {
  return get<ServiceTag[]>('/service-tags')
}
