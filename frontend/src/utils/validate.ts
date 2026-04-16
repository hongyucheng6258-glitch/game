export function isValidPhone(phone: string): boolean {
  return /^1[3-9]\d{9}$/.test(phone)
}

export function isValidEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

export function isValidPassword(password: string): boolean {
  if (!password || password.length < 6 || password.length > 100) return false
  let typeCount = 0
  if (/[a-zA-Z]/.test(password)) typeCount++
  if (/[0-9]/.test(password)) typeCount++
  if (/[^a-zA-Z0-9]/.test(password)) typeCount++
  return typeCount >= 2
}

export function isValidIdCard(idCard: string): boolean {
  // 简单的身份证验证（18位）
  return /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[\dXx]$/.test(idCard)
}
