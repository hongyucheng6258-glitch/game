<template>
  <div class="admin-dashboard-page">
    <h2 class="page-title">仪表盘</h2>

    <!-- 统计卡片 -->
    <section class="stats-section">
      <div class="stats-grid">
        <StatCard title="用户总数" :value="stats.totalUsers" icon="User" color="#6366f1" @click="$router.push('/admin/users')" />
        <StatCard title="服务者总数" :value="stats.totalProviders" icon="Trophy" color="#22c55e" @click="$router.push('/admin/users')" />
        <StatCard title="订单总数" :value="stats.totalOrders" icon="Document" color="#3b82f6" @click="$router.push('/admin/orders')" />
        <StatCard title="总销售额" :value="stats.totalRevenue" icon="Money" color="#f59e0b" prefix="¥" @click="$router.push('/admin/payments')" />
        <StatCard title="今日新增用户" :value="stats.todayUsers" icon="UserFilled" color="#8b5cf6" @click="$router.push('/admin/users')" />
        <StatCard title="今日订单数" :value="stats.todayOrders" icon="Tickets" color="#ec4899" @click="$router.push('/admin/orders')" />
        <StatCard title="今日收入" :value="stats.todayRevenue" icon="Wallet" color="#14b8a6" prefix="¥" @click="$router.push('/admin/payments')" />
        <StatCard title="待处理提现" :value="stats.pendingWithdrawals" icon="MoneyFilled" color="#f97316" @click="$router.push('/admin/withdrawals')" />
      </div>
    </section>

    <!-- 快捷入口 -->
    <section class="quick-section">
      <div class="quick-grid">
        <div class="quick-item" @click="$router.push('/admin/users')">
          <el-icon :size="28" color="#6366f1"><User /></el-icon>
          <span class="quick-label">用户管理</span>
        </div>
        <div class="quick-item" @click="$router.push('/admin/services')">
          <el-icon :size="28" color="#22c55e"><Goods /></el-icon>
          <span class="quick-label">服务管理</span>
        </div>
        <div class="quick-item" @click="$router.push('/admin/orders')">
          <el-icon :size="28" color="#3b82f6"><Document /></el-icon>
          <span class="quick-label">订单管理</span>
        </div>
        <div class="quick-item" @click="$router.push('/admin/payments')">
          <el-icon :size="28" color="#f59e0b"><Money /></el-icon>
          <span class="quick-label">财务管理</span>
        </div>
        <div class="quick-item" @click="$router.push('/admin/withdrawals')">
          <el-icon :size="28" color="#8b5cf6"><Wallet /></el-icon>
          <span class="quick-label">提现记录</span>
        </div>
        <div class="quick-item" @click="$router.push('/admin/reviews')">
          <el-icon :size="28" color="#ec4899"><ChatDotRound /></el-icon>
          <span class="quick-label">评价管理</span>
        </div>
        <div class="quick-item" @click="$router.push('/admin/announcements')">
          <el-icon :size="28" color="#14b8a6"><Bell /></el-icon>
          <span class="quick-label">公告管理</span>
        </div>
        <div class="quick-item" @click="$router.push('/admin/settings')">
          <el-icon :size="28" color="#f97316"><Setting /></el-icon>
          <span class="quick-label">系统设置</span>
        </div>
      </div>
    </section>

    <!-- 图表区域 -->
    <section class="charts-section">
      <div class="charts-grid">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header-row">
              <span class="card-title">订单趋势（近7天）</span>
              <el-button text type="primary" @click="$router.push('/admin/orders')">
                查看全部
              </el-button>
            </div>
          </template>
          <DataChart :option="orderChartOption" height="350px" />
        </el-card>
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header-row">
              <span class="card-title">收入趋势（近7天）</span>
              <el-button text type="primary" @click="$router.push('/admin/payments')">
                查看全部
              </el-button>
            </div>
          </template>
          <DataChart :option="revenueChartOption" height="350px" />
        </el-card>
      </div>
    </section>

    <!-- 排行榜和最近订单 -->
    <section class="bottom-section">
      <div class="bottom-grid">
        <!-- 服务者排行榜 -->
        <el-card shadow="never" class="ranking-card">
          <template #header>
            <div class="card-header-row">
              <span class="card-title">服务者排行榜</span>
              <el-button text type="primary" @click="$router.push('/admin/users')">
                查看全部
              </el-button>
            </div>
          </template>
          <div v-loading="rankingLoading" class="ranking-list">
            <template v-if="providerRanking.length > 0">
              <div
                v-for="(item, index) in providerRanking"
                :key="item.providerId"
                class="ranking-item"
                @click="$router.push(`/admin/users`)"
              >
                <div class="ranking-number" :class="'rank-' + (index + 1)">
                  {{ index + 1 }}
                </div>
                <el-avatar :size="40" :src="getAvatarUrl(item.avatar)">
                  {{ (item.providerName || item.username)?.charAt(0)?.toUpperCase() }}
                </el-avatar>
                <div class="ranking-info">
                  <span class="ranking-name">{{ item.providerName || item.username }}</span>
                  <span class="ranking-stats">
                    完成{{ item.completedOrders }}单 · 
                    {{ item.gameType || '综合' }}
                  </span>
                </div>
                <div class="ranking-value">
                  {{ item.totalRevenue ? '¥' + item.totalRevenue : '¥0' }}
                </div>
              </div>
            </template>
            <el-empty v-else description="暂无数据" :image-size="80" />
          </div>
        </el-card>

        <!-- 最近订单 -->
        <el-card shadow="never" class="recent-card">
          <template #header>
            <div class="card-header-row">
              <span class="card-title">最近订单</span>
              <el-button text type="primary" @click="$router.push('/admin/orders')">
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
                @click="$router.push(`/admin/orders`)"
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
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ArrowRight, User, Goods, Document, Money, Trophy, Wallet, Bell, Setting, ChatDotRound, UserFilled, MoneyFilled } from '@element-plus/icons-vue'
import { getDashboardStats, getAdminOrders, getAdminUsers } from '@/api/admin'
import { get } from '@/api/request'
import type { Order } from '@/types/order'
import { formatDate, formatMoney } from '@/utils/format'
import { getAvatarUrl } from '@/utils/avatar'
import StatCard from '@/components/admin/StatCard.vue'
import DataChart from '@/components/admin/DataChart.vue'
import OrderStatusBadge from '@/components/business/OrderStatusBadge.vue'
import type { EChartsOption } from 'echarts'

