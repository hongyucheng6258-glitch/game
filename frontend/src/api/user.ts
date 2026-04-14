import { get, put, post } from './request'
import type { User } from '@/types/user'

export function getUserInfo() {
  return get<User>('/users/info')
}

export function updateUserInfo(data: any) {
  return put<User>('/users/info', data)
}

export function updatePassword(data: { oldPassword: string; newPassword: string }) {
  return put('/users/password', data)
}

export function updateAvatar(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return post<string>('/users/avatar', formData)
}

export function getBalance() {
  return get<number>('/users/balance')
}

export function getUserById(id: number) {
  return get<User>(`/users/id/${id}`)
}
