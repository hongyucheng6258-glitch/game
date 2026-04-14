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
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: $text-primary;
  margin: 0;
}

.service-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  min-height: 300px;
}

.service-item {
  :deep(.el-card__body) {
    padding: $spacing-md;
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
}

.cover-tag {
  position: absolute;
  top: $spacing-sm;
  left: $spacing-sm;
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
  color: $warning-color;
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
  }
}

.service-item-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: $spacing-sm;
  flex-shrink: 0;
}

.action-buttons {
  display: flex;
  gap: $spacing-xs;
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
