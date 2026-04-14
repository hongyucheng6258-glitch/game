import { get, post, put } from './request'
import type { PageData } from '@/types/common'

export interface Complaint {
  id: number
  orderId: number
  complainantId: number
  respondentId: number
  type: number
  content: string
  evidenceImages?: string
  status: number
  arbitrationResult?: string
  arbitratorId?: number
  arbitratedAt?: string
  createdAt: string
  updatedAt: string
}

export function createComplaint(data: {
  orderId: number
  type: number
  content: string
  evidenceImages?: string
}) {
  return post<Complaint>('/complaints', data)
}

export function getComplaint(id: number) {
  return get<Complaint>(`/complaints/${id}`)
}

export function getMyComplaints(params?: any) {
  return get<PageData<Complaint>>('/complaints/my', params)
}

// Admin API
export function getAdminComplaints(params?: any) {
  return get<PageData<Complaint>>('/admin/complaints', params)
}

export function getAdminComplaint(id: number) {
  return get<Complaint>(`/admin/complaints/${id}`)
}

export function arbitrateComplaint(data: {
  complaintId: number
  arbitrationResult: string
  decision: number
}) {
  return put<Complaint>('/admin/complaints/arbitrate', data)
}
