import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { post, get } from '@/api/request'
import { getToken, setToken, removeToken, setUserInfo, removeUserInfo, getUserInfo } from '@/utils/storage'
import type { LoginResponse, User } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken() || '')
  const userInfo = ref<any>(getUserInfo())

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 2)
  const isProvider = computed(() => userInfo.value?.role >= 1)

  async function login(username: string, password: string) {
    const res = await post<LoginResponse>('/users/login', { username, password })
    token.value = res.data.token
    userInfo.value = res.data.user
    setToken(res.data.token)
    setUserInfo(res.data.user)
    return res
  }

  async function sendSmsCode(phone: string) {
    const res = await post<string>('/users/send-sms', { phone })
    return res.data
  }

  async function phoneLogin(phone: string, code: string) {
    const res = await post<LoginResponse>('/users/phone-login', { phone, code })
    token.value = res.data.token
    userInfo.value = res.data.user
    setToken(res.data.token)
    setUserInfo(res.data.user)
    return res
  }

  async function resetPassword(phone: string, code: string, newPassword: string) {
    await post('/users/reset-password', { phone, code, newPassword })
  }

  async function register(data: any) {
    const res = await post<User>('/users/register', data)
    return res
  }

  async function checkUsername(username: string) {
    const res = await get<boolean>('/users/check-username', { username })
    return res.data
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    removeToken()
    removeUserInfo()
  }

  async function fetchUserInfo() {
    try {
      const res = await get<User>('/users/info')
      userInfo.value = res.data
      setUserInfo(res.data)
      return res.data
    } catch {
      logout()
    }
  }

  return { token, userInfo, isLoggedIn, isAdmin, isProvider, login, sendSmsCode, phoneLogin, resetPassword, register, logout, fetchUserInfo }
})
