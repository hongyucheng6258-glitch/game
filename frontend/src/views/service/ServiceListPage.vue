<template>
  <div class="service-list-page">
    <!-- 服务商标题（当查看特定服务商时显示） -->
    <section v-if="providerId" class="provider-header-section">
      <el-card shadow="never">
        <div class="provider-header">
          <div class="provider-info">
            <el-tag type="primary" size="large">服务商</el-tag>
            <h2 class="provider-name">{{ providerName || '未知服务商' }}</h2>
          </div>
          <el-button @click="handleClearProvider">
            <el-icon><Close /></el-icon>
            查看全部服务
          </el-button>
        </div>
      </el-card>
    </section>

    <!-- 筛选区域 -->
    <section class="filter-section">
      <el-card shadow="never">
        <div class="filter-row">
          <div class="filter-item">
            <span class="filter-label">搜索</span>
            <el-input
              v-model="filters.keyword"
              placeholder="搜索服务标题或描述"
              clearable
              style="width: 280px"
              @keyup.enter="handleFilter"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
          <div class="filter-item">
            <span class="filter-label">游戏类型</span>
            <el-select
              v-model="filters.gameType"
              placeholder="全部游戏"
              clearable
              @change="handleFilter"
            >
              <el-option
                v-for="game in GAME_TYPES"
                :key="game"
                :label="game"
                :value="game"
              />
            </el-select>
          </div>

          <div class="filter-item">
            <span class="filter-label">服务类型</span>
            <el-radio-group v-model="filters.serviceType" @change="handleFilter">
              <el-radio-button :value="-1">全部</el-radio-button>
              <el-radio-button :value="0">陪玩</el-radio-button>
              <el-radio-button :value="1">代打</el-radio-button>
            </el-radio-group>
          </div>

          <div class="filter-item">
            <span class="filter-label">价格范围</span>
            <div class="price-inputs">
              <el-input-number
                v-model="filters.minPrice"
                :min="0"
                :max="99999"
                placeholder="最低价"
                controls-position="right"
                size="default"
                @change="handleFilter"
              />
              <span class="price-separator">-</span>
              <el-input-number
                v-model="filters.maxPrice"
                :min="0"
                :max="99999"
                placeholder="最高价"
                controls-position="right"
                size="default"
                @change="handleFilter"
              />
            </div>
          </div>
          <div class="filter-item">
            <el-button type="primary" @click="handleReset">
              <el-icon><RefreshLeft /></el-icon>
              重置
            </el-button>
          </div>
        </div>
      </el-card>
    </section>

    <!-- 排序栏 -->
    <section class="sort-section">
      <div class="sort-bar">
        <el-radio-group v-model="sortBy" @change="handleSort">
          <el-radio-button value="default">综合排序</el-radio-button>
          <el-radio-button value="price_asc">价格升序</el-radio-button>
          <el-radio-button value="price_desc">价格降序</el-radio-button>
          <el-radio-button value="rating">评分排序</el-radio-button>
          <el-radio-button value="sales">销量排序</el-radio-button>
          <el-radio-button value="newest">最新发布</el-radio-button>
        </el-radio-group>
        <span class="result-count">共 {{ total }} 个服务</span>
      </div>
    </section>

    <!-- 服务列表 -->
    <section class="list-section">
      <div v-loading="loading" class="service-grid">
        <template v-if="services.length > 0">
          <div v-for="item in services" :key="item.id" class="service-card-wrapper">
            <ServiceCard :service="item" />
          </div>
        </template>
        <el-empty v-else description="暂无服务" />
      </div>

      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="total"
          :page-sizes="[12, 24, 36]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handlePageChange"
          @current-change="handlePageChange"
        />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get } from '@/api/request'
import type { Service } from '@/types/service'
import type { PageData } from '@/types/common'
import { GAME_TYPES } from '@/utils/constants'
import ServiceCard from '@/components/business/ServiceCard.vue'
import { Search, RefreshLeft, Close } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const services = ref<Service[]>([])
const total = ref(0)

