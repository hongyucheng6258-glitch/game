<template>
  <div class="provider-center-page">
    <h2 class="page-title">服务者中心</h2>

    <!-- 数据概览 -->
    <section class="stats-section">
      <div class="stats-grid">
        <StatCard title="服务总数" :value="stats.totalServices" icon="Goods" color="#6366f1" />
        <StatCard title="在售服务" :value="stats.activeServices" icon="CircleCheck" color="#22c55e" />
        <StatCard title="总订单数" :value="stats.totalOrders" icon="Document" color="#3b82f6" />
        <StatCard title="总销售额" :value="stats.totalRevenue" icon="Money" color="#f59e0b" prefix="¥" />
        <StatCard title="信誉分" :value="stats.creditScore || 100" icon="Medal" :color="creditScoreColor" />
        <StatCard title="平均评分" :value="stats.avgRating" icon="Star" color="#8b5cf6" />
        <StatCard title="总评价数" :value="stats.totalReviews" icon="ChatDotSquare" color="#ec4899" />
      </div>
    </section>

    <!-- 快捷操作 -->
    <section class="actions-section">
      <el-card shadow="never">
        <template #header>
          <span class="card-title">快捷操作</span>
        </template>
        <div class="actions-grid">
          <el-button type="primary" @click="$router.push('/provider/service/create')">
            <el-icon><Plus /></el-icon> 发布新服务
          </el-button>
          <el-button @click="$router.push('/provider/services')">
            <el-icon><List /></el-icon> 管理我的服务
          </el-button>
          <el-button @click="$router.push('/provider/orders')">
            <el-icon><Tickets /></el-icon> 接单管理
          </el-button>
          <el-button @click="$router.push('/user/balance')">
            <el-icon><Wallet /></el-icon> 余额管理
          </el-button>
        </div>
      </el-card>
    </section>

    <!-- 最近订单 -->
    <section class="recent-section">
      <el-card shadow="never">
        <template #header>
          <div class="card-header-row">
            <span class="card-title">最近订单</span>
            <el-button text type="primary" @click="$router.push('/provider/orders')">
              查看全部 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </template>
        <div v-loading="orderLoading" class="recent-list">
          <template v-if="recentOrders.length > 0">
            <div
              v-for="order in recentOrders"
              :key="order.id"
              class="recent-item"
              @click="$router.push(`/order/${order.orderNo}`)"
            >
              <div class="recent-info">
                <span class="recent-title">{{ order.serviceTitle || '服务订单' }}</span>
                <span class="recent-time">{{ formatDate(order.createdAt) }}</span>
              </div>
              <div class="recent-right">
                <OrderStatusBadge :status="order.status" />
                <span class="recent-amount">{{ formatMoney(order.totalAmount) }}</span>
              </div>
            </div>
          </template>
          <el-empty v-else description="暂无订单" :image-size="80" />
        </div>
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Plus, List, Tickets, Wallet, ArrowRight, Medal } from '@element-plus/icons-vue'
import { get } from '@/api/request'
import { useUserStore } from '@/stores/user'
import type { Order } from '@/types/order'
import type { PageData } from '@/types/common'
import { formatMoney, formatDate } from '@/utils/format'
import StatCard from '@/components/admin/StatCard.vue'
import OrderStatusBadge from '@/components/business/OrderStatusBadge.vue'

const userStore = useUserStore()

const orderLoading = ref(false)
const recentOrders = ref<Order[]>([])

const stats = reactive({
  totalServices: 0,
  activeServices: 0,
  totalOrders: 0,
  totalRevenue: 0,
  creditScore: 100,
  avgRating: 0,
  totalReviews: 0,
})

const creditScoreColor = computed(() => {
  const score = stats.creditScore || 100
  if (score >= 90) return '#22c55e'
  if (score >= 70) return '#eab308'
  if (score >= 50) return '#f97316'
  return '#ef4444'
})

async function fetchStats() {
  try {
    const res = await get<any>('/provider/statistics')
    console.log('Provider stats response:', res)
    console.log('Provider stats data:', res.data)
    if (res.data) {
      Object.assign(stats, res.data)
      console.log('Stats after assign:', stats)
    }
    stats.creditScore = userStore.userInfo?.creditScore || 100
  } catch (err) {
    console.error('Error fetching stats:', err)
  }
}

async function fetchRecentOrders() {
  orderLoading.value = true
  try {
    const res = await get<PageData<Order>>('/orders/received', { page: 1, size: 5 })
    recentOrders.value = res.data.records
  } catch {
    // ignore
  } finally {
    orderLoading.value = false
  }
}

