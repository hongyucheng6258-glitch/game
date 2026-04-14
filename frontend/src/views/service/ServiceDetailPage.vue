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

.service-detail-page {
  padding: $spacing-lg 0;
  padding-bottom: 100px;
}

.detail-container {
  max-width: 960px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

// 服务信息
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
}

.service-title {
  font-size: 24px;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: $spacing-md;
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

  .el-icon {
    color: $warning-color;
  }
}

.service-description {
  color: $text-secondary;
  font-size: 14px;
  line-height: 1.8;
  margin-bottom: $spacing-lg;
  white-space: pre-wrap;
}

.service-price {
  display: flex;
  align-items: baseline;
  gap: $spacing-sm;
}

.price-label {
  color: $text-secondary;
  font-size: 14px;
}

.original-price {
  font-size: 16px;
  text-decoration: line-through;
  color: $text-muted;
  font-weight: 400;
}

.price-value {
  font-size: 28px;
  font-weight: 700;
  color: $danger-color;
}

.discount-tag {
  display: inline-block;
  padding: 2px 8px;
  background: linear-gradient(135deg, #ff6b6b, #ee5a5a);
  color: white;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.activity-tag {
  margin-left: 8px;
}

.level-discount-info {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-top: $spacing-sm;
}

.discount-text {
  color: $success-color;
  font-size: 13px;
  font-weight: 500;
}

// 服务者信息
.provider-info {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.provider-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.provider-name {
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
}

.provider-id {
  font-size: 12px;
  color: $text-muted;
}

// 评价
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

.review-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  min-height: 100px;
}

.review-item {
  padding: $spacing-md;
  border-bottom: 1px solid $border-color;

  &:last-child {
    border-bottom: none;
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
  color: $text-primary;
}

.review-time {
  font-size: 12px;
  color: $text-muted;
}

.review-content {
  color: $text-secondary;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: $spacing-sm;
}

.review-reply {
  background: $bg-hover;
  border-radius: $border-radius;
  padding: $spacing-sm $spacing-md;
  font-size: 13px;
  color: $text-secondary;
}

.reply-label {
  color: $primary-light;
  font-weight: 500;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: $spacing-lg;
}

// 底部操作栏
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: $bg-card;
  border-top: 1px solid $border-color;
  padding: $spacing-md $spacing-xl;
  display: flex;
  align-items: center;
  justify-content: space-between;
  z-index: 100;
}

.action-left {
  display: flex;
  gap: $spacing-sm;
}

.order-btn {
  min-width: 160px;
  height: 44px;
  font-size: 16px;
  border-radius: $border-radius;
  background: linear-gradient(135deg, $primary-color, $primary-dark);
  border: none;

  &:hover {
    background: linear-gradient(135deg, $primary-light, $primary-color);
  }
}

// 响应式
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
  }
}
</style>
