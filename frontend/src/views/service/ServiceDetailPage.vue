<template>
  <div class="service-detail-page">
    <div v-loading="loading" class="detail-container">
      <template v-if="service">
        <!-- 服务基本信息 -->
        <section class="info-section">
          <el-card shadow="never">
            <div class="service-main">
              <div class="service-info">
                <div class="service-tags">
                  <el-tag type="primary" size="small">{{ gameTypeLabel }}</el-tag>
                  <el-tag :type="service.serviceType === 0 ? 'success' : 'warning'" size="small">
                    {{ service.serviceType === 0 ? '陪玩' : '代打' }}
                  </el-tag>
                  <el-tag v-for="tag in service.tags" :key="tag" size="small" effect="plain">
                    {{ tag }}
                  </el-tag>
                </div>
                <h1 class="service-title">{{ service.title }}</h1>
                <div class="service-meta">
                  <span class="meta-item">
                    <el-icon><Star /></el-icon>
                    {{ service.rating.toFixed(1) }}
                  </span>
                  <span class="meta-item">
                    <el-icon><ChatDotRound /></el-icon>
                    {{ service.reviewCount }} 条评价
                  </span>
                  <span class="meta-item">
                    <el-icon><ShoppingCart /></el-icon>
                    已售 {{ service.salesCount }}
                  </span>
                  <span class="meta-item">
                    <el-icon><Timer /></el-icon>
                    {{ formatDuration(service.duration) }}
                  </span>
                </div>
                <p class="service-description">{{ service.description }}</p>
                <div class="service-price">
                  <span class="price-label">价格</span>
                  <template v-if="service.activityId && service.activityPrice">
                    <span class="original-price">{{ formatMoney(service.price) }}</span>
                    <span class="price-value">{{ formatMoney(service.activityPrice) }}</span>
                    <span class="discount-tag">{{ Math.round(service.activityDiscountRate! * 100) }}折</span>
                    <el-tag type="danger" size="small" effect="dark" class="activity-tag">{{ service.activityTitle }}</el-tag>
                  </template>
                  <template v-else-if="discountedPrice">
                    <span class="original-price">{{ formatMoney(service.price) }}</span>
                    <span class="price-value">{{ formatMoney(discountedPrice) }}</span>
                    <span class="discount-tag">{{ Math.round(levelInfo!.discountRate * 100) }}折</span>
                  </template>
                  <span v-else class="price-value">{{ formatMoney(service.price) }}</span>
                </div>
                <div v-if="service.activityId && service.activityPrice && levelInfo && levelInfo.discountRate < 1" class="level-discount-info">
                  <el-tag type="success" size="small">
                    {{ levelInfo.currentLevelName }}会员再享{{ Math.round(levelInfo.discountRate * 100) }}折
                  </el-tag>
                  <span class="discount-text">最终价 {{ formatMoney(Math.round(service.activityPrice * levelInfo.discountRate * 100) / 100) }}</span>
                </div>
                <div v-else-if="levelInfo && savedAmount" class="level-discount-info">
                  <el-tag type="success" size="small">
                    {{ levelInfo.currentLevelName }}会员特权
                  </el-tag>
                  <span class="discount-text">购买此服务可省 {{ formatMoney(savedAmount) }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </section>

        <!-- 服务者信息 -->
        <section class="provider-section">
          <el-card shadow="never">
            <template #header>
              <span class="card-header-title">服务者信息</span>
            </template>
            <div class="provider-info">
              <el-avatar :size="56" :src="service.providerAvatar || undefined">
                {{ service.providerName?.charAt(0) }}
              </el-avatar>
              <div class="provider-detail">
                <span class="provider-name">{{ service.providerName }}</span>
                <span class="provider-id">ID: {{ service.providerId }}</span>
              </div>
              <el-button type="primary" plain @click="handleChat">
                <el-icon><ChatDotRound /></el-icon> 联系TA
              </el-button>
            </div>
          </el-card>
        </section>

        <!-- 评价列表 -->
        <section class="review-section">
          <el-card shadow="never">
            <template #header>
              <div class="card-header-row">
                <span class="card-header-title">用户评价 ({{ reviews.length }})</span>
              </div>
            </template>

            <div v-loading="reviewLoading" class="review-list">
              <template v-if="reviews.length > 0">
                <div v-for="review in reviews" :key="review.id" class="review-item">
                  <div class="review-header">
                    <el-avatar :size="36" :src="review.userAvatar || undefined">
                      {{ review.isAnonymous ? '匿' : review.userName?.charAt(0) }}
                    </el-avatar>
                    <div class="review-user">
                      <span class="review-name">
                        {{ review.isAnonymous ? '匿名用户' : review.userName }}
                      </span>
                      <el-rate
                        :model-value="review.rating"
                        disabled
                        size="small"
                      />
                    </div>
                    <span class="review-time">{{ formatDate(review.createdAt) }}</span>
                  </div>
                  <p v-if="review.content" class="review-content">{{ review.content }}</p>
                  <div v-if="review.reply" class="review-reply">
                    <span class="reply-label">商家回复：</span>
                    {{ review.reply }}
                  </div>
                </div>
              </template>
              <el-empty v-else description="暂无评价" />
            </div>

            <div v-if="reviewTotal > 10" class="pagination-wrapper">
              <el-pagination
                v-model:current-page="reviewPage"
                :page-size="10"
                :total="reviewTotal"
                layout="prev, pager, next"
                background
                @current-change="fetchReviews"
              />
            </div>
          </el-card>
        </section>

        <!-- 底部操作栏 -->
        <div class="action-bar">
          <div class="action-left">
            <el-button @click="handleFavorite">
              <el-icon><Star /></el-icon>
              {{ isFavorited ? '已收藏' : '收藏' }}
            </el-button>
          </div>
          <el-button type="primary" size="large" class="order-btn" @click="handleOrder">
            立即下单
          </el-button>
        </div>
      </template>

      <el-empty v-else-if="!loading" description="服务不存在" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, ChatDotRound, ShoppingCart, Timer } from '@element-plus/icons-vue'