const recentOrders = ref<Order[]>([])
const providerRanking = ref<any[]>([])
const orderTrendData = ref<any[]>([])
const revenueTrendData = ref<any[]>([])
const orderLoading = ref(false)
const rankingLoading = ref(false)
const trendLoading = ref(false)

const stats = reactive({
  totalUsers: 0,
  totalProviders: 0,
  totalOrders: 0,
  totalRevenue: 0,
  todayUsers: 0,
  todayOrders: 0,
  todayRevenue: 0,
  totalServices: 0,
  pendingWithdrawals: 0,
})

const last7Days = computed(() => {
  return orderTrendData.value.map((item: any) => {
    const date = new Date(item.date)
    return `${date.getMonth() + 1}/${date.getDate()}`
  })
})

const orderData = computed(() => {
  return orderTrendData.value.map((item: any) => item.count || 0)
})

const revenueData = computed(() => {
  return revenueTrendData.value.map((item: any) => item.amount || 0)
})

const orderChartOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { 
    type: 'category', 
    data: last7Days.value.length > 0 ? last7Days.value : ['4/7', '4/8', '4/9', '4/10', '4/11', '4/12', '4/13'], 
    axisLine: { lineStyle: { color: '#334155' } }, 
    axisLabel: { color: '#94a3b8' } 
  },
  yAxis: { 
    type: 'value', 
    axisLine: { lineStyle: { color: '#334155' } }, 
    axisLabel: { color: '#94a3b8' }, 
    splitLine: { lineStyle: { color: '#1e293b' } } 
  },
  series: [
    { 
      name: '订单数', 
      type: 'line', 
      smooth: true, 
      data: orderData.value.length > 0 ? orderData.value : [0, 0, 0, 0, 0, 0, 0], 
      areaStyle: { opacity: 0.3, color: '#6366f1' }, 
      itemStyle: { color: '#6366f1' },
      lineStyle: { width: 3 }
    }
  ],
}))

const revenueChartOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'axis', formatter: '{b}: ¥{c}' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { 
    type: 'category', 
    data: last7Days.value.length > 0 ? last7Days.value : ['4/7', '4/8', '4/9', '4/10', '4/11', '4/12', '4/13'], 
    axisLine: { lineStyle: { color: '#334155' } }, 
    axisLabel: { color: '#94a3b8' } 
  },
  yAxis: { 
    type: 'value', 
    axisLine: { lineStyle: { color: '#334155' } }, 
    axisLabel: { color: '#94a3b8', formatter: '¥{value}' }, 
    splitLine: { lineStyle: { color: '#1e293b' } } 
  },
  series: [
    { 
      name: '收入', 
      type: 'bar', 
      data: revenueData.value.length > 0 ? revenueData.value : [0, 0, 0, 0, 0, 0, 0], 
      itemStyle: { 
        color: '#22c55e', 
        borderRadius: [4, 4, 0, 0] 
      },
      barWidth: '50%'
    }
  ],
}))

