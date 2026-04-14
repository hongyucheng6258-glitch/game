<template>
  <div class="admin-withdrawal-page">
    <div class="page-header">
      <h2 class="page-title">提现记录</h2>
    </div>

    <!-- 搜索筛选 -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <el-select v-model="filterStatus" placeholder="状态筛选" clearable style="width: 150px" @change="fetchWithdrawals">
          <el-option label="处理中" :value="0" />
          <el-option label="已批准" :value="1" />
          <el-option label="已拒绝" :value="2" />
          <el-option label="已完成" :value="3" />
        </el-select>
        <el-button type="primary" @click="fetchWithdrawals">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </el-card>

    <!-- 提现表格 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="withdrawals" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="amount" label="提现金额" width="120">
          <template #default="{ row }">
            <span style="color: #f59e0b; font-weight: 600">¥{{ row.amount?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="feeAmount" label="手续费" width="100">
          <template #default="{ row }">
            <span v-if="row.feeAmount" style="color: #f97316; font-weight: 500">¥{{ row.feeAmount?.toFixed(2) }}</span>
            <span v-else style="color: #64748b">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="actualAmount" label="实际到账" width="110">
          <template #default="{ row }">
            <span v-if="row.actualAmount" style="color: #10b981; font-weight: 600">¥{{ row.actualAmount?.toFixed(2) }}</span>
            <span v-else style="color: #64748b">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="feeRate" label="费率" width="80">
          <template #default="{ row }">
            <span v-if="row.feeRate">{{ row.feeRate }}%</span>
            <span v-else style="color: #64748b">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="bankName" label="银行" width="120" />
        <el-table-column prop="bankAccount" label="银行卡号" width="180" show-overflow-tooltip />
        <el-table-column prop="accountName" label="账户名" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="withdrawalStatusType(row.status)" size="small">
              {{ withdrawalStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="170">
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
          @size-change="fetchWithdrawals"
          @current-change="fetchWithdrawals"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getAdminWithdrawals } from '@/api/admin'
import type { WithdrawalApplication } from '@/types/payment'
import { formatDate } from '@/utils/format'
import { Refresh } from '@element-plus/icons-vue'

const loading = ref(false)
const withdrawals = ref<WithdrawalApplication[]>([])
const total = ref(0)
const filterStatus = ref<number | undefined>()
const pagination = reactive({ page: 1, size: 10 })

function withdrawalStatusLabel(status: number) {
  const map: Record<number, string> = { 0: '处理中', 1: '已批准', 2: '已拒绝', 3: '已完成' }
  return map[status] || '未知'
}

function withdrawalStatusType(status: number) {
  const map: Record<number, string> = { 0: 'warning', 1: 'primary', 2: 'danger', 3: 'success' }
  return map[status] || 'info'
}

async function fetchWithdrawals() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: pagination.page, size: pagination.size }
    if (filterStatus.value !== undefined) params.status = filterStatus.value
    const res = await getAdminWithdrawals(params)
    let data = res.data?.records || []
    data.sort((a: any, b: any) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    withdrawals.value = data
    total.value = res.data?.total || 0
  } catch {
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchWithdrawals())
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.admin-withdrawal-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, $text-primary 0%, $primary-light 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 0.5px;
}

.filter-card {
  background: $glass-bg !important;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid rgba(148, 163, 184, 0.06) !important;
  border-radius: $border-radius-lg !important;
  transition: all $transition-normal;

  &:hover {
    border-color: rgba($primary-color, 0.2) !important;
    box-shadow: $shadow-glow;
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

// 卡片毛玻璃效果
:deep(.el-card) {
  background: $glass-bg !important;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid rgba(148, 163, 184, 0.06) !important;
  border-radius: $border-radius-lg !important;
  transition: all $transition-normal;

  &:hover {
    border-color: rgba($primary-color, 0.2) !important;
    box-shadow: $shadow-glow;
  }
}

// 表格暗色电竞风格
:deep(.el-table) {
  --el-table-bg-color: #0f172a;
  --el-table-header-bg-color: #1e293b;
  --el-table-row-hover-bg-color: rgba($primary-color, 0.08);
  --el-table-border-color: rgba(148, 163, 184, 0.06);
  --el-table-text-color: #f1f5f9;
  --el-table-header-text-color: #94a3b8;
  border-radius: $border-radius-lg;
  overflow: hidden;

  .el-table__row--striped {
    background-color: rgba(30, 41, 59, 0.5) !important;
  }
}

:deep(.el-table__row) {
  background-color: #0f172a !important;
  transition: all $transition-fast;

  &:hover {
    background-color: rgba($primary-color, 0.08) !important;
  }
}

:deep(.el-table__header-wrapper) {
  th {
    background-color: #1e293b !important;
    font-weight: 600;
    font-size: 13px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }
}

// 按钮渐变效果
:deep(.el-button--primary) {
  background: linear-gradient(135deg, $primary-color, $primary-dark) !important;
  border: none !important;
  box-shadow: 0 2px 8px rgba($primary-color, 0.3);

  &:hover {
    background: linear-gradient(135deg, $primary-light, $primary-color) !important;
    box-shadow: 0 4px 16px rgba($primary-color, 0.4);
    transform: translateY(-1px);
  }
}

// 分页器美化
:deep(.el-pagination) {
  .el-pager li {
    background: rgba(30, 41, 59, 0.6) !important;
    border: 1px solid rgba(148, 163, 184, 0.06) !important;
    color: $text-secondary !important;
    border-radius: $border-radius;
    transition: all $transition-fast;

    &:hover {
      color: $primary-light !important;
      border-color: rgba($primary-color, 0.3) !important;
    }

    &.is-active {
      background: linear-gradient(135deg, $primary-color, $primary-dark) !important;
      border-color: transparent !important;
      color: #fff !important;
      box-shadow: 0 2px 8px rgba($primary-color, 0.4);
    }
  }

  .btn-prev,
  .btn-next {
    background: rgba(30, 41, 59, 0.6) !important;
    border: 1px solid rgba(148, 163, 184, 0.06) !important;
    color: $text-secondary !important;
    border-radius: $border-radius;
  }
}
</style>
