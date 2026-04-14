<template>
  <div class="balance-page">
    <h2 class="page-title">余额管理</h2>

    <!-- 余额展示 -->
    <section class="balance-section">
      <el-card shadow="never" class="balance-card">
        <div class="balance-content">
          <div class="balance-info">
            <span class="balance-label">当前余额</span>
            <span class="balance-amount">{{ formatMoney(balance) }}</span>
          </div>
          <div class="balance-actions">
            <el-button type="primary" @click="showRechargeDialog = true">
              <el-icon><Plus /></el-icon> 充值
            </el-button>
            <el-button @click="showWithdrawDialog = true">
              <el-icon><Sort /></el-icon> 提现
            </el-button>
          </div>
        </div>
      </el-card>
    </section>

    <!-- 充值对话框 -->
    <el-dialog
      v-model="showRechargeDialog"
      title="账户充值"
      width="440px"
      destroy-on-close
    >
      <div class="recharge-amounts">
        <div
          v-for="amount in rechargeOptions"
          :key="amount"
          class="amount-option"
          :class="{ active: rechargeAmount === amount }"
          @click="rechargeAmount = amount"
        >
          {{ formatMoney(amount) }}
        </div>
      </div>
      <el-form label-position="top" style="margin-top: 16px">
        <el-form-item label="自定义金额">
          <el-input-number
            v-model="rechargeAmount"
            :min="1"
            :max="10000"
            :step="10"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-radio-group v-model="paymentMethod" size="large">
            <el-radio-button
              v-for="method in paymentMethods"
              :key="method.value"
              :label="method.value"
            >
              {{ method.label }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRechargeDialog = false">取消</el-button>
        <el-button type="primary" :loading="recharging" @click="handleRecharge">
          确认充值
        </el-button>
      </template>
    </el-dialog>

    <!-- 提现对话框 -->
    <el-dialog
      v-model="showWithdrawDialog"
      title="申请提现"
      width="480px"
      destroy-on-close
    >
      <el-form
        ref="withdrawFormRef"
        :model="withdrawForm"
        :rules="withdrawRules"
        label-position="top"
      >
        <el-form-item label="提现金额" prop="amount">
          <el-input-number
            v-model="withdrawForm.amount"
            :min="1"
            :max="balance"
            :step="10"
            controls-position="right"
            style="width: 100%"
            @change="calculateFee"
          />
        </el-form-item>
        <div v-if="withdrawForm.amount > 0" class="fee-preview">
          <div class="fee-item">
            <span class="fee-label">提现金额：</span>
            <span class="fee-value">{{ formatMoney(withdrawForm.amount) }}</span>
          </div>
          <div class="fee-item">
            <span class="fee-label">标准手续费（5%）：</span>
            <span class="fee-value">{{ formatMoney(withdrawForm.amount * 5 / 100) }}</span>
          </div>
          <div v-if="levelInfo" class="fee-item">
            <span class="fee-label">等级特权折扣（{{ levelInfo.currentLevelName }}）：</span>
            <span class="fee-value discount">{{ formatMoney(withdrawForm.amount * 5 / 100 * (1 - levelInfo.withdrawalFeeDiscount)) }}</span>
          </div>
          <div class="fee-item">
            <span class="fee-label">实际手续费（{{ withdrawFeeRate.toFixed(2) }}%）：</span>
            <span class="fee-value fee-amount">{{ formatMoney(withdrawFeeAmount) }}</span>
          </div>
          <div class="fee-item total">
            <span class="fee-label">实际到账：</span>
            <span class="fee-value actual-amount">{{ formatMoney(withdrawActualAmount) }}</span>
          </div>
        </div>
        <el-form-item label="银行名称" prop="bankName">
          <el-input v-model="withdrawForm.bankName" placeholder="请输入银行名称" />
        </el-form-item>
        <el-form-item label="银行卡号" prop="bankAccount">
          <el-input v-model="withdrawForm.bankAccount" placeholder="请输入银行卡号" />
        </el-form-item>
        <el-form-item label="持卡人姓名" prop="accountName">
          <el-input v-model="withdrawForm.accountName" placeholder="请输入持卡人姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showWithdrawDialog = false">取消</el-button>
        <el-button type="primary" :loading="withdrawing" @click="handleWithdraw">
          确认提现
        </el-button>
      </template>
    </el-dialog>

    <!-- 最近交易 -->
    <section class="records-section">
      <el-card shadow="never">
        <template #header>
          <div class="card-header-row">
            <span class="card-header-title">最近交易</span>
            <el-button text type="primary" @click="$router.push('/user/payment-records')">
              查看全部 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </template>
        <div v-loading="recordLoading" class="record-list">
          <template v-if="recentRecords.length > 0">
            <div v-for="record in recentRecords" :key="record.id" class="record-item">
              <div class="record-info">
                <span class="record-type">
                  {{ getRecordTypeLabel(record.type) }}
                </span>
                <span class="record-time">{{ formatDate(record.createdAt) }}</span>
              </div>
              <span
                class="record-amount"
                :class="{ income: record.amount > 0, expense: record.amount < 0 }"
              >
                {{ record.amount > 0 ? '+' : '' }}{{ formatMoney(record.amount) }}
              </span>
            </div>
          </template>
          <el-empty v-else description="暂无交易记录" :image-size="80" />
        </div>
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Sort, ArrowRight } from '@element-plus/icons-vue'
import { get, post } from '@/api/request'
import type { PaymentRecord } from '@/types/payment'
import type { PageData } from '@/types/common'
import { useUserStore } from '@/stores/user'
import { formatMoney, formatDate } from '@/utils/format'
import { PAYMENT_TYPE_LABELS } from '@/utils/constants'
import { getUserLevelInfo, getMyLevelInfo } from '@/api/level'
import type { UserLevelInfo } from '@/api/level'

