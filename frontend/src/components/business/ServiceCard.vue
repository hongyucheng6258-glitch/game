<template>
  <el-card class="service-card card-hover" @click="$router.push(`/service/${service.id}`)">
    <div class="service-cover" :style="{ background: coverGradient }">
      <div class="cover-badges">
        <el-tag class="service-type-tag" size="small" :type="service.serviceType === 0 ? 'primary' : 'warning'">
          {{ service.serviceType === 0 ? '陪玩' : '代打' }}
        </el-tag>
        <div v-if="showRankingBadge" class="ranking-badge effect-glow-border">
          <el-icon><Trophy /></el-icon>
          <span>TOP</span>
        </div>
        <div v-if="service.activityId" class="activity-badge effect-glow-border">
          <el-icon><Promotion /></el-icon>
          <span>活动</span>
        </div>
      </div>
      <span class="game-type glass-effect">{{ service.gameType }}</span>
      <div class="cover-grid"></div>
    </div>
    <div class="service-info">
      <h3 class="service-title">{{ service.title }}</h3>
      <div class="service-meta">
        <span class="price">
          <template v-if="service.activityId && service.activityPrice">
            <span class="original-price-small">¥{{ service.price }}</span>
            <span class="current-price gradient-text">¥{{ service.activityPrice }}</span>
          </template>
          <template v-else>
            <span class="current-price gradient-text">¥{{ service.price }}</span>
          </template>
          <span class="price-unit">/小时</span>
        </span>
        <span class="duration glass-effect">{{ service.duration }}分钟</span>
      </div>
      <div class="service-stats">
        <span class="stat-item">
          <el-icon class="stat-icon"><Star /></el-icon>
          <span>{{ service.rating || '暂无' }}</span>
        </span>
        <span class="stat-item">
          <el-icon class="stat-icon"><TrendCharts /></el-icon>
          <span>已售 {{ service.salesCount || 0 }}</span>
        </span>
        <span class="stat-item">
          <el-icon class="stat-icon"><ChatLineSquare /></el-icon>
          <span>评价 {{ service.reviewCount || 0 }}</span>
        </span>
      </div>
      <div v-if="service.activityId && service.activityTitle" class="activity-info">
        <el-tag type="danger" size="small" effect="dark" class="activity-tag">{{ service.activityTitle }}</el-tag>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Star, Trophy, Promotion, TrendCharts, ChatLineSquare } from '@element-plus/icons-vue'
import type { Service } from '@/types/service'

const props = defineProps<{ service: Service }>()

const gradients = [
  'linear-gradient(135deg, #00ff9f 0%, #6366f1 100%)',
  'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
  'linear-gradient(135deg, #f43f5e 0%, #ec4899 100%)',
  'linear-gradient(135deg, #10b981 0%, #06b6d4 100%)',
  'linear-gradient(135deg, #f59e0b 0%, #ef4444 100%)',
  'linear-gradient(135deg, #8b5cf6 0%, #ec4899 100%)',
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
  transition: all $transition-bounce;
  border-radius: $border-radius-lg;
  overflow: hidden;
  border: 1px solid $border-color;
  position: relative;
  background: $bg-card;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: linear-gradient(90deg, $primary-color, $secondary-color, $primary-color);
    transform: scaleX(0);
    transition: transform $transition-normal;
  }

  &:hover {
    transform: translateY(-8px) scale(1.02);
    box-shadow: $shadow-lg, $shadow-glow;
    border-color: rgba($primary-color, 0.3);
    &::before {
      transform: scaleX(1);
    }
  }

  :deep(.el-card__body) {
    padding: 0;
  }
}

.service-cover {
  height: 180px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding: 16px;
  position: relative;
  overflow: hidden;
  transition: all $transition-normal;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 80px;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
    pointer-events: none;
  }

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, rgba($primary-color, 0.1), rgba($secondary-color, 0.1));
    pointer-events: none;
  }
}

