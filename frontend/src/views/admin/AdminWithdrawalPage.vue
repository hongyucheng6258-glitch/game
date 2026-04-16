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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" size="small" @click="handleAudit(row, 1)">
                <el-icon><Check /></el-icon> 通过
              </el-button>
              <el-button type="danger" size="small" @click="handleAudit(row, 2)">
                <el-icon><Close /></el-icon> 拒绝
              </el-button>
            </template>
            <span v-else style="color: #64748b; font-size: 13px">已处理</span>
          </template>
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
import { getAdminWithdrawals, auditWithdrawal } from '@/api/admin'
import type { WithdrawalApplication } from '@/types/payment'
import { formatDate } from '@/utils/format'
import { Refresh, Check, Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

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

async function handleAudit(row: WithdrawalApplication, status: number) {
  const action = status === 1 ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(
      `确认${action}该提现申请？金额：¥${row.amount?.toFixed(2)}`,
      `${action}提现`,
      { confirmButtonText: `确认${action}`, cancelButtonText: '取消', type: 'warning' }
    )
    await auditWithdrawal(row.id, { status, remark: status === 1 ? '审核通过' : '审核拒绝' })
    ElMessage.success(`提现申请已${action}`)
    fetchWithdrawals()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e?.message || `${action}失败`)
    }
  }
}
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
</style>