import { get } from '@/api/request'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/favorite'
import { getMyLevelInfo } from '@/api/level'
import type { UserLevelInfo } from '@/api/level'
import type { Service } from '@/types/service'
import type { Review } from '@/types/review'
import type { PageData } from '@/types/common'
import { GAME_TYPES, SERVICE_TYPE_LABELS } from '@/utils/constants'
import { formatMoney, formatDuration, formatDate } from '@/utils/format'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const service = ref<Service | null>(null)
const isFavorited = ref(false)
const levelInfo = ref<UserLevelInfo | null>(null)

const reviews = ref<Review[]>([])
const reviewLoading = ref(false)
const reviewTotal = ref(0)
const reviewPage = ref(1)

const discountedPrice = computed(() => {
  if (!service.value || !levelInfo.value) return null
  const originalPrice = service.value.price
  const discountRate = levelInfo.value.discountRate
  if (discountRate === 1) return null
  return Math.round(originalPrice * discountRate * 100) / 100
})

const savedAmount = computed(() => {
  if (!discountedPrice.value || !service.value) return null
  return service.value.price - discountedPrice.value
})

const gameTypeLabel = computed(() => {
  if (!service.value) return ''
  return GAME_TYPES.find(g => g === service.value!.gameType) || service.value.gameType
})

async function fetchService() {
  const id = route.params.id as string
  if (!id) return

  loading.value = true
  try {
    const res = await get<Service>(`/services/${id}`)
    service.value = res.data
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function fetchUserLevelInfo() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getMyLevelInfo()
    levelInfo.value = res.data
  } catch {
    // ignore
  }
}

async function fetchReviews() {
  const id = route.params.id as string
  if (!id) return

  reviewLoading.value = true
  try {
    const res = await get<PageData<Review>>(`/services/${id}/reviews`, {
      page: reviewPage.value,
      size: 10,
    })
    reviews.value = res.data.records
    reviewTotal.value = res.data.total
  } catch {
    // ignore
  } finally {
    reviewLoading.value = false
  }
}

async function checkFavoriteStatus() {
  if (!userStore.isLoggedIn) return
  try {
    const id = Number(route.params.id)
    const res = await checkFavorite(id)
    if (res.code === 200) {
      isFavorited.value = res.data.favorited
    }
  } catch {
    // ignore
  }
}

async function handleFavorite() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }

  const id = Number(route.params.id)
  try {
    if (isFavorited.value) {
      const res = await removeFavorite(id)
      if (res.code === 200) {
        isFavorited.value = false
        ElMessage.success('已取消收藏')
      }
    } else {
      const res = await addFavorite(id)
      if (res.code === 200) {
        isFavorited.value = true
        ElMessage.success('收藏成功')
      }
    }
  } catch (error) {
    ElMessage.error('操作失败，请稍后重试')
  }
}

