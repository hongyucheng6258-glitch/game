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
  transition: all 0.2s;
  cursor: pointer;

  &:hover {
    background: $bg-hover;
    transform: translateX(4px);
  }

  &.top-three {
    background: rgba(99, 102, 241, 0.05);
    border-radius: $border-radius;
    margin-bottom: $spacing-xs;
    border-bottom: none;
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

  &.rank-1 {
    background: linear-gradient(135deg, #fbbf24, #f59e0b);
    color: white;
  }

  &.rank-2 {
    background: linear-gradient(135deg, #d1d5db, #9ca3af);
    color: white;
  }

  &.rank-3 {
    background: linear-gradient(135deg, #d97706, #b45309);
    color: white;
  }
}

.rank-number {
  font-size: 16px;
  font-weight: 700;
}

.ranking-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
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
}

.value-number {
  font-size: 18px;
  font-weight: 700;
  color: $warning-color;
}

.value-label {
  font-size: 12px;
  color: $text-muted;
}
</style>