const userStore = useUserStore()
const balance = ref(0)

// 等级信息
const levelInfo = ref<UserLevelInfo | null>(null)

// 充值
const showRechargeDialog = ref(false)
const recharging = ref(false)
const rechargeAmount = ref(50)
const rechargeOptions = [10, 50, 100, 200, 500, 1000]
const paymentMethod = ref('alipay')
const paymentMethods = [
  { label: '支付宝', value: 'alipay' },
  { label: '微信', value: 'wechat' },
  { label: '余额', value: 'balance' }
]

// 提现
const showWithdrawDialog = ref(false)
const withdrawing = ref(false)
const withdrawFormRef = ref<FormInstance>()
const withdrawForm = reactive({
  amount: 100,
  bankName: '',
  bankAccount: '',
  accountName: '',
})
const withdrawFeeRate = ref(5)
const withdrawFeeAmount = ref(0)
const withdrawActualAmount = ref(0)
const withdrawRules: FormRules = {
  amount: [{ required: true, message: '请输入提现金额', trigger: 'blur' }],
  bankName: [{ required: true, message: '请输入银行名称', trigger: 'blur' }],
  bankAccount: [{ required: true, message: '请输入银行卡号', trigger: 'blur' }],
  accountName: [{ required: true, message: '请输入持卡人姓名', trigger: 'blur' }],
}

function calculateFee() {
  if (!withdrawForm.amount || withdrawForm.amount <= 0) {
    withdrawFeeAmount.value = 0
    withdrawActualAmount.value = 0
    return
  }
  
  // 应用等级特权折扣
  let actualRate = 5
  if (levelInfo.value && levelInfo.value.withdrawalFeeDiscount) {
    // 等级折扣已经是小数形式，比如0.60表示60%折扣，即实际手续费为 5% * 折扣
    actualRate = 5 * levelInfo.value.withdrawalFeeDiscount
  }
  withdrawFeeRate.value = actualRate
  
  withdrawFeeAmount.value = Math.round((withdrawForm.amount * actualRate / 100) * 100) / 100
  withdrawActualAmount.value = Math.round((withdrawForm.amount - withdrawFeeAmount.value) * 100) / 100
}

async function fetchWithdrawSettings() {
  try {
    const res = await get<any>('/public/settings')
    if (res.data?.withdrawCommissionRate !== undefined) {
      withdrawFeeRate.value = res.data.withdrawCommissionRate
    }
  } catch {
    // ignore
  }
  calculateFee()
}

async function fetchUserLevelInfo() {
  try {
    const res = await getMyLevelInfo()
    levelInfo.value = res.data
    calculateFee() // 重新计算手续费
  } catch {
    // ignore
  }
}

// 最近交易
const recordLoading = ref(false)
const recentRecords = ref<PaymentRecord[]>([])

function getRecordTypeLabel(type: number): string {
  return PAYMENT_TYPE_LABELS[type] || '其他'
}

