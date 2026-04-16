<template>
  <div class="ranking-list">
    <div v-for="(item, index) in list" :key="item.id" class="ranking-item" :class="{ 'top-three': index < 3 }" @click="handleItemClick(item)">
      <div class="rank-badge" :class="`rank-${index + 1}`">
        <template v-if="index < 3">
          <el-icon :size="20"><Trophy /></el-icon>
        </template>
        <span v-else class="rank-number">{{ index + 1 }}</span>
      </div>
      <UserAvatar :avatar="item.avatar" :username="item.name" :size="40" />
      <div class="ranking-info">
        <span class="ranking-name">{{ item.name }}</span>
        <span class="ranking-desc">{{ item.description || '' }}</span>
      </div>
      <div class="ranking-value">
        <span class="value-number">{{ item.value }}</span>
        <span class="value-label">{{ valueLabel }}</span>
      </div>
    </div>
    <el-empty v-if="list.length === 0" description="暂无排行数据" />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Trophy } from '@element-plus/icons-vue'
import UserAvatar from './UserAvatar.vue'

const router = useRouter()

export interface RankingItem {
  id: number
  name: string
  avatar?: string
  description?: string
  value: number | string
}

withDefaults(defineProps<{
  list: RankingItem[]
  valueLabel?: string
}>(), {
  valueLabel: '分',
})

function handleItemClick(item: RankingItem) {
  router.push({
    name: 'ServiceList',
    query: { providerId: item.id }
  })
}
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.ranking-list {
  display: flex;
  flex-direction: column;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  border-bottom: 1px solid $border-color;
  transition: all $transition-normal;
  cursor: pointer;
  position: relative;

  &:hover {
    background: $bg-hover;
    transform: translateX(4px);

    .ranking-info {
      text-shadow: 0 0 8px rgba(0, 240, 255, 0.15);
    }
  }

  &.top-three {
    border-radius: $border-radius-lg;
    margin-bottom: $spacing-xs;
    border-bottom: none;
    position: relative;

    &::before {
      content: '';
      position: absolute;
      inset: 0;
      border-radius: $border-radius-lg;
      pointer-events: none;
      z-index: 0;
    }

    &:nth-child(1) {
      background: rgba(255, 215, 0, 0.06);
      border: 1px solid rgba(255, 215, 0, 0.12);

      &::before {
        background: linear-gradient(90deg, rgba(255, 215, 0, 0.04), transparent);
      }

      &:hover {
        box-shadow: 0 0 20px rgba(255, 215, 0, 0.15), inset 0 0 20px rgba(255, 215, 0, 0.03);
        border-color: rgba(255, 215, 0, 0.25);
      }
    }

    &:nth-child(2) {
      background: rgba(192, 192, 192, 0.06);
      border: 1px solid rgba(192, 192, 192, 0.12);

      &::before {
        background: linear-gradient(90deg, rgba(192, 192, 192, 0.04), transparent);
      }

      &:hover {
        box-shadow: 0 0 20px rgba(192, 192, 192, 0.15), inset 0 0 20px rgba(192, 192, 192, 0.03);
        border-color: rgba(192, 192, 192, 0.25);
      }
    }

    &:nth-child(3) {
      background: rgba(205, 127, 50, 0.06);
      border: 1px solid rgba(205, 127, 50, 0.12);

      &::before {
        background: linear-gradient(90deg, rgba(205, 127, 50, 0.04), transparent);
      }

      &:hover {
        box-shadow: 0 0 20px rgba(205, 127, 50, 0.15), inset 0 0 20px rgba(205, 127, 50, 0.03);
        border-color: rgba(205, 127, 50, 0.25);
      }
    }
  }
}

.rank-badge {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  flex-shrink: 0;
  background: $bg-hover;
  color: $text-muted;
  font-weight: 700;
  font-size: 14px;
  position: relative;
  z-index: 1;

  &.rank-1 {
    background: linear-gradient(135deg, #ffd700, #f59e0b);
    color: #1a1a2e;
    box-shadow: 0 0 12px rgba(255, 215, 0, 0.5), 0 0 24px rgba(255, 215, 0, 0.2);
    animation: glow-gold 2.5s ease-in-out infinite;
  }

  &.rank-2 {
    background: linear-gradient(135deg, #e8e8e8, #a8a8a8);
    color: #1a1a2e;
    box-shadow: 0 0 12px rgba(192, 192, 192, 0.5), 0 0 24px rgba(192, 192, 192, 0.2);
    animation: glow-silver 2.5s ease-in-out infinite;
  }

  &.rank-3 {
    background: linear-gradient(135deg, #cd7f32, #a0522d);
    color: #1a1a2e;
    box-shadow: 0 0 12px rgba(205, 127, 50, 0.5), 0 0 24px rgba(205, 127, 50, 0.2);
    animation: glow-bronze 2.5s ease-in-out infinite;
  }
}

@keyframes glow-gold {
  0%, 100% {
    box-shadow: 0 0 12px rgba(255, 215, 0, 0.5), 0 0 24px rgba(255, 215, 0, 0.2);
  }
  50% {
    box-shadow: 0 0 18px rgba(255, 215, 0, 0.7), 0 0 36px rgba(255, 215, 0, 0.3);
  }
}

@keyframes glow-silver {
  0%, 100% {
    box-shadow: 0 0 12px rgba(192, 192, 192, 0.5), 0 0 24px rgba(192, 192, 192, 0.2);
  }
  50% {
    box-shadow: 0 0 18px rgba(192, 192, 192, 0.7), 0 0 36px rgba(192, 192, 192, 0.3);
  }
}

@keyframes glow-bronze {
  0%, 100% {
    box-shadow: 0 0 12px rgba(205, 127, 50, 0.5), 0 0 24px rgba(205, 127, 50, 0.2);
  }
  50% {
    box-shadow: 0 0 18px rgba(205, 127, 50, 0.7), 0 0 36px rgba(205, 127, 50, 0.3);
  }
}

.rank-number {
  font-family: 'Orbitron', sans-serif;
  font-size: 16px;
  font-weight: 700;
}

.ranking-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  position: relative;
  z-index: 1;
  transition: text-shadow $transition-normal;
}

.ranking-name {
  font-size: 15px;
  font-weight: 600;
  color: $text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ranking-desc {
  font-size: 12px;
  color: $text-muted;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ranking-value {
  text-align: right;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.value-number {
  font-family: 'Orbitron', sans-serif;
  font-size: 18px;
  font-weight: 700;
  color: $neon-cyan;
  text-shadow: 0 0 8px rgba(0, 240, 255, 0.4);
}

.value-label {
  font-size: 12px;
  color: $text-muted;
}
</style>