function handleOrder() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  router.push(`/order/confirm/${route.params.id}`)
}

function handleChat() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  if (service.value) {
    router.push({
      path: `/message/chat/${service.value.providerId}`,
      query: {
        serviceId: service.value.id,
        serviceTitle: service.value.title,
        servicePrice: service.value.price
      }
    })
  }
}

onMounted(() => {
  fetchService()
  fetchReviews()
  checkFavoriteStatus()
  fetchUserLevelInfo()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

// --- 霓虹光环旋转动画 ---
@keyframes neon-ring-rotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

// --- 霓虹脉冲动画 ---
@keyframes neon-glow-pulse {
  0%, 100% {
    box-shadow: 0 0 8px rgba($neon-cyan, 0.3), 0 0 20px rgba($neon-cyan, 0.1);
  }
  50% {
    box-shadow: 0 0 14px rgba($neon-cyan, 0.5), 0 0 35px rgba($neon-cyan, 0.2);
  }
}

// --- 按钮呼吸发光 ---
@keyframes btn-glow-breathe {
  0%, 100% {
    box-shadow:
      0 0 10px rgba($neon-cyan, 0.3),
      0 0 30px rgba($neon-cyan, 0.1),
      0 4px 15px rgba($primary-color, 0.3);
  }
  50% {
    box-shadow:
      0 0 18px rgba($neon-cyan, 0.5),
      0 0 45px rgba($neon-cyan, 0.15),
      0 6px 25px rgba($primary-color, 0.4);
  }
}

// --- 渐变位移动画 ---
@keyframes gradient-shift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.service-detail-page {
  padding: $spacing-lg 0;
  padding-bottom: 100px;
  position: relative;

  // 赛博网格背景
  &::before {
    content: '';
    position: fixed;
    inset: 0;
    background-image:
      linear-gradient(rgba($primary-color, 0.03) 1px, transparent 1px),
      linear-gradient(90deg, rgba($primary-color, 0.03) 1px, transparent 1px);
    background-size: 40px 40px;
    pointer-events: none;
    z-index: 0;
  }

  // 让所有子内容在网格之上
  > * {
    position: relative;
    z-index: 1;
  }
}

.detail-container {
  max-width: 960px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: $spacing-md;

  :deep(.el-card) {
    background: $glass-bg;
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
    border: 1px solid $glass-border;
    border-radius: $border-radius-lg;
    box-shadow: $shadow-glow;
    transition: all $transition-normal;

    &:hover {
      box-shadow: 0 0 30px rgba(99, 102, 241, 0.2);
      border-color: $border-color-hover;
    }
  }
}

// --- 服务信息 ---
.service-main {
  display: flex;
  gap: $spacing-xl;
}

.service-info {
  flex: 1;
}

.service-tags {
  display: flex;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
  flex-wrap: wrap;

  :deep(.el-tag) {
    border: 1px solid $border-color;
    backdrop-filter: blur(8px);
    transition: all $transition-normal;

    &:hover {
      border-color: $border-color-hover;
      box-shadow: 0 0 8px rgba($primary-color, 0.15);
    }
  }
}

// --- 服务标题：大号加粗 + 微弱发光 ---
.service-title {
  font-family: 'Noto Sans SC', sans-serif;
  font-size: 28px;
  font-weight: 900;
  color: $text-primary;
  margin-bottom: $spacing-md;
  line-height: 1.3;
  background: linear-gradient(135deg, $text-primary, $primary-light, $neon-cyan);
  background-size: 200% 200%;
  animation: gradient-shift 6s ease infinite;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 0 12px rgba($neon-cyan, 0.2));
}

.service-meta {
  display: flex;
  gap: $spacing-lg;
  margin-bottom: $spacing-md;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  color: $text-secondary;
  font-size: 14px;
  padding: 6px 12px;
  background: rgba($neon-cyan, 0.04);
  border: 1px solid rgba($neon-cyan, 0.06);
  border-radius: $border-radius;
  transition: all $transition-fast;

  &:hover {
    background: rgba($neon-cyan, 0.08);
    border-color: rgba($neon-cyan, 0.15);
    box-shadow: 0 0 10px rgba($neon-cyan, 0.08);
  }

  .el-icon {
    color: $neon-yellow;
  }
}

// --- 描述区域排版优化，行间距增加 ---
.service-description {
  color: $text-secondary;
  font-size: 15px;
  line-height: 2;
  margin-bottom: $spacing-lg;
  white-space: pre-wrap;
  padding: $spacing-md;
  background: rgba($primary-color, 0.03);
  border-radius: $border-radius;
  border: 1px solid rgba($primary-color, 0.05);
  letter-spacing: 0.3px;
}

// --- 价格区域 ---
.service-price {
  display: flex;
  align-items: baseline;
  gap: $spacing-sm;
  padding: $spacing-md $spacing-lg;
  background: rgba($neon-cyan, 0.04);
  border-radius: $border-radius-lg;
  border: 1px solid rgba($neon-cyan, 0.1);
  box-shadow: inset 0 0 20px rgba($neon-cyan, 0.03);
}

.price-label {
  color: $text-muted;
  font-size: 14px;
  font-family: 'Rajdhani', 'Noto Sans SC', sans-serif;
  font-weight: 600;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.original-price {
  font-size: 16px;
  text-decoration: line-through;
  color: $text-muted;
  font-weight: 400;
}

// --- 价格显示：$neon-cyan色，大号字体 ---
.price-value {
  font-family: 'Orbitron', 'Noto Sans SC', sans-serif;
  font-size: 32px;
  font-weight: 800;
  color: $neon-cyan;
  text-shadow: 0 0 15px rgba($neon-cyan, 0.4), 0 0 30px rgba($neon-cyan, 0.15);
  letter-spacing: 1px;
}

.discount-tag {
  display: inline-block;
  padding: 3px 10px;
  background: linear-gradient(135deg, $neon-pink, $danger-color);
  color: white;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 700;
  box-shadow: 0 2px 12px rgba($neon-pink, 0.3);
  letter-spacing: 0.5px;
}

.activity-tag {
  margin-left: 8px;
}

.level-discount-info {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-top: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  background: rgba($neon-green, 0.05);
  border-radius: $border-radius;
  border: 1px solid rgba($neon-green, 0.12);
}

.discount-text {
  color: $neon-green;
  font-size: 13px;
  font-weight: 600;
  text-shadow: 0 0 8px rgba($neon-green, 0.2);
}

// --- 服务者信息卡片：霓虹光环 ---
.provider-section {
  :deep(.el-card) {
    position: relative;
    overflow: hidden;

    // 霓虹光环效果
    &::before {
      content: '';
      position: absolute;
      top: -2px;
      left: -2px;
      right: -2px;
      bottom: -2px;
      background: conic-gradient(
        from 0deg,
        transparent 0%,
        rgba($neon-cyan, 0.3) 10%,
        transparent 20%,
        transparent 50%,
        rgba($neon-purple, 0.3) 60%,
        transparent 70%
      );
      border-radius: inherit;
      z-index: -1;
      animation: neon-ring-rotate 6s linear infinite;
    }

    &::after {
      content: '';
      position: absolute;
      inset: 1px;
      background: $glass-bg;
      border-radius: inherit;
      z-index: -1;
    }
  }
}

.provider-info {
  display: flex;
  align-items: center;
  gap: $spacing-md;

  :deep(.el-avatar) {
    box-shadow:
      0 0 12px rgba($neon-cyan, 0.3),
      0 0 25px rgba($neon-cyan, 0.1);
    border: 2px solid rgba($neon-cyan, 0.3);
    transition: all $transition-normal;
  }
}

.provider-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.provider-name {
  font-size: 16px;
  font-weight: 700;
  color: $text-primary;
  text-shadow: 0 0 8px rgba($neon-cyan, 0.15);
}

.provider-id {
  font-family: 'Rajdhani', monospace;
  font-size: 12px;
  color: $text-muted;
  letter-spacing: 1px;
}

// --- 评价区域 ---
.card-header-title {
  font-family: 'Noto Sans SC', sans-serif;
  font-size: 16px;
  font-weight: 700;
  color: $text-primary;
  letter-spacing: 0.5px;
}

.card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  min-height: 100px;
}

// --- 评价卡片：neon-border 效果 ---
.review-item {
  padding: $spacing-md;
  border: 1px solid $border-color;
  border-radius: $border-radius-lg;
  transition: all $transition-normal;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 3px;
    height: 100%;
    background: linear-gradient(180deg, $neon-cyan, $primary-color);
    border-radius: 3px 0 0 3px;
    opacity: 0;
    transition: opacity $transition-normal;
  }

  &:hover {
    background: rgba($neon-cyan, 0.03);
    border-color: rgba($neon-cyan, 0.2);
    box-shadow:
      0 0 10px rgba($neon-cyan, 0.05),
      inset 0 0 20px rgba($neon-cyan, 0.02);

    &::before {
      opacity: 1;
    }
  }
}

