<template>
  <el-card shadow="never" class="order-card">
    <div class="order-header">
      <span class="order-no">订单号：{{ order.orderNo }}</span>
      <OrderStatusBadge :status="order.status" />
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
      <slot name="actions" :order="order">
        <el-button size="small" @click.stop="$router.push(`/order/${order.orderNo}`)">
          查看详情
        </el-button>
      </slot>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import type { Order } from '@/types/order'
import OrderStatusBadge from './OrderStatusBadge.vue'
import { formatMoney, formatDate } from '@/utils/format'

defineProps<{ order: Order }>()
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

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

.order-provider,
.order-time {
  font-size: 13px;
  color: $text-muted;
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