async function fetchBalance() {
  try {
    const res = await get<number>('/users/balance')
    console.log('fetchBalance response:', res)
    balance.value = res.data
  } catch (error) {
    console.error('fetchBalance error:', error)
    balance.value = userStore.userInfo?.balance || 0
  }
}

async function fetchRecentRecords() {
  recordLoading.value = true
  try {
    const res = await get<PageData<PaymentRecord>>('/payment-records', {
      page: 1,
      size: 10,
    })
    recentRecords.value = res.data.records
  } catch {
    // ignore
  } finally {
    recordLoading.value = false
  }
}

async function handleRecharge() {
  if (rechargeAmount.value <= 0) {
    ElMessage.warning('请输入充值金额')
    return
  }

  recharging.value = true
  try {
    await post('/payment-records/recharge', {
      amount: rechargeAmount.value,
      paymentMethod: paymentMethod.value
    })
    ElMessage.success('充值成功')
    showRechargeDialog.value = false
    fetchBalance()
    fetchRecentRecords()
    userStore.fetchUserInfo()
  } catch {
    // handled by interceptor
  } finally {
    recharging.value = false
  }
}

async function handleWithdraw() {
  if (!withdrawFormRef.value) return
  await withdrawFormRef.value.validate(async (valid) => {
    if (!valid) return

    withdrawing.value = true
    try {
      await post('/withdrawals', withdrawForm)
      ElMessage.success('提现申请已提交')
      showWithdrawDialog.value = false
      withdrawForm.amount = 100
      withdrawForm.bankName = ''
      withdrawForm.bankAccount = ''
      withdrawForm.accountName = ''
      fetchBalance()
      fetchRecentRecords()
      userStore.fetchUserInfo()
    } catch (error) {
      console.error('handleWithdraw error:', error)
      // handled by interceptor
    } finally {
      withdrawing.value = false
    }
  })
}

onMounted(() => {
  fetchBalance()
  fetchRecentRecords()
  fetchWithdrawSettings()
  fetchUserLevelInfo()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.balance-page {
  padding: $spacing-lg 0;
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: $text-primary;
}

// 余额卡片
.balance-card {
  :deep(.el-card__body) {
    padding: $spacing-xl;
  }
}

.balance-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.balance-info {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.balance-label {
  color: $text-secondary;
  font-size: 14px;
}

.balance-amount {
  font-size: 36px;
  font-weight: 700;
  color: $primary-light;
}

.balance-actions {
  display: flex;
  gap: $spacing-sm;
}

// 充值金额选择
.recharge-amounts {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-sm;
}

.amount-option {
  padding: $spacing-md;
  text-align: center;
  border: 1px solid $border-color;
  border-radius: $border-radius;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
  transition: all 0.2s;

  &:hover,
  &.active {
    border-color: $primary-color;
    color: $primary-light;
    background: rgba($primary-color, 0.1);
  }
}

// 交易记录
.card-header-title {
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
}

.card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.record-list {
  min-height: 100px;
}

.record-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md 0;
  border-bottom: 1px solid $border-color;

  &:last-child {
    border-bottom: none;
  }
}

.record-info {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.record-type {
  font-size: 14px;
  color: $text-primary;
  font-weight: 500;
}

.record-time {
  font-size: 12px;
  color: $text-muted;
}

.record-amount {
  font-size: 16px;
  font-weight: 600;

  &.income {
    color: $success-color;
  }

  &.expense {
    color: $danger-color;
  }
}

// 手续费预览
.fee-preview {
  background: rgba($primary-color, 0.08);
  border-radius: $border-radius;
  padding: $spacing-md;
  margin-bottom: $spacing-md;
}

.fee-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-xs 0;
  font-size: 14px;

  &.total {
    border-top: 1px dashed $border-color;
    margin-top: $spacing-xs;
    padding-top: $spacing-sm;
    font-weight: 600;
  }
}

.fee-label {
  color: $text-secondary;
}

.fee-value {
  color: $text-primary;
  font-weight: 500;

  &.fee-amount {
    color: $warning-color;
  }

  &.actual-amount {
    color: $success-color;
    font-size: 16px;
  }

  &.discount {
    color: $success-color;
  }
}

// 响应式
@media (max-width: 768px) {
  .balance-content {
    flex-direction: column;
    align-items: flex-start;
    gap: $spacing-md;
  }

  .balance-actions {
    width: 100%;

    .el-button {
      flex: 1;
    }
  }

  .recharge-amounts {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
