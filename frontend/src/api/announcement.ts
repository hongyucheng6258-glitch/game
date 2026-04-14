import { get } from './request'

export function getAnnouncements(params?: any) {
  return get<any>('/announcements', params)
}

export function getAnnouncementDetail(id: number) {
  return get<any>(`/announcements/${id}`)
}