async function fetchStats() {
  try {
    const res = await getDashboardStats()
    if (res.data) {
      stats.totalUsers = res.data.totalUsers || 0
      stats.totalProviders = res.data.totalProviders || 0
      stats.totalOrders = res.data.totalOrders || 0
      stats.totalRevenue = res.data.totalRevenue || 0
      stats.todayOrders = res.data.todayOrders || 0
      stats.todayRevenue = res.data.todayRevenue || 0
      stats.totalServices = res.data.totalServices || 0
      stats.pendingWithdrawals = res.data.pendingWithdrawals || 0
      stats.todayUsers = 0 // 后端暂未提供，后续可以添加
    }
  } catch {
    // ignore
  }
}

async function fetchRecentOrders() {
  orderLoading.value = true
  try {
    const res = await getAdminOrders({ page: 1, size: 5 })
    recentOrders.value = res.data?.records || []
  } catch {
    // ignore
  } finally {
    orderLoading.value = false
  }
}

async function fetchProviderRanking() {
  rankingLoading.value = true
  try {
    const res = await get('/statistics/ranking', { limit: 5 })
    providerRanking.value = res.data || []
  } catch {
    // ignore
  } finally {
    rankingLoading.value = false
  }
}

async function fetchTrendData() {
  trendLoading.value = true
  try {
    const [orderRes, revenueRes] = await Promise.all([
      get('/statistics/trend/orders', { days: 7 }),
      get('/statistics/trend/revenue', { days: 7 })
    ])
    orderTrendData.value = orderRes.data || []
    revenueTrendData.value = revenueRes.data || []
  } catch {
    // ignore
  } finally {
    trendLoading.value = false
  }
}

onMounted(() => {
  fetchStats()
  fetchRecentOrders()
  fetchProviderRanking()
  fetchTrendData()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.admin-dashboard-page {
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-md;
}

.quick-section {
  margin-top: $spacing-md;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-md;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-lg $spacing-md;
  background: $bg-card;
  border: 1px solid $border-color;
  border-radius: $border-radius-lg;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    border-color: $primary-color;
    transform: translateY(-2px);
    box-shadow: $shadow-md;
  }
}

.quick-label {
  font-size: 14px;
  color: $text-secondary;
  font-weight: 500;
}

.charts-section {
  margin-top: $spacing-md;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-md;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
}

.card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.bottom-section {
  margin-top: $spacing-md;
}

.bottom-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-md;
}

.ranking-list,
.recent-list {
  min-height: 100px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md 0;
  border-bottom: 1px solid $border-color;
  cursor: pointer;
  transition: background 0.2s;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: $bg-hover;
    margin: 0 (-$spacing-md);
    padding-left: $spacing-md;
    padding-right: $spacing-md;
    border-radius: $border-radius;
  }
}

.ranking-number {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  background: #1e293b;
  color: #94a3b8;

  &.rank-1 {
    background: linear-gradient(135deg, #fbbf24, #f59e0b);
    color: #fff;
  }

  &.rank-2 {
    background: linear-gradient(135deg, #94a3b8, #64748b);
    color: #fff;
  }

  &.rank-3 {
    background: linear-gradient(135deg, #fb923c, #ea580c);
    color: #fff;
  }
}

.ranking-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.ranking-name {
  font-size: 14px;
  color: $text-primary;
  font-weight: 500;
}

.ranking-stats {
  font-size: 12px;
  color: $text-muted;
}

.ranking-value {
  font-size: 16px;
  font-weight: 600;
  color: $success-color;
}

.recent-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md 0;
  border-bottom: 1px solid $border-color;
  cursor: pointer;
  transition: background 0.2s;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: $bg-hover;
    margin: 0 (-$spacing-md);
    padding-left: $spacing-md;
    padding-right: $spacing-md;
    border-radius: $border-radius;
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
}

.recent-time {
  font-size: 12px;
  color: $text-muted;
}

.recent-right {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.recent-amount {
  font-size: 16px;
  font-weight: 600;
  color: $danger-color;
  white-space: nowrap;
}

:deep(.el-table) {
  --el-table-bg-color: #0f172a;
  --el-table-tr-bg-color: #0f172a;
  --el-table-header-bg-color: #1e293b;
  --el-table-row-hover-bg-color: #334155;
  --el-table-border-color: #334155;
  --el-table-text-color: #f1f5f9;
  --el-table-header-text-color: #94a3b8;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .quick-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .bottom-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
