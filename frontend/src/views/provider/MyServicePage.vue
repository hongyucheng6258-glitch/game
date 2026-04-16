<template>
  <div class="my-service-page">
    <div class="page-header">
      <h2 class="page-title">我的服务</h2>
      <el-button type="primary" @click="$router.push('/provider/service/create')">
        <el-icon><Plus /></el-icon> 发布新服务
      </el-button>
    </div>

    <div v-loading="loading" class="service-list">
      <template v-if="services.length > 0">
        <el-card v-for="item in services" :key="item.id" shadow="never" class="service-item">
          <div class="service-item-body">
            <div class="service-item-cover" :style="{ background: getGradient(item.id) }">
              <el-tag
                class="cover-tag"
                size="small"
                :type="item.serviceType === 0 ? 'primary' : 'warning'"
              >
                {{ item.serviceType === 0 ? '陪玩' : '代打' }}
              </el-tag>
            </div>
            <div class="service-item-info">
              <h3 class="service-item-title">{{ item.title }}</h3>
              <div class="service-item-meta">
                <span class="game-type">{{ item.gameType }}</span>
                <span class="price">¥{{ item.price }}/小时</span>
              </div>
              <div class="service-item-stats">
                <span><el-icon><Star /></el-icon> {{ item.rating || '暂无' }}</span>
                <span>已售 {{ item.salesCount || 0 }}</span>
                <span>评价 {{ item.reviewCount || 0 }}</span>
              </div>
            </div>
            <div class="service-item-actions">
              <el-tag :type="serviceStatusType(item.status)" size="small">
                {{ serviceStatusLabel(item.status) }}
              </el-tag>
              <div class="action-buttons">
                <el-button size="small" @click="$router.push(`/provider/service/edit/${item.id}`)">
                  编辑
                </el-button>
                <el-button
                  v-if="item.status === 1 || item.status === 0"
                  size="small"
                  :type="item.status === 1 ? 'warning' : 'success'"
                  @click="handleToggleStatus(item)"
                >
                  {{ item.status === 1 ? '下架' : '上架' }}
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </template>
      <el-empty v-else description="暂无服务，点击上方按钮发布" />
    </div>

    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="total"
        :page-sizes="[10, 20, 30]"
        layout="total, sizes, prev, pager, next"
        background
        @size-change="fetchServices"
        @current-change="fetchServices"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Star } from '@element-plus/icons-vue'
import { get, put } from '@/api/request'
import type { Service } from '@/types/service'
import type { PageData } from '@/types/common'
import { toggleServiceStatus } from '@/api/service'

const loading = ref(false)
const services = ref<Service[]>([])
const total = ref(0)
const pagination = reactive({ page: 1, size: 10 })

function serviceStatusLabel(status: number) {
  const map: Record<number, string> = { 0: '已下架', 1: '上架中', 2: '待审核', 3: '已拒绝' }
  return map[status] || '未知'
}

function serviceStatusType(status: number) {
  const map: Record<number, string> = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }
  return map[status] || 'info'
}

const gradients = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
]

function getGradient(id: number) {
  return gradients[id % gradients.length]
}

