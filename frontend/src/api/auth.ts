import { post } from './request'
import type { LoginResponse, User } from '@/types/user'

export function login(data: { username: string; password: string }) {
  return post<LoginResponse>('/users/login', data)
}

export function register(data: { username: string; password: string; phone?: string; email?: string }) {
  return post<User>('/users/register', data)
}
