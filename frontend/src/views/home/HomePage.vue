<template>
  <div class="home-page effect-grid">
    <section class="banner-section">
      <el-carousel height="480px" :interval="5000" arrow="hover">
        <el-carousel-item v-for="(banner, index) in banners" :key="index">
          <div class="banner-item" :style="{ background: banner.gradient }">
            <div class="banner-overlay"></div>
            <div class="banner-grid"></div>
            <div class="banner-content">
              <h1 class="banner-title">{{ banner.title }}</h1>
              <p class="banner-desc">{{ banner.desc }}</p>
              <el-button type="primary" size="large" class="banner-btn btn-glow" @click="$router.push('/service')">
                立即体验
                <el-icon class="btn-icon"><ArrowRight /></el-icon>
              </el-button>
            </div>
            <div class="banner-decoration">
              <div class="deco-circle deco-1"></div>
              <div class="deco-circle deco-2"></div>
              <div class="deco-circle deco-3"></div>
              <div class="deco-grid"></div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <section class="game-section">
      <div class="section-header">
        <div class="section-header-left">
          <h2 class="section-title">
            <el-icon class="section-icon"><VideoPlay /></el-icon>
            热门游戏
          </h2>
          <p class="section-subtitle">选择你喜欢的游戏，找到最合适的队友</p>
        </div>
      </div>
      <div class="game-grid">
        <div
          v-for="game in gameList"
          :key="game.name"
          class="game-card effect-glow-border"
          @click="goToGameService(game.name)"
        >
          <div class="game-icon" :style="{ background: game.color }">
            <span class="game-emoji">{{ game.icon }}</span>
          </div>
          <span class="game-name">{{ game.name }}</span>
        </div>
      </div>
    </section>

    <section v-if="activities.length > 0" class="activity-section">
      <div class="section-header">
        <div class="section-header-left">
          <h2 class="section-title">
            <el-icon class="section-icon"><Promotion /></el-icon>
            限时活动
          </h2>
          <p class="section-subtitle">优惠不容错过</p>
        </div>
      </div>
      <div class="activity-grid">
        <div v-for="activity in activities" :key="activity.id" class="activity-card card-hover">
          <div class="activity-banner" :style="{ background: activityGradient(activity.id) }">
            <div class="activity-badge-wrap">
              <el-tag type="danger" size="small" effect="dark" class="activity-type-tag">{{ activity.type === 0 ? '全场折扣' : activity.type === 1 ? '服务折扣' : '限时特价' }}</el-tag>
            </div>
            <div class="activity-banner-content">
              <span class="activity-discount-num gradient-text">{{ activity.type === 2 ? '特价' : Math.round(activity.discountRate * 100) + '折' }}</span>
            </div>
            <div class="activity-banner-grid"></div>
          </div>
          <div class="activity-body">
            <h3 class="activity-title">{{ activity.title }}</h3>
            <p v-if="activity.description" class="activity-desc">{{ activity.description }}</p>
            <div class="activity-time">
              <el-icon><Timer /></el-icon>
              <span>{{ formatActivityTime(activity.endTime) }}结束</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="service-section">
      <div class="section-header">
        <div class="section-header-left">
          <h2 class="section-title">
            <el-icon class="section-icon"><Star /></el-icon>
            推荐服务
          </h2>
          <p class="section-subtitle">精选优质陪玩代打服务</p>
        </div>
        <el-button text type="primary" class="view-more-btn btn-ghost" @click="$router.push('/service')">
          查看更多 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <div v-loading="loading" class="service-grid">
        <template v-if="services.length > 0">
          <div v-for="item in services" :key="item.id" class="service-card-wrapper">
            <ServiceCard :service="item" />
          </div>
        </template>
        <el-empty v-else description="暂无推荐服务" />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, Promotion, Timer, Star, VideoPlay } from '@element-plus/icons-vue'
import { get } from '@/api/request'
import type { Service } from '@/types/service'
import type { PageData } from '@/types/common'
import type { Activity } from '@/types/activity'
import { getActiveActivities } from '@/api/activity'
import ServiceCard from '@/components/business/ServiceCard.vue'

const router = useRouter()
const loading = ref(false)
const services = ref<Service[]>([])
const activities = ref<Activity[]>([])

const banners = [
  {
    title: '专业电竞陪玩',
    desc: '百万玩家在线，找到你的最佳队友，一起上分',
    gradient: 'linear-gradient(135deg, #00ff9f 0%, #6366f1 100%)',
  },
  {
    title: '高效代打服务',
    desc: '专业代打团队，快速上分，安全可靠',
    gradient: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
  },
  {
    title: '新用户专享优惠',
    desc: '注册即送新人礼包，首单立享折扣',
    gradient: 'linear-gradient(135deg, #f43f5e 0%, #ec4899 100%)',
  },
]

