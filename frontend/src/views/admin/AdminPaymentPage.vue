<template>
  <div class="admin-payment-page">
    <div class="page-header">
      <h2 class="page-title">财务管理</h2>
    </div>

    <!-- 统计卡片 -->
    <section class="stats-section">
      <div class="stats-grid">
        <StatCard title="总消费" :value="paymentStats.totalRevenue" icon="Money" color="#22c55e" prefix="¥" />
        <StatCard title="总提现" :value="paymentStats.totalWithdraw" icon="Wallet" color="#ef4444" prefix="¥" />
        <StatCard title="平台净收入" :value="paymentStats.netIncome" icon="DataLine" color="#6366f1" prefix="¥" />
        <StatCard title="充值总额" :value="paymentStats.totalRecharge" icon="CreditCard" color="#f59e0b" prefix="¥" />
      </div>
    </section>

    <!-- 搜索筛选 -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <el-input
          v-model="filterKeyword"
          placeholder="搜索用户ID或订单ID"
          clearable
          style="width: 200px"
          @keyup.enter="fetchPayments"
        />
        <el-select v-model="filterType" placeholder="交易类型" clearable style="width: 150px" @change="fetchPayments">
          <el-option label="消费" :value="0" />
          <el-option label="充值" :value="1" />
          <el-option label="提现" :value="2" />
          <el-option label="收入" :value="3" />
          <el-option label="退款" :value="4" />
          <el-option label="平台手续费" :value="5" />
          <el-option label="罚金" :value="6" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 120px" @change="fetchPayments">
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
          <el-option label="处理中" :value="2" />
        </el-select>
        <el-button type="primary" @click="fetchPayments">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="resetFilters">
          <el-icon><RefreshLeft /></el-icon>
          重置
        </el-button>
      </div>
    </el-card>

    <!-- 交易记录表格 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="payments" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="transactionNo" label="交易号" width="200" show-overflow-tooltip />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="orderId" label="订单ID" width="80">
          <template #default="{ row }">{{ row.orderId || '-' }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="110">
          <template #default="{ row }">
            <span :style="{ color: getAmountColor(row.type, row.amount), fontWeight: 600 }">
              {{ getAmountPrefix(row.type, row.amount) }}¥{{ Math.abs(row.amount)?.toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="feeAmount" label="手续费" width="90">
          <template #default="{ row }">
            <span v-if="row.feeAmount" style="color: #f97316; font-weight: 500">¥{{ row.feeAmount?.toFixed(2) }}</span>
            <span v-else style="color: #64748b">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="actualAmount" label="实际到账" width="100">
          <template #default="{ row }">
            <span v-if="row.actualAmount" style="color: #10b981; font-weight: 600">¥{{ row.actualAmount?.toFixed(2) }}</span>
            <span v-else style="color: #64748b">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="feeRate" label="费率" width="70">
          <template #default="{ row }">
            <span v-if="row.feeRate">{{ row.feeRate }}%</span>
            <span v-else style="color: #64748b">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="110">
          <template #default="{ row }">
            <el-tag :type="paymentTypeTag(row.type)" size="small">{{ paymentTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100">
          <template #default="{ row }">{{ getPaymentMethodLabel(row.paymentMethod) || '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : row.status === 2 ? '处理中' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="fetchPayments"
          @current-change="fetchPayments"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getAdminPayments, getAdminPaymentStats } from '@/api/admin'
import type { PaymentRecord } from '@/types/payment'
import { formatDate } from '@/utils/format'
import StatCard from '@/components/admin/StatCard.vue'
import { Search, RefreshLeft } from '@element-plus/icons-vue'

const loading = ref(false)
const payments = ref<PaymentRecord[]>([])
const total = ref(0)
const filterKeyword = ref('')
const filterType = ref<number | undefined>()
const filterStatus = ref<number | undefined>()
const pagination = reactive({ page: 1, size: 10 })

const paymentStats = reactive({
  totalRevenue: 0,
  totalRecharge: 0,
  totalWithdraw: 0,
  netIncome: 0,
})

function paymentTypeLabel(type: number) {
  const map: Record<number, string> = { 
    0: '消费', 
    1: '充值', 
    2: '提现', 
    3: '收入', 
    4: '退款', 
    5: '平台手续费',
    6: '罚金'
  }
  return map[type] || '未知'
}

function paymentTypeTag(type: number) {
  const map: Record<number, string> = { 
    0: 'success', 
    1: 'primary', 
    2: 'danger', 
    3: 'success', 
    4: 'danger', 
    5: 'info',
    6: 'danger'
  }
  return map[type] || 'info'
}

function getAmountColor(type: number, amount: number) {
  if (type === 0 || type === 2 || type === 4) return '#ef4444' // 消费、提现、退款 - 红色（用户支出/平台支出）
  if (type === 1 || type === 3 || type === 5 || type === 6) return '#22c55e' // 充值、收入、平台手续费、罚金 - 绿色（平台收入）
  return amount >= 0 ? '#22c55e' : '#ef4444'
}

function getAmountPrefix(type: number, amount: number) {
  if (type === 0 || type === 2 || type === 4) return '' // 消费、提现、退款 - 不加前缀（显示为负数的绝对值）
  if (type === 1 || type === 3 || type === 5 || type === 6) return '+' // 充值、收入、平台手续费、罚金 - 加+（平台收入）
  return amount >= 0 ? '+' : ''
}

function getPaymentMethodLabel(method: string | null | undefined): string {
  if (!method) return '-'
  const map: Record<string, string> = {
    'alipay': '支付宝',
    'wechat': '微信',
    'balance': '余额',
    'bank': '银行卡',
    'system': '系统'
  }
  return map[method] || method
}

function resetFilters() {
  filterKeyword.value = ''
  filterType.value = undefined
  filterStatus.value = undefined
  pagination.page = 1
  fetchPayments()
}

async function fetchPayments() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: pagination.page, size: pagination.size }
    if (filterType.value !== undefined) params.type = filterType.value
    if (filterStatus.value !== undefined) params.status = filterStatus.value
    if (filterKeyword.value) {
      params.keyword = filterKeyword.value
    }
    const res = await getAdminPayments(params)
    let data = res.data?.records || []
    // 确保按新到旧排序（创建时间降序）
    data.sort((a: any, b: any) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    payments.value = data
    total.value = res.data?.total || 0
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

async function fetchStats() {
  try {
    const res = await getAdminPaymentStats()
    if (res.data) Object.assign(paymentStats, res.data)
  } catch {
    // ignore
  }
}

onMounted(() => {
  fetchPayments()
  fetchStats()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.admin-payment-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  font-family: 'Orbitron', 'Rajdhani', sans-serif;
  background: linear-gradient(135deg, $neon-cyan 0%, $primary-light 50%, $neon-purple 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 2px;
  text-transform: uppercase;
  position: relative;

  &::after {
    content: '';
    display: block;
    width: 120px;
    height: 2px;
    margin-top: $spacing-sm;
    background: linear-gradient(90deg, $neon-cyan, $primary-color, transparent);
    border-radius: 1px;
    box-shadow: 0 0 10px rgba($neon-cyan, 0.4);
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-md;
}

.filter-card {
  background: $glass-bg !important;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid $border-color !important;
  border-radius: $border-radius-lg !important;
  transition: all $transition-normal;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba($neon-cyan, 0.3), transparent);
    pointer-events: none;
  }

  &:hover {
    border-color: rgba($neon-cyan, 0.2) !important;
    box-shadow: $shadow-glow, 0 0 15px rgba($neon-cyan, 0.06);
  }

  :deep(.el-card__body) { padding: $spacing-md; }
}

.filter-row {
  display: flex;
  gap: $spacing-sm;
  align-items: center;
  flex-wrap: wrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: $spacing-md;
  padding-bottom: $spacing-sm;
}

// 卡片毛玻璃效果 + 霓虹边框
:deep(.el-card) {
  background: $glass-bg !important;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid $border-color !important;
  border-radius: $border-radius-lg !important;
  transition: all $transition-normal;

  &:hover {
    border-color: rgba($neon-cyan, 0.2) !important;
    box-shadow: $shadow-glow, 0 0 15px rgba($neon-cyan, 0.06);
  }
}

// 表格赛博朋克风格
:deep(.el-table) {
  --el-table-bg-color: #{$bg-dark};
  --el-table-header-bg-color: transparent;
  --el-table-row-hover-bg-color: rgba(0, 240, 255, 0.04);
  --el-table-border-color: rgba(0, 240, 255, 0.06);
  --el-table-text-color: #{$text-primary};
  --el-table-header-text-color: #{$neon-cyan};
  border-radius: $border-radius-lg;
  overflow: hidden;

  .el-table__row--striped {
    background-color: rgba($bg-elevated, 0.35) !important;
  }
}

:deep(.el-table__row) {
  background-color: $bg-dark !important;
  transition: all $transition-fast;

  &:hover {
    background-color: rgba(0, 240, 255, 0.04) !important;
    box-shadow: inset 0 0 30px rgba(0, 240, 255, 0.03);
  }
}

// 表头渐变背景 + 霓虹文字
:deep(.el-table__header-wrapper) {
  th {
    background: linear-gradient(135deg, rgba($neon-cyan, 0.06), rgba($primary-color, 0.04)) !important;
    color: $neon-cyan !important;
    font-weight: 600;
    font-size: 13px;
    text-transform: uppercase;
    letter-spacing: 1px;
    border-bottom: 1px solid rgba($neon-cyan, 0.12) !important;
  }
}

// 按钮霓虹发光效果
:deep(.el-button--primary) {
  background: linear-gradient(135deg, $neon-cyan, $primary-color) !important;
  border: none !important;
  color: #06080f !important;
  font-weight: 600;
  box-shadow: 0 2px 12px rgba($neon-cyan, 0.3);
  transition: all $transition-normal;

  &:hover {
    background: linear-gradient(135deg, lighten($neon-cyan, 5%), lighten($primary-color, 5%)) !important;
    box-shadow: 0 4px 20px rgba($neon-cyan, 0.5), 0 0 30px rgba($neon-cyan, 0.2);
    transform: translateY(-1px);
  }
}

// 默认按钮霓虹hover
:deep(.el-button--default) {
  background: rgba($bg-elevated, 0.6) !important;
  border: 1px solid $border-color !important;
  color: $text-secondary !important;
  transition: all $transition-fast;

  &:hover {
    border-color: rgba($neon-cyan, 0.3) !important;
    color: $neon-cyan !important;
    box-shadow: 0 0 10px rgba($neon-cyan, 0.1);
  }
}

// 状态标签霓虹风格
:deep(.el-tag) {
  border: 1px solid currentColor;
  backdrop-filter: blur(4px);
}

:deep(.el-tag--success) {
  background: rgba($neon-green, 0.1) !important;
  color: $neon-green !important;
  border-color: rgba($neon-green, 0.3) !important;
  box-shadow: 0 0 8px rgba($neon-green, 0.15);
}

:deep(.el-tag--danger) {
  background: rgba($neon-pink, 0.1) !important;
  color: $neon-pink !important;
  border-color: rgba($neon-pink, 0.3) !important;
  box-shadow: 0 0 8px rgba($neon-pink, 0.15);
}

:deep(.el-tag--warning) {
  background: rgba($neon-yellow, 0.1) !important;
  color: $neon-yellow !important;
  border-color: rgba($neon-yellow, 0.3) !important;
  box-shadow: 0 0 8px rgba($neon-yellow, 0.15);
}

:deep(.el-tag--info) {
  background: rgba($neon-cyan, 0.1) !important;
  color: $neon-cyan !important;
  border-color: rgba($neon-cyan, 0.3) !important;
  box-shadow: 0 0 8px rgba($neon-cyan, 0.15);
}

:deep(.el-tag--primary) {
  background: rgba($primary-color, 0.1) !important;
  color: $primary-light !important;
  border-color: rgba($primary-color, 0.3) !important;
  box-shadow: 0 0 8px rgba($primary-color, 0.15);
}

// 搜索框focus时cyan发光
:deep(.el-input__wrapper) {
  background: $bg-input !important;
  border: 1px solid $border-color !important;
  box-shadow: none !important;
  transition: border-color $transition-normal, box-shadow $transition-normal;

  &:focus-within,
  &.is-focus {
    border-color: rgba($neon-cyan, 0.4) !important;
    box-shadow: 0 0 12px rgba($neon-cyan, 0.15) !important;
  }
}

// 分页器赛博风格
:deep(.el-pagination) {
  .el-pager li {
    background: rgba($bg-elevated, 0.5) !important;
    border: 1px solid $border-color !important;
    color: $text-secondary !important;
    border-radius: $border-radius;
    transition: all $transition-fast;

    &:hover {
      color: $neon-cyan !important;
      border-color: rgba($neon-cyan, 0.3) !important;
      box-shadow: 0 0 8px rgba($neon-cyan, 0.1);
    }

    &.is-active {
      background: linear-gradient(135deg, $neon-cyan, $primary-color) !important;
      border-color: transparent !important;
      color: #06080f !important;
      font-weight: 700;
      box-shadow: 0 2px 12px rgba($neon-cyan, 0.4);
    }
  }

  .btn-prev,
  .btn-next {
    background: rgba($bg-elevated, 0.5) !important;
    border: 1px solid $border-color !important;
    color: $text-secondary !important;
    border-radius: $border-radius;
    transition: all $transition-fast;

    &:hover {
      border-color: rgba($neon-cyan, 0.3) !important;
      color: $neon-cyan !important;
    }
  }

  .el-pagination__total {
    color: $text-secondary !important;
  }
}

@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