onMounted(() => {
  fetchStats()
  fetchRecentOrders()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.provider-center-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  font-family: 'Orbitron', sans-serif;
  color: $text-primary;
  margin: 0;
  background: linear-gradient(135deg, $neon-cyan 0%, $primary-light 50%, $neon-purple 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 2px;
  text-transform: uppercase;
  position: relative;
  padding-bottom: $spacing-md;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 120px;
    height: 2px;
    background: linear-gradient(90deg, $neon-cyan, $primary-light, $neon-purple);
    box-shadow: 0 0 12px rgba(0, 240, 255, 0.5), 0 0 24px rgba(191, 90, 242, 0.3);
    border-radius: 2px;
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-md;

  :deep(.stat-card) {
    background: $glass-bg;
    backdrop-filter: blur($glass-blur);
    -webkit-backdrop-filter: blur($glass-blur);
    border: 1px solid $border-glow;
    border-radius: $border-radius-lg;
    transition: transform $transition-normal, box-shadow $transition-normal, border-color $transition-normal;
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 2px;
      background: linear-gradient(90deg, $neon-cyan, $primary-light, $neon-purple);
      opacity: 0;
      transition: opacity $transition-normal;
    }

    &:hover {
      transform: translateY(-3px);
      border-color: rgba(0, 240, 255, 0.35);
      box-shadow:
        0 0 20px rgba(0, 240, 255, 0.15),
        0 0 40px rgba(99, 102, 241, 0.1),
        0 8px 24px rgba(0, 0, 0, 0.3);

      &::before {
        opacity: 1;
      }
    }

    :deep(.stat-value) {
      font-family: 'Orbitron', sans-serif;
      color: $neon-cyan;
      text-shadow: 0 0 10px rgba(0, 240, 255, 0.4);
    }
  }
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
  position: relative;
  padding-left: $spacing-sm;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 3px;
    height: 16px;
    background: linear-gradient(180deg, $neon-cyan, $neon-purple);
    border-radius: 2px;
    box-shadow: 0 0 8px rgba(0, 240, 255, 0.4);
  }
}

.card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.actions-section,
.recent-section {
  :deep(.el-card) {
    background: $glass-bg;
    backdrop-filter: blur($glass-blur);
    -webkit-backdrop-filter: blur($glass-blur);
    border: 1px solid $border-glow;
    border-radius: $border-radius-lg;
    box-shadow: $shadow-glow;
    transition: transform $transition-normal, box-shadow $transition-normal, border-color $transition-normal;

    &:hover {
      border-color: rgba(0, 240, 255, 0.3);
      box-shadow:
        0 0 30px rgba(0, 240, 255, 0.12),
        0 0 60px rgba(99, 102, 241, 0.08),
        0 8px 32px rgba(0, 0, 0, 0.3);
    }
  }
}

.actions-grid {
  display: flex;
  gap: $spacing-md;
  flex-wrap: wrap;

  :deep(.el-button--primary) {
    background: linear-gradient(135deg, $primary-color, $neon-cyan);
    border: 1px solid rgba(0, 240, 255, 0.3);
    box-shadow: 0 2px 8px rgba(0, 240, 255, 0.2);
    font-weight: 600;
    letter-spacing: 0.5px;
    transition: all $transition-normal;

    &:hover {
      box-shadow:
        0 0 16px rgba(0, 240, 255, 0.4),
        0 0 32px rgba(0, 240, 255, 0.15);
      transform: translateY(-2px);
      border-color: rgba(0, 240, 255, 0.6);
    }
  }

  :deep(.el-button--default) {
    background: rgba(51, 65, 85, 0.5);
    border: 1px solid $border-glow;
    color: $text-secondary;
    transition: all $transition-normal;

    &:hover {
      background: rgba(0, 240, 255, 0.08);
      color: $neon-cyan;
      border-color: rgba(0, 240, 255, 0.4);
      box-shadow: 0 0 12px rgba(0, 240, 255, 0.15);
      transform: translateY(-1px);
    }
  }
}

.recent-list {
  min-height: 100px;
}

.recent-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md 0;
  border-bottom: 1px solid rgba(0, 240, 255, 0.06);
  cursor: pointer;
  transition: all $transition-fast;
  border-radius: $border-radius;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: rgba(0, 240, 255, 0.04);
    margin: 0 (-$spacing-md);
    padding-left: $spacing-md;
    padding-right: $spacing-md;
    box-shadow: inset 0 0 20px rgba(0, 240, 255, 0.03);

    .recent-title {
      color: $neon-cyan;
      text-shadow: 0 0 8px rgba(0, 240, 255, 0.3);
    }
  }
}

.recent-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.recent-title {
  font-size: 14px;
  color: $text-primary;
  font-weight: 500;
  transition: all $transition-fast;
}

.recent-time {
  font-size: 12px;
  color: $text-muted;
  font-family: 'Courier New', monospace;
}

.recent-right {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.recent-amount {
  font-size: 16px;
  font-weight: 700;
  color: $neon-cyan;
  white-space: nowrap;
  text-shadow: 0 0 10px rgba(0, 240, 255, 0.4);
  font-family: 'Orbitron', sans-serif;
}

@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .recent-item {
    flex-direction: column;
    align-items: flex-start;
    gap: $spacing-sm;
  }

  .recent-right {
    width: 100%;
    justify-content: space-between;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