.review-header {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;
}

.review-user {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.review-name {
  font-size: 14px;
  font-weight: 600;
  color: $text-primary;
}

.review-time {
  font-family: 'Rajdhani', monospace;
  font-size: 12px;
  color: $text-muted;
  letter-spacing: 0.5px;
}

.review-content {
  color: $text-secondary;
  font-size: 14px;
  line-height: 1.8;
  margin-bottom: $spacing-sm;
}

.review-reply {
  background: rgba($neon-cyan, 0.04);
  border-radius: $border-radius;
  padding: $spacing-sm $spacing-md;
  font-size: 13px;
  color: $text-secondary;
  border-left: 3px solid $neon-cyan;
  box-shadow: inset 0 0 15px rgba($neon-cyan, 0.02);
}

.reply-label {
  color: $neon-cyan;
  font-weight: 600;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: $spacing-lg;
  padding: $spacing-md 0;

  :deep(.el-pagination) {
    .btn-prev,
    .btn-next,
    .el-pager li {
      background: $bg-elevated !important;
      border: 1px solid $border-color;
      border-radius: $border-radius !important;
      color: $text-secondary !important;
      font-weight: 500;
      transition: all $transition-normal;
      min-width: 36px;
      margin: 0 2px;

      &:hover {
        border-color: $neon-cyan;
        color: $neon-cyan !important;
        box-shadow: 0 0 10px rgba($neon-cyan, 0.2);
      }

      &.is-active {
        background: rgba($neon-cyan, 0.15) !important;
        border-color: $neon-cyan;
        color: $neon-cyan !important;
        font-weight: 700;
        box-shadow:
          0 0 8px rgba($neon-cyan, 0.3),
          0 0 20px rgba($neon-cyan, 0.1);
      }
    }
  }
}

// --- 底部操作栏 ---
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba($bg-dark, 0.85);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-top: 1px solid $glass-border;
  padding: $spacing-md $spacing-xl;
  display: flex;
  align-items: center;
  justify-content: space-between;
  z-index: 100;
  box-shadow:
    0 -4px 20px rgba(0, 0, 0, 0.4),
    0 -1px 0 rgba($neon-cyan, 0.08);
}

