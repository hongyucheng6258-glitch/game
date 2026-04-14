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

  :deep(.el-card__body) {
    padding: $spacing-md;
  }
}

.table-card {
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

  :deep(.el-card__body) {
    padding: 0;
  }
}

.pagination-container {
  padding: $spacing-md;
  display: flex;
  justify-content: flex-end;
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
  --el-table-bg-color: #0f172a;
  --el-table-header-bg-color: #1e293b;
  --el-table-row-hover-bg-color: rgba($primary-color, 0.08);
  --el-table-border-color: rgba(148, 163, 184, 0.06);
  --el-table-text-color: #f1f5f9;
  --el-table-header-text-color: #94a3b8;
  border-radius: $border-radius-lg;
  overflow: hidden;

  .el-table__row--striped {
    background-color: rgba(30, 41, 59, 0.5) !important;
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

// 对话框美化
:deep(.el-dialog) {
  --el-dialog-bg-color: #0f172a !important;
  --el-dialog-border-color: rgba(148, 163, 184, 0.06) !important;
  --el-dialog-title-color: #f1f5f9 !important;
  background-color: $glass-bg !important;
  backdrop-filter: blur($glass-blur);
  -webkit-backdrop-filter: blur($glass-blur);
  border: 1px solid rgba(148, 163, 184, 0.06) !important;
  border-radius: $border-radius-xl !important;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.5), $shadow-glow;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid rgba(148, 163, 184, 0.06) !important;
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
  border-top: 1px solid rgba(148, 163, 184, 0.06) !important;
  background-color: transparent !important;
  padding: $spacing-md $spacing-xl;
}

// 描述列表美化
:deep(.el-descriptions) {
  --el-descriptions-table-border: rgba(148, 163, 184, 0.06) !important;
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
  border-color: rgba(148, 163, 184, 0.06) !important;
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

:deep(.el-button--success) {
  background: linear-gradient(135deg, #22c55e, #16a34a) !important;
  border: none !important;
  box-shadow: 0 2px 8px rgba(34, 197, 94, 0.3);

  &:hover {
    background: linear-gradient(135deg, #4ade80, #22c55e) !important;
    box-shadow: 0 4px 16px rgba(34, 197, 94, 0.4);
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
