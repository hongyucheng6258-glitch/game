export interface PaymentRecord {
  id: number
  userId: number
  orderId: number | null
  amount: number
  type: number
  paymentMethod: string | null
  status: number
  transactionNo: string | null
  createdAt: string
}

export interface WithdrawalApplication {
  id: number
  userId: number
  amount: number
  bankAccount: string
  bankName: string
  accountName: string
  status: number
  auditUserId: number | null
  auditTime: string | null
  auditRemark: string | null
  createdAt: string
}
