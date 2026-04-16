<template>
  <div class="order-list-page">
    <h2 class="page-title">我的订单</h2>

    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="待支付" name="0" />
      <el-tab-pane label="待服务" name="1" />
      <el-tab-pane label="服务中" name="2" />
      <el-tab-pane label="待评价" name="3" />
      <el-tab-pane label="已完成" name="4" />
      <el-tab-pane label="已取消" name="5" />
      <el-tab-pane label="退款中" name="6" />
      <el-tab-pane label="已退款" name="7" />
    </el-tabs>

    <!-- 订单列表 -->
    <div v-loading="loading" class="order-list">
      <template v-if="orders.length > 0">
        <el-card
          v-for="order in orders"
          :key="order.id"
          shadow="never"
          class="order-card"
        >
          <div class="order-header">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <el-tag :type="ORDER_STATUS_TYPES[order.status]" size="small">
              {{ ORDER_STATUS_LABELS[order.status] }}
            </el-tag>
          </div>

          <div class="order-body" @click="$router.push(`/order/${order.orderNo}`)">
            <div class="order-info">
              <h3 class="order-title">{{ order.serviceTitle || '服务订单' }}</h3>
              <span class="order-provider">服务者：{{ order.providerName || '-' }}</span>
              <span class="order-time">下单时间：{{ formatDate(order.createdAt) }}</span>
            </div>
            <div class="order-amount">
              {{ formatMoney(order.totalAmount) }}
            </div>
          </div>

          <div class="order-footer">
            <template v-if="order.status === 0">
              <el-button size="small" @click.stop="handleCancel(order.orderNo)">取消订单</el-button>
              <el-button type="primary" size="small" @click.stop="handlePay(order.orderNo)">
                立即支付
              </el-button>
            </template>
            <template v-else-if="order.status === 1">
              <el-button size="small" @click.stop="handleCancel(order.orderNo)">取消</el-button>
              <el-button type="danger" size="small" @click.stop="handleApplyRefund(order.orderNo)">退款</el-button>
            </template>
            <template v-else-if="order.status === 2">
              <el-button type="danger" size="small" @click.stop="handleApplyRefund(order.orderNo)">退款</el-button>
            </template>
            <template v-else-if="order.status === 3">
              <el-button type="primary" size="small" @click.stop="handleReview(order)">
                去评价
              </el-button>
            </template>
            <template v-else-if="order.status === 4">
              <el-button size="small" @click.stop="$router.push(`/order/${order.orderNo}`)">
                查看详情
              </el-button>
            </template>
            <template v-else>
              <el-button size="small" @click.stop="$router.push(`/order/${order.orderNo}`)">
                查看详情
              </el-button>
            </template>
          </div>
        </el-card>
      </template>
      <el-empty v-else description="暂无订单" />
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
        @size-change="fetchOrders"
        @current-change="fetchOrders"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, put } from '@/api/request'
import { applyRefund } from '@/api/order'
import type { Order } from '@/types/order'
import type { PageData } from '@/types/common'
import { ORDER_STATUS_LABELS, ORDER_STATUS_TYPES } from '@/utils/constants'
import { formatMoney, formatDate } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const orders = ref<Order[]>([])
const total = ref(0)
const activeTab = ref('all')

const pagination = reactive({
  page: 1,
  size: 10,
})

function handleTabChange() {
  pagination.page = 1
  fetchOrders()
}

async function fetchOrders() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: pagination.page,
      size: pagination.size,
    }
    if (activeTab.value !== 'all') {
      params.status = Number(activeTab.value)
    }

    const res = await get<PageData<Order>>('/orders/my', params)
    orders.value = res.data.records
    total.value = res.data.total
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

async function handleCancel(orderNo: string) {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await put(`/orders/${orderNo}/cancel`)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch {
    // cancelled or error
  }
}

async function handlePay(orderNo: string) {
  router.push(`/order/${orderNo}`)
}

