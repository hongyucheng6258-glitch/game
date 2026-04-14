export interface User {
  id: number
  username: string
  phone: string | null
  email: string | null
  avatar: string | null
  realName: string | null
  role: number
  status: number
  balance: number
  creditScore: number
  currentLevel: number
  experience: number
  totalExpense: number
  createdAt: string
  updatedAt?: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  phone?: string
  email?: string
}

export interface LoginResponse {
  token: string
  user: {
    id: number
    username: string
    phone: string | null
    email: string | null
    avatar: string | null
    role: number
  }
}

export interface PasswordUpdateRequest {
  oldPassword: string
  newPassword: string
}
