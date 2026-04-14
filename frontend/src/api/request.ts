import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
})

// Request interceptor
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor
request.interceptors.response.use(
  (response) => {
    const res = response.data
    const url = response.config.url
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401 && !url?.includes('/users/login')) {
        localStorage.removeItem('token')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      const url = error.config.url
      
      if (status === 401 && !url?.includes('/users/login')) {
        localStorage.removeItem('token')
        router.push('/login')
        ElMessage.error('登录已过期，请重新登录')
      } else if (status === 403) {
        ElMessage.error('没有权限访问')
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else if (status === 423) {
        ElMessage.error(error.response.data?.message || '登录失败次数过多，请稍后再试')
      } else {
        ElMessage.error(error.response.data?.message || '服务器错误')
      }
    } else {
      ElMessage.error('网络连接失败')
    }
    return Promise.reject(error)
  }
)

export default request

export const get = <T = any>(url: string, params?: any) =>
  request.get<any, { code: number; message: string; data: T }>(url, { params })

export const post = <T = any>(url: string, data?: any) =>
  request.post<any, { code: number; message: string; data: T }>(url, data)

export const put = <T = any>(url: string, data?: any) =>
  request.put<any, { code: number; message: string; data: T }>(url, data)

export const del = <T = any>(url: string) =>
  request.delete<any, { code: number; message: string; data: T }>(url)
