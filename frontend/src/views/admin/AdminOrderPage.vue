<template>
  <div class="admin-order-page">
    <div class="page-header">
      <h2 class="page-title">订单管理</h2>
    </div>

    <!-- 搜索筛选 -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <el-input v-model="searchKeyword" placeholder="搜索订单号" clearable style="width: 250px" @clear="fetchOrders" @keyup.enter="fetchOrders">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="filterStatus" placeholder="订单状态" clearable style="width: 150px" @change="fetchOrders">
          <el-option v-for="(label, key) in ORDER_STATUS_LABELS" :key="key" :label="label" :value="Number(key)" />
        </el-select>
        <el-button type="primary" @click="fetchOrders">搜索</el-button>
      </div>
    </el-card>

    <!-- 订单表格 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="orders" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="serviceTitle" label="服务标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="userName" label="用户" width="100" />
        <el-table-column prop="providerName" label="服务者" width="100" />
        <el-table-column prop="totalAmount" label="金额" width="100">
          <template #default="{ row }">
            <span style="color: #f59e0b; font-weight: 600">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <OrderStatusBadge :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleView(row)">详情</el-button>
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
          @size-change="fetchOrders"
          @current-change="fetchOrders"
        />
      </div>
    </el-card>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <template v-if="currentOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态"><OrderStatusBadge :status="currentOrder.status" /></el-descriptions-item>
          <el-descriptions-item label="服务标题">{{ currentOrder.serviceTitle }}</el-descriptions-item>
          <el-descriptions-item label="金额">¥{{ currentOrder.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="用户">{{ currentOrder.userName }}</el-descriptions-item>
          <el-descriptions-item label="服务者">{{ currentOrder.providerName }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatDate(currentOrder.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ formatDate(currentOrder.paymentTime) }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatDate(currentOrder.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ formatDate(currentOrder.endTime) }}</el-descriptions-item>
          <el-descriptions-item label="用户备注" :span="2">{{ currentOrder.requirements || '无' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getAdminOrders, getAdminOrder } from '@/api/admin'
import type { Order } from '@/types/order'
import { ORDER_STATUS_LABELS } from '@/utils/constants'
import { formatDate } from '@/utils/format'
import OrderStatusBadge from '@/components/business/OrderStatusBadge.vue'

const loading = ref(false)
const orders = ref<Order[]>([])
const total = ref(0)
const searchKeyword = ref('')
const filterStatus = ref<number | undefined>()
const pagination = reactive({ page: 1, size: 10 })
const detailVisible = ref(false)
const currentOrder = ref<Order | null>(null)

async function fetchOrders() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: pagination.page, size: pagination.size }
    if (searchKeyword.value) params.orderNo = searchKeyword.value
    if (filterStatus.value !== undefined) params.status = filterStatus.value
    const res = await getAdminOrders(params)
    orders.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

async function handleView(order: Order) {
  try {
    const res = await getAdminOrder(order.orderNo)
    currentOrder.value = res.data
    detailVisible.value = true
  } catch {
    currentOrder.value = order
    detailVisible.value = true
  }
}

onMounted(() => fetchOrders())
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.admin-order-page {
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
  --el-table-bg-color: #{$bg-dark} !important;
  --el-table-header-bg-color: transparent !important;
  --el-table-row-hover-bg-color: rgba(0, 240, 255, 0.04) !important;
  --el-table-border-color: rgba(0, 240, 255, 0.06) !important;
  --el-table-text-color: #{$text-primary} !important;
  --el-table-header-text-color: #{$neon-cyan} !important;
  background-color: #{$bg-dark} !important;
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

// 对话框霓虹边框
:deep(.el-dialog) {
  --el-dialog-bg-color: #{$bg-card} !important;
  --el-dialog-border-color: rgba($neon-cyan, 0.15) !important;
  --el-dialog-title-color: #{$text-primary} !important;
  background-color: $glass-bg !important;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid rgba($neon-cyan, 0.15) !important;
  border-radius: $border-radius-xl !important;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.5), 0 0 30px rgba($neon-cyan, 0.08);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba($neon-cyan, 0.5), rgba($primary-color, 0.3), transparent);
    pointer-events: none;
  }
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid rgba($neon-cyan, 0.08) !important;
  background-color: transparent !important;
  padding: $spacing-lg $spacing-xl;
  margin-right: 0;

  .el-dialog__title {
    font-family: 'Orbitron', 'Rajdhani', sans-serif;
    font-weight: 600;
    letter-spacing: 0.5px;
    background: linear-gradient(135deg, $neon-cyan, $primary-light);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

:deep(.el-dialog__body) {
  background-color: transparent !important;
  color: $text-primary !important;
  padding: $spacing-lg $spacing-xl;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid rgba($neon-cyan, 0.08) !important;
  background-color: transparent !important;
  padding: $spacing-md $spacing-xl;
}

// 描述列表赛博风格
:deep(.el-descriptions) {
  --el-descriptions-table-border: rgba($neon-cyan, 0.06) !important;
  --el-descriptions-item-bordered-label-background: rgba($bg-elevated, 0.5) !important;
  --el-descriptions-item-bordered-content-background: transparent !important;
  --el-descriptions-item-content-color: #{$text-primary} !important;
  --el-descriptions-item-label-color: #{$neon-cyan} !important;
}

:deep(.el-descriptions__header) {
  color: $text-primary !important;
}

:deep(.el-descriptions__label) {
  color: $text-secondary !important;
  background-color: rgba($bg-elevated, 0.5) !important;
}

:deep(.el-descriptions__content) {
  color: $text-primary !important;
  background-color: transparent !important;
}

:deep(.el-descriptions__cell) {
  border-color: rgba($neon-cyan, 0.06) !important;
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
</style>
