<template>
  <div class="user-center-page">
    <!-- 用户信息卡片 -->
    <section class="profile-section">
      <el-card shadow="never" class="profile-card">
        <div class="profile-content">
          <el-avatar :size="72" :src="userInfo?.avatar || undefined">
            {{ userInfo?.username?.charAt(0)?.toUpperCase() }}
          </el-avatar>
          <div class="profile-info">
            <div class="username-row">
              <h2 class="username">{{ userInfo?.username || '用户' }}</h2>
              <LevelBadge 
                v-if="levelInfo" 
                :level="levelInfo.currentLevel" 
                :name="levelInfo.currentLevelName"
                :icon="levelInfo.currentLevelIcon"
              />
            </div>
            <p class="user-role">
              <el-tag v-if="isAdmin" type="danger" size="small">管理员</el-tag>
              <el-tag v-else-if="isProvider" type="warning" size="small">服务者</el-tag>
              <el-tag v-else size="small">普通用户</el-tag>
            </p>
            <p class="user-id">ID: {{ userInfo?.id }}</p>
            <div class="exp-bar-wrap" v-if="levelInfo">
              <ExperienceBar 
                :current-exp="levelInfo.experience"
                :current-level-exp="levelInfo.currentLevelRequiredExp"
                :next-level-exp="levelInfo.nextLevelRequiredExp"
                :show-to-next="false"
              />
            </div>
          </div>
          <div class="balance-display">
            <span class="balance-label">账户余额</span>
            <span class="balance-value">{{ formatMoney(userInfo?.balance || 0) }}</span>
            <el-button type="primary" text size="small" @click="$router.push('/user/balance')">
              充值
            </el-button>
          </div>
        </div>
      </el-card>
    </section>

    <!-- 快捷入口 -->
    <section class="quick-section">
      <div class="quick-grid">
        <div class="quick-item" @click="$router.push('/order/list')">
          <el-icon :size="28" color="#6366f1"><Document /></el-icon>
          <span class="quick-label">我的订单</span>
        </div>
        <div class="quick-item" @click="$router.push('/user/level')">
          <el-icon :size="28" color="#f59e0b"><Medal /></el-icon>
          <span class="quick-label">等级中心</span>
        </div>
        <div class="quick-item" @click="$router.push('/user/favorites')">
          <el-icon :size="28" color="#ef4444"><Star /></el-icon>
          <span class="quick-label">我的收藏</span>
        </div>
        <div class="quick-item" @click="$router.push('/user/balance')">
          <el-icon :size="28" color="#22c55e"><Wallet /></el-icon>
          <span class="quick-label">余额管理</span>
        </div>
        <div class="quick-item" @click="$router.push('/user/payment-records')">
          <el-icon :size="28" color="#f59e0b"><List /></el-icon>
          <span class="quick-label">支付记录</span>
        </div>
        <div class="quick-item" @click="$router.push('/user/game-accounts')">
          <el-icon :size="28" color="#3b82f6"><Monitor /></el-icon>
          <span class="quick-label">游戏账号</span>
        </div>
        <div class="quick-item" @click="$router.push('/user/reviews')">
          <el-icon :size="28" color="#8b5cf6"><ChatDotSquare /></el-icon>
          <span class="quick-label">我的评价</span>
        </div>
        <div class="quick-item" @click="$router.push('/user/profile')">
          <el-icon :size="28" color="#06b6d4"><Setting /></el-icon>
          <span class="quick-label">个人设置</span>
        </div>
        <div class="quick-item" @click="$router.push('/message')">
          <el-icon :size="28" color="#ec4899"><Bell /></el-icon>
          <span class="quick-label">消息中心</span>
        </div>
      </div>
    </section>

    <!-- 最近订单 -->
    <section class="recent-section">
      <el-card shadow="never">
        <template #header>
          <div class="card-header-row">
            <span class="card-header-title">最近订单</span>
            <el-button text type="primary" @click="$router.push('/order/list')">
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
                <el-tag :type="ORDER_STATUS_TYPES[order.status]" size="small">
                  {{ ORDER_STATUS_LABELS[order.status] }}
                </el-tag>
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
import { ref, computed, onMounted } from 'vue'
import { Document, Star, Wallet, List, Monitor, ChatDotSquare, Setting, Bell, ArrowRight, Medal } from '@element-plus/icons-vue'
import { get } from '@/api/request'
import { levelApi, type UserLevelInfo } from '@/api/level'
import type { Order } from '@/types/order'
import type { PageData } from '@/types/common'
import { ORDER_STATUS_LABELS, ORDER_STATUS_TYPES } from '@/utils/constants'
import { formatMoney, formatDate } from '@/utils/format'
import { useUserStore } from '@/stores/user'
import LevelBadge from '@/components/business/LevelBadge.vue'
import ExperienceBar from '@/components/business/ExperienceBar.vue'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const isAdmin = computed(() => userStore.isAdmin)
const isProvider = computed(() => userStore.isProvider)

const orderLoading = ref(false)
const recentOrders = ref<Order[]>([])
const levelInfo = ref<UserLevelInfo | null>(null)

async function fetchRecentOrders() {
  orderLoading.value = true
  try {
    const res = await get<PageData<Order>>('/orders', { page: 1, size: 5 })
    recentOrders.value = res.data.records
  } catch {
    // ignore
  } finally {
    orderLoading.value = false
  }
}

async function fetchLevelInfo() {
  try {
    const res = await levelApi.getMyLevelInfo()
    levelInfo.value = res.data
  } catch {
    // ignore
  }
}

onMounted(() => {
  userStore.fetchUserInfo()
  fetchRecentOrders()
  fetchLevelInfo()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.user-center-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

// 用户信息卡片
.profile-card {
  :deep(.el-card__body) {
    padding: $spacing-xl;
  }
}

.profile-content {
  display: flex;
  align-items: center;
  gap: $spacing-lg;
}

.profile-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.username-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.username {
  font-size: 22px;
  font-weight: 700;
  color: $text-primary;
  margin: 0;
}

.exp-bar-wrap {
  width: 100%;
  max-width: 300px;
  margin-top: 8px;
}

.user-role {
  margin: 0;
}

.user-id {
  color: $text-muted;
  font-size: 13px;
}

.balance-display {
  text-align: right;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: $spacing-xs;
}

.balance-label {
  color: $text-secondary;
  font-size: 13px;
}

.balance-value {
  font-size: 24px;
  font-weight: 700;
  color: $primary-light;
}

// 快捷入口
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

// 最近订单
.card-header-title {
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
}

.card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.recent-list {
  min-height: 100px;
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

// 响应式
@media (max-width: 768px) {
  .profile-content {
    flex-direction: column;
    text-align: center;
  }

  .balance-display {
    align-items: center;
  }

  .quick-grid {
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
</style>
