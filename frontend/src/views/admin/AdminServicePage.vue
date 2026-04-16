<template>
  <div class="admin-service-page">
    <div class="page-header">
      <h2 class="page-title">服务管理</h2>
    </div>

    <!-- 搜索筛选 -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <el-input v-model="searchKeyword" placeholder="搜索服务标题" clearable style="width: 250px" @clear="onFilterChange" @keyup.enter="onFilterChange">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="filterGameType" placeholder="游戏类型" clearable style="width: 150px" @change="onFilterChange">
          <el-option v-for="game in GAME_TYPES" :key="game" :label="game" :value="game" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 150px" @change="onFilterChange">
          <el-option label="待审核" :value="2" />
          <el-option label="已拒绝" :value="3" />
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
        <el-button type="primary" @click="onFilterChange">搜索</el-button>
      </div>
    </el-card>

    <!-- 服务表格 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="filteredServices" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="服务标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="providerName" label="服务者" width="120" />
        <el-table-column prop="gameType" label="游戏" width="100" />
        <el-table-column prop="serviceType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.serviceType === 0 ? 'primary' : 'warning'" size="small">
              {{ row.serviceType === 0 ? '陪玩' : '代打' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span style="color: #f59e0b; font-weight: 600">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="80" />
        <el-table-column prop="salesCount" label="销量" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="serviceStatusType(row.status)" size="small">
              {{ serviceStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/service/${row.id}`)">查看</el-button>
            <el-button v-if="Number(row.status) === 2" size="small" type="success" @click="handleApprove(row)">
              通过
            </el-button>
            <el-button v-if="Number(row.status) === 2" size="small" type="danger" @click="handleReject(row)">
              拒绝
            </el-button>
            <el-button v-if="Number(row.status) === 1 || Number(row.status) === 0" size="small" type="warning" @click="handleToggleStatus(row)">
              {{ Number(row.status) === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="fetchServices"
          @current-change="fetchServices"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getAdminServices, adminToggleService, adminDeleteService, approveService, rejectService } from '@/api/admin'
import type { Service } from '@/types/service'
import { GAME_TYPES } from '@/utils/constants'
import { formatDate } from '@/utils/format'

const loading = ref(false)
const services = ref<Service[]>([])
const total = ref(0)
const searchKeyword = ref('')
const filterGameType = ref('')
const filterStatus = ref<number | undefined>()
const pagination = reactive({ page: 1, size: 10 })

const filteredServices = computed(() => {
  // 现在过滤由后端处理，直接返回所有数据
  return services.value
})

function serviceStatusLabel(status: number | string) {
  const s = Number(status)
  const map: Record<number, string> = { 0: '下架', 1: '上架', 2: '待审核', 3: '已拒绝' }
  return map[s] || '未知'
}

function serviceStatusType(status: number | string) {
  const s = Number(status)
  const map: Record<number, string> = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }
  return map[s] || 'info'
}

async function fetchServices() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: pagination.page, size: pagination.size }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterGameType.value) params.gameType = filterGameType.value
    if (filterStatus.value !== undefined) params.status = filterStatus.value
    const res = await getAdminServices(params)
    services.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

// 监听筛选状态变化，重置分页到第1页
function onFilterChange() {
  pagination.page = 1
  fetchServices()
}

async function handleApprove(service: Service) {
  try {
    await ElMessageBox.confirm('确定要审核通过该服务吗？通过后服务将可在平台显示。', '审核通过', { type: 'success' })
    await approveService(service.id)
    ElMessage.success('审核通过成功')
    fetchServices()
  } catch {
    // cancelled
  }
}

async function handleReject(service: Service) {
  try {
    await ElMessageBox.confirm('确定要拒绝该服务吗？', '审核拒绝', { type: 'warning' })
    await rejectService(service.id)
    ElMessage.success('已拒绝该服务')
    fetchServices()
  } catch {
    // cancelled
  }
}

async function handleToggleStatus(service: Service) {
  const action = service.status === 1 ? '下架' : '上架'
  try {
    await ElMessageBox.confirm(`确定要${action}该服务吗？`, '提示', { type: 'warning' })
    await adminToggleService(service.id)
    ElMessage.success(`${action}成功`)
    fetchServices()
  } catch {
    // cancelled
  }
}

async function handleDelete(service: Service) {
  try {
    await ElMessageBox.confirm('确定要删除该服务吗？此操作不可恢复。', '警告', { type: 'error' })
    await adminDeleteService(service.id)
    ElMessage.success('删除成功')
    fetchServices()
  } catch {
    // cancelled
  }
}

onMounted(() => fetchServices())
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.admin-service-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  font-family: 'Orbitron', 'Rajdhani', sans-serif;
  background: linear-gradient(135deg, $neon-cyan 0%, $primary-light 50%, $neon-purple 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 2px;
  text-transform: uppercase;
  position: relative;

  &::after {
    content: '';
    display: block;
    width: 120px;
    height: 2px;
    margin-top: $spacing-sm;
    background: linear-gradient(90deg, $neon-cyan, $primary-color, transparent);
    border-radius: 1px;
    box-shadow: 0 0 10px rgba($neon-cyan, 0.4);
  }
}

.filter-card {
  background: $glass-bg !important;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid $border-color !important;
  border-radius: $border-radius-lg !important;
  transition: all $transition-normal;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba($neon-cyan, 0.3), transparent);
    pointer-events: none;
  }

  &:hover {
    border-color: rgba($neon-cyan, 0.2) !important;
    box-shadow: $shadow-glow, 0 0 15px rgba($neon-cyan, 0.06);
  }

  :deep(.el-card__body) { padding: $spacing-md; }
}

.filter-row {
  display: flex;
  gap: $spacing-sm;
  align-items: center;
  flex-wrap: wrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: $spacing-md;
  padding-bottom: $spacing-sm;
}

// 卡片毛玻璃效果 + 霓虹边框
:deep(.el-card) {
  background: $glass-bg !important;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid $border-color !important;
  border-radius: $border-radius-lg !important;
  transition: all $transition-normal;

  &:hover {
    border-color: rgba($neon-cyan, 0.2) !important;
    box-shadow: $shadow-glow, 0 0 15px rgba($neon-cyan, 0.06);
  }
}

// 表格赛博朋克风格
:deep(.el-table) {
  --el-table-bg-color: #{$bg-dark};
  --el-table-header-bg-color: transparent;
  --el-table-row-hover-bg-color: rgba(0, 240, 255, 0.04);
  --el-table-border-color: rgba(0, 240, 255, 0.06);
  --el-table-text-color: #{$text-primary};
  --el-table-header-text-color: #{$neon-cyan};
  border-radius: $border-radius-lg;
  overflow: hidden;

  .el-table__row--striped {
    background-color: rgba($bg-elevated, 0.35) !important;
  }
}

:deep(.el-table__row) {
  background-color: $bg-dark !important;
  transition: all $transition-fast;

  &:hover {
    background-color: rgba(0, 240, 255, 0.04) !important;
    box-shadow: inset 0 0 30px rgba(0, 240, 255, 0.03);
  }
}

// 表头渐变背景 + 霓虹文字
:deep(.el-table__header-wrapper) {
  th {
    background: linear-gradient(135deg, rgba($neon-cyan, 0.06), rgba($primary-color, 0.04)) !important;
    color: $neon-cyan !important;
    font-weight: 600;
    font-size: 13px;
    text-transform: uppercase;
    letter-spacing: 1px;
    border-bottom: 1px solid rgba($neon-cyan, 0.12) !important;
  }
}

// 按钮霓虹发光效果
:deep(.el-button--primary) {
  background: linear-gradient(135deg, $neon-cyan, $primary-color) !important;
  border: none !important;
  color: #06080f !important;
  font-weight: 600;
  box-shadow: 0 2px 12px rgba($neon-cyan, 0.3);
  transition: all $transition-normal;

  &:hover {
    background: linear-gradient(135deg, lighten($neon-cyan, 5%), lighten($primary-color, 5%)) !important;
    box-shadow: 0 4px 20px rgba($neon-cyan, 0.5), 0 0 30px rgba($neon-cyan, 0.2);
    transform: translateY(-1px);
  }
}

:deep(.el-button--success) {
  background: linear-gradient(135deg, $neon-green, darken($neon-green, 15%)) !important;
  border: none !important;
  color: #06080f !important;
  font-weight: 600;
  box-shadow: 0 2px 12px rgba($neon-green, 0.3);
  transition: all $transition-normal;

  &:hover {
    box-shadow: 0 4px 20px rgba($neon-green, 0.5), 0 0 30px rgba($neon-green, 0.2);
    transform: translateY(-1px);
  }
}

:deep(.el-button--danger) {
  background: linear-gradient(135deg, $neon-pink, darken($neon-pink, 15%)) !important;
  border: none !important;
  color: #fff !important;
  font-weight: 600;
  box-shadow: 0 2px 12px rgba($neon-pink, 0.3);
  transition: all $transition-normal;

  &:hover {
    box-shadow: 0 4px 20px rgba($neon-pink, 0.5), 0 0 30px rgba($neon-pink, 0.2);
    transform: translateY(-1px);
  }
}

:deep(.el-button--warning) {
  background: linear-gradient(135deg, $neon-yellow, darken($neon-yellow, 15%)) !important;
  border: none !important;
  color: #06080f !important;
  font-weight: 600;
  box-shadow: 0 2px 12px rgba($neon-yellow, 0.3);
  transition: all $transition-normal;

  &:hover {
    box-shadow: 0 4px 20px rgba($neon-yellow, 0.5), 0 0 30px rgba($neon-yellow, 0.2);
    transform: translateY(-1px);
  }
}

// 默认按钮霓虹hover
:deep(.el-button--default) {
  background: rgba($bg-elevated, 0.6) !important;
  border: 1px solid $border-color !important;
  color: $text-secondary !important;
  transition: all $transition-fast;

  &:hover {
    border-color: rgba($neon-cyan, 0.3) !important;
    color: $neon-cyan !important;
    box-shadow: 0 0 10px rgba($neon-cyan, 0.1);
  }
}

// 状态标签霓虹风格
:deep(.el-tag) {
  border: 1px solid currentColor;
  backdrop-filter: blur(4px);
}

:deep(.el-tag--success) {
  background: rgba($neon-green, 0.1) !important;
  color: $neon-green !important;
  border-color: rgba($neon-green, 0.3) !important;
  box-shadow: 0 0 8px rgba($neon-green, 0.15);
}

:deep(.el-tag--danger) {
  background: rgba($neon-pink, 0.1) !important;
  color: $neon-pink !important;
  border-color: rgba($neon-pink, 0.3) !important;
  box-shadow: 0 0 8px rgba($neon-pink, 0.15);
}

:deep(.el-tag--warning) {
  background: rgba($neon-yellow, 0.1) !important;
  color: $neon-yellow !important;
  border-color: rgba($neon-yellow, 0.3) !important;
  box-shadow: 0 0 8px rgba($neon-yellow, 0.15);
}

:deep(.el-tag--info) {
  background: rgba($neon-cyan, 0.1) !important;
  color: $neon-cyan !important;
  border-color: rgba($neon-cyan, 0.3) !important;
  box-shadow: 0 0 8px rgba($neon-cyan, 0.15);
}

:deep(.el-tag--primary) {
  background: rgba($primary-color, 0.1) !important;
  color: $primary-light !important;
  border-color: rgba($primary-color, 0.3) !important;
  box-shadow: 0 0 8px rgba($primary-color, 0.15);
}

// 搜索框focus时cyan发光
:deep(.el-input__wrapper) {
  background: $bg-input !important;
  border: 1px solid $border-color !important;
  box-shadow: none !important;
  transition: border-color $transition-normal, box-shadow $transition-normal;

  &:focus-within,
  &.is-focus {
    border-color: rgba($neon-cyan, 0.4) !important;
    box-shadow: 0 0 12px rgba($neon-cyan, 0.15) !important;
  }
}

// 分页器赛博风格
:deep(.el-pagination) {
  .el-pager li {
    background: rgba($bg-elevated, 0.5) !important;
    border: 1px solid $border-color !important;
    color: $text-secondary !important;
    border-radius: $border-radius;
    transition: all $transition-fast;

    &:hover {
      color: $neon-cyan !important;
      border-color: rgba($neon-cyan, 0.3) !important;
      box-shadow: 0 0 8px rgba($neon-cyan, 0.1);
    }

    &.is-active {
      background: linear-gradient(135deg, $neon-cyan, $primary-color) !important;
      border-color: transparent !important;
      color: #06080f !important;
      font-weight: 700;
      box-shadow: 0 2px 12px rgba($neon-cyan, 0.4);
    }
  }

  .btn-prev,
  .btn-next {
    background: rgba($bg-elevated, 0.5) !important;
    border: 1px solid $border-color !important;
    color: $text-secondary !important;
    border-radius: $border-radius;
    transition: all $transition-fast;

    &:hover {
      border-color: rgba($neon-cyan, 0.3) !important;
      color: $neon-cyan !important;
    }
  }

  .el-pagination__total {
    color: $text-secondary !important;
  }
}
</style>
