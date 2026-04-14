<template>
  <div class="home-page">
    <section class="banner-section">
      <el-carousel height="400px" :interval="5000" arrow="hover">
        <el-carousel-item v-for="(banner, index) in banners" :key="index">
          <div class="banner-item" :style="{ background: banner.gradient }">
            <div class="banner-overlay"></div>
            <div class="banner-content">
              <h2 class="banner-title">{{ banner.title }}</h2>
              <p class="banner-desc">{{ banner.desc }}</p>
              <el-button type="primary" size="large" round class="banner-btn" @click="$router.push('/service')">
                立即体验
                <el-icon class="btn-icon"><ArrowRight /></el-icon>
              </el-button>
            </div>
            <div class="banner-decoration">
              <div class="deco-circle deco-1"></div>
              <div class="deco-circle deco-2"></div>
              <div class="deco-circle deco-3"></div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <section class="game-section">
      <div class="section-header">
        <div class="section-header-left">
          <h2 class="section-title">热门游戏</h2>
          <p class="section-subtitle">选择你喜欢的游戏，找到最合适的队友</p>
        </div>
      </div>
      <div class="game-grid">
        <div
          v-for="game in gameList"
          :key="game.name"
          class="game-card"
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
        <div v-for="activity in activities" :key="activity.id" class="activity-card">
          <div class="activity-banner" :style="{ background: activityGradient(activity.id) }">
            <div class="activity-badge-wrap">
              <el-tag type="danger" size="small" effect="dark">{{ activity.type === 0 ? '全场折扣' : activity.type === 1 ? '服务折扣' : '限时特价' }}</el-tag>
            </div>
            <div class="activity-banner-content">
              <span class="activity-discount-num">{{ activity.type === 2 ? '特价' : Math.round(activity.discountRate * 100) + '折' }}</span>
            </div>
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
          <h2 class="section-title">推荐服务</h2>
          <p class="section-subtitle">精选优质陪玩代打服务</p>
        </div>
        <el-button text type="primary" class="view-more-btn" @click="$router.push('/service')">
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
import { ArrowRight, Promotion, Timer } from '@element-plus/icons-vue'
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
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  },
  {
    title: '高效代打服务',
    desc: '专业代打团队，快速上分，安全可靠',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  },
  {
    title: '新用户专享优惠',
    desc: '注册即送新人礼包，首单立享折扣',
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
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
}

.banner-section {
  margin: -24px -20px 0;
  :deep(.el-carousel__item) {
    border-radius: 0 0 $border-radius-xl $border-radius-xl;
    overflow: hidden;
  }
  :deep(.el-carousel__indicators) {
    .el-carousel__indicator {
      .el-carousel__button {
        width: 24px;
        height: 4px;
        border-radius: 2px;
        background: rgba(255, 255, 255, 0.3);
        opacity: 1;
      }
      &.is-active .el-carousel__button {
        width: 36px;
        background: white;
      }
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
  background: radial-gradient(ellipse at 30% 50%, rgba(255, 255, 255, 0.05), transparent 70%);
  pointer-events: none;
}

.banner-content {
  text-align: center;
  color: #fff;
  position: relative;
  z-index: 2;
}

.banner-title {
  font-size: 42px;
  font-weight: 800;
  margin-bottom: $spacing-md;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  letter-spacing: -0.5px;
}

.banner-desc {
  font-size: 18px;
  margin-bottom: $spacing-xl;
  opacity: 0.9;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
}

.banner-btn {
  height: 48px;
  padding: 0 32px;
  font-size: 16px;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.25);
  transition: all $transition-normal;

  &:hover {
    background: rgba(255, 255, 255, 0.25);
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  }
}

.btn-icon {
  margin-left: 4px;
  transition: transform $transition-fast;
}

.banner-btn:hover .btn-icon {
  transform: translateX(4px);
}

.banner-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.deco-1 {
  width: 300px;
  height: 300px;
  right: -50px;
  top: -50px;
}

.deco-2 {
  width: 200px;
  height: 200px;
  left: -30px;
  bottom: -30px;
}

.deco-3 {
  width: 150px;
  height: 150px;
  right: 20%;
  bottom: 10%;
}

.game-section,
.service-section {
  padding: $spacing-xl 0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
  flex-wrap: wrap;
  justify-content: space-between;
}

.section-header-left {
  display: flex;
  align-items: baseline;
  gap: $spacing-md;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: $text-primary;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-icon {
  color: $danger-color;
}

.section-subtitle {
  color: $text-secondary;
  font-size: 14px;
}

.view-more-btn {
  font-weight: 500;
  font-size: 14px;
}

.game-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: $spacing-md;
}

.activity-section {
  padding: $spacing-xl 0;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: $spacing-md;
}

.activity-card {
  background: $bg-card;
  border: 1px solid rgba(148, 163, 184, 0.06);
  border-radius: $border-radius-lg;
  overflow: hidden;
  transition: all $transition-normal;

  &:hover {
    transform: translateY(-4px);
    box-shadow: $shadow-lg, $shadow-glow;
    border-color: rgba($primary-color, 0.2);
  }
}

.activity-banner {
  height: 130px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
}

.activity-badge-wrap {
  position: absolute;
  top: 12px;
  left: 12px;
}

.activity-banner-content {
  text-align: center;
}

.activity-discount-num {
  font-size: 40px;
  font-weight: 800;
  color: white;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.activity-body {
  padding: $spacing-md;
}

.activity-title {
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.activity-desc {
  font-size: 13px;
  color: $text-secondary;
  margin-bottom: $spacing-sm;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-time {
  font-size: 12px;
  color: $danger-color;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.game-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-lg $spacing-md;
  background: rgba(30, 41, 59, 0.6);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(148, 163, 184, 0.06);
  border-radius: $border-radius-lg;
  cursor: pointer;
  transition: all $transition-normal;

  &:hover {
    border-color: rgba($primary-color, 0.3);
    transform: translateY(-4px);
    box-shadow: $shadow-md, $shadow-glow;
  }
}

.game-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform $transition-normal;
}

.game-card:hover .game-icon {
  transform: scale(1.1);
}

.game-emoji {
  font-size: 28px;
}

.game-name {
  font-size: 14px;
  font-weight: 600;
  color: $text-primary;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: $spacing-md;
  min-height: 200px;
}

.service-card-wrapper {
  min-height: 0;
}

@media (max-width: 768px) {
  .banner-title {
    font-size: 28px;
  }

  .banner-desc {
    font-size: 14px;
  }

  .game-grid {
    grid-template-columns: repeat(4, 1fr);
  }

  .service-grid {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .section-header-left {
    flex-direction: column;
    gap: $spacing-xs;
  }
}
</style>
