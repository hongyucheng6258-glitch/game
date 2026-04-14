<template>
  <div class="provider-order-page">
    <h2 class="page-title">接单管理</h2>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="待服务" name="1" />
      <el-tab-pane label="服务中" name="2" />
      <el-tab-pane label="待评价" name="3" />
      <el-tab-pane label="已完成" name="4" />
      <el-tab-pane label="已取消" name="5" />
    </el-tabs>

    <div v-loading="loading" class="order-list">
      <template v-if="orders.length > 0">
        <el-card v-for="order in orders" :key="order.id" shadow="never" class="order-card">
          <div class="order-header">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <OrderStatusBadge :status="order.status" />
          </div>
          <div class="order-body" @click="$router.push(`/order/${order.orderNo}`)">
            <div class="order-info">
              <h3 class="order-title">{{ order.serviceTitle || '服务订单' }}</h3>
              <span class="order-user">下单用户：{{ order.userName || '-' }}</span>
              <span class="order-time">下单时间：{{ formatDate(order.createdAt) }}</span>
              <span v-if="order.requirements" class="order-req">
                用户备注：{{ order.requirements }}
              </span>
            </div>
            <div class="order-amount">{{ formatMoney(order.totalAmount) }}</div>
          </div>
          <div class="order-footer">
            <template v-if="order.status === 1">
              <el-button type="primary" size="small" @click.stop="handleStart(order.orderNo)">
                开始服务
              </el-button>
            </template>
            <template v-else-if="order.status === 2">
              <el-button type="success" size="small" @click.stop="handleComplete(order.orderNo)">
                完成服务
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, put } from '@/api/request'
import type { Order } from '@/types/order'
import type { PageData } from '@/types/common'
import { formatMoney, formatDate } from '@/utils/format'
import OrderStatusBadge from '@/components/business/OrderStatusBadge.vue'

const loading = ref(false)
const orders = ref<Order[]>([])
const total = ref(0)
const activeTab = ref('all')
const pagination = reactive({ page: 1, size: 10 })

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
    const res = await get<PageData<Order>>('/orders/received', params)
    orders.value = res.data.records
    total.value = res.data.total
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

async function handleStart(orderNo: string) {
  try {
    await ElMessageBox.confirm('确定要开始服务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    })
    await put(`/orders/${orderNo}/start`)
    ElMessage.success('服务已开始')
    fetchOrders()
  } catch {
    // cancelled or error
  }
}

async function handleComplete(orderNo: string) {
  try {
    await ElMessageBox.confirm('确定要完成服务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    })
    await put(`/orders/${orderNo}/complete`)
    ElMessage.success('服务已完成')
    fetchOrders()
  } catch {
    // cancelled or error
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.provider-order-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: $text-primary;
  margin: 0;
}

:deep(.el-tabs__item) {
  color: $text-secondary;
  &.is-active {
    color: $primary-light;
  }
}

:deep(.el-tabs__active-bar) {
  background-color: $primary-color;
}

.order-list {
  min-height: 300px;
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.order-card {
  :deep(.el-card__body) {
    padding: $spacing-md;
  }
}

.order-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: $spacing-sm;
  border-bottom: 1px solid $border-color;
  margin-bottom: $spacing-sm;
}

.order-no {
  color: $text-muted;
  font-size: 13px;
}

.order-body {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  padding: $spacing-sm 0;

  &:hover .order-title {
    color: $primary-light;
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
  transition: color 0.2s;
}

.order-user,
.order-time {
  font-size: 13px;
  color: $text-muted;
}

.order-req {
  font-size: 13px;
  color: $text-secondary;
  background: $bg-hover;
  padding: $spacing-xs $spacing-sm;
  border-radius: $border-radius;
  margin-top: $spacing-xs;
}

.order-amount {
  font-size: 20px;
  font-weight: 700;
  color: $danger-color;
  white-space: nowrap;
  margin-left: $spacing-md;
}

.order-footer {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
  padding-top: $spacing-sm;
  border-top: 1px solid $border-color;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
}

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
