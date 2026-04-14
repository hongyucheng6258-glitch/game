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
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: $text-primary;
  margin: 0;
  background: linear-gradient(135deg, $text-primary 0%, $primary-light 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 0.5px;
}

:deep(.el-tabs) {
  .el-tabs__header {
    background: $glass-bg;
    backdrop-filter: blur($glass-blur);
    -webkit-backdrop-filter: blur($glass-blur);
    border: 1px solid rgba(148, 163, 184, 0.06);
    border-radius: $border-radius-lg;
    padding: 4px $spacing-md;
  }

  .el-tabs__item {
    color: $text-secondary;
    transition: color $transition-fast;

    &.is-active {
      color: $primary-light;
      text-shadow: 0 0 12px rgba(99, 102, 241, 0.4);
    }

    &:hover {
      color: $primary-light;
    }
  }

  .el-tabs__active-bar {
    background: linear-gradient(90deg, $primary-color, $primary-light);
    border-radius: 2px;
    box-shadow: 0 0 8px rgba(99, 102, 241, 0.5);
  }

  .el-tabs__nav-wrap::after {
    display: none;
  }
}

.ranking-content {
  min-height: 400px;

  :deep(.el-card) {
    background: $glass-bg;
    backdrop-filter: blur($glass-blur);
    -webkit-backdrop-filter: blur($glass-blur);
    border: 1px solid rgba(148, 163, 184, 0.06);
    border-radius: $border-radius-xl;
    box-shadow: $shadow-glow;
    transition: transform $transition-normal, box-shadow $transition-normal;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 0 30px rgba(99, 102, 241, 0.2), 0 8px 32px rgba(0, 0, 0, 0.3);
    }
  }
}
</style>
