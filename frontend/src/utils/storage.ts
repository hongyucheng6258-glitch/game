export function getToken(): string | null {
  return localStorage.getItem('token')
}

export function setToken(token: string): void {
  localStorage.setItem('token', token)
}

export function removeToken(): void {
  localStorage.removeItem('token')
}

export function getUserInfo(): any {
  const info = localStorage.getItem('userInfo')
  return info ? JSON.parse(info) : null
}

export function setUserInfo(info: any): void {
  localStorage.setItem('userInfo', JSON.stringify(info))
}

export function removeUserInfo(): void {
  localStorage.removeItem('userInfo')
}
