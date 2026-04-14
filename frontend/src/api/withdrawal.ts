import { get, post } from './request'
import type { WithdrawalApplication } from '@/types/payment'
import type { PageData } from '@/types/common'

export function applyWithdrawal(data: any) {
  return post<WithdrawalApplication>('/withdrawals', data)
}

export function getWithdrawals(params?: any) {
  return get<PageData<WithdrawalApplication>>('/withdrawals', params)
}