async function fetchServices() {
  loading.value = true
  try {
    const res = await get<PageData<Service>>('/services', {
      page: pagination.page,
      size: pagination.size,
      providerOnly: true,
    })
    services.value = res.data.records
    total.value = res.data.total
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

async function handleToggleStatus(item: Service) {
  const action = item.status === 1 ? '下架' : '上架'
  try {
    await ElMessageBox.confirm(`确定要${action}该服务吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await toggleServiceStatus(item.id)
    ElMessage.success(`${action}成功`)
    fetchServices()
  } catch {
    // cancelled or error
  }
}

onMounted(() => {
  fetchServices()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.my-service-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  :deep(.el-button--primary) {
    background: linear-gradient(135deg, $primary-color, $neon-cyan);
    border: 1px solid rgba(0, 240, 255, 0.3);
    box-shadow: 0 2px 8px rgba(0, 240, 255, 0.2);
    font-weight: 600;
    letter-spacing: 0.5px;
    transition: all $transition-normal;

    &:hover {
      box-shadow:
        0 0 16px rgba(0, 240, 255, 0.4),
        0 0 32px rgba(0, 240, 255, 0.15);
      transform: translateY(-2px);
      border-color: rgba(0, 240, 255, 0.6);
    }
  }
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  font-family: 'Orbitron', sans-serif;
  color: $text-primary;
  margin: 0;
  background: linear-gradient(135deg, $neon-cyan 0%, $primary-light 50%, $neon-purple 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 2px;
  text-transform: uppercase;
  position: relative;
  padding-bottom: $spacing-md;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 120px;
    height: 2px;
    background: linear-gradient(90deg, $neon-cyan, $primary-light, $neon-purple);
    box-shadow: 0 0 12px rgba(0, 240, 255, 0.5), 0 0 24px rgba(191, 90, 242, 0.3);
    border-radius: 2px;
  }
}

.service-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  min-height: 300px;
}

.service-item {
  background: $glass-bg !important;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid $border-glow !important;
  border-radius: $border-radius-lg !important;
  transition: transform $transition-normal, box-shadow $transition-normal, border-color $transition-normal;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: linear-gradient(90deg, $neon-cyan, $primary-light, $neon-purple);
    opacity: 0;
    transition: opacity $transition-normal;
  }

  &:hover {
    transform: translateY(-3px);
    border-color: rgba(0, 240, 255, 0.35) !important;
    box-shadow:
      0 0 24px rgba(0, 240, 255, 0.12),
      0 0 48px rgba(99, 102, 241, 0.08),
      0 8px 28px rgba(0, 0, 0, 0.3);

    &::before {
      opacity: 1;
    }
  }

  :deep(.el-card__body) {
    padding: $spacing-lg;
  }
}

.service-item-body {
  display: flex;
  gap: $spacing-md;
  align-items: center;
}

.service-item-cover {
  width: 120px;
  height: 80px;
  border-radius: $border-radius;
  flex-shrink: 0;
  position: relative;
  display: flex;
  align-items: flex-start;
  padding: $spacing-sm;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  transition: all $transition-normal;

  .service-item:hover & {
    box-shadow:
      0 6px 20px rgba(0, 0, 0, 0.4),
      0 0 16px rgba(0, 240, 255, 0.15);
  }
}

.cover-tag {
  position: absolute;
  top: $spacing-sm;
  left: $spacing-sm;
  backdrop-filter: blur(4px);
  border: 1px solid rgba(0, 240, 255, 0.3);
  box-shadow: 0 0 8px rgba(0, 240, 255, 0.2);
}

.service-item-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.service-item-title {
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: all $transition-fast;

  .service-item:hover & {
    color: $neon-cyan;
    text-shadow: 0 0 10px rgba(0, 240, 255, 0.3);
  }
}

.service-item-meta {
  display: flex;
  gap: $spacing-md;
  align-items: center;
}

.game-type {
  font-size: 13px;
  color: $text-secondary;
}

.price {
  font-size: 16px;
  font-weight: 700;
  color: $neon-cyan;
  text-shadow: 0 0 10px rgba(0, 240, 255, 0.4);
  font-family: 'Orbitron', sans-serif;
}

.service-item-stats {
  display: flex;
  gap: $spacing-md;
  font-size: 13px;
  color: $text-muted;

  span {
    display: flex;
    align-items: center;
    gap: 4px;
    transition: color $transition-fast;

    &:hover {
      color: $neon-cyan;
      text-shadow: 0 0 6px rgba(0, 240, 255, 0.3);
    }
  }
}

.service-item-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: $spacing-sm;
  flex-shrink: 0;

  :deep(.el-tag) {
    border: 1px solid rgba(0, 240, 255, 0.2);
    backdrop-filter: blur(4px);

    &.el-tag--success {
      background: rgba(34, 197, 94, 0.15);
      border-color: rgba(34, 197, 94, 0.4);
      color: $neon-green;
      box-shadow: 0 0 8px rgba(57, 255, 20, 0.15);
    }

    &.el-tag--warning {
      background: rgba(245, 158, 11, 0.15);
      border-color: rgba(245, 158, 11, 0.4);
      color: $neon-yellow;
      box-shadow: 0 0 8px rgba(255, 230, 0, 0.15);
    }

    &.el-tag--info {
      background: rgba(99, 102, 241, 0.15);
      border-color: rgba(99, 102, 241, 0.4);
      color: $primary-light;
      box-shadow: 0 0 8px rgba(99, 102, 241, 0.15);
    }

    &.el-tag--danger {
      background: rgba(239, 68, 68, 0.15);
      border-color: rgba(239, 68, 68, 0.4);
      color: $neon-pink;
      box-shadow: 0 0 8px rgba(255, 45, 120, 0.15);
    }
  }
}

.action-buttons {
  display: flex;
  gap: $spacing-xs;

  :deep(.el-button--warning) {
    background: linear-gradient(135deg, $warning-color, #fbbf24);
    border: 1px solid rgba(255, 230, 0, 0.3);
    box-shadow: 0 2px 6px rgba(245, 158, 11, 0.3);
    transition: all $transition-normal;

    &:hover {
      box-shadow: 0 0 12px rgba(255, 230, 0, 0.4), 0 0 24px rgba(245, 158, 11, 0.15);
      transform: translateY(-1px);
      border-color: rgba(255, 230, 0, 0.6);
    }
  }

  :deep(.el-button--success) {
    background: linear-gradient(135deg, $success-color, #4ade80);
    border: 1px solid rgba(57, 255, 20, 0.3);
    box-shadow: 0 2px 6px rgba(34, 197, 94, 0.3);
    transition: all $transition-normal;

    &:hover {
      box-shadow: 0 0 12px rgba(57, 255, 20, 0.4), 0 0 24px rgba(34, 197, 94, 0.15);
      transform: translateY(-1px);
      border-color: rgba(57, 255, 20, 0.6);
    }
  }

  :deep(.el-button--default) {
    background: rgba(51, 65, 85, 0.5);
    border: 1px solid $border-glow;
    color: $text-secondary;
    transition: all $transition-normal;

    &:hover {
      background: rgba(0, 240, 255, 0.08);
      color: $neon-cyan;
      border-color: rgba(0, 240, 255, 0.4);
      box-shadow: 0 0 12px rgba(0, 240, 255, 0.15);
      transform: translateY(-1px);
    }
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .service-item-body {
    flex-direction: column;
    align-items: stretch;
  }

  .service-item-cover {
    width: 100%;
    height: 100px;
  }

  .service-item-actions {
    align-items: flex-start;
  }
}
</style>