const gameList = [
  { name: 'LOL', icon: '🎮', color: 'linear-gradient(135deg, #1a237e, #283593)' },
  { name: 'DOTA2', icon: '⚔️', color: 'linear-gradient(135deg, #b71c1c, #c62828)' },
  { name: 'CSGO', icon: '🔫', color: 'linear-gradient(135deg, #e65100, #ef6c00)' },
  { name: '王者荣耀', icon: '👑', color: 'linear-gradient(135deg, #4a148c, #6a1b9a)' },
  { name: '和平精英', icon: '🎯', color: 'linear-gradient(135deg, #1b5e20, #2e7d32)' },
  { name: '原神', icon: '🌟', color: 'linear-gradient(135deg, #0d47a1, #1565c0)' },
  { name: '永劫无间', icon: '🗡️', color: 'linear-gradient(135deg, #263238, #37474f)' },
  { name: 'APEX', icon: '🦅', color: 'linear-gradient(135deg, #bf360c, #d84315)' },
]

function goToGameService(gameType: string) {
  router.push({ path: '/service', query: { gameType } })
}

async function fetchRecommendServices() {
  loading.value = true
  try {
    const res = await get<PageData<Service>>('/services', {
      page: 1,
      size: 6,
      sort: 'rating',
    })
    services.value = res.data.records
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

async function fetchActivities() {
  try {
    const res = await getActiveActivities()
    activities.value = res.data || []
  } catch {
    // ignore
  }
}

function activityGradient(id: number) {
  const gradients = [
    'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)',
    'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
    'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)',
    'linear-gradient(135deg, #ec4899 0%, #db2777 100%)',
  ]
  return gradients[(id - 1) % gradients.length]
}

function formatActivityTime(endTime: string) {
  const end = new Date(endTime)
  const now = new Date()
  const diff = end.getTime() - now.getTime()
  if (diff <= 0) return '已'
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  if (days > 0) return `${days}天${hours}小时后`
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  return `${hours}小时${minutes}分钟后`
}

onMounted(() => {
  fetchRecommendServices()
  fetchActivities()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.home-page {
  min-height: 100vh;
  position: relative;
}

.banner-section {
  margin: -24px -20px 0;
  position: relative;
  z-index: 1;
  :deep(.el-carousel__item) {
    border-radius: 0 0 $border-radius-xl $border-radius-xl;
    overflow: hidden;
  }
  :deep(.el-carousel__indicators) {
    bottom: 32px;
    .el-carousel__indicator {
      .el-carousel__button {
        width: 24px;
        height: 4px;
        border-radius: 2px;
        background: rgba(255, 255, 255, 0.3);
        opacity: 1;
        transition: all $transition-normal;
      }
      &.is-active .el-carousel__button {
        width: 48px;
        background: $primary-color;
        box-shadow: 0 0 10px rgba($primary-color, 0.8);
      }
    }
  }
  :deep(.el-carousel__arrow) {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.3);
    backdrop-filter: blur(8px);
    &:hover {
      background: rgba($primary-color, 0.8);
      box-shadow: 0 0 20px rgba($primary-color, 0.8);
    }
  }
}

.banner-item {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 30% 50%, rgba(255, 255, 255, 0.1), transparent 70%);
  pointer-events: none;
  z-index: 1;
}

.banner-grid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(255, 255, 255, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.1) 1px, transparent 1px);
  background-size: 30px 30px;
  pointer-events: none;
  z-index: 2;
}

.banner-content {
  text-align: center;
  color: $text-primary;
  position: relative;
  z-index: 3;
  max-width: 800px;
  padding: 0 20px;
}

.banner-title {
  font-size: 56px;
  font-weight: $font-weight-black;
  margin-bottom: $spacing-lg;
  text-shadow: 0 4px 16px rgba(0, 0, 0, 0.5);
  letter-spacing: -1px;
  font-family: $font-family-heading;
  background: linear-gradient(135deg, $text-primary, rgba($text-primary, 0.8));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: fadeIn 1s ease-out;
}

.banner-desc {
  font-size: 20px;
  margin-bottom: $spacing-2xl;
  opacity: 0.9;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
  font-family: $font-family-primary;
  animation: slideInUp 1s ease-out 0.2s both;
}

.banner-btn {
  height: 56px;
  padding: 0 40px;
  font-size: 18px;
  font-weight: $font-weight-bold;
  background: linear-gradient(135deg, $primary-color, $primary-dark);
  border: 1px solid $primary-color;
  transition: all $transition-bounce;
  font-family: $font-family-primary;
  animation: slideInUp 1s ease-out 0.4s both;

  &:hover {
    background: linear-gradient(135deg, $primary-light, $primary-color);
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba($primary-color, 0.4), $shadow-glow-lg;
  }
}

.btn-icon {
  margin-left: 8px;
  transition: transform $transition-fast;
}

.banner-btn:hover .btn-icon {
  transform: translateX(8px) rotate(90deg);
}

.banner-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 2;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba($primary-color, 0.2);
  animation: pulse 4s ease-in-out infinite;
}

.deco-1 {
  width: 400px;
  height: 400px;
  right: -100px;
  top: -100px;
  animation-delay: 0s;
}

.deco-2 {
  width: 250px;
  height: 250px;
  left: -50px;
  bottom: -50px;
  animation-delay: 1s;
}

.deco-3 {
  width: 200px;
  height: 200px;
  right: 20%;
  bottom: 10%;
  animation-delay: 2s;
}

