<template>
  <div class="payment-record-page">
    <h2 class="page-title">支付记录</h2>

    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="充值" name="1" />
      <el-tab-pane label="消费" name="0" />
      <el-tab-pane label="提现" name="2" />
      <el-tab-pane label="收入" name="3" />
      <el-tab-pane label="退款" name="4" />
      <el-tab-pane label="罚金" name="6" />
    </el-tabs>

    <!-- 记录列表 -->
    <div v-loading="loading" class="record-list">
      <template v-if="records.length > 0">
        <el-card shadow="never" class="record-table-card">
          <el-table :data="records" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getTypeTagType(row.type)" size="small">
                  {{ getTypeLabel(row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="金额" width="140">
              <template #default="{ row }">
                <span :class="{ income: row.amount > 0, expense: row.amount < 0 }" class="amount-text">
                  {{ row.amount > 0 ? '+' : '' }}{{ formatMoney(row.amount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="paymentMethod" label="支付方式" width="120">
              <template #default="{ row }">
                {{ getPaymentMethodLabel(row.paymentMethod) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="transactionNo" label="交易号" min-width="180">
              <template #default="{ row }">
                <span class="transaction-no">{{ row.transactionNo || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="时间" width="170">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </template>
      <el-empty v-else description="暂无记录" />
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="total"
        :page-sizes="[10, 20, 30]"
        layout="total, sizes, prev, pager, next"
        background
        @size-change="fetchRecords"
        @current-change="fetchRecords"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { get } from '@/api/request'
import type { PaymentRecord } from '@/types/payment'
import type { PageData } from '@/types/common'
import { formatMoney, formatDate } from '@/utils/format'
import { PAYMENT_TYPE_LABELS } from '@/utils/constants'

const loading = ref(false)
const records = ref<PaymentRecord[]>([])
const total = ref(0)
const activeTab = ref('all')

const pagination = reactive({
  page: 1,
  size: 10,
})

function getTypeLabel(type: number): string {
  return PAYMENT_TYPE_LABELS[type] || '其他'
}

function getTypeTagType(type: number): string {
  const types: Record<number, string> = {
    0: 'danger',
    1: 'success',
    2: 'warning',
    3: 'success',
    4: 'info',
    5: 'info',
    6: 'danger',
  }
  return types[type] || 'info'
}

function getPaymentMethodLabel(method: string | null): string {
  const labels: Record<string, string> = {
    balance: '余额',
    wechat: '微信',
    alipay: '支付宝',
    system: '系统',
  }
  return method ? (labels[method] || method) : '-'
}

function getStatusLabel(status: number): string {
  const labels: Record<number, string> = {
    0: '待处理',
    1: '成功',
    2: '失败',
  }
  return labels[status] || '未知'
}

function getStatusType(status: number): string {
  const types: Record<number, string> = {
    0: 'warning',
    1: 'success',
    2: 'danger',
  }
  return types[status] || 'info'
}

function handleTabChange() {
  pagination.page = 1
  fetchRecords()
}

async function fetchRecords() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: pagination.page,
      size: pagination.size,
    }
    if (activeTab.value !== 'all') {
      params.type = Number(activeTab.value)
    }

    const res = await get<PageData<PaymentRecord>>('/payment-records', params)
    records.value = res.data.records
    total.value = res.data.total
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.payment-record-page {
  padding: $spacing-lg 0;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: $spacing-lg;
}

// Tab
:deep(.el-tabs__item) {
  color: $text-secondary;

  &.is-active {
    color: $primary-light;
  }
}

:deep(.el-tabs__active-bar) {
  background-color: $primary-color;
}

// 记录列表
.record-list {
  min-height: 300px;
}

.record-table-card {
  overflow: hidden;
}

.amount-text {
  font-weight: 600;

  &.income {
    color: $success-color;
  }

  &.expense {
    color: $danger-color;
  }
}

.transaction-no {
  color: $text-muted;
  font-size: 13px;
  font-family: monospace;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: $spacing-xl;
}

// 响应式
@media (max-width: 768px) {
  .record-table-card {
    :deep(.el-table) {
      font-size: 13px;
    }

    :deep(.el-table__body-wrapper) {
      overflow-x: auto;
    }
  }
}
</style>
