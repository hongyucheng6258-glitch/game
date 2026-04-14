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
  font-size: 24px;
  font-weight: 700;
  color: $text-primary;
  margin: 0;
}

:deep(.el-tabs__item) {
  color: $text-secondary;
  &.is-active {
    color: $primary-light;
  }
}

:deep(.el-tabs__active-bar) {
  background-color: $primary-color;
}

.ranking-content {
  min-height: 400px;
}
</style>
