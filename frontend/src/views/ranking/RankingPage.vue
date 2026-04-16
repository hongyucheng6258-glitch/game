<template>
  <div class="ranking-page">
    <h2 class="page-title">排行榜</h2>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="评分榜" name="rating" />
      <el-tab-pane label="销量榜" name="sales" />
      <el-tab-pane label="人气榜" name="popular" />
    </el-tabs>

    <div v-loading="loading" class="ranking-content">
      <el-card shadow="never">
        <RankingList
          :list="rankingData"
          :value-label="valueLabel"
        />
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { get } from '@/api/request'
import RankingList from '@/components/business/RankingList.vue'
import type { RankingItem } from '@/components/business/RankingList.vue'

const activeTab = ref('rating')
const loading = ref(false)
const rankingData = ref<RankingItem[]>([])

const valueLabel = computed(() => {
  const labels: Record<string, string> = {
    rating: '分',
    sales: '单',
    popular: '人气',
  }
  return labels[activeTab.value] || '分'
})

function handleTabChange() {
  fetchRanking()
}

async function fetchRanking() {
  loading.value = true
  try {
    const res = await get<any>(`/rankings/${activeTab.value}`, { limit: 50 })
    rankingData.value = (res.data || []).map((item: any) => {
      let value: number | string = 0
      
      if (activeTab.value === 'rating') {
        value = item.rating != null ? Number(item.rating).toFixed(1) : '0.0'
      } else if (activeTab.value === 'sales') {
        value = item.salesCount || 0
      } else if (activeTab.value === 'popular') {
        value = item.popularity || 0
      }
      
      return {
        id: item.id,
        name: item.providerName || item.name || '未知',
        avatar: item.providerAvatar || item.avatar,
        description: item.gameType || item.description || '',
        value,
      }
    })
  } catch {
    rankingData.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchRanking()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.ranking-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
  position: relative;
}

.page-title {
  font-family: 'Orbitron', sans-serif;
  font-size: 32px;
  font-weight: 900;
  margin: 0;
  background: linear-gradient(135deg, $neon-cyan 0%, $primary-light 50%, $neon-purple 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 3px;
  text-transform: uppercase;
  position: relative;
  display: inline-block;

  &::after {
    content: '';
    position: absolute;
    bottom: -6px;
    left: 0;
    width: 60%;
    height: 2px;
    background: linear-gradient(90deg, $neon-cyan, transparent);
    box-shadow: 0 0 8px rgba(0, 240, 255, 0.5);
  }
}

:deep(.el-tabs) {
  .el-tabs__header {
    background: $glass-bg;
    backdrop-filter: blur($glass-blur);
    -webkit-backdrop-filter: blur($glass-blur);
    border: 1px solid $glass-border;
    border-radius: $border-radius-lg;
    padding: 4px $spacing-md;
  }

  .el-tabs__item {
    color: $text-secondary;
    font-weight: 600;
    letter-spacing: 0.5px;
    transition: all $transition-normal;

    &.is-active {
      color: $neon-cyan;
      text-shadow: 0 0 12px rgba(0, 240, 255, 0.6), 0 0 24px rgba(0, 240, 255, 0.3);
    }

    &:hover {
      color: $neon-cyan;
      text-shadow: 0 0 8px rgba(0, 240, 255, 0.3);
    }
  }

  .el-tabs__active-bar {
    background: $neon-cyan;
    border-radius: 2px;
    box-shadow: 0 0 10px rgba(0, 240, 255, 0.6), 0 0 20px rgba(0, 240, 255, 0.3);
  }

  .el-tabs__nav-wrap::after {
    display: none;
  }
}

.ranking-content {
  min-height: 400px;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: $border-radius-xl;
    background-image:
      linear-gradient(rgba(0, 240, 255, 0.03) 1px, transparent 1px),
      linear-gradient(90deg, rgba(0, 240, 255, 0.03) 1px, transparent 1px);
    background-size: 40px 40px;
    pointer-events: none;
    z-index: 0;
  }

  :deep(.el-card) {
    background: $glass-bg;
    backdrop-filter: blur($glass-blur);
    -webkit-backdrop-filter: blur($glass-blur);
    border: 1px solid $glass-border;
    border-radius: $border-radius-xl;
    box-shadow: $shadow-glow;
    transition: transform $transition-normal, box-shadow $transition-normal;
    position: relative;
    z-index: 1;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 0 30px rgba(0, 240, 255, 0.15), 0 8px 32px rgba(0, 0, 0, 0.3);
      border-color: rgba(0, 240, 255, 0.15);
    }
  }
}
</style>
