<template>
  <div class="admin-complaint-page">
    <div class="page-header">
      <h2 class="page-title">投诉仲裁管理</h2>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable @change="handleFilter">
            <el-option label="全部" :value="undefined" />
            <el-option
              v-for="(label, value) in COMPLAINT_STATUS_LABELS"
              :key="value"
              :label="label"
              :value="Number(value)"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="complaints" v-loading="loading" stripe>
        <el-table-column prop="id" label="投诉ID" width="100" />
        <el-table-column prop="orderId" label="订单ID" width="100" />
        <el-table-column label="投诉方" width="100">
          <template #default="{ row }">
            {{ row.complainantId }}
          </template>
        </el-table-column>
        <el-table-column label="被投诉方" width="100">
          <template #default="{ row }">
            {{ row.respondentId }}
          </template>
        </el-table-column>
        <el-table-column label="投诉类型" width="140">
          <template #default="{ row }">
            {{ COMPLAINT_TYPE_LABELS[row.type] }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="COMPLAINT_STATUS_TYPES[row.status]">
              {{ COMPLAINT_STATUS_LABELS[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="投诉内容" show-overflow-tooltip />
        <el-table-column label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button
              v-if="row.status === COMPLAINT_STATUS.PENDING || row.status === COMPLAINT_STATUS.PROCESSING"
              link
              type="success"
              @click="handleArbitrate(row)"
            >
              仲裁
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchComplaints"
          @current-change="fetchComplaints"
        />
      </div>
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="投诉详情"
      width="600px"
      destroy-on-close
    >
      <template v-if="currentComplaint">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="投诉ID">{{ currentComplaint.id }}</el-descriptions-item>
          <el-descriptions-item label="订单ID">{{ currentComplaint.orderId }}</el-descriptions-item>
          <el-descriptions-item label="投诉方ID">{{ currentComplaint.complainantId }}</el-descriptions-item>
          <el-descriptions-item label="被投诉方ID">{{ currentComplaint.respondentId }}</el-descriptions-item>
          <el-descriptions-item label="投诉类型">
            {{ COMPLAINT_TYPE_LABELS[currentComplaint.type] }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="COMPLAINT_STATUS_TYPES[currentComplaint.status]">
              {{ COMPLAINT_STATUS_LABELS[currentComplaint.status] }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="投诉内容">
            {{ currentComplaint.content }}
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">
            {{ formatDate(currentComplaint.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentComplaint.arbitrationResult" label="仲裁结果">
            {{ currentComplaint.arbitrationResult }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentComplaint.arbitratedAt" label="仲裁时间">
            {{ formatDate(currentComplaint.arbitratedAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 仲裁对话框 -->
    <el-dialog
      v-model="showArbitrateDialog"
      title="仲裁处理"
      width="500px"
      destroy-on-close
    >
      <el-form ref="arbitrateFormRef" :model="arbitrateForm" :rules="arbitrateRules" label-position="top">
        <el-form-item label="仲裁决定" prop="decision">
          <el-radio-group v-model="arbitrateForm.decision">
            <el-radio :value="0">支持投诉方</el-radio>
            <el-radio :value="1">支持被投诉方</el-radio>
            <el-radio :value="2">双方各承担责任</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="仲裁结果说明" prop="arbitrationResult">
          <el-input
            v-model="arbitrateForm.arbitrationResult"
            type="textarea"
            :rows="4"
            placeholder="请详细说明仲裁结果（不少于10个字符）"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showArbitrateDialog = false">取消</el-button>
        <el-button type="primary" :loading="arbitrating" @click="handleSubmitArbitration">提交仲裁</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  getAdminComplaints,
  arbitrateComplaint,
  type Complaint,
} from '@/api/complaint'
import {
  COMPLAINT_STATUS,
  COMPLAINT_STATUS_LABELS,
  COMPLAINT_STATUS_TYPES,
  COMPLAINT_TYPE_LABELS,
} from '@/utils/constants'
import { formatDate } from '@/utils/format'

const loading = ref(false)
const complaints = ref<Complaint[]>([])
const total = ref(0)

const pagination = reactive({
  page: 1,
  size: 10,
})

const filters = reactive({
  status: undefined as number | undefined,
})

const showDetailDialog = ref(false)
const showArbitrateDialog = ref(false)
const currentComplaint = ref<Complaint | null>(null)
const arbitrating = ref(false)
const arbitrateFormRef = ref<FormInstance>()
const arbitrateForm = reactive({
  complaintId: 0,
  decision: 0,
  arbitrationResult: '',
})

const arbitrateRules: FormRules = {
  decision: [{ required: true, message: '请选择仲裁决定', trigger: 'change' }],
  arbitrationResult: [
    { required: true, message: '请输入仲裁结果说明', trigger: 'blur' },
    { min: 10, message: '仲裁结果说明不能少于10个字符', trigger: 'blur' },
  ],
}

async function fetchComplaints() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: pagination.page,
      size: pagination.size,
    }
    if (filters.status !== undefined) {
      params.status = filters.status
    }
    const res = await getAdminComplaints(params)
    complaints.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function handleFilter() {
  pagination.page = 1
  fetchComplaints()
}

function handleView(complaint: Complaint) {
  currentComplaint.value = complaint
  showDetailDialog.value = true
}

function handleArbitrate(complaint: Complaint) {
  arbitrateForm.complaintId = complaint.id
  arbitrateForm.decision = 0
  arbitrateForm.arbitrationResult = ''
  showArbitrateDialog.value = true
}

async function handleSubmitArbitration() {
  if (!arbitrateFormRef.value) return
  await arbitrateFormRef.value.validate(async (valid) => {
    if (!valid) return

    arbitrating.value = true
    try {
      await arbitrateComplaint({
        complaintId: arbitrateForm.complaintId,
        decision: arbitrateForm.decision,
        arbitrationResult: arbitrateForm.arbitrationResult,
      })
      ElMessage.success('仲裁成功')
      showArbitrateDialog.value = false
      fetchComplaints()
    } catch {
      // handled by interceptor
    } finally {
      arbitrating.value = false
    }
  })
}

onMounted(() => {
  fetchComplaints()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.admin-complaint-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

// 页面标题 - Orbitron字体 + 霓虹渐变
.page-title {
  font-family: 'Orbitron', sans-serif;
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, $neon-cyan 0%, $primary-light 50%, $neon-purple 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 2px;
  text-transform: uppercase;
  position: relative;
  padding-bottom: $spacing-sm;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 60px;
    height: 2px;
    background: linear-gradient(90deg, $neon-cyan, transparent);
    box-shadow: 0 0 8px rgba($neon-cyan, 0.5);
  }
}

// 筛选卡片 - neon-border
.filter-card {
  background: $glass-bg !important;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid $glass-border !important;
  border-radius: $border-radius-lg !important;
  transition: all $transition-normal;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, $neon-cyan, transparent);
    opacity: 0;
    transition: opacity $transition-normal;
  }

  &:hover {
    border-color: $border-glow !important;
    box-shadow: $shadow-neon-cyan, $shadow-glow;

    &::before {
      opacity: 1;
    }
  }

  :deep(.el-card__body) {
    padding: $spacing-md;
  }
}

// 表格卡片 - neon-border
.table-card {
  background: $glass-bg !important;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid $glass-border !important;
  border-radius: $border-radius-lg !important;
  transition: all $transition-normal;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, $neon-cyan, transparent);
    opacity: 0;
    transition: opacity $transition-normal;
    z-index: 1;
  }

  &:hover {
    border-color: $border-glow !important;
    box-shadow: $shadow-neon-cyan, $shadow-glow;

    &::before {
      opacity: 1;
    }
  }

  :deep(.el-card__body) {
    padding: 0;
  }
}

.pagination-container {
  padding: $spacing-md;
  display: flex;
  justify-content: flex-end;
}

// 卡片 - neon-border效果
:deep(.el-card) {
  background: $glass-bg !important;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid $glass-border !important;
  border-radius: $border-radius-lg !important;
  transition: all $transition-normal;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, $neon-cyan, transparent);
    opacity: 0;
    transition: opacity $transition-normal;
    z-index: 1;
  }

  &:hover {
    border-color: $border-glow !important;
    box-shadow: $shadow-neon-cyan, $shadow-glow;

    &::before {
      opacity: 1;
    }
  }
}

// 表格 - 赛博朋克电竞风
:deep(.el-table) {
  --el-table-bg-color: #0f172a;
  --el-table-header-bg-color: transparent;
  --el-table-row-hover-bg-color: rgba($neon-cyan, 0.06);
  --el-table-border-color: rgba(148, 163, 184, 0.06);
  --el-table-text-color: #f1f5f9;
  --el-table-header-text-color: #94a3b8;
  border-radius: $border-radius-lg;
  overflow: hidden;

  .el-table__row--striped {
    background-color: rgba(30, 41, 59, 0.5) !important;
  }
}

// 表头 - 渐变背景 + cyan文字
:deep(.el-table__header-wrapper) {
  th {
    background: linear-gradient(135deg, rgba($neon-cyan, 0.08), rgba($primary-color, 0.08)) !important;
    font-weight: 600;
    font-size: 13px;
    text-transform: uppercase;
    letter-spacing: 1px;
    color: $neon-cyan !important;
    border-bottom: 1px solid rgba($neon-cyan, 0.15) !important;
  }
}

:deep(.el-table__row) {
  transition: all $transition-fast;

  &:hover {
    background-color: rgba($neon-cyan, 0.06) !important;
    box-shadow: inset 0 0 30px rgba($neon-cyan, 0.03);
  }
}

// 搜索框 - focus时cyan发光
:deep(.el-input__wrapper),
:deep(.el-select .el-input__wrapper) {
  background: rgba(15, 23, 42, 0.6) !important;
  border: 1px solid rgba(148, 163, 184, 0.08) !important;
  box-shadow: none !important;
  border-radius: $border-radius;
  transition: all $transition-fast;

  &:focus-within {
    border-color: rgba($neon-cyan, 0.5) !important;
    box-shadow: 0 0 0 2px rgba($neon-cyan, 0.1), 0 0 12px rgba($neon-cyan, 0.15) !important;
  }
}

// 对话框 - 霓虹边框
:deep(.el-dialog) {
  --el-dialog-bg-color: #0f172a !important;
  --el-dialog-border-color: $glass-border !important;
  --el-dialog-title-color: #f1f5f9 !important;
  background-color: $glass-bg !important;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid $glass-border !important;
  border-radius: $border-radius-xl !important;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.5), $shadow-neon-cyan;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, $neon-cyan, $neon-purple, transparent);
    border-radius: $border-radius-xl $border-radius-xl 0 0;
  }
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid rgba($neon-cyan, 0.1) !important;
  background-color: transparent !important;
  padding: $spacing-lg $spacing-xl;
  margin-right: 0;
}

:deep(.el-dialog__body) {
  background-color: transparent !important;
  color: $text-primary !important;
  padding: $spacing-lg $spacing-xl;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid rgba($neon-cyan, 0.1) !important;
  background-color: transparent !important;
  padding: $spacing-md $spacing-xl;
}

// 描述列表 - 霓虹风格
:deep(.el-descriptions) {
  --el-descriptions-table-border: rgba($neon-cyan, 0.08) !important;
  --el-descriptions-item-bordered-label-background: rgba(30, 41, 59, 0.6) !important;
  --el-descriptions-item-bordered-content-background: transparent !important;
  --el-descriptions-item-content-color: #f1f5f9 !important;
  --el-descriptions-item-label-color: #94a3b8 !important;
}

:deep(.el-descriptions__label) {
  color: $text-secondary !important;
  background-color: rgba(30, 41, 59, 0.6) !important;
}

:deep(.el-descriptions__content) {
  color: $text-primary !important;
  background-color: transparent !important;
}

:deep(.el-descriptions__cell) {
  border-color: rgba($neon-cyan, 0.08) !important;
}

// 操作按钮 - 霓虹发光hover
:deep(.el-button--primary) {
  background: linear-gradient(135deg, $primary-color, $primary-dark) !important;
  border: none !important;
  box-shadow: 0 2px 8px rgba($primary-color, 0.3);
  transition: all $transition-normal;

  &:hover {
    background: linear-gradient(135deg, $primary-light, $primary-color) !important;
    box-shadow: 0 0 16px rgba($primary-color, 0.5), 0 4px 16px rgba($primary-color, 0.3);
    transform: translateY(-1px);
  }
}

:deep(.el-button--success) {
  background: linear-gradient(135deg, #22c55e, #16a34a) !important;
  border: none !important;
  box-shadow: 0 2px 8px rgba(34, 197, 94, 0.3);
  transition: all $transition-normal;

  &:hover {
    background: linear-gradient(135deg, #4ade80, #22c55e) !important;
    box-shadow: 0 0 16px rgba(34, 197, 94, 0.5), 0 4px 16px rgba(34, 197, 94, 0.3);
    transform: translateY(-1px);
  }
}

// 状态标签 - 霓虹风格（投诉状态增强）
:deep(.el-tag) {
  border: none;
  font-weight: 500;
  letter-spacing: 0.5px;
}

:deep(.el-tag--success) {
  background: rgba($neon-green, 0.12) !important;
  color: $neon-green !important;
  box-shadow: 0 0 8px rgba($neon-green, 0.2);
}

:deep(.el-tag--danger) {
  background: rgba($neon-pink, 0.15) !important;
  color: $neon-pink !important;
  box-shadow: 0 0 12px rgba($neon-pink, 0.3);
  animation: neon-pink-pulse 2s ease-in-out infinite;
}

:deep(.el-tag--warning) {
  background: rgba($neon-yellow, 0.12) !important;
  color: $neon-yellow !important;
  box-shadow: 0 0 8px rgba($neon-yellow, 0.2);
}

:deep(.el-tag--info) {
  background: rgba($neon-cyan, 0.12) !important;
  color: $neon-cyan !important;
  box-shadow: 0 0 8px rgba($neon-cyan, 0.2);
}

// 紧急投诉 - neon-pink发光脉冲动画
@keyframes neon-pink-pulse {
  0%, 100% {
    box-shadow: 0 0 8px rgba($neon-pink, 0.3);
  }
  50% {
    box-shadow: 0 0 16px rgba($neon-pink, 0.5), 0 0 24px rgba($neon-pink, 0.2);
  }
}

// 分页器 - 赛博朋克风格
:deep(.el-pagination) {
  --el-pagination-bg-color: transparent;
  --el-pagination-text-color: #{$text-secondary};
  --el-pagination-button-bg-color: rgba(30, 41, 59, 0.6);
  --el-pagination-button-color: #{$text-secondary};
  --el-pagination-hover-color: #{$neon-cyan};

  .el-pager li {
    background: rgba(30, 41, 59, 0.6) !important;
    border: 1px solid rgba(148, 163, 184, 0.08) !important;
    color: $text-secondary !important;
    border-radius: $border-radius;
    transition: all $transition-fast;
    font-weight: 500;

    &:hover {
      color: $neon-cyan !important;
      border-color: rgba($neon-cyan, 0.3) !important;
      box-shadow: 0 0 8px rgba($neon-cyan, 0.15);
    }

    &.is-active {
      background: linear-gradient(135deg, rgba($neon-cyan, 0.2), rgba($primary-color, 0.2)) !important;
      border-color: rgba($neon-cyan, 0.4) !important;
      color: $neon-cyan !important;
      box-shadow: 0 0 12px rgba($neon-cyan, 0.3);
    }
  }

  .btn-prev,
  .btn-next {
    background: rgba(30, 41, 59, 0.6) !important;
    border: 1px solid rgba(148, 163, 184, 0.08) !important;
    color: $text-secondary !important;
    border-radius: $border-radius;
    transition: all $transition-fast;

    &:hover {
      color: $neon-cyan !important;
      border-color: rgba($neon-cyan, 0.3) !important;
    }
  }

  .el-pagination__total,
  .el-pagination__sizes {
    color: $text-secondary !important;
  }
}
</style>
