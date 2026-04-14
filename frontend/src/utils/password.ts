export function calculatePasswordStrength(password: string): number {
  if (!password || password.length === 0) {
    return 0
  }

  let strength = 0

  if (password.length >= 6) strength += 1
  if (password.length >= 8) strength += 1
  if (password.length >= 12) strength += 1

  if (/[a-z]/.test(password)) strength += 1
  if (/[A-Z]/.test(password)) strength += 1
  if (/[0-9]/.test(password)) strength += 1
  if (/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) strength += 1

  return Math.min(strength, 5)
}

export function getPasswordStrengthColor(strength: number): string {
  switch (strength) {
    case 0:
      return '#e5e7eb'
    case 1:
      return '#ef4444'
    case 2:
      return '#f97316'
    case 3:
      return '#eab308'
    case 4:
      return '#22c55e'
    case 5:
      return '#10b981'
    default:
      return '#e5e7eb'
  }
}

export function getPasswordStrengthLabel(strength: number): string {
  switch (strength) {
    case 0:
      return '请输入密码'
    case 1:
      return '非常弱'
    case 2:
      return '弱'
    case 3:
      return '一般'
    case 4:
      return '较强'
    case 5:
      return '非常强'
    default:
      return '未知'
  }
}