.action-left {
  display: flex;
  gap: $spacing-sm;

  :deep(.el-button) {
    transition: all $transition-normal;

    &:hover {
      border-color: $neon-cyan;
      color: $neon-cyan;
      box-shadow: 0 0 10px rgba($neon-cyan, 0.15);
    }
  }
}

// --- 操作按钮：霓虹发光效果 ---
.order-btn {
  min-width: 180px;
  height: 48px;
  font-size: 17px;
  font-weight: 800;
  letter-spacing: 2px;
  border-radius: $border-radius-lg;
  background: linear-gradient(135deg, $primary-color, $neon-cyan) !important;
  border: 1px solid rgba($neon-cyan, 0.3) !important;
  color: $bg-abyss !important;
  animation: btn-glow-breathe 3s ease-in-out infinite;
  transition: all $transition-normal;
  position: relative;
  overflow: hidden;

  // 光泽扫过效果
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 60%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    transition: left 0.5s ease;
  }

  &:hover {
    transform: translateY(-2px) scale(1.03);
    box-shadow:
      0 0 20px rgba($neon-cyan, 0.5),
      0 0 50px rgba($neon-cyan, 0.2),
      0 8px 30px rgba($primary-color, 0.4) !important;
    border-color: $neon-cyan !important;

    &::before {
      left: 120%;
    }
  }

  &:active {
    transform: translateY(0) scale(0.98);
  }
}

// --- 响应式 ---
@media (max-width: 768px) {
  .service-meta {
    flex-direction: column;
    gap: $spacing-sm;
  }

  .action-bar {
    padding: $spacing-sm $spacing-md;
  }

  .order-btn {
    min-width: 120px;
    font-size: 14px;
    height: 42px;
  }

  .service-title {
    font-size: 22px;
  }

  .price-value {
    font-size: 26px;
  }
}
</style>
