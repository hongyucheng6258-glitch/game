<template>
  <el-card class="service-card" shadow="hover" @click="$router.push(`/service/${service.id}`)">
    <div class="service-cover" :style="{ background: coverGradient }">
      <div class="cover-badges">
        <el-tag class="service-type-tag" size="small" :type="service.serviceType === 0 ? 'primary' : 'warning'">
          {{ service.serviceType === 0 ? '陪玩' : '代打' }}
        </el-tag>
        <div v-if="showRankingBadge" class="ranking-badge">
          <el-icon><Trophy /></el-icon>
          <span>TOP</span>
        </div>
        <div v-if="service.activityId" class="activity-badge">
          <el-icon><Promotion /></el-icon>
          <span>活动</span>
        </div>
      </div>
      <span class="game-type">{{ service.gameType }}</span>
    </div>
    <div class="service-info">
      <h3 class="service-title">{{ service.title }}</h3>
      <div class="service-meta">
        <span class="price">
          <template v-if="service.activityId && service.activityPrice">
            <span class="original-price-small">¥{{ service.price }}</span>
            ¥{{ service.activityPrice }}
          </template>
          <template v-else>¥{{ service.price }}</template>
          <span class="price-unit">/小时</span>
        </span>
        <span class="duration">{{ service.duration }}分钟</span>
      </div>
      <div class="service-stats">
        <span><el-icon><Star /></el-icon> {{ service.rating || '暂无' }}</span>
        <span>已售 {{ service.salesCount || 0 }}</span>
      </div>
      <div v-if="service.activityId && service.activityTitle" class="activity-info">
        <el-tag type="danger" size="small" effect="dark">{{ service.activityTitle }}</el-tag>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Star, Trophy, Promotion } from '@element-plus/icons-vue'
import type { Service } from '@/types/service'

const props = defineProps<{ service: Service }>()

const gradients = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
]

const coverGradient = computed(() => {
  const index = (props.service.id || 0) % gradients.length
  return gradients[index]
})

const showRankingBadge = computed(() => {
  const rating = Number(props.service.rating || 0)
  const sales = Number(props.service.salesCount || 0)
  return rating >= 4.5 || sales >= 50
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.service-card {
  cursor: pointer;
  transition: all $transition-normal;
  border-radius: $border-radius-lg;
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, 0.06);

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.4), $shadow-glow-strong;
    border-color: rgba($neon-cyan, 0.25);
  }

  :deep(.el-card__body) {
    padding: 0;
  }
}

.service-cover {
  height: 150px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding: 12px;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: repeating-linear-gradient(
      0deg,
      transparent,
      transparent 2px,
      rgba(0, 0, 0, 0.08) 2px,
      rgba(0, 0, 0, 0.08) 4px
    );
    pointer-events: none;
    animation: scanline 4s linear infinite;
    z-index: 1;
  }

  &::before {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 60px;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.3));
    pointer-events: none;
    z-index: 2;
  }
}

@keyframes scanline {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 0 100%;
  }
}

.cover-badges {
  position: absolute;
  top: 12px;
  left: 12px;
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
  z-index: 3;
}

.service-type-tag {
  position: relative;
  top: auto;
  left: auto;
}

.ranking-badge {
  display: flex;
  align-items: center;
  gap: 2px;
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 0 10px rgba(251, 191, 36, 0.4);
}

.activity-badge {
  display: flex;
  align-items: center;
  gap: 2px;
  background: $neon-pink;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 0 10px rgba($neon-pink, 0.4);
}

.game-type {
  color: white;
  font-size: 12px;
  background: $glass-bg;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid $glass-border;
  padding: 4px 10px;
  border-radius: 6px;
  z-index: 3;
}

.service-info {
  padding: 16px;
}

.service-title {
  font-size: 15px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color $transition-fast;
}

.service-card:hover .service-title {
  color: $neon-cyan;
}

.service-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.price {
  color: $neon-cyan;
  font-size: 18px;
  font-weight: 700;
  text-shadow: 0 0 8px rgba($neon-cyan, 0.5);

  .original-price-small {
    font-size: 12px;
    text-decoration: line-through;
    color: $text-muted;
    font-weight: 400;
    margin-right: 4px;
    text-shadow: none;
  }

  .price-unit {
    font-size: 12px;
    font-weight: 400;
    color: $text-muted;
    text-shadow: none;
  }
}

.duration {
  color: $text-muted;
  font-size: 13px;
}

.service-stats {
  display: flex;
  gap: 16px;
  color: $text-muted;
  font-size: 13px;

  span {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.activity-info {
  margin-top: 10px;
}
</style>
