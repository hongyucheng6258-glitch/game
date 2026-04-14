export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface PageData<T = any> {
  total: number
  pages: number
  current: number
  records: T[]
}

export interface PageQuery {
  page?: number
  size?: number
}