async function handleApplyRefund(orderNo: string) {
  try {
    await ElMessageBox.confirm('确定要申请退款吗？提交后需等待平台审核。', '申请退款', {
      confirmButtonText: '确定申请',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await applyRefund(orderNo)
    ElMessage.success('退款申请已提交，请等待平台审核')
    fetchOrders()
  } catch {
    // cancelled or error
  }
}

function handleReview(order: Order) {
  router.push({ path: `/order/${order.orderNo}`, query: { action: 'review' } })
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.order-list-page {
  padding: $spacing-lg 0;
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
  margin-bottom: $spacing-lg;
}

// Tab
:deep(.el-tabs__item) {
  color: $text-secondary;
  transition: color $transition-normal, text-shadow $transition-normal;

  &.is-active {
    color: $neon-cyan;
    text-shadow: 0 0 10px rgba(0, 240, 255, 0.4);
  }

  &:hover {
    color: $neon-cyan;
    text-shadow: 0 0 6px rgba(0, 240, 255, 0.2);
  }
}

:deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, $neon-cyan, $primary-color);
  box-shadow: 0 0 10px rgba(0, 240, 255, 0.4);
}

:deep(.el-tabs__nav-wrap::after) {
  background-color: rgba(148, 163, 184, 0.06);
}

// 订单列表
.order-list {
  min-height: 300px;
  display: flex;
  flex-direction: column;
  gap: $spacing-md;

  :deep(.el-empty__image svg) {
    filter: drop-shadow(0 0 6px rgba(0, 240, 255, 0.2));
  }
}

.order-card {
  transition: transform $transition-normal, box-shadow $transition-normal, border-color $transition-normal;

  :deep(.el-card__body) {
    padding: $spacing-md;
  }

  :deep(.el-card) {
    background: $glass-bg;
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
    border: 1px solid $glass-border;
    border-radius: $border-radius-lg;
    transition: border-color $transition-normal, box-shadow $transition-normal;
  }

  &:hover {
    transform: translateY(-3px);

    :deep(.el-card) {
      border-color: $neon-cyan;
      box-shadow: 0 0 20px rgba(0, 240, 255, 0.15), 0 0 40px rgba(0, 240, 255, 0.05);
    }
  }
}

.order-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: $spacing-sm;
  border-bottom: 1px solid rgba(148, 163, 184, 0.06);
  margin-bottom: $spacing-sm;

  :deep(.el-tag) {
    backdrop-filter: blur(4px);
    transition: all $transition-normal;

    &.el-tag--primary {
      background: rgba(0, 240, 255, 0.1);
      color: $neon-cyan;
      border-color: rgba(0, 240, 255, 0.3);
      box-shadow: 0 0 8px rgba(0, 240, 255, 0.15);
    }

    &.el-tag--success {
      background: rgba(57, 255, 20, 0.1);
      color: $neon-green;
      border-color: rgba(57, 255, 20, 0.3);
      box-shadow: 0 0 8px rgba(57, 255, 20, 0.15);
    }

    &.el-tag--warning {
      background: rgba(255, 230, 0, 0.1);
      color: $neon-yellow;
      border-color: rgba(255, 230, 0, 0.3);
      box-shadow: 0 0 8px rgba(255, 230, 0, 0.15);
    }

    &.el-tag--danger {
      background: rgba(255, 45, 120, 0.1);
      color: $neon-pink;
      border-color: rgba(255, 45, 120, 0.3);
      box-shadow: 0 0 8px rgba(255, 45, 120, 0.15);
    }

    &.el-tag--info {
      background: rgba(148, 163, 184, 0.08);
      color: $text-secondary;
      border-color: rgba(148, 163, 184, 0.2);
    }
  }
}

.order-no {
  color: $text-muted;
  font-size: 13px;
  font-family: 'Courier New', monospace;
  letter-spacing: 0.5px;
}

.order-body {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  padding: $spacing-sm 0;
  border-radius: $border-radius;
  transition: background $transition-fast;

  &:hover {
    background: rgba(0, 240, 255, 0.03);
  }

  &:hover .order-title {
    color: $neon-cyan;
    text-shadow: 0 0 8px rgba(0, 240, 255, 0.3);
  }
}

.order-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.order-title {
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
  transition: color $transition-fast, text-shadow $transition-fast;
}

.order-provider,
.order-time {
  font-size: 13px;
  color: $text-muted;
}

.order-amount {
  font-size: 22px;
  font-weight: 700;
  color: $neon-cyan;
  white-space: nowrap;
  margin-left: $spacing-md;
  text-shadow: 0 0 16px rgba(0, 240, 255, 0.35);
}

.order-footer {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
  padding-top: $spacing-sm;
  border-top: 1px solid rgba(148, 163, 184, 0.06);

  :deep(.el-button--primary) {
    background: linear-gradient(135deg, $primary-color, $neon-purple);
    border: 1px solid rgba(0, 240, 255, 0.15);
    box-shadow: 0 2px 10px rgba(0, 240, 255, 0.12);
    transition: all $transition-normal;

    &:hover {
      background: linear-gradient(135deg, $neon-cyan, $primary-color);
      box-shadow: 0 4px 16px rgba(0, 240, 255, 0.3);
      border-color: $neon-cyan;
      transform: translateY(-1px);
    }
  }

  :deep(.el-button--default) {
    border: 1px solid rgba(148, 163, 184, 0.12);
    transition: all $transition-normal;

    &:hover {
      border-color: rgba(0, 240, 255, 0.3);
      color: $neon-cyan;
    }
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: $spacing-xl;
}

// 响应式
@media (max-width: 768px) {
  .order-body {
    flex-direction: column;
    align-items: flex-start;
    gap: $spacing-sm;
  }

  .order-amount {
    margin-left: 0;
  }
}
</style>
