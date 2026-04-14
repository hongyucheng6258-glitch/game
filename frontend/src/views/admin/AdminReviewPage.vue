<template>
  <div class="admin-review-page">
    <div class="page-header">
      <h2 class="page-title">评价管理</h2>
    </div>

    <!-- 搜索筛选 -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <el-input v-model="searchKeyword" placeholder="搜索用户名/内容" clearable style="width: 250px" @clear="fetchReviews" @keyup.enter="fetchReviews">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="filterRating" placeholder="评分筛选" clearable style="width: 150px" @change="fetchReviews">
          <el-option label="5星" :value="5" />
          <el-option label="4星" :value="4" />
          <el-option label="3星" :value="3" />
          <el-option label="2星" :value="2" />
          <el-option label="1星" :value="1" />
        </el-select>
        <el-button type="primary" @click="fetchReviews">搜索</el-button>
      </div>
    </el-card>

    <!-- 评价表格 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="reviews" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="用户" width="120">
          <template #default="{ row }">
            <div class="user-cell">
              <UserAvatar :avatar="row.userAvatar" :username="row.userName" :size="28" />
              <span>{{ row.isAnonymous ? '匿名' : (row.userName || '-') }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="serviceId" label="服务ID" width="80" />
        <el-table-column prop="rating" label="评分" width="140">
          <template #default="{ row }">
            <el-rate :model-value="row.rating" disabled :colors="['#f59e0b', '#f59e0b', '#f59e0b']" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.content || '-' }}</template>
        </el-table-column>
        <el-table-column prop="reply" label="回复" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.reply" style="color: #22c55e">{{ row.reply }}</span>
            <span v-else style="color: #64748b">未回复</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="评价时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
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
          @size-change="fetchReviews"
          @current-change="fetchReviews"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getAdminReviews, adminDeleteReview } from '@/api/admin'
import type { Review } from '@/types/review'
import { formatDate } from '@/utils/format'
import UserAvatar from '@/components/business/UserAvatar.vue'

const loading = ref(false)
const reviews = ref<Review[]>([])
const total = ref(0)
const searchKeyword = ref('')
const filterRating = ref<number | undefined>()
const pagination = reactive({ page: 1, size: 10 })

async function fetchReviews() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: pagination.page, size: pagination.size }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterRating.value !== undefined) params.rating = filterRating.value
    const res = await getAdminReviews(params)
    reviews.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

async function handleDelete(review: Review) {
  try {
    await ElMessageBox.confirm('确定要删除该评价吗？此操作不可恢复。', '警告', { type: 'error' })
    await adminDeleteReview(review.id)
    ElMessage.success('删除成功')
    fetchReviews()
  } catch {
    // cancelled
  }
}

onMounted(() => fetchReviews())
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.admin-review-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, $text-primary 0%, $primary-light 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 0.5px;
}

.filter-card {
  background: $glass-bg !important;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid rgba(148, 163, 184, 0.06) !important;
  border-radius: $border-radius-lg !important;
  transition: all $transition-normal;

  &:hover {
    border-color: rgba($primary-color, 0.2) !important;
    box-shadow: $shadow-glow;
  }

  :deep(.el-card__body) { padding: $spacing-md; }
}

.filter-row {
  display: flex;
  gap: $spacing-sm;
  align-items: center;
  flex-wrap: wrap;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: $spacing-md;
  padding-bottom: $spacing-sm;
}

// 卡片毛玻璃效果
:deep(.el-card) {
  background: $glass-bg !important;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid rgba(148, 163, 184, 0.06) !important;
  border-radius: $border-radius-lg !important;
  transition: all $transition-normal;

  &:hover {
    border-color: rgba($primary-color, 0.2) !important;
    box-shadow: $shadow-glow;
  }
}

// 表格暗色电竞风格
:deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: #1e293b;
  --el-table-row-hover-bg-color: rgba($primary-color, 0.08);
  --el-table-border-color: rgba(148, 163, 184, 0.06);
  --el-table-text-color: #f1f5f9;
  --el-table-header-text-color: #94a3b8;
  border-radius: $border-radius-lg;
  overflow: hidden;

  // 暗色行交替
  .el-table__row--striped {
    background-color: rgba(30, 41, 59, 0.4) !important;
  }
}

:deep(.el-table__row) {
  transition: all $transition-fast;

  &:hover {
    background-color: rgba($primary-color, 0.08) !important;
  }
}

:deep(.el-table__header-wrapper) {
  th {
    background-color: #1e293b !important;
    font-weight: 600;
    font-size: 13px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }
}

// 按钮渐变效果
:deep(.el-button--primary) {
  background: linear-gradient(135deg, $primary-color, $primary-dark) !important;
  border: none !important;
  box-shadow: 0 2px 8px rgba($primary-color, 0.3);

  &:hover {
    background: linear-gradient(135deg, $primary-light, $primary-color) !important;
    box-shadow: 0 4px 16px rgba($primary-color, 0.4);
    transform: translateY(-1px);
  }
}

:deep(.el-button--danger) {
  background: linear-gradient(135deg, #ef4444, #dc2626) !important;
  border: none !important;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.3);

  &:hover {
    background: linear-gradient(135deg, #f87171, #ef4444) !important;
    box-shadow: 0 4px 16px rgba(239, 68, 68, 0.4);
    transform: translateY(-1px);
  }
}

// 分页器美化
:deep(.el-pagination) {
  .el-pager li {
    background: rgba(30, 41, 59, 0.6) !important;
    border: 1px solid rgba(148, 163, 184, 0.06) !important;
    color: $text-secondary !important;
    border-radius: $border-radius;
    transition: all $transition-fast;

    &:hover {
      color: $primary-light !important;
      border-color: rgba($primary-color, 0.3) !important;
    }

    &.is-active {
      background: linear-gradient(135deg, $primary-color, $primary-dark) !important;
      border-color: transparent !important;
      color: #fff !important;
      box-shadow: 0 2px 8px rgba($primary-color, 0.4);
    }
  }

  .btn-prev,
  .btn-next {
    background: rgba(30, 41, 59, 0.6) !important;
    border: 1px solid rgba(148, 163, 184, 0.06) !important;
    color: $text-secondary !important;
    border-radius: $border-radius;
  }
}
</style>
