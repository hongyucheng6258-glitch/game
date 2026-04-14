export interface Activity {
  id: number
  title: string
  description: string
  type: number
  discountRate: number
  startTime: string
  endTime: string
  status: number
  image?: string
  services?: ActivityServiceItem[]
  createdAt?: string
  updatedAt?: string
}

export interface ActivityServiceItem {
  serviceId: number
  serviceTitle?: string
  servicePrice?: number
  specialPrice?: number
}

export const ACTIVITY_TYPE_LABELS: Record<number, string> = {
  0: '全场折扣',
  1: '指定服务折扣',
  2: '指定服务特价',
}

export const ACTIVITY_STATUS_LABELS: Record<number, string> = {
  0: '未开始',
  1: '进行中',
  2: '已结束',
}
