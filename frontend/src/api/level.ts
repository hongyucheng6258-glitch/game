import { get } from './request'

export interface Level {
  id: number
  level: number
  name: string
  requiredExp: number
  discountRate: number
  withdrawalFeeDiscount: number
  dailyWithdrawalLimit: number
  maxServicePrice: number
  icon: string
  description: string
}

export interface UserLevelInfo {
  currentLevel: number
  currentLevelName: string
  currentLevelIcon: string
  experience: number
  currentLevelRequiredExp: number
  nextLevelRequiredExp: number
  expToNextLevel: number
  discountRate: number
  withdrawalFeeDiscount: number
  dailyWithdrawalLimit: number
  maxServicePrice: number
  totalExpense: number
}

export const levelApi = {
  getAllLevels: () => get<Level[]>('/level/list'),
  
  getLevelByLevel: (level: number) => get<Level>(`/level/${level}`),
  
  getMyLevelInfo: () => get<UserLevelInfo>('/level/my-info'),
  
  getUserLevelInfo: (userId: number) => get<UserLevelInfo>(`/level/user-info/${userId}`)
}

// 导出单独的函数
export const getAllLevels = levelApi.getAllLevels
export const getLevelByLevel = levelApi.getLevelByLevel
export const getMyLevelInfo = levelApi.getMyLevelInfo
export const getUserLevelInfo = levelApi.getUserLevelInfo