const filters = reactive({
  keyword: '',
  gameType: '',
  serviceType: -1,
  minPrice: undefined as number | undefined,
  maxPrice: undefined as number | undefined,
})

const providerId = ref<number | undefined>(undefined)
const providerName = ref<string>('')

const sortBy = ref('default')
const pagination = reactive({
  page: 1,
  size: 12,
})

// 从 URL query 初始化筛选条件
onMounted(() => {
  if (route.query.gameType) {
    filters.gameType = route.query.gameType as string
  }
  if (route.query.providerId) {
    providerId.value = Number(route.query.providerId)
  }
  fetchServices()
})

watch(() => route.query.gameType, (val) => {
  if (val) {
    filters.gameType = val as string
    pagination.page = 1
    fetchServices()
  }
})

watch(() => route.query.providerId, (val) => {
  providerId.value = val ? Number(val) : undefined
  pagination.page = 1
  fetchServices()
})

function handleFilter() {
  pagination.page = 1
  fetchServices()
}

function handleSort() {
  pagination.page = 1
  fetchServices()
}

function handleClearProvider() {
  router.push({ name: 'ServiceList' })
}

function handleReset() {
  filters.keyword = ''
  filters.gameType = ''
  filters.serviceType = -1
  filters.minPrice = undefined
  filters.maxPrice = undefined
  sortBy.value = 'default'
  pagination.page = 1
  fetchServices()
}

function handlePageChange() {
  fetchServices()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

async function fetchServices() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: pagination.page,
      size: pagination.size,
    }
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.gameType) params.gameType = filters.gameType
    if (filters.serviceType !== -1) params.serviceType = filters.serviceType
    if (filters.minPrice !== undefined) params.minPrice = filters.minPrice
    if (filters.maxPrice !== undefined) params.maxPrice = filters.maxPrice
    if (sortBy.value !== 'default') params.sort = sortBy.value
    if (providerId.value) params.providerId = providerId.value

    const res = await get<PageData<Service>>('/services', params)
    services.value = res.data.records
    total.value = res.data.total
    
    // 如果是查看某个服务商的服务，显示服务商名称
    if (providerId.value && services.value.length > 0) {
      // 从第一个服务获取服务商名称（如果有的话）
      const firstService = services.value[0] as any
      if (firstService.providerName) {
        providerName.value = firstService.providerName
      }
    }
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.service-list-page {
  padding: $spacing-lg 0;
}

.provider-header-section {
  margin-bottom: $spacing-md;

  :deep(.el-card) {
    background: $glass-bg;
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
    border: 1px solid $glass-border;
    border-radius: $border-radius-lg;
    box-shadow: $shadow-glow;
  }
}

.provider-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-md;
}

.provider-info {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.provider-name {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: $text-primary;
  background: linear-gradient(135deg, $text-primary, $primary-light);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

// 筛选区域
.filter-section {
  margin-bottom: $spacing-md;

  :deep(.el-card) {
    background: $glass-bg;
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
    border: 1px solid $glass-border;
    border-radius: $border-radius-lg;
    box-shadow: $shadow-glow;
  }
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-lg;
  align-items: flex-end;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.filter-label {
  font-size: 14px;
  color: $text-secondary;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.price-inputs {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.price-separator {
  color: $text-muted;
}

// 排序栏
.sort-section {
  margin-bottom: $spacing-md;
}

.sort-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  background: $glass-bg;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid $glass-border;
  border-radius: $border-radius-lg;
}

.result-count {
  color: $text-secondary;
  font-size: 14px;
}

// 服务列表
.list-section {
  min-height: 400px;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: $spacing-md;
  min-height: 300px;
}

.service-card-wrapper {
  min-height: 0;
  transition: transform $transition-normal, box-shadow $transition-normal;

  &:hover {
    transform: translateY(-4px);
    box-shadow: $shadow-glow;
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: $spacing-xl;
}

// 响应式
@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
    gap: $spacing-md;
  }

  .sort-bar {
    flex-direction: column;
    align-items: flex-start;
  }

  .service-grid {
    grid-template-columns: 1fr;
  }
}
</style>
