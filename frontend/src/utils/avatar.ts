export function getAvatarUrl(avatar: string | null | undefined): string | undefined {
  if (!avatar) return undefined
  
  // 如果已经是完整URL，直接返回
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }
  
  // 如果是相对路径，确保以 / 开头
  if (!avatar.startsWith('/')) {
    return '/' + avatar
  }
  
  return avatar
}
