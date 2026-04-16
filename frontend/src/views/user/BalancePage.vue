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
  { label: '微信', value: 'wechat' }
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
    balance.value = res.data
  } catch (error) {
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
    await post('/payments/recharge', {
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
  font-size: 28px;
  font-weight: 700;
  font-family: 'Orbitron', sans-serif;
  background: linear-gradient(135deg, $neon-cyan, $primary-light, $neon-purple);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: none;
  filter: drop-shadow(0 0 12px rgba(0, 240, 255, 0.25));
  position: relative;
  padding-left: 16px;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 4px;
    height: 28px;
    border-radius: 2px;
    background: linear-gradient(180deg, $neon-cyan, $neon-purple);
    box-shadow: 0 0 10px rgba(0, 240, 255, 0.4);
  }
}

// 余额卡片
.balance-card {
  background: $glass-bg;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid $glass-border;
  border-radius: $border-radius-xl;
  box-shadow: $shadow-glow, $shadow-md;
  transition: transform $transition-normal, box-shadow $transition-normal, border-color $transition-normal;

  &:hover {
    transform: translateY(-2px);
    border-color: $neon-cyan;
    box-shadow: 0 0 20px rgba(0, 240, 255, 0.2), 0 0 40px rgba(0, 240, 255, 0.06), $shadow-lg;
  }

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
  letter-spacing: 1px;
  text-transform: uppercase;
}

.balance-amount {
  font-size: 42px;
  font-weight: 700;
  font-family: 'Orbitron', sans-serif;
  color: $neon-cyan;
  text-shadow: 0 0 20px rgba(0, 240, 255, 0.5), 0 0 40px rgba(0, 240, 255, 0.2);
  letter-spacing: 2px;
}

.balance-actions {
  display: flex;
  gap: $spacing-sm;

  :deep(.el-button--primary) {
    background: linear-gradient(135deg, $primary-color, $neon-purple);
    border: 1px solid rgba(0, 240, 255, 0.2);
    box-shadow: 0 4px 12px rgba(0, 240, 255, 0.15);
    transition: all $transition-normal;

    &:hover {
      background: linear-gradient(135deg, $neon-cyan, $primary-color);
      box-shadow: 0 6px 24px rgba(0, 240, 255, 0.35), 0 0 40px rgba(0, 240, 255, 0.1);
      border-color: $neon-cyan;
      transform: translateY(-2px);
    }
  }

  :deep(.el-button--default) {
    background: $glass-bg;
    backdrop-filter: blur($glass-blur);
    border: 1px solid rgba(148, 163, 184, 0.06);
    color: $text-primary;
    transition: all $transition-normal;

    &:hover {
      background: $bg-hover;
      border-color: rgba(0, 240, 255, 0.3);
      color: $neon-cyan;
      transform: translateY(-1px);
      box-shadow: 0 0 12px rgba(0, 240, 255, 0.1);
    }
  }
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
  border: 1px solid rgba(148, 163, 184, 0.06);
  border-radius: $border-radius;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
  background: $glass-bg;
  backdrop-filter: blur($glass-blur);
  transition: all $transition-normal;

  &:hover {
    border-color: rgba(0, 240, 255, 0.3);
    color: $neon-cyan;
    background: rgba(0, 240, 255, 0.06);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 240, 255, 0.12);
  }

  &.active {
    border-color: $neon-cyan;
    color: $neon-cyan;
    background: rgba(0, 240, 255, 0.1);
    box-shadow: 0 0 16px rgba(0, 240, 255, 0.2), inset 0 0 12px rgba(0, 240, 255, 0.05);
    text-shadow: 0 0 8px rgba(0, 240, 255, 0.3);
  }
}

// 交易记录
.records-section {
  :deep(.el-card) {
    background: $glass-bg;
    backdrop-filter: blur($glass-blur);
    -webkit-backdrop-filter: blur($glass-blur);
    border: 1px solid $glass-border;
    border-radius: $border-radius-xl;
    box-shadow: $shadow-md;
    transition: border-color $transition-normal, box-shadow $transition-normal;

    &:hover {
      border-color: rgba(0, 240, 255, 0.15);
      box-shadow: 0 0 16px rgba(0, 240, 255, 0.08), $shadow-md;
    }
  }
}

.card-header-title {
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
  letter-spacing: 0.5px;
}

.card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.record-list {
  min-height: 100px;

  :deep(.el-empty__image svg) {
    filter: drop-shadow(0 0 6px rgba(0, 240, 255, 0.2));
  }
}

.record-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md $spacing-sm;
  border-bottom: 1px solid rgba(148, 163, 184, 0.06);
  border-radius: $border-radius;
  transition: all $transition-normal;

  &:hover {
    background: rgba(0, 240, 255, 0.03);
    box-shadow: 0 0 8px rgba(0, 240, 255, 0.04);
  }

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
  font-weight: 700;

  &.income {
    color: $neon-green;
    text-shadow: 0 0 10px rgba(57, 255, 20, 0.35);
  }

  &.expense {
    color: $neon-pink;
    text-shadow: 0 0 10px rgba(255, 45, 120, 0.35);
  }
}

// 手续费预览
.fee-preview {
  background: rgba(0, 240, 255, 0.04);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(0, 240, 255, 0.1);
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
    border-top: 1px dashed rgba(0, 240, 255, 0.15);
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
    color: $neon-yellow;
    text-shadow: 0 0 8px rgba(255, 230, 0, 0.25);
  }

  &.actual-amount {
    color: $neon-green;
    font-size: 16px;
    font-weight: 700;
    text-shadow: 0 0 10px rgba(57, 255, 20, 0.3);
  }

  &.discount {
    color: $neon-green;
    text-shadow: 0 0 6px rgba(57, 255, 20, 0.2);
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
