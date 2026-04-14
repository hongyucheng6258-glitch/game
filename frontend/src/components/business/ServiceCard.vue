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
.service-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border-radius: 12px;
  overflow: hidden;
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
  }
  :deep(.el-card__body) {
    padding: 0;
  }
}
.service-cover {
  height: 140px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding: 12px;
  position: relative;
}
.cover-badges {
  position: absolute;
  top: 12px;
  left: 12px;
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
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
}
.activity-badge {
  display: flex;
  align-items: center;
  gap: 2px;
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}
.game-type {
  color: white;
  font-size: 12px;
  background: rgba(0,0,0,0.3);
  padding: 2px 8px;
  border-radius: 4px;
}
.service-info {
  padding: 16px;
}
.service-title {
  font-size: 15px;
  font-weight: 600;
  color: #f1f5f9;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.service-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.price {
  color: #f59e0b;
  font-size: 18px;
  font-weight: 700;
  .original-price-small {
    font-size: 12px;
    text-decoration: line-through;
    color: #94a3b8;
    font-weight: 400;
    margin-right: 4px;
  }
  .price-unit {
    font-size: 12px;
    font-weight: 400;
    color: #94a3b8;
  }
}
.duration {
  color: #94a3b8;
  font-size: 13px;
}
.service-stats {
  display: flex;
  gap: 16px;
  color: #94a3b8;
  font-size: 13px;
  span {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}
.activity-info {
  margin-top: 8px;
}
</style>
