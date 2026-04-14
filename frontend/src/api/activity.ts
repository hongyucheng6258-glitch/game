import { get } from './request'
import type { Activity } from '@/types/activity'

export function getActiveActivities() {
  return get<Activity[]>('/activities')
}

export function getActivityDetail(id: number) {
  return get<Activity>(`/activities/${id}`)
}
