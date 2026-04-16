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

// --- 交错入场动画 ---
@keyframes card-stagger-in {
  from {
    opacity: 0;
    transform: translateY(40px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

// --- 霓虹脉冲动画 ---
@keyframes neon-glow-pulse {
  0%, 100% {
    box-shadow: 0 0 8px rgba($neon-cyan, 0.3), 0 0 20px rgba($neon-cyan, 0.1);
  }
  50% {
    box-shadow: 0 0 14px rgba($neon-cyan, 0.5), 0 0 35px rgba($neon-cyan, 0.2);
  }
}

// --- 标题渐变动画 ---
@keyframes gradient-shift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.service-list-page {
  padding: $spacing-lg 0;
  position: relative;

  // 赛博网格背景
  &::before {
    content: '';
    position: fixed;
    inset: 0;
    background-image:
      linear-gradient(rgba($primary-color, 0.03) 1px, transparent 1px),
      linear-gradient(90deg, rgba($primary-color, 0.03) 1px, transparent 1px);
    background-size: 40px 40px;
    pointer-events: none;
    z-index: 0;
  }

  // 让所有子内容在网格之上
  > * {
    position: relative;
    z-index: 1;
  }
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
    transition: all $transition-normal;

    &:hover {
      border-color: $border-color-hover;
      box-shadow: $shadow-glow-strong;
    }
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
  font-family: 'Orbitron', 'Noto Sans SC', sans-serif;
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, $neon-cyan, $primary-light, $neon-purple);
  background-size: 200% 200%;
  animation: gradient-shift 4s ease infinite;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: none;
  filter: drop-shadow(0 0 8px rgba($neon-cyan, 0.25));
}

// --- 筛选区域 ---
.filter-section {
  margin-bottom: $spacing-md;

  :deep(.el-card) {
    background: $glass-bg;
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
    border: 1px solid $glass-border;
    border-radius: $border-radius-lg;
    box-shadow: $shadow-glow;
    transition: all $transition-normal;

    &:hover {
      border-color: $border-color-hover;
    }
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
  font-family: 'Rajdhani', 'Noto Sans SC', sans-serif;
  font-size: 13px;
  color: $text-muted;
  font-weight: 600;
  letter-spacing: 1.5px;
  text-transform: uppercase;
}

// --- 搜索框霓虹边框 focus ---
:deep(.el-input__wrapper) {
  background-color: $bg-input !important;
  box-shadow: 0 0 0 1px $border-color inset !important;
  transition: all $transition-normal !important;

  &:hover {
    box-shadow: 0 0 0 1px $border-color-hover inset !important;
  }

  &.is-focus {
    box-shadow:
      0 0 0 1px $neon-cyan inset,
      0 0 0 3px rgba($neon-cyan, 0.15),
      0 0 20px rgba($neon-cyan, 0.1) !important;
    border-color: transparent !important;
  }
}

// --- 筛选标签/按钮 active 状态 ---
:deep(.el-radio-button) {
  .el-radio-button__inner {
    background: $bg-elevated;
    border-color: $border-color;
    color: $text-secondary;
    font-weight: 500;
    transition: all $transition-normal;
    box-shadow: none !important;
  }

  &.is-active {
    .el-radio-button__inner {
      background: rgba($neon-cyan, 0.12);
      border-color: $neon-cyan;
      color: $neon-cyan;
      font-weight: 700;
      box-shadow:
        0 0 8px rgba($neon-cyan, 0.3),
        0 0 20px rgba($neon-cyan, 0.1),
        inset 0 0 12px rgba($neon-cyan, 0.05) !important;
      animation: neon-glow-pulse 2.5s ease-in-out infinite;
    }
  }
}

// --- Select 下拉框 focus ---
:deep(.el-select__wrapper) {
  &.is-focus {
    box-shadow:
      0 0 0 1px $neon-cyan inset,
      0 0 0 3px rgba($neon-cyan, 0.15),
      0 0 20px rgba($neon-cyan, 0.1) !important;
  }
}

// --- InputNumber focus ---
:deep(.el-input-number) {
  .el-input__wrapper.is-focus {
    box-shadow:
      0 0 0 1px $neon-cyan inset,
      0 0 0 3px rgba($neon-cyan, 0.15),
      0 0 20px rgba($neon-cyan, 0.1) !important;
  }
}

.price-inputs {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.price-separator {
  color: $text-muted;
  font-family: 'Orbitron', monospace;
  font-weight: 600;
}

// --- 排序栏 ---
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
  transition: all $transition-normal;

  &:hover {
    border-color: $border-color-hover;
  }

  // 排序按钮 active 状态
  :deep(.el-radio-button.is-active) {
    .el-radio-button__inner {
      background: rgba($neon-cyan, 0.12);
      border-color: $neon-cyan;
      color: $neon-cyan;
      font-weight: 700;
      box-shadow:
        0 0 8px rgba($neon-cyan, 0.3),
        0 0 20px rgba($neon-cyan, 0.1),
        inset 0 0 12px rgba($neon-cyan, 0.05) !important;
      animation: neon-glow-pulse 2.5s ease-in-out infinite;
    }
  }
}

.result-count {
  font-family: 'Rajdhani', 'Noto Sans SC', sans-serif;
  color: $text-muted;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.5px;

  // 数字高亮
  :deep(&) {
    // 使用伪元素无法生效，直接给文字加样式
  }
}

// --- 服务列表 ---
.list-section {
  min-height: 400px;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: $spacing-md;
  min-height: 300px;
}

// --- 服务卡片交错入场动画 ---
.service-card-wrapper {
  min-height: 0;
  opacity: 0;
  animation: card-stagger-in 0.5s cubic-bezier(0.4, 0, 0.2, 1) forwards;
  transition: transform $transition-normal, box-shadow $transition-normal;

  // 交错延迟：每张卡片递增 60ms
  @for $i from 1 through 36 {
    &:nth-child(#{$i}) {
      animation-delay: #{$i * 0.06}s;
    }
  }

  &:hover {
    transform: translateY(-6px) scale(1.02);
    box-shadow:
      $shadow-glow-strong,
      0 0 30px rgba($neon-cyan, 0.08);
  }
}

// --- 分页器样式优化 ---
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: $spacing-xl;
  padding: $spacing-md 0;

  :deep(.el-pagination) {
    --el-pagination-bg-color: transparent;
    --el-pagination-text-color: #{$text-secondary};
    --el-pagination-button-bg-color: #{$bg-elevated};
    --el-pagination-hover-color: #{$neon-cyan};

    .btn-prev,
    .btn-next,
    .el-pager li {
      background: $bg-elevated !important;
      border: 1px solid $border-color;
      border-radius: $border-radius !important;
      color: $text-secondary !important;
      font-weight: 500;
      transition: all $transition-normal;
      min-width: 36px;
      margin: 0 2px;

      &:hover {
        border-color: $neon-cyan;
        color: $neon-cyan !important;
        box-shadow: 0 0 10px rgba($neon-cyan, 0.2);
      }

      &.is-active {
        background: rgba($neon-cyan, 0.15) !important;
        border-color: $neon-cyan;
        color: $neon-cyan !important;
        font-weight: 700;
        box-shadow:
          0 0 8px rgba($neon-cyan, 0.3),
          0 0 20px rgba($neon-cyan, 0.1);
      }
    }

    .el-pagination__total,
    .el-pagination__jump {
      color: $text-muted;
      font-family: 'Rajdhani', 'Noto Sans SC', sans-serif;
    }

    .el-pagination__sizes {
      .el-select .el-select__wrapper {
        background: $bg-elevated !important;
        border: 1px solid $border-color;
      }
    }

    // 跳转输入框
    .el-pagination__jump {
      .el-input__wrapper {
        background: $bg-elevated !important;
        border: 1px solid $border-color;
      }
    }
  }
}

// --- 响应式 ---
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

  .service-card-wrapper {
    @for $i from 1 through 36 {
      &:nth-child(#{$i}) {
        animation-delay: #{$i * 0.08}s;
      }
    }
  }
}
</style>