.cover-grid {
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

.cover-badges {
  position: absolute;
  top: 16px;
  left: 16px;
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  z-index: 2;
}

.service-type-tag {
  position: relative;
  top: auto;
  left: auto;
  font-family: $font-family-primary;
  font-weight: $font-weight-medium;
  border-radius: $border-radius;
  padding: 4px 12px;
  font-size: 12px;
  &.el-tag--primary {
    background: rgba($primary-color, 0.2);
    border-color: $primary-color;
    color: $primary-color;
  }
  &.el-tag--warning {
    background: rgba($warning-color, 0.2);
    border-color: $warning-color;
    color: $warning-color;
  }
}

.ranking-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  color: $text-primary;
  padding: 4px 12px;
  border-radius: $border-radius;
  font-size: 12px;
  font-weight: $font-weight-bold;
  font-family: $font-family-heading;
  box-shadow: 0 0 15px rgba(#f59e0b, 0.5);
  transition: all $transition-normal;
  &:hover {
    transform: scale(1.05);
    box-shadow: 0 0 20px rgba(#f59e0b, 0.8);
  }
}

.activity-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: $text-primary;
  padding: 4px 12px;
  border-radius: $border-radius;
  font-size: 12px;
  font-weight: $font-weight-bold;
  font-family: $font-family-heading;
  box-shadow: 0 0 15px rgba(#ef4444, 0.5);
  transition: all $transition-normal;
  &:hover {
    transform: scale(1.05);
    box-shadow: 0 0 20px rgba(#ef4444, 0.8);
  }
}

.game-type {
  color: $text-primary;
  font-size: 13px;
  font-weight: $font-weight-medium;
  padding: 6px 12px;
  border-radius: $border-radius;
  z-index: 2;
  backdrop-filter: blur(8px);
  font-family: $font-family-primary;
}

.service-info {
  padding: 20px;
  position: relative;
  z-index: 1;
}

.service-title {
  font-size: 17px;
  font-weight: $font-weight-bold;
  color: $text-primary;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: all $transition-normal;
  font-family: $font-family-primary;
  &:hover {
    color: $primary-color;
    text-shadow: 0 0 10px rgba($primary-color, 0.4);
  }
}

.service-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-family: $font-family-heading;

  .original-price-small {
    font-size: 14px;
    text-decoration: line-through;
    color: $text-muted;
    font-weight: $font-weight-normal;
  }

  .current-price {
    font-size: 24px;
    font-weight: $font-weight-black;
    text-shadow: 0 0 10px rgba($primary-color, 0.4);
  }

  .price-unit {
    font-size: 13px;
    font-weight: $font-weight-normal;
    color: $text-muted;
    font-family: $font-family-primary;
  }
}

.duration {
  color: $text-primary;
  font-size: 13px;
  font-weight: $font-weight-medium;
  padding: 4px 10px;
  border-radius: $border-radius;
  font-family: $font-family-primary;
}

.service-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 12px;
  font-family: $font-family-primary;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: $text-secondary;
  font-size: 13px;
  font-weight: $font-weight-medium;
  transition: all $transition-normal;
  &:hover {
    color: $primary-color;
    transform: translateY(-2px);
  }
}

.stat-icon {
  font-size: 14px;
  color: $primary-color;
}

.activity-info {
  margin-top: 12px;
}

.activity-tag {
  font-family: $font-family-primary;
  font-weight: $font-weight-medium;
  border-radius: $border-radius;
  padding: 4px 12px;
  font-size: 12px;
  &::before {
    content: '';
    position: absolute;
    top: -1px;
    left: -1px;
    right: -1px;
    bottom: -1px;
    background: linear-gradient(135deg, #ef4444, #dc2626);
    border-radius: $border-radius;
    z-index: -1;
    animation: glow 2s ease-in-out infinite alternate;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .service-cover {
    height: 150px;
    padding: 12px;
  }
  
  .service-info {
    padding: 16px;
  }
  
  .service-title {
    font-size: 15px;
  }
  
  .current-price {
    font-size: 20px;
  }
  
  .service-stats {
    gap: 16px;
  }
}
</style>
