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
  font-size: 24px;
  font-weight: 700;
  color: $text-primary;
  margin: 0;
}

.filter-card {
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
}

:deep(.el-table) {
  --el-table-bg-color: #0f172a;
  --el-table-header-bg-color: #1e293b;
  --el-table-row-hover-bg-color: #334155;
  --el-table-border-color: #334155;
  --el-table-text-color: #f1f5f9;
  --el-table-header-text-color: #94a3b8;
}

:deep(.el-table__row) {
  background-color: #0f172a !important;
}
</style>