.deco-grid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba($primary-color, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba($primary-color, 0.1) 1px, transparent 1px);
  background-size: 50px 50px;
  pointer-events: none;
}

.game-section,
.service-section {
  padding: $spacing-2xl 0;
  position: relative;
  z-index: 1;
}

.activity-section {
  padding: $spacing-2xl 0;
  position: relative;
  z-index: 1;
}

.section-header {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-xl;
  flex-wrap: wrap;
  justify-content: space-between;
}

.section-header-left {
  display: flex;
  align-items: baseline;
  gap: $spacing-md;
}

.section-title {
  font-size: 32px;
  font-weight: $font-weight-bold;
  color: $text-primary;
  display: flex;
  align-items: center;
  gap: 12px;
  font-family: $font-family-heading;
  background: linear-gradient(135deg, $text-primary, $text-secondary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.section-icon {
  color: $primary-color;
  font-size: 24px;
  box-shadow: 0 0 15px rgba($primary-color, 0.5);
  animation: glow 2s ease-in-out infinite alternate;
}

.section-subtitle {
  color: $text-secondary;
  font-size: 16px;
  font-family: $font-family-primary;
  margin-top: 4px;
}

.view-more-btn {
  font-weight: $font-weight-medium;
  font-size: 14px;
  font-family: $font-family-primary;
  transition: all $transition-normal;
  &:hover {
    transform: translateY(-2px);
  }
}

.game-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: $spacing-lg;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: $spacing-lg;
}

.activity-card {
  background: $bg-card;
  border: 1px solid $border-color;
  border-radius: $border-radius-lg;
  overflow: hidden;
  transition: all $transition-bounce;
  position: relative;
}

.activity-banner {
  height: 150px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.activity-banner-grid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(255, 255, 255, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.1) 1px, transparent 1px);
  background-size: 20px 20px;
  pointer-events: none;
  z-index: 1;
}

.activity-badge-wrap {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 2;
}

.activity-type-tag {
  font-family: $font-family-primary;
  font-weight: $font-weight-bold;
  border-radius: $border-radius;
  padding: 4px 12px;
  font-size: 12px;
  box-shadow: 0 0 10px rgba($danger-color, 0.5);
}

.activity-banner-content {
  text-align: center;
  z-index: 2;
}

.activity-discount-num {
  font-size: 48px;
  font-weight: $font-weight-black;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);
  font-family: $font-family-heading;
}

.activity-body {
  padding: $spacing-lg;
}

.activity-title {
  font-size: 18px;
  font-weight: $font-weight-bold;
  color: $text-primary;
  margin-bottom: $spacing-sm;
  font-family: $font-family-primary;
}

.activity-desc {
  font-size: 14px;
  color: $text-secondary;
  margin-bottom: $spacing-md;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: $font-family-primary;
}

.activity-time {
  font-size: 13px;
  color: $danger-color;
  font-weight: $font-weight-medium;
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: $font-family-primary;
}

.game-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-xl $spacing-lg;
  background: $glass-bg;
  backdrop-filter: blur($glass-blur);
  border: 1px solid $glass-border;
  border-radius: $border-radius-lg;
  cursor: pointer;
  transition: all $transition-bounce;
  position: relative;
  overflow: hidden;
  &:hover {
    transform: translateY(-8px) scale(1.05);
    box-shadow: $shadow-lg, $shadow-glow;
  }
}

.game-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all $transition-normal;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.3);
  &:hover {
    transform: scale(1.2) rotate(10deg);
  }
}

.game-emoji {
  font-size: 32px;
}

.game-name {
  font-size: 16px;
  font-weight: $font-weight-bold;
  color: $text-primary;
  font-family: $font-family-primary;
  text-align: center;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: $spacing-lg;
  min-height: 200px;
}

.service-card-wrapper {
  min-height: 0;
  animation: slideInUp 0.6s ease-out;
  &:nth-child(2) {
    animation-delay: 0.1s;
  }
  &:nth-child(3) {
    animation-delay: 0.2s;
  }
  &:nth-child(4) {
    animation-delay: 0.3s;
  }
  &:nth-child(5) {
    animation-delay: 0.4s;
  }
  &:nth-child(6) {
    animation-delay: 0.5s;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .banner-section {
    margin: -24px -16px 0;
  }
  
  .banner-item {
    height: 360px;
  }
  
  .banner-title {
    font-size: 36px;
  }

  .banner-desc {
    font-size: 16px;
  }

  .game-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: $spacing-md;
  }

  .game-card {
    padding: $spacing-lg $spacing-md;
  }

  .game-icon {
    width: 56px;
    height: 56px;
  }

  .game-emoji {
    font-size: 28px;
  }

  .game-name {
    font-size: 14px;
  }

  .service-grid {
    grid-template-columns: 1fr;
    gap: $spacing-md;
  }

  .activity-grid {
    grid-template-columns: 1fr;
    gap: $spacing-md;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .section-header-left {
    flex-direction: column;
    gap: $spacing-xs;
  }

  .section-title {
    font-size: 24px;
  }
}
</style>
