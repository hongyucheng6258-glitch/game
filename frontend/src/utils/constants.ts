// 用户角色
export const ROLE_USER = 0
export const ROLE_PROVIDER = 1
export const ROLE_ADMIN = 2

// 订单状态
export const ORDER_STATUS = {
  PENDING_PAYMENT: 0,
  PENDING_SERVICE: 1,
  IN_SERVICE: 2,
  PENDING_REVIEW: 3,
  COMPLETED: 4,
  CANCELLED: 5,
  REFUNDING: 6,
  REFUNDED: 7,
} as const

export const ORDER_STATUS_LABELS: Record<number, string> = {
  0: '待支付',
  1: '待服务',
  2: '服务中',
  3: '待评价',
  4: '已完成',
  5: '已取消',
  6: '退款中',
  7: '已退款',
}

export const ORDER_STATUS_TYPES: Record<number, string> = {
  0: 'warning',
  1: 'info',
  2: 'primary',
  3: 'success',
  4: 'success',
  5: 'info',
  6: 'danger',
  7: 'danger',
}

// 服务类型
export const SERVICE_TYPE_LABELS: Record<number, string> = {
  0: '陪玩',
  1: '代打',
}

// 游戏类型
export const GAME_TYPES = ['LOL', 'DOTA2', 'CSGO', '王者荣耀', '和平精英', '原神', '永劫无间', 'APEX']

// 支付方式
export const PAYMENT_METHODS = [
  { label: '余额支付', value: 'balance' },
  { label: '微信支付', value: 'wechat' },
  { label: '支付宝支付', value: 'alipay' },
]

// 投诉状态
export const COMPLAINT_STATUS = {
  PENDING: 0,
  PROCESSING: 1,
  RESOLVED: 2,
  REJECTED: 3,
} as const

export const COMPLAINT_STATUS_LABELS: Record<number, string> = {
  0: '待处理',
  1: '处理中',
  2: '已解决',
  3: '已驳回',
}

export const COMPLAINT_STATUS_TYPES: Record<number, string> = {
  0: 'warning',
  1: 'primary',
  2: 'success',
  3: 'danger',
}

// 投诉类型
export const COMPLAINT_TYPE_LABELS: Record<number, string> = {
  0: '服务质量问题',
  1: '延迟交付',
  2: '欺诈行为',
  3: '其他问题',
}

// 支付类型
export const PAYMENT_TYPE_LABELS: Record<number, string> = {
  0: '消费',
  1: '充值',
  2: '提现',
  3: '收入',
  4: '退款',
  5: '平台费',
  6: '罚金',
}
